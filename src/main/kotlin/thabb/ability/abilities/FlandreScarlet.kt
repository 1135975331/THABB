package thabb.ability.abilities

import org.bukkit.ChatColor.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import thabb.Settings.canBlocksBeDestroyed
import thabb.ability.Ability
import thabb.util.Notes.*
import thabb.util.randomRealNumInRange
import thabb.util.sendActionBar

class FlandreScarlet : Ability(
		intArrayOf(0, 0, 0, 0, 0, 0),  //현재 대기시간  cooldown
		intArrayOf(10, 20, 60, 0, 0, 5),  //설정 대기시간  cooldownInit
		intArrayOf(0, 0, 0, 0, 0, 0),  //지속시간  duration
		intArrayOf(0, 0, 0, 0, 0, 0),  //설정 대기시간  durationInit
		"FlandreScarlet",
		"모든 사물을 파괴하는 정도의 능력",
		arrayOf("${WHITE}철괴 좌클릭 : 바라보는 블럭을 파괴한다. : [대기시간 10초]",  //능력의 기술 설명  abilityDesc
				"${GOLD}금괴 좌클릭 : 레바테인을 휘두른다. : [대기시간 20초]",
				"${AQUA}다이아몬드 좌클릭 : 블럭의 눈을 눌러 블럭을 파괴한다. : [대기시간 60초]",
				"${AQUA}다이아몬드 우클릭 : 블럭의 눈을 손 위에 둔다. : [대기시간 5초]"),
		2, /*volume*/floatArrayOf(1.0f), /*instrument*/arrayOf(Sound.BLOCK_NOTE_BLOCK_HARP),
		arrayOf(floatArrayOf(A2, R, E2, R, B2, R, C3, R,  B2, R, C3, B2, A2, R, G2, R, A2, END)))
{
	override fun ironIngotLeft(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.IRON_LEFT
		val clickedBlock = event.clickedBlock
		var clickedBlockMaterial: Material? = null
		val world = player.world

		try { clickedBlockMaterial = clickedBlock?.blockData?.material }
		catch(ignored: NullPointerException) { }

		//todo 밸런스 조정 (폭발력, 위치 관련하여)
		if(event.action == Action.LEFT_CLICK_BLOCK && clickedBlockMaterial != Material.AIR) {  //공기블럭의 선택(==null)을 막기 위함
			if(cooldown[SKILL_TYPE] > 0) {  //대기시간이 남아있을 시
				sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
				return
			}

			world.createExplosion(clickedBlock!!.location.add(0.0, 1.0, 0.0), 4.0f, false, canBlocksBeDestroyed)
			countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "철괴(좌클릭)") //대기시간 카운트다운 시작
		}
	}

	override fun goldIngotLeft(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.GOLD_LEFT
		val location = player.location
		val addX = location.direction.x
		val addZ = location.direction.z

		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		val addIntX = if(Math.round(addX).toInt() == 0) -1 else 1 //0이면 -1을 넣고, 아니라면 1을 넣는다.
		val addIntZ = if(Math.round(addZ).toInt() == 0) -1 else 1
		val addIntX2 = if(Math.round(addX).toInt() == 0) -2 else 2 //0이면 -1을 넣고, 아니라면 1을 넣는다.
		val addIntZ2 = if(Math.round(addZ).toInt() == 0) -2 else 2
		val locationAlt = arrayOfNulls<Location>(9)
		/*너무 비효율적이다..*/
		locationAlt[0] = player.location.add(0.0, 0.0, 0.0)
		locationAlt[1] = player.location.add(addIntX.toDouble(), 0.0, addIntZ.toDouble())
		locationAlt[2] = player.location.add(-addIntX.toDouble(), 0.0, addIntZ.toDouble())
		locationAlt[3] = player.location.add(addIntX.toDouble(), 0.0, -addIntZ.toDouble())
		locationAlt[4] = player.location.add(-addIntX.toDouble(), 0.0, -addIntZ.toDouble())
		locationAlt[5] = player.location.add(addIntX2.toDouble(), 0.0, addIntZ2.toDouble())
		locationAlt[6] = player.location.add(-addIntX2.toDouble(), 0.0, addIntZ2.toDouble())
		locationAlt[7] = player.location.add(addIntX2.toDouble(), 0.0, -addIntZ2.toDouble())
		locationAlt[8] = player.location.add(-addIntX2.toDouble(), 0.0, -addIntZ2.toDouble())

		for(i in 0..10) {
			for(loc in locationAlt)
				loc!!.add(addX, 0.0, addZ)

			if(i < 2)
				continue  //자신의 바로 앞에 불이 생성되는 것을 막기 위함

			for(loc in locationAlt)
				loc!!.block.type = Material.FIRE
		}

		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "금괴(좌클릭)")
	}

	private var targetedBlocks = ArrayList<Block?>()
	override fun diamondLeft(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.DIAMOND_LEFT
		val world = player.world
		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}

		if(targetedBlocks.size == 0) {  //지정된 블럭이 없을때
			sendActionBar("${RED}지정된 블럭이 없습니다.", player)
			return  //그냥 여기서 함수 종료 (대기시간 카운트다운을 시작해서는 안되기 때문)
		}
		for(selectedBlock in targetedBlocks) {
			val location = selectedBlock!!.location
			val power = randomRealNumInRange(3.0, 4.0).toFloat()  //3.0 ~ 4.0 사이의 난수
			world.createExplosion(location.add(0.0, 1.0, 0.0), power, false, canBlocksBeDestroyed)
		}
		targetedBlocks.clear()  //지정되었던 블럭을 리스트에서 제거


		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "다이아몬드(좌클릭)")
	}

	override fun diamondRight(event: PlayerInteractEvent)
	{
		val player = event.player
		val SKILL_TYPE = SkillType.DIAMOND_RIGHT
		val clickedBlock = event.clickedBlock  //이벤트와 관련된 클릭된 블럭을 가져온다
		val playerAction = event.action

		if(cooldown[SKILL_TYPE] > 0) {
			sendActionBar("${RED}재사용까지 ${cooldown[SKILL_TYPE]}초 남았습니다.", player)
			return
		}
		else if(playerAction == Action.RIGHT_CLICK_AIR) {
			return  //그냥 여기서 함수 종료 (대기시간 카운트다운을 시작해서는 안되기 때문)
		}
		else if(targetedBlocks.size >= 5) {
			sendActionBar("${RED}더 이상 지정할 수 없습니다.", player)
			return  //그냥 여기서 함수 종료 (대기시간 카운트다운을 시작해서는 안되기 때문)
		}

		targetedBlocks.add(clickedBlock)  //블럭을 리스트에 추가
		sendActionBar("${BLUE}${targetedBlocks.size}번째 블럭이 지정되었습니다.", player)

		countdownCooldown(event, cooldown, cooldownInit, SKILL_TYPE, "다이아몬드(우클릭)")
	}
}