package pedrixzz.barium.mixin.render;

import pedrixzz.barium.client.render.RenderOtimize;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(World.class)
public class WorldMixin {

    @Inject(at = @At("TAIL"), method = "<init>")
    private void onWorldInit(CallbackInfo ci) {
        RenderOtimize.loadTextures((World) (Object) this);
    }
}
