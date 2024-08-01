// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.ai.translation.text.models;

import com.azure.core.v2.annotation.Generated;
import com.azure.core.v2.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Translation source term.
 */
@Immutable
public final class DictionaryTranslation implements JsonSerializable<DictionaryTranslation> {
    /*
     * A string giving the normalized form of this term in the target language.
     * This value should be used as input to lookup examples.
     */
    @Generated
    private final String normalizedTarget;

    /*
     * A string giving the term in the target language and in a form best suited
     * for end-user display. Generally, this will only differ from the normalizedTarget
     * in terms of capitalization. For example, a proper noun like "Juan" will have
     * normalizedTarget = "juan" and displayTarget = "Juan".
     */
    @Generated
    private final String displayTarget;

    /*
     * A string associating this term with a part-of-speech tag.
     */
    @Generated
    private final String posTag;

    /*
     * A value between 0.0 and 1.0 which represents the "confidence"
     * (or perhaps more accurately, "probability in the training data") of that translation pair.
     * The sum of confidence scores for one source word may or may not sum to 1.0.
     */
    @Generated
    private final double confidence;

    /*
     * A string giving the word to display as a prefix of the translation. Currently,
     * this is the gendered determiner of nouns, in languages that have gendered determiners.
     * For example, the prefix of the Spanish word "mosca" is "la", since "mosca" is a feminine noun in Spanish.
     * This is only dependent on the translation, and not on the source.
     * If there is no prefix, it will be the empty string.
     */
    @Generated
    private final String prefixWord;

    /*
     * A list of "back translations" of the target. For example, source words that the target can translate to.
     * The list is guaranteed to contain the source word that was requested (e.g., if the source word being
     * looked up is "fly", then it is guaranteed that "fly" will be in the backTranslations list).
     * However, it is not guaranteed to be in the first position, and often will not be.
     */
    @Generated
    private final List<BackTranslation> backTranslations;

    /**
     * Creates an instance of DictionaryTranslation class.
     * 
     * @param normalizedTarget the normalizedTarget value to set.
     * @param displayTarget the displayTarget value to set.
     * @param posTag the posTag value to set.
     * @param confidence the confidence value to set.
     * @param prefixWord the prefixWord value to set.
     * @param backTranslations the backTranslations value to set.
     */
    @Generated
    private DictionaryTranslation(String normalizedTarget, String displayTarget, String posTag, double confidence,
        String prefixWord, List<BackTranslation> backTranslations) {
        this.normalizedTarget = normalizedTarget;
        this.displayTarget = displayTarget;
        this.posTag = posTag;
        this.confidence = confidence;
        this.prefixWord = prefixWord;
        this.backTranslations = backTranslations;
    }

    /**
     * Get the normalizedTarget property: A string giving the normalized form of this term in the target language.
     * This value should be used as input to lookup examples.
     * 
     * @return the normalizedTarget value.
     */
    @Generated
    public String getNormalizedTarget() {
        return this.normalizedTarget;
    }

    /**
     * Get the displayTarget property: A string giving the term in the target language and in a form best suited
     * for end-user display. Generally, this will only differ from the normalizedTarget
     * in terms of capitalization. For example, a proper noun like "Juan" will have
     * normalizedTarget = "juan" and displayTarget = "Juan".
     * 
     * @return the displayTarget value.
     */
    @Generated
    public String getDisplayTarget() {
        return this.displayTarget;
    }

    /**
     * Get the posTag property: A string associating this term with a part-of-speech tag.
     * 
     * @return the posTag value.
     */
    @Generated
    public String getPosTag() {
        return this.posTag;
    }

    /**
     * Get the confidence property: A value between 0.0 and 1.0 which represents the "confidence"
     * (or perhaps more accurately, "probability in the training data") of that translation pair.
     * The sum of confidence scores for one source word may or may not sum to 1.0.
     * 
     * @return the confidence value.
     */
    @Generated
    public double getConfidence() {
        return this.confidence;
    }

    /**
     * Get the prefixWord property: A string giving the word to display as a prefix of the translation. Currently,
     * this is the gendered determiner of nouns, in languages that have gendered determiners.
     * For example, the prefix of the Spanish word "mosca" is "la", since "mosca" is a feminine noun in Spanish.
     * This is only dependent on the translation, and not on the source.
     * If there is no prefix, it will be the empty string.
     * 
     * @return the prefixWord value.
     */
    @Generated
    public String getPrefixWord() {
        return this.prefixWord;
    }

    /**
     * Get the backTranslations property: A list of "back translations" of the target. For example, source words that
     * the target can translate to.
     * The list is guaranteed to contain the source word that was requested (e.g., if the source word being
     * looked up is "fly", then it is guaranteed that "fly" will be in the backTranslations list).
     * However, it is not guaranteed to be in the first position, and often will not be.
     * 
     * @return the backTranslations value.
     */
    @Generated
    public List<BackTranslation> getBackTranslations() {
        return this.backTranslations;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("normalizedTarget", this.normalizedTarget);
        jsonWriter.writeStringField("displayTarget", this.displayTarget);
        jsonWriter.writeStringField("posTag", this.posTag);
        jsonWriter.writeDoubleField("confidence", this.confidence);
        jsonWriter.writeStringField("prefixWord", this.prefixWord);
        jsonWriter.writeArrayField("backTranslations", this.backTranslations,
            (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DictionaryTranslation from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of DictionaryTranslation if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the DictionaryTranslation.
     */
    @Generated
    public static DictionaryTranslation fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String normalizedTarget = null;
            String displayTarget = null;
            String posTag = null;
            double confidence = 0.0;
            String prefixWord = null;
            List<BackTranslation> backTranslations = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("normalizedTarget".equals(fieldName)) {
                    normalizedTarget = reader.getString();
                } else if ("displayTarget".equals(fieldName)) {
                    displayTarget = reader.getString();
                } else if ("posTag".equals(fieldName)) {
                    posTag = reader.getString();
                } else if ("confidence".equals(fieldName)) {
                    confidence = reader.getDouble();
                } else if ("prefixWord".equals(fieldName)) {
                    prefixWord = reader.getString();
                } else if ("backTranslations".equals(fieldName)) {
                    backTranslations = reader.readArray(reader1 -> BackTranslation.fromJson(reader1));
                } else {
                    reader.skipChildren();
                }
            }
            return new DictionaryTranslation(normalizedTarget, displayTarget, posTag, confidence, prefixWord,
                backTranslations);
        });
    }
}