package com.bobacadodl.JSONChatLib;

import com.bobacadodl.JSONChatLib.lib.org.json.simple.JSONArray;
import com.bobacadodl.JSONChatLib.lib.org.json.simple.JSONObject;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

//import net.minecraft.server.v1_9_R1.IChatBaseComponent;
//import net.minecraft.server.v1_9_R1.PacketPlayOutChat;
//import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
/**
 * User: bobacadodl Date: 10/27/13 Time: 8:03 PM
 */
public class JSONChatMessage {

	private final JSONObject chatObject;

	public JSONChatMessage(String text, JSONChatColor color, List<JSONChatFormat> formats) {
		chatObject = new JSONObject();
		chatObject.put("text", text);
		if (color != null) {
			chatObject.put("color", color.getColorString());
		}
		if (formats != null) {
			for (JSONChatFormat format : formats) {
				chatObject.put(format.getFormatString(), true);
			}
		}
	}

	public void addExtra(JSONChatExtra extraObject) {
		if (!chatObject.containsKey("extra")) {
			chatObject.put("extra", new JSONArray());
		}
		JSONArray extra = (JSONArray) chatObject.get("extra");
		extra.add(extraObject.toJSON());
		chatObject.put("extra", extra);
	}

	public void sendToPlayer(Player player) {
		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "tellraw " + player.getName() + " " + chatObject.toJSONString());
	}

	@Override
	public String toString() {
		return chatObject.toJSONString();
	}
}
