package org.openflexo.foundation.fml.expr;

import java.util.List;

import org.openflexo.connie.expr.BindingValue.NewInstanceBindingPathElement;
import org.openflexo.connie.expr.Expression;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;

public class NewVirtualModelInstanceBindingPathElement extends NewInstanceBindingPathElement {

	public NewVirtualModelInstanceBindingPathElement(VirtualModelInstanceType aType, String creationSchemeName, List<Expression> someArgs) {
		super(aType, creationSchemeName, someArgs);
	}

	@Override
	public VirtualModelInstanceType getType() {
		return (VirtualModelInstanceType) super.getType();
	}

	public VirtualModel getVirtualModel() {
		return getType().getVirtualModel();
	}

	@Override
	public String toString() {
		return "NewVirtualModelInstance[" + type + "(" + args + ")" + "]";
	}

	@Override
	public String getSerializationRepresentation() {
		StringBuffer sb = new StringBuffer();
		sb.append("new " + TypeUtils.simpleRepresentation(type) + "(");
		boolean isFirst = true;
		for (Expression arg : args) {
			sb.append((isFirst ? "" : ",") + arg);
			isFirst = false;
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

}
