package pedrixzz.barium.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RenderOtimize {

    private static final int renderDistance = 16; // Adjust this as needed
    private static final Map<Identifier, SpriteAtlasTexture> loadedTextures = new HashMap<>();
    private static int lastPlayerX = 0;
    private static int lastPlayerZ = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) {
                return; // World is not loaded yet
            }

            // Get current player position
            int playerX = (int) client.player.getX();
            int playerZ = (int) client.player.getZ();

            // Check if the player has moved significantly
            if (Math.abs(playerX - lastPlayerX) > 1 || Math.abs(playerZ - lastPlayerZ) > 1) {
                unloadTextures(client.world);
                loadTextures(client.world);

                lastPlayerX = playerX;
                lastPlayerZ = playerZ;
            }
        });
    }

    public static void loadTextures(World world) {
        int playerX = (int) MinecraftClient.getInstance().player.getX();
        int playerZ = (int) MinecraftClient.getInstance().player.getZ();

        for (int x = playerX - renderDistance; x <= playerX + renderDistance; x++) {
            for (int z = playerZ - renderDistance; z <= playerZ + renderDistance; z++) {
                Optional<Block> block = world.getBlockState(new net.minecraft.util.math.BlockPos(x, 0, z)).getBlock();
                if (block.isPresent()) {
                    Identifier textureName = Registry.BLOCK.getId(block.get());
                    if (!loadedTextures.containsKey(textureName)) {
                        SpriteAtlasTexture texture = new SpriteAtlasTexture(textureName);
                        loadedTextures.put(textureName, texture);
                    }
                }
            }
        }
    }

    public static void unloadTextures(World world) {
        int playerX = (int) MinecraftClient.getInstance().player.getX();
        int playerZ = (int) MinecraftClient.getInstance().player.getZ();

        for (int x = playerX - renderDistance; x <= playerX + renderDistance; x++) {
            for (int z = playerZ - renderDistance; z <= playerZ + renderDistance; z++) {
                Optional<Block> block = world.getBlockState(new net.minecraft.util.math.BlockPos(x, 0, z)).getBlock();
                if (block.isPresent()) {
                    Identifier textureName = Registry.BLOCK.getId(block.get());
                    if (loadedTextures.containsKey(textureName)) {
                        loadedTextures.remove(textureName);
                    }
                }
            }
        }
    }
}
