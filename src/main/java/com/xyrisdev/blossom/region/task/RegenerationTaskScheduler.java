package com.xyrisdev.blossom.region.task;

import com.xyrisdev.blossom.RegenerationPlugin;
import com.xyrisdev.blossom.region.RegionManager;
import com.xyrisdev.blossom.region.model.Region;
import com.xyrisdev.blossom.util.AsyncWorldEditUtil;
import com.xyrisdev.blossom.util.SoundUtil;
import com.xyrisdev.library.location.XLocation;
import com.xyrisdev.library.message.XMessageBuilder;
import com.xyrisdev.library.scheduler.XRunnable;
import com.xyrisdev.library.scheduler.scheduling.tasks.MyScheduledTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RegenerationTaskScheduler {

	private static final Map<String, MyScheduledTask> tasks = new ConcurrentHashMap<>();
	private static final Map<String, List<MyScheduledTask>> warnings = new ConcurrentHashMap<>();
	private static final Map<String, Long> times = new ConcurrentHashMap<>();

	public static void start() {
		RegionManager.instance().regions().forEach(region -> start(region.getName()));
	}

	public static void start(@NotNull String name) {
		final Region region = RegionManager.instance().region(name);
		if (region == null || region.getInterval() <= 0 || region.getMin() == null || region.getMax() == null || region.getCenter() == null) {
			return;
		}

		stop(name);

		final long intervalMs = region.getInterval();
		final long intervalTicks = intervalMs / 50;

		final MyScheduledTask task = new XRunnable() {
			@Override
			public void run() {
				// Regenerate the region
				AsyncWorldEditUtil.regenerate(name);

				times.put(name, System.currentTimeMillis());

				XLocation.players(region.getMin(), region.getMax()).forEach(player -> {
					// Teleport all the player's to the top if enabled
                    if (RegenerationPlugin.instance().getConfig().getBoolean("regeneration.teleport_to_top")) {
						XLocation.top(region.getMin(), region.getMax(), region.getCenter());
					}

					// Broadcast the regeneration message if enabled
                    if (RegenerationPlugin.instance().getConfig().getBoolean("broadcast.regenerated")) {
                        XMessageBuilder.of(player, RegenerationPlugin.instance().getConfig())
								.id("regenerated")
								.placeholders("name", RegionManager.instance().region(name).getDisplayName())
								.send();

						SoundUtil.play(player, "sounds.regeneration");
					}
				});

                if (RegenerationPlugin.instance().getConfig().getBoolean("broadcast.regeneration_warning")) {
					warn(name, intervalMs, region);
				}
			}
        }.runTaskTimerAsynchronously(RegenerationPlugin.instance(), intervalTicks, intervalTicks);

		times.put(name, System.currentTimeMillis());
		tasks.put(name, task);

        if (RegenerationPlugin.instance().getConfig().getBoolean("broadcast.regeneration_warning")) {
			warn(name, intervalMs, region);
		}
	}

	private static void warn(@NotNull String name, long intervalMs, @NotNull Region region) {
		Optional.ofNullable(warnings.remove(name)).ifPresent(warnings -> warnings.forEach(MyScheduledTask::cancel));

		if (region.getMin() == null || region.getMax() == null || region.getCenter() == null) {
			return;
		}

		List<MyScheduledTask> newWarnings = new ArrayList<>();
		long intervalSec = intervalMs / 1000;
		List<Long> warningTimes;

		if (intervalSec >= 3600) warningTimes = List.of(1800L, 600L, 300L, 60L, 10L);
		else if (intervalSec >= 1800) warningTimes = List.of(900L, 300L, 10L);
		else if (intervalSec >= 600) warningTimes = List.of(300L, 60L, 10L);
		else if (intervalSec >= 300) warningTimes = List.of(120L, 60L, 10L);
		else warningTimes = List.of(intervalSec / 2, 10L);

		for (long time : warningTimes) {
			long warningMs = time * 1000;
			if (warningMs >= intervalMs) continue;

			newWarnings.add(new XRunnable() {
				@Override
				public void run() {
					long seconds = warningMs / 1000;
					String timeString = (seconds >= 3600) ? (seconds / 3600) + "h" :
							(seconds >= 60) ? (seconds / 60) + "m" : seconds + "s";

					XLocation.players(region.getMin(), region.getMax()).forEach(player -> {
                        XMessageBuilder.of(player, RegenerationPlugin.instance().getConfig())
								.id("regeneration_warning")
								.placeholders("name", RegionManager.instance().region(name).getDisplayName(), "time", timeString)
								.send();

						SoundUtil.play(player, "sounds.regeneration_warning");
					});
				}
            }.runTaskLaterAsynchronously(RegenerationPlugin.instance(), (intervalMs - warningMs) / 50));
		}

		warnings.put(name, newWarnings);
	}

	public static void restart(@NotNull String name) {
		stop(name);
		start(name);
	}

	public static void stop(@NotNull String name) {
		Optional.ofNullable(tasks.remove(name)).ifPresent(MyScheduledTask::cancel);
		Optional.ofNullable(warnings.remove(name)).ifPresent(warnings -> warnings.forEach(MyScheduledTask::cancel));
		times.remove(name);
	}

	public static void stop() {
		tasks.keySet().forEach(RegenerationTaskScheduler::stop);
	}

	public static boolean running(@NotNull String name) {
		return tasks.containsKey(name) && tasks.get(name) != null;
	}

	public static long left(@NotNull String name) {
		final Region region = RegionManager.instance().region(name);
		final Long startTime = times.get(name);

		if (region == null || startTime == null) {
			return -1;
		}

		long elapsed = System.currentTimeMillis() - startTime;
		return Math.max(0, region.getInterval() - elapsed);
	}

	@Contract(pure = true)
	@NotNull
	public static @UnmodifiableView Map<String, MyScheduledTask> tasks() {
		return Collections.unmodifiableMap(tasks);
	}
}