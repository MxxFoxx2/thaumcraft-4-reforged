package thaumcraft.client.geom;

import org.joml.Matrix4f;

public final class GolemItemTransforms {
    private GolemItemTransforms() { }

    public static Matrix4f fishingRod(float armAngle) {
        return new Matrix4f()
            .rotateX((float) Math.toRadians(5.0F) + armAngle * 0.5F)
            .translate(-0.26875F, 1.6F, -0.53F)
            .rotateY((float) -Math.PI / 2.0F)
            .rotateZ((float) -Math.PI / 6.0F)
            .scale(0.66F, -0.66F, 0.66F)
            .translate(0.5F, 0.5F, -1.0F / 32.0F)
            .rotateY((float) Math.PI);
    }
}
