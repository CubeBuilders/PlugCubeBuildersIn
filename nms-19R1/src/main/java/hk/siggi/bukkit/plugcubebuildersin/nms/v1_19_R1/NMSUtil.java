package hk.siggi.bukkit.plugcubebuildersin.nms.v1_19_R1;

import com.mojang.authlib.GameProfile;
import hk.siggi.bukkit.plugcubebuildersin.nms.ChatSetting;
import hk.siggi.bukkit.plugcubebuildersin.nms.SkinSettings;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.player.EnumChatVisibility;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.world.level.block.entity.TileEntitySkull;
import net.minecraft.server.level.WorldServer;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftSkull;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Husk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Monster;
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
		return new SkinSettings((int) ((Integer) handle.ai().a(PA.getClientSettings())));
	}

	@Override
	public ChatSetting getChatSetting(Player p) {
		CraftPlayer pl = (CraftPlayer) p;
		EntityPlayer handle = pl.getHandle();
		EnumChatVisibility nmsVisibility = handle.A();
		if (nmsVisibility == null) {
			return ChatSetting.ON;
		}
		switch (nmsVisibility) {
			case a:
				return ChatSetting.ON;
			case b:
				return ChatSetting.COMMANDS_ONLY;
			case c:
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
			org.bukkit.craftbukkit.v1_19_R1.CraftWorld cw = (org.bukkit.craftbukkit.v1_19_R1.CraftWorld) world;
			WorldServer handle = cw.getHandle();
			Method setViewDistance = PlayerChunkMap.class.getDeclaredMethod("a", int.class);
			setViewDistance.invoke(handle.k().a, distance);
		} catch (Exception e) {
		}
	}

	private abstract static class PA extends EntityHuman {

		public static DataWatcherObject getClientSettings() {
			return bO;
		}

		public static DataWatcherObject getMainHandSetting() {
			return bP;
		}

		private PA() {
			super(null, null, 0.0f, null, null);
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
		cp.getHandle().b.a(packet);
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
		NBTTagCompound tag = tileEntity.aa_();
		if (gameProfile == null) {
			// r = remove
			tag.r("SkullOwner");
		} else {
			NBTTagCompound gameProfileTag = new NBTTagCompound();
			// a = serialize
			GameProfileSerializer.a(gameProfileTag, gameProfile);
			// a = set
			tag.a("SkullOwner", gameProfileTag);
		}
		// TileEntityTypes.o = skull
		PacketPlayOutTileEntityData updatePacket = PacketPlayOutTileEntityData.a(tileEntity, te -> tag);
		return updatePacket;
	}

	@Override
	public int getPing(Player player) {
		return ((CraftPlayer) player).getHandle().e;
	}

	@Override
	public boolean isHostile(LivingEntity entity) {
		if (entity instanceof Monster) {
			return true;
			// below are mobs that are hostile but are not a subclass of Monster
		} else if (entity instanceof Ghast) {
			return true;
		} else if (entity instanceof MagmaCube) {
			return true;
		} else if (entity instanceof Phantom) {
			return true;
		} else if (entity instanceof PolarBear) {
			return true;
		} else if (entity instanceof Shulker) {
			return true;
		} else if (entity instanceof Slime) {
			return true;
		} else if (entity instanceof Wolf) {
			Wolf wolf = (Wolf) entity;
			return !wolf.isTamed() && wolf.isAngry();
		} else {
			return false;
		}
	}

	private final BrigadierUtil brigadierUtil = new BrigadierUtil();

	@Override
	public BrigadierUtil getBrigadierUtil() {
		return brigadierUtil;
	}
}
