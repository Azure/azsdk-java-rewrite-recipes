import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.ChangeType;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * ServiceAnnotationTest tests declarative ChangeType recipe on ServiceClient
 * related annotations from com.azure.core.annotations to
 * com.azure.core.v2.annotations
 * NOTE
 * Double up in both packages of ReturnType, ServiceClient, ServiceClientBuilder,
 * ServiceInterface and ServiceMethod.
 * Also tests non-ServiceClient annotation Immutable
 * @author Annabelle Mittendorf Smith
 */
public class ServiceAnnotationsTest implements RewriteTest {

    /**
     * This method sets which recipe should be used for testing
     * @param spec stores settings for testing environment; e.g. which recipes to use for testing
     */
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml",
                "com.azure.rewrite.java.core.MigrateAzureCoreSamplesToAzureCoreV2");
    }

    /**
     * Global Java language strings to help testing.
     */
    @Language("java") String before, after;

    @BeforeEach
    public void setup() {
        before = ""; after = "";
    }

    @Test
    public void testServiceClientAndBuilder() {
        before = "import com.azure.core.annotation.ServiceClient;\n" +
                "import com.azure.core.annotation.ServiceClientBuilder;\n" +
                "\n" +
                "@ServiceClient(builder = ServiceClientBuilder.class)\n" +
                "@ServiceClientBuilder(serviceClients = {TestServiceClient.class})\n" +
                "public class TestServiceClient {\n" +
                "}\n";

        after = "import com.azure.core.v2.annotation.ServiceClient;\n" +
                "import com.azure.core.v2.annotation.ServiceClientBuilder;\n" +
                "\n" +
                "@ServiceClient(builder = ServiceClientBuilder.class)\n" +
                "@ServiceClientBuilder(serviceClients = {TestServiceClient.class})\n" +
                "public class TestServiceClient {\n" +
                "}\n";

        rewriteRun(java(before,after));
    }

    /**
     * ServiceInterface changes to io.clientcore.core.annotations
     */
    @Test
    public void testServiceInterface() {
        before = "import com.azure.core.annotation.ServiceInterface;\n" +
                "\n" +
                "@ServiceInterface(name = \"ServiceInterface\")\n" +
                "public interface ServiceInterface {\n" +
                "}";

        after = "import io.clientcore.core.annotation.ServiceInterface;\n" +
                "\n" +
                "@ServiceInterface(name = \"ServiceInterface\")\n" +
                "public interface ServiceInterface {\n" +
                "}";

        rewriteRun(java(before,after));
    }

    @Test
    public void testServiceMethod() {
        before = "import com.azure.core.annotation.ServiceInterface;\n" +
                "import com.azure.core.annotation.ServiceMethod;\n" +
                "\n" +
                "@ServiceInterface(name = \"ServiceInterface\")\n" +
                "public interface ServiceInterface {\n" +
                "   @ServiceMethod()\n" +
                "   void serviceMethod();\n" +
                "}";

        after = "import com.azure.core.v2.annotation.ServiceMethod;\n" +
                "import io.clientcore.core.annotation.ServiceInterface;\n" +
                "\n" +
                "@ServiceInterface(name = \"ServiceInterface\")\n" +
                "public interface ServiceInterface {\n" +
                "   @ServiceMethod()\n" +
                "   void serviceMethod();\n" +
                "}";

        rewriteRun(java(before,after));
    }

    @Test
    public void testGenerated() {
        before = "import com.azure.core.annotation.ServiceInterface;\n" +
                "import com.azure.core.annotation.Generated;\n" +
                "\n" +
                "@ServiceInterface(name = \"ServiceInterface\")\n" +
                "public interface ServiceInterface {\n" +
                "   @Generated\n" +
                "   String Name;\n" +
                "}";

        after = "import com.azure.core.v2.annotation.Generated;\n" +
                "import io.clientcore.core.annotation.ServiceInterface;\n" +
                "\n" +
                "@ServiceInterface(name = \"ServiceInterface\")\n" +
                "public interface ServiceInterface {\n" +
                "   @Generated\n" +
                "   String Name;\n" +
                "}";

        rewriteRun(java(before,after));
    }

    @Test
    public void testReturnType() {
        before = "import com.azure.core.annotation.ServiceInterface;\n" +
                "import com.azure.core.annotation.ReturnType;\n" +
                "\n" +
                "@ServiceInterface(name = \"ServiceInterface\")\n" +
                "public interface ServiceInterface {\n" +
                "   ReturnType returnType;\n" +
                "}";

        after = "import com.azure.core.v2.annotation.ReturnType;\n" +
                "import io.clientcore.core.annotation.ServiceInterface;\n" +
                "\n" +
                "@ServiceInterface(name = \"ServiceInterface\")\n" +
                "public interface ServiceInterface {\n" +
                "   ReturnType returnType;\n" +
                "}";

        rewriteRun(java(before,after));
    }

    /**
     * Immutable is not a ServiceClient annotation.
     */
    @Test
    public void testImmutable() {
        before = "import com.azure.core.annotation.Immutable;\n" +
                "\n" +
                "@Immutable\n" +
                "public final class TestImmutable {\n" +
                "   private final String val;\n" +
                "}";

        after = "import com.azure.core.v2.annotation.Immutable;\n" +
                "\n" +
                "@Immutable\n" +
                "public final class TestImmutable {\n" +
                "   private final String val;\n" +
                "}";

        rewriteRun(java(before,after));
    }
}
