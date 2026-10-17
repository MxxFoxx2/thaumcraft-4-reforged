import org.joml.Matrix4f;
import org.joml.Vector3f;
import thaumcraft.client.geom.GolemItemTransforms;

public final class GolemItemTransformsTest {
    public static void main(String[] args) {
        Matrix4f idle = GolemItemTransforms.fishingRod(0.0F);
        Vector3f handle = idle.transformPosition(new Vector3f(-0.375F, -0.375F, 0.0F));
        Vector3f tip = idle.transformPosition(new Vector3f(0.375F, 0.375F, 0.0F));
        if (tip.z >= handle.z || tip.y >= handle.y) {
            throw new AssertionError("Rod must point forward and upward from the hand");
        }
        for (float arm : new float[]{-3.5F, -2.0F, -1.0F, 0.0F, 1.0F}) {
            Matrix4f legacy = new Matrix4f()
                .rotateX((float) Math.toRadians(5.0F + 90.0F * arm / (float) Math.PI))
                .translate(-0.26875F, 1.6F, -0.53F)
                .rotateY((float) -Math.PI / 2.0F)
                .rotateZ((float) -Math.PI / 6.0F)
                .scale(0.66F, -0.66F, 0.66F);
            for (float u : new float[]{0.0F, 0.25F, 1.0F}) {
                for (float v : new float[]{0.0F, 0.75F, 1.0F}) {
                    Vector3f expected = legacy.transformPosition(new Vector3f(1.0F - u, 1.0F - v, -1.0F / 32.0F));
                    Vector3f actual = GolemItemTransforms.fishingRod(arm).transformPosition(new Vector3f(u - 0.5F, 0.5F - v, 0.0F));
                    if (actual.distance(expected) > 0.00001F) {
                        throw new AssertionError("Modern rod lost the TC4 grip during arm animation");
                    }
                }
            }
        }
        System.out.println("Golem rod direction and 45 animated grip samples passed");
    }
}
