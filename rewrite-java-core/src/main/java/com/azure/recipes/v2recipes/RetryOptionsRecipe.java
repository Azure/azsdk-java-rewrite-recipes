package com.azure.recipes.v2recipes;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeTree;

import java.util.ArrayList;
import java.util.List;

/**
 * RetryOptionsRecipe changes RetryOptions constructor to HttpRetryOptions constructor.
 * It also removes any references to FixedDelay and ExponentialDelay and changes
 * com.azure.core.http.policy.RetryOptions to io.clientcore.core.http.models.HttpRetryOptions
 * @author Ali Soltanian Fard Jahromi
 */
public class RetryOptionsRecipe extends Recipe {
    /**
     * Method to return a simple short description of RetryOptionsRecipe
     * @return A simple short description/name of the recipe
     */
    @Override
    public @NotNull String getDisplayName() {
        return "Change RetryOptions constructor";
    }
    /**
     * Method to return a description of RetryOptionsRecipe
     * @return A short description of the recipe
     */
    @Override
    public @NotNull String getDescription() {
        return "This recipe changes the constructor for RetryOptions to HttpRetryOptions.\n" +
                "This includes removing any references to FixedDelay and ExponentialDelay and changing\n" +
                " * com.azure.core.http.policy.RetryOptions to io.clientcore.core.http.models.HttpRetryOptions.";
    }
    /**
     * Method to return the visitor that changes RetryOptions constructor to HttpRetryOptions constructor
     * @return A TreeVisitor to change RetryOptions constructor to HttpRetryOptions constructor
     */
    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new RetryVisitor();
    }
    /**
     * Visitor to change RetryOptions constructor to HttpRetryOptions constructor
     */
    private static class RetryVisitor extends JavaIsoVisitor<ExecutionContext> {
        /**
         * Method to change constructor for HttpRetryOptions to not use FixedDelay or ExponentialDelay
         */
        @Override
        public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext executionContext) {
            J.NewClass n = super.visitNewClass(newClass, executionContext);
            if(n.toString().contains("new HttpRetryOptions")){
                // If number of arguments is 1, that means either FixedDelay or ExponentialDelay is being used
                if (n.getArguments().size() == 1){
                    List<Expression> args = new ArrayList<>();
                    // Gets arguments from FixedDelay or ExponentialDelay constructor and adds it to HttpRetry constructor
                    for (Expression e:
                            ((J.NewClass)n.getArguments().getFirst()).getArguments()) {
                        args.add(e.unwrap());
                    }
                    J.NewClass modified = n.withArguments(args);
                    return modified;
                }
                return n;
            }
            return n;
        }
        /**
         * Method to change constructor for RetryOptions to HttpRetryOptions and Builder api method calls to httpRetryOptions
         */
        @Override
        public J.@NotNull Identifier visitIdentifier(J.@NotNull Identifier identifier, @NotNull ExecutionContext ctx) {
            J.Identifier id = super.visitIdentifier(identifier, ctx);
            if (id.getSimpleName().equals("RetryOptions")) {
                return id.withSimpleName("HttpRetryOptions");
            }
            if (id.getSimpleName().equals("retryOptions")) {
                return id.withSimpleName("httpRetryOptions");
            }
            return id;
        }
        /**
         * Method to change imports to the correct class name
         */
        @Override
        public J.@NotNull FieldAccess visitFieldAccess(J.@NotNull FieldAccess fieldAccess, @NotNull ExecutionContext ctx) {
            J.FieldAccess fa = super.visitFieldAccess(fieldAccess, ctx);
            String fullyQualified = fa.getTarget() + "." + fa.getSimpleName();
            if (fullyQualified.equals("com.azure.core.http.policy.HttpRetryOptions")) {
                return TypeTree.build(" io.clientcore.core.http.models.HttpRetryOptions");
            }
            return fa;
        }
    }
}
