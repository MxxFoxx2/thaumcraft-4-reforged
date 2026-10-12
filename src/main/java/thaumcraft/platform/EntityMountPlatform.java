package thaumcraft.platform;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.EventHooks;

public final class EntityMountPlatform {
    private EntityMountPlatform() {
    }

    public static boolean startRiding(Entity passenger, Entity vehicle, boolean force, boolean sendEventAndTriggers) {
        if (passenger == vehicle) {
            return false;
        }
        if (vehicle.getType().canSerialize() || !(vehicle instanceof Player) || !force || passenger.level().isClientSide()) {
            return passenger.startRiding(vehicle, force, sendEventAndTriggers);
        }
        if (!vehicle.couldAcceptPassenger()) {
            return false;
        }
        for (Entity current = vehicle; current.getVehicle() != null; current = current.getVehicle()) {
            if (current.getVehicle() == passenger) {
                return false;
            }
        }
        if (!EventHooks.canMountEntity(passenger, vehicle, true)) {
            return false;
        }
        if (passenger.isPassenger()) {
            passenger.stopRiding();
        }
        passenger.setPose(Pose.STANDING);
        passenger.vehicle = vehicle;
        vehicle.addPassenger(passenger);
        if (sendEventAndTriggers) {
            passenger.level().gameEvent(passenger, GameEvent.ENTITY_MOUNT, vehicle.position());
        }
        return true;
    }
}
