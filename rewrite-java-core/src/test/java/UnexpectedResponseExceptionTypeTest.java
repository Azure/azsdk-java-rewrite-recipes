import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * UnexpectedResponseExceptionTypeTest is used to test out the recipe that changes the usage
 * of UnexpectedResponseExceptionType (azure core v1) to UnexpectedResponseExceptionDetail (azure core v2)
 * including all of its parameters.
 * @author Ali Soltanian Fard Jahromi
 */
public class UnexpectedResponseExceptionTypeTest implements RewriteTest {

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
     * This test method is used to make sure that UnexpectedResponseExceptionType and
     * its parameters are converted correctly.
     */
    @Test
    void testUnexpectedResponseExceptionAnnotationChange() {
        @Language("java") String before = "import com.azure.core.annotation.UnexpectedResponseExceptionType;\n" +
                "import com.azure.core.exception.ClientAuthenticationException;\n" +
                "import com.azure.core.exception.HttpResponseException;\n" +
                "import com.azure.core.exception.ResourceModifiedException;\n" +
                "import com.azure.core.exception.ResourceNotFoundException;\n" +
                "public class Testing{" +
                "@UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })\n" +
                        "@UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })\n" +
                        "@UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })\n" +
                        "@UnexpectedResponseExceptionType(HttpResponseException.class)\nprivate String v(){}\n}";

        @Language("java") String after =
                "import com.azure.core.exception.ClientAuthenticationException;\n" +
                "import com.azure.core.exception.HttpResponseException;\n" +
                "import com.azure.core.exception.ResourceModifiedException;\n" +
                "import com.azure.core.exception.ResourceNotFoundException;\n" +
                "import io.clientcore.core.http.annotation.UnexpectedResponseExceptionDetail;\n\n" +
                "public class Testing{" +
                "@UnexpectedResponseExceptionDetail(exceptionTypeName = \"CLIENT_AUTHENTICATION\", statusCode = { 401 })\n" +
                        "@UnexpectedResponseExceptionDetail(exceptionTypeName = \"RESOURCE_NOT_FOUND\", statusCode = { 404 })\n" +
                        "@UnexpectedResponseExceptionDetail(exceptionTypeName = \"RESOURCE_MODIFIED\", statusCode = { 409 })\n" +
                        "@UnexpectedResponseExceptionDetail\nprivate String v(){}\n}";

        rewriteRun(
                java(before, after)
        );

    }
}
