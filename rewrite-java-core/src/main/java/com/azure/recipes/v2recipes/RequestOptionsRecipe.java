package com.azure.recipes.v2recipes;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeTree;

/**
 * RequestOptionsRecipe changes all instances of com.azure.core.http.rest.RequestOptions (from azure core v1)
 * to io.clientcore.core.http.models.RequestOptions (from azure core v2).
 * @author Ali Soltanian Fard Jahromi
 */
public class RequestOptionsRecipe extends Recipe {
    /**
     * Method to return a simple short description of RequestOptionsRecipe
     * @return A simple short description/name of the recipe
     */
    @Override
    public @NotNull String getDisplayName() {
        return "Update RequestOptions";
    }
    /**
     * Method to return a description of RequestOptionsRecipe
     * @return A short description of the recipe
     */
    @Override
    public @NotNull String getDescription() {
        return "This recipe changes all instances of com.azure.core.http.rest.RequestOptions " +
                "to io.clientcore.core.http.models.RequestOptions.";
    }
    /**
     * Method to return the visitor that visits the RequestOptions identifier
     * @return A TreeVisitor to visit the RequestOptions identifier and update it to new library
     */
    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ChangeStaticFieldToMethodVisitor();
    }
    /**
     * Visitor to change com.azure.core.http.rest.RequestOptions to io.clientcore.core.http.models.RequestOptions
     */
    private static class ChangeStaticFieldToMethodVisitor extends JavaIsoVisitor<ExecutionContext> {
        /**
         * Method to change com.azure.core.http.rest.RequestOptions to io.clientcore.core.http.models.RequestOptions
         */
        @Override
        public J.@NotNull FieldAccess visitFieldAccess(J.@NotNull FieldAccess fieldAccess, @NotNull ExecutionContext ctx) {
            J.FieldAccess fa = super.visitFieldAccess(fieldAccess, ctx);
            String fullyQualified = fa.getTarget() + "." + fa.getSimpleName();
            if (fullyQualified.equals("com.azure.core.http.rest.RequestOptions")) {
                return TypeTree.build(" io.clientcore.core.http.models.RequestOptions");
            }
            return fa;
        }
    }
}
