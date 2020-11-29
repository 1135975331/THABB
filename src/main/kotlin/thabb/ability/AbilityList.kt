package thabb.ability

import thabb.ability.abilities.*

object AbilityList
{
	var abilityPlayerList = HashMap<String, Ability>()  //플레이어의 능력을 저장한 리스트.  능력을 얻으면 기록된다.


	var MarisaKirisame: Ability = MarisaKirisame()

	var Rumia: Ability = Rumia()  //todo 아직 미구현
	var FlandreScarlet: Ability = FlandreScarlet()

	var YukariYakumo: Ability = YukariYakumo()

	var TewiInaba: Ability = TewiInaba()


	var abilityList = arrayOf(MarisaKirisame,
			/*Rumia - 미구현,*/
			FlandreScarlet,
			YukariYakumo,
			TewiInaba)
}