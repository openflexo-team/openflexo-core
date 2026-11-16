/**
 * 
 * Copyright (c) 2026, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.binding;

import java.lang.reflect.Type;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.SimplePathElementImpl;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.rt.FlexoExceptionInstance;
import org.openflexo.foundation.fml.rt.error.FMLErrorReporter;
import org.openflexo.foundation.fml.rt.error.FMLUserError;
import org.openflexo.localization.FlexoLocalization;

/**
 * The members {@code message}, {@code kind} and {@code fmlStackTrace} of an instance of an FML exception (CORE-F-11 phase 7), as a caught
 * built-in error has them (its Java getters). Not settable. An exception never thrown has its message, the name of its exception as kind,
 * and an empty stack.
 */
public class ExceptionInfoPathElement extends SimplePathElementImpl<FMLNativeProperty> {

	public enum Info {
		MESSAGE("message"), KIND("kind"), FML_STACK_TRACE("fmlStackTrace");

		private final String name;
		private final FMLNativeProperty property;

		Info(String name) {
			this.name = name;
			this.property = new FMLNativeProperty(name, String.class);
		}

		public String getName() {
			return name;
		}
	}

	private final Info info;

	public ExceptionInfoPathElement(IBindingPathElement parent, Info info, Bindable bindable) {
		super(parent, info.name, String.class, bindable);
		this.info = info;
		setProperty(info.property);
	}

	public Info getInfo() {
		return info;
	}

	@Override
	public String getLabel() {
		return getPropertyName();
	}

	@Override
	public String getTooltipText(Type resultingType) {
		return FlexoLocalization.getMainLocalizer().localizedForKey("exception_" + info.name);
	}

	@Override
	public boolean isSettable() {
		return false;
	}

	@Override
	public Object getBindingValue(Object target, BindingEvaluationContext context) throws TypeMismatchException, NullReferenceException {
		if (!(target instanceof FlexoExceptionInstance)) {
			return null;
		}
		FlexoExceptionInstance exception = (FlexoExceptionInstance) target;
		FMLUserError error = exception.getThrownError();
		switch (info) {
			case MESSAGE:
				return error != null ? error.getMessage() : exception.getExceptionMessage();
			case KIND:
				return error != null ? FMLErrorReporter.kind(error)
						: exception.getFlexoConcept() != null ? exception.getFlexoConcept().getName() : null;
			case FML_STACK_TRACE:
				return error != null ? error.getFmlStackTrace() : "";
			default:
				return null;
		}
	}

	@Override
	public void setBindingValue(Object value, Object target, BindingEvaluationContext context)
			throws TypeMismatchException, NullReferenceException {
		// Not settable
	}

	@Override
	public boolean isNotificationSafe() {
		return false;
	}

	@Override
	public boolean isResolved() {
		return true;
	}

	@Override
	public void resolve() {
		// Not applicable
	}
}
