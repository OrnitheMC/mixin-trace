package net.ornithemc.mixintrace;

import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class MixinTraceUtil {
	/**
	 * Appends mixin information about classes in the stacktrace to the crash report StringBuilder.
	 * @param sb The crash report StringBuilder.
	 * @param stackTrace the crash report's stackTrace.
	 */
	public static void addMixinInfoToCrashReport(StringBuilder sb, StackTraceElement[] stackTrace) {
		if (stackTrace != null && stackTrace.length > 0) {
			sb.append("-- Mixins affecting classes in stacktrace --\n");

			// Recursion exists, so stackTrace may have multiple times the same class
			Set<String> classNames = new HashSet<>();
			for (StackTraceElement ste : stackTrace) {
				classNames.add(ste.getClassName());
			}
			boolean found = false;
			for (String className : classNames) {
				Set<IMixinInfo> infoSet = getMixinInfoFromClass(className);
				if (infoSet == null) {
					sb.append("Failed to get Mixin metadata\n\n");
					return;
				}
				if (infoSet.isEmpty()) continue;

				found = true;
				sb.append(className).append(":");
				infoSet.forEach(info -> sb.append(stringifyMixinInfo(info)));
				sb.append("\n");
			}

			if (!found) sb.append("None found\n");
			sb.append("\n");
		}
	}

	/**
	 * Returns a String containing relevant information for the provided mixin information.
	 * @param info The mixin information provided.
	 * @return The string containing relevant bits of the mixin information.
	 */
	private static String stringifyMixinInfo(IMixinInfo info) {
		return "\n\t"
			+ info.getClassName()
			+ " ("
			+ info.getConfig().getName()
			+ ")";
	}

	/**
	 * Fetching mixin information for a given class name. Returns null in case the fetching failed, you need to handle that case.
	 * @param className The name of a class. Example: "com.example.SomeClass".
	 * @return null in case fetching mixin info from class name failed, and the set of mixin info otherwise.
	 */
	@SuppressWarnings("unchecked")
	private static Set<IMixinInfo> getMixinInfoFromClass(String className) {
		ClassInfo classInfo = ClassInfo.forName(className);
		if (classInfo == null) return new HashSet<>();

		Set<IMixinInfo> infoSet;
		try {
			// There is a getter for mixins, but it is protected, so we have to use reflection to get its content
			// There is also getAppliedMixins but for some reasons it's always empty even when the mixins are very much
			// applied, so reflection it is
			Field mixinsField = ClassInfo.class.getDeclaredField("mixins");
			mixinsField.setAccessible(true);
			infoSet = (Set<IMixinInfo>) mixinsField.get(classInfo);
			return infoSet;
		} catch (Exception e) {
			return null;

		}
	}
}
