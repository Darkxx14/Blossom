package com.xyrisdev.blossom.util;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.xyrisdev.blossom.RegenerationPlugin;
import com.xyrisdev.blossom.region.RegionManager;
import com.xyrisdev.blossom.region.model.Region;
import com.xyrisdev.library.cachable.Cachable;
import com.xyrisdev.library.logger.XLogger;
import com.xyrisdev.library.scheduler.XScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class AsyncWorldEditUtil {

	private static final Cachable<String, Clipboard> cache = Cachable.of();
	public static final String FILE_EXTENSION = ".schem"; // .rizzomatic would work too

    public static void schematic(@NotNull String name, BuiltInClipboardFormat clipboardFormat) {
        final Region region = RegionManager.instance().region(name);

        if (region == null || region.getMin() == null || region.getMax() == null) {
            return;
        }

        execute(region, () -> {
            final long start = System.currentTimeMillis();

            final Path path = RegenerationPlugin.instance().getDataFolder().toPath()
                    .resolve("schematics")
                    .resolve(region.getName() + FILE_EXTENSION);

            try {
                final Location min = region.getMin(), max = region.getMax();
                final com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(min.getWorld());

                final CuboidRegion cuboid = new CuboidRegion(
                        BukkitAdapter.adapt(min).toBlockPoint(),
                        BukkitAdapter.adapt(max).toBlockPoint()
                );

                final BlockArrayClipboard clipboard = new BlockArrayClipboard(cuboid);
                clipboard.setOrigin(BukkitAdapter.adapt(min).toBlockPoint());

                final ForwardExtentCopy extentCopy = new ForwardExtentCopy(world, cuboid, clipboard, cuboid.getMinimumPoint());
                extentCopy.setCopyingBiomes(true);
                extentCopy.setCopyingEntities(false);

                FaweAPI.getTaskManager().async(() -> {
                    try {
                        Operations.complete(extentCopy);

                        Files.createDirectories(path.getParent());
                        try (ClipboardWriter writer = clipboardFormat.getWriter(Files.newOutputStream(path))) {
                            writer.write(clipboard);
                        }

                        final long duration = System.currentTimeMillis() - start;
                        RegenerationPlugin.instance().debug("Saved schematic for region</gray> <color:#f29db4>" + name + "</color> <gray>in</gray> <color:#f29db4>" + duration + "</color> <gray>ms.</gray>");
                    } catch (Exception e) {
                        XLogger.custom().fatal("Failed to save schematic: " + region.getName());
                        XLogger.stackTrace(e);
                    }
                });
            } catch (Exception e) {
                XLogger.custom().fatal("Failed to save schematic: " + region.getName());
                XLogger.stackTrace(e);
            }
        });
    }

	public static void regenerate(@NotNull String name) {
		FaweAPI.getTaskManager().async(() -> {
			final long start = System.currentTimeMillis();

			final Region region = RegionManager.instance().region(name);

			if (region == null || region.getMin() == null || region.getMax() == null || region.getCenter() == null) {
				return;
			}

			final Location min = region.getMin();
			final com.sk89q.worldedit.world.World world = FaweAPI.getWorld(min.getWorld().getName());
            final Path path = RegenerationPlugin.instance().getDataFolder().toPath()
					.resolve("schematics")
					.resolve(region.getName() + FILE_EXTENSION);

			final Clipboard clipboard = cache.ensure(name, key -> {
				try (ClipboardReader reader = Objects.requireNonNull(ClipboardFormats.findByFile(path.toFile()))
                        .getReader(Files.newInputStream(path))) {
					return reader.read();
				} catch (IOException e) {
					XLogger.custom().fatal("Failed to load clipboard: " + key);
					XLogger.stackTrace(e);
					return null;
				}
			});

			if (clipboard == null) {
				return;
			}

			try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
				Operations.complete(new ClipboardHolder(clipboard).createPaste(editSession)
						.ignoreAirBlocks(false)
						.to(BukkitAdapter.adapt(min).toBlockPoint())
						.build());

				// Clear all the entities
                if (RegenerationPlugin.instance().config().get("regeneration.clear_entities", true))
                    RegenerationPlugin.instance().scheduler().runTask(region.getCenter(), () ->
                            region.getCenter().getWorld()
                                    .getNearbyEntities(BoundingBox.of(region.getMin(), region.getMax())).stream()
                                    .filter(entity -> !(entity instanceof Player))
                                    .forEach(Entity::remove));
				final long duration = System.currentTimeMillis() - start;
                RegenerationPlugin.instance().debug("Regenerated schematic for region</gray> <color:#f29db4>" + name + "</color> <gray>in</gray> <color:#f29db4>" + duration + "</color> <gray>ms.</gray>");
			} catch (Exception e) {
				XLogger.custom().fatal("Failed to regenerate schematic: " + name);
				XLogger.stackTrace(e);
			}
		});
	}

    public static void execute(@NotNull Region region, @NotNull Runnable task) {
        if (XScheduler.isFolia) {
            RegenerationPlugin.instance().scheduler().runTask(region.getCenter(), task);
        } else {
            FaweAPI.getTaskManager().async(task);
        }
    }

}