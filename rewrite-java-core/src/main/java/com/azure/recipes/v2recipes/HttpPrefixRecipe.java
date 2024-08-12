package com.azure.recipes.v2recipes;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeTree;

/**
 * HttpPrefixRecipe changes all instances of the RetryOptions identifier to HttpRetryOptions.
 * It also changes the according import statement from com.azure.core.http.policy.RetryOptions to io.clientcore.http.models.HttpRetryOptions.
 */

public class HttpPrefixRecipe extends Recipe {
    /**
     * Method to return a short description of HttpPrefixRecipe
     * @return A simple short description/name of the recipe
     */

    @Override
    public @NotNull String getDisplayName() {
        return "Change RetryOptions to HttpRetryOptions";
    }

    /**
     * Method to return a short description of HttpPrefixRecipe
     * @return A short description of the recipe
     */
    @Override
    public @NotNull String getDescription() {
        return "Change identifier RetryOptions to HttpRetryOptions and update the import statement";
    }

    /**
     * Method to return the visitor that visits the RetryOptions identifier
     * @return A treeVisitor to visit the RetryOptions identifier and change it to HttpRetryOptions
     */

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ChangeRetryOptionsToHttpRetryOptionsVisitor();
    }




    public static class ChangeRetryOptionsToHttpRetryOptionsVisitor extends JavaIsoVisitor<ExecutionContext> {

        /**
         * Method to modify the import statement accordingly from com.azure.core.http.policy.RetryOptions to io.clientcore.http.models.HttpRetryOptions
         */
        @Override
        public J.@NotNull FieldAccess visitFieldAccess(J.@NotNull FieldAccess fieldAccess, @NotNull ExecutionContext ctx) {
            J.FieldAccess fa = super.visitFieldAccess(fieldAccess, ctx);
            String fullyQualified = fa.getTarget() + "." + fa.getSimpleName();
            if (fullyQualified.equals("com.azure.core.http.policy.RetryOptions")) {
                return TypeTree.build(" io.clientcore.http.models.HttpRetryOptions");
            }
            return fa;
        }

        /**
         * Method to change the identifier RetryOptions to HttpRetryOptions
         */
        @Override
        public J.@NotNull Identifier visitIdentifier(J.@NotNull Identifier identifier, @NotNull ExecutionContext ctx) {
            J.Identifier id = super.visitIdentifier(identifier, ctx);
            if (id.getSimpleName().equals("RetryOptions")) {
                return id.withSimpleName("HttpRetryOptions");
            }
            return id;
        }
    }
}
