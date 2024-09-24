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
 * CURRENTLY UNFINISHED. All tests for HttpTrait migration will be added here.
 * @author Annabelle Mittendorf Smith
 */

public class HttpTraitTest implements RewriteTest {
    /**
     * This method defines recipes used for testing.
     * @param spec stores settings for testing environment.
     */
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml",
                "com.azure.rewrite.java.core.MigrateAzureCoreSamplesToAzureCoreV2");
    }
    /**
     * Test that non-interface methods with the same name are not altered.
     */
    @Test
    void testDoesNotChangeNonInheritedMethods() {
        @Language("java")String noChange = "public class TestClass {\n" +
                "    public void retryOptions() {\n" +
                "    }\n" +
                "    public void pipeline() {\n" +
                "    }\n" +
                "    public void addPolicy() {\n" +
                "    }\n" +
                "}\n";
        rewriteRun(java(noChange));
    }

    /**
     * Test simple declarative rename of:
     * retryOptions to httpRetryOptions
     * pipeline to httpPipeline
     * addPolicy to addHttpPipelinePolicy
     * and complex rename of clientOptions to httpRedirectOptions
     */
    @Test
    void declarativeRenameMethodsSuccessful() {
        @Language("java") String before = "import com.azure.ai.translation.text.TextTranslationClient;\n" +
                "import com.azure.ai.translation.text.TextTranslationClientBuilder;\n" +
                "public class UserClass {\n" +
                "    \n" +
                "    TextTranslationClient textTranslationClient = new TextTranslationClientBuilder()\n" +
                "            .pipeline(null)\n" +
                "            .addPolicy(null)\n" +
                "            .retryOptions(null)\n" +
                "            .httpLogOptions(null)\n" +
                "            .clientOptions(null)\n" +
                "            .buildClient();\n" +
                "}";


        @Language("java") String after = "import com.azure.ai.translation.text.TextTranslationClient;\n" +
                "import com.azure.ai.translation.text.TextTranslationClientBuilder;\n" +
                "public class UserClass {\n" +
                "\n" +
                "        TextTranslationClient textTranslationClient = new TextTranslationClientBuilder()\n" +
                "                .httpPipeline(null)\n" +
                "                .addHttpPipelinePolicy(null)\n" +
                "                .httpRetryOptions(null)\n" +
                "                .httpLogOptions(null)\n" +
                "                .httpRedirectOptions(null)\n" +
                "                .buildClient();\n" +
                "\n" +
                "}";

        rewriteRun(
                java(before,after)
        );
    }

    @Test
    void test_clientOptions_httpRedirectOptions_migration() {
        @Language("java") String before = "import com.azure.core.client.traits.HttpTrait;\n" +
                "import com.azure.core.util.ClientOptions;\n" +
                "\n" +
                "public class TestClass implements HttpTrait<TestClass> {\n" +
                "\n" +
                "    private ClientOptions clientOptions;\n" +
                "\n" +
                "    @Override\n" +
                "    public TestClass clientOptions(ClientOptions clientOptions) {\n" +
                "        this.clientOptions = clientOptions;\n" +
                "        return null;\n" +
                "    }\n" +
                "}\n";

        @Language("java") String after = "import com.azure.core.util.ClientOptions;\n" +
                "import io.clientcore.core.http.models.HttpRedirectOptions;\n" +
                "import io.clientcore.core.models.traits.HttpTrait;\n" +
                "\n" +
                "public class TestClass implements HttpTrait<TestClass> {\n" +
                "\n" +
                "    private ClientOptions clientOptions;\n" +
                "\n" +
                "    private HttpRedirectOptions redirectOptions;\n" +
                "\n" +
                "    @Override\n" +
                "    public TestClass httpRedirectOptions(HttpRedirectOptions redirectOptions) {\n" +
                "        this.redirectOptions = redirectOptions;\n" +
                "        return this;\n" +
                "    }\n" +
                "}\n";

        rewriteRun(
                java(before, after)
        );
    }
}