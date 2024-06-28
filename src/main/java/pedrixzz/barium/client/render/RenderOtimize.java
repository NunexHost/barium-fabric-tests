package pedrixzz.barium.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.event.listener.WorldEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RenderOtimize {

    private static final int renderDistance = 16;
    private static final Map<Identifier, SpriteAtlasTexture> loadedTextures = new HashMap<>();

    @Override
    public void onInitializeClient() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        minecraftClient.world.getEventManager().register(World.class, new WorldEventListener() {
            @Override
            public void onPlayerMove(World world, double x, double y, double z) {
                unloadTextures(world);
                loadTextures(world);
            }
        });
    }

    public static void loadTextures(World world) {
        if (world == null) return;

        // Use o jogador atual
        int playerX = (int) world.getCameras().iterator().next().getPos().x;
        int playerZ = (int) world.getCameras().iterator().next().getPos().z;

        for (int x = playerX - renderDistance; x <= playerX + renderDistance; x++) {
            for (int z = playerZ - renderDistance; z <= playerZ + renderDistance; z++) {
                // Pega o bloco na posição
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
        if (world == null) return;

        // Use o jogador atual
        int playerX = (int) world.getCameras().iterator().next().getPos().x;
        int playerZ = (int) world.getCameras().iterator().next().getPos().z;

        for (int x = playerX - renderDistance; x <= playerX + renderDistance; x++) {
            for (int z = playerZ - renderDistance; z <= playerZ + renderDistance; z++) {
                // Pega o bloco na posição
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
