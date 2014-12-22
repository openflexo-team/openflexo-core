package org.openflexo.foundation.fml.binding;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;

public class FlexoConceptInstanceBindingVariable extends BindingVariable {
	static final Logger logger = Logger.getLogger(FlexoConceptInstanceBindingVariable.class.getPackage().getName());

	private FlexoConcept flexoConcept;
	private int index;

	public FlexoConceptInstanceBindingVariable(FlexoConcept anFlexoConcept, int index) {
		super(anFlexoConcept.getVirtualModel().getName() + "_" + anFlexoConcept.getName() + "_" + index, FlexoConceptInstanceType
				.getFlexoConceptInstanceType(anFlexoConcept));
		this.flexoConcept = anFlexoConcept;
	}

	@Override
	public Type getType() {
		return FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept);
	}

	@Override
	public String getTooltipText(Type resultingType) {
		return flexoConcept.getDescription();
	}

	public FlexoConcept getFlexoConcept() {
		return flexoConcept;
	}

	public int getIndex() {
		return index;
	}
}