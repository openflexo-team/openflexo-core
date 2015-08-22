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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NotSettableContextException;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoDocumentElement;
import org.openflexo.foundation.doc.FlexoParagraph;
import org.openflexo.foundation.doc.FlexoRun;
import org.openflexo.foundation.doc.TextSelection;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
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
public interface TextBinding<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> extends FlexoConceptObject {

	@PropertyIdentifier(type = TextSelection.class)
	public static final String TEXT_SELECTION_KEY = "textSelection";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VALUE_KEY = "value";

	@PropertyIdentifier(type = FlexoDocumentFragmentRole.class)
	public static final String FRAGMENT_ROLE_KEY = "fragmentRole";

	@Getter(TEXT_SELECTION_KEY)
	@XMLElement
	public TextSelection<D, TA> getTextSelection();

	@Setter(TEXT_SELECTION_KEY)
	public void setTextSelection(TextSelection<D, TA> textSelection);

	@Getter(VALUE_KEY)
	@XMLAttribute
	public DataBinding<String> getValue();

	@Setter(VALUE_KEY)
	public void setValue(DataBinding<String> value);

	@Getter(FRAGMENT_ROLE_KEY)
	public FlexoDocumentFragmentRole<?, D, TA> getFragmentRole();

	@Setter(FRAGMENT_ROLE_KEY)
	public void setFragmentRole(FlexoDocumentFragmentRole<?, D, TA> fragmentRole);

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

	public static abstract class TextBindingImpl<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter>
			extends FlexoConceptObjectImpl implements TextBinding<D, TA> {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(TextBinding.class.getPackage().getName());

		private DataBinding<String> value;

		// Use it only for deserialization
		public TextBindingImpl() {
			super();
		}

		@Override
		public TextSelection<D, TA> getTextSelection() {
			TextSelection<D, TA> returned = (TextSelection<D, TA>) performSuperGetter(TEXT_SELECTION_KEY);
			if (returned != null && returned.getFragment() == null && getFragmentRole() != null) {
				returned.setFragment(getFragmentRole().getFragment());
			}
			return returned;
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
				FragmentActorReference<?> actorReference = (FragmentActorReference<?>) fci.getActorReference(getFragmentRole());
				// FlexoDocumentFragment<?, ?> templateFragment = getFragmentRole().getFragment();
				// FlexoDocumentFragment<?, ?> fragment = actorReference.getModellingElement();

				String value = getValue().getBindingValue(fci);

				if (getTextSelection().isSingleParagraph()) {
					FlexoRun<?, ?> templateStartRun = getTextSelection().getStartRun();
					FlexoRun<?, ?> templateEndRun = getTextSelection().getEndRun();
					List<String> newStructure = new ArrayList<String>();

					if (getTextSelection().getStartCharacterIndex() > -1) {
						newStructure.add(templateStartRun.getText().substring(0, getTextSelection().getStartCharacterIndex()));
						newStructure.add(value);
						if (getTextSelection().getEndCharacterIndex() > -1) {
							newStructure.add(templateEndRun.getText().substring(getTextSelection().getEndCharacterIndex()));
						}
					}
					else {
						newStructure.add(value);
						if (getTextSelection().getEndCharacterIndex() > -1) {
							newStructure.add(templateEndRun.getText().substring(getTextSelection().getEndCharacterIndex()));
						}
					}

					performTextReplacementInSingleParagraphContext(newStructure, actorReference);
				}

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
		 * Internally used to perform text replacement in single paragraph context (when TextSelection apply on a single paraggraph)
		 * 
		 * @param newStructure
		 * @param actorReference
		 */
		private void performTextReplacementInSingleParagraphContext(List<String> newStructure, FragmentActorReference<?> actorReference) {

			FlexoRun<D, TA> templateStartRun = getTextSelection().getStartRun();
			FlexoRun<D, TA> templateEndRun = getTextSelection().getEndRun();
			FlexoParagraph<D, TA> templateParagraph = (FlexoParagraph<D, TA>) getTextSelection().getStartElement();

			if (actorReference.getElementsMatchingTemplateElement(getTextSelection().getStartElement()).size() > 0) {

				FlexoDocumentElement<?, ?> targetDocumentElement = actorReference
						.getElementsMatchingTemplateElement(getTextSelection().getStartElement()).get(0);
				if (targetDocumentElement instanceof FlexoParagraph) {
					FlexoParagraph<D, TA> targetParagraph = (FlexoParagraph<D, TA>) targetDocumentElement;

					// We compute start target run relatively to the beginning of actual target paragraph, because
					// we cannot rely on structure that may have changed because of concurrent modifications
					FlexoRun<D, TA> startTargetRun = null;
					if (templateStartRun.getIndex() < targetParagraph.getRuns().size()) {
						startTargetRun = targetParagraph.getRuns().get(templateStartRun.getIndex());
					}
					else {
						startTargetRun = targetParagraph.getRuns().get(targetParagraph.getRuns().size() - 1);
					}

					// We compute end target run relatively to the end of actual target paragraph, because
					// we cannot rely on structure that may have changed because of concurrent modifications
					FlexoRun<D, TA> endTargetRun = null;
					if (targetParagraph.getRuns().size() - templateParagraph.getRuns().size() + templateEndRun.getIndex() < targetParagraph
							.getRuns().size()) {
						endTargetRun = targetParagraph.getRuns()
								.get(targetParagraph.getRuns().size() - templateParagraph.getRuns().size() + templateEndRun.getIndex());
					}
					else {
						endTargetRun = targetParagraph.getRuns().get(targetParagraph.getRuns().size() - 1);
					}

					int targetRunsNb = endTargetRun.getIndex() - startTargetRun.getIndex() + 1;

					// We compare cardinality of the two structures to synchronize

					if (targetRunsNb < newStructure.size()) {
						// We have to add extra runs, at the end of actual structure
						int currentIndex = endTargetRun.getIndex() + 1;
						for (int i = 0; i < newStructure.size() - targetRunsNb; i++) {
							FlexoRun<D, TA> clonedRun = (FlexoRun<D, TA>) startTargetRun.cloneObject();
							targetParagraph.insertRunAtIndex(clonedRun, currentIndex++);
							endTargetRun = clonedRun;
						}
					}

					else if (targetRunsNb > newStructure.size()) {
						// We have to remove extra runs
						// We remove runs from the end of actual structure
						int lastRunIndex = endTargetRun.getIndex();
						endTargetRun = targetParagraph.getRuns().get(lastRunIndex - targetRunsNb + newStructure.size());
						for (int i = 0; i < targetRunsNb - newStructure.size(); i++) {
							FlexoRun<D, TA> runToRemove = targetParagraph.getRuns().get(lastRunIndex - i);
							targetParagraph.removeFromRuns(runToRemove);
						}
					}

					targetRunsNb = endTargetRun.getIndex() - startTargetRun.getIndex() + 1;

					if (targetRunsNb != newStructure.size()) {
						logger.warning("Something was wrong when performing text replacement");
					}

					// Now the structures are same: targetRunNb == newStructure.size()
					for (int i = 0; i < newStructure.size(); i++) {
						FlexoRun<?, ?> run = targetParagraph.getRuns().get(i + startTargetRun.getIndex());
						String v = newStructure.get(i);
						run.setText(v);
					}

				}
				else {
					logger.warning("Text replacement not implemented for " + targetDocumentElement);
				}
			}
			else {
				logger.warning("Could not find element in target document matching " + getTextSelection().getStartElement());
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

				FragmentActorReference<?> actorReference = (FragmentActorReference<?>) fci.getActorReference(getFragmentRole());
				// FlexoDocumentFragment<?, ?> fragment = fci.getFlexoActor(getFragmentRole());

				if (getTextSelection().isSingleParagraph()) {
					String value = extractTextInSingleParagraphContext(actorReference);

					System.out.println("Sets binding " + getValue() + " with " + value);

					try {
						getValue().setBindingValue(value, fci);
					} catch (TypeMismatchException e) {
						e.printStackTrace();
					} catch (NullReferenceException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (NotSettableContextException e) {
						e.printStackTrace();
					}
				}

			}
			return null;
		}

		/**
		 * Internally used to extract text in single paragraph context (when TextSelection apply on a single paraggraph)
		 * 
		 * @param newStructure
		 * @param actorReference
		 */
		private String extractTextInSingleParagraphContext(FragmentActorReference<?> actorReference) {

			FlexoRun<D, TA> templateStartRun = getTextSelection().getStartRun();
			FlexoRun<D, TA> templateEndRun = getTextSelection().getEndRun();
			FlexoParagraph<D, TA> templateParagraph = (FlexoParagraph<D, TA>) getTextSelection().getStartElement();

			if (actorReference.getElementsMatchingTemplateElement(getTextSelection().getStartElement()).size() > 0) {

				FlexoDocumentElement<?, ?> targetDocumentElement = actorReference
						.getElementsMatchingTemplateElement(getTextSelection().getStartElement()).get(0);
				if (targetDocumentElement instanceof FlexoParagraph) {
					FlexoParagraph<D, TA> targetParagraph = (FlexoParagraph<D, TA>) targetDocumentElement;

					// We compute start target run relatively to the beginning of actual target paragraph, because
					// we cannot rely on structure that may have changed because of concurrent modifications
					FlexoRun<D, TA> startTargetRun = null;
					if (templateStartRun.getIndex() < targetParagraph.getRuns().size()) {
						startTargetRun = targetParagraph.getRuns().get(templateStartRun.getIndex());
					}
					else {
						startTargetRun = targetParagraph.getRuns().get(targetParagraph.getRuns().size() - 1);
					}

					// We compute end target run relatively to the end of actual target paragraph, because
					// we cannot rely on structure that may have changed because of concurrent modifications
					FlexoRun<D, TA> endTargetRun = null;
					if (targetParagraph.getRuns().size() - templateParagraph.getRuns().size() + templateEndRun.getIndex() < targetParagraph
							.getRuns().size()) {
						endTargetRun = targetParagraph.getRuns()
								.get(targetParagraph.getRuns().size() - templateParagraph.getRuns().size() + templateEndRun.getIndex());
					}
					else {
						endTargetRun = targetParagraph.getRuns().get(targetParagraph.getRuns().size() - 1);
					}

					StringBuffer sb = new StringBuffer();

					boolean extraStartRun = (getTextSelection().getStartCharacterIndex() > -1);
					boolean extraEndRun = (getTextSelection().getEndCharacterIndex() > -1);

					for (int i = startTargetRun.getIndex() + (extraStartRun ? 1 : 0); i <= endTargetRun.getIndex()
							- (extraEndRun ? 1 : 0); i++) {
						sb.append(targetParagraph.getRuns().get(i).getText());
					}

					return sb.toString();
				}
				else {
					logger.warning("Text extraction not implemented for " + targetDocumentElement);
				}
			}
			else {
				logger.warning("Could not find element in target document matching " + getTextSelection().getStartElement());
			}

			return null;
		}

	}
}
