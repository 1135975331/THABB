package thabb.ability.abilities

import org.bukkit.ChatColor.*
import org.bukkit.Sound
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin.getPlugin
import org.bukkit.scheduler.BukkitRunnable
import thabb.Main
import thabb.ability.Ability
import thabb.util.Notes.*
import thabb.util.sendActionBar

class AbilityExample : Ability(
		intArrayOf(0, 0, 0, 0, 0, 0),  //현재 대기시간  cooldown
		intArrayOf(10, 20, 30, 10, 20, 30),  //설정 대기시간  cooldownInit
		intArrayOf(0, 0, 0, 0, 0, 0),  //지속시간  duration
		intArrayOf(10, 0, 0, 0, 0, 0),  //설정 대기시간  durationInit
		"Example",
		"예시를 표시하는 정도의 능력",
		arrayOf("${WHITE}철괴 좌클릭 : 약한 정도의 기술.  [지속시간 10초, 대기시간 10초]",  //능력의 기술 설명  abilityDesc
				"${GOLD}금괴 좌클릭 : 중간 정도의 기술.  [대기시간 20초]",
				"${AQUA}다이아몬드 좌클릭 : 강한 정도의 기술.  [대기시간 30초]"),
		2, /*volume*/floatArrayOf(1.0f, 0.8f), /*instrument*/arrayOf(Sound.BLOCK_NOTE_BLOCK_HARP, Sound.BLOCK_NOTE_BLOCK_BASS),
		arrayOf(floatArrayOf(C2, D2, E2, F2, G2, END), floatArrayOf(E2, F2, G2, A2, B2, END)))
{
	override fun ironIngotLeft(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.IRON_LEFT

		if(cooldown[SKILL_TYPE] > 0) {  //대기시간이 남아있을 시
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		//	구현할 것들 Code here.
		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "철괴(좌클릭)") //대기시간 카운트다운 시작
	}

	override fun ironIngotRight(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.IRON_RIGHT

		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		//구현할 것들 Code here.
		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "철괴(우클릭)")
	}

	/*---------------------------------------------------------------------------------------------*/

	override fun goldIngotLeft(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.GOLD_LEFT

		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		//구현할 것들 Code here.
		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "금괴(좌클릭)")
	}

	override fun goldIngotRight(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.GOLD_RIGHT

		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		//구현할 것들 Code here.
		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "금괴(우클릭)")
	}

	/*---------------------------------------------------------------------------------------------*/

	override fun diamondLeft(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.DIAMOND_LEFT

		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		//구현할 것들 Code here.
		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "다이아몬드(좌클릭)")
	}

	override fun diamondRight(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.DIAMOND_RIGHT

		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		//구현할 것들 Code here.
		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "다이아몬드(우클릭)")
	}




	/*override*/ fun durationExample(event: PlayerInteractEvent)
	{
		val player = event.player

		val SKILL_TYPE = SkillType.DIAMOND_LEFT

		if(duration[SKILL_TYPE] > 0) {
			sendActionBar("${GREEN}지속시간 ${duration[SKILL_TYPE]}초 남았습니다.", player)
			return
		}
		else if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		countdownDuration(event, duration, durationInit, SKILL_TYPE, "다이아몬드(좌클릭)")
		object: BukkitRunnable() {
			override fun run() {  //지속시간 동안의 코드
				//구현할 것들  Code here.

				if(duration[SKILL_TYPE] <= 0) this.cancel()
			}
		}.runTaskTimer(getPlugin(Main::class.java), 1L, 1L)

		object: BukkitRunnable() {
			override fun run() {  //지속시간 끝났을때의 코드
				countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "다이아몬드(좌클릭)")
			}
		}.runTaskLater(getPlugin(Main::class.java), (durationInit[SKILL_TYPE]*20).toLong())  //쿨타임이 다 지나면 시작하도록

	}
}