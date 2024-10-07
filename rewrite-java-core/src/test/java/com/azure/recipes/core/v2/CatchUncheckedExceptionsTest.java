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
        spec.recipes(new CatchUncheckedExceptions("UserClass myMethod(..)", true,"e.printStackTrace();"));
    }

    @Test
    void test_wrapSimpleMethod() {
        @Language("java") String before ="import java.io.IOException;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    \n" +
                "    private void myMethod() {\n" + // throws IOException
                "       int a = 1 + 1;\n" +
                "    }\n" +
                "    \n" +
                "    private void anotherMethod(){\n" +
                "       int b = 2 + 2;\n" +
                "       myMethod();\n" +
                "    }\n" +
                "}\n";

        @Language("java") String after = "import java.io.IOException;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    \n" +
                "    private void myMethod() {\n" + // throws IOException
                "       int a = 1 + 1;\n" +
                "    }\n" +
                "    \n" +
                "    private void anotherMethod(){\n" +
                "       int b = 2 + 2;\n" +
                "        try {\n" +
                "            myMethod();\n" +
                "        } catch (IOException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        rewriteRun(
                java(before,after)
        );
    }

    @Test
    void test_wrapMethodWithReturn() {
        @Language("java") String before ="import java.io.IOException;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    \n" +
                "    private int myMethod() {\n" + // throws IOException
                "       return 1 + 1;\n" +
                "    }\n" +
                "    \n" +
                "    private void anotherMethod(){\n" +
                "       int b = myMethod();\n" +
                "    }\n" +
                "}\n";

        @Language("java") String after = "import java.io.IOException;\n" +
                "\n" +
                "public class UserClass {\n" +
                "    \n" +
                "    private int myMethod() {\n" + // throws IOException
                "       return a = 2 + 2;\n" +
                "    }\n" +
                "    \n" +
                "    private void anotherMethod(){\n" +
                "       int b;\n" +
                "        try {\n" +
                "            b = myMethod();\n" +
                "        } catch (IOException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        rewriteRun(
                java(before,after)
        );
    }
}
