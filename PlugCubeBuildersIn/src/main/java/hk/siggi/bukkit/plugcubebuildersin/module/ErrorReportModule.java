package hk.siggi.bukkit.plugcubebuildersin.module;

public interface ErrorReportModule extends Module {
	public void reportProblem(String message, Throwable t);
}
