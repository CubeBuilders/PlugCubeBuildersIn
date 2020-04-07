package hk.siggi.bukkit.plugcubebuildersin;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class MobUtility {

	private MobUtility() {
	}

	private static final Map<Material, EntityType> materialToEntity = new HashMap<>();
	private static final Map<EntityType, Material> entityToMaterial = new HashMap<>();

	public static boolean isEgg(Material material) {
		return materialToEntity.containsKey(material);
	}

	public static EntityType getMob(Material material) {
		return materialToEntity.get(material);
	}

	public static Material getEgg(EntityType entityType) {
		return entityToMaterial.get(entityType);
	}
	
	private static void add(Material material, EntityType entityType){
		materialToEntity.put(material, entityType);
		entityToMaterial.put(entityType, material);
	}

	static {
		for (Material eggMaterial : Material.values()) {
			try {
				String name = eggMaterial.name();
				if (!name.endsWith("_SPAWN_EGG")) {
					continue;
				}
				String mob = name.substring(name.length() - 10);
				EntityType entityType;

				// Only Mooshroom and Zombie Pigman mismatch
				if (mob.equals("MOOSHROOM")) {
					entityType = EntityType.MUSHROOM_COW;
				} else if (mob.equals("ZOMBIE_PIGMAN")) {
					entityType = EntityType.PIG_ZOMBIE;
				} else {
					entityType = EntityType.valueOf(mob);
				}

				add(eggMaterial, entityType);
			} catch (Exception e) {
			}
		}
	}
}
