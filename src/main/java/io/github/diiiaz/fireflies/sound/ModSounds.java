package io.github.diiiaz.fireflies.sound;

import io.github.diiiaz.fireflies.Mod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {


    public static final SoundEvent BOTTLE_USED = registerSoundEvent("bottle_used");
    public static final SoundEvent FIREFLY_AMBIENT = registerSoundEvent("firefly_ambient");


    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(Mod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {

    }

}
