package io.github.diiiaz.fireflies.utils;

import io.github.diiiaz.fireflies.Mod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

@SuppressWarnings("unused")
public class ModChat {

    public static void print(Object object) {
        if (MinecraftClient.getInstance() != null) {
            MinecraftClient.getInstance().getMessageHandler().onGameMessage(Text.of(String.valueOf(object)), false);
        } else {
            Mod.LOGGER.error("{} (This message was intended for Minecraft client, but none was found.)", object);
        }
    }

}
