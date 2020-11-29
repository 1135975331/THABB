package thabb

object Settings
{
	var canBlocksBeDestroyed = true

	fun sendCurrentSettings(): Array<String>
	{
		return arrayOf(
				"폭발시 블럭 파괴 여부 : $canBlocksBeDestroyed"
		)
	}
}