// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.ai.translation.text.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Element containing the translated text.
 */
@Immutable
public final class TranslatedTextItem implements JsonSerializable<TranslatedTextItem> {
    /*
     * The detectedLanguage property is only present in the result object when language auto-detection is requested.
     */
    @Generated
    private DetectedLanguage detectedLanguage;

    /*
     * An array of translation results. The size of the array matches the number of target
     * languages specified through the to query parameter.
     */
    @Generated
    private final List<TranslationText> translations;

    /*
     * Input text in the default script of the source language. sourceText property is present only when
     * the input is expressed in a script that's not the usual script for the language. For example,
     * if the input were Arabic written in Latin script, then sourceText.text would be the same Arabic text
     * converted into Arab script.
     */
    @Generated
    private SourceText sourceText;

    /**
     * Creates an instance of TranslatedTextItem class.
     * 
     * @param translations the translations value to set.
     */
    @Generated
    private TranslatedTextItem(List<TranslationText> translations) {
        this.translations = translations;
    }

    /**
     * Get the detectedLanguage property: The detectedLanguage property is only present in the result object when
     * language auto-detection is requested.
     * 
     * @return the detectedLanguage value.
     */
    @Generated
    public DetectedLanguage getDetectedLanguage() {
        return this.detectedLanguage;
    }

    /**
     * Get the translations property: An array of translation results. The size of the array matches the number of
     * target
     * languages specified through the to query parameter.
     * 
     * @return the translations value.
     */
    @Generated
    public List<TranslationText> getTranslations() {
        return this.translations;
    }

    /**
     * Get the sourceText property: Input text in the default script of the source language. sourceText property is
     * present only when
     * the input is expressed in a script that's not the usual script for the language. For example,
     * if the input were Arabic written in Latin script, then sourceText.text would be the same Arabic text
     * converted into Arab script.
     * 
     * @return the sourceText value.
     */
    @Generated
    public SourceText getSourceText() {
        return this.sourceText;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("translations", this.translations, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeJsonField("detectedLanguage", this.detectedLanguage);
        jsonWriter.writeJsonField("sourceText", this.sourceText);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of TranslatedTextItem from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of TranslatedTextItem if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the TranslatedTextItem.
     */
    @Generated
    public static TranslatedTextItem fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            List<TranslationText> translations = null;
            DetectedLanguage detectedLanguage = null;
            SourceText sourceText = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("translations".equals(fieldName)) {
                    translations = reader.readArray(reader1 -> TranslationText.fromJson(reader1));
                } else if ("detectedLanguage".equals(fieldName)) {
                    detectedLanguage = DetectedLanguage.fromJson(reader);
                } else if ("sourceText".equals(fieldName)) {
                    sourceText = SourceText.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            TranslatedTextItem deserializedTranslatedTextItem = new TranslatedTextItem(translations);
            deserializedTranslatedTextItem.detectedLanguage = detectedLanguage;
            deserializedTranslatedTextItem.sourceText = sourceText;

            return deserializedTranslatedTextItem;
        });
    }
}