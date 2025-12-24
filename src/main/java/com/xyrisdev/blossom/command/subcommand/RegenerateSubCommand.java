package com.xyrisdev.blossom.command.subcommand;

import com.xyrisdev.blossom.RegenerationPlugin;
import com.xyrisdev.blossom.command.argument.RegionArgumentType;
import com.xyrisdev.blossom.region.RegionManager;
import com.xyrisdev.blossom.region.model.Region;
import com.xyrisdev.blossom.util.AsyncWorldEditUtil;
import com.xyrisdev.blossom.util.SoundUtil;
import com.xyrisdev.blossom.util.command.RegisterableSubCommand;
import com.xyrisdev.library.command.Commands;
import com.xyrisdev.library.location.XLocation;
import com.xyrisdev.library.message.XMessageBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@SuppressWarnings({"UnstableApiUsage", "DataFlowIssue"})
public class RegenerateSubCommand implements RegisterableSubCommand {

	@Override
	public @NotNull Consumer<Commands.Builder> build(@NotNull JavaPlugin plugin) {
		return builder -> builder
				.argument(new RegionArgumentType().region("region"))
				.executes(context -> {
					final CommandSender sender = context.sender();
					final Region region = context.resolve("region");

					if (region == null || !region.valid() && !region.schematic()) {
						sender.sendRichMessage("<b><gradient:#8c75a5:#f46c90>Blossom</gradient></b> <gray>→ Region does not exist. Please provide a valid region.</gray>");
						return;
					}

					// Regenerate the region
					AsyncWorldEditUtil.regenerate(region.getName());

					XLocation.players(region.getMin(), region.getMax()).forEach(player -> {
						// Teleport all the player's to the top if enabled
                        if (RegenerationPlugin.instance().getConfig().getBoolean("regeneration.teleport_to_top")) {
							XLocation.top(region.getMin(), region.getMax(), region.getCenter());
						}

						// Broadcast the regeneration message if enabled
                        if (RegenerationPlugin.instance().getConfig().getBoolean("broadcast.regenerated")) {
                            XMessageBuilder.of(player, RegenerationPlugin.instance().getConfig())
									.id("regenerated")
									.placeholders("name", RegionManager.instance().region(region.getName()).getDisplayName())
									.send();

							SoundUtil.play(player, "sounds.regeneration");
						}
					});

					sender.sendRichMessage(" ");
					sender.sendRichMessage("<b><gradient:#8c75a5:#f46c90>Blossom</gradient></b> <gray>(v" + plugin.getPluginMeta().getVersion() + ")</gray>");
					sender.sendRichMessage(" ");
					context.senderAsPlayer().sendRichMessage("<gray>→ The region has been regenerated.</gray>");
					sender.sendRichMessage(" ");
				});
	}
}