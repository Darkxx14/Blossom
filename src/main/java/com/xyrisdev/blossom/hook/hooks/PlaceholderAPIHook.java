package com.xyrisdev.blossom.hook.hooks;

import com.xyrisdev.blossom.RegenerationPlugin;
import com.xyrisdev.blossom.hook.BlossomHook;
import com.xyrisdev.blossom.region.task.RegenerationTaskScheduler;
import com.xyrisdev.blossom.util.time.TimeFormatter;
import com.xyrisdev.blossom.util.time.format.TimeFormat;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class PlaceholderAPIHook extends PlaceholderExpansion {

	public static BlossomHook of() {
		return BlossomHook.builder()
				.plugin("PlaceholderAPI")
				.execute(plugin -> new PlaceholderAPIHook().register())
				.success("Successfully hooked into PlaceholderAPI!")
				.failure("PlaceholderAPI not found. You will not be able to use any placeholders.")
				.log(true)
				.build();
	}

	@Override
	public @NotNull String getIdentifier() {
        return RegenerationPlugin.instance().getPluginMeta().getName().toLowerCase();
	}

	@Override
	public @NotNull String getAuthor() {
        return RegenerationPlugin.instance().getPluginMeta().getAuthors().stream().findFirst().toString();
	}

	@Override
	public @NotNull String getVersion() {
        return RegenerationPlugin.instance().getPluginMeta().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, @NotNull String identifier) {
		if (identifier.startsWith("interval_")) {
			final String regionName = identifier.substring("interval_".length());
			final long interval = RegenerationTaskScheduler.left(regionName);

            return TimeFormatter.format(interval, TimeFormat.of(RegenerationPlugin.instance().config().getString("placeholders.interval.format")));
		}

		return "";
	}
}