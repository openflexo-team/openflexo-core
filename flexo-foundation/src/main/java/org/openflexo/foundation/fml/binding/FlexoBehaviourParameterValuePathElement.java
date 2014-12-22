package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingEvaluationContext;
import org.openflexo.antar.binding.BindingPathElement;
import org.openflexo.antar.binding.SimplePathElement;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fmlrt.action.FlexoBehaviourAction.ParameterValues;

public class FlexoBehaviourParameterValuePathElement extends SimplePathElement implements PropertyChangeListener {

	private static final Logger logger = Logger.getLogger(FlexoBehaviourParameterValuePathElement.class.getPackage().getName());

	private final FlexoBehaviourParameter parameter;
	private Type lastKnownType = null;

	public FlexoBehaviourParameterValuePathElement(BindingPathElement parent, FlexoBehaviourParameter parameter) {
		super(parent, parameter.getName(), parameter.getType());
		this.parameter = parameter;
		if (parameter != null && parameter.getPropertyChangeSupport() != null) {
			parameter.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		if (parameter != null) {
			lastKnownType = parameter.getType();
		}

	}

	@Override
	public void delete() {
		if (parameter != null && parameter.getPropertyChangeSupport() != null) {
			parameter.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public Type getType() {
		return parameter.getType();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == parameter) {
			if (lastKnownType != getType()) {
				lastKnownType = getType();
				System.out.println("Tiens, si je veux, je notifie que le type change");
			}

		}
	}

	@Override
	public String getLabel() {
		return parameter.getName();
	}

	@Override
	public String getTooltipText(Type resultingType) {
		return parameter.getDescription();
	}

	@Override
	public Object getBindingValue(Object target, BindingEvaluationContext context) throws TypeMismatchException, NullReferenceException {
		if (target instanceof Hashtable) {
			Hashtable<FlexoBehaviourParameter, Object> allParameters = (Hashtable<FlexoBehaviourParameter, Object>) target;
			return allParameters.get(parameter);
		}
		logger.warning("Please implement me, target=" + target + " context=" + context);
		return null;
	}

	@Override
	public void setBindingValue(Object value, Object target, BindingEvaluationContext context) throws TypeMismatchException,
			NullReferenceException {
		if (target instanceof ParameterValues) {
			ParameterValues allParameters = (ParameterValues) target;
			// System.out.println("Setting value " + value + " for " + parameter);
			// System.out.println("Parent=" + getParent() + " of " + getParent().getClass());
			if (value != null) {
				allParameters.put(parameter, value);
			} else {
				allParameters.remove(parameter);
			}
			return;
		}
		logger.warning("Please implement me, target=" + target + " context=" + context);
	}

}