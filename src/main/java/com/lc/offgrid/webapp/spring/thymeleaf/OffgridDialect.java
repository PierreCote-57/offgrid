package com.lc.offgrid.webapp.spring.thymeleaf;

import org.springframework.stereotype.Component;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

import java.util.HashSet;
import java.util.Set;

/**
 * The site's own Thymeleaf dialect. Spring Boot hands every IDialect bean to the template
 * engine, so being a @Component is the whole registration.
 */
@Component
public class OffgridDialect extends AbstractProcessorDialect
{
	private static final String		NAME			= "Offgrid";
	private static final String		PREFIX			= "gl";
	private static final int		PRECEDENCE		= 1000;

	public OffgridDialect()
	{
		super(NAME, PREFIX, PRECEDENCE);
	}

	@Override
	public Set<IProcessor> getProcessors(String dialectPrefix)
	{
		Set<IProcessor> processorSet = new HashSet<>();
		processorSet.add(new ChecklistElementProcessor(dialectPrefix, "ol"));
		processorSet.add(new ChecklistElementProcessor(dialectPrefix, "ul"));
		return processorSet;
	}
}
