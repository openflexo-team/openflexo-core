package org.openflexo.foundation.action.copypaste;

import org.openflexo.foundation.FlexoObject;

/**
 * This is the default implementation of {@link PasteHandler}<br>
 * Pasting context is retrieved as focused object, and default paste is performed without any data translation
 * 
 * @author sylvain
 * 
 */
public class DefaultPasteHandler extends FlexoPasteHandler<FlexoObject> {

	@Override
	public Class<FlexoObject> getPastingPointHolderType() {
		return FlexoObject.class;
	}

}