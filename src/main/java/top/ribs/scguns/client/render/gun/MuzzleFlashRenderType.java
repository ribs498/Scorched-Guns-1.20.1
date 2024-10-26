package top.ribs.scguns.client.render.gun;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class MuzzleFlashRenderType extends RenderType {
    private static final RenderType MUZZLE_FLASH;

    public MuzzleFlashRenderType(String name, VertexFormat vertexFormat, VertexFormat.Mode mode, int bufferSize, boolean hasCrumbling, boolean translucent, Runnable setupState, Runnable clearState) {
        super(name, vertexFormat, mode, bufferSize, hasCrumbling, translucent, setupState, clearState);
    }

    public static RenderType getMuzzleFlash() {
        return MUZZLE_FLASH;
    }

    static {
        MUZZLE_FLASH = create("jeg:muzzle_flash", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, true, false, CompositeState.builder().setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER).setShaderState(RENDERTYPE_EYES_SHADER).setTextureState(new RenderStateShard.TextureStateShard(new ResourceLocation("jeg", "textures/effect/muzzle_flash_1.png"), false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).createCompositeState(false));
    }
}
