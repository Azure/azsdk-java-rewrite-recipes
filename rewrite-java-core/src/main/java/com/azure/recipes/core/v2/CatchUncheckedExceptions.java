package com.azure.recipes.core.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.*;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.TreeVisitingPrinter;
import org.openrewrite.java.tree.J;

import lombok.EqualsAndHashCode;
import lombok.Value;
import java.io.IOException;
import java.lang.reflect.Method;

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
            example = "e.printStackTrace(); return;")
    @NonNull
    String catchTemplateString;

    /**
     * All recipes must be serializable. This is verified by RewriteTest.rewriteRun() in your tests.
     * Json creator allows your recipes to be used from a yaml file.
     */
    @JsonCreator
    public CatchUncheckedExceptions(@NonNull @JsonProperty("methodPattern") String methodPattern,
                                    @Nullable @JsonProperty("matchOverrides") Boolean matchOverrides,
                                    @NonNull @JsonProperty("catchTemplateString") String catchTemplateString)
    {
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

        private JavaTemplate catchTemplate = JavaTemplate.builder(catchTemplateString)
                .contextSensitive()
                .build();
        /*
        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(context, "core-1.0.0-beta.1"))
                    .contextSensitive()
                    .imports("io.clientcore.core.http.models.HttpRedirectOptions")

         */
        @Override
        public J.@NotNull ClassDeclaration visitClassDeclaration(J.@NotNull ClassDeclaration classDeclaration, @NotNull ExecutionContext ctx) {
            J.ClassDeclaration cd = super.visitClassDeclaration(classDeclaration, ctx);
            System.out.println(TreeVisitingPrinter.printTree(getCursor()));

            // Return unchanged if method is not found in this class.
            J.MethodDeclaration methodDeclaration = getCursor().pollMessage("FOUND_METHOD");
            if (methodDeclaration == null) { return cd; }


            // Copy and remove the method instance

            // Add appropriate try-catch block

            // With method call inside
                // Include variable naming

            // maybe add import
            return cd;
        }

        /**
         * Visitor methods to find and label method uses.
         */

        // Maybe methodCall visitor??


        @Override
        public J.@NotNull MethodInvocation visitMethodInvocation(J.@NotNull MethodInvocation methodInvocation, @NotNull ExecutionContext context) {
            J.MethodInvocation method = super.visitMethodInvocation(methodInvocation, context);
            // If the method matches, add a message to the declaring class
            if (methodMatcher.matches(method)) {
                getCursor().putMessageOnFirstEnclosing(J.ClassDeclaration.class, "FOUND_METHOD", method);
            }
            return method;
        }

        @Override
        public J.@NotNull MemberReference visitMemberReference(J.@NotNull MemberReference memberRef, @NotNull ExecutionContext context) {
            J.MemberReference member = super.visitMemberReference(memberRef, context);
            // If the method matches, add a message to the declaring class
            if (methodMatcher.matches(member)) {
                getCursor().putMessageOnFirstEnclosing(J.ClassDeclaration.class, "FOUND_METHOD", member);
            }
            return member;
        }

        @Override
        public J.@NotNull NewClass visitNewClass(J.@NotNull NewClass newClass, @NotNull ExecutionContext context) {
            J.NewClass nc = super.visitNewClass(newClass, context);
            // If the method matches, add a message to the declaring class
            if (methodMatcher.matches(nc)) {
                getCursor().putMessageOnFirstEnclosing(J.ClassDeclaration.class, "FOUND_METHOD", nc);
            }
            return nc;
        }
    }

    /*
    TODO
    find method invocation
    find throws
    find parent
    find if in try
    find if catch handles it

    add try to parent
    add method invocation to try
    add catch block to try with appropriate exception

    if try and catch already exists, add exception to catch

     */
}

/*
https://github.com/openrewrite/rewrite-static-analysis/blob/main/src/main/java/org/openrewrite/staticanalysis/UnnecessaryCatch.java

@Override
            public J.Block visitBlock(J.Block block, ExecutionContext ctx) {
                J.Block b = super.visitBlock(block, ctx);
                return b.withStatements(ListUtils.flatMap(b.getStatements(), statement -> {
                    if (statement instanceof J.Try) {
                        // if a try has no catches, no finally, and no resources get rid of it and merge its statements into the current block
                        J.Try aTry = (J.Try) statement;
                        if (aTry.getCatches().isEmpty() && aTry.getResources() == null && aTry.getFinally() == null) {
                            return ListUtils.map(aTry.getBody().getStatements(), tryStat -> autoFormat(tryStat, ctx, getCursor()));
                        }
                    }
                    return statement;
                }));
            }


List<JavaType.FullyQualified> thrownExceptions = new ArrayList<>();
                AtomicBoolean missingTypeInformation = new AtomicBoolean(false);
                //Collect any checked exceptions thrown from the try block.
                new JavaIsoVisitor<Integer>() {
                    @Override
                    public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, Integer integer) {
                        JavaType.Method methodType = method.getMethodType();
                        if (methodType == null) {
                            //Do not make any changes if there is missing type information.
                            missingTypeInformation.set(true);
                        } else {
                            thrownExceptions.addAll(methodType.getThrownExceptions());
                        }
                        return super.visitMethodInvocation(method, integer);
                    }
                }.visit(t.getBody(), 0);

                //If there is any missing type information, it is not safe to make any transformations.
                if (missingTypeInformation.get()) {
                    return t;
                }

 */