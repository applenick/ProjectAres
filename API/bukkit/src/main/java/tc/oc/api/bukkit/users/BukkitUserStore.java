package tc.oc.api.bukkit.users;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventBus;

import com.applenick.Lightning.Lightning;
import com.applenick.Lightning.users.ThunderUser;
import com.applenick.Lightning.users.ThunderUsers;

import tc.oc.api.bukkit.event.UserUpdateEvent;
import tc.oc.api.bukkit.friends.OnlineFriends;
import tc.oc.api.docs.User;
import tc.oc.api.docs.UserId;
import tc.oc.api.minecraft.users.UserStore;
import tc.oc.api.users.UserService;
import tc.oc.minecraft.scheduler.MainThreadExecutor;

@Singleton
public class BukkitUserStore extends UserStore<Player> implements OnlinePlayers, OnlineFriends {

    @Inject private EventBus eventBus;
    @Inject private MainThreadExecutor executor;
    @Inject private UserService userService;
    		

    @Override
    protected void updateUser(tc.oc.minecraft.api.entity.Player player, @Nullable User before, @Nullable User after) {
        eventBus.callEvent(new UserUpdateEvent((Player) player, before, after),
                           event -> super.updateUser(player, before, after));
    }

    @Override
    public boolean areFriends(Player a, UserId b) {
    	ThunderUsers users = Lightning.get().getUsers();
    	if(users != null){
        	ThunderUser user = users.getThunderUser(a.getUniqueId());
        	if(user != null){
        		return user.areFriends(UUID.fromString(b.player_id()));
        	}else{
        		return false;
        	}
    	}
    	Bukkit.getServer().getConsoleSender().sendMessage("LIGHTNING NOT FOUND - a idb");
    	return false;
    }

    @Override
    public boolean areFriends(Player a, Player b) {
    	ThunderUsers users = Lightning.get().getUsers();
    	if(users != null){
        	ThunderUser user = users.getThunderUser(a.getUniqueId());
        	if(user != null){
        		return user.areFriends(b.getUniqueId());
        	}else{
        		return false;
        	}
    	}
    	Bukkit.getServer().getConsoleSender().sendMessage("LIGHTNING NOT FOUND - ab");
    	return false;

    }

    @Override
    public Stream<Player> onlineFriends(UserId userId) {
        return all().stream().filter(player -> areFriends(player, userId));
    }

    @Override
    public Stream<Player> onlineFriends(Player player) {
        final User user = tryUser(player);
        return user == null ? Stream.empty() : onlineFriends(user);
    }

    public void refresh(final Collection<Player> players) {
        final Map<String, Player> idToKey = new HashMap<>();
        for(Player player : players) {
            User user = tryUser(player);
            if(user != null) {
                idToKey.put(user._id(), player);
            }
        }

        if(idToKey.isEmpty()) return;

        executor.callback(userService.find(idToKey.keySet()), (result) -> {
            for(User user : result.documents()) {
                final Player player = idToKey.get(user._id());
                if(player != null) {
                    replaceUser(player, user);
                }
            }
        });
    }
}
