package thabb.ability.abilities

import thabb.ability.Ability
import thabb.Main
import thabb.Settings
import thabb.util.sendActionBar
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.ChatColor.*
import thabb.util.Notes.*
import org.bukkit.Sound
import org.bukkit.entity.SmallFireball
import org.bukkit.plugin.java.JavaPlugin.getPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.player.PlayerMoveEvent

class MarisaKirisame : Ability(
		intArrayOf(0, 0, 0, 0, 0, 0),  //현재 대기시간  cooldown
		intArrayOf(30, 15, 0, 0, 0, 0),  //설정 대기시간  cooldownInit
		intArrayOf(0, 0, 0, 0, 0, 0),  //지속시간  duration
		intArrayOf(15, 0, 10, 0, 0, 0),  //설정 대기시간  durationInit
		"MarisaKirisame",
		"마법을 사용하는 정도의 능력",
		arrayOf("${WHITE}철괴 좌클릭 : ${GRAY}${STRIKETHROUGH}(빗자루로) ${RESET}잠시동안 날아다닌다.  [지속시간 15초, 대기시간 30초]\n  " +
					"(단, 효과가 끝나면 ${RED}비행이 풀리므로 낙하 대미지에 주의${WHITE}할 것.)",  //능력의 기술 설명  abilityDesc
				"${GOLD}금괴 좌클릭 : 화염구를 발사한다.  [대기시간 20초]",
				"${AQUA}다이아몬드 좌클릭 : 상대에게 돌진하여 대미지를 입힌다.  [지속시간 10초, 대기시간 30초]\n  " +
						"(단, 날고 있는 경우에만 대미지를 입힐 수 있다.)"),
		2, /*volume*/floatArrayOf(1.0f, 0.8f), /*instrument*/arrayOf(Sound.BLOCK_NOTE_BLOCK_HARP, Sound.BLOCK_NOTE_BLOCK_BASS),
		arrayOf(floatArrayOf(A2, R, B2, R, C3, R, R, R,  B2, R, C3, R, D3, R, E3, R,  B2, C3, B2, C3, G2, R, E2, R,  A2, END)))

{
	override fun ironIngotLeft(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.IRON_LEFT

		if(duration[SKILL_TYPE] > 0) {  //쿨타임이 다 지나면 시작하도록
			sendActionBar("${GREEN}지속시간 ${duration[SKILL_TYPE]}초 남았습니다.", player)
			return
		}
		else if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		countdownDuration(event, duration, durationInit, SKILL_TYPE, "철괴(좌클릭)")
		player.allowFlight = true

		object: BukkitRunnable() {
			override fun run() {
				player.isFlying = false
				player.allowFlight = false
				countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "철괴(좌클릭)")
			}
		}.runTaskLater(getPlugin(Main::class.java), (durationInit[SKILL_TYPE]*20).toLong())  //쿨타임이 다 지나면 시작하도록
	}

	/*---------------------------------------------------------------------------------------------*/

	override fun goldIngotLeft(event: PlayerInteractEvent)
	{
		val player = event.player
		val world = player.world
		val SKILL_TYPE = SkillType.GOLD_LEFT

		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		var shotCount = 0
		object : BukkitRunnable() {
			override fun run() {
				if(shotCount >= 30)
					this.cancel()

				val fireball = world.spawn(player.eyeLocation, SmallFireball::class.java)
				fireball.direction = player.eyeLocation.direction
				fireball.setIsIncendiary(false)
				//fireball.yield = 3f  //yield == power
				player.playSound(fireball.location, Sound.ENTITY_BLAZE_SHOOT, 1f, 1.2f)
				shotCount++
			}
		}.runTaskTimer(getPlugin(Main::class.java), 10, 2)

		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "금괴(좌클릭)")
	}

	/*---------------------------------------------------------------------------------------------*/

	var isActivatedDiamondLeft = false  //EventManager onPlayerMove에서 사용
	var twoDimVelocity = 0.0  //headbutt 에서 사용
	override fun diamondLeft(event: PlayerInteractEvent)
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
		isActivatedDiamondLeft = true
		player.allowFlight = true
		var location1: Location
		var location2: Location = player.location

		object: BukkitRunnable() {
			override fun run() {  //지속시간 동안의 코드
				location1 = location2
				location2 = player.location
				twoDimVelocity = Math.sqrt(Math.pow((location1.x - location2.x), 2.0) + Math.pow((location1.z - location2.z), 2.0))  //두 점 사이의 거리 공식

				if(duration[SKILL_TYPE] <= 0) this.cancel()
			}
		}.runTaskTimer(getPlugin(Main::class.java), 1L, 1L)

		object: BukkitRunnable() {
			override fun run() {  //지속시간 끝났을때의 코드
				isActivatedDiamondLeft = false
				player.allowFlight = false
				player.isFlying = false
				countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "다이아몬드(좌클릭)")
			}
		}.runTaskLater(getPlugin(Main::class.java), (durationInit[SKILL_TYPE]*20).toLong())  //쿨타임이 다 지나면 시작하도록
	}

	fun explosivePotion(splashEvent: PotionSplashEvent)
	{
		val potion = splashEvent.potion
		val shooter = potion.shooter as Player
		val world = shooter.world

		world.spawnParticle(Particle.PORTAL, potion.location, 200, 0.0, 0.0, 0.0, 2.0)
		object : BukkitRunnable() {
			override fun run() {
				world.strikeLightningEffect(potion.location)
				world.createExplosion(potion.location, 3.0f, false, Settings.canBlocksBeDestroyed)
			}
		}.runTaskLater(getPlugin(Main::class.java), 55L)
	}

	fun headbutt(moveEvent: PlayerMoveEvent)
	{
		val player = moveEvent.player
		val world = player.world

		val closeEntities = world.getNearbyEntities(player.location, 0.5, 0.5, 0.5)
		closeEntities.remove(player)

		if(closeEntities.isEmpty()) return

		for(entity in closeEntities) {
			if(entity is LivingEntity && twoDimVelocity > 0.45) { //todo 상대의 방어력을 고려한 데미지로 변경
				entity.damage(15.0 * (Math.pow((twoDimVelocity), 2.0)/2.0))  //그 엔티티에 현재 속도 비례 대미지, 최대 약16.32(15* 약1.088)대미지  todo 공식 변경

				print("\nVelocity: ${twoDimVelocity} \nFinalVelocity: ${Math.pow((twoDimVelocity), 2.0)/2.0} \nDamaged: ${15.0 * (Math.pow((twoDimVelocity), 2.0)/2.0)} ")  //디버그 전용
			}
		}
	}
}

	/*
	* print("")
	print(location1)
	print(location2)
	print("3D Dist: ${location1.distance(location2)}")
	print("2D Dist: ${twoDimDistance}")
	*/