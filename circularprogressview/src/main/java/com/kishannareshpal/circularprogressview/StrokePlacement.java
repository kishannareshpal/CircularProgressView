package com.kishannareshpal.circularprogressview;

public enum StrokePlacement {
    INSIDE(0),
    OUTSIDE(1),
    CENTER(2);

    private int id;
    StrokePlacement(int id) {
        this.id = id;
    }

    static StrokePlacement fromId(int id) {
        for (StrokePlacement strokePlacement : values()) {
            if (strokePlacement.id == id) return strokePlacement;
        }
        throw new IllegalArgumentException("There is no StrokePlacement matching the id: " + id + ". Please check the StrokePlacement values using #values() to find all available ids.");
    }

    public int getId() {
        return id;
    }
}
