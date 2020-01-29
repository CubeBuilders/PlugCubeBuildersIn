package hk.siggi.bukkit.plugcubebuildersin.module.minigamehub;

import java.io.File;

public interface MinigameMapLoader<M extends MinigameMap> {
	public M loadMap(File mapDirectory) throws MinigameMapLoadException;
}
