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

import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.TextSelection;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;

/**
 * A role that allows to point on a given {@link TextSelection} in a {@link FlexoDocument}
 * 
 * @author sylvain
 *
 * @param <F>
 * @param <D>
 * @param <TA>
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(TextSelectionRole.TextSelectionRoleImpl.class)
public interface TextSelectionRole<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends FlexoRole<TextSelection<D, TA>> {

	public FlexoDocument<D, TA> getDocument();

	public static abstract class TextSelectionRoleImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends FlexoRoleImpl<TextSelection<D, TA>> implements TextSelectionRole<D, TA> {

		@Override
		public FlexoDocument<D, TA> getDocument() {
			if (getModelSlot() instanceof FlexoDocumentModelSlot
					&& ((FlexoDocumentModelSlot<D>) getModelSlot()).getTemplateResource() != null) {
				return ((FlexoDocumentModelSlot<D>) getModelSlot()).getTemplateResource().getDocument();
			}
			return null;
		}

		@Override
		public TextSelectionActorReference<D, TA> makeActorReference(TextSelection<D, TA> textSelection, FlexoConceptInstance fci) {
			VirtualModelInstanceModelFactory<?> factory = fci.getFactory();
			TextSelectionActorReference<D, TA> returned = factory.newInstance(TextSelectionActorReference.class);
			returned.setFlexoRole(this);
			returned.setFlexoConceptInstance(fci);
			returned.setModellingElement(textSelection);
			return returned;
		}

	}

}
