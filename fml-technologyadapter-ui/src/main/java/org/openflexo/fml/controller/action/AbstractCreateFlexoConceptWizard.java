/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.action;

import java.awt.Dimension;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.FlexoActionWizard;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.action.AbstractCreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateContainedVirtualModel;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreateTopLevelVirtualModel;
import org.openflexo.foundation.fml.action.PropertyEntry;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.view.controller.FlexoController;

/**
 * Common stuff for wizards of {@link AbstractCreateFlexoConcept} action
 * 
 * @author sylvain
 *
 * @param <A>
 * @see CreateFlexoConcept
 * @see CreateContainedVirtualModel
 * @see CreateTopLevelVirtualModel
 */
public abstract class AbstractCreateFlexoConceptWizard<A extends AbstractCreateFlexoConcept<?, ?, ?>> extends FlexoActionWizard<A> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractCreateFlexoConceptWizard.class.getPackage().getName());

	private static final Dimension DIMENSIONS = new Dimension(900, 600);

	private ConfigurePropertiesForNewFlexoConcept configurePropertiesForNewFlexoConcept;
	private ConfigureBehavioursForNewFlexoConcept configureBehavioursForNewFlexoConcept;
	private ConfigureInspectorForNewFlexoConcept configureInspectorForNewFlexoConcept;

	public AbstractCreateFlexoConceptWizard(A action, FlexoController controller) {
		super(action, controller);
	}

	protected void createAdditionalSteps() {
		addStep(configurePropertiesForNewFlexoConcept = new ConfigurePropertiesForNewFlexoConcept());
		addStep(configureBehavioursForNewFlexoConcept = new ConfigureBehavioursForNewFlexoConcept());
		addStep(configureInspectorForNewFlexoConcept = new ConfigureInspectorForNewFlexoConcept());
	}

	public ConfigurePropertiesForNewFlexoConcept getConfigurePropertiesForNewFlexoConcept() {
		return configurePropertiesForNewFlexoConcept;
	}

	public ConfigureBehavioursForNewFlexoConcept getConfigureBehavioursForNewFlexoConcept() {
		return configureBehavioursForNewFlexoConcept;
	}

	public ConfigureInspectorForNewFlexoConcept getConfigureInspectorForNewFlexoConcept() {
		return configureInspectorForNewFlexoConcept;
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	/**
	 * This step is used to define some properties to be created for new {@link FlexoConcept}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/ConfigurePropertiesForNewFlexoConcept.fib")
	public class ConfigurePropertiesForNewFlexoConcept extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public AbstractCreateFlexoConcept getAction() {
			return AbstractCreateFlexoConceptWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("declare_some_properties_for_new_flexo_concept");
		}

		@Override
		public boolean isValid() {

			return true;
		}

		public boolean getDefineSomeProperties() {
			return getAction().getPropertiesEntries().size() > 0;
		}

		public Icon getIconForProperty(PropertyEntry entry) {
			return getIconFor(entry);
		}
	}

	/**
	 * This step is used to define some properties to be created for new {@link FlexoConcept}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/ConfigureBehavioursForNewFlexoConcept.fib")
	public class ConfigureBehavioursForNewFlexoConcept extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public AbstractCreateFlexoConcept getAction() {
			return AbstractCreateFlexoConceptWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("declare_some_behaviours_for_new_flexo_concept");
		}

		@Override
		public boolean isValid() {

			return true;
		}

		public boolean getDefineSomeBehaviours() {
			return getAction().getDefineSomeBehaviours();
		}

		public boolean getDefineDefaultCreationScheme() {
			return getAction().getDefineDefaultCreationScheme();
		}

		public boolean getDefineDefaultDeletionScheme() {
			return getAction().getDefineDefaultDeletionScheme();
		}

		public boolean getDefineSynchronizationScheme() {
			return getAction().getDefineSynchronizationScheme();
		}

		public boolean getDefineCloningScheme() {
			return getAction().getDefineCloningScheme();
		}

		public void setDefineSomeBehaviours(boolean defineSomeBehaviours) {
			if (defineSomeBehaviours != getDefineSomeBehaviours()) {
				getAction().setDefineSomeBehaviours(defineSomeBehaviours);
				getPropertyChangeSupport().firePropertyChange("defineSomeBehaviours", !defineSomeBehaviours, defineSomeBehaviours);
				checkValidity();
			}
		}

		public void setDefineDefaultCreationScheme(boolean defineDefaultCreationScheme) {
			if (defineDefaultCreationScheme != getDefineDefaultCreationScheme()) {
				getAction().setDefineDefaultCreationScheme(defineDefaultCreationScheme);
				getPropertyChangeSupport().firePropertyChange("defineDefaultCreationScheme", !defineDefaultCreationScheme,
						defineDefaultCreationScheme);
				checkValidity();
			}
		}

		public void setDefineDefaultDeletionScheme(boolean defineDefaultDeletionScheme) {
			if (defineDefaultDeletionScheme != getDefineDefaultDeletionScheme()) {
				getAction().setDefineDefaultDeletionScheme(defineDefaultDeletionScheme);
				getPropertyChangeSupport().firePropertyChange("defineDefaultDeletionScheme", !defineDefaultDeletionScheme,
						defineDefaultDeletionScheme);
				checkValidity();
			}
		}

		public void setDefineSynchronizationScheme(boolean defineSynchronizationScheme) {
			if (defineSynchronizationScheme != getDefineSynchronizationScheme()) {
				getAction().setDefineSynchronizationScheme(defineSynchronizationScheme);
				getPropertyChangeSupport().firePropertyChange("defineSynchronizationScheme", !defineSynchronizationScheme,
						defineSynchronizationScheme);
				checkValidity();
			}
		}

		public void setDefineCloningScheme(boolean defineCloningScheme) {
			if (defineCloningScheme != getDefineCloningScheme()) {
				getAction().setDefineCloningScheme(defineCloningScheme);
				getPropertyChangeSupport().firePropertyChange("defineCloningScheme", !defineCloningScheme, defineCloningScheme);
				checkValidity();
			}
		}

		public Icon getIconForProperty(PropertyEntry entry) {
			return getIconFor(entry);
		}

		public ImageIcon getCreationSchemeIcon() {
			return FMLIconLibrary.CREATION_SCHEME_ICON;
		}

		public ImageIcon getDeletionSchemeIcon() {
			return FMLIconLibrary.DELETION_SCHEME_ICON;
		}

		public ImageIcon getSynchronizationSchemeIcon() {
			return FMLIconLibrary.SYNCHRONIZATION_SCHEME_ICON;
		}

		public ImageIcon getCloningSchemeIcon() {
			return FMLIconLibrary.CLONING_SCHEME_ICON;
		}
	}

	/**
	 * This step is used to define some properties to be created for new {@link FlexoConcept}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/ConfigureInspectorForNewFlexoConcept.fib")
	public class ConfigureInspectorForNewFlexoConcept extends WizardStep {

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public AbstractCreateFlexoConcept getAction() {
			return AbstractCreateFlexoConceptWizard.this.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("configure_new_flexo_concept_inspector");
		}

		@Override
		public boolean isValid() {

			return true;
		}

		public boolean getDefineInspector() {
			return getAction().getDefineInspector();
		}

		public void setDefineInspector(boolean defineInspector) {
			if (defineInspector != getDefineInspector()) {
				getAction().setDefineInspector(defineInspector);
				getPropertyChangeSupport().firePropertyChange("defineInspector", !defineInspector, defineInspector);
				checkValidity();
			}
		}

		public Icon getIconForProperty(PropertyEntry entry) {
			return getIconFor(entry);
		}
	}

	public Icon getIconFor(PropertyEntry entry) {
		// TechnologyAdapter ta = entry.getTechnologyAdapter();
		switch (entry.getPropertyType()) {
			case PRIMITIVE:
				if (TypeUtils.isString(entry.getType())) {
					return FMLIconLibrary.STRING_PRIMITIVE_ICON;
				}
				if (TypeUtils.isDate(entry.getType())) {
					return FMLIconLibrary.DATE_PRIMITIVE_ICON;
				}
				if (TypeUtils.isBoolean(entry.getType())) {
					return FMLIconLibrary.BOOLEAN_PRIMITIVE_ICON;
				}
				if (TypeUtils.isInteger(entry.getType()) || TypeUtils.isLong(entry.getType()) || TypeUtils.isShort(entry.getType())
						|| TypeUtils.isByte(entry.getType())) {
					return FMLIconLibrary.INTEGER_PRIMITIVE_ICON;
				}
				if (TypeUtils.isFloat(entry.getType()) || TypeUtils.isDouble(entry.getType())) {
					return FMLIconLibrary.DOUBLE_PRIMITIVE_ICON;
				}
				return FMLIconLibrary.UNKNOWN_ICON;
			case ABSTRACT_PROPERTY:
				return FMLIconLibrary.ABSTRACT_PROPERTY_ICON;
			case GET_PROPERTY:
			case GET_SET_PROPERTY:
				return FMLIconLibrary.GET_SET_PROPERTY_ICON;
			case EXPRESSION_PROPERTY:
				return FMLIconLibrary.EXPRESSION_PROPERTY_ICON;
			case FLEXO_CONCEPT_INSTANCE:
				return FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON;
			case MODEL_SLOT:
				return FMLIconLibrary.MODEL_SLOT_ICON;
			case TECHNOLOGY_ROLE:
				return FMLIconLibrary.FLEXO_ROLE_ICON;
		}
		return FMLIconLibrary.UNKNOWN_ICON;

	}

}
