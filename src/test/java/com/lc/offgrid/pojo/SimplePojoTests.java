package com.lc.offgrid.pojo;

import com.lc.offgrid.AbstractTests;
import com.lc.offgrid.common.pojo.page.MaintenancePage;
import com.lc.offgrid.common.pojo.part.MaintenanceEntry;
import com.lc.offgrid.common.pojo.part.MaintenanceEntry.NextDue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimplePojoTests extends AbstractTests
{
	public static Object[][] NextDueData()
	{
		LocalDate dateEarly = LocalDate.of(2026, 9, 1);
		LocalDate dateLate = LocalDate.of(2026, 9, 30);
		NextDue due1 = new NextDue(100, dateEarly);
		NextDue due2 = new NextDue(200, dateLate);
		NextDue dueEmpty = new NextDue(null, null);

		MaintenanceEntry entry1 = new MaintenanceEntry(due1);
		MaintenanceEntry entry2 = new MaintenanceEntry(due2);
		MaintenanceEntry entryEmpty = new MaintenanceEntry(dueEmpty);
		MaintenanceEntry entryNull = new MaintenanceEntry(null);

		List<MaintenanceEntry>	actualList12 = Arrays.asList(entry1, entry2);
		List<MaintenanceEntry>	actualList21 = Arrays.asList(entry2, entry1);
		List<MaintenanceEntry>	actualListNull = Arrays.asList(entry1, entryEmpty, entryNull);

		MaintenancePage page12 = new MaintenancePage(actualList12);
		MaintenancePage page21 = new MaintenancePage(actualList21);
		MaintenancePage pageNull = new MaintenancePage(actualListNull);

		return new Object[][]{
				new Object[] { page12, entry2, due2 },
				new Object[] { page21, entry1, due1 },
				new Object[] { pageNull, entry1, due1 },
		};
	}

	@ParameterizedTest
	@MethodSource("NextDueData")
	public void testNextDue(MaintenancePage page, MaintenanceEntry expectedEntry, NextDue expectedNext)
	{
		MaintenanceEntry actualEntry = page.getLastMaintenance();
		assertEquals(expectedEntry, actualEntry);

		NextDue actualNextDue = page.getNextMaintenance();
		assertEquals(expectedNext, actualNextDue);
	}
}
