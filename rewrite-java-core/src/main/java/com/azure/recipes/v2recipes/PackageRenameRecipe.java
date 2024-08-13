package com.azure.recipes.v2recipes;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeTree;

public class PackageRenameRecipe extends Recipe {

    /*
    * Returns the package name
    */
    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Package Rename Recipe";
    }

    /*
     * Returns the package description
     */

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Recipe renames Package from 'com.azure.core.credential' to 'io.clientcore.core.credential'";
    }

    /*
     * Returns the visitor that modifies the package name
     */
    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new RenamePackageVisitor();
    }


    /*
     * The visitor to rename package com.azure.core.credential to io.clientcore.core.credential
     * And methods to change package name in import statements and source files
     */
    private static class RenamePackageVisitor extends JavaIsoVisitor<ExecutionContext>{

        private static final String ORIGINAL_PACKAGE_NAME = "com.azure.core.credential";
        private static final String FINAL_PACKAGE_NAME = "io.clientcore.core.credential";

        @Override
        public J.@NotNull Import visitImport(J.@NotNull Import imported, @NotNull ExecutionContext context) {
            J.Import imp = super.visitImport(imported, context);
            String packageName = imp.getPackageName();
            String updatedPackageName = updatePackageName(packageName);
            if (packageName.startsWith("com.azure.core.credential")) {
                return imp.withQualid(TypeTree.build(updatedPackageName));
            }
            return imp;
        }


        @Override
        public J.@NotNull Package visitPackage(J.@NotNull Package pack, @NotNull ExecutionContext context){
            J.Package pck = super.visitPackage(pack, context);
            if(pck.getExpression().toString().equals("com.azure.core.credential")){
                return pck.withExpression(TypeTree.build("io.clientcore.core.credential"));
            }
            return pck;
        }

        
        private String updatePackageName(String packageName){
            if(packageName.startsWith(ORIGINAL_PACKAGE_NAME)){
                return packageName.replace(ORIGINAL_PACKAGE_NAME, FINAL_PACKAGE_NAME);
            }
            return packageName;
        }
    }
}
