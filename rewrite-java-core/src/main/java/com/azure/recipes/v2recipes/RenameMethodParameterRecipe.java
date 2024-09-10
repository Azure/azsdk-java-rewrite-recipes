package com.azure.recipes.v2recipes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.*;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipe to find and perform a simple rename on a method parameter.
 * @author Annabelle Mittendorf Smith
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class RenameMethodParameterRecipe extends Recipe {

    /**
     * Configuration options required my the recipe.
     */
    @Option(displayName = "Method pattern",
            description = "A method pattern used to find matching method calls.",
            example = "*..* hello(..)")
    String methodPattern;

    @Option(displayName = "Parameter index",
            description = "The index of the parameter",
            example = "b = 1 in: method(int a, int b)")
    Integer parameterIndex;

    @Option(displayName = "New name",
            description = "The new name of your parameter.",
            example = "c")
    String newName;

    /**
     * Json creator to allow the recipe to be used from a yaml file.
     * All recipes must be serializable. This is verified by RewriteTest.rewriteRun() in tests.
     */

    @JsonCreator
    public RenameMethodParameterRecipe(@NonNull @JsonProperty("methodPattern") String methodPattern,
                                       @NonNull @JsonProperty("parameterIndex") Integer parameterIndex,
                                       @NonNull @JsonProperty("newName") String newName) {
        this.methodPattern = methodPattern;
        this.parameterIndex = parameterIndex;
        this.newName = newName;
    }

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Rename a method parameter";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Rename method parameter at given index to given name.";
    }

    @Override
    public @NonNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new RenameParameterRecipe(methodPattern, parameterIndex, newName);

    }

    @AllArgsConstructor
    public static class RenameParameterRecipe extends JavaIsoVisitor<ExecutionContext> {

        String methodPattern;
        Integer parameterIndex;
        String newName;
        private final MethodMatcher methodMatcher = new MethodMatcher(methodPattern, true);

        @Override
        public J.@NotNull MethodDeclaration visitMethodDeclaration(J.MethodDeclaration methodDeclaration, ExecutionContext ec) {
            J.MethodDeclaration method = super.visitMethodDeclaration(methodDeclaration, ec);

            // Not the correct Method
            if (!methodMatcher.matches(method.getMethodType())) {
                return method;
            }

            if (method.getMethodType() == null || parameterIndex >= method.getParameters().size()) {
                return method;
            }

            JavaType.Method type = method.getMethodType();
            //String[] names = type.getParameterNames().toArray(new String[0]);
            //System.out.println(names);
            //names[parameterIndex] = newName;
            //type.withParameterNames(type.getParameterNames().)

            method = method.withMethodType(type);

            return method;
        }
    }
}