package io.github.diiiaz.fireflies.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ModChat {

    public static void print(String message) {
        if (MinecraftClient.getInstance() != null) {
            MinecraftClient.getInstance().getMessageHandler().onGameMessage(Text.of(message), false);
        }
    }

}
