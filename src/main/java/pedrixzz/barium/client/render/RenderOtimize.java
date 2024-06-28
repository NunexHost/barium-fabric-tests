package pedrixzz.barium.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.texture.Texture;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

// Classe para otimizar a renderização
public class RenderOtimize {

    // Distância de renderização (ajuste conforme necessário)
    private static final int renderDistance = 16; 

    // Mapa para armazenar as texturas já carregadas
    private static Map<String, Texture> loadedTextures = new HashMap<>();

    // Método para carregar as texturas
    public static void loadTextures(World world) {
        // Verifica a posição do jogador
        int playerX = (int) world.getPlayer().getX();
        int playerZ = (int) world.getPlayer().getZ();

        // Carrega as texturas dos blocos visíveis na área de renderização
        for (int x = playerX - renderDistance; x <= playerX + renderDistance; x++) {
            for (int z = playerZ - renderDistance; z <= playerZ + renderDistance; z++) {
                // Verifica se o bloco é visível
                Block block = world.getBlockAt(x, 0, z);
                String textureName = block.getTextureName();

                // Carrega a textura apenas se ela não estiver no mapa
                if (!loadedTextures.containsKey(textureName)) {
                    Texture texture = new Texture(textureName);
                    loadedTextures.put(textureName, texture);
                }
            }
        }
    }

    // Método para descarregar texturas
    public static void unloadTextures(World world) {
        // Descarrega as texturas que não estão mais visíveis na área de renderização
        // ... (implementação semelhante a loadTextures)
    }
}
