package com.azure.recipes.v2recipes;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.*;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

/**
 * A partial recipe to convert HttpTrait clientOptions(ClientOptions clientOptions)
 * to httpRedirectOptions(HttpRedirectOptions redirectOptions)
 * This recipe changes the parameter type and name, adds an HttpRedirectOptions object
 * before the method declaration, and an assignment inside the method.
 * Converting the methods (instead of removing one and adding another) preserves
 * the return type.
 * Recipe could be expanded for generic use
 * @author Annabelle Mittendorf Smith
 */

public class ChangeClientOptionsParamsRecipe extends Recipe {
    @Override
    public @NlsRewrite.DisplayName @NotNull String getDisplayName() {
        return "Change clientOptions parameters";
    }

    @Override
    public @NlsRewrite.Description @NotNull String getDescription() {
        return "Changes clientOptions parameters and assignment variable to HttpRedirectOptions.";
    }

    @Override
    public @NonNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AddVariableDeclarationVisitor();

    }

    public static class AddVariableDeclarationVisitor extends JavaIsoVisitor<ExecutionContext> {

        // Method pattern to find target method
        private final String methodPattern = "com.azure.core.client.traits.HttpTrait clientOptions(com.azure.core.util.ClientOptions)";
        private final MethodMatcher methodMatcher = new MethodMatcher(methodPattern, true);

        /**
         * Method to add 'private HttpRedirectOptions redirectOptions;' before clientOptions().
         * Makes changes only if class contains target method and does not contain any variable named redirectOptions.
         */
        @Override
        public J.@NotNull ClassDeclaration visitClassDeclaration(J.@NotNull ClassDeclaration classDeclaration, @NotNull ExecutionContext context) {

            J.ClassDeclaration cd = super.visitClassDeclaration(classDeclaration, context);

            // Return unchanged if method is not found in this class.
            J.MethodDeclaration methodDeclaration = getCursor().pollMessage("FOUND_METHOD");
            if (methodDeclaration == null) { return cd; }

            // Return unchanged if a variable named redirectOptions already exists.
            if (cd.getBody().getStatements().stream().anyMatch(
                    statement -> statement instanceof J.VariableDeclarations &&
                            ((J.VariableDeclarations) statement).getVariables().stream().anyMatch(
                                    v -> v.getSimpleName().equals("redirectOptions")))) {
                return cd;
            }

            // Make and apply template for a new Variable declaration.
            JavaTemplate newVarTemplate = JavaTemplate.builder("private HttpRedirectOptions redirectOptions;")
                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(context, "core-1.0.0-beta.1"))
                    .contextSensitive()
                    .imports("io.clientcore.core.http.models.HttpRedirectOptions")
                    .build();

            // Apply it directly before method declaration.
            cd = newVarTemplate.apply(updateCursor(cd),methodDeclaration.getCoordinates().before());

            maybeAddImport("io.clientcore.core.http.models.HttpRedirectOptions");
            maybeRemoveImport("com.azure.core.util.ClientOptions");

            return cd;
        }

        /**
         * Method to find and update instances of target method declaration.
         * Adds message to declaring class.
         */
        @Override
        public J.@NotNull MethodDeclaration visitMethodDeclaration(J.MethodDeclaration methodDeclaration, @NotNull ExecutionContext context) {


            // Make no changes if not target method
            if (!methodMatcher.matches(methodDeclaration.getMethodType())) { return methodDeclaration; }

            // Add message to declaring class
            getCursor().putMessageOnFirstEnclosing(J.ClassDeclaration.class, "FOUND_METHOD", methodDeclaration);

            JavaTemplate addParametersTemplate = JavaTemplate.builder("HttpRedirectOptions redirectOptions")
                   .javaParser(JavaParser.fromJavaVersion().classpathFromResources(context, "core-1.0.0-beta.1"))
                   .imports("io.clientcore.core.http.models.HttpRedirectOptions")
                   .build();

            JavaTemplate addBody = JavaTemplate.builder("this.redirectOptions = redirectOptions; return this;")
                   .javaParser(JavaParser.fromJavaVersion().classpathFromResources(context, "core-1.0.0-beta.1"))
                   .contextSensitive()
                   .imports("io.clientcore.core.http.models.HttpRedirectOptions")
                   .build();

            // Replace parameters
            methodDeclaration = addParametersTemplate.apply(updateCursor(methodDeclaration),
                   methodDeclaration.getCoordinates().replaceParameters());

            if (methodDeclaration.getBody() == null) {
                methodDeclaration = methodDeclaration.withBody(J.Block.createEmptyBlock());
            }

            // Apply Template to method body
            methodDeclaration = maybeAutoFormat(
                    // Option replace entire body block
                    methodDeclaration, addBody.apply(updateCursor(methodDeclaration), methodDeclaration.getCoordinates().replaceBody()),context
                    // Option add to body block, would need to filter out and remove statements that use the old parameters.
                    //methodDeclaration, addBody.apply(updateCursor(methodDeclaration),methodDeclaration.getBody().getCoordinates().firstStatement()),context
            );

            maybeAddImport("io.clientcore.core.http.models.HttpRedirectOptions");
            maybeRemoveImport("com.azure.core.util.ClientOptions");

            return methodDeclaration;
        }
    }
}
