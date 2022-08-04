package hk.siggi.bukkit.plugcubebuildersin.nms;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import org.bukkit.Bukkit;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public abstract class NMSUtil<PKT> {

	private static NMSUtil util = null;

	public static NMSUtil get() {
		if (util == null) {
			try {
				Class clazz = Class.forName("hk.siggi.bukkit.plugcubebuildersin.nms." + getVersion() + ".NMSUtil");
				util = (NMSUtil) clazz.newInstance();
			} catch (Exception e) {
				util = new NullNMSUtil();
			}
		}
		return util;
	}

	private static String getVersion() {
		String name = Bukkit.getServer().getClass().getName();
		String version = name.substring(name.indexOf(".v") + 1);
		version = version.substring(0, version.indexOf("."));
		return version;
	}

	public final Object get(Object object, String field) throws NoSuchFieldException {
		Class clazz = object.getClass();
		NoSuchFieldException nsfe = null;
		while (true) {
			try {
				Field f = clazz.getDeclaredField(field);
				f.setAccessible(true);
				Object get = f.get(object);
				return get;
			} catch (NoSuchFieldException e) {
				if (nsfe == null) {
					nsfe = e;
				}
			} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			}
			if (clazz == Object.class) {
				break;
			}
			clazz = clazz.getSuperclass();
		}
		if (nsfe != null) {
			throw nsfe;
		}
		return null;
	}

	public final void set(Object object, String field, Object value) throws NoSuchFieldException {
		Class clazz = object.getClass();
		NoSuchFieldException nsfe = null;
		while (true) {
			try {
				Field f = clazz.getDeclaredField(field);
				f.setAccessible(true);
				f.set(object, value);
				return;
			} catch (NoSuchFieldException e) {
				if (nsfe == null) {
					nsfe = e;
				}
			} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			}
			if (clazz == Object.class) {
				break;
			}
			clazz = clazz.getSuperclass();
		}
		if (nsfe != null) {
			throw nsfe;
		}
	}

	public abstract SkinSettings getSkinSettings(Player p);

	public abstract ChatSetting getChatSetting(Player p);

	public abstract String getLocale(Player p);

	public abstract void setRenderDistance(org.bukkit.World world, int distance);

	public abstract void sendPacket(Player p, PKT packet);

	public abstract PKT createPacket(Skull skull, GameProfile gameProfile);

	public abstract int getPing(Player p);
	
	public abstract boolean isHostile(LivingEntity entity);

	public abstract PKT createEntityStatusPacket(Entity entity, int status);

	public void setClientSideOpLevel(Player p, int opLevel) {
		sendPacket(p, createEntityStatusPacket(p, clamp(opLevel, 0, 4) + 24));
	}

	public static int clamp(int value, int min, int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	public BrigadierUtil getBrigadierUtil() {
		throw new UnsupportedOperationException();
	}
}
