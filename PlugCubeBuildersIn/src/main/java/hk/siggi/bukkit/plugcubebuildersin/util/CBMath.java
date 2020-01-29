package hk.siggi.bukkit.plugcubebuildersin.util;

public class CBMath {

	private static final double[] sqrttable;

	static {
		sqrttable = new double[8000000];
		for (int i = 0; i < sqrttable.length; i++) {
			sqrttable[i] = Math.sqrt(((double) i) / 1000.0);
		}
	}

	/**
	 * Returns a square root for x, where x can be between 0 and 8000. This uses
	 * a lookup table, and so the answer won't be exact.
	 *
	 * @param x value to find the square root of
	 * @return square root of x
	 */
	public static double sqrt(double x) {
		if (x == 0.0) {
			return 0.0;
		}
		if (x < 0.0) {
			return Double.NaN;
		}
		int theSquare = (int) Math.round(x * 1000.0);
		if (sqrttable.length <= theSquare) {
			return Double.NaN;
		}
		return sqrttable[theSquare];
	}
}
