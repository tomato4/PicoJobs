package com.gmail.picono435.picojobs.listeners.jobs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.picono435.picojobs.PicoJobsPlugin;
import com.gmail.picono435.picojobs.api.Job;
import com.gmail.picono435.picojobs.api.JobPlayer;
import com.gmail.picono435.picojobs.api.PicoJobsAPI;
import com.gmail.picono435.picojobs.api.Type;
import com.gmail.picono435.picojobs.managers.LanguageManager;

public class MilkListener implements Listener {
	
	@EventHandler()
	public void onTakeMilk(PlayerBucketFillEvent  e) {
		if(e.getPlayer() == null) return;
		Player p = e.getPlayer();
		JobPlayer jp = PicoJobsAPI.getPlayersManager().getJobPlayer(p);
		if(!jp.hasJob()) return;
		if(!jp.isWorking()) return;
		Job job = jp.getJob();
		if(job.getType() != Type.MILK) return;
		
		new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				boolean isMilk = false;
				if(PicoJobsPlugin.isLegacy()) {
					isMilk = p.getInventory().getItemInHand().getType() == Material.MILK_BUCKET;
				} else {
					isMilk = p.getInventory().getItem(p.getInventory().getHeldItemSlot()).getType() == Material.MILK_BUCKET;
				}
				if(isMilk) {
					if(jp.simulateEvent()) {
						p.sendMessage(LanguageManager.getMessage("finished-work", p));
					}
				}
			}
		}.runTaskLater(PicoJobsPlugin.getPlugin(), 1L);
	}
}
