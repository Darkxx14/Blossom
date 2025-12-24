package com.xyrisdev.blossom.region.storage;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xyrisdev.blossom.RegenerationPlugin;
import com.xyrisdev.blossom.region.model.Region;
import com.xyrisdev.blossom.util.adapter.LocationAdapter;
import com.xyrisdev.library.logger.XLogger;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class RegionStorage {

	private static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Location.class, new LocationAdapter())
			.setPrettyPrinting()
			.create();

	private final Path path;
	private final ExecutorService executor;

	public RegionStorage() {
        this.path = RegenerationPlugin.instance().getDataFolder().toPath().resolve("data");
		this.executor = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder()
				.setNameFormat("Blossom IO Thread %d")
				.build());

		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			XLogger.custom().fatal("Failed to create data folder");
			XLogger.stackTrace(e);
		}
	}

	public @NotNull Optional<Region> load(@NotNull String name) {
		final Path file = path.resolve(name + ".json");

		if (!Files.exists(file)) {
			return Optional.empty();
		}

		try {
			return Optional.ofNullable(GSON.fromJson(Files.readString(file), Region.class));
		} catch (IOException e) {
			XLogger.custom().fatal("Failed to load region: " + name);
			XLogger.stackTrace(e);
			return Optional.empty();
		}
	}

	public @NotNull List<Region> load() {
		try (Stream<Path> paths = Files.list(path)) {
			return paths.filter(p -> p.toString().endsWith(".json"))
					.map(p -> {
						try {
							return GSON.fromJson(Files.readString(p), Region.class);
						} catch (IOException e) {
							XLogger.custom().warn("Failed to load region from: " + p.getFileName());
							XLogger.stackTrace(e);
							return null;
						}
					})
					.filter(Objects::nonNull)
					.toList();
		} catch (IOException e) {
			XLogger.custom().fatal("Failed to list region files in: " + path);
			XLogger.stackTrace(e);
			return Collections.emptyList();
		}
	}

	public void save(@NotNull Region region) {
		executor.submit(() -> {
			final Path file = path.resolve(region.getName() + ".json");
			try {
				Files.writeString(file, GSON.toJson(region));
			} catch (IOException e) {
				XLogger.custom().fatal("Failed to save region: " + region.getName());
				XLogger.stackTrace(e);
			}
			return null;
		});
	}

	public void delete(@NotNull String name) {
		executor.submit(() -> {
			final Path file = path.resolve(name + ".json");
			try {
				Files.deleteIfExists(file);
			} catch (IOException e) {
				XLogger.custom().fatal("Failed to delete region: " + name);
				XLogger.stackTrace(e);
			}
			return null;
		});
	}

	public void shutdown() {
		executor.shutdown();
	}
}
