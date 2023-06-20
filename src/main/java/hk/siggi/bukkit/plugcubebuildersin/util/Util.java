package hk.siggi.bukkit.plugcubebuildersin.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Server;

public class Util {

	private Util() {
	}

	public static UUID uuidFromString(String str) {
		return UUID.fromString(str.replace("-", "").replaceAll("([0-9A-Fa-f]{8})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{12})", "$1-$2-$3-$4-$5"));
	}

	public static String toHex(Color color) {
		StringBuilder builder = new StringBuilder();
		String r = Integer.toString(color.getRed(), 16);
		String g = Integer.toString(color.getGreen(), 16);
		String b = Integer.toString(color.getBlue(), 16);
		if (r.length() != 2) {
			builder.append("0");
		}
		builder.append(r);
		if (g.length() != 2) {
			builder.append("0");
		}
		builder.append(g);
		if (b.length() != 2) {
			builder.append("0");
		}
		builder.append(b);
		return builder.toString();
	}

	public static Color fromHex(String hex) {
		try {
			return Color.fromRGB(Integer.parseInt(hex.substring(0, 2), 16),
					Integer.parseInt(hex.substring(2, 4), 16),
					Integer.parseInt(hex.substring(4, 6), 16));
		} catch (Exception e) {
			return null;
		}
	}

	public static byte[] readToByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int c;
		byte[] b = new byte[4096];
		while ((c = in.read(b, 0, b.length)) != -1) {
			baos.write(b, 0, c);
		}
		return baos.toByteArray();
	}

	public static String readToString(InputStream in) throws IOException {
		return new String(readToByteArray(in));
	}

	public static byte[] readToByteArray(File in) throws IOException {
		FileInputStream inStream = null;
		try {
			inStream = new FileInputStream(in);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int c;
			byte[] b = new byte[4096];
			while ((c = inStream.read(b, 0, b.length)) != -1) {
				baos.write(b, 0, c);
			}
			return baos.toByteArray();
		} finally {
			tryClose(inStream);
		}
	}

	public static String readToString(File in) throws IOException {
		return new String(readToByteArray(in));
	}

	public static void writeToFile(File file, byte[] data) throws IOException {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(data);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public static void writeToFile(File file, String data) throws IOException {
		writeToFile(file, data.getBytes());
	}

	public static void compressFile(File file) {
		OSSpecificUtil.get().compressFile(file);
	}

	public static long parseTime(String input, long relativeTo) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		int currentYear = Integer.parseInt(sdf.format(new Date(System.currentTimeMillis())));
		long foundTime = -1L;
		try {
			findTime:
			if (input.startsWith("+") || input.startsWith("-")) {
				boolean negate = false;
				if (input.startsWith("-")) {
					negate = true;
				}
				String[] backtrack = input.substring(1).split(":");
				int days;
				int hours;
				int minutes;
				int seconds;
				if (backtrack.length == 4) {
					days = Integer.parseInt(backtrack[0]);
					hours = Integer.parseInt(backtrack[1]);
					minutes = Integer.parseInt(backtrack[2]);
					seconds = Integer.parseInt(backtrack[3]);
				} else if (backtrack.length == 3) {
					days = 0;
					hours = Integer.parseInt(backtrack[0]);
					minutes = Integer.parseInt(backtrack[1]);
					seconds = Integer.parseInt(backtrack[2]);
				} else if (backtrack.length == 2) {
					days = 0;
					hours = 0;
					minutes = Integer.parseInt(backtrack[0]);
					seconds = Integer.parseInt(backtrack[1]);
				} else if (backtrack.length == 1) {
					days = 0;
					hours = 0;
					minutes = 0;
					seconds = Integer.parseInt(backtrack[0]);
				} else {
					break findTime;
				}
				GregorianCalendar c = new GregorianCalendar();
				c.setTimeInMillis(relativeTo);
				c.add(Calendar.SECOND, negate ? -seconds : seconds);
				c.add(Calendar.MINUTE, negate ? -minutes : minutes);
				c.add(Calendar.HOUR, negate ? -hours : hours);
				c.add(Calendar.DAY_OF_MONTH, negate ? -days : days);
				foundTime = c.getTimeInMillis();
			} else {
				String[] pieces = input.split("-", 3);
				String date, time, timezone;
				if (pieces.length == 2) {
					date = pieces[0];
					time = pieces[1];
					timezone = "GMT";
				} else if (pieces.length == 3) {
					date = pieces[0];
					time = pieces[1];
					timezone = pieces[2];
				} else {
					break findTime;
				}
				String[] timePieces = time.split(":");
				String[] datePieces = date.split("/");

				if (timePieces.length != 3) {
					break findTime;
				} else if (datePieces.length < 2 || datePieces.length > 3) {
					break findTime;
				}
				int hour = Integer.parseInt(timePieces[0]);
				int min = Integer.parseInt(timePieces[1]);
				int sec = Integer.parseInt(timePieces[2]);
				int day, month, year;
				if (datePieces.length == 2) {
					year = currentYear;
					month = Integer.parseInt(datePieces[0]);
					day = Integer.parseInt(datePieces[1]);
				} else if (datePieces.length == 3) {
					year = Integer.parseInt(datePieces[0]);
					month = Integer.parseInt(datePieces[1]);
					day = Integer.parseInt(datePieces[2]);
					if (year < 100) {
						int rollover = currentYear - 50;
						int prefix = ((currentYear / 100) * 100);
						rollover = rollover - prefix;
						if (year > rollover) {
							year += prefix;
						} else {
							year += (prefix + 100);
						}
					}
				} else {
					break findTime;
				}
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeZone(TimeZone.getTimeZone(timezone));
				cal.set(year, month - 1, day, hour, min, sec);
				foundTime = cal.getTimeInMillis();
			}
		} catch (Exception e) {
		}
		return foundTime;
	}

	public static String unparseTime(long date) {
		return unparseTime(date, "GMT");
	}

	public static String unparseTime(long date, String timezone) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone(timezone));
		return sdf.format(new Date(date)) + "-" + timezone;
	}

	public static void tryClose(Closeable... closeable) {
		if (closeable == null) {
			return;
		}
		for (Closeable c : closeable) {
			if (c == null) {
				continue;
			}
			try {
				c.close();
			} catch (Exception e) {
			}
		}
	}

	public static boolean same(Object a, Object b) {
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return false;
		}
		return a.equals(b);
	}

	static {
		int major;
		int minor;
		int r;
		try {
			Server server = Bukkit.getServer();
			String name = server.getClass().getName();
			int pos = name.indexOf(".v");
			int x = pos + 2;
			int y = name.indexOf("_", x);
			int z = name.indexOf("_R", y + 1);
			int w = name.indexOf(".", z + 2);
			major = Integer.parseInt(name.substring(x, y));
			minor = Integer.parseInt(name.substring(y + 1, z));
			r = Integer.parseInt(name.substring(z + 2, w));
		} catch (Exception e) {
			major = 0;
			minor = 0;
			r = 0;
		}
		versionMajor = major;
		versionMinor = minor;
		versionR = r;
	}
	public static final int versionMajor;
	public static final int versionMinor;
	public static final int versionR;
}
