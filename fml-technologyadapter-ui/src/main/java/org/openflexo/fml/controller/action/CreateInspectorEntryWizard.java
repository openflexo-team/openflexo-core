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
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.components.wizard.WizardStep;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.FlexoBehaviourParameterImpl;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.WidgetType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.CreateInspectorEntry;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.FlexoController;

public class CreateInspectorEntryWizard extends AbstractCreateFMLElementWizard<CreateInspectorEntry, FlexoConceptInspector, FMLObject> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateInspectorEntryWizard.class.getPackage().getName());

	private static final String DUPLICATED_NAME = "this_name_is_already_used_please_choose_an_other_one";
	private static final String EMPTY_NAME = "inspector_entry_must_have_an_non_empty_and_unique_name";
	private static final String INVALID_DATA = "inspector_entry_should_define_valid_data";

	private final DescribeInspectorEntry describeInspectorEntry;

	private static final Dimension DIMENSIONS = new Dimension(600, 500);

	public CreateInspectorEntryWizard(CreateInspectorEntry action, FlexoController controller) {
		super(action, controller);
		addStep(describeInspectorEntry = new DescribeInspectorEntry());
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_inspector_entry");
	}

	@Override
	public Image getDefaultPageImage() {
		return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_CONCEPT_PARAMETER_ICON, IconLibrary.BIG_NEW_MARKER).getImage();
	}

	public DescribeInspectorEntry getDescribeInspectorEntry() {
		return describeInspectorEntry;
	}

	@Override
	public Dimension getPreferredSize() {
		return DIMENSIONS;
	}

	/**
	 * This step is used to set {@link VirtualModel} to be used, as well as name and title of the {@link FMLRTVirtualModelInstance}
	 * 
	 * @author sylvain
	 *
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeInspectorEntry.fib")
	public class DescribeInspectorEntry extends WizardStep implements PropertyChangeListener {

		public DescribeInspectorEntry() {
			CreateInspectorEntryWizard.this.getAction().getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		@Override
		public void delete() {
			CreateInspectorEntryWizard.this.getAction().getPropertyChangeSupport().removePropertyChangeListener(this);
			super.delete();
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (Objects.equals(evt.getPropertyName(), "data")) {
				getPropertyChangeSupport().firePropertyChange("entryType", null, getEntryType());
				checkValidity();
			}
		}

		public ApplicationContext getServiceManager() {
			return getController().getApplicationContext();
		}

		public CreateInspectorEntry getAction() {
			return CreateInspectorEntryWizard.this.getAction();
		}

		public VirtualModel getVirtualModel() {
			return CreateInspectorEntryWizard.this.getVirtualModel();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_inspector_entry");
		}

		@Override
		public boolean isValid() {

			if (StringUtils.isEmpty(getEntryName())) {
				setIssueMessage(getAction().getLocales().localizedForKey(EMPTY_NAME), IssueMessageType.ERROR);
				return false;
			}
			else if (getFocusedObject().getEntry(getEntryName()) != null) {
				setIssueMessage(getAction().getLocales().localizedForKey(DUPLICATED_NAME), IssueMessageType.ERROR);
				return false;
			}
			else if (!getAction().getData().isValid()) {
				setIssueMessage(getAction().getLocales().localizedForKey(INVALID_DATA), IssueMessageType.ERROR);
				return false;
			}
			if (StringUtils.isEmpty(getDescription())) {
				setIssueMessage(getAction().getLocales().localizedForKey("it_is_recommanded_to_describe_entry"), IssueMessageType.WARNING);
			}

			return true;
		}

		public String getEntryName() {
			return getAction().getEntryName();
		}

		public void setEntryName(String entryName) {
			if ((entryName == null && getEntryName() != null) || (entryName != null && !entryName.equals(getEntryName()))) {
				String oldValue = getEntryName();
				getAction().setEntryName(entryName);
				getPropertyChangeSupport().firePropertyChange("entryName", oldValue, entryName);
				checkValidity();
			}
		}

		public Type getEntryType() {
			return getAction().getEntryType();
		}

		public void setEntryType(Type entryType) {
			if ((entryType == null && getEntryType() != null) || (entryType != null && !entryType.equals(getEntryType()))) {
				String oldEntryName = getEntryName();
				Type oldValue = getEntryType();
				getAction().setEntryType(entryType);
				getPropertyChangeSupport().firePropertyChange("entryType", oldValue, entryType);
				getPropertyChangeSupport().firePropertyChange("entryName", oldEntryName, getEntryName());
				getPropertyChangeSupport().firePropertyChange("availableWidgetTypes", null, getAvailableWidgetTypes());
				checkValidity();
			}
		}

		public WidgetType getWidgetType() {
			return getAction().getWidgetType();
		}

		public void setWidgetType(WidgetType widgetType) {
			if (widgetType != getWidgetType()) {
				WidgetType oldValue = getWidgetType();
				getAction().setWidgetType(widgetType);
				getPropertyChangeSupport().firePropertyChange("setWidgetType", oldValue, widgetType);
				getPropertyChangeSupport().firePropertyChange("isList", !isList(), isList());
				getPropertyChangeSupport().firePropertyChange("availableWidgetTypes", null, getAvailableWidgetTypes());
				checkValidity();
			}
		}

		public List<WidgetType> getAvailableWidgetTypes() {
			return FlexoBehaviourParameterImpl.getAvailableWidgetTypes(getEntryType());
		}

		public boolean isList() {
			return TypeUtils.isList(getEntryType());
		}

		public boolean getIsReadOnly() {
			return getAction().getIsReadOnly();
		}

		public void setIsReadOnly(boolean isReadOnly) {
			if (isReadOnly != getIsReadOnly()) {
				getAction().setIsReadOnly(isReadOnly);
				getPropertyChangeSupport().firePropertyChange("isReadOnly", !isReadOnly, isReadOnly);
				checkValidity();
			}
		}

		public String getDescription() {
			return getAction().getDescription();
		}

		public void setDescription(String description) {
			if ((description == null && getDescription() != null) || (description != null && !description.equals(getDescription()))) {
				String oldValue = getDescription();
				getAction().setDescription(description);
				getPropertyChangeSupport().firePropertyChange("description", oldValue, description);
				checkValidity();
			}
		}

		/*public Class<? extends InspectorEntry> getInspectorEntryClass() {
			return getAction().getInspectorEntryClass();
		}
		
		public void setInspectorEntryClass(Class<? extends InspectorEntry> entryClass) {
			if (getInspectorEntryClass() != entryClass) {
				Class<? extends InspectorEntry> oldValue = getInspectorEntryClass();
				getAction().setInspectorEntryClass(entryClass);
				getPropertyChangeSupport().firePropertyChange("inspectorEntryClass", oldValue, entryClass);
				getPropertyChangeSupport().firePropertyChange("entryName", oldValue, entryClass);
				checkValidity();
			}
		}
		
		public List<Class<? extends InspectorEntry>> getAvailableInspectorEntryTypes() {
			return getAction().getAvailableInspectorEntryTypes();
		}*/

	}

}
