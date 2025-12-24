package com.xyrisdev.blossom.menu;

import com.xyrisdev.blossom.RegenerationPlugin;
import com.xyrisdev.blossom.exception.DuplicateRegionException;
import com.xyrisdev.blossom.region.RegionManager;
import com.xyrisdev.blossom.region.model.Region;
import com.xyrisdev.blossom.region.task.RegenerationTaskScheduler;
import com.xyrisdev.blossom.util.AnvilMenu;
import com.xyrisdev.library.item.ItemBuilder;
import com.xyrisdev.library.menu.ConfirmationMenu;
import com.xyrisdev.library.menu.Menu;
import com.xyrisdev.library.text.XText;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RegionManageMenu {

	public static @NotNull Menu region(@NotNull Region region) {
		final Menu menu = new Menu(27, Component.text("Managing > " + region.getName()));

		ItemStack nameStack = new ItemBuilder(Material.NAME_TAG)
				.name(XText.text("<gradient:#8c75a5:#f46c90>Name</gradient>").small().component())
				.lore(
						XText.text("<gray>Click to edit the name,</gray>").component(),
						XText.text("<gray>the current name is " + region.getName() + ".</gray>").component()
				)
				.build();

		menu.item(10, nameStack, event -> {
			Player player = (Player) event.getWhoClicked();
            AnvilMenu.of(RegenerationPlugin.instance())
					.title("Renaming " + region.getName())
					.text("Enter new name")
					.click((slot, state) -> {
						if (slot != AnvilGUI.Slot.OUTPUT) {
							return Collections.emptyList();
						}

						final String newName = state.getText();

						try {
							RegionManager.instance().rename(region.getName(), newName);
						} catch (DuplicateRegionException e) {
							AnvilGUI.ResponseAction.close();
							player.sendRichMessage("<b><gradient:#8c75a5:#f46c90>Blossom</gradient></b> <gray>→ Region <gradient:#8c75a5:#f46c90>" + newName + "</gradient> already exists.</gray>");
						}

						return List.of(AnvilGUI.ResponseAction.close(),
								AnvilGUI.ResponseAction.run(() -> region(RegionManager.instance().region(newName)).open(player)));
					})
					.preventClose()
					.open(player);
		});

		ItemStack displayStack = new ItemBuilder(Material.NAME_TAG)
				.name(XText.text("<gradient:#8c75a5:#f46c90>Display Name</gradient>").small().component())
				.lore(
						XText.text("<gray>Click to edit the display name,</gray>").component(),
						XText.text("<gray>the current display name is " + region.getDisplayName() + ".</gray>").component()
				)
				.build();

		menu.item(11, displayStack, event -> {
			Player player = (Player) event.getWhoClicked();
            AnvilMenu.of(RegenerationPlugin.instance())
					.title("Editing display name of " + region.getName())
					.text("Enter new display name")
					.click((slot, state) -> {
						if (slot != AnvilGUI.Slot.OUTPUT) {
							return Collections.emptyList();
						}

						String newDisplayName = state.getText();
						RegionManager.instance().display(region.getName(), newDisplayName);
						return List.of(AnvilGUI.ResponseAction.close(),
								AnvilGUI.ResponseAction.run(() -> region(RegionManager.instance().region(region.getName())).open(player)));
					})
					.preventClose()
					.open(player);
		});

		ItemStack defineStack = new ItemBuilder(Material.ENDER_EYE)
				.name(XText.text("<gradient:#8c75a5:#f46c90>Define</gradient>".toLowerCase()).small().component())
				.lore(
						XText.text("<gray>Click to define the region's positions,</gray>").component(),
						XText.text("<gray>such as min, max, and center.</gray>").component()
				)
				.build();

		menu.item(13, defineStack , event -> RegionDefineMenu.define(region).open((Player) event.getWhoClicked()));

		ItemStack validSchemStack = new ItemBuilder(Material.CHEST)
				.name(XText.text("<gradient:#8c75a5:#f46c90>Schematic</gradient>".toLowerCase()).small().component())
				.lore(
						XText.text("<gray>Click to generate the region's schematic,</gray>").component(),
						XText.text("<gray>to allow Blossom to regenerate the region.</gray>").component()
				)
				.build();

		ItemStack invalidSchemStack = new ItemBuilder(Material.CHEST)
				.name(XText.text("<gradient:#8c75a5:#f46c90>Schematic</gradient>".toLowerCase()).small().component())
				.lore(
						XText.text("<gray>Click to generate the region's schematic,</gray>").component(),
						XText.text("<gray>to allow Blossom to regenerate the region.</gray>").component(),
						XText.text("<gray>Please define the region's positions.</gray>").component()
				)
				.build();
		if (region.valid()) {
			menu.item(15, validSchemStack, event -> RegionSchematicFormatMenu.format(region).open((Player) event.getWhoClicked()));
		} else {
			menu.item(15, invalidSchemStack);
		}

		ItemStack validRunningStack = new ItemBuilder(Material.REDSTONE)
				.name(XText.text("<gradient:#8c75a5:#f46c90>Task</gradient>".toLowerCase()).small().component())
				.lore(
						XText.text("<gray>Click to toggle the region's regeneration task,</gray>").component(),
						XText.text("<gray>The task is currently " + (RegenerationTaskScheduler.running(region.getName()) ? "<green>Running</green>." : "<red>Not Running</red>.")).component()
				)
				.build();

		ItemStack invalidRunningStack = new ItemBuilder(Material.REDSTONE)
				.name(XText.text("<gradient:#8c75a5:#f46c90>Task</gradient>".toLowerCase()).small().component())
				.lore(
						XText.text("<gray>Click to toggle the region's regeneration task,</gray>").component(),
						XText.text("<gray>Please define the region's positions,</gray>").component(),
						XText.text("<gray>and generate the region's schematic.</gray>").component()
				)
				.build();

		if (region.valid() && region.schematic()) {
			menu.item(16, validRunningStack, event -> {
				if (RegenerationTaskScheduler.running(region.getName())) {
					RegenerationTaskScheduler.stop(region.getName());
				} else {
					RegenerationTaskScheduler.start(region.getName());
				}

				ItemStack updatedItem = Objects.requireNonNull(menu.getInventory().getItem(16));
				updatedItem.lore(List.of(
						XText.text("<gray>Click to toggle the region's regeneration task.</gray>").component(),
						XText.text("<gray>The task is currently " + (RegenerationTaskScheduler.running(region.getName()) ?
										"<green>Running</green>" : "<red>Not Running</red>") + ".</gray>").component()
				));

				menu.item(16, updatedItem);
			});
		} else {
			menu.item(16, invalidRunningStack);
		}

		ItemStack deleteStack = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
				.name(XText.text("<gradient:#8c75a5:#f46c90>Delete</gradient>".toLowerCase()).small().component())
				.lore(XText.text("<gray>Click to delete the region.</gray>").component())
				.build();

		menu.item(26, deleteStack, event -> new ConfirmationMenu.Builder()
				.title(Component.text("Are you sure?"))
				.size(9)
				.slots(3, 5)
				.yes(Material.SLIME_BALL, XText.text("<green>Confirm</green>".toLowerCase()).small().component())
				.no(Material.FIREWORK_STAR, XText.text("<red>Cancel</red>".toLowerCase()).small().component())
				.execute(confirmed -> {
					if (confirmed) {
						RegionManager.instance().delete(region.getName());
						event.getWhoClicked().sendRichMessage("<b><gradient:#8c75a5:#f46c90>Blossom</gradient></b> <gray>→ The region has been deleted.</gray>");
						event.getWhoClicked().closeInventory();
					} else region(region).open((Player) event.getWhoClicked());
				})
				.build()
				.open((Player) event.getWhoClicked())
		);

		ItemStack backStack = new ItemBuilder(Material.ARROW)
				.name(XText.text("<gradient:#8c75a5:#f46c90>Back</gradient>".toLowerCase()).small().component())
				.build();

		menu.item(22, backStack, event -> event.getWhoClicked().closeInventory());

		return menu;
	}
}
