package hk.siggi.bukkit.plugcubebuildersin.skins;

import static hk.siggi.bukkit.plugcubebuildersin.util.Util.tryClose;
import hk.siggi.statues.Statue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.entity.Player;

public final class PlayerSkins {

	public final WeakReference<Player> p;
	public final UUID uuid;
	public final SkinServerHandler handler;

	public boolean quitWithoutSaving = false;
	public boolean loaded = false;

	private ArrayList<PlayerSkin> skins = null;
	public PlayerSkin mojangSkin = null;
	public PlayerSkin customSkin = null;

	public boolean showingOther = false;
	public String otherName = null;
	private ArrayList<PlayerSkin> otherSkins = null;
	public PlayerSkin otherMojangSkin = null;
	public PlayerSkin otherCustomSkin = null;

	ArrayList<Statue> shownStatues = new ArrayList<>();

	public PlayerSkins(SkinServerHandler handler, Player p) {
		this.p = new WeakReference<>(p);
		this.uuid = p.getUniqueId();
		this.handler = handler;
		this.skins = loadSkins(this.uuid);
	}

	private ArrayList<PlayerSkin> loadSkins(UUID uuid) {
		ArrayList<PlayerSkin> theSkins = new ArrayList<>();
		BufferedReader reader = null;
		try {
			File skinsFolder = new File(handler.plugin.getDataFolder(), "skins");
			File f = new File(skinsFolder, uuid.toString().replaceAll("-", "") + ".txt");
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String line2 = reader.readLine();
				if (line == null || line2 == null) {
					break;
				}
				if (line.equals("null") || line2.equals("null")) {
					line = line2 = null;
				}
				theSkins.add(new PlayerSkin(line, line2));
			}
			reader.close();
		} catch (Exception e) {
		} finally {
			tryClose(reader);
		}
		return theSkins;
	}

	public void loadOthers(UUID uuid, String name, PlayerSkin mojang, PlayerSkin custom) {
		otherSkins = loadSkins(uuid);
		this.otherName = name;
		this.otherMojangSkin = mojang;
		this.otherCustomSkin = custom;
		this.showingOther = true;
	}

	public PlayerSkin get(int index) {
		if (skins.size() > index) {
			PlayerSkin get = skins.get(index);
			if (get.skin == null) {
				return null;
			}
			return get;
		}
		return null;
	}

	public PlayerSkin getOther(int index) {
		if (otherSkins.size() > index) {
			PlayerSkin get = otherSkins.get(index);
			if (get.skin == null) {
				return null;
			}
			return get;
		}
		return null;
	}

	public void delete(int index) {
		set(index, null);
	}

	public void set(int index, PlayerSkin skin) {
		if (skin == null) {
			skin = new PlayerSkin(null, null);
		}
		if (skins.size() > index) {
			skins.set(index, skin);
			return;
		}
		while (skins.size() < index) {
			skins.add(new PlayerSkin(null, null));
		}
		skins.add(skin);
	}

	public void save() {
		File skinsFolder = new File(handler.plugin.getDataFolder(), "skins");
		if (!skinsFolder.isDirectory()) {
			if (skinsFolder.exists()) {
				skinsFolder.delete();
			}
			skinsFolder.mkdirs();
		}
		File f = new File(skinsFolder, uuid.toString().replaceAll("-", "") + ".txt");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			int nullCount = 0;
			for (PlayerSkin skin : skins) {
				String value = skin.skin;
				String signature = skin.signature;
				if (value == null || signature == null) {
					nullCount += 1;
					continue;
				}
				for (;nullCount > 0;nullCount--) {
					fos.write(("null\nnull\n").getBytes());
				}
				fos.write((value + "\n" + signature + "\n").getBytes());
			}
		} catch (Exception e) {
		} finally {
			tryClose(fos);
		}
	}

	int skinArraySize() {
		return skins.size();
	}
}
