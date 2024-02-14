package pedrixzz.barium.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    private static final float RADIANS_PER_DEGREE = 0.017453292F;
    private static final float TWO_PI = 6.2831855F;

    private float cachedSkyAngle;

    @WrapOperation(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getSkyAngle(F)F", ordinal = 0))
    private float cacheSkyAngle(ClientWorld world, float delta, Operation<Float> original,
                                @Share("skyAngle") LocalFloatRef skyAngle) {
        cachedSkyAngle = original.call(world, delta);
        return cachedSkyAngle;
    }

    @Redirect(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getSkyAngleRadians(F)F"))
    private float getSkyAngleRadians(ClientWorld world, float delta) {
        return cachedSkyAngle * TWO_PI;
    }

    @Redirect(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getSkyAngle(F)F", ordinal = 1))
    private float getSkyAngle(ClientWorld world, float delta) {
        return cachedSkyAngle;
    }
}
