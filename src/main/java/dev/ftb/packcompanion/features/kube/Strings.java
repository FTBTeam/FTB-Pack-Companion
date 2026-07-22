package dev.ftb.packcompanion.features.kube;

public class Strings {
    /**
     * Converts a string to capitalized form, where the first letter is uppercase and the rest are lowercase.
     */
    public String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    /**
     * Converts to Title Case, where each word's first letter is uppercase.
     */
    public String titleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String[] words = input.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(capitalize(words[i]));
        }

        return sb.toString();
    }

    /**
     * Null-safe check for whether a string is null or empty.
     */
    public boolean isEmpty(String input) {
        return com.google.common.base.Strings.isNullOrEmpty(input);
    }

    /**
     * Null-safe check for whether a string is null, empty, or only whitespace.
     */
    public boolean isBlank(String input) {
        return input == null || input.isBlank();
    }

    /**
     * Truncates a string to a max length, appending an ellipsis if truncated.
     */
    public String truncate(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) {
            return input;
        }

        return input.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    /**
     * Pads a string on the left with the given character to reach a minimum length.
     */
    public String padLeft(String input, int length, char padChar) {
        return com.google.common.base.Strings.padStart(input, length, padChar);
    }

    public String padRight(String input, int length, char padChar) {
        return com.google.common.base.Strings.padEnd(input, length, padChar);
    }
}
