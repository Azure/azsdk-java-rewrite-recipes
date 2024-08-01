// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.ai.translation.text.models;

/**
 * Translator profanity actions.
 */
public enum ProfanityAction {
    /**
     * No Action is taken on profanity.
     */
    NO_ACTION("NoAction"),

    /**
     * Profanity is marked.
     */
    MARKED("Marked"),

    /**
     * Profanity is deleted from the translated text.
     */
    DELETED("Deleted");

    /**
     * The actual serialized value for a ProfanityAction instance.
     */
    private final String value;

    ProfanityAction(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ProfanityAction instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ProfanityAction object, or null if unable to parse.
     */
    public static ProfanityAction fromString(String value) {
        if (value == null) {
            return null;
        }
        ProfanityAction[] items = ProfanityAction.values();
        for (ProfanityAction item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value;
    }
}