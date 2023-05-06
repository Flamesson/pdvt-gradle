package org.izumi.pdvt.gradle;

import java.awt.color.ColorSpace;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
public class Color extends java.awt.Color {
    private static final String UPPER_HEX_FORMAT = "#%02X%02X%02X";
    private static final String LOWER_HEX_FORMAT = "#%02x%02x%02x";

    public Color(int r, int g, int b) {
        super(r, g, b);
    }

    public Color(int r, int g, int b, int a) {
        super(r, g, b, a);
    }

    public Color(int rgb) {
        super(rgb);
    }

    public Color(int rgba, boolean hasalpha) {
        super(rgba, hasalpha);
    }

    public Color(float r, float g, float b) {
        super(r, g, b);
    }

    public Color(float r, float g, float b, float a) {
        super(r, g, b, a);
    }

    public Color(ColorSpace cspace, float[] components, float alpha) {
        super(cspace, components, alpha);
    }

    public Color(java.awt.Color color) {
        super(color.getRGB());
    }

    public String toHex() {
        return toHex(false);
    }

    public String toHex(boolean upperCase) {
        if (upperCase) {
            return String.format(UPPER_HEX_FORMAT, getRed(), getGreen(), getBlue());
        } else {
            return String.format(LOWER_HEX_FORMAT, getRed(), getGreen(), getBlue());
        }
    }
}
