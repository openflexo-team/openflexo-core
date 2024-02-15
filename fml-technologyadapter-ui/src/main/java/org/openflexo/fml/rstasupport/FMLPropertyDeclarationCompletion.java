/*
 * 06/25/2012
 *
 * Copyright (C) 2012 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.openflexo.fml.rstasupport;

import org.openflexo.foundation.fml.FMLModelContext;
import org.openflexo.foundation.fml.FMLModelContext.FMLEntity;
import org.openflexo.foundation.fml.FMLModelContext.FMLProperty;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.technologyadapter.ModelSlot;

/**
 * A completion for a {@link ModelSlot} declaration
 *
 * @author sylvain
 */
public abstract class FMLPropertyDeclarationCompletion<P extends FlexoProperty<?>> extends AbstractTemplateCompletion {

	/**
	 * The relevance of fields. This allows fields to be "higher" in the completion list than other types.
	 */
	static final int RELEVANCE = 4;

	static <I extends FMLObject> FMLEntity<I> getFMLEntity(FMLSourceCompletionProvider provider, Class<I> objectClass) {
		return FMLModelContext.getFMLEntity(objectClass, provider.getCompilationUnit().getFMLModelFactory());
	}

	static <I extends FMLObject> String getParametersTemplate(FMLSourceCompletionProvider provider, Class<I> objectClass) {
		FMLEntity<I> fmlEntity = getFMLEntity(provider, objectClass);
		StringBuffer params = new StringBuffer();
		int index = 0;
		for (FMLProperty<?, ?> fmlProperty : fmlEntity.getProperties()) {
			if (fmlProperty.isRequired()) {
				params.append((index == 0 ? "" : ",") + fmlProperty.getName() + "=${value}");
				index++;
			}
		}
		return params.toString();
	}

	private Class<P> propertyClass;

	public FMLPropertyDeclarationCompletion(FMLSourceCompletionProvider provider, String inputText, String definitionString,
			String template, Class<P> propertyClass) {
		super(provider, inputText, definitionString, template);
		this.propertyClass = propertyClass;
		setRelevance(RELEVANCE);
	}

	public Class<P> getPropertyClass() {
		return propertyClass;
	}

	@Override
	public String getSummary() {
		if (getPropertyClass().getAnnotation(FML.class) != null) {
			FML annotation = getPropertyClass().getAnnotation(FML.class);
			return "<html>" + "<b>" + annotation.value() + "</b>" + "<br>" + annotation.description() + "</html>";
		}
		return "No description available";
	}

}
