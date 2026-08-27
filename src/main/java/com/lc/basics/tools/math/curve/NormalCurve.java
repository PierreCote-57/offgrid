/*
 * Copyright (c) 2015 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.math.curve;


import com.lc.basics.monitoring.statistics.SimpleAccumulator;
import com.lc.basics.tools.logging.BasicLogger;

// https://en.wikipedia.org/wiki/Normal_distribution
public class NormalCurve implements ProbabilityCurve
{
	private static final BasicLogger LOGGER = BasicLogger.getLogger(NormalCurve.class);

	private static final double[]		CUMULATIVE_BY_STD		=
			{
					0.0,				// +/- 0 STD
					0.682689492137,		// +/- 1 STD
					0.954499736104,		// +/- 2 STD
					0.997300203937,		// +/- 3 STD
					0.999936657516,		// +/- 4 STD
					0.999999426697,		// +/- 5 STD
					0.999999998027		// +/- 6 STD
			};

	private double		m_mean;
	private double		m_std;
	private double		m_divisor;

	public NormalCurve(double mean, double std)
	{
		init(mean, std);
	}
	private void init(double mean, double std)
	{
		m_mean		= mean;
		m_std		= std;
		m_divisor	= m_std * Math.sqrt(2 * Math.PI);
	}

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}

	@Override
	public double getMean()
	{
		return m_mean;
	}
	public void setMean(double mean)
	{
		init(mean, getStd());
	}

	public double getStd()
	{
		return m_std;
	}
	public void setStd(double std)
	{
		init(getMean(), std);
	}

	@Override
	public Double apply(Double x)
	{
		double		offset		= (x - m_mean) / m_std;
		double		exponent	= -(offset * offset) / 2;
		double		value		= Math.exp(exponent) / m_divisor;

		return value;
	}
	@Override
	public  double getCumulative(double x)
	{
		double		sum		= 0.0;
		for (int i = 0; i < x; i++)
		{
			sum += apply((double) i);
		}
		return sum;
	}

	public static void analyseCountList(String prefix, int[] countList)
	{
		double		mean				= measureMean(prefix, countList, true);
		double		std					= Math.sqrt(mean);
		int[]		countByStdList		= new int[CUMULATIVE_BY_STD.length];

		for (int value : countList)
		{
			double delta = Math.abs(value - mean);
			double relative = delta / std;
			int stdCount = Double.valueOf(relative).intValue();
			stdCount = Math.min(stdCount, CUMULATIVE_BY_STD.length - 1);
			countByStdList[stdCount]++;
		}

		int				cumulativeCountActual		= 0;
		double			cumulativeCountExpected		= 0.0;
		for (int iSTD = 1; iSTD < CUMULATIVE_BY_STD.length; iSTD++)
		{
			double		probabilitySlice			= CUMULATIVE_BY_STD[iSTD] - CUMULATIVE_BY_STD[iSTD - 1];
			double		countSliceExpected			= probabilitySlice * countList.length;
			cumulativeCountExpected					+= countSliceExpected;
			double		probabilityInsideExpected	= cumulativeCountExpected * 100.0 / countList.length;
			double		countOutsideExpected		= countList.length - cumulativeCountExpected;
			double		probabilityOutsideExpected	= countOutsideExpected * 100.0 / countList.length;

			int			countSliceActual			= countByStdList[iSTD - 1];
			cumulativeCountActual					+= countSliceActual;
			double		probabilityInsideActual		= cumulativeCountActual * 100.0 / countList.length;
			int			countOutsideActual			= countList.length - cumulativeCountActual;
			double		probabilityOutsideActual	= countOutsideActual * 100.0 / countList.length;

			double		countDelta					= Math.abs(cumulativeCountActual - cumulativeCountExpected);
			double		deltaPercent				= countDelta * 100.0 / cumulativeCountExpected;
			String result						= deltaPercent < 5 ? "    Success    " : "*** Failure ***";

			getLogger().info(
//					"Slice +/- %1$1d STD: %2$s Expected %3$,10.1f + %4$,10.1f (%5$12.8f %% + %6$12.8f %%), "
//					+ "Got %7$,10d (%8$+7.2f%%) + %9$,10d (%10$+7.2f%%) (%11$12.8f %% + %12$12.8f %%)",
					"Slice +/- %1$1d STD: %2$s Expected (%5$12.8f %%), "
					+                      "Got (%11$12.8f %%)",
					iSTD, result,
					cumulativeCountExpected, countOutsideExpected,
					probabilityInsideExpected, probabilityOutsideExpected,
					cumulativeCountActual, (cumulativeCountActual - cumulativeCountExpected) * 100.0 / cumulativeCountExpected,
					countOutsideActual, (countOutsideActual - countOutsideExpected) * 100.0 / countOutsideExpected,
					probabilityInsideActual, probabilityOutsideActual
			);
		}
	}
	public static void analyseHistogram(String prefix, long[] histogram)
	{
		SimpleAccumulator accumulator			= new SimpleAccumulator();
		for (int i = 0; i < histogram.length; i++)
		{
			for (int j = 0; j < histogram[i]; j++)
			{
				accumulator.record(i);
			}
		}
		double				mean	= accumulator.getAvg();
		double				std		= accumulator.getStd();
		getLogger().info("%s: Found average = %,14.1f; STD = %,14.1f",
				prefix, accumulator.getAvg(), accumulator.getStd());

		long[]					countByStdList		= new long[CUMULATIVE_BY_STD.length];
		for (int i = 0; i < histogram.length; i++)
		{
			int			value		= i;
			double		delta		= Math.abs(value - mean);
			double		relative	= delta / std;
			int			stdCount	= Double.valueOf(relative).intValue();
			stdCount = Math.min(stdCount, CUMULATIVE_BY_STD.length - 1);
			countByStdList[stdCount] += histogram[i];
		}

		for (int i = 0; i < countByStdList.length; i++)
		{
			getLogger().info("CountByStdList[%d] = %,15d", i, countByStdList[i]);
		}

		int				cumulativeCountActual		= 0;
		double			cumulativeCountExpected		= 0.0;
		for (int iSTD = 1; iSTD < CUMULATIVE_BY_STD.length; iSTD++)
		{
			double		probabilitySlice			= CUMULATIVE_BY_STD[iSTD] - CUMULATIVE_BY_STD[iSTD - 1];
			double		countSliceExpected			= probabilitySlice * accumulator.getCount();
			cumulativeCountExpected					+= countSliceExpected;
			double		probabilityInsideExpected	= cumulativeCountExpected * 100.0 / accumulator.getCount();
			double		countOutsideExpected		= accumulator.getCount() - cumulativeCountExpected;
			double		probabilityOutsideExpected	= countOutsideExpected * 100.0 / accumulator.getCount();

			long		countSliceActual			= countByStdList[iSTD - 1];
			cumulativeCountActual					+= countSliceActual;
			double		probabilityInsideActual		= cumulativeCountActual * 100.0 / accumulator.getCount();
			long		countOutsideActual			= accumulator.getCount() - cumulativeCountActual;
			double		probabilityOutsideActual	= countOutsideActual * 100.0 / accumulator.getCount();

			double		countDelta					= Math.abs(cumulativeCountActual - cumulativeCountExpected);
			double		deltaPercent				= countDelta * 100.0 / cumulativeCountExpected;
			String		result						= deltaPercent < 5 ? "    Success    " : "*** Failure ***";

			getLogger().info(
//					"Slice +/- %1$1d STD: %2$s Expected %3$,10.1f + %4$,10.1f (%5$12.8f %% + %6$12.8f %%), "
//					+ "Got %7$,10d (%8$+7.2f%%) + %9$,10d (%10$+7.2f%%) (%11$12.8f %% + %12$12.8f %%)",
					"Slice +/- %1$1d STD: %2$s Expected (%5$12.8f %%), "
							+                      "Got (%11$12.8f %%)",
					iSTD, result,
					cumulativeCountExpected, countOutsideExpected,
					probabilityInsideExpected, probabilityOutsideExpected,
					cumulativeCountActual, (cumulativeCountActual - cumulativeCountExpected) * 100.0 / cumulativeCountExpected,
					countOutsideActual, (countOutsideActual - countOutsideExpected) * 100.0 / countOutsideExpected,
					probabilityInsideActual, probabilityOutsideActual
			);
		}
	}
	private static double measureMean(String prefix, int[] countList, boolean inform)
	{
		SimpleAccumulator accumulator		= new SimpleAccumulator();
		for (int j : countList)
		{
			accumulator.record(j);
		}
		double					mean			= accumulator.getAvg();
		double					std				= accumulator.getStd();
		double					sqrt			= Math.sqrt(mean);
		double					deltaPercent	= (std - sqrt) * 100.0 / sqrt;
		if (inform)
		{
			getLogger().info("%s: Found average = %,14.1f; STD = %,14.1f; SQRT(mean) = %,14f; Delta(sqrt) = %,5.2f %%",
					prefix, mean, std, sqrt, deltaPercent);
		}
		return mean;
	}

	public static int getCumulativeStdCountMin()
	{
		return 1;
	}
	public static int getCumulativeStdCountMax()
	{
		return CUMULATIVE_BY_STD.length;
	}
	public static double getCumulativeByStd(int stdCount)
	{
		return CUMULATIVE_BY_STD[stdCount - 1];
	}

	@Override
	public String toString()
	{
		return String.format("Normal(Mu = %.3f; Sigma = %.3f)", getMean(), getStd());
	}

	public static void main(String[] args)
	{
		for (String arg : args)
		{
			String[]		parts	= arg.split(",");
			double			mean	= Double.parseDouble(parts[0]);
			double			std		= Double.parseDouble(parts[1]);
			System.out.printf("Normal(%6.1f, %6.1f)%n", mean, std);
			NormalCurve		normal		= new NormalCurve(mean, std);
			show(normal);
		}
	}
	private static void show(NormalCurve normal)
	{
		int			stdCount	= 3;
		int			iMax		= 10 * stdCount;
		double		min			= normal.getMean() - stdCount * normal.getStd();
		double		delta		= 2 * stdCount * normal.getStd() / iMax;

		for (int i = 0; i <= 30; i++)
		{
			double		x			= min + (i * delta);
			double		prob		= normal.apply(x);
			System.out.printf("%2d %6.4f %6.4f%n",
					i, x, prob);
		}
	}
}
