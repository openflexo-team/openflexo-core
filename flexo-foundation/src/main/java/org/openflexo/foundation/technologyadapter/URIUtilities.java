package org.openflexo.foundation.technologyadapter;

import org.openflexo.toolbox.StringUtils;
import org.openflexo.toolbox.ToolBox;

public class URIUtilities {

	/**
	 * Return true if URI is well formed and valid regarding its unicity (no one other object has same URI)
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean testValidURI(FlexoModel<?, ?> model, String conceptURI) {
		if (StringUtils.isEmpty(conceptURI)) {
			return false;
		}
		if (StringUtils.isEmpty(conceptURI.trim())) {
			return false;
		}
		return conceptURI.equals(ToolBox.getJavaName(conceptURI, true, false)) && !isDuplicatedURI(model, conceptURI);
	}

	/**
	 * Return true if URI is duplicated in the context of supplied {@link FlexoModel}
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean isDuplicatedURI(FlexoModel<?, ?> model, String conceptURI) {
		if (model != null) {
			return model.getObject(model.getURI() + "#" + conceptURI) != null;
		}
		return false;
	}

	/**
	 * Return true if URI is well formed and valid regarding its unicity (no one other object has same URI)
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean testValidURI(FlexoMetaModel<?> metaModel, String conceptURI) {
		if (StringUtils.isEmpty(conceptURI)) {
			return false;
		}
		if (StringUtils.isEmpty(conceptURI.trim())) {
			return false;
		}
		return conceptURI.equals(ToolBox.getJavaName(conceptURI, true, false)) && !isDuplicatedURI(metaModel, conceptURI);
	}

	/**
	 * Return true if URI is duplicated in the context of supplied {@link FlexoModel}
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean isDuplicatedURI(FlexoMetaModel<?> metaModel, String conceptURI) {
		if (metaModel != null) {
			return metaModel.getObject(metaModel.getURI() + "#" + conceptURI) != null;
		}
		return false;
	}

}
