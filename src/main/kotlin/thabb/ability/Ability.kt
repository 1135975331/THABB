package thabb.ability

import thabb.Main
import thabb.util.sendActionBar
import org.bukkit.ChatColor.*
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.plugin.java.JavaPlugin.getPlugin
import org.bukkit.scheduler.BukkitRunnable

abstract class Ability(
		var cooldown: IntArray,  //각각 철괴, 금괴, 다이아몬드의 쿨타임 (실시간 쿨타임 값)
		var cooldownInit: IntArray,  //각각의 설정된 쿨타임  (상수값)
		var duration: IntArray,  //각각 철괴, 금괴, 다이아몬드의 지속시간 (실시간 지속시간 값)
		var durationInit: IntArray,  //각각의 설정된 지속시간  (상수값)

		var abilityID: String,  //능력 아이디
		var abilityName: String,  //능력 이름
		var abilityDesc: Array<String>,  //능력 설명

		var period: Long,
		var volume: FloatArray,
		var instrument: Array<Sound>,
		var score: Array<FloatArray>
) {
	var alreadyTaken = false


	fun passiveSkill(event: ProjectileLaunchEvent) {}

	fun passiveSkill(event: ProjectileHitEvent) {}

	fun passiveSkill(event: PlayerRespawnEvent) {}

	fun passiveSkill(event: EntityDamageEvent) {}

	fun passiveSkill(event: BlockExplodeEvent) {}


	open fun passiveEffect(player: Player) {}

	open fun ironIngotLeft(event: PlayerInteractEvent) {} //오버라이딩

	open fun goldIngotLeft(event: PlayerInteractEvent) {}

	open fun diamondLeft(event: PlayerInteractEvent) {}

	open fun ironIngotRight(event: PlayerInteractEvent) {} //오버라이딩

	open fun goldIngotRight(event: PlayerInteractEvent) {}

	open fun diamondRight(event: PlayerInteractEvent) {}


	//todo 능력을 사용하는 플레이어 리턴
	fun countdownCooldown(event: PlayerInteractEvent, cooldown: IntArray, cooldownInit: IntArray, SKILL_TYPE: Int, abilityType: String)
	{
		val player = event.player

		cooldown[SKILL_TYPE] = cooldownInit[SKILL_TYPE] //쿨타임이 0인 상태였으니 설정된 쿨타임을 더해 쿨타임 리셋

		object : BukkitRunnable() {
			override fun run() {  //BukkitRunnable로 타이머를 생성
				when {
					cooldown[SKILL_TYPE] >= 4 -> {
						cooldown[SKILL_TYPE]-- //지속시간을 1씩 감소시킨다
					}
					cooldown[SKILL_TYPE] > 0 -> {  //쿨타임이 0보다 크면 (3 2 1 카운트다운)
						sendActionBar("${RED}[대기시간] $abilityType : ${cooldown[SKILL_TYPE]}초 남았습니다...", player)
						cooldown[SKILL_TYPE]-- //쿨타임을 1씩 감소시킨다
						//System.out.println(abilityType + " : " + cooldown[SKILL_TYPE]);  //디버그 전용
					}
					else -> {  //그렇지 않다면
						cancel() //BukkitRunnable을 중단시킨다.
						sendActionBar("${GREEN}${abilityType}을(를) 사용할 수 있습니다.", player) //<아이템> 능력을 사용할 수 있음을 알린다.
					}
				}
			}
		}.runTaskTimer(getPlugin(Main::class.java), 0L, 20L) //20틱(1초)의 간격으로 실행한다.
	}

	fun countdownDuration(event: PlayerInteractEvent, duration: IntArray, durationInit: IntArray, SKILL_TYPE: Int, abilityType: String)
	{
		val player = event.player

		duration[SKILL_TYPE] = durationInit[SKILL_TYPE] //지속시간이 0인 상태였으니 설정된 지속시간으로 만들어 지속시간 리셋

		object : BukkitRunnable() {
			override fun run() {  //BukkitRunnable로 타이머를 생성
				when {
					duration[SKILL_TYPE] >= 4 -> {
						duration[SKILL_TYPE]-- //지속시간을 1씩 감소시킨다
					}
					duration[SKILL_TYPE] > 0 -> {  //지속시간이 0보다 크면  (3 2 1 카운트다운)
						sendActionBar("${GREEN}[지속시간] $abilityType : ${duration[SKILL_TYPE]}초 남았습니다...", player)
						duration[SKILL_TYPE]-- //지속시간을 1씩 감소시킨다
						//System.out.println(abilityType + " : " + duration[SKILL_TYPE]);
					}
					else -> {  //그렇지 않다면
						cancel() //BukkitRunnable을 중단시킨다.
						sendActionBar("${RED}${abilityType}의 효과가 끝났습니다.", player) //<아이템> 능력을 사용할 수 있음을 알린다.
					}
				}
			}
		}.runTaskTimer(getPlugin(Main::class.java), 0L, 20L) //20틱(1초)의 간격으로 실행한다.
	}

	//public static JavaPlugin Plugin = asdf1135975331.thabb.Main.getPlugin(Main.class);
/*
	init
	{
		this.cooldown = cooldown
		this.cooldownInit = cooldownInit
		this.duration = duration
		this.durationInit = durationInit

		this.abilityName = abilityName
		this.abilityDesc = abilityDesc
		this.abilityID = abilityID

		this.score = score
	}
 */
}