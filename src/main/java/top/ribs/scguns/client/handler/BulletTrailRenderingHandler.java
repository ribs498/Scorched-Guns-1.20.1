package top.ribs.scguns.client.handler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import top.ribs.scguns.client.BulletTrail;
import top.ribs.scguns.entity.projectile.ShulkshotProjectileEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class BulletTrailRenderingHandler
{
    private static BulletTrailRenderingHandler instance;
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft:textures/misc/white.png");

    public static BulletTrailRenderingHandler get()
    {
        if(instance == null)
        {
            instance = new BulletTrailRenderingHandler();
        }
        return instance;
    }

    private final Map<Integer, BulletTrail> bullets = new HashMap<>();

    private BulletTrailRenderingHandler() {}

    /**
     * Adds a bullet trail to render into the world
     *
     * @param trail the bullet trail get
     */
    public void add(BulletTrail trail) {
        Level world = Minecraft.getInstance().level;
        if (world != null) {
            // Check if the entity is not a ShulkshotProjectileEntity before adding the trail
            if (!(world.getEntity(trail.getEntityId()) instanceof ShulkshotProjectileEntity)) {
                this.bullets.put(trail.getEntityId(), trail);
            }
        }
    }
    /**
     * Removes the bullet for the given entity id.
     *
     * @param entityId the entity id of the bullet
     */
    public void remove(int entityId)
    {
        this.bullets.remove(entityId);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        Level world = Minecraft.getInstance().level;
        if(world != null)
        {
            if(event.phase == TickEvent.Phase.END)
            {
                this.bullets.values().forEach(BulletTrail::tick);
                this.bullets.values().removeIf(BulletTrail::isDead);
            }
        }
        else if(!this.bullets.isEmpty())
        {
            this.bullets.clear();
        }
    }

    public void render(PoseStack stack, float partialSticks)
    {
        for(BulletTrail bulletTrail : this.bullets.values())
        {
            this.renderBulletTrail(bulletTrail, stack, partialSticks);
        }
    }

    @SubscribeEvent
    public void onRespawn(ClientPlayerNetworkEvent.Clone event)
    {
        this.bullets.clear();
    }

    @SubscribeEvent
    public void onLoggedOut(ClientPlayerNetworkEvent.LoggingOut event)
    {
        this.bullets.clear();
    }

    private void renderBulletTrail(BulletTrail trail, PoseStack poseStack, float deltaTicks)
    {
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.getCameraEntity();
        if(entity == null || trail.isDead())
            return;

        if(!trail.isTrailVisible()) {
            return;
        }

        poseStack.pushPose();

        Vec3 view = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 position = trail.getPosition();
        Vec3 motion = trail.getMotion();
        double bulletX = position.x + motion.x * deltaTicks;
        double bulletY = position.y + motion.y * deltaTicks;
        double bulletZ = position.z + motion.z * deltaTicks;
        poseStack.translate(bulletX - view.x(), bulletY - view.y(), bulletZ - view.z());

        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(deltaTicks, trail.getYaw(), trail.getYaw()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(deltaTicks, trail.getPitch(), trail.getPitch())));

        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(-4.0F, 0.0F, 0.0F);
        MultiBufferSource.BufferSource renderTypeBuffer = mc.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = renderTypeBuffer.getBuffer(RenderType.energySwirl(TEXTURE, 0.0F, 0.15625F));
        PoseStack.Pose posestack$pose = poseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();

        // Makes the Trail longer the longer airtime it has
        int size = Math.min((trail.getAge() + 1) * 30, 200);

        int color = trail.getTrailColor();
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        int light = 15728880;
        if(trail.isTrailVisible())
        {
            this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, -1 - size, -1, -1, 0.0F, 0.15625F, -1, 0, 0, light);
            this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, -1 - size, -1, 1, 0.15625F, 0.15625F, -1, 0, 0, light);
            this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, -1 - size, 1, 1, 0.15625F, 0.3125F, -1, 0, 0, light);
            this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, -1 - size, 1, -1, 0.0F, 0.3125F, -1, 0, 0, light);

            this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, 1, 1, -1, 0.0F, 0.15625F, 1, 0, 0, light);
            this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, 1, 1, 1, 0.15625F, 0.15625F, 1, 0, 0, light);
            this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, 1, -1, 1, 0.15625F, 0.3125F, 1, 0, 0, light);
            this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, 1, -1, -1, 0.0F, 0.3125F, 1, 0, 0, light);

            for(int j = 0; j < 4; ++j) {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, -1 - size, -1, 1, 0.0F, 0.0F, 0, 1, 0, light);
                this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, 1, -1, 1, 0.5F, 0.0F, 0, 1, 0, light);
                this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, 1, 1, 1, 0.5F, 0.15625F, 0, 1, 0, light);
                this.vertex(red, green, blue, matrix4f, matrix3f, vertexConsumer, -1 - size, 1, 1, 0.0F, 0.15625F, 0, 1, 0, light);
            }
        }

        poseStack.popPose();
    }

    public void vertex(int red, int green, int blue, Matrix4f pMatrix, Matrix3f pNormal, VertexConsumer pConsumer, int pX, int pY, int pZ, float pU, float pV, int pNormalX, int pNormalZ, int pNormalY, int pPackedLight) {

        pConsumer.vertex(pMatrix, (float)pX, (float)pY, (float)pZ)
                .color(red, green, blue, 255)
                .uv(pU, pV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(pPackedLight)
                .normal(pNormal, (float)pNormalX, (float)pNormalY, (float)pNormalZ)
                .endVertex();
    }
}