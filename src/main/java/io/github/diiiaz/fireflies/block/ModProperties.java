package io.github.diiiaz.fireflies.block;

import net.minecraft.state.property.IntProperty;

public class ModProperties {

    /**
     * A property that specifies the amount of fireflies in a firefly lantern block.
     */
    public static final IntProperty FIREFLIES_AMOUNT = IntProperty.of("fireflies_amount", 0, 5);
    public static final int FIREFLIES_AMOUNT_MIN = 0;
    public static final int FIREFLIES_AMOUNT_MAX = 5;


    public static void initialize() {}

}
