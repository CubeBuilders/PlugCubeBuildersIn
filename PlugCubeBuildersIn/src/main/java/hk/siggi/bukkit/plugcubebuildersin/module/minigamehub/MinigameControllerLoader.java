package hk.siggi.bukkit.plugcubebuildersin.module.minigamehub;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import static hk.siggi.bukkit.plugcubebuildersin.util.Util.tryClose;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MinigameControllerLoader {

	private static File minigameDirectory = null;

	public static File getMinigameDirectory() {
		if (minigameDirectory == null) {
			File dataFolder = PlugCubeBuildersIn.getInstance().getDataFolder();
			minigameDirectory = new File(dataFolder, "minigames");
			if (!minigameDirectory.isDirectory()) {
				if (minigameDirectory.exists()) {
					minigameDirectory.delete();
				}
				minigameDirectory.mkdirs();
			}
		}
		return minigameDirectory;
	}

	private static final Map<String, WeakReference<Class<? extends MinigameController>>> classCache = new HashMap<>();

	public static List<MinigameController> getMinigameControllers() {
		List<MinigameController> controllers = new LinkedList<>();
		try {
			File[] allFiles = MinigameControllerLoader.getMinigameDirectory().listFiles();
			for (File file : allFiles) {
				MinigameController loadMinigameController = MinigameControllerLoader.loadMinigameController(file);
				if (loadMinigameController == null) {
					continue;
				}
				controllers.add(loadMinigameController);
			}
		} catch (Exception e) {
		}
		return controllers;
	}

	private static MinigameController loadMinigameController(File f) {
		File reloadFile = new File(getMinigameDirectory(), "reload");
		if (reloadFile.exists()) {
			classCache.clear();
			reloadFile.delete();
		}
		try {
			String fileName = f.getName();
			if (!fileName.toLowerCase().endsWith(".jar")) {
				return null;
			}
			String minigameName = fileName.substring(0, fileName.length() - 4);
			Class<? extends MinigameController> clazz = null;
			WeakReference<Class<? extends MinigameController>> clazzRef = classCache.get(minigameName);
			if (clazzRef != null) {
				clazz = clazzRef.get();
			}
			if (clazz == null) {
				String mainClassName = getMainClass(f);
				URLClassLoader classLoader = new URLClassLoader(new URL[]{f.toURI().toURL()}, MinigameControllerLoader.class.getClassLoader());
				classCache.put(minigameName, new WeakReference<Class<? extends MinigameController>>(clazz = (Class<? extends MinigameController>) classLoader.loadClass(mainClassName)));
			}
			return clazz.newInstance();
		} catch (Exception e) {
			return null;
		}
	}

	private static String getMainClass(File f) {
		JarFile jf = null;
		try {
			jf = new JarFile(f);
			JarEntry entry = jf.getJarEntry("minigame-main.txt");
			InputStream in = jf.getInputStream(entry);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			return reader.readLine();
		} catch (Exception e) {
		} finally {
			tryClose(jf);
		}
		return null;
	}
}
