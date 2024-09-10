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
        @Language("java") String before = "import com.azure.core.client.traits.HttpTrait;\n" +
                "import com.azure.core.http.HttpClient;\n" +
                "import com.azure.core.http.HttpPipeline;\n" +
                "import com.azure.core.http.policy.HttpLogOptions;\n" +
                "import com.azure.core.http.policy.HttpPipelinePolicy;\n" +
                "import com.azure.core.http.policy.RetryOptions;\n" +
                "import com.azure.core.util.ClientOptions;\n" +
                "\n" +
                "public class BlankHttpTrait implements HttpTrait<BlankHttpTrait> {\n" +
                "    @Override\n" +
                "    public BlankHttpTrait httpClient(HttpClient httpClient) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait pipeline(HttpPipeline pipeline) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait addPolicy(HttpPipelinePolicy pipelinePolicy) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait retryOptions(RetryOptions retryOptions) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait httpLogOptions(HttpLogOptions logOptions) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait clientOptions(ClientOptions clientOptions) {\n" +
                "        return null;\n" +
                "    }\n" +
                "}";

        @Language("java") String after = "import com.azure.core.client.traits.HttpTrait;\n" +
                "import com.azure.core.http.policy.HttpLogOptions;\n" +
                "import com.azure.core.http.policy.RetryOptions;\n" +
                "import io.clientcore.core.http.client.HttpClient;\n" +
                //"import io.clientcore.core.http.models.HttpLogOptions;\n" +
                "import io.clientcore.core.http.models.HttpRedirectOptions;\n" +
                //"import io.clientcore.core.http.models.HttpRetryOptions;\n" +
                "import io.clientcore.core.http.pipeline.HttpPipeline;\n" +
                "import io.clientcore.core.http.pipeline.HttpPipelinePolicy;\n" +
                "\n" +
                "public class BlankHttpTrait implements HttpTrait<BlankHttpTrait> {\n" +
                "    @Override\n" +
                "    public BlankHttpTrait httpClient(HttpClient httpClient) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait httpPipeline(HttpPipeline pipeline) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait addHttpPipelinePolicy(HttpPipelinePolicy pipelinePolicy) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait httpRetryOptions(RetryOptions retryOptions) {\n" + //HttpRetryOptions
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait httpLogOptions(HttpLogOptions logOptions) {\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    private HttpRedirectOptions redirectOptions;\n" +
                "\n" +
                "    @Override\n" +
                "    public BlankHttpTrait httpRedirectOptions(HttpRedirectOptions redirectOptions) {\n" +
                "        this.redirectOptions = redirectOptions;\n" +
                "        return this;\n" +
                "    }\n" +
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

        @Language("java") String after = "import com.azure.core.client.traits.HttpTrait;\n" +
                "import com.azure.core.util.ClientOptions;\n" +
                "import io.clientcore.core.http.models.HttpRedirectOptions;\n" +
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