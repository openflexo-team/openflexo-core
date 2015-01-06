package org.openflexo.foundation.fml.binding;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingEvaluationContext;
import org.openflexo.antar.binding.BindingPathElement;
import org.openflexo.antar.binding.SimplePathElement;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParametersType;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;

public class FlexoBehaviourParametersDefinitionsPathElement extends SimplePathElement {
	static final Logger logger = Logger.getLogger(FlexoBehaviourParametersDefinitionsPathElement.class.getPackage().getName());

	private FlexoBehaviour flexoBehaviour;

	public FlexoBehaviourParametersDefinitionsPathElement(BindingPathElement parent, FlexoBehaviour aFlexoBehaviour) {
		super(parent, "parametersDefinitions", FlexoBehaviourParametersType.getFlexoBehaviourParametersType(aFlexoBehaviour));
		this.flexoBehaviour = aFlexoBehaviour;
	}

	public FlexoBehaviour getFlexoBehaviour() {
		return flexoBehaviour;
	}

	@Override
	public String getLabel() {
		return "parametersDefinitions";
	}

	@Override
	public String getTooltipText(Type resultingType) {
		return "parametersDefinitions";
	}

	@Override
	public Object getBindingValue(Object target, BindingEvaluationContext context) throws TypeMismatchException, NullReferenceException {
		if (target instanceof FlexoBehaviour) {
			return ((FlexoBehaviour) target).getParameters();
		}
		if (target instanceof FlexoBehaviourAction) {
			return ((FlexoBehaviourAction) target).getFlexoBehaviour().getParameters();
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