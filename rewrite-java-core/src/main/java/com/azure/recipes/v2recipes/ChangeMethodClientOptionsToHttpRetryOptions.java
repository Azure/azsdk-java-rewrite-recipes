package com.azure.recipes.v2recipes;

import com.azure.core.util.ClientOptions;
import org.openrewrite.*;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.java.*;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.List;

public class ChangeMethodClientOptionsToHttpRetryOptions extends Recipe {
    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "clientOptions to retryOptions";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Turns Client options to retry Options.";
    }

    //String fullyQualifiedClassName = ""
    @Override
    public @NonNull TreeVisitor<?, ExecutionContext> getVisitor() {
        // return Preconditions.check(new UsesType<>(fullyQualifiedClassName, true),
        return new AddMethodToImplementationVisitor();
    }

    public class AddMethodToImplementationVisitor extends JavaIsoVisitor<ExecutionContext> {

        private final MethodMatcher methodMatcher = new MethodMatcher(
                "com.azure.core.client.traits.HttpTrait clientOptions(com.azure.core.util.ClientOptions)", true);


        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration methodDeclaration, ExecutionContext executionContext) {
            //System.out.println(TreeVisitingPrinter.printTree(getCursor()));
            if (!methodMatcher.matches(methodDeclaration.getMethodType())) {
                return methodDeclaration;
            }

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


            return methodDeclaration;
        }

    }
}
