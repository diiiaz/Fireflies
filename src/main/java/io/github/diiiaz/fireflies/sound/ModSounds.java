package io.github.diiiaz.fireflies.sound;

import io.github.diiiaz.fireflies.Mod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {


    public static final SoundEvent CATCHING_NET_USED = registerSoundEvent("catching_net_used");
    public static final SoundEvent FIREFLY_AMBIENT = registerSoundEvent("firefly_ambient");
    public static final SoundEvent BLOCK_LUMINESCENT_SOIL_ENTER = registerSoundEvent("block_luminescent_soil_enter");
    public static final SoundEvent BLOCK_LUMINESCENT_SOIL_EXIT = registerSoundEvent("block_luminescent_soil_exit");


    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Mod.createIdentifier(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {

    }

}
