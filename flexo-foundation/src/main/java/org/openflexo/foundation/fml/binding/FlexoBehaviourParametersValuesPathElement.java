package org.openflexo.foundation.fml.binding;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingEvaluationContext;
import org.openflexo.antar.binding.BindingPathElement;
import org.openflexo.antar.binding.SimplePathElement;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParametersValuesType;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;

public class FlexoBehaviourParametersValuesPathElement extends SimplePathElement {
	static final Logger logger = Logger.getLogger(FlexoBehaviourParametersValuesPathElement.class.getPackage().getName());

	private FlexoBehaviour flexoBehaviour;

	public FlexoBehaviourParametersValuesPathElement(BindingPathElement parent, FlexoBehaviour aFlexoBehaviour) {
		super(parent, "parameters", FlexoBehaviourParametersValuesType.getFlexoBehaviourParametersValuesType(aFlexoBehaviour));
		this.flexoBehaviour = aFlexoBehaviour;
	}

	public FlexoBehaviour getFlexoBehaviour() {
		return flexoBehaviour;
	}

	@Override
	public String getLabel() {
		return "parameters";
	}

	@Override
	public String getTooltipText(Type resultingType) {
		return "parameters";
	}

	@Override
	public Object getBindingValue(Object target, BindingEvaluationContext context) throws TypeMismatchException, NullReferenceException {
		if (target instanceof FlexoBehaviourAction) {
			return ((FlexoBehaviourAction) target).getParametersValues();
		}
		logger.warning("Please implement me, target=" + target + " of " + target.getClass() + " context=" + context);
		return null;
	}

	@Override
	public void setBindingValue(Object value, Object target, BindingEvaluationContext context) throws TypeMismatchException,
			NullReferenceException {
		logger.warning("Please implement me, target=" + target + " context=" + context);
	}

}