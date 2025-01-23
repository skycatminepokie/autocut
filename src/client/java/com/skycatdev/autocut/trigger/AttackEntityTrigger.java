package com.skycatdev.autocut.trigger;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.skycatdev.autocut.Autocut;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AttackEntityTrigger extends RecordingTrigger {

	public AttackEntityTrigger() {
		super(RecordingTriggerTypes.ATTACK_ENTITY);
	}


	public static class Type extends RecordingTriggerType<AttackEntityTrigger> implements AttackEntityCallback { // TODO: Config options
		public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "attack_entity");
		public static final MapCodec<AttackEntityTrigger> CODEC = MapCodec.unit(new AttackEntityTrigger());

		public Type() {
			super(ID, CODEC);
		}

		@Override
		public AttackEntityTrigger makeDefault() {
			return new AttackEntityTrigger();
		}

		@Override
		public ActionResult interact(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
			storeEvent(new JsonObject(), System.currentTimeMillis());
			return ActionResult.PASS;
		}
	}
}
