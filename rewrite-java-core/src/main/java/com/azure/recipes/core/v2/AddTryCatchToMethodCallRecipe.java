package com.azure.recipes.core.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.*;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.*;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Add try-catch to method recipe places all calls to methods matching the provided method
 * pattern in a try-catch block based off the template provided.
 * Recipe will attempt to use the JavaParser first, but will also run on code that cannot be
 * parsed by Open Rewrites Java parser.
 * Recipe does not check if the method throws the supplied exception, only that the method is
 * in a suitable try-catch block.
 * If the template is not syntactically correct, the recipe will not make any changes.
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

//    @Option(displayName = "Match on overrides",
//            description = "When enabled, find methods that are overrides of the method pattern.",
//            required = false)
//    @Nullable
//    boolean matchOverrides;

    @Option(displayName = "Catch template",
            description = "The code snippet to be executed in the catch block",
            example = "catch (IOException e) { e.printStackTrace(); }")
    @NonNull
    String catchTemplateString;

    @Option(displayName = "Fully qualified exception name",
            description = "The fully qualified type name for the caught exception",
            example = "java.io.IOException")
    @NonNull
    String fullyQualifiedExceptionName;

    /**
     * All recipes must be serializable. This is verified by RewriteTest.rewriteRun() in your tests.
     * Json creator allows your recipes to be used from a yaml file.
     */
    @JsonCreator
    public AddTryCatchToMethodCallRecipe(@NonNull @JsonProperty("methodPattern") String methodPattern,
//                                         @Nullable @JsonProperty("matchOverrides") boolean matchOverrides,
                                         @NonNull @JsonProperty("catchTemplateString") String catchTemplateString,
                                         @NonNull @JsonProperty("fullyQualifiedExceptionName") String fullyQualifiedExceptionName) {
        this.methodPattern = methodPattern;
//        this.matchOverrides = matchOverrides;
        this.catchTemplateString = catchTemplateString;
        this.fullyQualifiedExceptionName = fullyQualifiedExceptionName;
    }


    @Override
    public @NlsRewrite.DisplayName @NotNull String getDisplayName() {
        return "Add try-catch to method";
    }

    @Override
    public @NlsRewrite.Description @NotNull String getDescription() {
        return "Surrounds calls to the target method in a custom try-catch block.";
    }

    /**
     * Method to return the visitor that performs the checks and changes
     *
     * @return A TreeVisitor to visit the usages of HttpLogOptions and HttpLogDetailLevel
     */
    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AddTryCatchVisitor();
    }

    /**
     * Visitor to change HttpLogOptions type and change usage of HttpLogDetailLevel
     */
    private class AddTryCatchVisitor extends JavaIsoVisitor<ExecutionContext> {

        private final MethodMatcher methodMatcher = new MethodMatcher(methodPattern, true);

        /**
         * Overridden visitBlock method performs the changes to methods filtered by visitMethodCall.
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
            // Get the first statement parent of method
            Statement parent_statement = getCursor().pollMessage("STATEMENT");

            /**
             * Recipe will first attempt to use the JavaParser to generate the new elements needed.
             * This implementation is not completely portable. It would need an option to declare the
             * jar and get the relevant imports at runtime.
             * Note:
             * JavaParser may not be able to resolve azure-ai-translation-text-1.0.0-beta.1.jar and be unable
             * to parse azure-ai-translation-text elements.
             * The parser implementation below is based on openRewrite suggestions and works for the core-1.0.0-beta.1.jar
             * in META-INF/rewrite/classpath but not for azure-ai-translation-text-1.0.0-beta.1.jar.
             * As I am unable to resolve this, I have also added a Parser-free implementation that runs.
             * May throw: java.lang.IllegalArgumentException: Could not parse as Java
             */

//            try {
//                JavaTemplate tryCatchTemplate = JavaTemplate.builder("try{ #{any()}; } " + catchTemplateString)
//                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(context, "azure-ai-translation-text-1.0.0-beta.1.jar"))
//                        .contextSensitive()
//                        .imports(fullyQualifiedExceptionName)
//                        .imports("com.azure.ai.translation.text.models.InputTextItem")
//                        .imports("com.azure.ai.translation.text.models.TranslatedTextItem")
//                        .imports("java.util.Arrays")
//                        .build();
//
//                // Method is a direct statement
//                if (parent == null) {
//                    body = tryCatchTemplate.apply(getCursor(), method.getCoordinates().replace(), method);
//                }
//                else if (parent instanceof J.Assignment) {
//                    J.Assignment assignment = (J.Assignment) parent;
//                    body = tryCatchTemplate.apply(getCursor(), assignment.getCoordinates().replace(), assignment);
//                }
//                else if (parent_statement instanceof J.VariableDeclarations) {
//                    // The Java template is not 100% accurate at creating Assignment statements
//                    // Some manual checking and assignment may be necessary.
//                    // The Java parser is still better than creating the elements by hand.
//
//                    J.VariableDeclarations parent_vd = (J.VariableDeclarations) parent_statement;
//
//                    int parent_index = body.getStatements().indexOf(parent_vd);
//
//                    if (((J.VariableDeclarations) parent_statement).getVariables().size() != 1) {
//                        // Recipe can only handle a variable declaration with a single named variable at this time
//                        return body;
//                    }
//
//                    // Extract the correctly compiled components from the original code
//                    // The named variable that looks like the new assignment we will create
//                    // E.g. from J.VariableDeclarations: "int a = num.getMyInt()" named variable is "a = num.getMyInt()"
//                    J.VariableDeclarations.NamedVariable namedVariable = ((J.VariableDeclarations) parent_statement).getVariables().get(0);
//
//                    // equivalent to "num.getMyInt()"
//                    Expression good_expression = namedVariable.getInitializer();
//
//                    // Apply a template to split the variable assignment into:
//                    // int a = null;
//                    // a = num.getMyInt()
//                    // Note: the second expression may have missing components.
//                    JavaTemplate template1 = JavaTemplate.builder(
//                                    body.getStatements().get(parent_index) + " = null;" + namedVariable)
//                            .javaParser(JavaParser.fromJavaVersion().classpathFromResources(context, "azure-ai-translation-text-1.0.0-beta.1.jar"))
//                            .contextSensitive()
//                            .imports("com.azure.ai.translation.text.models.InputTextItem")
//                            .imports("com.azure.ai.translation.text.models.TranslatedTextItem")
//                            .build();
//
//                    J.Block oldBody = body;
//                    body = template1.apply(updateCursor(body), parent_statement.getCoordinates().replace());
//
//                    if (body.getStatements().size() == oldBody.getStatements().size() + 1) {
//
//                        // Check if the Created assignment matches the working assignment
//                        J.Assignment assignment = (J.Assignment) body.getStatements().get(parent_index + 1);
//                        Expression template_expression = assignment.getAssignment();
//
//                        if (good_expression != null && template_expression != good_expression) {
//                            // If the new expression is faulty, replace the templated expression with the original one
//                            assignment = assignment.withAssignment(good_expression);
//                        }
//                        // Add the try block where the templated expression was
//                        body = tryCatchTemplate.apply(updateCursor(body), assignment.getCoordinates().replace(), assignment);
//                    }
//                } // end else if (parent_statement instanceof J.VariableDeclarations)
//                else {
//                    System.out.println("unhandled method use");
//                }
//                    // Add the import if needed
//                maybeAddImport(fullyQualifiedExceptionName,false);
//                // return the transformed body
//                // System.out.println("Java Parser Successful");
//                return body;
//
//            } catch (Exception ignored) {
//               // System.out.println("COULD NOT PARSE");
//            }

            /**
             * Parser-free implementation:
             * If the parser was not successful, parser cannot be used anywhere that the unresolvable types are
             * present. All components must be manually created and placed.
             * This is NOT recommended by Open Rewrite.
             * This version creates a template for the try-catch block with dummy statements that
             * are correctly formed. These are then altered or copied from to create elements that the parser
             * cannot resolve.
             */

            JavaTemplate tryCatchTemplate = JavaTemplate.builder("try { int a = null; a = 3; } " + catchTemplateString)
                    .imports(fullyQualifiedExceptionName)
                    .build();

            // Create an empty block to apply the try-catch template based off the cursor values from the main body
            // This should create the correct formatting.
            J.Block b = J.Block.createEmptyBlock();
            b = tryCatchTemplate.apply(new Cursor(getCursor(),b), b.getCoordinates().firstStatement());
            int parent_index;

            // Extract the try-catch block and dummy elements
            J.Try _try = (J.Try) b.getStatements().get(0);
            J.VariableDeclarations dummy_varDec = (J.VariableDeclarations) _try.getBody().getStatements().get(0);
            J.Assignment dummy_assignment = (J.Assignment) _try.getBody().getStatements().get(1);

            if (_try.getCatches().isEmpty()) {
                // The catch template was incorrect, recipe is unsafe.
                return body;
            }
            // The original list of statements to alter
            List<Statement> body_statements = body.getStatements();

            // Method is the first element on its line.
            if (parent == null) {
                // Cast method as a statement and update the indentation (prefix)

                Statement method_statement = method.withPrefix(dummy_varDec.getPrefix());
                parent_index = body.getStatements().indexOf(method_statement);
                // Make it the only statement in the try block
                _try = _try.withBody(_try.getBody().withStatements(ListUtils.insert(
                        new ArrayList<>(), method_statement, 0 )));

                // Update the statements
                body_statements.set(parent_index, _try);
            }
            else if (parent instanceof J.Assignment) {
                parent_index = body.getStatements().indexOf(parent);
                J.Assignment new_assignment = ((J.Assignment) parent).withPrefix(dummy_assignment.getPrefix());

                _try = _try.withBody(_try.getBody().withStatements(ListUtils.insert(
                        new ArrayList<>(), new_assignment, 0 )));

                body_statements.set(parent_index, _try);
            }
            else if (parent_statement instanceof J.VariableDeclarations) {

                J.VariableDeclarations parent_vd = (J.VariableDeclarations) parent_statement;
                parent_index = body.getStatements().indexOf(parent_vd);

                if (parent_vd.getVariables().size() != 1) {
                    // Recipe can only handle a variable declaration with a single named variable at this time
                    // Could be changed.
                    return body;
                }

                J.VariableDeclarations.NamedVariable namedVariable = parent_vd.getVariables().get(0);
                Expression expression = namedVariable.getInitializer();

                assert expression != null;
                // Repurpose the dummy_assignment variable
                dummy_assignment = dummy_assignment.withVariable(namedVariable.getName().unwrap());
                dummy_assignment = dummy_assignment.withAssignment(expression);

                _try = _try.withBody(_try.getBody().withStatements(ListUtils.insert(
                       new ArrayList<>(), dummy_assignment, 0 )));

                // Make the original declaration initialise with '= null'
                namedVariable = namedVariable.withInitializer(dummy_varDec.getVariables().get(0).getInitializer());

                parent_vd = parent_vd.withVariables(ListUtils.insert(
                        new ArrayList<>(), namedVariable, 0 ));

                // Replace the old VariableDeclarations
                body_statements.set(parent_index, parent_vd);
                // Add the try below it
                body_statements.add(parent_index +1, _try);

            }
            else {
                // A case I haven't thought of yet
                System.out.println("unhandled method use");
            }

            // Update the body block with the new set of statements and return.
            body = body.withStatements(body_statements);
            // Add the import if needed
            maybeAddImport(fullyQualifiedExceptionName,false);
            return body;
        }

        /**
         * Method to find method calls that need to be wrapped
         */
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

                    // Make no changes if exception already caught
                    if (isCaught) {
                        return visitSuper.get();
                    }
                }
            } catch (IllegalStateException ignored) {}
            // If the method matches and exception is not caught set messages for block
            getCursor().putMessageOnFirstEnclosing(J.Block.class, "METHOD", methodCall);

            Tree parent = getCursor().getParentTreeCursor().getValue();
            if (! (parent instanceof J.Block)) {
                // If the method is part of a nested statement flag the direct tree parent
                getCursor().putMessageOnFirstEnclosing(J.Block.class, "PARENT", parent);
                try {
                    // And the first parent that is a statement
                    Statement statement = getCursor().dropParentUntil(it -> it instanceof Statement).getValue();
                    getCursor().putMessageOnFirstEnclosing(J.Block.class, "STATEMENT", statement);
                } catch (IllegalStateException ignored) {}
            }

            return visitSuper.get();
        }

        /**
         * The Suppliers that traverse the LST and redirect all types of method calls through visitMethodCall.
         */

        @Override
        public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext context) {
            return visitMethodCall(newClass, () -> super.visitNewClass(newClass, context));
        }

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
            return visitMethodCall(method, () -> super.visitMethodInvocation(method, context));
        }

        @Override
        public J.MemberReference visitMemberReference(J.MemberReference memberRef, ExecutionContext context) {
            return visitMethodCall(memberRef, () -> super.visitMemberReference(memberRef, context));
        }

    } // end catchUncheckedVisitor

}
