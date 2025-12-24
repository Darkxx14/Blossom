package com.xyrisdev.blossom.util;

import com.xyrisdev.blossom.RegenerationPlugin;
import com.xyrisdev.library.config.CachableConfiguration;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

@UtilityClass
public final class SoundUtil {

	public static void play(Player player, String path) {
        final CachableConfiguration config = RegenerationPlugin.instance().config();

		final Boolean enabled = config.get(path + ".enabled");
		if (enabled == null || !enabled) return;

		final Number volumeValue = config.get(path + ".volume");
		final Number pitchValue = config.get(path + ".pitch");
		final String key = config.get(path + ".key");
		final String source = config.get(path + ".source");

		if (volumeValue == null || pitchValue == null || key == null || source == null) {
			return;
		}

		final float volume = volumeValue.floatValue();
		final float pitch = pitchValue.floatValue();

		Sound sound = Sound.sound(
				Key.key(key),
				Sound.Source.valueOf(source),
				volume,
				pitch
		);

		player.playSound(sound);
	}
}
