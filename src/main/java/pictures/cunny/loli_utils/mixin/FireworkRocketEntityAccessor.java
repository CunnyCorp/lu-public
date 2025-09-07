package pictures.cunny.loli_utils.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FireworkRocketEntity.class)
public interface FireworkRocketEntityAccessor {
    @Accessor("attachedToEntity")
    LivingEntity getShooter();
}
