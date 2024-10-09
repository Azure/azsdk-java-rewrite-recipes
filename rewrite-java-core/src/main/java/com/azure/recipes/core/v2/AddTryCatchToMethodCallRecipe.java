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
 * TODO Add Options
 * TODO Add recipe
 * TODO Add Real Use Test
 * TODO Tidy code
 * TODO add documentation
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

        private final MethodMatcher methodMatcher = new MethodMatcher(methodPattern, true);

        /**
         * Receives the Method calls and performs checks and alterations
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

            try {       // Attempt to parse with the java parser
                // Parser may throw java.lang.IllegalArgumentException: Could not parse as Java
                // JavaParser may not resolve azure-ai-translation-text-1.0.0-beta.1.jar and be unable
                // to parse azure-ai-translation-text elements.
                JavaTemplate tryCatchTemplate = JavaTemplate.builder("try{ #{any()}; } " + catchTemplateString)
                        //.javaParser(JavaParser.fromJavaVersion().classpathFromResources(context, "azure-ai-translation-text-1.0.0-beta.1.jar"))
                        .contextSensitive()
                        .imports(fullyQualifiedExceptionName)
                        //.imports("com.azure.ai.translation.text.models.InputTextItem")//.imports(com.azure.ai.translation.text.models)//Objects.requireNonNull(method.getMethodType()).getDeclaringType().getFullyQualifiedName()
                        //.imports("com.azure.ai.translation.text.models.TranslatedTextItem")
                        //.imports("java.util.Arrays")
                        .build();

                // Method is a direct statement
                if (parent == null) {
                    body = tryCatchTemplate.apply(getCursor(), method.getCoordinates().replace(), method);
                }
                else if (parent instanceof J.Assignment) {
                    J.Assignment assignment = (J.Assignment) parent;
                    body = tryCatchTemplate.apply(getCursor(), assignment.getCoordinates().replace(), assignment);
                }
                else if (parent_statement instanceof J.VariableDeclarations) { // TODO parent statement not guaranteed to be top statement
                    // The Java template is not 100% accurate at creating Assignment statements
                    // Some manual checking and assignment may be necessary.
                    // The Java parser is still better than creating the elements by hand.

                    J.VariableDeclarations parent_vd = (J.VariableDeclarations) parent_statement;

                    int parent_index = body.getStatements().indexOf(parent_vd);

                    if (((J.VariableDeclarations) parent_statement).getVariables().size() != 1) {
                        // Recipe can only handle a variable declaration with a single named variable at this time
                        return body;
                    }

                    // Extract the correctly compiled components from the original code
                    // The named variable that looks like the new assignment we will create
                    // E.g. from J.VariableDeclarations: "int a = num.getMyInt()" named variable is "a = num.getMyInt()"
                    J.VariableDeclarations.NamedVariable namedVariable = ((J.VariableDeclarations) parent_statement).getVariables().get(0);

                    // equivalent to "num.getMyInt()"
                    Expression good_expression = namedVariable.getInitializer();

                    // Apply a template to split the variable assignment into:
                    // int a = null;
                    // a = num.getMyInt()
                    // Note: the second expression may have missing components.
                    JavaTemplate template1 = JavaTemplate.builder(
                                    body.getStatements().get(parent_index) + " = null;" + namedVariable)
                            //.javaParser(JavaParser.fromJavaVersion().classpathFromResources(context, "azure-ai-translation-text-1.0.0-beta.1.jar"))
                            .contextSensitive()
                            //.imports("com.azure.ai.translation.text.models.InputTextItem")
                            //.imports("com.azure.ai.translation.text.models.TranslatedTextItem")
                            // For now manually add imports and resource files
                            .build();

                    J.Block oldBody = body;
                    body = template1.apply(updateCursor(body), parent_statement.getCoordinates().replace());

                    if (body.getStatements().size() == oldBody.getStatements().size() + 1) {

                        // Check if the Created assignment matches the working assignment
                        J.Assignment assignment = (J.Assignment) body.getStatements().get(parent_index + 1);
                        Expression template_expression = assignment.getAssignment();

                        if (good_expression != null && template_expression != good_expression) {
                            // If the new expression is faulty, replace the templated expression with the original one
                            assignment = assignment.withAssignment(good_expression);
                        }
                        // Add the try block where the templated expression was
                        body = tryCatchTemplate.apply(updateCursor(body), assignment.getCoordinates().replace(), assignment);
                    }
                } // end else if (parent_statement instanceof J.VariableDeclarations)
                else {
                    System.out.println("unhandled method use");
                }
                    // Add the import if needed
                maybeAddImport(fullyQualifiedExceptionName,false);
                // return the transformed body
                System.out.println("Java Parser Successful");
                return body;

            } catch (Exception ignored) {
                System.out.println("COULD NOT PARSE");
            }

            // If the parser was not successful, components must be manually created.
            // This can lead to formatting issues, is not as flexible, and is more error-prone.

            // Create a template for the try-catch block with dummy Tree elements to copy.
            // These will hopefully give us correct not tree elements to copy from the context.
            // "int a = null; try { a = 3; } "
            JavaTemplate tryCatchTemplate = JavaTemplate.builder("try { int a = null; a = 3; } " + catchTemplateString)
                    .imports(fullyQualifiedExceptionName)
                    .build();

            // Method is a direct statement
            if (parent == null) {
                // TODO
            }
            else if (parent instanceof J.Assignment) {
                J.Assignment assignment = (J.Assignment) parent;
                // TODO
            }
            else if (parent_statement instanceof J.VariableDeclarations) { // TODO parent statement not guaranteed to be top statement

                J.VariableDeclarations parent_vd = (J.VariableDeclarations) parent_statement;

                //System.out.println("parent_vd: " + parent_vd);
                // parent_vd: List<TranslatedTextItem> result = textTranslationClient.translate(Arrays.asList("es"), inputTextItems)

                // Get the index of the statement containing the method.
                int parent_index = body.getStatements().indexOf(parent_vd); // 2
                //System.out.println("parent_index: " + parent_index);

                if (((J.VariableDeclarations) parent_statement).getVariables().size() != 1) {
                    // Recipe can only handle a variable declaration with a single named variable at this time
                    return body;
                }

                J.VariableDeclarations.NamedVariable namedVariable = ((J.VariableDeclarations) parent_statement).getVariables().get(0);

                Expression good_expression = namedVariable.getInitializer(); // result

                //System.out.println("namedVar as expression:" + namedVariable.getName().unwrap());

                // Create an empty block to apply the try-catch template based off the cursor values from the main body
                J.Block b = J.Block.createEmptyBlock();
                b = tryCatchTemplate.apply(new Cursor(getCursor(),b), b.getCoordinates().firstStatement());
                //System.out.println("b.statements: " + b.getStatements());

                // Get the created try-catch block
                J.Try _try = (J.Try) b.getStatements().get(0);
                //System.out.println("catches: " + _try.getCatches());

                // Extract the generated dummy elements
                J.VariableDeclarations dummy_varDec = (J.VariableDeclarations) _try.getBody().getStatements().get(0);
                J.Assignment dummy_Assign = (J.Assignment) _try.getBody().getStatements().get(1);

                assert good_expression != null;
                // Repurpose the dummy variable
                dummy_Assign = dummy_Assign.withVariable(namedVariable.getName().unwrap());
                dummy_Assign = dummy_Assign.withAssignment(good_expression);

                // Create a new Assignment from the two;
//                J.Assignment assignment = new J.Assignment(
//
//                        Tree.randomId(),
//                        dummy_Assign.getPrefix(),
//                        dummy_Assign.getMarkers(),
//                        namedVariable.getName().unwrap(),
//                        dummy_Assign.getPadding().getAssignment().withElement(good_expression),
//                        //JLeftPadded.build(good_expression),
//                        namedVariable.getType());

//                Statement s = assignment;
                Statement s = dummy_Assign;
                System.out.println("s: " + s);
                _try = _try.withBody(_try.getBody().withStatements(ListUtils.insert(
                       new ArrayList<>(), s, 0 )));

                System.out.println("try statements: " + _try.getBody().getStatements());

                J.Block tempBody = body;

                // Add the try block where the templated expression was
//                System.out.println("assignment:" + assignment);
//                System.out.println("coordinates: "+assignment.getCoordinates().replace());
                //body = tryCatchTemplate.apply(updateCursor(body), body.getCoordinates().firstStatement()); //  assignment assignment.getCoordinates().replace()
                // throwing java.lang.IllegalArgumentException: Could not parse as Java

                List<Statement> statements = new ArrayList<>();

                namedVariable = namedVariable.withInitializer(dummy_varDec.getVariables().get(0).getInitializer());
                //parent_vd = parent_vd.withVariables(ListUtils.insert(parent_vd.getVariables(), namedVariable, 0));
                //parent_vd
                List<J.VariableDeclarations.NamedVariable> ls = parent_vd.getVariables();
                ls.set(0, namedVariable);
                parent_vd = parent_vd.withVariables(ls); // parent_vd: List<TranslatedTextItem> result = null
                //parent_vd.getVariables().remove(1);
                System.out.println("parent_vd: " + parent_vd);
                System.out.println("parent_vd.getVariables(): " + parent_vd.getVariables());
                //statements.add();


                namedVariable = namedVariable.withInitializer(dummy_varDec.getVariables().get(0).getInitializer());
                System.out.println("namedVariable: " + namedVariable);
                List<J.VariableDeclarations.NamedVariable> variableList = new ArrayList<>();
                variableList.add(namedVariable);
                System.out.println("variableList: " + variableList);
                //parent_vd = parent_vd.withVariables(variableList);

//                J.VariableDeclarations vd = (J.VariableDeclarations) body.getStatements().get(parent_index);
//                vd.getVariables().add(0, namedVariable);
//                System.out.println("vd: " + vd);
//                body.getStatements().remove(parent_statement);
//                System.out.println("body.getStatements(): " + body.getStatements());
                //parent_vd.getVariables().add(0, namedVariable);

//                body_statements.remove(parent_index);
//                body_statements.add(parent_index, vd);
                List<Statement> body_statements = body.getStatements();
                System.out.println("body_statements: " + body_statements);

                body_statements.set(parent_index, parent_vd); // Works
                body_statements.add(parent_index +1, _try);

                body = body.withStatements(body_statements);
//                body = body.withStatements(ListUtils.insert(
//                        body.getStatements(), _try, parent_index + 1
//                ));
                System.out.println("final body: "+ body.getStatements());
                //body = body.withStatements()
            }
            else {
                System.out.println("unhandled method use");
            }

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

            // Remove the method call.
            return null;
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
