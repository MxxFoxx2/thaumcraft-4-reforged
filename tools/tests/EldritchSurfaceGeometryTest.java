import thaumcraft.client.geom.EldritchSurfaceGeometry;
import thaumcraft.client.geom.QuadSink;

public final class EldritchSurfaceGeometryTest {
    private static final int[][] OFFSETS = {{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}};
    private static final class Output implements QuadSink {
        int vertices;
        boolean sharedPlane;
        final int blockX;
        Output(int blockX) {
            this.blockX = blockX;
        }
        public void vertex(float x, float y, float z, float u, float v, int color, int light) {
            if (!Float.isFinite(u) || !Float.isFinite(v)) throw new AssertionError("Non-finite UV");
            if (vertices % 4 == 0) sharedPlane = true;
            sharedPlane &= x + blockX == 1;
            vertices++;
            if (vertices % 4 == 0 && sharedPlane) throw new AssertionError("Internal face was emitted");
        }
    }
    public static void main(String[] args) {
        int vertices = 0;
        for (int blockX = 0; blockX < 2; blockX++) {
            int mask = 0;
            for (int side = 0; side < 6; side++) {
                int[] step = OFFSETS[side];
                boolean neighborIsVoid = blockX + step[0] >= 0 && blockX + step[0] < 2 && step[1] == 0 && step[2] == 0;
                if (EldritchSurfaceGeometry.exposed(neighborIsVoid, false)) mask |= 1 << side;
            }
            Output out = new Output(blockX);
            EldritchSurfaceGeometry.faces(out, mask, 0.125F, 0, -1, 0xF000F0);
            vertices += out.vertices;
        }
        if (vertices != 40) throw new AssertionError("Two adjacent cubes must emit ten exterior faces");
        if (EldritchSurfaceGeometry.exposed(false, true)) throw new AssertionError("Solid neighbor must hide a face");
        System.out.println("Eldritch geometry passed: ten exterior faces, no internal face, finite UVs.");
    }
}
