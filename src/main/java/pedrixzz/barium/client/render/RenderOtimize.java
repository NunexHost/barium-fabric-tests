package pedrixzz.barium.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;

import java.util.HashMap;
import java.util.Map;

public class RenderOtimize {

    private static final int renderDistance = 16;
    private static final Map<Identifier, SpriteAtlasTexture> loadedTextures = new HashMap<>();

    @Override
    public void onInitializeClient() {
        // Registra um listener para descarregar as texturas quando o jogador se mover
        MinecraftClient.getInstance().world.getEventManager().register(World.class, new GameEventListener() {
            @Override
            public void onEvent(World world, GameEvent event) {
                if (event == GameEvent.PLAYER_MOVE) {
                    unloadTextures(world);
                    loadTextures(world);
                }
            }
        });
    }

    public static void loadTextures(World world) {
        if (world == null) return;
        int playerX = (int) world.getPlayer().getX();
        int playerZ = (int) world.getPlayer().getZ();

        for (int x = playerX - renderDistance; x <= playerX + renderDistance; x++) {
            for (int z = playerZ - renderDistance; z <= playerZ + renderDistance; z++) {
                Block block = world.getBlockAt(x, 0, z);
                Identifier textureName = block.getRegistryEntry().getKey();
                if (!loadedTextures.containsKey(textureName)) {
                    SpriteAtlasTexture texture = new SpriteAtlasTexture(textureName);
                    loadedTextures.put(textureName, texture);
                }
            }
        }
    }

    public static void unloadTextures(World world) {
        if (world == null) return;
        int playerX = (int) world.getPlayer().getX();
        int playerZ = (int) world.getPlayer().getZ();
        for (int x = playerX - renderDistance; x <= playerX + renderDistance; x++) {
            for (int z = playerZ - renderDistance; z <= playerZ + renderDistance; z++) {
                Block block = world.getBlockAt(x, 0, z);
                Identifier textureName = block.getRegistryEntry().getKey();
                if (loadedTextures.containsKey(textureName)) {
                    loadedTextures.remove(textureName);
                }
            }
        }
    }
}
