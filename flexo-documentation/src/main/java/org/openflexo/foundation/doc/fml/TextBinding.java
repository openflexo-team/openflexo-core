/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.doc.fml;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.doc.FlexoDocumentFragment;
import org.openflexo.foundation.doc.TextSelection;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

/**
 * This class represent a text binding declared in a document fragment.<br>
 * A {@link TextSelection} is an expression that is to be replaced as text of a run in a document fragment.<br>
 * More exactely we maintain here a bi-directional synchronization between text and data if the binding is declared as settable
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(TextBinding.TextBindingImpl.class)
@XMLElement
public interface TextBinding extends FlexoConceptObject {

	@PropertyIdentifier(type = TextSelection.class)
	public static final String TEXT_SELECTION_KEY = "textSelection";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VALUE_KEY = "value";

	@PropertyIdentifier(type = FlexoDocumentFragmentRole.class)
	public static final String FRAGMENT_ROLE_KEY = "fragmentRole";

	@Getter(TEXT_SELECTION_KEY)
	@XMLElement
	public TextSelection<?, ?> getTextSelection();

	@Setter(TEXT_SELECTION_KEY)
	public void setTextSelection(TextSelection<?, ?> textSelection);

	@Getter(VALUE_KEY)
	@XMLAttribute
	public DataBinding<String> getValue();

	@Setter(VALUE_KEY)
	public void setValue(DataBinding<String> value);

	@Getter(FRAGMENT_ROLE_KEY)
	public FlexoDocumentFragmentRole<?, ?, ?> getFragmentRole();

	@Setter(FRAGMENT_ROLE_KEY)
	public void setFragmentRole(FlexoDocumentFragmentRole<?, ?, ?> fragmentRole);

	/**
	 * This method is called to extract a value from the federated data and apply it to the represented fragment representation
	 * 
	 * @param gr
	 * @param element
	 */
	public void applyToFragment(FlexoConceptInstance fci);

	/**
	 * This method is called to extract a value from the fragment, and apply it to underlying federated data
	 * 
	 * @param gr
	 * @param element
	 * @return
	 */
	public String extractFromFragment(FlexoConceptInstance fci);

	public static abstract class TextBindingImpl extends FlexoConceptObjectImpl implements TextBinding {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(TextBinding.class.getPackage().getName());

		private DataBinding<String> value;

		// Use it only for deserialization
		public TextBindingImpl() {
			super();
		}

		@Override
		public String getURI() {
			return null;
		}

		public int getIndex() {
			if (getFragmentRole() != null) {
				return getFragmentRole().getTextBindings().indexOf(this);
			}
			return -1;
		}

		@Override
		public DataBinding<String> getValue() {
			if (value == null) {
				value = new DataBinding<String>(this, String.class, DataBinding.BindingDefinitionType.GET);
				value.setBindingName("TextSelection" + getIndex());
				value.setMandatory(true);
			}
			return value;
		}

		@Override
		public void setValue(DataBinding<String> value) {
			if (value != null) {
				value.setOwner(this);
				value.setDeclaredType(String.class);
				value.setBindingName("TextSelection" + getIndex());
				value.setMandatory(true);
				value.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.value = value;
			notifiedBindingChanged(getValue());
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			return getFragmentRole() != null ? getFragmentRole().getFlexoConcept() : null;
		}

		@Override
		public BindingFactory getBindingFactory() {
			return getFlexoConcept().getInspector().getBindingFactory();
		}

		@Override
		public BindingModel getBindingModel() {
			if (getFlexoConcept() != null && getFlexoConcept().getInspector() != null) {
				return getFlexoConcept().getInspector().getBindingModel();
			}
			return null;
		}

		/**
		 * This method is called to extract a value from the data and apply it to the represented fragment representation
		 * 
		 * @param gr
		 * @param element
		 */
		@Override
		public void applyToFragment(FlexoConceptInstance fci) {

			try {
				System.out.println("Pour le fci: " + fci);
				System.out.println("role: " + getFragmentRole());

				FragmentActorReference<?> actorReference = (FragmentActorReference<?>) fci.getActorReference(getFragmentRole());
				FlexoDocumentFragment<?, ?> templateFragment = getFragmentRole().getFragment();
				FlexoDocumentFragment<?, ?> fragment = actorReference.getModellingElement();

				// System.out.println("getStartParagraphIdentifier()=" + getStartParagraphIdentifier());
				// System.out.println("getStartRunIndex()=" + getStartRunIndex());

				/*FlexoParagraph<?, ?> paragraph = (FlexoParagraph<?, ?>) actorReference
						.getElementMatchingTemplateElementId(getStartParagraphIdentifier());
				
				System.out.println("paragraph=" + paragraph);
				
				FlexoRun<?, ?> run = paragraph.getRuns().get(getStartRunIndex());*/

				String value = getValue().getBindingValue(fci);

				System.out.println("On cherche a remplacer la TextSelection: " + getTextSelection());
				System.out.println("Avec le binding " + getValue());
				System.out.println("Le texte c'est: " + getTextSelection().getRawText());
				System.out.println("La nouvelle valeur c'est: " + value);

				// run.setText(value);

			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		/**
		 * This method is called to extract a value from the graphical representation and conform to the related feature, and apply it to
		 * model
		 * 
		 * @param gr
		 * @param element
		 * @return
		 */
		@Override
		public String extractFromFragment(FlexoConceptInstance fci) {
			if (getValue().isSettable()) {
				FlexoDocumentFragment<?, ?> fragment = fci.getFlexoActor(getFragmentRole());
				// FlexoRun<?, ?> run = fragment.getRun(getStartRunIdentifier());
				/*String newValue = run.getText();
				try {
					getValue().setBindingValue(newValue, fci);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NotSettableContextException e) {
					e.printStackTrace();
				}
				return newValue;*/
			}
			return null;
		}

	}
}
