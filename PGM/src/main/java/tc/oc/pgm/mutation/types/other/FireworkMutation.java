package tc.oc.pgm.mutation.types.other;

import java.util.Random;

import org.apache.commons.lang.math.Fraction;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import com.applenick.Lightning.utils.ThunderUtils;

import tc.oc.commons.core.random.RandomUtils;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.fireworks.FireworkUtil;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.mutation.types.MutationModule;

/************************************************
			 Created By AppleNick
Copyright © 2017 , AppleNick, All rights reserved.
			http://applenick.com
 *************************************************/
public class FireworkMutation extends MutationModule.Impl {
	
	/*
	 * FireworkMutation
	 * Applies a firework effect to all projectiles when landing & a chance for effect on death/damage
	 * 
	 * 1/4 Chance of firework on death
	 * 1/5 Chance of firework on damage
	 * 
	 */
	
	public static Fraction DEATH_CHANCE = Fraction.ONE_QUARTER;
	public static Fraction DAMAGE_CHANCE = Fraction.ONE_FIFTH;

	public FireworkMutation(Match match) {
		super(match);
	}
	
		
	private void playRandomFirework(Location loc){
		ThunderUtils.instantFirework(loc, getRandomColor());
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event){
		playRandomFirework(event.getHitBlock().getLocation());
	}
	
	@EventHandler
	public void onDeath(MatchPlayerDeathEvent event){
		Random rand = new Random();
		if(RandomUtils.nextBoolean(rand, DEATH_CHANCE)){
			playRandomFirework(event.getVictim().getLocation());
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event){
		Random rand = new Random();
		if(RandomUtils.nextBoolean(rand, DAMAGE_CHANCE)){
			playRandomFirework(event.getEntity().getLocation());
		}
	}
	
	
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
