package com.gmail.picono435.picojobs.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.picono435.picojobs.PicoJobsPlugin;
import com.gmail.picono435.picojobs.api.JobPlayer;
import com.gmail.picono435.picojobs.managers.LanguageManager;

import mkremins.fanciful.FancyMessage;
import net.md_5.bungee.api.ChatColor;

public class CreatePlayerListener implements Listener {
	
	@EventHandler()
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if(!PicoJobsPlugin.getInstance().playersdata.containsKey(p.getUniqueId())) {
			PicoJobsPlugin.getInstance().playersdata.put(p.getUniqueId(), new JobPlayer(null, 0, 1, 0, false, p.getUniqueId()));
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onCheckVersionJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		// VERSION CHECKER
		new BukkitRunnable() {
			public void run() {
				if(p.hasPermission("picojobs.admin") && PicoJobsPlugin.getInstance().isOldVersion()) {
					String message = "\n" + LanguageManager.formatMessage("&cYou are using an old version of PicoJobs. This new version can include fixes to current errors.\n&7 You can update automatically by clicking in this message.\n&c");
					new FancyMessage(message)
							.command("/jobsadmin update")
							.tooltip(ChatColor.RED + "Click here to update PicoJobs plugin to v" + PicoJobsPlugin.getInstance().getLastestPluginVersion())
							.send(p);
				}
			}
		}.runTaskLater(PicoJobsPlugin.getInstance(), 20L);
	}
}
