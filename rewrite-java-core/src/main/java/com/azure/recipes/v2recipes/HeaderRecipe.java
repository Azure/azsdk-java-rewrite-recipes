package com.azure.recipes.v2recipes;

import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;

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

    }
}
