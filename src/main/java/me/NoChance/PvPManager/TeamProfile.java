package me.NoChance.PvPManager;

import me.NoChance.PvPManager.Config.Variables;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TeamProfile {

	private static Team inCombat;
	private static Team pvpOn;
	private static Team pvpOff;
	private Team previousTeam;
	private PvPlayer pvPlayer;
	private static Scoreboard scoreboard;

	public TeamProfile(PvPlayer p) {
		this.pvPlayer = p;
		if (scoreboard == null)
			scoreboard = p.getPlayer().getScoreboard();
		previousTeam = scoreboard.getPlayerTeam(p.getPlayer());
	}

	public static void setupTeams() {
		if (scoreboard.getTeam("InCombat") != null)
			inCombat = scoreboard.getTeam("InCombat");
		else
			inCombat = scoreboard.registerNewTeam("InCombat");
		inCombat.setPrefix(ChatColor.translateAlternateColorCodes('&', Variables.nameTagColor));
		if (!Variables.toggleColorOn.equalsIgnoreCase("none")) {
			if (scoreboard.getTeam("PvPOn") != null)
				pvpOn = scoreboard.getTeam("PvPOn");
			else
				pvpOn = scoreboard.registerNewTeam("PvPOn");
			pvpOn.setPrefix(ChatColor.translateAlternateColorCodes('&', Variables.toggleColorOn));
		}
		if (!Variables.toggleColorOff.equalsIgnoreCase("none")) {
			if (scoreboard.getTeam("PvPOff") != null)
				pvpOff = scoreboard.getTeam("PvPOff");
			else
				pvpOff = scoreboard.registerNewTeam("PvPOff");
			pvpOff.setPrefix(ChatColor.translateAlternateColorCodes('&', Variables.toggleColorOff));
		}
	}

	public void setInCombat() {
		Player player = pvPlayer.getPlayer();
		if (pvpOn != null || pvpOff != null)
			previousTeam = scoreboard.getPlayerTeam(player);
		inCombat.addPlayer(player);
	}

	public void restoreTeam() {
		try {
			if (previousTeam != null && scoreboard.getTeam(previousTeam.getName()) != null)
				previousTeam.addPlayer(pvPlayer.getPlayer());
			else
				inCombat.removePlayer(pvPlayer.getPlayer());
		} catch (IllegalStateException e) {
			System.out.println("[PvPManager] Error restoring nametag for: " + pvPlayer.getName());
			inCombat.removePlayer(pvPlayer.getPlayer());
		}
	}

	public void setPvP(boolean state) {
		if (state) {
			if (pvpOn == null)
				restoreTeam();
			else
				pvpOn.addPlayer(pvPlayer.getPlayer());
		} else {
			if (pvpOff == null)
				restoreTeam();
			else
				pvpOff.addPlayer(pvPlayer.getPlayer());
		}
	}
}
