package hk.siggi.bukkit.plugcubebuildersin.serverwatcherapi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ServerWatcherAPI {

	private final String address;

	public ServerWatcherAPI(String address) {
		this.address = address;
	}

	public WatchedServer[] getServers() {
		try {
			WatchedServer[] result = WatchedServer.getGson().fromJson(load("http://" + address + ":8183/api/servers"), WatchedServer[].class);
			for (WatchedServer ws : result) {
				ws.api = this;
			}
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	public String createServer(String template, String group) {
		String result = load("http://" + address + ":8183/api/create?template=" + template + "&group=" + group).trim();
		if (result != null && result.isEmpty()) {
			result = null;
		}
		return result;
	}

	public boolean start(String serverKind) {
		return command(serverKind, "start");
	}

	public boolean restart(String serverKind) {
		return command(serverKind, "restart");
	}

	public boolean restartOnEmpty(String serverKind) {
		return command(serverKind, "restartonempty");
	}

	public boolean stop(String serverKind) {
		return command(serverKind, "stop");
	}

	public boolean stopOnEmpty(String serverKind) {
		return command(serverKind, "stoponempty");
	}

	public boolean kill(String serverKind) {
		return command(serverKind, "kill");
	}

	private boolean command(String serverKind, String command) {
		return load("http://" + address + ":8183/api/control/" + serverKind + "?action=" + command).trim().equals("1");
	}

	private String load(String addr) {
		InputStream in = null;
		try {
			URL url = new URL(addr);
			URLConnection urlc = url.openConnection();
			in = urlc.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] b = new byte[4096];
			int c;
			while ((c = in.read(b, 0, b.length)) != -1) {
				out.write(b, 0, c);
			}
			return out.toString();
		} catch (Exception e) {
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
	}

	private Object load() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
