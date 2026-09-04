package com.crystalgarden;

public enum FormationType {
    PRISM("Prismatic"),
    NEEDLE("Needle"),
    BLADE("Blade"),
    CUBE("Cubic"),
    HOPPER("Hopper / Terraced"),
    SCALENOHEDRON("Scalenohedron / Dogtooth"),
    BIPYRAMID("Bipyramid"),
    STARBURST("Radial Starburst"),
    FAN("Bladed Fan"),
    DENDRITE("Dendritic Branching");

    private final String displayName;

    FormationType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public int id() {
        return ordinal();
    }

    public static String[] labels() {
        FormationType[] values = values();
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i].displayName;
        }
        return labels;
    }
}
