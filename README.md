PvPManager
===========

There are multiple PvP plugins, but PvPManager aims to be All in One. Meaning that instead of using multiple plugins that change/customize PvP in your server you can use just PvPManager. 
The features include allowing players to toggle PvP easily, which is a nice feature for donors, block commands for players in combat, detecting and applying defined punishments on PvP logging and a timer feature that toggles PvP for a world automatically! 
All this features have Multi-World support and don't conflict with plugins like WorldGuard!

Bukkit Page: [PvPManager] (http://dev.bukkit.org/bukkit-plugins/pvpmanager/)

Commands and Permissions
-----------

* /pvp - Toggles PvP -> pvpmanager.pvpstatus.change
* /pvp status	- Check your PvP status	-> pvpmanager.pvpstatus.self
* /pvp status <player>	- Check other player PvP status	-> pvpmanager.pvpstatus.others
* /pvp disable protection	- Disables Newbie protection -> No permission
* /pm	- Shows PvPManager help page -> No permission
* /pm update - Update PvPManager to latest version -> pvpmanager.admin
* /pm reload - Reloads PvPManager -> pvpmanager.reload
* /pm pvpstart <time> [world]	- Changes PvP start time on a world -> pvpmanager.pvptimer
* /pm pvpend <time> [world] -	Changes PvP end time on a world -> pvpmanager.pvptimer

Special Permissions:
-----------

pvpmanager.nodrop - Players/ranks don't drop items if killed in PvP

pvpmanager.nocombat - Players/ranks are not placed in combat

pvpmanager.nopvp - Players/ranks have PvP disabled

pvpmanager.nodisable - Players/ranks don't get fly and gamemode disabled on PvP 
