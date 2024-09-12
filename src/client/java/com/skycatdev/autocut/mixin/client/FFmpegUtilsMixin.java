package com.skycatdev.autocut.mixin.client;

import net.bramp.ffmpeg.FFmpegUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FFmpegUtils.class, remap = false)
public abstract class FFmpegUtilsMixin {
    @Inject(method = "fromTimecode(Ljava/lang/String;)J", at = @At(value = "INVOKE", target = "Lnet/bramp/ffmpeg/Preconditions;checkNotEmpty(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;", shift = At.Shift.AFTER), cancellable = true)
    private static void autocut$patchTimecodeNa(String time, CallbackInfoReturnable<Long> cir) {
        if (time.equals("N/A")) { // From patch by Euklios on ffmpeg-cli-wrapper, see https://github.com/bramp/ffmpeg-cli-wrapper/pull/315/commits/82516d0f4157f7b2fdaf456310db9018a3317777
            cir.setReturnValue(-1L);
        }
    }
}
