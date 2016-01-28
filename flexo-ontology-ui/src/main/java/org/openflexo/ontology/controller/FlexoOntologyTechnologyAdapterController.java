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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.URIParameter;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.TypeAwareModelSlotInstance;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyObject;
import org.openflexo.foundation.ontology.fml.ClassParameter;
import org.openflexo.foundation.ontology.fml.DataPropertyParameter;
import org.openflexo.foundation.ontology.fml.IndividualParameter;
import org.openflexo.foundation.ontology.fml.ObjectPropertyParameter;
import org.openflexo.foundation.ontology.fml.PropertyParameter;
import org.openflexo.foundation.ontology.fml.inspector.ClassInspectorEntry;
import org.openflexo.foundation.ontology.fml.inspector.DataPropertyInspectorEntry;
import org.openflexo.foundation.ontology.fml.inspector.IndividualInspectorEntry;
import org.openflexo.foundation.ontology.fml.inspector.ObjectPropertyInspectorEntry;
import org.openflexo.foundation.ontology.fml.inspector.PropertyInspectorEntry;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.FIBWidget;
import org.openflexo.gina.model.container.FIBPanel;
import org.openflexo.gina.model.container.FIBPanel.Layout;
import org.openflexo.gina.model.container.FIBTab;
import org.openflexo.gina.model.container.layout.GridBagLayoutConstraints;
import org.openflexo.gina.model.container.layout.GridBagLayoutConstraints.AnchorType;
import org.openflexo.gina.model.container.layout.GridBagLayoutConstraints.FillType;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints.TwoColsLayoutLocation;
import org.openflexo.gina.model.widget.FIBCustom;
import org.openflexo.gina.model.widget.FIBCustom.FIBCustomAssignment;
import org.openflexo.gina.model.widget.FIBLabel;
import org.openflexo.gina.model.widget.FIBTextField;
import org.openflexo.ontology.components.widget.FIBIndividualSelector;
import org.openflexo.ontology.components.widget.FIBPropertySelector;
import org.openflexo.ontology.components.widget.OntologyBrowserModel;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterController;

/**
 * Implemented by all TechnologyAdapterController implementing a technology conform to IFlexoOntology layer
 * 
 * @author sylvain
 * 
 */
public abstract class FlexoOntologyTechnologyAdapterController<TA extends TechnologyAdapter> extends TechnologyAdapterController<TA> {

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
	 * Factory method used to instanciate a technology-specific FIBWidget for a given {@link InspectorEntry}<br>
	 * We manage here {@link IFlexoOntology}-specific {@link InspectorEntry}
	 * 
	 * @param entry
	 * @param newTab
	 * @param factory
	 * @return
	 */
	@Override
	public FIBWidget makeWidget(final InspectorEntry entry, FIBTab newTab, FIBModelFactory factory) {
		if (entry instanceof IndividualInspectorEntry) {
			IndividualInspectorEntry individualEntry = (IndividualInspectorEntry) entry;
			FIBCustom individualSelector = factory.newFIBCustom();
			individualSelector.setComponentClass(FIBIndividualSelector.class);
			// Quick and dirty hack to configure ClassSelector: refactor this when new binding model will be in use
			// component.context = xxx
			FIBCustomAssignment projectAssignment = factory.newInstance(FIBCustomAssignment.class);
			projectAssignment.setOwner(individualSelector);
			projectAssignment.setVariable(new DataBinding<Object>("component.project"));
			projectAssignment.setValue(new DataBinding<Object>("data.project"));
			projectAssignment.setMandatory(true);
			individualSelector.addToAssignments(projectAssignment);

			/*individualSelector.addToAssignments(new FIBCustomAssignment(individualSelector, new DataBinding("component.project"),
					new DataBinding("data.project"), true));*/
			/*individualSelector.addToAssignments(new FIBCustomAssignment(individualSelector,
					new DataBinding("component.contextOntologyURI"), new DataBinding('"' + individualEntry.getViewPoint()
							.getViewpointOntology().getURI() + '"') {
						@Override
						public BindingFactory getBindingFactory() {
							return entry.getBindingFactory();
						}
					}, true));*/
			// Quick and dirty hack to configure IndividualSelector: refactor this when new binding model will be in use
			IFlexoOntologyClass conceptClass = null;
			if (individualEntry.getIsDynamicConceptValue()) {
				// conceptClass = classEntry.evaluateConceptValue(action);
				// TODO: implement proper scheme with new binding support
				logger.warning("Please implement me !!!!!!!!!");
			}
			else {
				conceptClass = individualEntry.getConcept();
			}
			if (conceptClass != null) {
				FIBCustomAssignment conceptClassAssignment = factory.newInstance(FIBCustomAssignment.class);
				conceptClassAssignment.setOwner(individualSelector);
				conceptClassAssignment.setVariable(new DataBinding<Object>("component.typeURI"));
				conceptClassAssignment.setValue(new DataBinding('"' + conceptClass.getURI() + '"'));
				conceptClassAssignment.setMandatory(true);
				individualSelector.addToAssignments(conceptClassAssignment);
				/*individualSelector.addToAssignments(new FIBCustomAssignment(individualSelector, new DataBinding("component.typeURI"),
						new DataBinding('"' + conceptClass.getURI() + '"'), true));*/
			}
			if (StringUtils.isNotEmpty(individualEntry.getRenderer())) {
				FIBCustomAssignment rendererAssignment = factory.newInstance(FIBCustomAssignment.class);
				rendererAssignment.setOwner(individualSelector);
				rendererAssignment.setVariable(new DataBinding<Object>("component.renderer"));
				rendererAssignment.setValue(new DataBinding('"' + individualEntry.getRenderer() + '"'));
				rendererAssignment.setMandatory(true);
				individualSelector.addToAssignments(rendererAssignment);
				/*individualSelector.addToAssignments(new FIBCustomAssignment(individualSelector, new DataBinding("component.renderer"),
						new DataBinding('"' + individualEntry.getRenderer() + '"'), true));*/
			}

			newTab.addToSubComponents(individualSelector, new TwoColsLayoutConstraints(TwoColsLayoutLocation.right, true, false));
			return individualSelector;
		}
		else if (entry instanceof ClassInspectorEntry) {
			ClassInspectorEntry classEntry = (ClassInspectorEntry) entry;
			FIBCustom classSelector = factory.newFIBCustom();
			classSelector.setComponentClass(org.openflexo.ontology.components.widget.FIBClassSelector.class);
			// Quick and dirty hack to configure ClassSelector: refactor this when new binding model will be in use
			// component.context = xxx
			FIBCustomAssignment projectAssignment = factory.newInstance(FIBCustomAssignment.class);
			projectAssignment.setOwner(classSelector);
			projectAssignment.setVariable(new DataBinding<Object>("component.project"));
			projectAssignment.setValue(new DataBinding<Object>("data.project"));
			projectAssignment.setMandatory(true);
			classSelector.addToAssignments(projectAssignment);
			/*classSelector.addToAssignments(new FIBCustomAssignment(classSelector, new DataBinding<Object>("component.project"),
					new DataBinding<Object>("data.project"), true));*/
			/*classSelector.addToAssignments(new FIBCustomAssignment(classSelector, new DataBinding("component.contextOntologyURI"),
					new DataBinding('"' + classEntry.getViewPoint().getViewpointOntology().getURI() + '"') {
						@Override
						public BindingFactory getBindingFactory() {
							return entry.getBindingFactory();
						}
					}, true));*/
			// Quick and dirty hack to configure ClassSelector: refactor this when new binding model will be in use
			IFlexoOntologyClass conceptClass = null;
			if (classEntry.getIsDynamicConceptValue()) {
				// conceptClass = classEntry.evaluateConceptValue(action);
				// TODO: implement proper scheme with new binding support
				logger.warning("Please implement me !!!!!!!!!");
			}
			else {
				conceptClass = classEntry.getConcept();
			}
			if (conceptClass != null) {
				FIBCustomAssignment rootClassAssignment = factory.newInstance(FIBCustomAssignment.class);
				rootClassAssignment.setOwner(classSelector);
				rootClassAssignment.setVariable(new DataBinding<Object>("component.rootClassURI"));
				rootClassAssignment.setValue(new DataBinding<Object>('"' + conceptClass.getURI() + '"'));
				rootClassAssignment.setMandatory(true);
				classSelector.addToAssignments(rootClassAssignment);
				/*	classSelector.addToAssignments(new FIBCustomAssignment(classSelector,
							new DataBinding<Object>("component.rootClassURI"), new DataBinding<Object>('"' + conceptClass.getURI() + '"'),
							true));*/
			}
			newTab.addToSubComponents(classSelector, new TwoColsLayoutConstraints(TwoColsLayoutLocation.right, true, false));
			return classSelector;
		}
		else if (entry instanceof PropertyInspectorEntry) {
			PropertyInspectorEntry propertyEntry = (PropertyInspectorEntry) entry;
			FIBCustom propertySelector = factory.newFIBCustom();
			propertySelector.setComponentClass(FIBPropertySelector.class);
			// Quick and dirty hack to configure FIBPropertySelector: refactor this when new binding model will be in use
			// component.context = xxx
			FIBCustomAssignment projectAssignment = factory.newInstance(FIBCustomAssignment.class);
			projectAssignment.setOwner(propertySelector);
			projectAssignment.setVariable(new DataBinding<Object>("component.project"));
			projectAssignment.setValue(new DataBinding<Object>("data.project"));
			projectAssignment.setMandatory(true);
			propertySelector.addToAssignments(projectAssignment);
			/*propertySelector.addToAssignments(new FIBCustomAssignment(propertySelector, new DataBinding<Object>("component.project"),
					new DataBinding<Object>("data.project"), true));*/
			/*propertySelector.addToAssignments(new FIBCustomAssignment(propertySelector, new DataBinding("component.contextOntologyURI"),
					new DataBinding('"' + propertyEntry.getViewPoint().getViewpointOntology().getURI() + '"') {
						@Override
						public BindingFactory getBindingFactory() {
							return entry.getBindingFactory();
						}
					}, true));*/

			// Quick and dirty hack to configure FIBPropertySelector: refactor this when new binding model will be in use
			IFlexoOntologyClass domainClass = null;
			if (propertyEntry.getIsDynamicDomainValue()) {
				// domainClass = propertyEntry.evaluateDomainValue(action);
				// TODO: implement proper scheme with new binding support
				logger.warning("Please implement me !!!!!!!!!");
			}
			else {
				domainClass = propertyEntry.getDomain();
			}
			if (domainClass != null) {
				FIBCustomAssignment domainClassAssignment = factory.newInstance(FIBCustomAssignment.class);
				domainClassAssignment.setOwner(propertySelector);
				domainClassAssignment.setVariable(new DataBinding<Object>("component.domainClassURI"));
				domainClassAssignment.setValue(new DataBinding<Object>('"' + domainClass.getURI() + '"'));
				domainClassAssignment.setMandatory(true);
				propertySelector.addToAssignments(domainClassAssignment);
				/*propertySelector.addToAssignments(new FIBCustomAssignment(propertySelector, new DataBinding<Object>(
						"component.domainClassURI"), new DataBinding<Object>('"' + domainClass.getURI() + '"'), true));*/
			}
			if (propertyEntry instanceof ObjectPropertyInspectorEntry) {
				IFlexoOntologyClass rangeClass = null;
				if (propertyEntry.getIsDynamicDomainValue()) {
					// domainClass = propertyEntry.evaluateDomainValue(action);
					// TODO: implement proper scheme with new binding support
					logger.warning("Please implement me !!!!!!!!!");
				}
				else {
					rangeClass = ((ObjectPropertyInspectorEntry) propertyEntry).getRange();
				}
				if (rangeClass != null) {
					FIBCustomAssignment rangeClassAssignment = factory.newInstance(FIBCustomAssignment.class);
					rangeClassAssignment.setOwner(propertySelector);
					rangeClassAssignment.setVariable(new DataBinding<Object>("component.rangeClassURI"));
					rangeClassAssignment.setValue(new DataBinding<Object>('"' + rangeClass.getURI() + '"'));
					rangeClassAssignment.setMandatory(true);
					propertySelector.addToAssignments(rangeClassAssignment);
					/*propertySelector.addToAssignments(new FIBCustomAssignment(propertySelector, new DataBinding<Object>(
							"component.rangeClassURI"), new DataBinding<Object>('"' + rangeClass.getURI() + '"'), true));*/
				}
			}
			if (propertyEntry instanceof ObjectPropertyInspectorEntry) {
				FIBCustomAssignment selectDataPropertiesAssignment = factory.newInstance(FIBCustomAssignment.class);
				selectDataPropertiesAssignment.setOwner(propertySelector);
				selectDataPropertiesAssignment.setVariable(new DataBinding<Object>("component.selectDataProperties"));
				selectDataPropertiesAssignment.setValue(new DataBinding<Object>("false"));
				selectDataPropertiesAssignment.setMandatory(true);
				propertySelector.addToAssignments(selectDataPropertiesAssignment);
				/*propertySelector.addToAssignments(new FIBCustomAssignment(propertySelector, new DataBinding<Object>(
						"component.selectDataProperties"), new DataBinding<Object>("false"), true));*/
			}
			else if (propertyEntry instanceof DataPropertyInspectorEntry) {
				FIBCustomAssignment selectObjectPropertiesAssignment = factory.newInstance(FIBCustomAssignment.class);
				selectObjectPropertiesAssignment.setOwner(propertySelector);
				selectObjectPropertiesAssignment.setVariable(new DataBinding<Object>("component.selectObjectProperties"));
				selectObjectPropertiesAssignment.setValue(new DataBinding<Object>("false"));
				selectObjectPropertiesAssignment.setMandatory(true);
				propertySelector.addToAssignments(selectObjectPropertiesAssignment);
				/*propertySelector.addToAssignments(new FIBCustomAssignment(propertySelector, new DataBinding<Object>(
						"component.selectObjectProperties"), new DataBinding<Object>("false"), true));*/
			}

			// Quick and dirty hack to configure PropertySelector: refactor this when new binding model will be in use
			/*propertySelector.addToAssignments(new FIBCustomAssignment(propertySelector, new DataBinding("component.domainClassURI"),
					new DataBinding('"' + ((PropertyInspectorEntry) entry)._getDomainURI() + '"') {
						@Override
						public BindingFactory getBindingFactory() {
							return entry.getBindingFactory();
						}
					}, true));*/
			newTab.addToSubComponents(propertySelector, new TwoColsLayoutConstraints(TwoColsLayoutLocation.right, true, false));
			return propertySelector;
		}

		return super.makeWidget(entry, newTab, factory);

	}

	/**
	 * Factory method used to instanciate a technology-specific FIBWidget for a given {@link FlexoBehaviourParameter}<br>
	 * Provides a hook to specialize this method in a given technology
	 * 
	 * @param parameter
	 * @param panel
	 * @param index
	 * @return
	 */
	@Override
	public FIBComponent makeWidget(final FlexoBehaviourParameter parameter, FIBPanel panel, int index, FlexoBehaviourAction<?, ?, ?> action,
			FIBModelFactory fibModelFactory) {
		if (parameter instanceof URIParameter) {
			FIBPanel uriPanel = makeURIPanel((URIParameter) parameter, fibModelFactory);
			return registerWidget(uriPanel, parameter, panel, index);
		}
		else if (parameter instanceof IndividualParameter) {
			FIBCustom individualSelector = fibModelFactory.newFIBCustom();
			individualSelector.setComponentClass(FIBIndividualSelector.class);
			// Quick and dirty hack to configure ClassSelector: refactor this when new binding model will be in use
			// component.context = xxx
			/*individualSelector.addToAssignments(new FIBCustomAssignment(individualSelector, new DataBinding<Object>("component.project"),
					new DataBinding<Object>("data.project"), true));*/
			/*individualSelector.addToAssignments(new FIBCustomAssignment(individualSelector,
					new DataBinding("component.contextOntologyURI"), new DataBinding('"' + parameter.getViewPoint().getViewpointOntology()
							.getURI() + '"') {
						@Override
						public BindingFactory getBindingFactory() {
							return parameter.getBindingFactory();
						}
					}, true));*/

			individualSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(individualSelector,
					new DataBinding<Object>("component.informationSpace"), new DataBinding<Object>("data.project.informationSpace"), true));
			if (action.getVirtualModelInstance() != null) {
				ModelSlotInstance msInstance = action.getVirtualModelInstance()
						.getModelSlotInstance(((IndividualParameter) parameter).getModelSlot());
				if (msInstance instanceof TypeAwareModelSlotInstance && ((TypeAwareModelSlotInstance) msInstance).getModel() != null) {
					individualSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(individualSelector,
							new DataBinding("component.contextOntologyURI"),
							new DataBinding<Object>('"' + ((TypeAwareModelSlotInstance) msInstance).getModel().getURI() + '"'), true));
				}
				else {
					logger.warning("No model defined for model slot " + ((IndividualParameter) parameter).getModelSlot());
				}
			}
			else {
				logger.warning("Inconsistent data: no VirtualModelInstance for action " + action);
			}
			// Quick and dirty hack to configure IndividualSelector: refactor this when new binding model will be in use
			individualSelector.addToAssignments(
					fibModelFactory.newFIBCustomAssignment(individualSelector, new DataBinding<Object>("component.typeURI"),
							new DataBinding<Object>('"' + ((IndividualParameter) parameter)._getConceptURI() + '"'), true));
			if (StringUtils.isNotEmpty(((IndividualParameter) parameter).getRenderer())) {
				individualSelector.addToAssignments(
						fibModelFactory.newFIBCustomAssignment(individualSelector, new DataBinding<Object>("component.renderer"),
								new DataBinding<Object>('"' + ((IndividualParameter) parameter).getRenderer() + '"'), true));
			}
			return registerWidget(individualSelector, parameter, panel, index);
		}
		else if (parameter instanceof ClassParameter) {
			ClassParameter classParameter = (ClassParameter) parameter;
			FIBCustom classSelector = fibModelFactory.newFIBCustom();
			classSelector.setComponentClass(org.openflexo.ontology.components.widget.FIBClassSelector.class);
			// Quick and dirty hack to configure ClassSelector: refactor this when new binding model will be in use
			// component.context = xxx
			/*classSelector.addToAssignments(new FIBCustomAssignment(classSelector, new DataBinding<Object>("component.project"),
					new DataBinding<Object>("data.project"), true));*/
			/*classSelector.addToAssignments(new FIBCustomAssignment(classSelector, new DataBinding("component.contextOntologyURI"),
					new DataBinding('"' + classParameter.getViewPoint().getViewpointOntology().getURI() + '"') {
						@Override
						public BindingFactory getBindingFactory() {
							return parameter.getBindingFactory();
						}
					}, true));*/
			classSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(classSelector,
					new DataBinding<Object>("component.informationSpace"), new DataBinding<Object>("data.project.informationSpace"), true));
			if (action.getVirtualModelInstance() != null) {
				ModelSlotInstance msInstance = action.getVirtualModelInstance()
						.getModelSlotInstance(((IndividualParameter) parameter).getModelSlot());
				if (msInstance instanceof TypeAwareModelSlotInstance && ((TypeAwareModelSlotInstance) msInstance).getModel() != null) {
					classSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(classSelector,
							new DataBinding("component.contextOntologyURI"),
							new DataBinding<Object>('"' + ((TypeAwareModelSlotInstance) msInstance).getModel().getURI() + '"'), true));
				}
				else {
					logger.warning("No model defined for model slot " + ((IndividualParameter) parameter).getModelSlot());
				}
			}
			else {
				logger.warning("Inconsistent data: no VirtualModelInstance for action " + action);
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
				classSelector.addToAssignments(
						fibModelFactory.newFIBCustomAssignment(classSelector, new DataBinding<Object>("component.rootClassURI"),
								new DataBinding<Object>('"' + conceptClass.getURI() + '"'), true));
			}
			return registerWidget(classSelector, parameter, panel, index);
		}
		else if (parameter instanceof PropertyParameter) {
			PropertyParameter propertyParameter = (PropertyParameter) parameter;
			FIBCustom propertySelector = fibModelFactory.newFIBCustom();
			propertySelector.setComponentClass(FIBPropertySelector.class);
			// Quick and dirty hack to configure FIBPropertySelector: refactor this when new binding model will be in use
			// component.context = xxx
			/*propertySelector.addToAssignments(new FIBCustomAssignment(propertySelector, new DataBinding<Object>("component.project"),
					new DataBinding<Object>("data.project"), true));*/
			/*propertySelector.addToAssignments(new FIBCustomAssignment(propertySelector, new DataBinding("component.contextOntologyURI"),
					new DataBinding('"' + propertyParameter.getViewPoint().getViewpointOntology().getURI() + '"') {
						@Override
						public BindingFactory getBindingFactory() {
							return parameter.getBindingFactory();
						}
					}, true));*/
			propertySelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(propertySelector,
					new DataBinding<Object>("component.informationSpace"), new DataBinding<Object>("data.project.informationSpace"), true));
			if (action.getVirtualModelInstance() != null) {
				ModelSlotInstance msInstance = action.getVirtualModelInstance()
						.getModelSlotInstance(((IndividualParameter) parameter).getModelSlot());
				if (msInstance instanceof TypeAwareModelSlotInstance && ((TypeAwareModelSlotInstance) msInstance).getModel() != null) {
					propertySelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(propertySelector,
							new DataBinding("component.contextOntologyURI"),
							new DataBinding<Object>('"' + ((TypeAwareModelSlotInstance) msInstance).getModel().getURI() + '"'), true));
				}
				else {
					logger.warning("No model defined for model slot " + ((IndividualParameter) parameter).getModelSlot());
				}
			}
			else {
				logger.warning("Inconsistent data: no VirtualModelInstance for action " + action);
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
				propertySelector.addToAssignments(
						fibModelFactory.newFIBCustomAssignment(propertySelector, new DataBinding<Object>("component.domainClassURI"),
								new DataBinding<Object>('"' + domainClass.getURI() + '"'), true));
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
					propertySelector.addToAssignments(
							fibModelFactory.newFIBCustomAssignment(propertySelector, new DataBinding<Object>("component.rangeClassURI"),
									new DataBinding<Object>('"' + rangeClass.getURI() + '"'), true));
				}
			}

			if (propertyParameter instanceof ObjectPropertyParameter) {
				propertySelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(propertySelector,
						new DataBinding<Object>("component.selectDataProperties"), DataBinding.makeFalseBinding(), true));
			}
			else if (propertyParameter instanceof DataPropertyParameter) {
				propertySelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(propertySelector,
						new DataBinding<Object>("component.selectObjectProperties"), DataBinding.makeFalseBinding(), true));
			}
			return registerWidget(propertySelector, parameter, panel, index);
		}

		return super.makeWidget(parameter, panel, index, action, fibModelFactory);
	}

	protected FIBPanel makeURIPanel(final URIParameter parameter, FIBModelFactory fibModelFactory) {
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
		uriLabel.setData(new DataBinding<Object>("data.retrieveFullURI" + "(data.parametersDefinitions" + "." + parameter.getName() + ")"));
		returned.addToSubComponents(tf, new GridBagLayoutConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE,
				GridBagConstraints.REMAINDER, 1, 1.0, 0, AnchorType.west, FillType.horizontal, 0, 0, 0, 0, 0, 0));
		returned.addToSubComponents(uriLabel, new GridBagLayoutConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE,
				GridBagConstraints.REMAINDER, 1, 1.0, 0, AnchorType.west, FillType.horizontal, -3, 0, 0, 0, 0, 0));
		tf.setData(new DataBinding<Object>("data.parameters." + parameter.getName()));
		return returned;
	}

	/**
	 * Provides a hook to handle specific {@link FlexoBehaviourParameter} for a given technology
	 * 
	 * @param availableParameterTypes
	 */
	@Override
	public void appendSpecificFlexoBehaviourParameters(List<Class<? extends FlexoBehaviourParameter>> availableParameterTypes) {
		super.appendSpecificFlexoBehaviourParameters(availableParameterTypes);
		availableParameterTypes.add(IndividualParameter.class);
		availableParameterTypes.add(ClassParameter.class);
		availableParameterTypes.add(ObjectPropertyParameter.class);
		availableParameterTypes.add(DataPropertyParameter.class);
	}

}
