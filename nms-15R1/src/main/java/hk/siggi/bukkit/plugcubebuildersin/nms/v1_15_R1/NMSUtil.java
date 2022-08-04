package hk.siggi.bukkit.plugcubebuildersin.nms.v1_15_R1;

import com.mojang.authlib.GameProfile;
import hk.siggi.bukkit.plugcubebuildersin.nms.ChatSetting;
import hk.siggi.bukkit.plugcubebuildersin.nms.SkinSettings;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.DataWatcherObject;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EnumChatVisibility;
import net.minecraft.server.v1_15_R1.GameProfileSerializer;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_15_R1.PacketPlayOutTileEntityData;
import net.minecraft.server.v1_15_R1.PlayerChunkMap;
import net.minecraft.server.v1_15_R1.TileEntitySkull;
import net.minecraft.server.v1_15_R1.World;
import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftSkull;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Husk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Stray;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;

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
		EnumChatVisibility nmsVisibility = handle.getChatFlags();
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
			org.bukkit.craftbukkit.v1_15_R1.CraftWorld cw = (org.bukkit.craftbukkit.v1_15_R1.CraftWorld) world;
			WorldServer handle = cw.getHandle();
			Method setViewDistance = PlayerChunkMap.class.getDeclaredMethod("setViewDistance", int.class);
			setViewDistance.invoke(handle.getChunkProvider().playerChunkMap, distance);
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
		TileEntitySkull tileEntity;
		try {
			// getTileEntity() changed from public to protected, so we have to use reflection now.
			Method getTileEntity = CraftBlockEntityState.class.getDeclaredMethod("getTileEntity");
			getTileEntity.setAccessible(true);
			tileEntity = (TileEntitySkull) getTileEntity.invoke(cs);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
		NBTTagCompound tag = tileEntity.b();
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
		} else if (entity instanceof ElderGuardian) {
			return true;
		} else if (entity instanceof Endermite) {
			return true;
		} else if (entity instanceof Evoker) {
			return true;
		} else if (entity instanceof Ghast) {
			return true;
		} else if (entity instanceof Guardian) {
			return true;
		} else if (entity instanceof MagmaCube) {
			return true;
		} else if (entity instanceof Phantom) {
			return true;
		} else if (entity instanceof PolarBear) {
			return true;
		} else if (entity instanceof Shulker) {
			return true;
		} else if (entity instanceof Silverfish) {
			return true;
		} else if (entity instanceof Skeleton) {
			return true;
		} else if (entity instanceof Stray) {
			return true;
		} else if (entity instanceof Slime) {
			return true;
		} else if (entity instanceof Vex) {
			return true;
		} else if (entity instanceof Vindicator) {
			return true;
		} else if (entity instanceof Witch) {
			return true;
		} else if (entity instanceof Wither) {
			return true;
		} else if (entity instanceof WitherSkeleton) {
			return true;
		} else if (entity instanceof Zombie) {
			return true;
		} else if (entity instanceof ZombieVillager) {
			return true;
		} else if (entity instanceof Husk) {
			return true;
		} else if (entity instanceof Drowned) {
			return true;
		} else if (entity instanceof Wolf) {
			Wolf wolf = (Wolf) entity;
			return !wolf.isTamed() && wolf.isAngry();
		} else if (entity instanceof Pillager) {
			return true;
		} else if (entity instanceof Ravager) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Packet createEntityStatusPacket(Entity entity, int status) {
		CraftEntity craftEntity = (CraftEntity) entity;
		net.minecraft.server.v1_15_R1.Entity nmsEntity = craftEntity.getHandle();

		return new PacketPlayOutEntityStatus(nmsEntity, (byte) status);
	}

	private final BrigadierUtil brigadierUtil = new BrigadierUtil();

	@Override
	public BrigadierUtil getBrigadierUtil() {
		return brigadierUtil;
	}
}
