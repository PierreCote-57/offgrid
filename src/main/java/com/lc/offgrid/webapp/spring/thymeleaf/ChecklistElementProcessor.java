package com.lc.offgrid.webapp.spring.thymeleaf;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.ICloseElementTag;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.model.ITemplateEvent;
import org.thymeleaf.processor.element.AbstractElementModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Arrays;
import java.util.List;

/**
 * Wraps the content of every direct &lt;li&gt; of a checklist list in
 * &lt;label&gt;&lt;input type="checkbox"&gt;&lt;span&gt;text&lt;/span&gt;&lt;/label&gt;, so
 * that clicking anywhere on the row toggles the box. Authors write bare &lt;li&gt; lines.
 *
 * The class on the list carries the styling and selects the variant: gl-checklist is a
 * plain checkbox row, gl-numcheck adds an empty &lt;span class="gl-num"&gt; between the box
 * and the text whose digit is drawn by a CSS counter.
 *
 * Lists without either class are left exactly as authored.
 */
public class ChecklistElementProcessor extends AbstractElementModelProcessor
{
	private static final String		CLASS_CHECKLIST		= "gl-checklist";
	private static final String		CLASS_NUMCHECK		= "gl-numcheck";
	private static final String		CLASS_NUMBER		= "gl-num";
	private static final String		TAG_ITEM			= "li";
	private static final String		TAG_INPUT			= "input";
	private static final String		TYPE_CHECKBOX		= "checkbox";
	private static final int		PRECEDENCE			= 1000;

	public ChecklistElementProcessor(String dialectPrefix, String elementName)
	{
		super(TemplateMode.HTML, dialectPrefix, elementName, false, null, false, PRECEDENCE);
	}

	@Override
	protected void doProcess(
			ITemplateContext context,
			IModel model,
			IElementModelStructureHandler structureHandler)
	{
		List<String> classList = getClassList(model);
		boolean numbered = classList.contains(CLASS_NUMCHECK);
		if (!numbered && !classList.contains(CLASS_CHECKLIST))
		{
			return;
		}
		if (isWrapped(model))
		{
			return;
		}

		IModelFactory	factory		= context.getModelFactory();
		IModel			wrapped		= factory.createModel();

		// Depth 1 is the list itself, so a <li> opened at depth 2 is one of its own rows.
		// Anything nested inside that row is copied across untouched.
		int			depth			= 0;
		boolean		insideItem		= false;

		for (int index = 0; index < model.size(); index++)
		{
			ITemplateEvent event = model.get(index);

			if (event instanceof IOpenElementTag)
			{
				depth++;
				wrapped.add(event);
				if (2 == depth && isItem((IOpenElementTag) event))
				{
					insideItem = true;
					wrapped.add(factory.createOpenElementTag("label"));
					wrapped.add(factory.createStandaloneElementTag("input", "type", "checkbox"));
					if (numbered)
					{
						wrapped.add(factory.createOpenElementTag("span", "class", CLASS_NUMBER));
						wrapped.add(factory.createCloseElementTag("span"));
					}
					wrapped.add(factory.createOpenElementTag("span"));
				}
				continue;
			}

			if (event instanceof ICloseElementTag)
			{
				depth--;
				if (insideItem && 1 == depth)
				{
					wrapped.add(factory.createCloseElementTag("span"));
					wrapped.add(factory.createCloseElementTag("label"));
					insideItem = false;
				}
				wrapped.add(event);
				continue;
			}

			wrapped.add(event);
		}

		model.reset();
		model.addModel(wrapped);
	}

	/**
	 * The classes carried by the list element itself, empty when it has none.
	 */
	private List<String> getClassList(IModel model)
	{
		ITemplateEvent event = model.get(0);
		if (!(event instanceof IProcessableElementTag))
		{
			return List.of();
		}
		String listClass = ((IProcessableElementTag) event).getAttributeValue("class");
		if (null == listClass || listClass.isBlank())
		{
			return List.of();
		}
		List<String> classList = Arrays.asList(listClass.trim().split("\\s+"));
		return classList;
	}

	/**
	 * Whether this list already carries the checkbox markup.
	 *
	 * A model processor that changes its model has the changed model handed straight back to
	 * it, and the list still matches on the way in, so without this the wrapping recurses
	 * until the stack runs out. A pass that changes nothing ends it.
	 *
	 * An authored input is not a checkbox — a write-on blank is a text input — so the type is
	 * what the test reads.
	 */
	private boolean isWrapped(IModel model)
	{
		for (int index = 0; index < model.size(); index++)
		{
			ITemplateEvent event = model.get(index);
			if (!(event instanceof IProcessableElementTag))
			{
				continue;
			}
			IProcessableElementTag tag = (IProcessableElementTag) event;
			if (!TAG_INPUT.equalsIgnoreCase(tag.getElementCompleteName()))
			{
				continue;
			}
			if (TYPE_CHECKBOX.equalsIgnoreCase(tag.getAttributeValue("type")))
			{
				return true;
			}
		}
		return false;
	}

	private boolean isItem(IOpenElementTag tag)
	{
		boolean item = TAG_ITEM.equalsIgnoreCase(tag.getElementCompleteName());
		return item;
	}
}
