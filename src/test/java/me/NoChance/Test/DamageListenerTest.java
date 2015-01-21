package me.NoChance.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Config.Messages;
import me.NoChance.PvPManager.Listeners.PlayerListener;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.Utils.CancelResult;
import me.NoChance.PvPManager.Utils.CombatUtils;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PvPManager.class, CombatUtils.class })
public class DamageListenerTest {

	PlayerListener damageListener;
	EntityDamageByEntityEvent mockEvent;
	Player attacker;
	Player defender;

	@Before
	public void setup() throws Exception {
		PvPManager plugin = mock(PvPManager.class);
		PowerMockito.mockStatic(CombatUtils.class);
		PlayerHandler ph = mock(PlayerHandler.class);

		mockEvent = PowerMockito.mock(EntityDamageByEntityEvent.class);

		attacker = mock(Player.class);
		defender = mock(Player.class, RETURNS_DEEP_STUBS);

		when(defender.getWorld().getName()).thenReturn("world");
		when(CombatUtils.PMAllowed(anyString())).thenReturn(true);
		when(mockEvent.getDamager()).thenReturn(attacker);
		when(mockEvent.getEntity()).thenReturn(defender);
		when(mockEvent.isCancelled()).thenReturn(false);
		when(CombatUtils.isPvP(mockEvent)).thenCallRealMethod();

		damageListener = new PlayerListener(plugin);

		Field playerHandlerfield = PlayerListener.class.getDeclaredField("ph");
		playerHandlerfield.setAccessible(true);
		playerHandlerfield.set(damageListener, ph);
	}

	public void createAttack(CancelResult cr) {
		when(CombatUtils.tryCancel(attacker, defender)).thenReturn(cr);
		damageListener.onPlayerDamage(mockEvent);
	}

	@Test
	public void pvpCheck() {
		assertTrue(CombatUtils.isPvP(mockEvent));
	}

	@Test
	public void cancelNewbie() {
		CancelResult cr = CancelResult.NEWBIE;
		createAttack(cr);

		verify(attacker).sendMessage(Messages.Newbie_Protection_On_Hit);
		verify(mockEvent).setCancelled(true);
		assertEquals(cr, CombatUtils.tryCancel(attacker, defender));
	}

	@Test
	public void cancelPvPDisabled() {
		CancelResult cr = CancelResult.PVPDISABLED;
		createAttack(cr);

		verify(attacker).sendMessage(Messages.Attack_Denied_You);
		verify(mockEvent).setCancelled(true);
		assertEquals(cr, CombatUtils.tryCancel(attacker, defender));
	}

	@Test
	public void failCancel() {
		CancelResult cr = CancelResult.FAIL;
		when(CombatUtils.tryCancel(attacker, defender)).thenReturn(cr);
		damageListener.onPlayerDamageMonitor(mockEvent);

		verify(mockEvent, never()).setCancelled(true);
		assertEquals(cr, CombatUtils.tryCancel(attacker, defender));
	}

	@Test
	public void overrideCancel() {
		CancelResult cr = CancelResult.FAIL_OVERRIDE;
		when(CombatUtils.tryCancel(attacker, defender)).thenReturn(cr);
		damageListener.onPlayerDamageOverride(mockEvent);

		verify(mockEvent, never()).setCancelled(false);
		verify(attacker, never()).sendMessage(anyString());
		assertEquals(cr, CombatUtils.tryCancel(attacker, defender));
	}

	@Test
	public void overrideCancelled() {
		CancelResult cr = CancelResult.FAIL_OVERRIDE;
		when(mockEvent.isCancelled()).thenReturn(true);
		when(CombatUtils.tryCancel(attacker, defender)).thenReturn(cr);
		damageListener.onPlayerDamageOverride(mockEvent);

		verify(mockEvent).setCancelled(false);
		verify(attacker, never()).sendMessage(anyString());
		assertEquals(cr, CombatUtils.tryCancel(attacker, defender));
	}

}
