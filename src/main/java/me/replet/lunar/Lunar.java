package me.replet.lunar;

import finalforeach.cosmicreach.gamestates.GameState;
import me.replet.lunar.api.modmenu.ConfigScreenFactory;
import me.replet.lunar.api.modmenu.ModConfigButtonAPI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lunar implements ClientModInitializer {

    public static Map<String, ConfigScreenFactory<?>> configScreenFactories = new HashMap<>();
    public static List<Map<String, ConfigScreenFactory<?>>> delayedScreenFactoryProviders = new ArrayList<>();

    public static GameState getConfigScreen(String modid, GameState menuScreen) {
        if(!delayedScreenFactoryProviders.isEmpty()) {
            delayedScreenFactoryProviders.forEach(map -> map.forEach(configScreenFactories::putIfAbsent));
            delayedScreenFactoryProviders.clear();
        }

        ConfigScreenFactory<?> factory = configScreenFactories.get(modid);
        if (factory != null) {
            return factory.create(menuScreen);
        }
        return null;
    }
    @Override
    public void onInitializeClient() {
        FabricLoader.getInstance().getEntrypointContainers("modmenu", ModConfigButtonAPI.class).forEach(entrypoint  -> {
            ModMetadata metadata = entrypoint.getProvider().getMetadata();
            String modId = metadata.getId();
            try {
                ModConfigButtonAPI api = entrypoint.getEntrypoint();
                configScreenFactories.put(modId, api.getModConfigScreenFactory());
                delayedScreenFactoryProviders.add(api.getProvidedConfigScreenFactories());
            } catch (Throwable e) {
                Log.error(LogCategory.LOG,"Mod {} provides a broken implementation of ModMenuApi", modId, e);
            }
        });
    }
}