package top.ribs.scguns.entity.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.monster.CogMinionEntity;

public class CogMinionRenderer extends MobRenderer<CogMinionEntity, CogMinionModel<CogMinionEntity>> {
    public CogMinionRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new CogMinionModel<>(pContext.bakeLayer(ModModelLayers.COG_MINION_LAYER)), 0.7f);
        this.addLayer(new ItemInHandLayer<>(this, pContext.getItemInHandRenderer()));

    }
    @Override
    public ResourceLocation getTextureLocation(CogMinionEntity pEntity) {
        return new ResourceLocation(Reference.MOD_ID, "textures/entity/cog_minion.png");
    }
}


