package com.lc.basics.tools.math.curve;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.function.Function;

public interface Curve extends Function<Double, Double>
{
	Random RANDOM = new Random();

	default Double getMinX()
	{
		return -Double.MAX_VALUE;
	}
	default Double getMaxX()
	{
		return Double.MAX_VALUE;
	}

	default Collection<CurvePoint> createPoints(double xMin, double xMax, double delta, double noiseMax)
	{
		Collection<CurvePoint> list = new ArrayList<>();
		for (double x = xMin; x < xMax; x += delta)
		{
			double y = apply(x);
			double noise = makeNoise(noiseMax);
			y = y + noise;
			CurvePoint point = new CurvePoint(x, y);
			list.add(point);
		}
		return list;
	}

	default double makeNoise(double noiseMax)
	{
		return noiseMax * (RANDOM.nextDouble() - 0.5);
	}

	default Double findXForward(double y, double xMin, double xMax, double deltaY)
	{
		while (true)
		{
			double x = (xMin + xMax) / 2;
			double yBack = apply(x);
			double delta = y - yBack;
			if (Math.abs(delta) < deltaY)
			{
				return x;
			}
			else if (delta < 0)
			{
				// yBack is too big
				xMax = x;
			}
			else
			{
				xMin = x;
			}
		}
	}
	default Double findXBackward(double y, double xMin, double xMax, double deltaY)
	{
		while (true)
		{
			double x = (xMin + xMax) / 2;
			double yBack = apply(x);
			double delta = y - yBack;
			if (Math.abs(delta) < deltaY)
			{
				return x;
			}
			else if (delta > 0)
			{
				// yBack is too big
				xMax = x;
			}
			else
			{
				xMin = x;
			}
		}
	}
}
