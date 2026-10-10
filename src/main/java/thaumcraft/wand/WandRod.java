package thaumcraft.wand;

import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;

public record WandRod(
    String tag,
    String textureTag,
    int capacity,
    ResourceLocation item,
    int craftCost,
    @Nullable RodUpdate onUpdate,
    boolean glowing,
    boolean staff,
    boolean runes
) {
    public ResourceLocation texture() {
        return Thaumcraft.id("textures/model/wand_rod_" + textureTag + ".png");
    }

    public String research() {
        return "ROD_" + tag;
    }
}
