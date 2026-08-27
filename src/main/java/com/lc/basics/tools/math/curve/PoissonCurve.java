/*
 * Copyright (c) 2015 LogicielCote.COM Systems All rights reserved.
 *
 * @Author <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.basics.tools.math.curve;

// https://en.wikipedia.org/wiki/Poisson_distribution
public class PoissonCurve implements ProbabilityCurve
{
	private double		m_lambda;

	public PoissonCurve(double lambda)
	{
		m_lambda = lambda;
	}

	@Override
	public double getMean()
	{
		return m_lambda;
	}
	public double getLambda()
	{
		return m_lambda;
	}

	@Override
	public Double apply(Double x)
	{
		return getProbability(x.intValue());
	}
	public double getProbability(int k)
	{
		return Math.pow(m_lambda, k) * Math.exp(-m_lambda) / factorial(k);
	}
	private static double factorial(int n)
	{
		double		fact		= 1.0;
		for (int i = 1; i <= n; i++)
		{
			fact *= i;
		}
		return fact;
	}
	@Override
	public double getCumulative(double x)
	{
		return getCumulative(Double.valueOf(x).intValue());
	}
	public double getCumulative(int n)
	{
		double		sum			= 0.0;
		for (int i = 0; i <= n; i++)
		{
			sum += getProbability(i);
		}
		return sum;
	}

	public static void main(String[] args)
	{
		for (String arg : args)
		{
			double			lambda		= Double.parseDouble(arg);
			System.out.printf("Poisson(%6.1f)\n", lambda);
			PoissonCurve	poisson		= new PoissonCurve(lambda);
			show(poisson);
		}
	}
	private static void show(PoissonCurve poisson)
	{
		for (int i = 0; i < 30; i++)
		{
			double		prob		= poisson.getProbability(i);
			double		sum			= poisson.getCumulative(i);
			System.out.printf("%2d %6.4f %6.4f\n",
					i, prob, sum);
			if (1e-4 > (1.0 - sum))
			{
				break;
			}
		}
	}
}
