package me.drexhd.itsmine.mixin;

import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/**
 * @author Indigo Amann
 */
@Mixin(LavaFluid.class)
public class LavaMixin {
    @Inject(method = "flow", at = @At("HEAD"), cancellable = true)
    private void dontFlow(WorldAccess world, BlockPos newPos, BlockState state, Direction direction, FluidState fluidState, CallbackInfo ci) {
        BlockPos oldPos = newPos.offset(direction.getOpposite());
        Claim oldClaim = ClaimManager.INSTANCE.getClaimAt(oldPos, world.getDimension());
        Claim newClaim = ClaimManager.INSTANCE.getClaimAt(newPos, world.getDimension());
        if (oldClaim != newClaim) {
            if (oldClaim == null) {
                if (!newClaim.flagManager.hasFlag("fluid_crosses_borders")) ci.cancel();
            } else if (newClaim == null) {
                if (!oldClaim.flagManager.hasFlag("fluid_crosses_borders")) ci.cancel();
            } else {
                if (!oldClaim.flagManager.hasFlag("fluid_crosses_borders") ||
                        !newClaim.flagManager.hasFlag("fluid_crosses_borders")) ci.cancel();
            }
        }
    }

    @Redirect(method = "onRandomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
    public boolean neverGonnaBurnMeUp(World world, BlockPos newPos, BlockState blockState_1, World world_1, BlockPos oldPos, FluidState fluidState_1, Random random_1) {
        Claim oldClaim = ClaimManager.INSTANCE.getClaimAt(oldPos, world.getDimension());
        Claim newClaim = ClaimManager.INSTANCE.getClaimAt(newPos, world.getDimension());
        if (oldClaim != newClaim) {
            if (oldClaim == null) {
                if (!newClaim.flagManager.hasFlag("fluid_crosses_borders")) return false;
            } else if (newClaim == null) {
                if (!oldClaim.flagManager.hasFlag("fluid_crosses_borders")) return false;
            } else {
                if (!oldClaim.flagManager.hasFlag("fluid_crosses_borders") ||
                        !newClaim.flagManager.hasFlag("fluid_crosses_borders")) return false;
            }
        }
        return world.setBlockState(newPos, blockState_1);
    }
}
