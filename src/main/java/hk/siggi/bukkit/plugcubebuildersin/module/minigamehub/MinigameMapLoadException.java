package hk.siggi.bukkit.plugcubebuildersin.module.minigamehub;

/**
 * Thrown when a map directory is invalid.
 *
 * @author Siggi
 */
public class MinigameMapLoadException extends Exception {
	public MinigameMapLoadException() {super();}
	public MinigameMapLoadException(String msg) {super(msg);}
	public MinigameMapLoadException(Throwable cause) {super(cause);}
	public MinigameMapLoadException(String msg, Throwable cause) {super(msg, cause);}
}
