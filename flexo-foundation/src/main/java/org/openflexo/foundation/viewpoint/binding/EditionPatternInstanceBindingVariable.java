package org.openflexo.foundation.viewpoint.binding;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.FlexoConceptInstanceType;

public class EditionPatternInstanceBindingVariable extends BindingVariable {
	static final Logger logger = Logger.getLogger(EditionPatternInstanceBindingVariable.class.getPackage().getName());

	private FlexoConcept flexoConcept;
	private int index;

	public EditionPatternInstanceBindingVariable(FlexoConcept anFlexoConcept, int index) {
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