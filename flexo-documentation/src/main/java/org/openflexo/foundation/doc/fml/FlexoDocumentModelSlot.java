/*
 * (c) Copyright 2013- Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openflexo.foundation.doc.fml;

import java.util.logging.Logger;

import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.rm.FlexoDocumentResource;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.technologyadapter.FreeModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.toolbox.StringUtils;

/**
 * Implementation of the ModelSlot class for the DOCX technology adapter<br>
 * We expect here to connect an .docx document<br>
 * 
 * We might here supply a template document, which might be used as a "metamodel" to help manage connected document
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoDocumentModelSlot.FlexoDocumentModelSlotImpl.class)
public interface FlexoDocumentModelSlot<D extends FlexoDocument<D, TA>, R extends FlexoDocumentResource<D, TA, ?>, TA extends TechnologyAdapter<TA>>
		extends FreeModelSlot<D, R> {

	@PropertyIdentifier(type = String.class)
	public static final String TEMPLATE_DOCUMENT_URI_KEY = "templateDocumentURI";
	@PropertyIdentifier(type = FlexoDocument.class)
	public static final String TEMPLATE_DOCUMENT_KEY = "templateDocument";
	@PropertyIdentifier(type = FlexoResource.class)
	public static final String TEMPLATE_RESOURCE_KEY = "templateResource";

	@Getter(value = TEMPLATE_DOCUMENT_URI_KEY)
	@XMLAttribute
	public String getTemplateDocumentURI();

	@Setter(TEMPLATE_DOCUMENT_URI_KEY)
	public void setTemplateDocumentURI(String templateDocumentURI);

	@Getter(value = TEMPLATE_DOCUMENT_KEY, ignoreType = true)
	@FMLAttribute(value = TEMPLATE_DOCUMENT_KEY, required = true)
	public FlexoDocument<D, TA> getTemplateDocument();

	@Setter(TEMPLATE_DOCUMENT_KEY)
	public void setTemplateDocument(FlexoDocument<D, TA> aDocument);

	public FlexoDocumentResource<D, TA, ?> getTemplateResource();

	public void setTemplateResource(FlexoDocumentResource<D, TA, ?> templateResource);

	public static abstract class FlexoDocumentModelSlotImpl<D extends FlexoDocument<D, TA>, R extends FlexoDocumentResource<D, TA, ?>, TA extends TechnologyAdapter<TA>>
			extends FreeModelSlotImpl<D, R> implements FlexoDocumentModelSlot<D, R, TA> {

		private static final Logger logger = Logger.getLogger(FlexoDocumentModelSlot.class.getPackage().getName());

		private String templateDocumentURI;
		private FlexoDocumentResource<D, TA, ?> templateResource;

		@Override
		public String getTemplateDocumentURI() {
			if (getTemplateResource() != null) {
				return getTemplateResource().getURI();
			}
			return templateDocumentURI;
		}

		@Override
		public void setTemplateDocumentURI(String templateDocumentURI) {
			if ((templateDocumentURI == null && this.templateDocumentURI != null)
					|| (templateDocumentURI != null && !templateDocumentURI.equals(this.templateDocumentURI))) {
				String oldValue = this.templateDocumentURI;
				this.templateDocumentURI = templateDocumentURI;
				getPropertyChangeSupport().firePropertyChange("templateDocumentURI", oldValue, templateDocumentURI);
			}
		}

		@Override
		protected String getFMLRepresentationForConformToStatement() {
			return "conformTo " + getTemplateDocumentURI() + " ";
		}

		@Override
		public FlexoDocument<D, TA> getTemplateDocument() {
			if (getTemplateResource() != null) {
				return getTemplateResource().getDocument();
			}
			return null;
		}

		@Override
		public void setTemplateDocument(FlexoDocument<D, TA> templateDocument) {
			setTemplateResource(templateDocument != null ? (FlexoDocumentResource<D, TA, ?>) templateDocument.getResource() : null);
		}

		@Override
		public FlexoDocumentResource<D, TA, ?> getTemplateResource() {
			if (templateResource == null && StringUtils.isNotEmpty(templateDocumentURI) && getServiceManager() != null
					&& getServiceManager().getResourceManager() != null) {
				getServiceManager().activateTechnologyAdapter(getModelSlotTechnologyAdapter(), true);
				templateResource = (FlexoDocumentResource<D, TA, ?>) getServiceManager().getResourceManager()
						.getResource(templateDocumentURI);
				logger.info("Looked-up " + templateResource + " for " + templateDocumentURI);
			}
			return templateResource;
		}

		@Override
		public void setTemplateResource(FlexoDocumentResource<D, TA, ?> templateResource) {
			//System.err.println("setTemplateResource with " + templateResource);
			this.templateResource = templateResource;
			setIsModified();
		}

	}
}
