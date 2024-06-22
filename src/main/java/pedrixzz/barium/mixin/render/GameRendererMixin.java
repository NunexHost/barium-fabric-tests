package pedrixzz.barium.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // Injeção no início do método renderWorld para desativar alguns efeitos de renderização
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void onRenderWorldHead(float partialTicks, long nanoTime, MatrixStack matrixStack, CallbackInfo ci) {
        // Desativar o efeito de contorno para otimização
        MinecraftClient.getInstance().gameRenderer.disableOutlineEffect();
        
        // Desativar o céu durante a renderização para melhorar o desempenho
        MinecraftClient.getInstance().options.viewDistance = 6;

        // Desativar a renderização de partículas para otimização
        MinecraftClient.getInstance().particleManager.setParticlesVisible(false);
    }

    // Injeção no final do método renderWorld para restaurar configurações originais
    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void onRenderWorldReturn(float partialTicks, long nanoTime, MatrixStack matrixStack, CallbackInfo ci) {
        // Restaurar a configuração do view distance
        MinecraftClient.getInstance().options.viewDistance = MinecraftClient.getInstance().options.graphics.viewDistance;

        // Restaurar a visibilidade das partículas
        MinecraftClient.getInstance().particleManager.setParticlesVisible(true);
    }

    // Injeção para reduzir a qualidade dos shaders para otimização
    @Inject(method = "getFovMultiplier", at = @At("RETURN"), cancellable = true)
    private void onGetFovMultiplier(float partialTicks, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        // Reduzir o multiplicador de FOV para otimização
        float fovMultiplier = cir.getReturnValue();
        cir.setReturnValue(fovMultiplier * 0.75f); // Reduzindo o FOV em 25%
    }

    // Injeção para desativar a atualização de entidades enquanto renderiza
    @Inject(method = "updateTargetedEntity", at = @At("HEAD"), cancellable = true)
    private void onUpdateTargetedEntity(CallbackInfo ci) {
        // Cancelar a atualização do alvo para otimizar
        ci.cancel();
    }
}
