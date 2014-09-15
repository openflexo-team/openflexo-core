package org.openflexo.foundation.viewpoint.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.viewpoint.FlexoBehaviour;

public class FlexoBehaviourParametersDefinitionBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(FlexoBehaviourParametersDefinitionBindingVariable.class.getPackage().getName());

	private FlexoBehaviour flexoBehaviour;

	public FlexoBehaviourParametersDefinitionBindingVariable(FlexoBehaviour flexoBehaviour) {
		super(FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY, flexoBehaviour.getFlexoBehaviourParametersType(), true);
	}

	@Override
	public void delete() {
		super.delete();
	}

	@Override
	public String getVariableName() {
		return FlexoBehaviourBindingModel.PARAMETERS_DEFINITION_PROPERTY;
	}

	@Override
	public Type getType() {
		if (flexoBehaviour != null) {
			return flexoBehaviour.getFlexoBehaviourParametersType();
		}
		return super.getType();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}

}