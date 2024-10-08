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
import org.openrewrite.java.tree.*;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Objects;
import java.util.function.Supplier;

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
public class AddTryCatchToMethodCallRecipe extends Recipe {

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
    public AddTryCatchToMethodCallRecipe(@NonNull @JsonProperty("methodPattern") String methodPattern,
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


        JavaTemplate template = JavaTemplate.builder("try{ #{any()}; } catch (IOException e) { e.printStackTrace(); }") //"+ returnType +"
                .contextSensitive()
                .imports(fullyQualifiedExceptionName)
                .build();

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
                    System.out.println("toString:" + _try.getCatches().get(0).getParameter().toString());

                    // Only works if import present
//                    boolean isCaught = _try.getCatches().stream().anyMatch(
//                            _catch -> Objects.requireNonNull(_catch.getParameter().getType())
//                                    .isAssignableFrom(Pattern.compile(fullyQualifiedExceptionName)));
                   boolean isCaught = _try.getCatches().stream().anyMatch(
                            _catch -> _catch.getParameter().toString().contains("IOException"));

//                    );
                    if (isCaught) {
                        System.out.println("is caught");
                        // Make no changes if exception already caught
                        return visitSuper.get();
                    }
                }
            } catch (IllegalStateException e) {
                System.out.println("no Try");
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
                    System.out.println("STATEMENT: "+ statement);
                } catch (IllegalStateException ignored) {}
            }

            // Remove the method from its original location.
            return null;
        }


        /**
         * Recieves the Method calls and performs checks and alterations
         */
        @Override
        public J.@NotNull Block visitBlock(J.@NotNull Block block, @NotNull ExecutionContext context) {
            J.Block body = super.visitBlock(block, context);

            // Get the method that needs to be changed
            MethodCall method = getCursor().pollMessage("METHOD");
            if (method == null) {
                return body;
            }


            //Get the parents of the method
            Tree parent = getCursor().pollMessage("PARENT");
            System.out.println("parent: "+ parent);
// Get the first statement parent of method
            Statement parent_statement = getCursor().pollMessage("STATEMENT");
            System.out.println("statement_parent:" + parent_statement);
            //System.out.println("parent:" + parent);
            //System.out.println(TreeVisitingPrinter.printTree(getCursor()));
            // Method is a direct statement
            if (parent == null) {
                // Passing tests
                //System.out.println("one");
                body = template.apply(getCursor(), method.getCoordinates().replace(), method);
                //return body;
            }
            else if (parent instanceof J.Assignment) {
                J.Assignment assignment = (J.Assignment) parent;
                body = template.apply(getCursor(), assignment.getCoordinates().replace(), assignment);
            }
            else if (parent_statement instanceof J.VariableDeclarations) {
                // The Java template is not 100% accurate at creating Assignment statements
                // Some manual checking and assignment may be necessary.
                // The Java parser is still better than creating the elements by hand.

                J.VariableDeclarations parent_vd = (J.VariableDeclarations) parent_statement;
                // Get the index of the statement containing the method.
                int parent_index = body.getStatements().indexOf(parent_vd);

                if (((J.VariableDeclarations) parent_statement).getVariables().size() != 1) {
                    // Recipe can only handle a single named Variable
                    // TODO account for multi variables.
                    body = template.apply(getCursor(), parent_statement.getCoordinates().replace(), parent);
                    return body;
                }

                // Extract the fully and correctly compiled components from the original code
                // The named variable that looks like the new assignment we will create
                // E.g. from J.VariableDeclarations: "int a = num.getMyInt()" named variable is "a = num.getMyInt()"
                J.VariableDeclarations.NamedVariable namedVariable = ((J.VariableDeclarations) parent_statement).getVariables().get(0);
                //System.out.println("named_variable: " + namedVariable);

                // equivalent to "num.getMyInt()"
                Expression good_expression = namedVariable.getInitializer();
                //System.out.println("expression: " + expression);

                // Apply a template to split the variable assignment into: 'int a = null; \n 'a = num.getMyInt()'
                // Note: the second expression may have missing components.
                JavaTemplate template1 = JavaTemplate.builder(body.getStatements().get(parent_index) +
                        " = null;" + namedVariable)
                        .contextSensitive()
                        .imports(Objects.requireNonNull(method.getMethodType()).getDeclaringType().getFullyQualifiedName())
                        .build();


                body = template1.apply(updateCursor(body), parent_statement.getCoordinates().replace());

                //TODO add length checks

                // Check if the Created assignment matches the working assignment
                J.Assignment assignment = (J.Assignment) body.getStatements().get(parent_index + 1);
                //System.out.println("assignment: " + assignment);

                Expression template_expression = assignment.getAssignment();

                if (good_expression!= null && template_expression != good_expression) {
                    // Replace the templated expression with the original one
                    assignment = assignment.withAssignment(good_expression);
                    //System.out.println("expressions not equal");
                }
                // Add the try block where the templated expression was
                body = template.apply(updateCursor(body), assignment.getCoordinates().replace(), assignment);
            }
            else {
                System.out.println("unhandled method use");
            }

            maybeAddImport(fullyQualifiedExceptionName,false);
            return body;
        }
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

    } // end catchUncheckedVisitor

}
