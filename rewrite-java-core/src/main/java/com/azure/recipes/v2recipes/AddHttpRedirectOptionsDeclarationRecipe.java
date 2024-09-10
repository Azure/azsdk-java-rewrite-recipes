package com.azure.recipes.v2recipes;

import org.openrewrite.*;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

public class AddHttpRedirectOptionsDeclarationRecipe extends Recipe {
    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Add Variable";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Add Variable.";
    }

    @Override

    public @NonNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AddVariableDeclarationVisitor();

    }

    public class AddVariableDeclarationVisitor extends JavaIsoVisitor<ExecutionContext> {

        private final MethodMatcher methodMatcher = new MethodMatcher("com.azure.core.client.traits.HttpTrait clientOptions(com.azure.core.util.ClientOptions)", true);

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDeclaration, ExecutionContext executionContext) {
            J.ClassDeclaration c = super.visitClassDeclaration(classDeclaration, executionContext);

            JavaTemplate newVarTemplate = JavaTemplate.builder("private HttpRedirectOptions redirectOptions;")
                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(executionContext, "core-1.0.0-beta.1"))
                    .contextSensitive()
                    .imports("io.clientcore.core.http.models.HttpRedirectOptions")
                    .build();

            if (c.getBody().getStatements().isEmpty()) { return c;}

            J.MethodDeclaration methodDeclaration = getCursor().pollMessage("FOUND_METHOD");
            if (methodDeclaration == null) { return c; }
            if (!methodMatcher.matches(methodDeclaration, classDeclaration)) { return c; }
            if (c.getBody().getStatements().stream().anyMatch(
                    statement -> statement instanceof J.VariableDeclarations &&
                            ((J.VariableDeclarations) statement).getVariables().stream().anyMatch(
                                    v -> v.getSimpleName().equals("redirectOptions")))) {
                return c;
            }
            c = newVarTemplate.apply(updateCursor(c),methodDeclaration.getCoordinates().before());
            maybeAddImport("io.clientcore.core.http.models.HttpRedirectOptions");

            return c;
        }

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration methodDeclaration, ExecutionContext executionContext) {
            // Needs super??
           if (methodMatcher.matches(methodDeclaration.getMethodType())) {

               getCursor().putMessageOnFirstEnclosing(J.ClassDeclaration.class, "FOUND_METHOD", methodDeclaration);
               //  WORKING
               JavaTemplate addParametersTemplate = JavaTemplate.builder("HttpRedirectOptions options")
                       .javaParser(JavaParser.fromJavaVersion().classpathFromResources(executionContext, "core-1.0.0-beta.1"))
                       .imports("io.clientcore.core.http.models.HttpRedirectOptions")
                       .build();

               JavaTemplate addBody = JavaTemplate.builder("this.redirectOptions = options; ") //return this;
                       .javaParser(JavaParser.fromJavaVersion().classpathFromResources(executionContext, "core-1.0.0-beta.1"))
                       .contextSensitive()
                       .imports("io.clientcore.core.http.models.HttpRedirectOptions")
                       .build();

               maybeAddImport("io.clientcore.core.http.models.HttpRedirectOptions");

               methodDeclaration = addParametersTemplate.apply(updateCursor(methodDeclaration),
                       methodDeclaration.getCoordinates().replaceParameters());

               if (methodDeclaration.getBody() == null) { return methodDeclaration; }
               methodDeclaration = maybeAutoFormat(
                       //methodDeclaration, addBody.apply(updateCursor(methodDeclaration), methodDeclaration.getCoordinates().replaceBody()),executionContext
                       methodDeclaration, addBody.apply(updateCursor(methodDeclaration),methodDeclaration.getBody().getCoordinates().firstStatement()),executionContext
               );

               //Should not change name here. Would cause errors with invocations
           }
           return methodDeclaration;
        }

    }
}
