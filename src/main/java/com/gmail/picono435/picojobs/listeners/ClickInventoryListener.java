package com.gmail.picono435.picojobs.listeners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.picono435.picojobs.PicoJobsPlugin;
import com.gmail.picono435.picojobs.api.EconomyImplementation;
import com.gmail.picono435.picojobs.api.Job;
import com.gmail.picono435.picojobs.api.JobPlayer;
import com.gmail.picono435.picojobs.api.PicoJobsAPI;
import com.gmail.picono435.picojobs.api.Type;
import com.gmail.picono435.picojobs.api.WhitelistConf;
import com.gmail.picono435.picojobs.commands.JobsCommand;
import com.gmail.picono435.picojobs.managers.LanguageManager;
import com.gmail.picono435.picojobs.menu.MenuAction;
import com.gmail.picono435.picojobs.menu.GUISettingsMenu;
import com.gmail.picono435.picojobs.menu.JobSettingsMenu;
import com.gmail.picono435.picojobs.utils.FileCreator;
import com.gmail.picono435.picojobs.utils.OtherUtils;
import com.gmail.picono435.picojobs.utils.TimeFormatter;

import mkremins.fanciful.FancyMessage;
import net.md_5.bungee.api.ChatColor;

public class ClickInventoryListener implements Listener {
	
	public static Map<ItemStack, String> actionItems = new HashMap<ItemStack, String>();
	
	private static Map<Player, MenuAction> menuActions  = new HashMap<Player, MenuAction>();
	private static Map<Player, Job> menuJobs  = new HashMap<Player, Job>();
	
	@EventHandler()
	public void onBasicClick(InventoryClickEvent e) {
		if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null || e.getCurrentItem().getItemMeta().getDisplayName() == null) return;
		
		/*
		 * Choose Jobs Menu Clicking Event
		 */
		if(e.getView().getTitle().equals(FileCreator.getGUI().getString("gui-settings.choose-job.title"))) {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			JobPlayer jp = PicoJobsAPI.getPlayersManager().getJobPlayer(p);
			Job job = PicoJobsAPI.getJobsManager().getJobByDisplayname(e.getCurrentItem().getItemMeta().getDisplayName());
			if(job.requiresPermission() && !p.hasPermission("picojobs.job." + job.getID())) {
				p.sendMessage(LanguageManager.getMessage("no-permission", p));
				return;
			}
			jp.setJob(job);
			p.sendMessage(LanguageManager.getMessage("choosed-job", p));
			p.closeInventory();
			return;
		}
		
		/*
		 * Accept Work Menu Clicking Event
		 */
		if(e.getView().getTitle().equals(FileCreator.getGUI().getString("gui-settings.need-work.title"))) {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			JobPlayer jp = PicoJobsAPI.getPlayersManager().getJobPlayer(p);
			//Job job = jp.getJob();
			String action = actionItems.get(e.getCurrentItem());
			if(action == null) return;
			if(action.equalsIgnoreCase("salary")) {
				if(!jp.hasJob()) {
					p.sendMessage(LanguageManager.getMessage("no-args", p));
					return;
				}
				if(JobsCommand.salaryCooldown.containsKey(p.getUniqueId())) {
					long a1 = JobsCommand.salaryCooldown.get(p.getUniqueId()) + TimeUnit.MINUTES.toMillis(PicoJobsAPI.getSettingsManager().getSalaryCooldown());
					if(System.currentTimeMillis() >= a1) {
						JobsCommand.salaryCooldown.remove(p.getUniqueId());
					} else {
						p.sendMessage(LanguageManager.getMessage("salary-cooldown", p).replace("%cooldown_mtime%", TimeFormatter.formatTimeInMinecraft(a1 - System.currentTimeMillis()).replace("%cooldown_time%", TimeFormatter.formatTimeInRealLife(a1 - System.currentTimeMillis()))));
						p.closeInventory();
						return;
					}
				}
				double salary = jp.getSalary();
				if(salary <= 0) {
					p.sendMessage(LanguageManager.getMessage("no-salary", p));
					return;
				}
				String economyString = jp.getJob().getEconomy();
				if(!PicoJobsPlugin.getInstance().economies.containsKey(economyString)) {
					p.sendMessage(LanguageManager.formatMessage("&cWe did not find the economy implementation said" + " (" + economyString + ")" + ". Please contact an administrator in order to get more information."));
					p.closeInventory();
					return;
				}
				EconomyImplementation economy = PicoJobsPlugin.getInstance().economies.get(economyString);
				p.sendMessage(LanguageManager.getMessage("got-salary", p));
				economy.deposit(p, salary);
				jp.removeSalary(salary);
				JobsCommand.salaryCooldown.put(p.getUniqueId(), System.currentTimeMillis());
				p.closeInventory();
				return;
			}
			if(action.equalsIgnoreCase("acceptwork")) {
				p.sendMessage(LanguageManager.getMessage("accepted-work", p));
				jp.setWorking(true);
				p.closeInventory();
				return;
			}
			if(action.equalsIgnoreCase("leavejob")) {
				jp.removePlayerStats();
				p.sendMessage(LanguageManager.getMessage("left-job", p));
				p.closeInventory();
				return;
			}
			return;
		}
		
		/*
		 * Status Work Menu Clicking Event
		 */
		if(e.getView().getTitle().equals(FileCreator.getGUI().getString("gui-settings.has-work.title"))) {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();
			JobPlayer jp = PicoJobsAPI.getPlayersManager().getJobPlayer(p);
			//Job job = jp.getJob();
			String action = actionItems.get(e.getCurrentItem());
			if(action.equalsIgnoreCase("salary")) {
				double salary = jp.getSalary();
				if(salary <= 0) {
					p.sendMessage(LanguageManager.getMessage("no-salary", p));
					p.closeInventory();
					return;
				}
				String economyString = jp.getJob().getEconomy();
				if(!PicoJobsPlugin.getInstance().economies.containsKey(economyString)) {
					p.sendMessage(LanguageManager.formatMessage("&cWe did not find the economy implementation said. Please contact an administrator for get more information."));
					p.closeInventory();
					return;
				}
				EconomyImplementation economy = PicoJobsPlugin.getInstance().economies.get(economyString);
				p.sendMessage(LanguageManager.getMessage("got-salary", p));
				economy.deposit(p, salary);
				jp.removeSalary(salary);
				p.closeInventory();
				return;
			}
			if(action.equalsIgnoreCase("leavejob")) {
				jp.removePlayerStats();
				p.sendMessage(LanguageManager.getMessage("left-job", p));
				p.closeInventory();
				return;
			}
			return;
		}
	}

	@EventHandler()
	public void onSettingsClick(InventoryClickEvent e) {
		if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null || e.getCurrentItem().getItemMeta().getDisplayName() == null) return;
		
		Player p = (Player) e.getWhoClicked();
		
		if(!p.hasPermission("picojobs.admin")) return;
		
		/*
		 * General Settings Click
		 */
		if(JobSettingsMenu.generalInventories.contains(e.getInventory())) {
			e.setCancelled(true);
			
			switch(e.getSlot()) {
			case(12): {
				p.closeInventory();
				JobSettingsMenu.openJobsList(p);
				return;
			}
			case(14): {
				p.closeInventory();
				GUISettingsMenu.openGeneral(p);
				return;
			}
			}
			return;
		}
		
		/*
		 * Job List Settings Click
		 */
		if(JobSettingsMenu.jobListInventories.contains(e.getInventory())) {
			e.setCancelled(true);
			
			Job job = PicoJobsAPI.getJobsManager().getJobByDisplayname(e.getCurrentItem().getItemMeta().getDisplayName());
			if(job == null) {
				if(e.getCurrentItem().getItemMeta().getLore().get(0).equalsIgnoreCase(ChatColor.GRAY + "Click to go to the next page.")) {
					int page = Integer.parseInt(e.getView().getTitle().split("\\[")[1].replace("]", "")) - 1;
					JobSettingsMenu.openJobsList(p, page + 1);
					return;
				}
				if(e.getCurrentItem().getItemMeta().getLore().get(0).equalsIgnoreCase(ChatColor.GRAY + "Click to go to the previous page.")) {
					int page = Integer.parseInt(e.getView().getTitle().split("\\[")[1].replace("]", "")) - 1;
					if(page == 0) return;
					JobSettingsMenu.openJobsList(p, page - 1);
					return;
				}
				return;
			}
			
			p.closeInventory();
			JobSettingsMenu.openJobSettings(p, job);
			return;
		}
		
		/*
		 * Job Settings Click
		 */
		if(JobSettingsMenu.jobSettingsInventories.containsKey(e.getInventory())) {
			e.setCancelled(true);
			
			Job job = JobSettingsMenu.jobSettingsInventories.get(e.getInventory());
			
			switch(e.getSlot()) {
			case(13): {
				p.closeInventory();
				JobSettingsMenu.openJobEdit(p, job);
				return;
			}
			case(15): {
				String id = job.getID();
				PicoJobsPlugin.getInstance().jobs.remove(id);
				FileCreator.getJobsConfig().getConfigurationSection("jobs").set(id, null);
				try {
					FileCreator.getJobsConfig().save(FileCreator.getJobsFile());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				p.sendMessage(LanguageManager.formatMessage("&cThe job " + id + " was deleted sucefully."));
				p.closeInventory();
				return;
			}
			}
			
			return;
		}
		
		/*
		 * Job Edit Click
		 */
		if(JobSettingsMenu.jobEditInventories.containsKey(e.getInventory())) {
			e.setCancelled(true);
			
			Job job = JobSettingsMenu.jobEditInventories.get(e.getInventory());
			
			switch(e.getSlot()) {
			case(11): {
				String message = "\n�aSend the new displayname on the chat in order to change it";
				new FancyMessage(message)
				.then("\n�a")
				.send(p);
				if(menuJobs.containsKey(p)) menuJobs.remove(p);
				if(menuActions.containsKey(p)) menuActions.remove(p);
				menuJobs.put(p, job);
				menuActions.put(p, MenuAction.SETDISPLAYNAME);
				p.closeInventory();
				return;
			}
			case(15): {
				String message = "\n�aSend the new salary on the chat in order to change it";
				new FancyMessage(message)
				.then("\n�a")
				.send(p);
				if(menuJobs.containsKey(p)) menuJobs.remove(p);
				if(menuActions.containsKey(p)) menuActions.remove(p);
				menuJobs.put(p, job);
				menuActions.put(p, MenuAction.SETSALARY);
				p.closeInventory();
				return;
			}
			case(19): {
				String message = "\n�aSend the new job type on the chat in order to change it";
				new FancyMessage(message)
				.then(" �a( �nWiki Page�r�a )")
				.link("https://github.com/Picono435/PicoJobs/wiki/Types-of-jobs")
				.tooltip("�8Click here to acess the WIKI PAGE about this action.")
				.then("\n�a")
				.send(p);
				if(menuJobs.containsKey(p)) menuJobs.remove(p);
				if(menuActions.containsKey(p)) menuActions.remove(p);
				menuJobs.put(p, job);
				menuActions.put(p, MenuAction.SETJOBTYPE);
				p.closeInventory();
				return;
			}
			case(20): {
				String message = "\n�aSend the new economy type on the chat in order to change it";
				new FancyMessage(message)
				.then(" �a( �nWiki Page�r�a )")
				.link("https://github.com/Picono435/PicoJobs/wiki/Economy-Types")
				.tooltip("�8Click here to acess the WIKI PAGE about this action.")
				.then("\n�a")
				.send(p);
				if(menuJobs.containsKey(p)) menuJobs.remove(p);
				if(menuActions.containsKey(p)) menuActions.remove(p);
				menuJobs.put(p, job);
				menuActions.put(p, MenuAction.SETECONOMY);
				p.closeInventory();
				return;
			}
			case(24): {
				String message = "\n�aSend the new method on the chat in order to change it";
				new FancyMessage(message)
				.then("\n�a")
				.send(p);
				if(menuJobs.containsKey(p)) menuJobs.remove(p);
				if(menuActions.containsKey(p)) menuActions.remove(p);
				menuJobs.put(p, job);
				menuActions.put(p, MenuAction.SETREQMETHOD);
				p.closeInventory();
				return;
			}
			case(33): {
				String message = "\n�aSend the new salary frequency on the chat in order to change it";
				new FancyMessage(message)
				.then("\n�a")
				.send(p);
				if(menuJobs.containsKey(p)) menuJobs.remove(p);
				if(menuActions.containsKey(p)) menuActions.remove(p);
				menuJobs.put(p, job);
				menuActions.put(p, MenuAction.SETSALARYFREQ);
				p.closeInventory();
				return;
			}
			case(37): {
				String message = "\n�aSend the formatted whitelist like this: To add something put +:THING, to remove something put -:THING. If you want to add multiple things separate them with a comma and da space(, ).";
				new FancyMessage(message)
				.tooltip("�8EXAMPLE: +:BRICKS, +:LEAVES, -:OAK_WOOD")
				.then("\n�a")
				.send(p);
				if(menuJobs.containsKey(p)) menuJobs.remove(p);
				if(menuActions.containsKey(p)) menuActions.remove(p);
				menuJobs.put(p, job);
				menuActions.put(p, MenuAction.SETWHITELIST);
				p.closeInventory();
				return;
			}
			case(38): {
				if(job.isWhitelist()) {
					job.setWhitelistType(false);
				} else {
					job.setWhitelistType(true);
				}
				JobSettingsMenu.openJobEdit(p, job);
			}
			case(39): {
				if(job.requiresPermission()) {
					job.setRequiresPermission(false);
				} else {
					job.setRequiresPermission(true);
				}
				JobSettingsMenu.openJobEdit(p, job);
				return;
			}
			case(42): {
				String message = "\n�aSend the new method frequency on the chat in order to change it";
				new FancyMessage(message)
				.then("\n�a")
				.send(p);
				if(menuJobs.containsKey(p)) menuJobs.remove(p);
				if(menuActions.containsKey(p)) menuActions.remove(p);
				menuJobs.put(p, job);
				menuActions.put(p, MenuAction.SETMETHODFREQ);
				p.closeInventory();
				return;
			}
			}
			return;
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if(!menuActions.containsKey(p)) return;
		if(!menuJobs.containsKey(p)) return;
		e.setCancelled(true);
		Job job = menuJobs.get(p);
		MenuAction action = menuActions.get(p);
		if(action == MenuAction.SETDISPLAYNAME) {
			job.setDisplayName(e.getMessage());
			p.sendMessage(LanguageManager.getMessage("sucefully", p));
			menuActions.remove(p);
			menuJobs.remove(p);
			new BukkitRunnable() {
				public void run() {
					JobSettingsMenu.openJobEdit(p, job);
				}
			}.runTask(PicoJobsPlugin.getInstance());
		}
		if(action == MenuAction.SETSALARY) {
			double value = 0;
			try {
				value = Double.parseDouble(e.getMessage());
			} catch(Exception ex) {
				p.sendMessage(LanguageManager.getMessage("invalid-arg", p));
				menuActions.remove(p);
				menuJobs.remove(p);
				return;
			}
			job.setSalary(value);
			p.sendMessage(LanguageManager.getMessage("sucefully", p));
			menuActions.remove(p);
			menuJobs.remove(p);
			new BukkitRunnable() {
				public void run() {
					JobSettingsMenu.openJobEdit(p, job);
				}
			}.runTask(PicoJobsPlugin.getInstance());
		}
		if(action == MenuAction.SETJOBTYPE) {
			Type type = Type.getType(e.getMessage());
			if(type == null) {
				p.sendMessage(LanguageManager.getMessage("invalid-arg", p));
				menuActions.remove(p);
				menuJobs.remove(p);
				return;
			}
			job.setType(type);
			p.sendMessage(LanguageManager.getMessage("sucefully", p));
			menuActions.remove(p);
			menuJobs.remove(p);
			new BukkitRunnable() {
				public void run() {
					JobSettingsMenu.openJobEdit(p, job);
				}
			}.runTask(PicoJobsPlugin.getInstance());
		}
		if(action == MenuAction.SETECONOMY) {
			EconomyImplementation economy = PicoJobsPlugin.getInstance().economies.get(e.getMessage());
			if(economy == null) {
				p.sendMessage(LanguageManager.getMessage("invalid-arg", p));
				menuActions.remove(p);
				menuJobs.remove(p);
				return;
			}
			job.setEconomy(economy.getName());
			p.sendMessage(LanguageManager.getMessage("sucefully", p));
			menuActions.remove(p);
			menuJobs.remove(p);
			new BukkitRunnable() {
				public void run() {
					JobSettingsMenu.openJobEdit(p, job);
				}
			}.runTask(PicoJobsPlugin.getInstance());
		}
		if(action == MenuAction.SETREQMETHOD) {
			double value = 0;
			try {
				value = Double.parseDouble(e.getMessage());
			} catch(Exception ex) {
				p.sendMessage(LanguageManager.getMessage("invalid-arg", p));
				menuActions.remove(p);
				menuJobs.remove(p);
				return;
			}
			job.setMethod(value);
			p.sendMessage(LanguageManager.getMessage("sucefully", p));
			menuActions.remove(p);
			menuJobs.remove(p);
			new BukkitRunnable() {
				public void run() {
					JobSettingsMenu.openJobEdit(p, job);
				}
			}.runTask(PicoJobsPlugin.getInstance());
		}
		if(action == MenuAction.SETSALARYFREQ) {
			double value = 0;
			try {
				value = Double.parseDouble(e.getMessage());
			} catch(Exception ex) {
				p.sendMessage(LanguageManager.getMessage("invalid-arg", p));
				menuActions.remove(p);
				menuJobs.remove(p);
				return;
			}
			job.setSalaryFrequency(value);
			p.sendMessage(LanguageManager.getMessage("sucefully", p));
			menuActions.remove(p);
			menuJobs.remove(p);
			new BukkitRunnable() {
				public void run() {
					JobSettingsMenu.openJobEdit(p, job);
				}
			}.runTask(PicoJobsPlugin.getInstance());
		}
		if(action == MenuAction.SETWHITELIST) {
			String[] values = e.getMessage().split(", ");
			List<String> newWhitelist = new ArrayList<String>(job.getStringWhitelist());
			for(String value : values) {
				String noFormat = value.replaceFirst("\\+:", "").replaceFirst("\\-:", "");
				WhitelistConf whitelistConf = PicoJobsAPI.getJobsManager().getConfigWhitelist(job.getType());
				if(whitelistConf == WhitelistConf.MATERIAL) {
					if(Material.matchMaterial(noFormat) == null) {
						p.sendMessage(LanguageManager.formatMessage("&cERROR: We could not found any material with the name " + noFormat + ". Continuing to the next value..."));
						continue;
					}
				} else if(whitelistConf == WhitelistConf.ENTITY) {
					if(OtherUtils.getEntityByName(noFormat) == null) {
						p.sendMessage(LanguageManager.formatMessage("&cERROR: We could not found any entity with the name " + noFormat + ". Continuing to the next value..."));
						continue;
					}
				} else if(whitelistConf == WhitelistConf.JOB) {
					if(PicoJobsAPI.getJobsManager().getJob(noFormat) == null) {
						p.sendMessage(LanguageManager.formatMessage("&cERROR: We could not found any job with the ID " + noFormat + ". Continuing to the next value..."));
						continue;
					}
				}
				
				if(value.substring(0, 2).equals("+:")) {
					if(newWhitelist.contains(noFormat)) {
						p.sendMessage(LanguageManager.formatMessage("&cERROR: The value " + noFormat + " is already in the whitelist. Continuing to the next value..."));
						continue;
					}
					newWhitelist.add(noFormat);
					continue;
				}
				if(value.substring(0, 2).equals("-:")) {
					if(!newWhitelist.contains(noFormat)) {
						p.sendMessage(LanguageManager.formatMessage("&cERROR: The value " + noFormat + " is not in the whitelist. Continuing to the next value..."));
						continue;
					}
					newWhitelist.remove(noFormat);
					continue;
				}
				p.sendMessage(LanguageManager.formatMessage("&cERROR: Undefined prefix &8 " + value.substring(0, 2) + "&c, please use +: to add & -: to remove. Continuing to the next value..."));
				continue;
			}
			job.setWhitelist(newWhitelist);
			p.sendMessage(LanguageManager.getMessage("sucefully", p));
			menuActions.remove(p);
			menuJobs.remove(p);
			new BukkitRunnable() {
				public void run() {
					JobSettingsMenu.openJobEdit(p, job);
				}
			}.runTask(PicoJobsPlugin.getInstance());
		}
		if(action == MenuAction.SETMETHODFREQ) {
			double value = 0;
			try {
				value = Double.parseDouble(e.getMessage());
			} catch(Exception ex) {
				p.sendMessage(LanguageManager.getMessage("invalid-arg", p));
				menuActions.remove(p);
				menuJobs.remove(p);
				return;
			}
			job.setMethodFrequency(value);
			p.sendMessage(LanguageManager.getMessage("sucefully", p));
			menuActions.remove(p);
			menuJobs.remove(p);
			new BukkitRunnable() {
				public void run() {
					JobSettingsMenu.openJobEdit(p, job);
				}
			}.runTask(PicoJobsPlugin.getInstance());
		}
	}
	
	@EventHandler()
	public void onGUISettingsClick(InventoryClickEvent e) {
		if(e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null || e.getCurrentItem().getItemMeta().getDisplayName() == null) return;
		
		Player p = (Player) e.getWhoClicked();
		
		if(!p.hasPermission("picojobs.admin")) return;
		
		/*
		 * General GUI Settings Click
		 */
		if(GUISettingsMenu.generalInventories.contains(e.getInventory())) {
			e.setCancelled(true);
			
			switch(e.getSlot()) {
			case(11): {
				p.closeInventory();
				GUISettingsMenu.openChooseJobSettings(p);
				return;
			}
			case(13): {
				p.closeInventory();
				GUISettingsMenu.openNeedWorkSettings(p);
				return;
			}
			case(15): {
				p.closeInventory();
				GUISettingsMenu.openHasWorkSettings(p);
				return;
			}
			}
			return;
		}
		
		/*
		 * Item Edit GUI Settings Click
		 */
		if(GUISettingsMenu.itemEdit.containsKey(e.getInventory())) {
			e.setCancelled(true);
			ItemStack item = GUISettingsMenu.itemEdit.get(e.getInventory());
			String gui = StringUtils.substringBetween(e.getView().getTitle(), "[", "]");
			String itemSetting = StringUtils.substringBetween(e.getView().getTitle(), "(", ")");
			ConfigurationSection itemSettings = FileCreator.getGUI().getConfigurationSection("gui-settings").getConfigurationSection(gui).getConfigurationSection("items").getConfigurationSection(itemSetting);
			
			switch(e.getSlot()) {
			case(10): {
				// RENAME
				return;
			}
			case(12): {
				// LORE EDITOR
				return;
			}
			case(14): {
				// ENCHANTED
				boolean enchanted = itemSettings.getBoolean("enchanted");
				boolean newEnchanted = false;
				if(enchanted) {
					newEnchanted = false;
				} else {
					newEnchanted = true;
				}
				itemSettings.set("enchanted", newEnchanted);
				try {
					FileCreator.getGUI().save(FileCreator.getGUIFile());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				p.closeInventory();
				GUISettingsMenu.openGeneral(p);
				return;
			}
			case(16): {
				// ACTION
				return;
			}
			}
			return;
		}
		
		/*
		 * Choose Job GUI Settings Click
		 */
		if(GUISettingsMenu.guiSettings.containsKey(e.getInventory()) && GUISettingsMenu.guiSettings.get(e.getInventory()).equals("choose-job")) {
			e.setCancelled(true);
			
			
			return;
		}
		
		/*
		 * Need Work GUI Settings Click
		 */
		if(GUISettingsMenu.guiSettings.containsKey(e.getInventory()) && GUISettingsMenu.guiSettings.get(e.getInventory()).equals("need-work")) {
			if(e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_RIGHT) {
				e.setCancelled(true);
				p.closeInventory();
				GUISettingsMenu.openItemEdit(p, e.getCurrentItem(), "need-work", StringUtils.substringBetween(e.getCurrentItem().getItemMeta().getDisplayName(), "[[", "]]"));
				return;
			}
			return;
		}
	}
	
	@EventHandler()
	public void onClose(InventoryCloseEvent e) {
		if(JobSettingsMenu.generalInventories.contains(e.getInventory())) JobSettingsMenu.generalInventories.remove(e.getInventory());
		if(JobSettingsMenu.jobListInventories.contains(e.getInventory())) JobSettingsMenu.jobListInventories.remove(e.getInventory());
		if(JobSettingsMenu.jobEditInventories.containsKey(e.getInventory())) JobSettingsMenu.jobEditInventories.remove(e.getInventory());
		if(JobSettingsMenu.jobSettingsInventories.containsKey(e.getInventory())) JobSettingsMenu.jobSettingsInventories.remove(e.getInventory());
		if(GUISettingsMenu.generalInventories.contains(e.getInventory())) GUISettingsMenu.generalInventories.remove(e.getInventory());
		if(GUISettingsMenu.guiSettings.containsKey(e.getInventory())) GUISettingsMenu.generalInventories.remove(e.getInventory());
	}
}
