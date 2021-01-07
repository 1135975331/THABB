package thabb.event

import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import thabb.ability.AbilityList
import thabb.ability.abilities.MarisaKirisame
import thabb.ability.abilities.TewiInaba

class EventManager : Listener {
	@EventHandler
	fun onAbilityDescInvClicked(event: InventoryClickEvent)
	{
		val player = event.view.player as Player
		
		
		if(!event.view.title.contains("정도의 능력"))  return
		
		val clickedItemType = event.currentItem?.type ?: return
		if(clickedItemType == Material.BARRIER)
			player.closeInventory()
		else
			event.isCancelled = true
	}
	
	@EventHandler
	fun onPlayerInteractEvent(event: PlayerInteractEvent) //PlayerInteractEvent가 발생했을때
	{
		val playerAction = event.action
		val clickedItem = event.item //이벤트 관련(클릭된) 아이템
		var clickedItemMaterial: Material? = null

		if(clickedItem != null) clickedItemMaterial = clickedItem.type //관련(클릭된) 아이템의 종류

		if(AbilityList.abilityPlayerList[event.player.name] != null) {  //플레이어의 이름을 가져오는데 null이 아닐때만
			val targetAbility = AbilityList.abilityPlayerList[event.player.name] //AbilityPlayerList에서 플레이어의 이름을 가져와 해당 능력을 불러오고 그 능력을 TargetAbility에 넣는다(주소값 공유)

			if(playerAction == Action.LEFT_CLICK_BLOCK || playerAction == Action.LEFT_CLICK_AIR) {  //좌클릭 시
				when(clickedItemMaterial) {
					Material.IRON_INGOT -> targetAbility!!.ironIngotLeft(event) //해당 이벤트를 TargetAbility에 대해 Ability.java 파일의 오버라이딩 함수에 전달한다.
					Material.GOLD_INGOT -> targetAbility!!.goldIngotLeft(event)
					Material.DIAMOND    -> targetAbility!!.diamondLeft(event)
					else -> { /*Ignored*/ }
				}
			}
			else if(playerAction == Action.RIGHT_CLICK_BLOCK || playerAction == Action.RIGHT_CLICK_AIR) {  //우클릭 시
				when(clickedItemMaterial) {
					Material.IRON_INGOT -> targetAbility!!.ironIngotRight(event) //해당 이벤트를 TargetAbility에 대해 Ability.java 파일의 오버라이딩 함수에 전달한다.
					Material.GOLD_INGOT -> targetAbility!!.goldIngotRight(event)
					Material.DIAMOND    -> targetAbility!!.diamondRight(event)
					else -> { /*Ignored*/ }
				}
			}
		}
	}

	@EventHandler
	fun onProjectileLaunchEvent(projLaunchEvent: ProjectileLaunchEvent) {
		if(AbilityList.abilityPlayerList[projLaunchEvent.entity.name] != null) {  //플레이어의 이름을 가져오는데 null이 아닐때만
			val targetAbility = AbilityList.abilityPlayerList[projLaunchEvent.entity.name] //AbilityPlayerList에서 플레이어의 이름을 가져와 해당 능력을 불러오고 그 능력을 TargetAbility에 넣는다(주소값 공유)
			targetAbility!!.passiveSkill(projLaunchEvent) //ProjectileHitEvent event
		}
	}

	@EventHandler
	fun onProjectileHitEvent(projHitEvent: ProjectileHitEvent) {
		if(AbilityList.abilityPlayerList[projHitEvent.entity.name] != null) {  //플레이어의 이름을 가져오는데 null이 아닐때만
			val targetAbility = AbilityList.abilityPlayerList[projHitEvent.entity.name] //AbilityPlayerList에서 플레이어의 이름을 가져와 해당 능력을 불러오고 그 능력을 TargetAbility에 넣는다(주소값 공유)
			targetAbility!!.passiveSkill(projHitEvent) //ProjectileHitEvent event
		}
	}

	@EventHandler
	fun onEntityDamageEvent(entityDamageEvent: EntityDamageEvent) {
		if(AbilityList.abilityPlayerList[entityDamageEvent.entity.name] != null) {
			val targetAbility = AbilityList.abilityPlayerList[entityDamageEvent.entity.name] //AbilityPlayerList에서 플레이어의 이름을 가져와 해당 능력을 불러오고 그 능력을 TargetAbility에 넣는다(주소값 공유)
			targetAbility!!.passiveSkill(entityDamageEvent)
		}
	}

	
	@EventHandler
	fun onExplode(explodeEvent: BlockExplodeEvent) {
		for(explodedBlock in explodeEvent.blockList()) {
			val explodedBlockData = explodedBlock.blockData
			val fallingBlock = explodedBlock.world.spawnFallingBlock(explodedBlock.location, explodedBlockData)

			fallingBlock.isInvulnerable = true
			fallingBlock.dropItem = true
			fallingBlock.setHurtEntities(true)

			val randomX = Math.random() * 0.3 - 0.15  //날라갈 정도를 랜덤으로 추첨  0.5배를 빼는 이유는 음수범위까지 사용하기 위해
			val randomY = Math.random() * 1.35  //다만 Y좌표로 날아갈때는 항상 양수
			val randomZ = Math.random() * 0.3 - 0.15

			val blockDirection = explodedBlock.location.toVector().setX(randomX).setY(randomY).setZ(randomZ)
			fallingBlock.velocity = blockDirection
		}
	}


	@EventHandler
	fun onPotionSplashed(splashEvent: PotionSplashEvent) 
	{
		val potion = splashEvent.potion
		val shooter = potion.shooter

		if(shooter !is Player)
			return  //플레이어가 던지지 않았다면 바로 끝냄

		val ability = AbilityList.abilityPlayerList[shooter.name]


		if(ability is MarisaKirisame) {  //마리사 전용
			ability.explosivePotion(splashEvent)
		}
	}

	@EventHandler
	fun trapActivate(moveEvent: PlayerMoveEvent)
	{
		val player = moveEvent.player
		val world = player.world
		val ability = AbilityList.abilityPlayerList[player.name]

		if(!player.isFlying) {  //테위의 함정
			val nearbyActivator = world.getNearbyEntities(player.location, 0.5, 0.5, 0.5)
			nearbyActivator.removeIf{ ent -> ent !is ArmorStand || "trapActivator" !in ent.scoreboardTags }  //trapActivator만 남김

			if(nearbyActivator.isNotEmpty()) {  //함정에 걸렸을때
				val tewiInaba = AbilityList.TewiInaba as TewiInaba

				for(activator in nearbyActivator) {
					tewiInaba.activateTrap(activator)
					tewiInaba.trapActivator.removeIf { activ -> activ == activator }
					activator.remove()
				}
				nearbyActivator.clear()
			}
		}
	}

	@EventHandler
	fun flying(moveEvent: PlayerMoveEvent) {
		val player = moveEvent.player
		val world = player.world
		val ability = AbilityList.abilityPlayerList[player.name]

		if(ability !is MarisaKirisame) return   //마리사 전용

		if(ability.isActivatedDiamondLeft && player.isFlying)
			ability.headbutt(moveEvent)
	}
}
