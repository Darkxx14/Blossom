package com.xyrisdev.blossom.listener;

import com.xyrisdev.blossom.RegenerationPlugin;
import com.xyrisdev.blossom.menu.RegionDefineMenu;
import com.xyrisdev.blossom.region.RegionManager;
import com.xyrisdev.blossom.region.model.PendingSelection;
import com.xyrisdev.blossom.region.model.Region;
import com.xyrisdev.blossom.region.model.RegionPointType;
import com.xyrisdev.library.text.XText;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

public class PlayerInteractListener implements Listener {

	@EventHandler
	public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();

        final Action action = event.getAction();

        if (!(action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR && player.isSneaking())) {
            return;
        }

		if (!RegionDefineMenu.getSelections().containsKey(uuid)) {
			return;
		}

		event.setCancelled(true);

		final PendingSelection selection = RegionDefineMenu.getSelections().remove(uuid);
		final Region region = selection.getRegion();
		final RegionPointType pointType = selection.getPointType();

		RegionManager.instance().point(region.getName(), pointType, player.getLocation());

		player.playSound(Sound.sound(Key.key("minecraft:entity.shulker.teleport"), Sound.Source.MASTER, 1.0f, 3.0f));
		player.sendRichMessage("<b><gradient:#8c75a5:#f46c90>Blossom</gradient></b> <gray>→ Set " + pointType.name().toLowerCase() + " to your location!</gray>");

		player.showTitle(Title.title(
				XText.text("<b><gradient:#8c75a5:#f46c90>Success</gradient></b>").small().component(),
				XText.text("<gray>Location has been set!</gray>").small().component(),
				Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(1))
		));

        RegenerationPlugin.instance().scheduler().runTaskLater(() ->
				RegionDefineMenu.define(region).open(player), 10L);
	}
}
