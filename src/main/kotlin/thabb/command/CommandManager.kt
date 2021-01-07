package thabb.command

import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandException
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import thabb.Main
import thabb.Settings
import thabb.Settings.canBlocksBeDestroyed
import thabb.ability.Ability
import thabb.ability.AbilityList
import thabb.util.Notes
import thabb.util.playNotes
import thabb.util.randomIntInRange
import thabb.util.sendActionBar

class CommonCommand(private var plugin: Main) : CommandExecutor
{
	override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean
	{
		if(command.name.equals("ability", ignoreCase = true)) {
			if(args[0].equals("start", ignoreCase = true)) {
				/*설정된 규칙 출력*/
				object : BukkitRunnable() {
					override fun run() {
						Bukkit.broadcastMessage("~~~~~~~~설정된 규칙~~~~~~~~")
						for(message in Settings.sendCurrentSettings())
							Bukkit.broadcastMessage(message)
						Bukkit.broadcastMessage("~~~~~~~~~~~~~~~~~~~~~~~~~~~")
					}
				}.runTaskLater(plugin, 50L)

				/*잠시 후 시작 메세지*/
				object : BukkitRunnable() {
					override fun run() {
						Bukkit.broadcastMessage("\n${BLUE}잠시 후 시작합니다..")
					}
				}.runTaskLater(plugin, 80L)


				/*능력뽑기 카운트다운*/
				val onlinePlayers = Bukkit.getOnlinePlayers()
				var drawAbilityCountdown = 3
				object : BukkitRunnable() {
					override fun run() {
						if(drawAbilityCountdown == 0) this.cancel()

						for(player in onlinePlayers) {
							player.sendTitle("", "$drawAbilityCountdown", 0, 21, 0)
							sendActionBar("${YELLOW}능력 추첨 중..", player)
							player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f)
						}

						drawAbilityCountdown--
					}
				}.runTaskTimer(plugin, 300L, 20L)

				/*능력 추첨*/
				object : BukkitRunnable() {
					override fun run() {
						for(targetPlayer in onlinePlayers) {
							var randNum: Int
							var someAbility: Ability

							while(true) {
								randNum = randomIntInRange(0, AbilityList.abilityList.size - 1)
								someAbility = AbilityList.abilityList[randNum]

								if(onlinePlayers.size < AbilityList.abilityList.size /*&& canBeDuplicated*/)  //접속한 유저 수가 등록된, 구현된 능력 갯수보다 많으면 능력 중복을 허용하며, 중복 검사를 하지 않는다.
									break
								if(someAbility.alreadyTaken)
									continue
							}

							AbilityList.abilityPlayerList[targetPlayer.name] = someAbility //AbilityList.abilityList[randNum]

							showAbilityInfo(targetPlayer)
							for(i in someAbility.score.indices)
								playNotes(someAbility.period, someAbility.volume[i], someAbility.instrument[i], someAbility.score[i], targetPlayer)

							someAbility.passiveEffect(targetPlayer)
						}
					}
				}.runTaskLater(plugin, 360L)

				/*시작 카운트다운*/
				var startCountdown = 3
				object : BukkitRunnable() {
					override fun run() {
						val coloredCount = when(startCountdown) {
							3    -> "${GREEN}3"
							2    -> "${YELLOW}2"
							else -> "${RED}1"
						}
						val pitch = when(startCountdown) {  //플링 노트블럭 효과음 음정
							3, 2, 1  -> Notes.D2
							else     -> Notes.G2
						}

						for(player in onlinePlayers) {
							player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitch)

							if(startCountdown > 0)
								player.sendTitle(coloredCount, "시작까지..", 0, 100, 0)
							else {
								player.sendTitle("${GREEN}시작!", "", 0, 20, 20)
								this.cancel()
							}
						}
						startCountdown--
					}
				}.runTaskTimer(plugin, 560L, 20L)
			}
			else if(args[0].equals("stop", true)) {
				val onlinePlayers = Bukkit.getOnlinePlayers()
				for(player in onlinePlayers) {
					lateinit var ability: Ability
					when(AbilityList.abilityPlayerList[player.name]!!.abilityID) {  //abilityID를 가져옴
						"MarisaKirisame" -> ability = AbilityList.MarisaKirisame

						"Rumia"          -> ability = AbilityList.Rumia
						"FlandreScarlet" -> ability = AbilityList.FlandreScarlet

						"YukariYakumo"   -> ability = AbilityList.YukariYakumo

						"TewiInaba"      -> ability = AbilityList.TewiInaba

						else -> { /*Ignored - 잘못 입력할 일이 없음*/ }
					}
					AbilityList.abilityPlayerList.remove(player.name, ability)

					sender.sendMessage("${RED}대전을 중지하였습니다.")
				}
			}

			if(sender is Player) {
				val player = sender
				if(args[0].equals("desc", true)) {  //개인설정으로 설명을 채팅창에 띄울지 GUI로 띄울지를 추가
					try {
						showAbilityInfo(player) //122번 줄
					}
					catch(npException: NullPointerException) {  //능력이 없을때(Null일때)
						player.sendMessage("${BOLD}${RED}에러 : 능력이 없습니다.${RESET}")
					}
				}
				else if(args[0] == "setting") {  //설정  todo 설정 리스트 만들기, 기본적으로 능력중복을 허용할지의 여부 추가
					if(args[1] == "isBlockExploded") {
						when(args[2]) {
							"true", "True"   -> canBlocksBeDestroyed = true
							"false", "False" -> canBlocksBeDestroyed = false
							else -> player.sendMessage("${RED}에러 : true(참), false(거짓)만 입력할 수 있습니다.")
						}
						player.sendMessage("${AQUA}폭발시 블럭 파괴 여부가 ${canBlocksBeDestroyed}로 변경되었습니다.")
					}
				}





				/*-------------------------
			    *   디버그 전용 명령어들
			    * ------------------------*/
				if(args[0] == "debug") {
					if(args[1] == "getAbility") {  //능력 얻기
						var hasAbility = false
						try { hasAbility = AbilityList.abilityPlayerList[player.name] != null } //능력이 있는지 검사
						catch(ignored: CommandException) { } //null 떴을때.  무시한다.

						if(hasAbility) {
							player.sendMessage("${RED}에러 : 이미 능력이 있습니다.")
						}
						else {
							var isCorrect = true  //올바르게 입력했는지의 여부
							lateinit var ability: Ability
							when(args[2]) {
								"MarisaKirisame" -> ability = AbilityList.MarisaKirisame

								"Rumia"          -> ability = AbilityList.Rumia
								"FlandreScarlet" -> ability = AbilityList.FlandreScarlet

								"YukariYakumo"   -> ability = AbilityList.YukariYakumo

								"TewiInaba"      -> ability = AbilityList.TewiInaba

								else -> {
									player.sendMessage("${RED}에러 : 올바르게 입력해 주세요")
									isCorrect = false
								}
							}

							if(isCorrect) {  //올바르게 입력한 경우에만
								AbilityList.abilityPlayerList[player.name] = ability
								player.sendMessage(AbilityList.abilityPlayerList[player.name]!!.abilityName + "을 받았습니다.")
							}
						}
					}
					else if(args[1] == "delAbility") {  //능력 제거, 삭제
						var hasAbility = false
						try { hasAbility = AbilityList.abilityPlayerList[player.name] != null } //능력이 있는지 검사
						catch(ignored: CommandException) { } //null 떴을때.  무시한다.

						if(!hasAbility) {
							player.sendMessage("${RED}에러 : 능력이 없습니다.")
						}
						else {
							player.sendMessage(AbilityList.abilityPlayerList[player.name]!!.abilityName + "을 제거했습니다.")
							lateinit var ability: Ability
							when(AbilityList.abilityPlayerList[player.name]!!.abilityID) {  //abilityID를 가져옴
								"MarisaKirisame" -> ability = AbilityList.MarisaKirisame

								"Rumia"          -> ability = AbilityList.Rumia
								"FlandreScarlet" -> ability = AbilityList.FlandreScarlet

								"YukariYakumo"   -> ability = AbilityList.YukariYakumo

								"TewiInaba"      -> ability = AbilityList.TewiInaba

								else -> { /*Ignored - 잘못 입력할 일이 없음*/ }
							}
							AbilityList.abilityPlayerList.remove(player.name, ability)
						}
					}
					else if(args[1] == "viewAbilities") {  //ability debug viewabilities, 현재 접속한 유저들의 능력을 표시
						for(targetPlayer in Bukkit.getOnlinePlayers()) {
							val abilityName: String = try {
								AbilityList.abilityPlayerList[targetPlayer.name]!!.abilityName //AbilityPlayerList에서 그 플레이어에 해당하는 능력의 이름
							}
							catch(exception: NullPointerException) {  //능력이 없으면 null
								player.sendMessage(player.name + " : 능력 없음")
								return false
							}
							player.sendMessage(player.name + " : " + abilityName)
						}
					}
					else if(args[1] == "playNotes") {
						val someAbility: Ability

						try {
							someAbility = try {
								when(args[2]) {
									"MarisaKirisame" -> AbilityList.MarisaKirisame

									"Rumia"          -> AbilityList.Rumia
									"FlandreScarlet" -> AbilityList.FlandreScarlet

									"YukariYakumo"   -> AbilityList.YukariYakumo

									"TewiInaba"      -> AbilityList.TewiInaba

									else             -> AbilityList.abilityPlayerList[player.name]!!  //자신의 능력
								}
							}
							catch(arrayIndexException: ArrayIndexOutOfBoundsException) {  //아무것도 적지 않았을때 args[]의 예외
								AbilityList.abilityPlayerList[player.name]!!
							}

							for(i in someAbility.score.indices)
								playNotes(someAbility.period, someAbility.volume[i], someAbility.instrument[i], someAbility.score[i], player)
						}
						catch(npException: java.lang.NullPointerException) {
							player.sendMessage("${RED}에러 : 능력이 없습니다. (혹은 뒤에 능력 이름을 적어주세요.)")
						}
					}
					else if(args[1] == "resetCooldown") {  //ability debug resetcooldown
						//Arrays.fill(abilityPlayerList.get(Player.getName()).cooldown, 0);  //아래 2줄과 동작이 같다.
						for(i in AbilityList.abilityPlayerList[player.name]!!.cooldown.indices)
							AbilityList.abilityPlayerList[player.name]!!.cooldown[i] = 0
						player.sendMessage("${BLUE}대기시간이 초기화 되었습니다.")
					}
				}
			}
		}
		return true
	}


	private fun showAbilityInfo(player: Player)
	{
		val abilityName = AbilityList.abilityPlayerList[player.name]!!.abilityName  //~~하는 정도의 능력
		val abilityDesc = AbilityList.abilityPlayerList[player.name]!!.abilityDesc.toList()  //능력 설명,
		
		val abilityDesc2 = mutableListOf<MutableList<String>>() //이차원 배열
		
		for(i in abilityDesc.indices)
			abilityDesc2.addAll(mutableListOf(abilityDesc[i].split(" : ").toMutableList()))
		
		val descInv = Bukkit.createInventory(null, 54, abilityName)
		
		val abilityNameLabel = ItemStack(Material.NAME_TAG, 1)
		val abilityNameLabelMeta = abilityNameLabel.itemMeta!!
		abilityNameLabelMeta.setDisplayName("${WHITE}${BOLD}${abilityName}")
		abilityNameLabel.itemMeta = abilityNameLabelMeta
		
		val cancelButton = ItemStack(Material.BARRIER, 1)
		val cancelButtonMeta = cancelButton.itemMeta!!
		cancelButtonMeta.setDisplayName("${BOLD}${RED}닫기")
		cancelButton.itemMeta = cancelButtonMeta
		
		descInv.setItem(4, abilityNameLabel)
		descInv.setItem(53, cancelButton)
		
		for(someDesc in abilityDesc2) {
			val usage = someDesc[0]
			val timeDesc = someDesc[2]
			val skillDesc = someDesc[1]
			var cautions = ""
			try { cautions = someDesc[3] } catch(ioobException: IndexOutOfBoundsException) { someDesc.add("") /*<--add someDesc[3]*/ }
			
			
			var isIron = false
			var isGold = false
			var isDiamond = false
			
			when {
				usage.contains("철괴")       -> isIron = true
				usage.contains("금괴")       -> isGold = true
				usage.contains("다이아몬드") -> isDiamond = true
				else -> throw Exception("It seemed that there's some typo.")
			}
			
			
			var needSneak = false
			var isLeftClick = false
			var isRightClick = false
			
			if(usage.contains("웅크리기"))
				needSneak = true
			when {
				usage.contains("좌클릭") -> isLeftClick = true
				usage.contains("우클릭") -> isRightClick = true
				else -> throw Exception("It seemed that there's some typo.")
			}
			/*-------------------*/
			
			val itemType = when {
				isIron    -> Material.IRON_INGOT
				isGold    -> Material.GOLD_INGOT
				isDiamond -> Material.DIAMOND
				else      -> throw Exception("It seemed that there's some typo.")
			}
			
			
			val itemName = ""
			
			if(needSneak)
				itemName.plus("웅크리기 +")
			when {
				isLeftClick  -> itemName.plus(" 좌클릭")
				isRightClick -> itemName.plus(" 우클릭")
			}
			
			
			var slotNum = 0
			
			lateinit var temp: Array<Int>
			if(needSneak)
				slotNum++
			when {
				isIron    -> temp = arrayOf(11, 15)
				isGold    -> temp = arrayOf(20, 24)
				isDiamond -> temp = arrayOf(29, 33)
			}
			when {
				isLeftClick  -> slotNum += temp[0]
				isRightClick -> slotNum += temp[1]
			}
			
			
			val item = ItemStack(itemType, 1)
			val itemMeta = item.itemMeta!!
			itemMeta.setDisplayName(usage)
			itemMeta.lore = listOf("${WHITE}${timeDesc}", "", "${WHITE}${skillDesc}", "${RED}${cautions}")
			item.itemMeta = itemMeta
			
			descInv.setItem(slotNum, item)
		}
		player.openInventory(descInv)
		//클릭 시 이벤트에 관해서는 이벤트 클래스 참고
		
		
		/*  //채팅창에 내보내는거
		player.sendMessage(""); player.sendMessage("")
		player.sendMessage("${BLUE}~~~~${abilityName}~~~~")
		
		for(line in abilityDesc)
			player.sendMessage(line)
		 */
	}
}