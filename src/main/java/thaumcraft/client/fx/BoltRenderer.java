package thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.LightningBolt;
import thaumcraft.client.geom.LightningGeometry;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.platform.VertexQuadSink;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class BoltRenderer {
    private static final ContextKey<Frame> DATA_KEY = new ContextKey<>(Thaumcraft.id("lightning_bolts"));
    private static final ResourceLocation LARGE = Thaumcraft.id("textures/misc/p_large.png");
    private static final ResourceLocation SMALL = Thaumcraft.id("textures/misc/p_small.png");
    private static final float[][] OUTER_COLORS = {
        {0.6F, 0.3F, 0.6F},
        {0.6F, 0.6F, 0.1F},
        {0.1F, 0.1F, 0.6F},
        {0.1F, 1.0F, 0.1F},
        {0.6F, 0.1F, 0.1F},
        {0.6F, 0.2F, 0.6F},
        {0.75F, 1.0F, 1.0F}
    };
    private static final float[][] INNER_COLORS = {
        {1.0F, 0.6F, 1.0F},
        {1.0F, 1.0F, 0.1F},
        {0.1F, 0.1F, 1.0F},
        {0.1F, 0.6F, 0.1F},
        {1.0F, 0.1F, 0.1F},
        {0.0F, 0.0F, 0.0F},
        {0.75F, 1.0F, 1.0F}
    };
    private static final double VISIBLE_DISTANCE = 100.0;
    private static final List<LightningBolt> BOLTS = new ArrayList<>();

    private BoltRenderer() {
    }

    public static void add(LightningBolt bolt) {
        BOLTS.add(bolt.finalizeBolt());
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            BOLTS.clear();
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        BOLTS.removeIf(bolt -> {
            bolt.tick();
            return bolt.isDead();
        });
    }

    @SubscribeEvent
    static void onExtract(ExtractLevelRenderStateEvent event) {
        if (BOLTS.isEmpty()) {
            return;
        }
        List<Snapshot> snapshots = new ArrayList<>(BOLTS.size());
        for (LightningBolt bolt : BOLTS) {
            snapshots.add(new Snapshot(bolt, bolt.age()));
        }
        event.getRenderState().setRenderData(DATA_KEY, new Frame(snapshots, event.getDeltaTracker().getGameTimeDeltaPartialTick(false)));
    }

    @SubscribeEvent
    static void onSubmit(SubmitCustomGeometryEvent event) {
        Frame frame = event.getLevelRenderState().getRenderData(DATA_KEY);
        if (frame == null) {
            return;
        }
        CameraRenderState camera = event.getLevelRenderState().cameraRenderState;
        Vec3 look = Vec3.directionFromRotation(camera.xRot, camera.yRot);
        PoseStack poseStack = event.getPoseStack();
        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        for (Snapshot snapshot : frame.bolts()) {
            LightningBolt bolt = snapshot.bolt();
            if (camera.pos.distanceTo(new Vec3(bolt.startX(), bolt.startY(), bolt.startZ())) > VISIBLE_DISTANCE) {
                continue;
            }
            int type = Math.clamp(bolt.type(), 0, OUTER_COLORS.length - 1);
            boolean translucent = type == 5 || type == 6;
            submitPass(collector, poseStack, LARGE, translucent, snapshot, frame.partialTick(), camera.pos, look, OUTER_COLORS[type], 0);
            submitPass(collector, poseStack, SMALL, translucent, snapshot, frame.partialTick(), camera.pos, look, INNER_COLORS[type], 1);
        }
    }

    private static void submitPass(SubmitNodeCollector collector, PoseStack poseStack, ResourceLocation texture, boolean translucent, Snapshot snapshot, float partialTick, Vec3 cameraPos, Vec3 look, float[] color, int pass) {
        collector.submitCustomGeometry(
            poseStack,
            TcRenderTypes.of(translucent ? TcRenderTypes.Kind.TRANSLUCENT : TcRenderTypes.Kind.ADDITIVE, texture),
            (pose, buffer) -> LightningGeometry.render(
                snapshot.bolt(),
                snapshot.age(),
                partialTick,
                (float) cameraPos.x,
                (float) cameraPos.y,
                (float) cameraPos.z,
                (float) look.x,
                (float) look.y,
                (float) look.z,
                color[0],
                color[1],
                color[2],
                pass,
                new VertexQuadSink(pose, buffer)
            )
        );
    }

    private record Snapshot(LightningBolt bolt, int age) {
    }

    private record Frame(List<Snapshot> bolts, float partialTick) {
    }
}
