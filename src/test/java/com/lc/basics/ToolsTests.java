package com.lc.basics;

import com.lc.basics.tools.misc.BasicTools;
import com.lc.offgrid.AbstractTests;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ToolsTests extends AbstractTests
{
	@Test
	public void testWifiName()
	{
		String name = BasicTools.getWifiName();
		assertNotNull(name);
	}
}
