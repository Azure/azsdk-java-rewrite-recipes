package com.azure.recipes.v2recipes;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeTree;

/**
 * ContextRecipe changes all instances of Context.NONE (from azure core v1) to Context.none() (from azure core v2).
 * Note: This recipe does not change the import statement for Context to work with new azure libraries.
 * @author Ali Soltanian Fard Jahromi
 */
public class ContextRecipe extends Recipe {
    /**
     * Method to return a simple short description of ContextRecipe
     * @return A simple short description/name of the recipe
     */
    @Override
    public @NotNull String getDisplayName() {
        return "Change Static Field 'Context.NONE' to Method 'Context.none()'";
    }
    /**
     * Method to return a description of ContextRecipe
     * @return A short description of the recipe
     */
    @Override
    public @NotNull String getDescription() {
        return "This recipe changes any calls to Context.NONE to Context.none().\n" +
                "It also changes the import statement of com.azure.core.util.Context to io.clientcore.util.Context.";
    }
    /**
     * Method to return the visitor that visits the Context.NONE identifier
     * @return A TreeVisitor to visit the NONE identifier and change it to none()
     */
    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ChangeStaticFieldToMethodVisitor();
    }
    /**
     * Visitor to change NONE identifier to none()
     */
    private static class ChangeStaticFieldToMethodVisitor extends JavaIsoVisitor<ExecutionContext> {
        /**
         * Method to change com.azure.core.util.Context to io.clientcore.util.Context
         */
        @Override
        public J.@NotNull FieldAccess visitFieldAccess(J.@NotNull FieldAccess fieldAccess, @NotNull ExecutionContext ctx) {
            J.FieldAccess fa = super.visitFieldAccess(fieldAccess, ctx);
            String fullyQualified = fa.getTarget() + "." + fa.getSimpleName();
            if (fullyQualified.equals("com.azure.core.util.Context")) {
                return TypeTree.build(" io.clientcore.util.Context");
            }
            return fa;
        }
        /**
         * Method to change the identifier "NONE" to "none()"
         * @return The modified identifier
         */
        @Override
        public J.@NotNull Identifier visitIdentifier(J.@NotNull Identifier identifier, @NotNull ExecutionContext ctx) {
            J.Identifier id = super.visitIdentifier(identifier, ctx);
            if (id.getSimpleName().equals("NONE")) {
                return id.withSimpleName("none()");
            }
            return id;
        }
    }
}
