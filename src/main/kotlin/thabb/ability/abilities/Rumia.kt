package thabb.ability.abilities

import thabb.ability.Ability
import thabb.Main
import thabb.util.sendActionBar
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.ChatColor.*
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Sound
import thabb.util.Notes.*
import org.bukkit.plugin.java.JavaPlugin.getPlugin
import org.bukkit.scheduler.BukkitRunnable

class Rumia : Ability(
		intArrayOf(0, 0, 0, 0, 0, 0),  //현재 대기시간  cooldown
		intArrayOf(30, 60, 0, 0, 0, 0),  //설정 대기시간  cooldownInit
		intArrayOf(0, 0, 0, 0, 0, 0),  //지속시간  duration
		intArrayOf(20, 10, 0, 0, 0, 0),  //설정 대기시간  durationInit
		"Example",
		"암흑을 조종하는 정도의 능력",
		arrayOf("${WHITE}철괴 좌클릭 : 자신의 주변에 암흑을 두어 어둡게 한다. (대기시간 30초)",  //능력의 기술 설명  abilityDesc
				"${GOLD}금괴 좌클릭 : 상대방의 시야에 암흑을 두어 아무것도 보이지 않게 한다. (대기시간 60초)",
				"${AQUA}다이아몬드 좌클릭 : 미정"),
		2, /*volume*/floatArrayOf(1.0f, 0.8f), /*instrument*/arrayOf(Sound.BLOCK_NOTE_BLOCK_HARP, Sound.BLOCK_NOTE_BLOCK_BASS),
		arrayOf(floatArrayOf(A2, R, C3, R, B2, A2, G2, R, A2, R, R, R,  E2, R, G2, R, A2, END)))
{
	override fun ironIngotLeft(event: PlayerInteractEvent)
	{
		val player = event.player
		val world = player.world
		val playerLocation = player.location
		val SKILL_TYPE = SkillType.IRON_LEFT

		if(cooldown[SKILL_TYPE] > 0) {  //대기시간이 남아있을 시
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		this.countdownDuration(event, this.duration, this.durationInit, SKILL_TYPE, "철괴(좌클릭)")

		object : BukkitRunnable() {
			override fun run() {
				if(duration[SKILL_TYPE] <= 0)
					this.cancel()

				val darkness = Particle.DustOptions(Color.BLACK, 20f)
				world.spawnParticle(Particle.FALLING_DUST, playerLocation, 100, 10.0, 10.0, 10.0, 1.0)
				world.spawnParticle(Particle.LEGACY_FALLING_DUST, playerLocation, 100, 10.0, 10.0, 10.0)
			}
		}.runTaskTimer(getPlugin(Main::class.java), 5L, 0L)

		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "철괴(좌클릭)") //대기시간 카운트다운 시작
	}

	override fun goldIngotLeft(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.GOLD_LEFT

		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		//todo 타인을 때려서 발동시켜야 하므로 EntityDamagedByEntityEvent를 활용한 새로운 함수를 만들기 or 가장 가까운 엔티티(플레이어 우선) 타겟
		//구현할 것들 Code here.
		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "금괴(좌클릭)")
	}

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
}