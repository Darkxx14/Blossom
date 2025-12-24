package com.xyrisdev.blossom.command.subcommand;

import com.xyrisdev.blossom.RegenerationPlugin;
import com.xyrisdev.blossom.util.command.RegisterableSubCommand;
import com.xyrisdev.library.command.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class ReloadSubCommand implements RegisterableSubCommand {

	@Override
	public @NotNull Consumer<Commands.Builder> build(@NotNull JavaPlugin plugin) {
		return builder -> builder.executes(context -> {
			final CommandSender sender = context.sender();

			sender.sendRichMessage(" ");
			sender.sendRichMessage("<b><gradient:#8c75a5:#f46c90>Blossom</gradient></b> <gray>(v" + plugin.getPluginMeta().getVersion() + ")</gray>");
			sender.sendRichMessage(" ");
			sender.sendRichMessage("<gray>→ The plugin's configuration has been reloaded.</gray>");
			sender.sendRichMessage(" ");

            RegenerationPlugin.instance().config().reload();
		});
	}
}