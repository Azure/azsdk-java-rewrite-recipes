package com.azure.recipes.core.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.*;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.TreeVisitingPrinter;
import org.openrewrite.java.tree.*;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 1. Find target method invocation and thrown exception
 * 2. Find parent block
 * 3. If call is in declaration, split to two lines.
 * 4. Use java template to add try catch block before invocation
 * 5. Remove invocation.
 * 6. Add invocation to try body.
 * 7. Add exception to catch block.
 * 8. Add action to catch block.
 * @author Annabelle Mittendorf Smith
 */

@Value
@EqualsAndHashCode(callSuper = false)
public class CatchUncheckedExceptions extends Recipe {

    @Option(displayName = "Method pattern",
            description = "A method pattern used to find matching method declaration.",
            example = "*..* hello(..)")
    @NonNull
    String methodPattern;

    @Option(displayName = "Match on overrides",
            description = "When enabled, find methods that are overrides of the method pattern.",
            required = false)
    @Nullable
    Boolean matchOverrides;

    @Option(displayName = "Catch e template",
            description = "The code snippet to be executed in the catch block",
            example = "catch (IOException e) { e.printStackTrace(); }")
    @NonNull
    String catchTemplateString;

    /**
     * All recipes must be serializable. This is verified by RewriteTest.rewriteRun() in your tests.
     * Json creator allows your recipes to be used from a yaml file.
     */
    @JsonCreator
    public CatchUncheckedExceptions(@NonNull @JsonProperty("methodPattern") String methodPattern,
                                    @Nullable @JsonProperty("matchOverrides") Boolean matchOverrides,
                                    @NonNull @JsonProperty("catchTemplateString") String catchTemplateString) {
        this.methodPattern = methodPattern;
        this.matchOverrides = matchOverrides;
        this.catchTemplateString = catchTemplateString;
    }


    @Override
    public @NlsRewrite.DisplayName @NotNull String getDisplayName() {
        return "Catch unchecked exceptions";
    }

    @Override
    public @NlsRewrite.Description @NotNull String getDescription() {
        return "Surround any unchecked exceptions thrown by methods called in a try catch block.";
    }

    /**
     * Method to return the visitor that visits the usages of HttpLogOptions and HttpLogDetailLevel
     *
     * @return A TreeVisitor to visit the usages of HttpLogOptions and HttpLogDetailLevel
     */
    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new CatchUncheckedExceptionsVisitor();
    }


    /**
     * Visitor to change HttpLogOptions type and change usage of HttpLogDetailLevel
     */
    private class CatchUncheckedExceptionsVisitor extends JavaIsoVisitor<ExecutionContext> {

        private final MethodMatcher methodMatcher = new MethodMatcher(methodPattern, matchOverrides);

        private String fullyQualifiedExceptionName = "java.io.IOException";

        private final JavaTemplate tryBlocKTemplate = JavaTemplate.builder(
                "try{#{};} "    //#{}  catch (IOException e) { e.printStackTrace(); }
        ).contextSensitive().build();

        private final JavaTemplate catchTemplate = JavaTemplate.builder(catchTemplateString)
                //.contextSensitive()
                .build();
        //private final JavaTemplate catchTemplate

        private final JavaTemplate fullTemplate = JavaTemplate.builder("try{ #{}; } catch (IOException e) { e.printStackTrace(); }").contextSensitive().build();
        /*
        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(context, "core-1.0.0-beta.1"))
                    .contextSensitive()
                    .imports("io.clientcore.core.http.models.HttpRedirectOptions")

         */


        /**
         * perform any changes in the containing code block
         *
         * @param block
         * @param context
         * @return
         */
        @Override
        public J.Block visitBlock(J.Block block, ExecutionContext context) {
            J.Block body = super.visitBlock(block, context);
            J.MethodInvocation method = getCursor().pollMessage("FOUND_BLOCK");
            if (method == null) return body;

            J.Try _try = getCursor().pollMessage("FOUND_TRY");
            if (_try != null && _try.getBody().getStatements().contains(method)) return body;
            //if (!body.getStatements().contains(method)) return body;

//            Tree temp =  getCursor().getParentTreeCursor().getValue();
//            System.out.println(temp);
//            if (temp instanceof J.Try) {
//                System.out.println("temp == Try");
//                //J.Try _try = getCursor().getParentTreeCursor().pollMessage("FOUND_TRY");
////            System.out.println(getCursor().getParent().getValue().toString());
//                J.Try _try = (J.Try) temp;
//                if (_try.getBody().getStatements().contains(method)) {
//                    if (_try.getCatches().stream().anyMatch(
//                            _catch -> Objects.requireNonNull(_catch.getParameter().getType()).isAssignableFrom(
//                                    Pattern.compile(fullyQualifiedExceptionName)))) {
//                        System.out.println("Exception caught in try");
//                        return body;
//                    }
//                }
//            }
//

            // Else put method into a try catch block

            System.out.println("apply template");
            body = fullTemplate.apply(getCursor(), method.getCoordinates().after(), method.toString());
            //body.getStatements().remove(method);
            body = body.withStatements(ListUtils.map(
                    body.getStatements(), statement -> {
                        if (statement instanceof J.MethodInvocation) {
                            if (methodMatcher.matches(((J.MethodInvocation) statement).getMethodType()))
                                return null;
                        }
                        return statement;
                    }
            ));


            return body;
        }


        /**
         * Find the method
         *
         */
        @Override
        public J.MethodInvocation visitMethodInvocation(J.@NotNull MethodInvocation method, @NotNull ExecutionContext context) {
            //J.MethodInvocation method = super.visitMethodInvocation(methodInvocation, context);
            // If the method matches
            if (!methodMatcher.matches(method)) return method;

            // If the parent is a try block
            // Only working for method invocation
            Tree temp = getCursor().getParentTreeCursor().getParentTreeCursor().getValue();
            System.out.println(temp);
            if (temp instanceof J.Try) {
                System.out.println("temp == Try");
//            System.out.println(getCursor().getParent().getValue().toString());
                J.Try _try = (J.Try) temp;
                if (_try.getBody().getStatements().contains(method)) {
                    if (_try.getCatches().stream().anyMatch(
                            _catch -> Objects.requireNonNull(_catch.getParameter().getType()).isAssignableFrom(
                                    Pattern.compile(fullyQualifiedExceptionName)))) {
                        System.out.println("Exception caught in try");
                        getCursor().putMessageOnFirstEnclosing(J.Block.class, "FOUND_TRY", _try);
                        return method;
                    }
                }
            }
            getCursor().putMessageOnFirstEnclosing(J.Block.class, "FOUND_BLOCK", method);
            getCursor().putMessageOnFirstEnclosing(J.Try.class, "FOUND_METHOD", method);

            return method;
        } // end methodVisitor
    } // end catchUncheckedVisitor

}
