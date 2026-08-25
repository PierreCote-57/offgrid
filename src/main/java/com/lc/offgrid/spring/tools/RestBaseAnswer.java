/*
 * Copyright (c) 2020 LogicielCote.COM Systems All rights reserved.
 *
 * @Author  <mailto:Pierre@LogicielCote.COM>Pierre Cote</mailto>
 */

package com.lc.offgrid.spring.tools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lc.basics.tools.time.WallClock;
import com.lc.basics.tools.time.WallClock.FormatSize;
import com.lc.basics.tools.units.TimeUnits;

import java.io.Serializable;

public class RestBaseAnswer implements Serializable
{
	private long		m_timeBeginMS;
	private String		m_dateBeginText;
	private long		m_timeBeginNS;		// for duration
	private long		m_timeDoneNS		= 0;
	private long		m_durationNS		= 0;
	private String		m_durationText		= "";

	public RestBaseAnswer()
	{
		begin(System.currentTimeMillis(), System.nanoTime());
	}

	public void begin(long timeBeginMS, long timeBeginNS)
	{
		setTimeBeginMS(timeBeginMS);
		setTimeBeginNS(timeBeginNS);
	}

	public void markDone()
	{
		setTimeDoneNS(System.nanoTime());
	}

	public void setTimeBeginMS(long timeBeginMS)
	{
		m_timeBeginMS = timeBeginMS;
		m_dateBeginText = WallClock.formatTime(WallClock.FormatType.DateTime, FormatSize.Medium, timeBeginMS);
	}
	public void setTimeBeginNS(long timeBeginNS)
	{
		m_timeBeginNS = timeBeginNS;
	}

	public void setTimeDoneNS(long timeDoneNS)
	{
		if (0 < timeDoneNS)
		{
			m_timeDoneNS = timeDoneNS;
			m_durationNS = timeDoneNS - m_timeBeginNS;
			m_durationText = TimeUnits.NS.format(m_durationNS);
		}
	}

	public long getTimeBeginMS()
	{
		return m_timeBeginMS;
	}
	@JsonIgnore
	public String getDateBeginText()
	{
		return m_dateBeginText;
	}
	public long getTimeBeginNS()
	{
		return m_timeBeginNS;
	}
	public long getTimeDoneNS()
	{
		return m_timeDoneNS;
	}
	public long getDurationNS()
	{
		return m_durationNS;
	}
	public String getDurationText()
	{
		return m_durationText;
	}
	@JsonIgnore
	public String getDurationTotal()
	{
		return TimeUnits.NS.format(System.nanoTime() - getTimeBeginNS());
	}

	@Override
	public String toString()
	{
		return String.format("%s(@ %s in %s)",
			getClass().getSimpleName(),
			getDateBeginText(),
			getDurationText()
		);
	}
}
