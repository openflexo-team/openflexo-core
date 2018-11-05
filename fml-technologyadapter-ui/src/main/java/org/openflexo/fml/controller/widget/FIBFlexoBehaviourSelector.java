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

package org.openflexo.fml.controller.widget;

import java.util.logging.Logger;

import org.openflexo.components.widget.FIBFlexoObjectSelector;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to select an {@link FlexoBehaviour} of a {@link FlexoConcept}
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBFlexoBehaviourSelector extends FIBFlexoObjectSelector<FlexoBehaviour> {

	static final Logger logger = Logger.getLogger(FIBFlexoBehaviourSelector.class.getPackage().getName());

	public static Resource FIB_FILE_NAME = ResourceLocator.locateResource("Fib/FlexoBehaviourSelector.fib");

	public FIBFlexoBehaviourSelector(FlexoBehaviour editedObject) {
		super(editedObject);
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE_NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<FlexoBehaviour> getRepresentedType() {
		return (Class<FlexoBehaviour>) getBehaviourClass();
	}

	@Override
	public String renderedString(FlexoBehaviour editedObject) {
		if (editedObject != null) {
			return editedObject.getName();
		}
		return "";
	}

	private FlexoConcept flexoConcept;

	public FlexoConcept getFlexoConcept() {
		return flexoConcept;
	}

	@CustomComponentParameter(name = "flexoConcept", type = CustomComponentParameter.Type.MANDATORY)
	public void setFlexoConcept(FlexoConcept flexoConcept) {

		if (this.flexoConcept != flexoConcept) {
			FlexoObject oldConcept = getFlexoConcept();
			this.flexoConcept = flexoConcept;
			getPropertyChangeSupport().firePropertyChange("flexoConcept", oldConcept, getFlexoConcept());
		}
	}

	private Class<? extends FlexoBehaviour> behaviourClass;

	public Class<? extends FlexoBehaviour> getBehaviourClass() {
		if (behaviourClass == null) {
			return FlexoBehaviour.class;
		}
		return behaviourClass;
	}

	@CustomComponentParameter(name = "behaviourClass", type = CustomComponentParameter.Type.OPTIONAL)
	public void setBehaviourClass(Class<? extends FlexoBehaviour> behaviourClass) {
		if ((behaviourClass == null && this.behaviourClass != null)
				|| (behaviourClass != null && !behaviourClass.equals(this.behaviourClass))) {
			Class<? extends FlexoBehaviour> oldValue = this.behaviourClass;
			this.behaviourClass = behaviourClass;
			getPropertyChangeSupport().firePropertyChange("behaviourClass", oldValue, behaviourClass);
		}
	}

	@Override
	public boolean isAcceptableValue(Object o) {
		return super.isAcceptableValue(o);
	}
}
