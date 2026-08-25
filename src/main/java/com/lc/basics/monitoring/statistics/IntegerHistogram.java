package com.lc.basics.monitoring.statistics;

import java.util.ArrayList;
import java.util.List;

public class IntegerHistogram
{
	private int count;
	private int[] countList;

	public IntegerHistogram(int iMax)
	{
		count = 0;
		countList = new int[iMax + 1];
	}

	public int getCount()
	{
		return count;
	}
	public int[] getCountList()
	{
		return countList;
	}

	public void record(int value)
	{
		count++;
		countList[value]++;
	}

	public List<Bin> getBinList()
	{
		List<Bin> binList = new ArrayList<>();
		for (int i = 0; i < countList.length; i++)
		{
			int count = countList[i];
			if (count > 0)
			{
				Bin bin = new Bin(i, count);
				binList.add(bin);
			}
		}
		return binList;
	}
	public List<Bin> getBinList(int iMin, int iMax)
	{
		List<Bin> binList = new ArrayList<>();
		for (int i = iMin; i < iMax; i++)
		{
			int count = countList[i];
			Bin bin = new Bin(i, count);
			binList.add(bin);
		}
		return binList;
	}

	public int getMin()
	{
		for (int i = 0; i < countList.length; i++)
		{
			if (countList[i] > 0)
				return i;
		}
		return countList.length;
	}
	public double getAverage()
	{
		double sum = 0;
		for (int i = 0; i < countList.length; i++)
		{
			sum += i * countList[i];
		}
		return sum / count;
	}
	public int getMax()
	{
		for (int i = countList.length - 1; i > 0; i--)
		{
			if (countList[i] > 0)
				return i;
		}
		return 0;
	}

	public int getPercentile(int percentile)
	{
		int countTarget = count * percentile / 100;
		countTarget = Math.max(1, countTarget);
		int sum = 0;
		for (int i = 0; i < countList.length; i++)
		{
			sum += countList[i];
			if (sum >= countTarget)
			{
				return i;
			}
		}
		return countList.length;
	}

	public class Bin
	{
		private int index;
		private int count;

		public Bin(int index, int count)
		{
			this.index = index;
			this.count = count;
		}

		public int getIndex()
		{
			return index;
		}
		public int getCount()
		{
			return count;
		}
		public double getPercent()
		{
			return getCount() * 100.0 / IntegerHistogram.this.getCount();
		}

		@Override
		public String toString()
		{
			return String.format("%3d = %,d (%,.1f %%)",
					getIndex(), getCount(), getPercent()
			);
		}
	}
}
