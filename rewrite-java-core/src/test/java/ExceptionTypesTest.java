import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class ExceptionTypesTest implements RewriteTest {
    /**
     * ExceptionTypesTest tests exception migrations from azure-core v1
     * to azure-core-v2 and client-core.
     * Recipes used: ChangeType
     * From:
     * com.azure.core.exception
     *      ClientAuthenticationException
     *      HttpResponseException
     *      ResourceModifiedException
     *      ResourceNotFoundException
     * To:
     * com.azure.core.v2.exception
     *      ClientAuthenticationException
     *      ResourceModifiedException
     *      ResourceNotFoundException
     * io.clientcore.core.http.exception
     *      HttpResponseException
     *
     * @author Annabelle Mittendorf Smith
     */

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml",
                "com.azure.rewrite.java.core.MigrateAzureCoreSamplesToAzureCoreV2");
    }

    /* Testing ChangeType recipes */
    @Test
    public void testClientAuthenticationExceptionChanged() {
        @Language("java") String before = "import com.azure.core.exception.ClientAuthenticationException;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.exception.ClientAuthenticationException e = new ClientAuthenticationException();";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.v2.exception.ClientAuthenticationException;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    com.azure.core.v2.exception.ClientAuthenticationException e = new ClientAuthenticationException();";
        after += "\n  }";
        after += "\n}";

        rewriteRun(
                java(before,after)
        );
    }

    @Test
    public void testHttpResponseExceptionChanged() {
        @Language("java") String before = "import com.azure.core.exception.HttpResponseException;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.exception.HttpResponseException e = new HttpResponseException();";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.exception.HttpResponseException;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    io.clientcore.core.http.exception.HttpResponseException e = new HttpResponseException();";
        after += "\n  }";
        after += "\n}";

        rewriteRun(
                java(before,after)
        );
    }

    @Test
    public void testResourceModifiedExceptionChanged() {
        @Language("java") String before = "import com.azure.core.exception.ResourceModifiedException;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.exception.ResourceModifiedException e = new ResourceModifiedException();";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.v2.exception.ResourceModifiedException;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    com.azure.core.v2.exception.ResourceModifiedException e = new ();";
        after += "\n  }";
        after += "\n}";

        rewriteRun(
                java(before,after)
        );
    }

    @Test
    public void testResourceNotFoundExceptionChanged() {
        @Language("java") String before = "import com.azure.core.exception.ResourceNotFoundException;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.exception.ResourceNotFoundException e = new ResourceNotFoundException();";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.v2.exception.ResourceNotFoundException;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    com.azure.core.v2.exception.ResourceNotFoundException e = new ResourceNotFoundException();";
        after += "\n  }";
        after += "\n}";

        rewriteRun(
                java(before,after)
        );
    }

    /**
     * Will fail and need updating if all azure-core v1 exceptions are migrated,
     * or if all exceptions are migrated to the same directory.
     */
    @Test
    public void testBundledImportsChanged() {
        @Language("java") String before = "import com.azure.core.exception.*;";
        before += "\npublic class Testing {";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.exception.*;" +
                "\nimport com.azure.core.v2.exception.*;" +
                "\nio.clientcore.core.http.exception.HttpResponseException;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    io.clientcore.core.util.configuration.Configuration c = new Configuration();";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }
}
