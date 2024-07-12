package java.com.azure.ai.translation.text;

import com.azure.ai.translation.text.TextTranslationAsyncClient;
import com.azure.ai.translation.text.TextTranslationClientBuilder;
import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class TextTranslationAsyncSample {
    public static void main(String[] args) {

        TextTranslationAsyncClient client = new TextTranslationClientBuilder()
                .credential(new KeyCredential("<api-key>"))
                .endpoint("<endpoint>")
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .retryOptions(new RetryOptions(new FixedDelayOptions(3, Duration.ofMillis(50))))
                .buildAsyncClient();

        List<InputTextItem> inputTextItems = Arrays.asList(new InputTextItem("hello world"));
        client.translate(Arrays.asList("es"), inputTextItems)
                .subscribe(result -> {
                    result.stream()
                            .flatMap(translatedTextItem -> translatedTextItem.getTranslations().stream())
                            .forEach(translation -> System.out.println("Translated text to " + translation.getTargetLanguage() + " : " + translation.getText()));
                });
    }
}
