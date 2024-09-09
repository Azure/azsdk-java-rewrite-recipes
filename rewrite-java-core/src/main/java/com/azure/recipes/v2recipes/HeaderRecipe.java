package com.azure.recipes.v2recipes;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeTree;

public class HeaderRecipe  extends Recipe {

    /**
     * Method to return a simple short description
     */
    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "'com.azure.core.http.HttpHeaderName' to 'io.clientcore.core.http.models.HttpHeaderName'";
    }

    /**
     * Method to return a description
     */
    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Recipe changes any usage of com.azure.core.http.HttpHeaderName to io.clientcore.core.http.models.HttpHeaderName. It also updates the import statement accordingly.";
    }

    /**
     * Method to return the visitor that visits the HttpHeaderName type and changes it.
     */
    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ChangeHttpHeaderNameVisitor();
    }

    private static class ChangeHttpHeaderNameVisitor extends JavaIsoVisitor<ExecutionContext> {

        @Override
        public J.@NotNull Identifier visitIdentifier(J.@NotNull Identifier ident, @NotNull ExecutionContext contxt) {
            J.Identifier visitedId = super.visitIdentifier(ident, contxt);

            if (visitedId.getType() != null && "com.azure.core.http.HttpHeaderName".equals(visitedId.getType().toString())) {
                return visitedId.withType(TypeTree.build("io.clientcore.core.http.models.HttpHeaderName").getType());
            }
            return visitedId;
        }

        @Override
        public J.Import visitImport(J.Import imp, ExecutionContext contxt) {
            J.Import vistedImp = super.visitImport(imp, contxt);

            if ("com.azure.core.http.HttpHeaderName".equals(vistedImp.getTypeName())) {
                return vistedImp.withTypeName("io.clientcore.core.http.models.HttpHeaderName");
            }
            return vistedImp;
        }

    }
}
