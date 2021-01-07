package thabb

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import thabb.command.CommonCommand
import thabb.event.EventManager

class Main : JavaPlugin()
{
	private lateinit var commands1: CommonCommand

	var jPlugin: JavaPlugin = this
	
	fun main() {}
	
	override fun onEnable()
	{
		commands1 = CommonCommand(this)
		jPlugin.getCommand("ability")?.setExecutor(commands1)
		CommonCommand(this)

		Bukkit.getPluginManager().registerEvents(EventManager(), this)


		println("${ChatColor.GREEN}플러그인이 활성화 되었습니다.")
	}

	override fun onDisable()
	{
		// Plugin shutdown logic
		println("${ChatColor.RED}플러그인이 비활성화 되었습니다.")
	}
}


/*
프로젝트 이름 변경시
최상위 폴더 이름 리팩토링,
resources.plugin.yml에서 메인파일의 위치변경
pom.xml에서 빌드되어 나온 파일의 이름 변경
Project Structure/Artifact에서 결과물 이름 변경 위쪽 아래쪽 모두
*/
