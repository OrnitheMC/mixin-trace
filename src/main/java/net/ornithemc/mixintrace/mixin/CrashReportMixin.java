package net.ornithemc.mixintrace.mixin;

import net.minecraft.util.crash.CrashReport;
import net.ornithemc.mixintrace.MixinTraceUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
abstract class CrashReportMixin {
	@Shadow private StackTraceElement[] stackTrace;

	@Inject(
		method = "addDetails",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/util/crash/CrashReport;systemDetails:Lnet/minecraft/util/crash/CrashReportCategory;",
			shift = At.Shift.BEFORE)
	)
	private void mixintrace$addMixinTraceDetails(StringBuilder sb, CallbackInfo ci) {
		MixinTraceUtil.addMixinInfoToCrashReport(sb, stackTrace);
	}
}
