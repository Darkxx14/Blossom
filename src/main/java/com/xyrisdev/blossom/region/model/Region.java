package com.xyrisdev.blossom.region.model;

import com.xyrisdev.blossom.RegenerationPlugin;
import com.xyrisdev.blossom.util.AsyncWorldEditUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class Region {

	private final @NotNull String name;
	private @NotNull String displayName;
	private @NotNull Long interval;
	private @Nullable Location center;
	private @Nullable Location min;
	private @Nullable Location max;

	public boolean valid() {
		return center != null && min != null && max != null;
	}

	public boolean schematic() {
		Path schematicPath = Paths.get(
                RegenerationPlugin.instance().getDataFolder().getAbsolutePath(),
				"schematics",
				name + AsyncWorldEditUtil.FILE_EXTENSION
		);
		return Files.exists(schematicPath);
	}

	public void renameSchematic(@NotNull String newName) {
		final Path old = Paths.get(
                RegenerationPlugin.instance().getDataFolder().getAbsolutePath(),
				"schematics",
				name + AsyncWorldEditUtil.FILE_EXTENSION
		);

		Path newPath = Paths.get(
                RegenerationPlugin.instance().getDataFolder().getAbsolutePath(),
				"schematics",
				newName + AsyncWorldEditUtil.FILE_EXTENSION
		);

		if (!Files.exists(old)) {
			return;
		}

		try {
			Files.move(old, newPath);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to rename schematic: " + old + " → " + newPath, e);
		}
	}

	public boolean contains(@NotNull Location loc) {
		if (min == null || max == null) {
			return false;
		}

		double x = loc.getX(), y = loc.getY(), z = loc.getZ();
		double minX = Math.min(min.getX(), max.getX()), maxX = Math.max(min.getX(), max.getX());
		double minY = Math.min(min.getY(), max.getY()), maxY = Math.max(min.getY(), max.getY());
		double minZ = Math.min(min.getZ(), max.getZ()), maxZ = Math.max(min.getZ(), max.getZ());

		return x >= minX && x <= maxX
				&& y >= minY && y <= maxY
				&& z >= minZ && z <= maxZ;
	}
}
