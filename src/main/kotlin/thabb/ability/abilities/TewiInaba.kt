package thabb.ability.abilities

import thabb.ability.Ability
import thabb.Main
import thabb.util.Notes.*
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin.getPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.*
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.entity.ArmorStand
import org.bukkit.event.block.Action
import java.lang.NullPointerException
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Entity
import thabb.util.*

class TewiInaba : Ability(
		intArrayOf(0, 0, 0, 0, 0, 0),  //현재 대기시간  cooldown
		intArrayOf(30, 30, 0, 0, 0, 30),  //설정 대기시간  cooldownInit
		intArrayOf(0, 0, 0, 0, 0, 0),  //지속시간  duration
		intArrayOf(0, 0, 0, 0, 0, 0),  //설정 대기시간  durationInit
		"TewiInaba",
		"행운을 주는 정도의 능력",
		arrayOf("${GREEN}패시브 : 신속 1, 점프강화 1, 행운 1를 항상 가지고 있는다.",
				"${WHITE}철괴 좌클릭 : 확률에 따라 광물을 1개 ~ 3개 얻는다.  [대기시간 30초]  " +
						"\n(단, 레드스톤과 에메랄드는 나오지 않는다." +
						"\n아이템이 돌어올 수 있는 빈 아이템 칸이 적어도 하나 이상 있어야 한다.)",  //능력의 기술 설명  abilityDesc
				"${GOLD}금괴 좌클릭 : 가장 가까이 있는 상대 하나에게 무작위의 해로운 포션효과를 준다.  [대기시간 30초]",
				"${GOLD}금괴 우클릭 : 자신에게 무작위의 이로운 포션효과를 건다.  [대기시간 30초]  " +
						"\n(단, 지속시간은 확률에 비례, 강도는 지속시간에 반비례한다. " +
						"\n절대 부여되지 않는 포션효과도 있다. " +
						"\n${RED}매우 낮은 확률로 자신에게 해로운 효과, " +
						"\n${RED}상대에게 이로운 효과가 부여될 수 있다. $GOLD" +
						"\n좌/우클릭의 쿨타임이 공유된다.)",
				"${AQUA}다이아몬드 좌클릭 : 주변의 만든 함정을 제거한다.  [대기시간 없음]",  //TODO 이거 없애기
				"${AQUA}다이아몬드 우클릭 : 함정을 만든다. [대기시간 30초]" +
				        "(단, 함정은 최대 3개 까지만 만들 수 있다." +
				        "자신이 판 함정에 자신이 빠질 수 있다.)",
				"${AQUA}웅크리기 + 다이아몬드 우클릭 : 자신이 판 함정의 위치를 본다. [대기시간 없음]"),
		2, /*volume*/floatArrayOf(1.0f), /*instrument*/arrayOf(Sound.BLOCK_NOTE_BLOCK_HARP),
		arrayOf(floatArrayOf(D2, R, A1, D2, A1, R, D2, R,  E2, R, B1, E2, B1, R, E2, R,  F2, R, G2, R, A2, R, F2, R,  D2, END)))
{
	override fun passiveEffect(player: Player)
	{
		val speed = PotionEffect(SPEED, 9999999, 0, false, false, true)
		val jump = PotionEffect(JUMP, 9999999, 0, false, false, true)
		val luck = PotionEffect(LUCK, 9999999, 0, false, false, true)

		player.addPotionEffect(speed)
		player.addPotionEffect(jump)
		player.addPotionEffect(luck)
	}

	override fun ironIngotLeft(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.IRON_LEFT


		if(cooldown[SKILL_TYPE] > 0) {  //대기시간이 남아있을 시
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		if(isInventoryFull(player)) {  //인벤토리가 가득할때
			sendActionBar("${RED}인벤토리가 가득 찼습니다.", player)
			object: BukkitRunnable() {
				override fun run() {
					sendActionBar("사용하려면 인벤토리 한 칸을 비워주세요.", player)
				}
			}.runTaskLater(getPlugin(Main::class.java), 20L)

			return
		}
		val ore = when(randomIntInRange(0, 100)) {  //광물 결정
			in 0..25   -> Material.COAL          //25%  석탄
			in 26..50  -> Material.LAPIS_LAZULI  //25%  청금석
			in 51..70  -> Material.IRON_INGOT    //20%  철괴
			in 71..90  -> Material.GOLD_INGOT    //20%  금괴
			else       -> Material.DIAMOND       //10%  다이아몬드
		}

		val amount = when(randomIntInRange(0, 100)) {  //수량 결정
			in 0..75  -> 1  //75%
			in 76..95 -> 2  //20%
			else      -> 3  //5%
		}

		player.inventory.addItem(ItemStack(ore, amount))  //플레이어의 인벤토리에 아이템 추가
		player.sendMessage("${AQUA}${ore.name} ${amount}개를 얻었습니다.")

		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "철괴(좌클릭)") //대기시간 카운트다운 시작
	}

	/*---------------------------------------------------------------------------------------------*/

	private val benificalEffects = arrayOf(SPEED, INCREASE_DAMAGE, JUMP, REGENERATION, DAMAGE_RESISTANCE, FIRE_RESISTANCE, INVISIBILITY, NIGHT_VISION, HEALTH_BOOST, ABSORPTION)
	private val harmfulEffects = arrayOf(SLOW, CONFUSION, BLINDNESS, HUNGER, WEAKNESS, POISON, WITHER, LEVITATION)
	override fun goldIngotRight(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.GOLD_LEFT  //스킬 쿨타임은 좌클릭과 공유한다.

		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

			val isHarmful = when(randomIntInRange(0, 100)) {
				in 0..3 -> true
				else    -> false
			}

		val potionEffect = if(isHarmful)
				harmfulEffects[randomIntInRange(0, harmfulEffects.size-1)]
			else
				benificalEffects[randomIntInRange(0, benificalEffects.size-1)]

		val durationTick = when(randomIntInRange(0, 100)) {
			in 0..25  -> randomIntInRange(60, 200)    //25%  3~10sec
			in 26..50 -> randomIntInRange(100, 400)   //25%  5~20sec
			in 51..75 -> randomIntInRange(100, 600)   //25%  5~30sec
			else      -> randomIntInRange(200, 800)   //25%  10~40sec
		}
		val durationSec = roundFrom(durationTick/20.0, 2)  //durationTick/20.0을 소수 2번째 자리에서 반올림

		val amplifier = when(durationTick) {
			in 0..7   -> randomIntInRange(3, 4)
			in 8..12  -> randomIntInRange(2, 3)
			in 13..15 -> 2
			in 16..20 -> randomIntInRange(1, 2)
			else      -> 1
		}

		player.addPotionEffect(PotionEffect(potionEffect, durationTick, amplifier, false, false, true))
		player.sendMessage("${translateToKor(potionEffect.name)}(${amplifier+1}단계) ${durationSec}초를 얻었습니다.")

		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "금괴")
	}

	override fun goldIngotLeft(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.GOLD_LEFT

		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		val targetEntity = findNearestLivingEntity(player, 5.0, 5.0, 5.0)
		if(targetEntity.isEmpty)
			sendActionBar("${RED}주변에 엔티티가 없습니다.", player)


		val isBenifical = when(randomIntInRange(0, 100)) {
			in 0..3 -> true
			else    -> false
		}

		val potionEffect = if(isBenifical)
			benificalEffects[randomIntInRange(0, benificalEffects.size-1)]
		else
			harmfulEffects[randomIntInRange(0, harmfulEffects.size-1)]

		val durationTick = when(randomIntInRange(0, 100)) {
			in 0..25  -> randomIntInRange(60, 200)    //25%  3~10sec
			in 26..50 -> randomIntInRange(100, 400)   //25%  5~20sec
			in 51..75 -> randomIntInRange(100, 600)   //25%  5~30sec
			else      -> randomIntInRange(200, 800)   //25%  10~40sec
		}
		val durationSec = roundFrom(durationTick/20.0, 2)  //durationTick/20.0을 소수 2번째 자리에서 반올림

		val amplifier = when(durationTick) {
			in 0..7   -> randomIntInRange(3, 4)
			in 8..12  -> randomIntInRange(2, 3)
			in 13..15 -> 2
			in 16..20 -> randomIntInRange(1, 2)
			else      -> 1
		}

		targetEntity.addPotionEffect(PotionEffect(potionEffect, durationTick, amplifier, false, false, true))
		player.sendMessage("상대에게 ${translateToKor(potionEffect.name)}(${amplifier+1}단계) ${duration}초를 주었습니다.")
		targetEntity.sendMessage("상대에게서 ${translateToKor(potionEffect.name)}(${amplifier+1}단계) ${durationSec}초를 받았습니다.")


		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "금괴")
	}

	/*---------------------------------------------------------------------------------------------*/

	//아머스탠드를 설치해 뒀다가 누군가가 건드리면 발동되도록, 이벤트를 사용할 수도?
	var trapActivator = ArrayList<ArmorStand?>()
	override fun diamondLeft(event: PlayerInteractEvent)
	{
		val player = event.player
		val world = player.world
		val SKILL_TYPE = SkillType.DIAMOND_LEFT

		val nearbyActivator = world.getNearbyEntities(player.location, 5.0, 5.0, 5.0)
		nearbyActivator.removeIf{ entity -> entity !is ArmorStand || "trapActivator" !in entity.scoreboardTags }


		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}
		else if(nearbyActivator.isEmpty()) {
			sendActionBar("${RED}함정이 주변에 없습니다.", player)
			return
		}

		for(activator in nearbyActivator) {
			val activator = activator as ArmorStand
			activator.remove()
		}
		sendActionBar("${BLUE}주변의 함정을 제거했습니다.", player)

		//쿨타임 없음
	}

	override fun diamondRight(event: PlayerInteractEvent)
	{
		val player = event.player
		val world = player.world
		val playerAction = event.action
		val clickedBlock = event.clickedBlock
		val SKILL_TYPE = SkillType.DIAMOND_RIGHT


		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}
		else if(trapActivator.size >= 3) {
			sendActionBar("${RED}더 이상 함정을 만들 수 없습니다.", player)
			return
		}
		else if(playerAction == Action.RIGHT_CLICK_AIR) {
			return
		}
		else if(player.isSneaking) {  //함정 설치
			sendActionBar("${YELLOW}함정을 설치중입니다...", player)
			player.addPotionEffect(PotionEffect(SLOW, 99999, 128, true, false, false))
			player.addPotionEffect(PotionEffect(SLOW_DIGGING, 99999, 128, true, false, false))
			player.addPotionEffect(PotionEffect(JUMP, 99999, 248, true, false, false))

			object : BukkitRunnable() {
				override fun run() {
					player.removePotionEffect(SLOW)
					player.removePotionEffect(SLOW_DIGGING)
					player.removePotionEffect(JUMP)

					val activator = world.spawn(clickedBlock!!.location.add(0.5, 0.0, 0.5), ArmorStand::class.java)
					activator.isVisible = true
					activator.isInvulnerable = true
					activator.addScoreboardTag("trapActivator")
					trapActivator.add(activator)

					sendActionBar("${BLUE}${trapActivator.size}번째 함정을 만들었습니다.", player)

					countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "다이아몬드(우클릭)")
				}
			}.runTaskLater(getPlugin(Main::class.java), 100L)
		}
		else {
			var i = 0
			try {
				for(stand in trapActivator) {
					i++
					player.sendMessage("#${i} : X:${stand!!.location.x} Y:${stand.location.y} Z:${stand.location.z}")
				}
			}
			catch(npE: NullPointerException) {
				sendActionBar("${RED}만든 함정이 없습니다.", player)
			}
		}
	}

	fun activateTrap(activator: Entity)
	{
		val world = activator.world
		val activatorLoc = activator.location

		val width = randomIntInRange(1,4)
		val height = randomIntInRange(3, 9)  //싱크홀 함정의 경우, 체력 0~6정도의 낙하대미지
		val range = arrayOf(width, height, width)  //X, Depth, Z

		//TODO 무브이벤트를 통해 플레이어 불러와서 함정걸린 메세지 출력하기
		when(randomIntInRange(1,2)) {
			1 -> {  //싱크홀
				val startLocation = Location(world, activatorLoc.x - range[0], activatorLoc.y - range[1], activatorLoc.z - range[2])

				for(depth in 0..range[1])
					for(x in 0..range[0] * 2)
						for(z in 0..range[2] * 2) {
							val currentLoc = Location(world, startLocation.x + x, startLocation.y + depth, startLocation.z + z)
							
							if(activatorLoc == currentLoc)
								continue
							else if(depth == range[1])
								world.spawnParticle(Particle.BLOCK_CRACK, currentLoc, 100, currentLoc.block.blockData)
							
							currentLoc.block.type = Material.AIR   //world.getBlockAt(currentLoc).type
						}
				}

			2 -> {  //모래/자갈 질식
				activatorLoc.add(0.0, 3.0, 0.0).block.type = Material.OAK_PLANKS
				val startLocation = Location(world, activatorLoc.x - range[0], activatorLoc.y + 6.0, activatorLoc.z - range[2])

				for(depth in 0..range[1])
					for(x in 0..range[0] * 2)  //현재위치를 포함하기 위해 +1
						for(z in 0..range[2] * 2) {
							val fallingBlock = when(randomIntInRange(1, 2)) {
								1    -> Material.GRAVEL
								else -> Material.SAND
							}

							val currentLoc = Location(world, startLocation.x + x, startLocation.y + depth, startLocation.z + z)
							world.spawnParticle(Particle.BLOCK_CRACK, currentLoc, 50, fallingBlock.createBlockData())
							currentLoc.block.type = fallingBlock
						}
			}
		}
	}
}

