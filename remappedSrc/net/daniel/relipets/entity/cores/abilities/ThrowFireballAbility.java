package net.daniel.relipets.entity.cores.abilities;

import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.math.Vec3d;
import net.tslat.smartbrainlib.util.BrainUtils;

public class ThrowFireballAbility implements CoreAbility {

    @Override
    public void start(CoreAbilityStats stats, BaseCore core) {
        LivingEntity target = BrainUtils.getMemory(core, MemoryModuleType.ATTACK_TARGET);

        if(target != null){

            Vec3d direction = target.getPos().subtract(core.getPos()).normalize();

            AbstractFireballEntity fireball = new SmallFireballEntity(
                    core.getWorld(),
                    core,
                    direction.x,
                    direction.y,
                    direction.z
            );

            fireball.setPosition(core.getX(), core.getEyeY() - 0.1, core.getZ());

            //fireball.setVelocity(lookVec.multiply(0.5));

            core.getWorld().spawnEntity(fireball);
        }
    }

    @Override
    public int getCooldown(BaseCore core) {
        return 2;
    }
}
