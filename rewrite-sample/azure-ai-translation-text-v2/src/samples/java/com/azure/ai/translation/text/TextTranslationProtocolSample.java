package java.com.azure.ai.translation.text;

import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.TextTranslationClientBuilder;
import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import io.clientcore.core.credential.KeyCredential;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.HttpRetryOptions;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.util.Context;
import io.clientcore.core.util.binarydata.BinaryData;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrating the use of protocol client methods to translate text.
 */
public class TextTranslationProtocolSample {
    public static void main(String[] args) throws IOException {

        TextTranslationClient textTranslationClient = new TextTranslationClientBuilder()
                .credential(new KeyCredential("<api-key>"))
                .endpoint("<endpoint>")
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY_AND_HEADERS))
                .httpRetryOptions(new HttpRetryOptions(3, Duration.ofMillis(50)))
                .buildClient();

        List<InputTextItem> inputTextItems = Arrays.asList(new InputTextItem("hello world"));
        List<String> targetLanguages = Arrays.asList("es");
        BinaryData requestBody = BinaryData.fromObject(inputTextItems);
        RequestOptions requestOptions = new RequestOptions().setContext(Context.none());

        Response<BinaryData> binaryDataResponse = textTranslationClient.translateWithResponse(targetLanguages, requestBody, requestOptions);
        List<TranslatedTextItem> result = null;
        try {
            result = binaryDataResponse.getValue().toObject(new ParameterizedType() {
                @Override
                public Type getRawType() {
                    return List.class;
                }

                @Override
                public Type[] getActualTypeArguments() {
                    return new Type[]{TranslatedTextItem.class};
                }

                @Override
                public Type getOwnerType() {
                    return null;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        result.stream()
                .flatMap(translatedTextItem -> translatedTextItem.getTranslations().stream())
                .forEach(translation -> System.out.println("Translated text to " + translation.getTargetLanguage() + " : " + translation.getText()));
    }
}
