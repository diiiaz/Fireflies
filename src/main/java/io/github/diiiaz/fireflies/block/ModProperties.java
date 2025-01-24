package io.github.diiiaz.fireflies.block;

import net.minecraft.state.property.IntProperty;

public class ModProperties {

    /**
     * A property that specifies the amount of fireflies in a firefly lantern block.
     */
    public static final int FIREFLIES_LANTERN_AMOUNT_MIN = 0;
    public static final int FIREFLIES_LANTERN_AMOUNT_MAX = 3;
    public static final IntProperty FIREFLIES_LANTERN_AMOUNT = IntProperty.of("fireflies_amount", FIREFLIES_LANTERN_AMOUNT_MIN, FIREFLIES_LANTERN_AMOUNT_MAX);
    /**
     * A property that specifies the amount of fireflies of a luminescent soil.
     */
    public static final int LUMINESCENT_SOIL_AMOUNT_MIN = 0;
    public static final int LUMINESCENT_SOIL_AMOUNT_MAX = 16;
    public static final IntProperty LUMINESCENT_SOIL_FIREFLIES_AMOUNT = IntProperty.of("fireflies_amount", LUMINESCENT_SOIL_AMOUNT_MIN, LUMINESCENT_SOIL_AMOUNT_MAX);


    public static void initialize() {}

}
