package io.github.diiiaz.fireflies.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;


@Environment(EnvType.CLIENT)
public class FireflyEntityRenderState extends LivingEntityRenderState {
    public int baseColor = -1;
}
