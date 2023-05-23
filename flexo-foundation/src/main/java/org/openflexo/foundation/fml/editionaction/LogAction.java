/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.logging.FMLConsole;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * Primitive used to display a log in FML virtual machine at run-time
 * 
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(LogAction.LogActionImpl.class)
@XMLElement
public interface LogAction extends EditionAction {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String LOG_STRING_KEY = "logString";
	@PropertyIdentifier(type = FMLConsole.LogLevel.class)
	public static final String LOG_LEVEL_KEY = "logLevel";

	@Getter(value = LOG_STRING_KEY)
	@XMLAttribute
	public DataBinding<String> getLogString();

	@Setter(LOG_STRING_KEY)
	public void setLogString(DataBinding<String> object);

	@Getter(value = LOG_LEVEL_KEY)
	@XMLAttribute
	public FMLConsole.LogLevel getLogLevel();

	@Setter(LOG_LEVEL_KEY)
	public void setLogLevel(FMLConsole.LogLevel logLevel);

	public static abstract class LogActionImpl extends EditionActionImpl implements LogAction {

		private static final Logger logger = Logger.getLogger(LogAction.class.getPackage().getName());

		private DataBinding<String> logString;
		private FMLConsole.LogLevel logLevel;

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + "log " + getLogString();
		}

		@Override
		public DataBinding<String> getLogString() {
			if (logString == null) {
				logString = new DataBinding<>(this, String.class, BindingDefinitionType.GET);
				logString.setBindingName("logString");
			}
			return logString;
		}

		@Override
		public void setLogString(DataBinding<String> logString) {
			if (logString != null) {
				logString.setOwner(this);
				logString.setBindingName("logString");
				logString.setDeclaredType(String.class);
				logString.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.logString = logString;
		}

		@Override
		public FMLConsole.LogLevel getLogLevel() {
			FMLConsole.LogLevel returned = (FMLConsole.LogLevel) performSuperGetter(LOG_LEVEL_KEY);
			if (returned == null) {
				return FMLConsole.LogLevel.INFO;
			}
			return returned;
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {
			String logString = null;
			try {
				logString = getLogString().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e1) {
				e1.printStackTrace();
			} catch (NullReferenceException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				throw new FMLExecutionException(e1.getCause());
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}

			evaluationContext.logOut(logString, getLogLevel());

			/*if (evaluationContext.getEditor() != null && evaluationContext.getEditor().getFMLConsole() != null) {
			
				if (getRootOwner() instanceof FlexoBehaviour) {
					evaluationContext.getEditor().getFMLConsole().log(logString, getLogLevel(), evaluationContext.getFlexoConceptInstance(),
							(FlexoBehaviour) getRootOwner());
				}
				else {
					evaluationContext.getEditor().getFMLConsole().log(logString, getLogLevel(), evaluationContext.getFlexoConceptInstance(),
							null);
				}
			}
			
			else {
				logger.warning("Cannot access FML console");
				System.out.println(logString);
			}*/
			return null;
		}

		@Override
		public Type getInferedType() {
			return Void.class;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getLogString().rebuild();
		}

	}

	@DefineValidationRule
	public static class LogStringBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<LogAction> {
		public LogStringBindingIsRequiredAndMustBeValid() {
			super("'log_string'_binding_is_not_valid", LogAction.class);
		}

		@Override
		public DataBinding<?> getBinding(LogAction object) {
			return object.getLogString();
		}

	}

}
