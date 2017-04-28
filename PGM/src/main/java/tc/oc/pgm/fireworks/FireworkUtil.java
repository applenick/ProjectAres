package tc.oc.pgm.fireworks;

import java.util.Random;

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
    public static @Nonnull Firework spawnFirework(@Nonnull Location location, @Nonnull FireworkEffect effect, int power) {
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
        while(true) {
            Block block = result.getBlock();
            if(block == null || block.getType() == Material.AIR) break;

            result.setY(result.getY() + 1);
        }

        return result;
    }
    
    
    
    public static class FireworkEffects{

    	public static FireworkEffect getRandomColor(){
    		Builder build = FireworkEffect.builder();
    		build.flicker(true);
    		build.trail(false);
    		build.with(Type.BURST);

    		Random rand = new Random();

    		build.withColor(getColor(rand.nextInt(11) + 1));
    		build.withFade((rand.nextBoolean() ? Color.WHITE : Color.BLACK));

    		return build.build();
    	}

    	public static Color getColor(int color){
    		switch(color){
    		case 1:
    			return Color.RED;
    		case 2:
    			return Color.WHITE;
    		case 3:
    			return Color.BLUE;
    		case 4:
    			return Color.YELLOW;
    		case 5:
    			return Color.AQUA;
    		case 6:
    			return Color.BLACK;
    		case 7:
    			return Color.PURPLE;
    		case 8:
    			return Color.FUCHSIA;
    		case 9:
    			return Color.ORANGE;
    		case 10:
    			return Color.MAROON;
    		case 11:
    			return Color.LIME;
    		case 12:
    			return Color.GRAY;
    		default:
    			return Color.WHITE;
    		}
    	}
    }

    private FireworkUtil() { }
}
