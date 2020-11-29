package thabb.ability.abilities

import thabb.ability.Ability
import thabb.Main
import thabb.util.Notes.*
import thabb.util.randomRealNumInRange
import thabb.util.randomRealNumInRangeWithOffset
import thabb.util.randomIntInRange
import thabb.util.sendActionBar
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.ChatColor.*
import org.bukkit.plugin.java.JavaPlugin.getPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import java.lang.ClassCastException

class YukariYakumo : Ability(
		intArrayOf(0, 0, 0, 0, 0, 0),  //현재 대기시간  cooldown
		intArrayOf(25, 20, 40, 5, 1, 0),  //설정 대기시간  cooldownInit
		intArrayOf(0, 0, 0, 0, 0, 0),  //지속시간  duration
		intArrayOf(0, 0, 10, 0, 0, 0),  //설정 대기시간  durationInit
		"YukariYakumo",
		"경계를 조종하는 정도의 능력",
		arrayOf("${WHITE}철괴 좌클릭 : 틈새를 이용해 지정한 위치로 이동한다.  [대기시간 25초]",  //능력의 기술 설명  abilityDesc
				"${WHITE}철괴 우클릭 : 현재 위치를 이동할 위치로 지정한다.  [대기시간 5초]\n   단, 틈새를 이용하여 이동한 뒤 지정된 위치는 사라진다.",
				"${GOLD}금괴 좌클릭 : 지정된 엔티티를 틈새에 빠뜨려 하늘에서 떨어지게 한다.  [대기시간 15초]\n   떨어지는 높이는 확률에 비례한다.",
				"${GOLD}금괴 우클릭 : 틈새에 빠뜨릴 엔티티를 정한다.  [대기시간 1초]\n   주변의 엔티티가 랜덤으로 정해지며 언제든 다시 정할 수 있다.",
				"${AQUA}다이아몬드 좌클릭 : 틈새를 열어 화살을 쏟아 붓는다.  [지속시간 10초, 대기시간 40초]"),
		3, /*volume*/floatArrayOf(1.0f, 0.8f), /*instrument*/arrayOf(Sound.BLOCK_NOTE_BLOCK_HARP, Sound.BLOCK_NOTE_BLOCK_BASS),
		arrayOf(floatArrayOf(F2, R, E2, R, D2, R, E2, F2,  R, R, E2, R, D2, R, E2, FS2, END)/*, floatArrayOf(END)*/))

{
	private var targetedLocation: Location? = null //지정된 블럭
	override fun ironIngotLeft(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.IRON_LEFT


		if(cooldown[SKILL_TYPE] > 0) {  //대기시간이 남아있을 시
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}
		else if(targetedLocation == null) {
			sendActionBar("${RED}이동할 위치가 지정되지 않았습니다.", player)
			return  //다음 명령이 실행되지 않도록 바로 종료
		}

		summonGap(player)
		gapIn(player)

		object : BukkitRunnable() {  //틈새 이동완료, 부여되었던 포션 효과를 모두 없앰
			override fun run() {
				player.sendMessage("이동하였습니다.")  //이동완료
				player.teleport(targetedLocation!!)

				removeGapEffects(player)  //포선효과 제거

				targetedLocation = null  //지정된 이동할 위치 삭제
			}
		}.runTaskLater(getPlugin(Main::class.java), 70L)

		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "철괴(좌클릭)") //대기시간 카운트다운 시작
	}

	override fun ironIngotRight(event: PlayerInteractEvent)  //위치 지정
	{
		val player = event.player
		val SKILL_TYPE = SkillType.IRON_RIGHT

		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		targetedLocation = player.location
		sendActionBar("${BLUE}위치가 지정되었습니다.", player)

		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "철괴(우클릭)")
	}

	/*---------------------------------------------------------------------------------------------*/

	private var targetEntity: LivingEntity? = null
	override fun goldIngotLeft(event: PlayerInteractEvent)  //화살로 맞힌 상대를 공중에서 떨굼
	{
		val player = event.player
		val SKILL_TYPE = SkillType.GOLD_LEFT

		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}
		else if(targetEntity == null) {
			sendActionBar("${RED}지정된 엔티티가 없습니다.", player)
			return
		}

		summonGap(player)
		gapIn(targetEntity!!)

		object : BukkitRunnable() {
			override fun run() {
				targetEntity!!.teleport(targetEntity!!.location.add(randomRealNumInRange(0.0, 5.0), fallingHeight()/*line 165*/, randomRealNumInRange(0.0, 5.0)))
				removeGapEffects(targetEntity!!)
				removeGapEffects(player)
				targetEntity = null  //설정한 타겟 초기화
			}
		}.runTaskLater(getPlugin(Main::class.java), 70L)

		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "금괴(좌클릭)")

	}

	override fun goldIngotRight(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.GOLD_RIGHT
		val world = player.world

		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		val nearbyEntities = world.getNearbyEntities(player.location, 7.5, 7.5, 7.5)  //주변 엔티티들을 가져옴

		iterate@ while(nearbyEntities.size != 1) {
			var i = 0
			val randNum = randomIntInRange(0, nearbyEntities.size)
			for(entity in nearbyEntities) {
				try { targetEntity = entity as LivingEntity }
				catch(ccE: ClassCastException) { i++ ; continue }

				if(entity != event.player && i == randNum)
					break@iterate

				targetEntity = null
				i++
			}
			//주변 엔티티들 중 무작위로 가져옴
		}

		if(targetEntity == null) {
			sendActionBar("${RED}주변에 엔티티가 없습니다.", player)
			return
		}
		else {
			player.sendMessage("${BLUE}타겟이 ${targetEntity!!.name}(으)로 지정되었습니다.")
		}

		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "금괴(우클릭)")
	}


	/*---------------------------------------------------------------------------------------------*/

	override fun diamondLeft(event: PlayerInteractEvent)  //비광충 네스트
	{
		val player = event.player
		val world = player.world

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
			override fun run() {
				val shootLocation = player.location
				val x = shootLocation.direction.x
				val y = shootLocation.direction.y
				val z = shootLocation.direction.z

				//player.sendMessage("$x $y $z")  //디버그 전용
				shootLocation.add(randomRealNumInRangeWithOffset(-2.5, -1.0, 0.0, 2.5) *z, randomRealNumInRange(0.2, 2.5), randomRealNumInRangeWithOffset(-2.5, -1.0, 0.0, 2.5) *x)

				world.spawnArrow(shootLocation, player.location.direction, randomRealNumInRange(1.0, 2.0).toFloat(), 0.0f)

				if(duration[SKILL_TYPE] <= 0) this.cancel()
			}
		}.runTaskTimer(getPlugin(Main::class.java), 20L, 1L)

		object: BukkitRunnable() {
			override fun run() {
				countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "다이아몬드(좌클릭)")
			}
		}.runTaskLater(getPlugin(Main::class.java), (durationInit[SKILL_TYPE]*20).toLong())  //쿨타임이 다 지나면 시작하도록

	}

	/*---------------------------------------------------------------------------------------------*/

	private fun summonGap(entity: LivingEntity)  //틈새 소환할때의 함수
	{
		//포션효과 : 구속, 점프강화(점프방지)  (틈새 소환)
		entity.sendMessage("틈새 소환중...")

		val slowness = PotionEffect(PotionEffectType.SLOW, 999, 100, true, false)
		val antiJump = PotionEffect(PotionEffectType.JUMP, 999, 249, true, false)

		entity.addPotionEffect(slowness)
		entity.addPotionEffect(antiJump)
	}

	private fun gapIn(entity: LivingEntity)
	{
		object : BukkitRunnable() {  //포션효과 : 실명(틈새 들어가는 중)
			override fun run() {
				val blindness = PotionEffect(PotionEffectType.BLINDNESS, 999, 0, true, false)
				entity.addPotionEffect(blindness)
			}
		}.runTaskLater(getPlugin(Main::class.java), 30L)
	}

	private fun removeGapEffects(entity: LivingEntity)  //틈새에서 나오면서 효과제거
	{
		entity.removePotionEffect(PotionEffectType.SLOW)  //포션 효과 제거
		entity.removePotionEffect(PotionEffectType.JUMP)
		entity.removePotionEffect(PotionEffectType.BLINDNESS)
	}

	private fun fallingHeight(): Double
	{
		return when(randomIntInRange(0, 100)) {
			in 0..50   -> randomRealNumInRange(7.0, 9.0)  //데미지 4 ~ 6, 확률 50%
			in 51..80  -> randomRealNumInRange(9.0, 11.0)  //데미지 6 ~ 8, 확률 30%
			in 81..95  -> randomRealNumInRange(11.0, 13.0)  //데미지 8 ~ 10, 확률 15%
			else/*96..100*/ -> randomRealNumInRange(13.0, 15.0)  //데미지 10 ~ 12, 확률 5%
		}
	}
}