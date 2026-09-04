package com.crystalgarden;

public enum SurfaceStyle {
    SMOOTH("Smooth / Glassy"),
    STRIATED("Striated"),
    STEPPED("Stepped / Terraced"),
    METALLIC("Metallic"),
    BANDED("Banded / Zoned"),
    IRIDESCENT("Iridescent");

    private final String displayName;

    SurfaceStyle(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public int id() {
        return ordinal();
    }

    public static String[] labels() {
        SurfaceStyle[] values = values();
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i].displayName;
        }
        return labels;
    }
}
