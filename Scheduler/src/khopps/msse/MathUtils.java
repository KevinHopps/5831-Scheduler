package khopps.msse;

public class MathUtils
{

	/**
	 * @param x
	 * @param y
	 * @return least common multiple of x and y (0 if either is zero)
	 */
	public static int lcm(int x, int y)
	{
		if (x < 0)
			x = -x;

		if (y < 0)
			y = -y;

		int result = lcmPositive(x, y);

		return result;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return greatest common divisor of x and y
	 */
	public static int gcd(int x, int y)
	{
		if (x < 0)
			x = -x;

		if (y < 0)
			y = -y;

		int result = gcdPositive(x, y);
		return result;
	}

	public static int lcm(int[] values)
	{
		int result = 0;

		if (values.length > 0)
		{
			int[] positives = positives(values);
			result = lcmPositives(positives);
		}

		return result;
	}

	public static int gcd(int[] values)
	{
		int result = 0;

		if (values.length > 0)
		{
			int[] positives = positives(values);
			result = gcdPositives(positives);
		}

		return result;
	}

	private static int gcdPositives(int[] positives)
	{
		int result = positives[0];
		for (int i = 1; result != 1 && i < positives.length; ++i)
			result = gcdPositive(positives[i], result);
		return result;
	}

	private static int lcmPositives(int[] positives)
	{
		int result = positives[0];
		for (int i = 1; result > 1 && i < positives.length; ++i)
			result = lcmPositive(positives[i], result);
		return result;
	}

	private static int gcdPositive(int x, int y)
	{
		int larger = x;
		int smaller = y;

		if (y > x)
		{
			larger = y;
			smaller = x;
		}

		while (smaller > 0)
		{
			int r = larger % smaller;
			larger = smaller;
			smaller = r;
		}

		return larger;
	}

	private static int lcmPositive(int x, int y)
	{
		int gcd = gcdPositive(x, y);
		int result = (x / gcd) * y;
		return result;
	}

	private static int[] positives(int[] values)
	{
		int[] positives = new int[values.length];

		for (int i = 0; i < values.length; ++i)
		{
			positives[i] = Math.abs(values[i]);
		}

		return positives;
	}

	/**
	 * @param x
	 * @param y
	 * @return least common multiple of x and y (0 if either is zero)
	 */
	public static long lcm(long x, long y)
	{
		if (x < 0)
			x = -x;

		if (y < 0)
			y = -y;

		long result = lcmPositive(x, y);

		return result;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return greatest common divisor of x and y
	 */
	public static long gcd(long x, long y)
	{
		if (x < 0)
			x = -x;

		if (y < 0)
			y = -y;

		long result = gcdPositive(x, y);
		return result;
	}

	public static long lcm(long[] values)
	{
		long result = 0;

		if (values.length > 0)
		{
			long[] positives = positives(values);
			result = lcmPositives(positives);
		}

		return result;
	}

	public static long gcd(long[] values)
	{
		long result = 0;

		if (values.length > 0)
		{
			long[] positives = positives(values);
			result = gcdPositives(positives);
		}

		return result;
	}

	private static long gcdPositives(long[] positives)
	{
		long result = positives[0];
		for (int i = 1; result != 1 && i < positives.length; ++i)
			result = gcdPositive(positives[i], result);
		return result;
	}

	private static long lcmPositives(long[] positives)
	{
		long result = positives[0];
		for (int i = 1; result > 1 && i < positives.length; ++i)
			result = lcmPositive(positives[i], result);
		return result;
	}

	private static long gcdPositive(long x, long y)
	{
		long larger = x;
		long smaller = y;

		if (y > x)
		{
			larger = y;
			smaller = x;
		}

		while (smaller > 0)
		{
			long r = larger % smaller;
			larger = smaller;
			smaller = r;
		}

		return larger;
	}

	private static long lcmPositive(long x, long y)
	{
		long gcd = gcdPositive(x, y);
		long result = (x / gcd) * y;
		return result;
	}

	private static long[] positives(long[] values)
	{
		long[] positives = new long[values.length];

		for (int i = 0; i < values.length; ++i)
		{
			positives[i] = Math.abs(values[i]);
		}

		return positives;
	}

}
