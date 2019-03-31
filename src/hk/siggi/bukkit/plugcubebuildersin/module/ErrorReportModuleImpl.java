package hk.siggi.bukkit.plugcubebuildersin.module;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import static hk.siggi.bukkit.plugcubebuildersin.util.Util.tryClose;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ErrorReportModuleImpl implements ErrorReportModule {

	private PlugCubeBuildersIn plugin;

	private final Object errorReportLock = new Object();
	private final Map<String, File> existingErrorReports = new HashMap<>();

	private File errorReportDir = null;

	private File getErrorReportDir() {
		if (errorReportDir == null) {
			errorReportDir = new File(plugin.getDataFolder(), "error-reports");
		}
		return errorReportDir;
	}

	@Override
	public void reportProblem(String message, Throwable t) {
		synchronized (errorReportLock) {
			CharArrayWriter caw = new CharArrayWriter();
			PrintWriter pw = new PrintWriter(caw);
			t.printStackTrace(pw);
			pw.flush();
			caw.flush();
			String stacktrace = caw.toString().trim();
			if (stacktrace.contains("\r")) {
				stacktrace = stacktrace.replace("\r\n", "\n").replace("\r", "\n");
			}
			String errorReport = message + "\n\n" + stacktrace;
			FileOutputStream fos = null;
			try {
				java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA1");
				byte[]bb=errorReport.getBytes();
				md.update(bb);
				byte[] digest = md.digest();
				String sha1 = byteToHex(digest);
				File check = existingErrorReports.get(sha1);
				if (check != null && check.exists()) {
					return;
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
				String formattedDate = sdf.format(new Date(System.currentTimeMillis()));
				File outFile = new File(getErrorReportDir(), formattedDate + ".txt");
				int incr = 0;
				while (outFile.exists()) {
					incr += 1;
					outFile = new File(getErrorReportDir(), formattedDate + "-" + incr + ".txt");
				}
				fos = new FileOutputStream(outFile);
				fos.write(bb);
				existingErrorReports.put(sha1, outFile);
			} catch (NoSuchAlgorithmException | IOException e) {
			} finally {
				tryClose(fos);
			}
		}
	}

	private void readErrorReports() {
		synchronized (errorReportLock) {
			try {
				File reportDir = getErrorReportDir();
				File[] ff = reportDir.listFiles();
				if (ff == null) {
					return;
				}
				byte[] b = new byte[8388608];
				int c;
				for (File f : ff) {
					String name = f.getName();
					if (!name.endsWith(".txt")) {
						continue;
					}
					FileInputStream in = null;
					try {
						in = new FileInputStream(f);
						java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA1");
						while ((c = in.read(b, 0, b.length)) != -1) {
							md.update(b, 0, c);
						}
						byte[] digest = md.digest();
						String sha1 = byteToHex(digest);
						existingErrorReports.put(sha1, f);
					} catch (NoSuchAlgorithmException | IOException e) {
					} finally {
						tryClose(in);
					}
				}
			} catch (Exception e) {
			}
		}
	}

	private String byteToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			int c = ((int) b) & 0xff;
			if (c < 0x10) {
				sb.append("0");
			}
			sb.append(Integer.toString(c, 16));
		}
		return sb.toString();
	}

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
		File d = getErrorReportDir();
		if (!d.isDirectory()) {
			if (d.exists()) {
				d.delete();
			}
			d.mkdirs();
		}
		readErrorReports();
	}

	@Override
	public void init() {
	}

	@Override
	public void kill() {
	}

	@Override
	public void tick() {
	}
}
