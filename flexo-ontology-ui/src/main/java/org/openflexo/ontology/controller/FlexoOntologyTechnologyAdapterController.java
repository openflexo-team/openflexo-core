/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.ontology.controller;

import java.util.logging.Logger;

import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.ontology.IFlexoOntologyObject;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.ontology.components.widget.OntologyBrowserModel;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterController;

/**
 * Implemented by all TechnologyAdapterController implementing a technology conform to IFlexoOntology layer
 * 
 * @author sylvain
 * 
 */
public abstract class FlexoOntologyTechnologyAdapterController<TA extends TechnologyAdapter<TA>> extends TechnologyAdapterController<TA> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FlexoOntologyTechnologyAdapterController.class.getPackage().getName());

	public abstract OntologyBrowserModel<TA> makeOntologyBrowserModel(IFlexoOntology<TA> context);

	@Override
	public String getWindowTitleforObject(TechnologyObject<TA> object, FlexoController controller) {
		if (object instanceof IFlexoOntologyObject) {
			return ((IFlexoOntologyObject<?>) object).getName();
		}
		return null;
	}

	/**
	 * Factory method used to instanciate a technology-specific FIBWidget for a given {@link FlexoBehaviourParameter}<br>
	 * Provides a hook to specialize this method in a given technology
	 * 
	 * @param parameter
	 * @return
	 */
	/*@Override
	public FIBWidget makeWidget(final WidgetContext parameter, FlexoBehaviourAction<?, ?, ?> action, FIBModelFactory fibModelFactory,
			boolean[] expand) {
		if (parameter instanceof URIParameter) {
			return makeURIPanel((URIParameter) parameter, fibModelFactory);
		}
		else if (parameter instanceof IndividualParameter) {
			FIBCustom individualSelector = fibModelFactory.newFIBCustom();
			individualSelector.setComponentClass(FIBIndividualSelector.class);
			// Quick and dirty hack to configure ClassSelector: refactor this when new binding model will be in use
			individualSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(individualSelector,
					new DataBinding<>("component.informationSpace"), new DataBinding<>("data.project.informationSpace"), true));
			if (action.getVirtualModelInstance() != null) {
				ModelSlotInstance msInstance = action.getVirtualModelInstance()
						.getModelSlotInstance((ModelSlot) ((IndividualParameter) parameter).getModelSlot());
				if (msInstance instanceof TypeAwareModelSlotInstance && ((TypeAwareModelSlotInstance) msInstance).getModel() != null) {
					individualSelector.addToAssignments(
							fibModelFactory.newFIBCustomAssignment(individualSelector, new DataBinding("component.contextOntologyURI"),
									new DataBinding<>('"' + ((TypeAwareModelSlotInstance) msInstance).getModel().getURI() + '"'), true));
				}
				else {
					logger.warning("No model defined for model slot " + ((IndividualParameter) parameter).getModelSlot());
				}
			}
			else {
				logger.warning("Inconsistent data: no FMLRTVirtualModelInstance for action " + action);
			}
			// Quick and dirty hack to configure IndividualSelector: refactor this when new binding model will be in use
			individualSelector
					.addToAssignments(fibModelFactory.newFIBCustomAssignment(individualSelector, new DataBinding<>("component.typeURI"),
							new DataBinding<>('"' + ((IndividualParameter) parameter)._getConceptURI() + '"'), true));
			if (StringUtils.isNotEmpty(((IndividualParameter) parameter).getRenderer())) {
				individualSelector.addToAssignments(
						fibModelFactory.newFIBCustomAssignment(individualSelector, new DataBinding<>("component.renderer"),
								new DataBinding<>('"' + ((IndividualParameter) parameter).getRenderer() + '"'), true));
			}
			return individualSelector;
		}
		else if (parameter instanceof ClassParameter) {
			ClassParameter classParameter = (ClassParameter) parameter;
			FIBCustom classSelector = fibModelFactory.newFIBCustom();
			classSelector.setComponentClass(org.openflexo.ontology.components.widget.FIBClassSelector.class);
			// Quick and dirty hack to configure ClassSelector: refactor this when new binding model will be in use
			classSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(classSelector,
					new DataBinding<>("component.informationSpace"), new DataBinding<>("data.project.informationSpace"), true));
			if (action.getVirtualModelInstance() != null) {
				ModelSlotInstance msInstance = action.getVirtualModelInstance()
						.getModelSlotInstance((ModelSlot) ((IndividualParameter) parameter).getModelSlot());
				if (msInstance instanceof TypeAwareModelSlotInstance && ((TypeAwareModelSlotInstance) msInstance).getModel() != null) {
					classSelector.addToAssignments(
							fibModelFactory.newFIBCustomAssignment(classSelector, new DataBinding("component.contextOntologyURI"),
									new DataBinding<>('"' + ((TypeAwareModelSlotInstance) msInstance).getModel().getURI() + '"'), true));
				}
				else {
					logger.warning("No model defined for model slot " + ((IndividualParameter) parameter).getModelSlot());
				}
			}
			else {
				logger.warning("Inconsistent data: no FMLRTVirtualModelInstance for action " + action);
			}
			// Quick and dirty hack to configure ClassSelector: refactor this when new binding model will be in use
			IFlexoOntologyClass conceptClass = null;
			if (classParameter.getIsDynamicConceptValue()) {
				conceptClass = classParameter.evaluateConceptValue(action);
			}
			else {
				conceptClass = classParameter.getConcept();
			}
			if (conceptClass != null) {
				classSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(classSelector,
						new DataBinding<>("component.rootClassURI"), new DataBinding<>('"' + conceptClass.getURI() + '"'), true));
			}
			return classSelector;
		}
		else if (parameter instanceof PropertyParameter) {
			PropertyParameter propertyParameter = (PropertyParameter) parameter;
			FIBCustom propertySelector = fibModelFactory.newFIBCustom();
			propertySelector.setComponentClass(FIBPropertySelector.class);
			// Quick and dirty hack to configure FIBPropertySelector: refactor this when new binding model will be in use
			propertySelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(propertySelector,
					new DataBinding<>("component.informationSpace"), new DataBinding<>("data.project.informationSpace"), true));
			if (action.getVirtualModelInstance() != null) {
				ModelSlotInstance msInstance = action.getVirtualModelInstance()
						.getModelSlotInstance((ModelSlot) ((IndividualParameter) parameter).getModelSlot());
				if (msInstance instanceof TypeAwareModelSlotInstance && ((TypeAwareModelSlotInstance) msInstance).getModel() != null) {
					propertySelector.addToAssignments(
							fibModelFactory.newFIBCustomAssignment(propertySelector, new DataBinding("component.contextOntologyURI"),
									new DataBinding<>('"' + ((TypeAwareModelSlotInstance) msInstance).getModel().getURI() + '"'), true));
				}
				else {
					logger.warning("No model defined for model slot " + ((IndividualParameter) parameter).getModelSlot());
				}
			}
			else {
				logger.warning("Inconsistent data: no FMLRTVirtualModelInstance for action " + action);
			}
	
			// Quick and dirty hack to configure PropertySelector: refactor this when new binding model will be in use
			IFlexoOntologyClass domainClass = null;
			if (propertyParameter.getIsDynamicDomainValue()) {
				domainClass = propertyParameter.evaluateDomainValue(action);
			}
			else {
				domainClass = propertyParameter.getDomain();
			}
			// System.out.println("domain class = " + domainClass + " uri=" + domainClass.getURI());
			if (domainClass != null) {
				propertySelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(propertySelector,
						new DataBinding<>("component.domainClassURI"), new DataBinding<>('"' + domainClass.getURI() + '"'), true));
			}
	
			if (propertyParameter instanceof ObjectPropertyParameter) {
				IFlexoOntologyClass rangeClass = null;
				if (propertyParameter.getIsDynamicDomainValue()) {
					rangeClass = ((ObjectPropertyParameter) propertyParameter).evaluateRangeValue(action);
				}
				else {
					rangeClass = ((ObjectPropertyParameter) propertyParameter).getRange();
				}
				// System.out.println("range class = " + rangeClass + " uri=" + rangeClass.getURI());
				if (rangeClass != null) {
					propertySelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(propertySelector,
							new DataBinding<>("component.rangeClassURI"), new DataBinding<>('"' + rangeClass.getURI() + '"'), true));
				}
			}
	
			if (propertyParameter instanceof ObjectPropertyParameter) {
				propertySelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(propertySelector,
						new DataBinding<>("component.selectDataProperties"), DataBinding.makeFalseBinding(), true));
			}
			else if (propertyParameter instanceof DataPropertyParameter) {
				propertySelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(propertySelector,
						new DataBinding<>("component.selectObjectProperties"), DataBinding.makeFalseBinding(), true));
			}
			return propertySelector;
		}
	
		return super.makeWidget(parameter, action, fibModelFactory, expand);
	}*/

	/*protected FIBWidget makeURIPanel(final URIParameter parameter, FIBModelFactory fibModelFactory) {
		FIBPanel returned = fibModelFactory.newFIBPanel();
		returned.setName(parameter.getName() + "URIPanel");
		returned.setLayout(Layout.gridbag);
		FIBTextField tf = fibModelFactory.newFIBTextField();
		tf.setName("tf");
		FIBLabel uriLabel = fibModelFactory.newFIBLabel("http://xxxxxx.owl");
		uriLabel.setName("uriLabel");
		Font f = uriLabel.retrieveValidFont();
		if (f != null) {
			uriLabel.setFont(f.deriveFont(10f));
		}
		else {
			uriLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		}
		uriLabel.setData(new DataBinding<>("data.retrieveFullURI" + "(data.parametersDefinitions" + "." + parameter.getName() + ")"));
		returned.addToSubComponents(tf, new GridBagLayoutConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE,
				GridBagConstraints.REMAINDER, 1, 1.0, 0, AnchorType.west, FillType.horizontal, 0, 0, 0, 0, 0, 0));
		returned.addToSubComponents(uriLabel, new GridBagLayoutConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE,
				GridBagConstraints.REMAINDER, 1, 1.0, 0, AnchorType.west, FillType.horizontal, -3, 0, 0, 0, 0, 0));
		tf.setData(new DataBinding<>("data.parameters." + parameter.getName()));
		return returned;
	}*/

	/**
	 * Provides a hook to handle specific {@link FlexoBehaviourParameter} for a given technology
	 * 
	 * @param availableParameterTypes
	 */
	/*@Override
	public void appendSpecificFlexoBehaviourParameters(List<Class<? extends FlexoBehaviourParameter>> availableParameterTypes) {
		super.appendSpecificFlexoBehaviourParameters(availableParameterTypes);
		availableParameterTypes.add(IndividualParameter.class);
		availableParameterTypes.add(ClassParameter.class);
		availableParameterTypes.add(ObjectPropertyParameter.class);
		availableParameterTypes.add(DataPropertyParameter.class);
	}*/

}
