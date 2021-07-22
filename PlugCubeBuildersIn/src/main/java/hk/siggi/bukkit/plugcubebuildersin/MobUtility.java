package hk.siggi.bukkit.plugcubebuildersin;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class MobUtility {

	private MobUtility() {
	}

	private static final Map<Material, EntityType> materialToEntity = new HashMap<>();
	private static final Map<EntityType, Material> entityToMaterial = new HashMap<>();
	private static final Map<Class<? extends LivingEntity>, EntityType> classToEntity = new HashMap<>();

	public static boolean isEgg(Material material) {
		return materialToEntity.containsKey(material);
	}

	public static EntityType getMob(Material material) {
		return materialToEntity.get(material);
	}

	public static Material getEgg(EntityType entityType) {
		return entityToMaterial.get(entityType);
	}
	
	public static EntityType getMob(Class<? extends LivingEntity> clazz) {
		return classToEntity.get(clazz);
	}
	
	private static void add(Material material, EntityType entityType, Class<? extends LivingEntity> clazz){
		materialToEntity.put(material, entityType);
		entityToMaterial.put(entityType, material);
		classToEntity.put(clazz, entityType);
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

				// Only Mooshroom mismatches
				if (mob.equals("MOOSHROOM")) {
					entityType = EntityType.MUSHROOM_COW;
				} else {
					entityType = EntityType.valueOf(mob);
				}
				Class<? extends LivingEntity> clazz = (Class<LivingEntity>) entityType.getEntityClass();
				add(eggMaterial, entityType, clazz);
			} catch (Exception e) {
			}
		}
	}
}
