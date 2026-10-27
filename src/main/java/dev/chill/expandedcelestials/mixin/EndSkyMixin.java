package dev.chill.expandedcelestials.mixin;
import dev.chill.expandedcelestials.ClientEnvironment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(LevelRenderer.class)
public abstract class EndSkyMixin {
    @Redirect(method="renderSky",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/DimensionSpecialEffects;skyType()Lnet/minecraft/client/renderer/DimensionSpecialEffects$SkyType;"),remap=false)
    private DimensionSpecialEffects.SkyType ecplus$endSky(DimensionSpecialEffects original) {
        var level=Minecraft.getInstance().level;
        return level!=null&&ClientEnvironment.endSky(level)?DimensionSpecialEffects.SkyType.END:original.skyType();
    }
}
