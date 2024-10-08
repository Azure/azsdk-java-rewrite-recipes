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
import org.openrewrite.marker.Markers;

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

        // #{any()} is a placeholder for the statement containing the method
        //private final

/*
.javaParser(JavaParser.fromJavaVersion().classpathFromResources(context, "core-1.0.0-beta.1"))
 */
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

            JavaTemplate tryCatchTemplate = JavaTemplate.builder("try { int a = null; a = 3; } " + catchTemplateString)//+catchTemplateString
            //JavaTemplate tryCatchTemplate = JavaTemplate.builder("try{ #{any()}; } " + catchTemplateString)
                    //.javaParser(JavaParser.fromJavaVersion().classpathFromResources(context, "azure-ai-translation-text-1.0.0-beta.1.jar"))
                    .contextSensitive()
                    .imports(fullyQualifiedExceptionName)
                    //.imports("com.azure.ai.translation.text.models.InputTextItem")//.imports(com.azure.ai.translation.text.models)//Objects.requireNonNull(method.getMethodType()).getDeclaringType().getFullyQualifiedName()
                    //.imports("com.azure.ai.translation.text.models.TranslatedTextItem")
                    //.imports("java.util.Arrays")
                    .build();

            //Get the parents of the method
            Tree parent = getCursor().pollMessage("PARENT");

            // Get the first statement parent of method
            Statement parent_statement = getCursor().pollMessage("STATEMENT");

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
                System.out.println("parent_vd: " + parent_vd);
                // Get the index of the statement containing the method.
                int parent_index = body.getStatements().indexOf(parent_vd);

                System.out.println("parent_index: " + parent_index);
                if (((J.VariableDeclarations) parent_statement).getVariables().size() != 1) {
                    // Recipe can only handle a variable declaration with a single named variable at this time
                    return body;
                }

                // Extract the fully and correctly compiled components from the original code
                // The named variable that looks like the new assignment we will create
                // E.g. from J.VariableDeclarations: "int a = num.getMyInt()" named variable is "a = num.getMyInt()"
                J.VariableDeclarations.NamedVariable namedVariable = ((J.VariableDeclarations) parent_statement).getVariables().get(0);

                // equivalent to "num.getMyInt()"
                Expression good_expression = namedVariable.getInitializer();

                System.out.println(namedVariable.getName().unwrap());

                J.Block b = J.Block.createEmptyBlock();
                b = tryCatchTemplate.apply(new Cursor(getCursor(),b), b.getCoordinates().firstStatement());

                System.out.println("b:" + b);
                J.Try _try = (J.Try) b.getStatements().get(0);
                System.out.println("catches: " + _try.getCatches());

                J.VariableDeclarations dummy_vd = (J.VariableDeclarations) _try.getBody().getStatements().get(0);
                J.Assignment dummy_AS = (J.Assignment) _try.getBody().getStatements().get(1);

                assert good_expression != null;
                J.Assignment assignment = new J.Assignment(
                        Tree.randomId(),
                        dummy_AS.getPrefix(),
                        dummy_AS.getMarkers(),
                        namedVariable.getName().unwrap(),
                        dummy_AS.getPadding().getAssignment().withElement(good_expression),
                        //JLeftPadded.build(good_expression),
                        namedVariable.getType());

                Statement s = assignment;
                _try = _try.withBody(_try.getBody().withStatements(ListUtils.insert(
                       new ArrayList<>(), s, 0 )));

                System.out.println("s: " + s);
                // Apply a template to split the variable assignment into:
                // int a = null;
                // a = num.getMyInt()
                // Note: the second expression may have missing components.
                // Java parser unable to resolve com.azure.ai.translation.text elements
//                JavaTemplate template1 = JavaTemplate.builder(body.getStatements().get(parent_index) +
//                        " = null;")
                        //.javaParser(JavaParser.fromJavaVersion().classpathFromResources(context, "azure-ai-translation-text-1.0.0-beta.1.jar"))
                       // .contextSensitive()
                        //.imports("com.azure.ai.translation.text.models.InputTextItem")//.imports(com.azure.ai.translation.text.models)//Objects.requireNonNull(method.getMethodType()).getDeclaringType().getFullyQualifiedName()
                       // .imports("com.azure.ai.translation.text.models.TranslatedTextItem")
                        //.imports("java.util.Arrays")
                       // .build();
                //System.out.println("template1");
                J.Block tempBody = body;
              //  body = template1.apply(updateCursor(body), parent_statement.getCoordinates().replace());
//                System.out.println("body: " + body);
//                //TODO add length checks
//                if (body.getStatements().size() != tempBody.getStatements().size() + 1) {
//                    // If a new statement wasn't added
//                    System.out.println("failure");
//                    //return tempBody;
//                }
                // Check if the Created assignment matches the working assignment
//                J.Assignment assignment = (J.Assignment) body.getStatements().get(parent_index + 1);
//                System.out.println("assignment:" + assignment);
//                System.out.println(assignment.getVariable());
//                Expression template_expression = assignment.getAssignment();
//                System.out.println("template_expression:" + template_expression);
//                System.out.println("good_expression:" + good_expression);

//                if (good_expression!= null && template_expression != good_expression) {
//                    // If the new expression is faulty, replace the templated expression with the original one
//                    assignment = assignment.withAssignment(good_expression);
//                    System.out.println("assignment:" + assignment);
//                }
                // Add the try block where the templated expression was
                System.out.println("assignment:" + assignment);
                System.out.println("coordinates: "+assignment.getCoordinates().replace());
                //body = tryCatchTemplate.apply(updateCursor(body), body.getCoordinates().firstStatement()); //  assignment assignment.getCoordinates().replace()
                // throwing java.lang.IllegalArgumentException: Could not parse as Java
                List<Statement> statements = new ArrayList<>();

                namedVariable = namedVariable.withInitializer(dummy_vd.getVariables().get(0).getInitializer());
                parent_vd = parent_vd.withVariables(ListUtils.insert(parent_vd.getVariables(), namedVariable, 0));
                //statements.add();

                body = body.withStatements(ListUtils.insert(
                        body.getStatements(), _try, parent_index + 1
                ));
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
