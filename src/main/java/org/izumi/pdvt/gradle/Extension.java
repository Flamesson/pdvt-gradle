package org.izumi.pdvt.gradle;

import lombok.RequiredArgsConstructor;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
@RequiredArgsConstructor
public class Extension {
    private final String path;

    public String get() {
        final int lastIndexOfDot = path.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            throw new IllegalStateException("No dots in stored path");
        }

        return path.substring(lastIndexOfDot);
    }
}
