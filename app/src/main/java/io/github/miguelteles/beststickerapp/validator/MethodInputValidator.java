package io.github.miguelteles.beststickerapp.validator;

public class MethodInputValidator {

    private static final String CANNOT_BE_NULL = "%s must be not null";
    private static final String CANNOT_BE_EMPTY = "%s must be not empty";

    public static void requireNotNull(Object value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(String.format(CANNOT_BE_NULL, field));
        }
    }

    public static void requireNotEmpty(String value, String field) {
        requireNotNull(value, field);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(String.format(CANNOT_BE_EMPTY, field));
        }
    }

    public static void requireNotEmpty(Integer value, String field) {
        requireNotNull(value, field);
        if (value == 0) {
            throw new IllegalArgumentException(String.format(CANNOT_BE_EMPTY, field));
        }
    }

}
