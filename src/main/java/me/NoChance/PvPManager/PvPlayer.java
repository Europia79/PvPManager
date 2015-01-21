package me.NoChance.PvPManager;

import java.util.HashMap;
import java.util.UUID;

import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.Tasks.NewbieTask;
import me.NoChance.PvPManager.Utils.CombatUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PvPlayer {

	private String name;
	private UUID id;
	private boolean newbie;
	private boolean tagged;
	private boolean pvpState;
	private boolean pvpLogged;
	private boolean override;
	private long toggleTime;
	private long respawnTime;
	private long taggedTime;
	private NewbieTask newbieTask;
	private HashMap<String, Integer> victim = new HashMap<String, Integer>();
	private PvPManager plugin;
	private TeamProfile teamProfile;

	public PvPlayer(Player player, PvPManager plugin) {
		this.name = player.getName();
		this.id = player.getUniqueId();
		this.plugin = plugin;
		this.newbieTask = new NewbieTask(this);
		if (Variables.useNameTag || Variables.toggleNametagsEnabled)
			teamProfile = new TeamProfile(this);
	}

	public String getName() {
		return this.name;
	}

	public UUID getUUID() {
		return this.id;
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(id);
	}

	public String getWorldName() {
		return getPlayer().getWorld().getName();
	}

	public long getToggleTime() {
		return this.toggleTime;
	}

	public void message(String message) {
		if (isOnline())
			getPlayer().sendMessage(message);
	}

	public void togglePvP() {
		if (!CombatUtils.hasTimePassed(toggleTime, Variables.toggleCooldown)) {
			message(Messages.Error_PvP_Cooldown);
			return;
		} else {
			toggleTime = System.currentTimeMillis();
			setPvP(!pvpState);
		}
	}

	public boolean isNewbie() {
		return this.newbie;
	}

	public boolean isOnline() {
		return getPlayer() != null;
	}

	public boolean isInCombat() {
		return this.tagged;
	}

	public boolean hasPvPEnabled() {
		return this.pvpState;
	}

	public boolean hasPvPLogged() {
		return this.pvpLogged;
	}

	public boolean hasOverride() {
		return this.override;
	}

	public void disableFly() {
		Player player = getPlayer();
		player.setFlying(false);
		player.setAllowFlight(false);
	}

	public void setNewbie(boolean newbie) {
		if (newbie) {
			message(Messages.Newbie_Protection.replace("%", Integer.toString(Variables.newbieProtectionTime)));
			newbieTask.runTaskLater(plugin, Variables.newbieProtectionTime * 1200);
		} else {
			if (Bukkit.getServer().getScheduler().isCurrentlyRunning(newbieTask.getTaskId()))
				message(Messages.Newbie_Protection_End);
			else {
				newbieTask.cancel();
				message("§6[§8PvPManager§6] §eYou Removed Your PvP Protection! Be Careful");
			}
		}
		this.newbie = newbie;
	}

	public void setTagged(boolean attacker, String tagger) {
		if (getPlayer().hasPermission("pvpmanager.nocombat"))
			return;

		taggedTime = System.currentTimeMillis();

		if (tagged)
			return;

		if (Variables.useNameTag)
			teamProfile.setInCombat();

		if (!Variables.inCombatSilent)
			if (attacker)
				message(Messages.Tagged_Attacker.replace("%p", tagger));
			else
				message(Messages.Tagged_Defender.replace("%p", tagger));

		this.tagged = true;
		plugin.getPlayerHandler().tag(this);
	}

	public void unTag() {
		if (isOnline()) {
			if (Variables.useNameTag)
				teamProfile.restoreTeam();

			if (!Variables.inCombatSilent)
				message(Messages.Out_Of_Combat);
		}

		this.tagged = false;
	}

	public void setPvP(boolean pvpState) {
		this.pvpState = pvpState;
		if (Variables.toggleNametagsEnabled)
			teamProfile.setPvP(pvpState);
		if (!pvpState) {
			message(Messages.PvP_Disabled);
			if (Variables.toggleBroadcast)
				Bukkit.broadcastMessage(Messages.PvPToggle_Off_Broadcast.replace("%p", name));
		} else {
			message(Messages.PvP_Enabled);
			if (Variables.toggleBroadcast)
				Bukkit.broadcastMessage(Messages.PvPToggle_On_Broadcast.replace("%p", name));
		}
	}

	public void addVictim(String victimName) {
		if (!victim.containsKey(victimName)) {
			victim.put(victimName, 1);
		} else if (victim.containsKey(victimName)) {
			int totalKills = victim.get(victimName);
			if (totalKills < Variables.killAbuseMaxKills) {
				totalKills++;
				victim.put(victimName, totalKills);
			}
			if (totalKills >= Variables.killAbuseMaxKills) {
				for (String command : Variables.killAbuseCommands) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<player>", name));
				}
			}
		}
	}

	public void clearVictims() {
		victim.clear();
	}

	public void setPvpLogged(boolean pvpLogged) {
		this.pvpLogged = pvpLogged;
	}

	public boolean isVictim(String victimName) {
		return victim.containsKey(victimName);
	}

	public boolean hasRespawnProtection() {
		if (respawnTime == 0)
			return false;
		if (CombatUtils.hasTimePassed(respawnTime, Variables.respawnProtection)) {
			respawnTime = 0;
			return false;
		}
		return true;
	}

	public void setRespawnTime(long respawnTime) {
		this.respawnTime = respawnTime;
	}

	public boolean toggleOverride() {
		this.override = !override;
		return this.override;
	}

	public long getTaggedTime() {
		return taggedTime;
	}

	public void loadPvPState() {
		if (getPlayer().hasPermission("pvpmanager.nopvp"))
			this.pvpState = false;
		else if (!getPlayer().hasPlayedBefore()) {
			this.pvpState = Variables.defaultPvp;
			if (Variables.newbieProtectionEnabled)
				setNewbie(true);
		} else if (!plugin.getConfigM().getUserFile().getStringList("players").contains(id.toString()))
			this.pvpState = true;
		if (Variables.toggleNametagsEnabled)
			teamProfile.setPvP(this.pvpState);
	}
}
