package com.gmail.picono435.picojobs.listeners.jobs;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;

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
		try {
			Block b = (Block)Class.forName("org.bukkit.event.player.PlayerBucketFillEvent").getMethod("getBlockClicked").invoke(this);
			if(b.isLiquid()) return;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException | ClassNotFoundException e1) {
			if(e.getBlock().isLiquid()) return;
		};
		Player p = e.getPlayer();
		JobPlayer jp = PicoJobsAPI.getPlayersManager().getJobPlayer(p);
		if(!jp.hasJob()) return;
		if(!jp.isWorking()) return;
		Job job = jp.getJob();
		if(job.getType() != Type.MILK) return;
		if(jp.simulateEvent(job.getType())) {
			p.sendMessage(LanguageManager.getMessage("finished-work", p));
		}
	}
}
