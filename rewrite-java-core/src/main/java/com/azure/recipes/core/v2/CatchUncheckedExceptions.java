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

import java.util.Objects;
import java.util.function.Supplier;
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
                "try{#{any(Object)};} "    //#{}  catch (IOException e) { e.printStackTrace(); }
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
         * The Suppliers that traverse the LST and find calls to be removed.
         */
        @Override
        public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext context) {
            System.out.println("visitNewClass");
            return visitMethodCall(newClass, () -> super.visitNewClass(newClass, context));
        }

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
            System.out.println("visitMethodInvocation");
            return visitMethodCall(method, () -> super.visitMethodInvocation(method, context));
        }

        @Override
        public J.MemberReference visitMemberReference(J.MemberReference memberRef, ExecutionContext context) {
            System.out.println("visitMemberReference");
            return visitMethodCall(memberRef, () -> super.visitMemberReference(memberRef, context));
        }


        private <M extends MethodCall> @Nullable M visitMethodCall(M methodCall, Supplier<M> visitSuper) {
            if (!methodMatcher.matches(methodCall)) {
                // Make no changes
                return visitSuper.get();
            }

            // If match found, check that it is not already handled by a try block
            try {
                // Get the first upstream try block. Will throw exception if there are none
                J.Try _try = getCursor().dropParentUntil(it -> it instanceof J.Try).getValue();

                // Get the first enclosing block
                J.Block block = getCursor().dropParentUntil(it -> it instanceof J.Block).getValue();

                // Check to see if this try block is the parent of the enclosing block
                if (_try.getBody().equals(block)) {
                    // Check if the correct exception is caught
                    boolean isCaught = _try.getCatches().stream().anyMatch(
                            _catch -> Objects.requireNonNull(_catch.getParameter().getType())
                                    .isAssignableFrom(Pattern.compile(fullyQualifiedExceptionName)));
                    if (isCaught) {
                        System.out.println("is caught");
                        // Make no changes if exception already caught
                        return visitSuper.get();
                    }
                }
            } catch (IllegalStateException e) {
                //return visitSuper.get();
            }
            // If the method matches and exception is not caught set messages for block
            getCursor().putMessageOnFirstEnclosing(J.Block.class, "METHOD", methodCall);
            // It's easier to id nested elements from below
            Tree parent = getCursor().getParentTreeCursor().getValue();
            if (! (parent instanceof J.Block)) {
                // If the method is part of a nested statement flag the direct tree parent
                getCursor().putMessageOnFirstEnclosing(J.Block.class, "PARENT", parent);
                try {
                    // And the first parent that is a statement
                    Statement statement = getCursor().dropParentUntil(it -> it instanceof Statement).getValue();
                    getCursor().putMessageOnFirstEnclosing(J.Block.class, "STATEMENT", statement);
                    System.out.println("statement: "+ statement);
                } catch (IllegalStateException ignored) {}
            }
            //getCursor().putMessageOnFirstEnclosing(J.Block.class, "FOUND_PARENT", parent);
//            System.out.println("parent is " + getCursor().getParentTreeCursor().getValue());
//            System.out.println("parent is " + getCursor().getParentTreeCursor().getParentTreeCursor().getValue());
//            System.out.println("parent is " + getCursor().getParentTreeCursor().getParentTreeCursor().getParentTreeCursor().getValue());

            // Remove the method from its original location.
            return null;
        }


        /**
         * Recieves the Method calls and performs checks and alterations
         */
        @Override
        public J.Block visitBlock(J.Block block, ExecutionContext context) {
            J.Block body = super.visitBlock(block, context);

            // Get the method that needs to be changed
            MethodCall method = getCursor().pollMessage("METHOD");
            if (method == null) {
                return body;
            }

            //Get the parents of the method
            Tree parent = getCursor().pollMessage("PARENT");
            Statement parent_statement = getCursor().pollMessage("STATEMENT");
            //String tryStatement;
            //JavaCoordinates coordinates;
            if (parent == null) {
                // Method is the statement
                body = fullTemplate.apply(getCursor(), method.getCoordinates().replace(), method.toString());
                return body;
                //tryStatement = method.toString();
                //coordinates = method.getCoordinates().replace();
            }

            if (parent_statement == null) {
                // Not at all sure when the parent_statement would be null but the parent wouldn't
                body = fullTemplate.apply(getCursor(), method.getCoordinates().replace(), parent.toString());
                return body;
            }

            body = fullTemplate.apply(updateCursor(body), parent_statement.getCoordinates().after(), parent.toString());
//
            return body;
        }

        /*
        @Override
        public J.Block visitBlock(J.Block block, ExecutionContext context) {
            J.Block body = super.visitBlock(block, context);

            // Get the matching method
            MethodCall method = getCursor().pollMessage("FOUND_BLOCK");
            if (method == null) {
                return body;
            }

            //Statement methodStatement;
            ;


            //Check if handled in try
            if((getCursor().getParentTreeCursor().getValue() instanceof J.Try)) {
                // Check that try is being handled

                return body;
            }

            //Expression methodExpression;
            //Get the parent of the method
            Tree parent = getCursor().pollMessage("METHOD_PARENT");
            if (parent == null)
                return body; // Error with parsing

            if (parent instanceof J.Block) {
                J.Block parentBlock = (J.Block) parent;
                System.out.println("method parent = block");
                Statement s = visitStatement((Statement) method, context);
                body = fullTemplate.apply(getCursor(), s.getCoordinates().replace(), s.toString());
                //m = method;
            } else if (parent instanceof Statement) {
                System.out.println("method parent = statement");
                Statement s = visitStatement((Statement) parent, context);
                body = fullTemplate.apply(getCursor(), s.getCoordinates().replace(), s.toString());

            } else if (parent instanceof J.MemberReference) {
                System.out.println("method parent = memberReference");
                J.MemberReference m = visitMemberReference((J.MemberReference) parent, context);
                body = fullTemplate.apply(getCursor(), m.getCoordinates().replace(), m.toString());
            } else if (parent instanceof J.VariableDeclarations.NamedVariable) {
                System.out.println("method parent = variableDeclarations.NamedVariable");
                J.VariableDeclarations.NamedVariable m = visitVariable((J.VariableDeclarations.NamedVariable) parent, context);
                //J.VariableDeclarations d = m.getInitializer();
                //System.out.println("d:"+m.getVariableType());
                JavaCoordinates coordinates = null;
                body = body.withStatements(ListUtils.map(
                    body.getStatements(), statement -> {
                        System.out.println("statement: " + statement.toString());
                        if (statement instanceof J.VariableDeclarations) {
                            if (((J.VariableDeclarations) statement).getVariables().contains(m)) {
                                System.out.println("method matched in list Utils");

                                return null;
                            }
                        }
                        return statement;
                    }
            ));
                System.out.println(body.getStatements().toString());
                //assert m.getInitializer() != null;
                body = fullTemplate.apply(updateCursor(body), body.getCoordinates().lastStatement(), m.toString());
            } else {
                System.out.println("method parent not block or statement");
                System.out.println("method parent:" + parent.toString());
                //m = visitExpression((Expression) parent, context);
            }


            System.out.println("Parent: " +parent);

            //System.out.println("methodStatement.toString():" + m);
            // Template will re-add method if null before
            //body = fullTemplate.apply(getCursor(), methodStatement.getCoordinates().replace(), methodStatement.toString());
           // body = fullTemplate.apply(getCursor(),),

            System.out.println(TreeVisitingPrinter.printTree(getCursor()));
            //body.getStatements().remove(method);
//            body = body.withStatements(ListUtils.map(
//                    body.getStatements(), statement -> {
//                        System.out.println("statement: " + statement.toString());
//                       // if(statement.equals(m)){
//                            return null;
//                       // }
////                        if (statement instanceof MethodCall) {
////                            if (methodMatcher.matches(((MethodCall) statement).getMethodType())) {
////                                System.out.println("method matched in list Utils");
////                                return null;
////                            }
////                        }
//                       // return statement;
//                    }
//            ));


            return body;
        }
         */
        // J.Try _try = getCursor().pollMessage("FOUND_TRY");
        //if (_try != null && _try.getBody().getStatements().contains(method)) return body;
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


        /**
         * Find the method
         *
         */
//        @Override
//        public J.MethodInvocation visitMethodInvocation(J.@NotNull MethodInvocation method, @NotNull ExecutionContext context) {
//            //J.MethodInvocation method = super.visitMethodInvocation(methodInvocation, context);
//            // If the method matches
//            if (!methodMatcher.matches(method)) return method;
//
//            // If the parent is a try block
//            // Only working for method invocation
//            Tree temp = getCursor().getParentTreeCursor().getParentTreeCursor().getValue();
//            System.out.println(temp);
//            if (temp instanceof J.Try) {
//                System.out.println("temp == Try");
////            System.out.println(getCursor().getParent().getValue().toString());
//                J.Try _try = (J.Try) temp;
//                if (_try.getBody().getStatements().contains(method)) {
//                    if (_try.getCatches().stream().anyMatch(
//                            _catch -> Objects.requireNonNull(_catch.getParameter().getType()).isAssignableFrom(
//                                    Pattern.compile(fullyQualifiedExceptionName)))) {
//                        System.out.println("Exception caught in try");
//                        getCursor().putMessageOnFirstEnclosing(J.Block.class, "FOUND_TRY", _try);
//                        return method;
//                    }
//                }
//            }
//            getCursor().putMessageOnFirstEnclosing(J.Block.class, "FOUND_BLOCK", method);
//            getCursor().putMessageOnFirstEnclosing(J.Try.class, "FOUND_METHOD", method);
//
//            return method;
//        } // end methodVisitor


    } // end catchUncheckedVisitor

}
