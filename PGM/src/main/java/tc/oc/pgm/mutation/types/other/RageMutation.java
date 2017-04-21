package tc.oc.pgm.mutation.types.other;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.mutation.types.MutationModule;
import tc.oc.pgm.rage.RageMatchModule;

public class RageMutation extends MutationModule.Impl {

    RageMatchModule rage;

    public RageMutation(Match match) {
        super(match);
        this.rage = match.module(RageMatchModule.class).orElse(new RageMatchModule(match));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
    	//rage.handlePlayerDamage(event);
    	//Allows for insta rage matches
        if(event.getDamager() instanceof Player) {
           event.setDamage(1000);
        } else if(event.getDamager() instanceof Arrow && ((Arrow) event.getDamager()).getShooter() instanceof Player) {
           event.setDamage(1000);
        }
    }

    @Override
    public void disable() {
        super.disable();
        rage = null;
    }

}
