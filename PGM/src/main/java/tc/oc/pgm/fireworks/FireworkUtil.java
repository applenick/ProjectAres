package tc.oc.pgm.fireworks;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import com.google.common.base.Preconditions;

public class FireworkUtil {
	
	public static @Nonnull Firework spawnFirework(@Nonnull Location location, @Nonnull FireworkEffect effect,
			int power) {
		Preconditions.checkNotNull(location, "location");
		Preconditions.checkNotNull(effect, "firework effect");
		Preconditions.checkArgument(power >= 0, "power must be positive");

		FireworkMeta meta = (FireworkMeta) Bukkit.getItemFactory().getItemMeta(Material.FIREWORK);
		meta.setPower(power);
		meta.addEffect(effect);

		Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
		firework.setFireworkMeta(meta);

		return firework;
	}

	public static @Nonnull Location getOpenSpaceAbove(@Nonnull Location location) {
		Preconditions.checkNotNull(location, "location");

		Location result = location.clone();
		while (true) {
			Block block = result.getBlock();
			if (block == null || block.getType() == Material.AIR)
				break;

			result.setY(result.getY() + 1);
		}

		return result;
	}

	public static class FireworkEffects {

		private static final Random random = ThreadLocalRandom.current();

		public static FireworkEffect getRandomColor() {
			Builder builder = FireworkEffect.builder();
			builder.flicker(true);
			builder.trail(false);
			builder.with(Type.BURST);
			builder.withColor(Color.fromRGB((int) (Math.random() * 0xFFFFFF)));
			builder.withFade((random.nextBoolean() ? Color.WHITE : Color.BLACK));
			return builder.build();
		}
	}

	private FireworkUtil() {
	}
}