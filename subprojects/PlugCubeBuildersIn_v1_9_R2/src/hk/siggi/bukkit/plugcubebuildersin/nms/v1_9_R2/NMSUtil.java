package hk.siggi.bukkit.plugcubebuildersin.nms.v1_9_R2;

import com.mojang.authlib.GameProfile;
import hk.siggi.bukkit.plugcubebuildersin.nms.ChatSetting;
import hk.siggi.bukkit.plugcubebuildersin.nms.SkinSettings;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.DataWatcherObject;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.GameProfileSerializer;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketPlayOutTileEntityData;
import net.minecraft.server.v1_9_R2.TileEntitySkull;
import net.minecraft.server.v1_9_R2.World;
import net.minecraft.server.v1_9_R2.WorldServer;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_9_R2.block.CraftSkull;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;

public class NMSUtil extends hk.siggi.bukkit.plugcubebuildersin.nms.NMSUtil<Packet> {

	@Override
	public SkinSettings getSkinSettings(Player p) {
		CraftPlayer pl = (CraftPlayer) p;
		EntityPlayer handle = pl.getHandle();
		return new SkinSettings((int) ((Integer) handle.getDataWatcher().get(PA.getClientSettings())));
	}

	@Override
	public ChatSetting getChatSetting(Player p) {
		CraftPlayer pl = (CraftPlayer) p;
		EntityPlayer handle = pl.getHandle();
		EntityHuman.EnumChatVisibility nmsVisibility = handle.getChatFlags();
		if (nmsVisibility == null) {
			return ChatSetting.ON;
		}
		switch (nmsVisibility) {
			case FULL:
				return ChatSetting.ON;
			case SYSTEM:
				return ChatSetting.COMMANDS_ONLY;
			case HIDDEN:
				return ChatSetting.OFF;
		}
		return ChatSetting.ON;
	}

	@Override
	public String getLocale(Player p) {
		CraftPlayer pl = (CraftPlayer) p;
		EntityPlayer handle = pl.getHandle();
		return handle.locale;
	}

	@Override
	public void setRenderDistance(org.bukkit.World world, int distance) {
		try {
			org.bukkit.craftbukkit.v1_9_R2.CraftWorld cw = (org.bukkit.craftbukkit.v1_9_R2.CraftWorld) world;
			WorldServer handle = cw.getHandle();
			handle.getPlayerChunkMap().a(distance);
			handle.getTracker().a(distance);
		} catch (Exception e) {
		}
	}

	private abstract static class PA extends EntityHuman {

		public static DataWatcherObject getClientSettings() {
			return bq;
		}

		public static DataWatcherObject getMainHandSetting() {
			return br;
		}

		private PA(World world, GameProfile gameprofile) {
			super(world, gameprofile);
		}
	}

	@Override
	public void sendPacket(Player p, Packet packet) {
		if (p == null) {
			throw new NullPointerException();
		}
		if (packet == null) {
			return;
		}
		CraftPlayer cp = (CraftPlayer) p;
		cp.getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public Packet createPacket(Skull skull, GameProfile gameProfile) {
		CraftSkull cs = (CraftSkull) skull;
		TileEntitySkull tileEntity = cs.getTileEntity();
		NBTTagCompound tag = tileEntity.E_();
		int x = tag.getInt("x");
		int y = tag.getInt("y");
		int z = tag.getInt("z");
		BlockPosition bp = new BlockPosition(x, y, z);
		if (gameProfile == null) {
			tag.remove("Owner");
		} else {
			NBTTagCompound gameProfileTag = new NBTTagCompound();
			GameProfileSerializer.serialize(gameProfileTag, gameProfile);
			tag.set("Owner", gameProfileTag);
		}
		PacketPlayOutTileEntityData updatePacket = new PacketPlayOutTileEntityData(bp, 4, tag);
		return updatePacket;
	}
	
	@Override
	public int getPing(Player player) {
		return ((CraftPlayer) player).getHandle().ping;
	}

	@Override
	public boolean isHostile(LivingEntity entity) {
		if (entity instanceof Blaze) {
			return true;
		} else if (entity instanceof Spider) {
			return true;
		} else if (entity instanceof Creeper) {
			return true;
		} else if (entity instanceof Endermite) {
			return true;
		} else if (entity instanceof Ghast) {
			return true;
		} else if (entity instanceof Guardian) {
			return true;
		} else if (entity instanceof MagmaCube) {
			return true;
		} else if (entity instanceof Shulker) {
			return true;
		} else if (entity instanceof Silverfish) {
			return true;
		} else if (entity instanceof Skeleton) {
			return true;
		} else if (entity instanceof Slime) {
			return true;
		} else if (entity instanceof Witch) {
			return true;
		} else if (entity instanceof Wither) {
			return true;
		} else if (entity instanceof Zombie) {
			return true;
		} else if (entity instanceof Wolf) {
			Wolf wolf = (Wolf) entity;
			return !wolf.isTamed() && wolf.isAngry();
		} else {
			return false;
		}
	}
}
