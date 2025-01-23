package io.github.diiiaz.fireflies.entity.custom;

import java.util.Arrays;
import java.util.Comparator;

public enum FireflyVariant {

    DEFAULT(0, -1769666, 4096, "yellow"),
    ORANGE(1, -16342, 2048, "orange"),
    PALE_RED(2, -33411, 512, "pale_red"),
    BLUE_GHOST(3, -14267652, 1, "blue_ghost");


    private final int id;
    private final int color;
    private final int weight;
    private final String name;
    private static final FireflyVariant[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(FireflyVariant::getId)).toArray(FireflyVariant[]::new);


    FireflyVariant(int id, int color, int weight, String name) {
        this.id = id;
        this.color = color;
        this.weight = weight;
        this.name = name;
    }


    public int getId() {
        return id;
    }

    public int getColor() {
        return color;
    }

    public int getWeight() {
        return weight;
    }

    public String getName() {
        return name;
    }


    public static FireflyVariant byId(int id) {
        return BY_ID[id % BY_ID.length];
    }

}
