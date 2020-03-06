package com.kishannareshpal.circularprogressview;

public enum ProgressType {
    INDETERMINATE(0),
    DETERMINATE(1);

    private int id;

    ProgressType(int id) {
        this.id = id;
    }

    static ProgressType fromId(int id) {
        for (ProgressType progressType : values()) {
            if (progressType.id == id) return progressType;
        }
        throw new IllegalArgumentException("There is no ProgressType matching the id: " + id + ". Please check the ProgressType values using #values() method to find all available ids.");
    }

    public int getId() {
        return this.id;
    }
}
