package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.fml.FlexoBehaviour;

public class FlexoBehaviourParametersBindingVariable extends BindingVariable implements PropertyChangeListener {
	static final Logger logger = Logger.getLogger(FlexoBehaviourParametersBindingVariable.class.getPackage().getName());

	private FlexoBehaviour flexoBehaviour;

	public FlexoBehaviourParametersBindingVariable(FlexoBehaviour flexoBehaviour) {
		super(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY, flexoBehaviour.getFlexoBehaviourParametersValuesType(), true);
	}

	@Override
	public void delete() {
		super.delete();
	}

	@Override
	public String getVariableName() {
		return FlexoBehaviourBindingModel.PARAMETERS_PROPERTY;
	}

	@Override
	public Type getType() {
		if (flexoBehaviour != null) {
			return flexoBehaviour.getFlexoBehaviourParametersValuesType();
		}
		return super.getType();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}

}