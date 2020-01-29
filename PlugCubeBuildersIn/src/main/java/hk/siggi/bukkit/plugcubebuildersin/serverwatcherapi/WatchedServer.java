package hk.siggi.bukkit.plugcubebuildersin.serverwatcherapi;

import com.google.gson.Gson;

public class WatchedServer {
	transient ServerWatcherAPI api = null;
	public boolean running = false;
	public String serverKind = null;
	public int pid = -1;
	public int players = 0;
	public int entities = 0;
	public int chunks = 0;
	public double avgTick = 0.0;
	public double maxTick = 0.0;
	public int over50 = 0;
	public long allocMem = 0L;
	public long usedMem = 0L;
	public long freeMem = 0L;
	public long maxMem = 0L;
	public long startTime = 0L;
	public int status = 0;
	public WatchedServer() {
	}
	public boolean start(){return api.start(serverKind);}
	public boolean restart(){return api.restart(serverKind);}
	public boolean restartOnEmpty(){return api.restartOnEmpty(serverKind);}
	public boolean stop(){return api.stop(serverKind);}
	public boolean stopOnEmpty(){return api.stopOnEmpty(serverKind);}
	public boolean kill(){return api.kill(serverKind);}
	public String toJson() {
		return gson.toJson(this);
	}
	public static String toJson(WatchedServer[] watchers) {
		return gson.toJson(watchers);
	}
	public WatchedServer fromJson(String json) {
		return gson.fromJson(json, WatchedServer.class);
	}
	public WatchedServer[] fromJsonArray(String json) {
		return gson.fromJson(json, WatchedServer[].class);
	}
	private static final Gson gson;
	static {
		gson = new Gson();
	}
	public static Gson getGson(){return gson;}
}
