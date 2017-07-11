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

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.doc.FlexoDocFragment;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.TextSelection;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;

/**
 * A role that allows to point on a given {@link FlexoDocFragment} in a {@link FlexoDocument}
 * 
 * @author sylvain
 *
 * @param <F>
 *            type of fragment
 * @param <D>
 *            type of document in which the fragment resides
 * @param <TA>
 *            type of technology adapter
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FlexoFragmentRole.FlexoDocumentFragmentRoleImpl.class)
public interface FlexoFragmentRole<F extends FlexoDocFragment<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
		extends FlexoRole<F> {

	@PropertyIdentifier(type = TextBinding.class, cardinality = Cardinality.LIST)
	public static final String TEXT_BINDINGS_KEY = "textBindings";
	@PropertyIdentifier(type = FlexoDocFragment.class)
	public static final String FRAGMENT_KEY = "fragment";

	public FlexoDocument<D, TA> getDocument();

	/**
	 * Return the represented fragment in the template resource<br>
	 * Note that is not the fragment that is to be managed at run-time
	 * 
	 * @return
	 */
	@Getter(value = FRAGMENT_KEY, isStringConvertable = true)
	@XMLAttribute
	public F getFragment();

	/**
	 * Sets the represented fragment in the template resource<br>
	 * 
	 * @param fragment
	 */
	@Setter(FRAGMENT_KEY)
	public void setFragment(F fragment);

	@Getter(value = TEXT_BINDINGS_KEY, cardinality = Cardinality.LIST, inverse = TextBinding.FRAGMENT_ROLE_KEY)
	@Embedded
	@XMLElement
	public List<TextBinding<D, TA>> getTextBindings();

	@Setter(TEXT_BINDINGS_KEY)
	public void setTextBindings(List<TextBinding<D, TA>> someTextBindings);

	@Adder(TEXT_BINDINGS_KEY)
	public void addToTextBindings(TextBinding<D, TA> aTextBinding);

	@Remover(TEXT_BINDINGS_KEY)
	public void removeFromTextBindings(TextBinding<D, TA> aTextBinding);

	public TextBinding<D, TA> makeTextBinding(TextSelection<D, TA> textSelection, DataBinding<String> binding);

	public TextBinding<D, TA> makeTextBinding(TextSelection<D, TA> textSelection, DataBinding<String> binding, boolean isMultiline);

	public static abstract class FlexoDocumentFragmentRoleImpl<F extends FlexoDocFragment<D, TA>, D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends FlexoRoleImpl<F> implements FlexoFragmentRole<F, D, TA> {

		@Override
		public FlexoDocument<D, TA> getDocument() {
			if (getModelSlot() instanceof FlexoDocumentModelSlot
					&& ((FlexoDocumentModelSlot<D>) getModelSlot()).getTemplateResource() != null) {
				return ((FlexoDocumentModelSlot<D>) getModelSlot()).getTemplateResource().getDocument();
			}
			return null;
		}

		@Override
		public TextBinding<D, TA> makeTextBinding(TextSelection<D, TA> textSelection, DataBinding<String> binding) {
			return makeTextBinding(textSelection, binding, false);
		}

		@Override
		public TextBinding<D, TA> makeTextBinding(TextSelection<D, TA> textSelection, DataBinding<String> binding, boolean isMultiline) {
			TextBinding<D, TA> returned = getFMLModelFactory().newInstance(TextBinding.class);
			textSelection.setFragment(getFragment());
			returned.setTextSelection(textSelection);
			returned.setValue(binding);
			returned.setMultiline(isMultiline);
			addToTextBindings(returned);
			return returned;
		}

		@Override
		public FragmentActorReference<F> makeActorReference(F fragment, FlexoConceptInstance fci) {
			VirtualModelInstanceModelFactory<?> factory = fci.getFactory();
			FragmentActorReference<F> returned = factory.newInstance(FragmentActorReference.class);
			returned.setFlexoRole(this);
			returned.setFlexoConceptInstance(fci);
			returned.setModellingElement(fragment);
			return returned;
		}

	}

	@DefineValidationRule
	public static class FragmentRoleMustAddressATemplateFragment
			extends ValidationRule<FragmentRoleMustAddressATemplateFragment, FlexoFragmentRole> {
		public FragmentRoleMustAddressATemplateFragment() {
			super(FlexoFragmentRole.class, "fragment_role_must_reference_a_template_fragment");
		}

		@Override
		public ValidationIssue<FragmentRoleMustAddressATemplateFragment, FlexoFragmentRole> applyValidation(FlexoFragmentRole role) {
			if (role.getModelSlot() instanceof FlexoDocumentModelSlot
					&& ((FlexoDocumentModelSlot) role.getModelSlot()).getTemplateResource() != null) {
				if (role.getFragment() == null) {
					return new ValidationError<>(this, role, "fragment_role_doesn't_reference_any_template_fragment");
				}
			}
			return null;
		}
	}

}
