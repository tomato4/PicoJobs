package com.gmail.picono435.picojobs.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.gmail.picono435.picojobs.PicoJobsPlugin;
import com.gmail.picono435.picojobs.api.PicoJobsAPI;
import com.gmail.picono435.picojobs.managers.LanguageManager;
import com.gmail.picono435.picojobs.vars.Job;
import com.gmail.picono435.picojobs.vars.JobPlayer;

public class ClickInventoryListener implements Listener {
	
	// CHOOSE JOBS MENU
	@EventHandler()
	public void onChooseJob(InventoryClickEvent e) {
		if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null || e.getCurrentItem().getItemMeta().getDisplayName() == null || e.getCurrentItem().getItemMeta().getLore() == null) return;
		if(!e.getView().getTitle().equals(PicoJobsPlugin.getPlugin().getConfig().getString("gui-settings.choose-job.title"))) return;
		e.setCancelled(true);
		Player p = (Player) e.getWhoClicked();
		JobPlayer jp = PicoJobsAPI.getPlayersManager().getJobPlayer(p);
		Job job = PicoJobsAPI.getJobsManager().getJobByDisplayname(e.getCurrentItem().getItemMeta().getDisplayName());
		jp.setJob(job);
		p.sendMessage(LanguageManager.getMessage("choosed-job", p));
	}
}
