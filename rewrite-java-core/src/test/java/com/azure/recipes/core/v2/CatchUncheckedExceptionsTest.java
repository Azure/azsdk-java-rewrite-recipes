package com.azure.recipes.core.v2;


import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;


import static org.openrewrite.java.Assertions.java;

/**
 * HttpTraitTest tests interface migration from com.azure.core.client.traits.HttpTrait
 * to io.clientcore.core.models.traits.HttpTrait.
 * Tests simple method renaming with declarative recipe.
 * @author Annabelle Mittendorf Smith
 */
public class CatchUncheckedExceptionsTest implements RewriteTest {
    /**
     * This method defines recipes used for testing.
     * @param spec stores settings for testing environment.
     */
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RemoveMethodDeclaration("UserClass myMethod(..)", true));
    }

    @Test
    void test() {
        @Language("java") String before ="import java.io.IOException;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    \n" +
                "    private void myMethod() throws IOException {\n" +
                "        \n" +
                "    }\n" +

                "    private void myMethod3(){\n" +
                "        try {\n" +
                "            myMethod();\n" +
                "        } catch (IOException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "        \n" +
                "    }" +
                "}\n";

        @Language("java") String after = "import java.io.IOException;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    \n" +
                "    private void myMethod() throws IOException {\n" +
                "        \n" +
                "    }\n" +
                "    private void myMethod2(){\n" +
              //  "        myMethod();\n" +
                "    }\n" +
                "    private void myMethod3(){\n" +
                "        try {\n" +
                "            myMethod();\n" +
                "        } catch (IOException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "        \n" +
                "    }" +
                "}\n";

        rewriteRun(
                java(before,after)
        );
    }
}
