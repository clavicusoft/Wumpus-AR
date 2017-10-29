package com.clavicusoft.wumpus.Maze;

/**
 * Cave Contents have information of entities, or if the cave is empty. Each state have a value.
 */
public enum CaveContent {
    PLAYER(1), EMPTY(1), BAT(2), PIT(3), WUMPUS(4);

    private int value;

    /**
     * Creates a CaveContent with a given value.
     * @param value the value of the entity.
     */
    CaveContent(int value) {
        this.value = value;
    }

    /**
     * Yields the CaveContent value.
     * @return the CaveContent value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets a new value to the CaveContent.
     * @param value the given value.
     */
    public void setValue(int value) {
        this.value = value;
    }

}
