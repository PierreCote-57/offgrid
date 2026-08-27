package com.lc.basics.tools.math.curve;

public class PolynomialCurve implements Curve
{
	private double[] m_coefficientList;

	public PolynomialCurve(double ... coefficientList)
	{
		m_coefficientList = coefficientList;
	}
	public PolynomialCurve(PolynomialCurve that, double noiseMax)
	{
		m_coefficientList = new double[that.getCoefficientCount()];
		for (int i = 0; i < that.getCoefficientCount(); i++)
		{
			double value = that.getCoefficient(i);
			setCoefficient(i, value + makeNoise(noiseMax));
		}
	}


	public int getCoefficientCount()
	{
		return m_coefficientList.length;
	}
	public double getCoefficient(int i)
	{
		return m_coefficientList[i];
	}
	public void setCoefficient(int i, double value)
	{
		m_coefficientList[i] = value;
	}

	@Override
	public Double apply(Double x)
	{
		double value = 0;
		double xN = 1.0;
		for (int i = 0; i < getCoefficientCount(); i++)
		{
			value += (xN * getCoefficient(i));
			xN *= x;
		}
		return value;
	}

	@Override
	public String toString()
	{
		StringBuilder		sb		= new StringBuilder();
		sb.append("y = ");
		String				prefix	= " ";
		for (int i = 0; i < getCoefficientCount(); i++)
		{
			sb.append(prefix);
			sb.append(String.format("%.6f * x^%d", getCoefficient(i), i));
			prefix = " + ";
		}
		return sb.toString();
	}
}
