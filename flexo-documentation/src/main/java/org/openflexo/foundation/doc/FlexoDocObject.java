/*
 * (c) Copyright 2013 Openflexo
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

package org.openflexo.foundation.doc;

import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;

/**
 * This concept provides abstraction for an object involved in generic FlexoDocumentation A.P.I
 * 
 * 
 * @author sylvain
 *
 * @param <D>
 *            type of {@link FlexoDocument} involving this concept
 * @param <TA>
 *            {@link TechnologyAdapter} of current implementation
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoDocObject.FlexoDocObjectImpl.class)
public interface FlexoDocObject<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
		extends TechnologyObject<TA>, InnerResourceData<D> {

	@PropertyIdentifier(type = FlexoDocument.class)
	public static final String DOCUMENT_KEY = "document";

	@Getter(value = DOCUMENT_KEY)
	public D getFlexoDocument();

	@Setter(DOCUMENT_KEY)
	public void setFlexoDocument(D flexoDocument);

	public static abstract class FlexoDocObjectImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter<TA>>
			extends FlexoObjectImpl implements FlexoDocObject<D, TA> {

		public FlexoDocObjectImpl() {
			super();
		}

		@Override
		public D getResourceData() {
			return getFlexoDocument();
		}

		@Override
		public TA getTechnologyAdapter() {
			if (getFlexoDocument() != null) {
				return getFlexoDocument().getTechnologyAdapter();
			}
			return null;
		}

		@Override
		public LocalizedDelegate getLocales() {
			if (getTechnologyAdapter() != null) {
				return getTechnologyAdapter().getLocales();
			}
			return super.getLocales();
		}

	}

}
