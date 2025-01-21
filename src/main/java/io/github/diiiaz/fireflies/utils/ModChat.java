package io.github.diiiaz.fireflies.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

@SuppressWarnings("unused")
public class ModChat {

    public static void print(Object object) {
        if (MinecraftClient.getInstance() != null) {
            MinecraftClient.getInstance().getMessageHandler().onGameMessage(Text.of(String.valueOf(object)), false);
        }
    }

}
