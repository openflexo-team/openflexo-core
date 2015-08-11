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

import java.util.List;

import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoDocumentFragment;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoDocumentFragmentRole.FlexoDocumentFragmentRoleImpl.class)
public interface FlexoDocumentFragmentRole<T extends FlexoDocumentFragment<?, ?>> extends FlexoRole<T> {

	@PropertyIdentifier(type = TextBinding.class, cardinality = Cardinality.LIST)
	public static final String TEXT_BINDINGS_KEY = "textBindings";
	@PropertyIdentifier(type = FlexoDocumentFragment.class)
	public static final String FRAGMENT_KEY = "fragment";

	public FlexoDocument<?, ?> getDocument();

	/**
	 * Return the represented fragment in the template resource<br>
	 * Note that is not the fragment that is to be managed at run-time
	 * 
	 * @return
	 */
	@Getter(value = FRAGMENT_KEY, isStringConvertable = true)
	@XMLAttribute
	public T getFragment();

	/**
	 * Sets the represented fragment in the template resource<br>
	 * 
	 * @param fragment
	 */
	@Setter(FRAGMENT_KEY)
	public void setFragment(T fragment);

	@Getter(value = TEXT_BINDINGS_KEY, cardinality = Cardinality.LIST, inverse = TextBinding.FRAGMENT_ROLE_KEY)
	@XMLElement
	public List<TextBinding> getTextBindings();

	@Setter(TEXT_BINDINGS_KEY)
	public void setTextBindings(List<TextBinding> someTextBindings);

	@Adder(TEXT_BINDINGS_KEY)
	public void addToTextBindings(TextBinding aTextBinding);

	@Remover(TEXT_BINDINGS_KEY)
	public void removeFromTextBindings(TextBinding aTextBinding);

	public static abstract class FlexoDocumentFragmentRoleImpl<T extends FlexoDocumentFragment<?, ?>> extends FlexoRoleImpl<T>
			implements FlexoDocumentFragmentRole<T> {

		@Override
		public FlexoDocument<?, ?> getDocument() {
			if (getModelSlot() instanceof FlexoDocumentModelSlot) {
				return ((FlexoDocumentModelSlot) getModelSlot()).getTemplateResource().getDocument();
			}
			return null;
		}

	}
}
