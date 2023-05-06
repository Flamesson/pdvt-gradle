package org.izumi.pdvt.gradle;

import lombok.RequiredArgsConstructor;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
@RequiredArgsConstructor
public class FilePath {
    private final String value;

    public String withoutExtension() {
        final int lastIndexOfDot = value.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            throw new IllegalStateException("No dots in stored path");
        }

        return value.substring(0, lastIndexOfDot);
    }

    public Extension getExtension() {
        return new Extension(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
