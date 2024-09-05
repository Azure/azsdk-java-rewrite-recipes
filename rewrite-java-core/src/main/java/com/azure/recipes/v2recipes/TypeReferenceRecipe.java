package com.azure.recipes.v2recipes;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeTree;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Recipe to convert TypeReference to ParameterizedType and remove TypeReference import statements.
 * @author Ali Soltanian Fard Jahromi
 */
public class TypeReferenceRecipe extends Recipe {

    @Override
    public @NotNull String getDisplayName() {
        return "Convert TypeReference to ParameterizedType and remove imports";
    }

    @Override
    public @NotNull String getDescription() {
        return "This recipe converts TypeReference<List<T>> to ParameterizedType and removes the import statement for TypeReference.";
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ConvertTypeReferenceVisitor();
    }

    private static class ConvertTypeReferenceVisitor extends JavaIsoVisitor<ExecutionContext> {
        /**
         * Method to visit instantiation of TypeReference and replace it with ParameterizedType
         * instantiation including override methods.
         * */
        @Override
        public J.@NotNull NewClass visitNewClass(J.@NotNull NewClass newClass, @NotNull ExecutionContext ctx) {
            J.NewClass visitedNewClass = super.visitNewClass(newClass, ctx);

            // Check if the TypeReference reference has already been transformed
            if (visitedNewClass.getBody() == null){return visitedNewClass;}
            boolean alreadyTransformed = visitedNewClass.getBody().getStatements().stream()
                    .filter(statement -> statement instanceof J.MethodDeclaration)
                    .map(J.MethodDeclaration.class::cast)
                    .anyMatch(methodDeclaration -> methodDeclaration.getName().getSimpleName().equals("getRawType"));

            if (!alreadyTransformed && visitedNewClass.toString().contains("TypeReference")) {
                // Extract type from List type in TypeReference declaration
                Pattern pattern = Pattern.compile("List<(\\w+)>");
                Matcher matcher = pattern.matcher(visitedNewClass.toString());
                String type = matcher.find() ? matcher.group(1) : null;

                JavaTemplate methodRawTypeTemplate = JavaTemplate.builder("@Override public java.lang.reflect.Type getRawType() { return java.util.List.class; }").build();
                JavaTemplate methodActualTypeTemplate = JavaTemplate.builder("@Override public java.lang.reflect.Type[] getActualTypeArguments() { return new java.lang.reflect.Type[] {  " + type + ".class  }; }").build();
                JavaTemplate methodOwnerTypeTemplate = JavaTemplate.builder("@Override public java.lang.reflect.Type getOwnerType() { return null; }").build();

                // Apply Templates (add methods to body)

                visitedNewClass = visitedNewClass.withBody(methodRawTypeTemplate.apply(new Cursor(getCursor(), visitedNewClass.getBody()),
                        visitedNewClass.getBody().getCoordinates().lastStatement()));
                visitedNewClass = visitedNewClass.withBody(methodActualTypeTemplate.apply(new Cursor(getCursor(), visitedNewClass.getBody()),
                        visitedNewClass.getBody().getCoordinates().lastStatement()));
                visitedNewClass = visitedNewClass.withBody(methodOwnerTypeTemplate.apply(new Cursor(getCursor(), visitedNewClass.getBody()),
                        visitedNewClass.getBody().getCoordinates().lastStatement()));

                visitedNewClass = visitedNewClass.withClazz(TypeTree.build(" ParameterizedType")); // Replace TypeReference with Type
            }
            return visitedNewClass;
        }

        /**
         * Method to remove TypeReference import and replace it with ParameterizedType.
         * Also makes sure that no duplicate imports of ParameterizedType are created.
         */
        int count = 0;
        @Override
        public J.@NotNull Import visitImport(J.@NotNull Import importStmt, @NotNull ExecutionContext ctx) {
            if (importStmt.getQualid().toString().equals("java.lang.reflect.ParameterizedType")) {
                count ++;
            }
            if (importStmt.getQualid().toString().equals("java.lang.reflect.ParameterizedType") && count > 1) {
                return null;
            }
            // Remove import statement for TypeReference and add import for ParameterizedType
            if (importStmt.getQualid().toString().equals("com.azure.core.util.serializer.TypeReference")) {
                return importStmt.withQualid(TypeTree.build(" java.lang.reflect.ParameterizedType"));
            }

            if (importStmt.getQualid().toString().equals("com.azure.core.util.BinaryData")){
                return importStmt.withQualid(TypeTree.build(" io.clientcore.core.util.binarydata.BinaryData"));
            }

            // Return other imports normally
            return importStmt;
        }

        /**
         * Method to visit variable declaration for TypeReference and make sure it is converted to java.lang.reflect.Type
         */
        @Override
        public J.@NotNull VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable, ExecutionContext executionContext) {
            J.VariableDeclarations visitedDeclarations = super.visitVariableDeclarations(multiVariable, executionContext);
            if (visitedDeclarations.toString().contains("TypeReference")
                    && visitedDeclarations.toString().contains("ParameterizedType")) {
                return visitedDeclarations.withTypeExpression(TypeTree.build(" java.lang.reflect.Type"));
            }
            return visitedDeclarations;
        }

        /**
         * Method to visit BinaryData type and change it to the new version
         */
        @Override
        public J.@NotNull FieldAccess visitFieldAccess(J.@NotNull FieldAccess fieldAccess, @NotNull ExecutionContext ctx) {
            J.FieldAccess fa = super.visitFieldAccess(fieldAccess, ctx);
            String fullyQualified = fa.getTarget() + "." + fa.getSimpleName();
            if (fullyQualified.equals("com.azure.core.util.BinaryData")) {
                return TypeTree.build(" io.clientcore.core.util.binarydata.BinaryData");
            }
            return fa;
        }
    }
}
