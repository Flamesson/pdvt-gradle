package org.izumi.pdvt.gradle;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
public final class Preconditions {
    private Preconditions() {}

    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
