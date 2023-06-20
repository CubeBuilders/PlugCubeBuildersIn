package hk.siggi.bukkit.plugcubebuildersin.module.minigamehub;

import static hk.siggi.bukkit.plugcubebuildersin.module.minigamehub.MinigameUtil.read;
import com.google.gson.GsonBuilder;
import io.siggi.nbt.NBTCompound;
import io.siggi.nbt.NBTTool;
import io.siggi.nbt.NBTToolBukkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class MinigameMap {

	private final String name;
	private final List<MinigameMapArtist> artists;
	private final NBTCompound icon;

	protected MinigameMap(File f) {
		this.name = f.getName();
		List<MinigameMapArtist> artistsList = new ArrayList<>();
		File artistsInfoFile = new File(f, "artists.txt");
		FileInputStream in = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in = new FileInputStream(artistsInfoFile)));
			String line;
			while ((line = reader.readLine()) != null) {
				String artistName;
				String credit;
				int eqPos = line.indexOf("=");
				if (eqPos == -1) {
					artistName = line.trim();
					credit = null;
				} else {
					artistName = line.substring(0, eqPos).trim();
					credit = line.substring(eqPos + 1).trim();
				}
				String uuidFromString = artistName;
				UUID uuid;
				int splitPos = artistName.indexOf("|");
				if (splitPos != -1) {
					uuidFromString = artistName.substring(0, splitPos).trim();
					artistName = artistName.substring(splitPos + 1).trim();
				}
				try {
					uuid = UUID.fromString(uuidFromString.replace("-", "").replaceAll("([0-9A-Fa-f]{8})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{12})", "$1-$2-$3-$4-$5"));
					if (uuidFromString.equals(artistName)) {
						artistName = null;
					}
				} catch (Exception e) {
					uuid = null;
				}
				artistsList.add(uuid == null ? new MinigameMapArtist(artistName, credit) : (artistName == null ? new MinigameMapArtist(uuid, credit) : new MinigameMapArtist(artistName, uuid, credit)));
			}
		} catch (Exception e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
				in = null;
			}
		}
		artists = Collections.unmodifiableList(artistsList);
		File iconFile = new File(f, "icon.json");
		if (iconFile.exists()) {
			this.icon = NBTTool.registerTo(new GsonBuilder()).create().fromJson(read(iconFile), NBTCompound.class);
		} else {
			this.icon = null;
		}
	}

	public final String getName() {
		return name;
	}

	public final List<MinigameMapArtist> getArtists() {
		return artists;
	}

	public final ItemStack getIcon() {
		ItemStack stack;
		try {
			stack = NBTToolBukkit.itemFromNBT(icon);
		} catch (Exception e) {
			stack = new ItemStack(Material.STONE);
		}
		ItemMeta itemMeta = stack.getItemMeta();
		itemMeta.setDisplayName(ChatColor.RESET + name);
		stack.setItemMeta(itemMeta);
		return stack;
	}
}
