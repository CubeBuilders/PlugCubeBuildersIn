package hk.siggi.bukkit.plugcubebuildersin.nms;

import com.mojang.authlib.GameProfile;
import org.bukkit.World;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class NullNMSUtil extends NMSUtil<Object> {

	private final SkinSettings allskin = new SkinSettings(127);

	@Override
	public SkinSettings getSkinSettings(Player p) {
		return allskin;
	}

	@Override
	public ChatSetting getChatSetting(Player p) {
		return ChatSetting.ON;
	}

	@Override
	public String getLocale(Player p) {
		return "en";
	}

	@Override
	public void setRenderDistance(World world, int distance) {
	}

	@Override
	public void sendPacket(Player p, Object packet) {
	}

	@Override
	public Object createPacket(Skull skull, GameProfile gameProfile) {
		return null;
	}
	
	@Override
	public int getPing(Player player) {
		return -1;
	}

	@Override
	public boolean isHostile(LivingEntity entity) {
		return false;
	}

	@Override
	public Object createEntityStatusPacket(Entity entity, int status) {
		return null;
	}
}
