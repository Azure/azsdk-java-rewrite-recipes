package java.com.azure.ai.translation.text;

import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.TextTranslationClientBuilder;
import com.azure.ai.translation.text.models.InputTextItem;
import com.azure.ai.translation.text.models.TranslatedTextItem;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.TypeReference;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrating the use of protocol client methods to translate text.
 */
public class UserClass {
    void myMethod() {
        TextTranslationClient textTranslationClient = new TextTranslationClientBuilder().buildClient();

        List<InputTextItem> inputTextItems = Arrays.asList(new InputTextItem("hello world"));

        List<String> targetLanguages = Arrays.asList("es");
        BinaryData requestBody = BinaryData.fromObject(inputTextItems);
        RequestOptions requestOptions = new RequestOptions().setContext(Context.NONE);

        Response<BinaryData> binaryDataResponse = textTranslationClient.translateWithResponse(targetLanguages, requestBody, requestOptions);
        List<TranslatedTextItem> result = binaryDataResponse.getValue().toObject(new TypeReference<List<TranslatedTextItem>>() { });

    }
}
