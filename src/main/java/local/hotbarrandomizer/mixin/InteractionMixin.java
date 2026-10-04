package local.hotbarrandomizer.mixin;

import local.hotbarrandomizer.Randomizer;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class InteractionMixin {
    @Inject(method = "interactItem", at = @At("HEAD"))
    private void randomizer$beginItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        Randomizer.begin(hand);
    }
    @Inject(method = "interactItem", at = @At("RETURN"))
    private void randomizer$finishItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        Randomizer.finish();
    }
    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void randomizer$begin(ClientPlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        Randomizer.begin(hand);
    }
    @Inject(method = "interactBlock", at = @At("RETURN"))
    private void randomizer$finish(ClientPlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        Randomizer.finish();
    }
}
