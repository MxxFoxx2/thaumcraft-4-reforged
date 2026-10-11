package thaumcraft.item.tool;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import thaumcraft.Thaumcraft;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class FollowingItems {
    private static final int START_AGE = 20;
    private static final double ATTRACT_RANGE = 2.0;
    private static final double ARRIVE_DISTANCE = 0.5;
    private static final Map<ItemEntity, Follow> FOLLOWING = new WeakHashMap<>();

    private static final class Follow {
        private final Entity target;
        private int age = START_AGE;

        private Follow(Entity target) {
            this.target = target;
        }
    }

    private FollowingItems() {
    }

    public static boolean isFollowing(ItemEntity item) {
        return FOLLOWING.containsKey(item);
    }

    public static void follow(ItemEntity item, Entity target) {
        FOLLOWING.put(item, new Follow(target));
        item.setNoGravity(true);
        item.noPhysics = true;
    }

    public static void attractNear(ServerLevel level, BlockPos pos, Entity target) {
        AABB area = new AABB(pos).inflate(ATTRACT_RANGE - 0.5);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            if (!item.isRemoved() && item.tickCount == 0 && !isFollowing(item)) {
                follow(item, target);
            }
        }
    }

    @SubscribeEvent
    static void onItemTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ItemEntity item) || FOLLOWING.isEmpty()) {
            return;
        }
        Follow follow = FOLLOWING.get(item);
        if (follow == null) {
            return;
        }
        Entity target = follow.target;
        if (target == null || target.isRemoved() || target.level() != item.level()) {
            release(item);
            return;
        }
        Vec3 goal = new Vec3(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0F, target.getZ());
        Vec3 offset = goal.subtract(item.position());
        if (follow.age > 1) {
            follow.age--;
        }
        double distance = offset.length();
        if (distance > ARRIVE_DISTANCE) {
            item.setDeltaMovement(offset.scale(1.0 / (distance * follow.age)));
        } else {
            item.setDeltaMovement(item.getDeltaMovement().scale(0.1));
            release(item);
        }
    }

    private static void release(ItemEntity item) {
        FOLLOWING.remove(item);
        item.setNoGravity(false);
        item.noPhysics = false;
    }
}
