package top.ribs.scguns.item.attachment.impl;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.apache.commons.lang3.tuple.Pair;
import top.ribs.scguns.debug.IDebugWidget;
import top.ribs.scguns.debug.IEditorMenu;
import top.ribs.scguns.debug.client.screen.widget.DebugSlider;
import top.ribs.scguns.interfaces.IGunModifier;

import java.util.List;
import java.util.function.Supplier;

public class Scope extends Attachment implements IEditorMenu
{
    protected float aimFovModifier;
    protected float additionalZoom;
    protected double reticleOffset;
    protected boolean stable;
    protected double viewFinderDist;

    private Scope() {}

    private Scope(float additionalZoom, double reticleOffset, IGunModifier... modifier)
    {
        super(modifier);
        this.aimFovModifier = 1.0F;
        this.additionalZoom = additionalZoom;
        this.reticleOffset = reticleOffset;
    }

    private Scope(float aimFovModifier, float additionalZoom, double reticleOffset, boolean stable, double viewFinderDist, IGunModifier... modifiers)
    {
        super(modifiers);
        this.aimFovModifier = aimFovModifier;
        this.additionalZoom = additionalZoom;
        this.reticleOffset = reticleOffset;
        this.stable = stable;
        this.viewFinderDist = viewFinderDist;
    }

    public float getFovModifier()
    {
        return this.aimFovModifier;
    }

    @Override
    public Component getEditorLabel()
    {
        return Component.translatable("Scope");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets)
    {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            widgets.add(Pair.of(Component.translatable("Aim FOV Modifier"), () -> new DebugSlider(0.0, 1.0, this.aimFovModifier, 0.05, 3, value -> this.aimFovModifier = value.floatValue())));
            widgets.add(Pair.of(Component.translatable("Zoom (Legacy)"), () -> new DebugSlider(0.0, 0.5, this.additionalZoom, 0.05, 3, value -> this.additionalZoom = value.floatValue())));
            widgets.add(Pair.of(Component.translatable("Reticle Offset"), () -> new DebugSlider(0.0, 4.0, this.reticleOffset, 0.025, 4, value -> this.reticleOffset = value)));
            widgets.add(Pair.of(Component.translatable("View Finder Distance"), () -> new DebugSlider(0.0, 5.0, this.viewFinderDist, 0.05, 3, value -> this.viewFinderDist = value)));
        });
    }

    public Scope copy()
    {
        Scope scope = new Scope();
        scope.aimFovModifier = this.aimFovModifier;
        scope.additionalZoom = this.additionalZoom;
        scope.reticleOffset = this.reticleOffset;
        scope.stable = this.stable;
        scope.viewFinderDist = this.viewFinderDist;
        return scope;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private float aimFovModifier = 1.0F;
        private final float additionalZoom = 0.0F;
        private final double reticleOffset = 0.0;
        private final boolean stable = false;
        private final double viewFinderDist = 0.0;
        private IGunModifier[] modifiers = new IGunModifier[]{};

        private Builder() {}

        public Builder aimFovModifier(float fovModifier)
        {
            this.aimFovModifier = fovModifier;
            return this;
        }

        public Builder modifiers(IGunModifier... modifiers)
        {
            this.modifiers = modifiers;
            return this;
        }

        public Scope build()
        {
            return new Scope(this.aimFovModifier, this.additionalZoom, this.reticleOffset, this.stable, this.viewFinderDist, this.modifiers);
        }
    }
}
