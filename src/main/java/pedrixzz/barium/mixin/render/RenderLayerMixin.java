package pedrixzz.barium.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.render.RenderLayer;

@Mixin(RenderLayer.class)
public abstract class RenderLayerMixin {

    // Exemplo: Uso de um accessor para um recurso estático
    @Accessor("field_24063")
    private static RenderLayer[] getLayers() {
        // Implementação otimizada para obter as camadas de renderização
        return RenderLayer.field_24063;
    }
}
