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
        before += "\n    com.azure.core.exception.ClientAuthenticationException e = new ClientAuthenticationException(null,null);";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.v2.exception.ClientAuthenticationException;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    com.azure.core.v2.exception.ClientAuthenticationException e = new ClientAuthenticationException(null,null);";
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
        before += "\n    com.azure.core.exception.HttpResponseException e = new HttpResponseException(null,null);";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.exception.HttpResponseException;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    io.clientcore.core.http.exception.HttpResponseException e = new HttpResponseException(null,null);";
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
        before += "\n    com.azure.core.exception.ResourceModifiedException e = new ResourceModifiedException(null,null);";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.v2.exception.ResourceModifiedException;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    com.azure.core.v2.exception.ResourceModifiedException e = new ResourceModifiedException(null,null);";
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
        before += "\n    com.azure.core.exception.ResourceNotFoundException e = new ResourceNotFoundException(null,null);";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.v2.exception.ResourceNotFoundException;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    com.azure.core.v2.exception.ResourceNotFoundException e = new ResourceNotFoundException(null,null);";
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
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.exception.ClientAuthenticationException e = new ClientAuthenticationException(null,null);";
        before += "\n    com.azure.core.exception.HttpResponseException e = new HttpResponseException(null,null);";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.exception.*;" +
                "\nimport com.azure.core.v2.exception.ClientAuthenticationException;" +
                "\nimport io.clientcore.core.http.exception.HttpResponseException;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    com.azure.core.v2.exception.ClientAuthenticationException e = new ClientAuthenticationException(null,null);";
        after += "\n    io.clientcore.core.http.exception.HttpResponseException e = new HttpResponseException(null,null);";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }
}
