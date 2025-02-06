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
import org.openflexo.foundation.fml.validation.BindingIsRequiredAndMustBeValid;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * Primitive used to display a log in FML virtual machine at run-time
 * 
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(ConnectAction.ConnectActionImpl.class)
@XMLElement
public interface ConnectAction<RD extends ResourceData<RD>, R extends FlexoResource<?>> extends AssignableAction<RD> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONNECT_KEY = "connect";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String USING_KEY = "using";

	@Getter(value = CONNECT_KEY)
	public DataBinding<RD> getConnect();

	@Setter(CONNECT_KEY)
	public void setConnect(DataBinding<RD> connect);

	@Getter(value = USING_KEY)
	public DataBinding<R> getUsing();

	@Setter(USING_KEY)
	public void setUsing(DataBinding<R> using);

	public static abstract class ConnectActionImpl<RD extends ResourceData<RD>, R extends FlexoResource<?>> extends AssignableActionImpl<RD>
			implements ConnectAction<RD, R> {

		private static final Logger logger = Logger.getLogger(ConnectAction.class.getPackage().getName());

		private DataBinding<RD> connect;
		private DataBinding<R> using;

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + "connect " + getConnect();
		}

		@Override
		public DataBinding<RD> getConnect() {
			if (connect == null) {
				connect = new DataBinding<>(this, ResourceData.class, BindingDefinitionType.GET);
				connect.setBindingName("connect");
			}
			return connect;
		}

		@Override
		public void setConnect(DataBinding<RD> connect) {
			if (connect != null) {
				connect.setOwner(this);
				connect.setBindingName("connect");
				connect.setDeclaredType(ResourceData.class);
				connect.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.connect = connect;
		}

		@Override
		public DataBinding<R> getUsing() {
			if (using == null) {
				using = new DataBinding<>(this, FlexoResource.class, BindingDefinitionType.GET);
				using.setBindingName("using");
			}
			return using;
		}

		@Override
		public void setUsing(DataBinding<R> using) {
			if (using != null) {
				using.setOwner(this);
				using.setBindingName("using");
				using.setDeclaredType(FlexoResource.class);
				using.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.using = using;
		}

		@Override
		public RD execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {
			R resource = null;
			try {
				resource = getUsing().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e1) {
				e1.printStackTrace();
			} catch (NullReferenceException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				throw new FMLExecutionException(e1.getCause());
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}

			System.out.println("Hop, il faudrait connecter la resource " + resource);

			return null;
		}

		@Override
		public Type getInferedType() {
			if (getConnect() != null && getConnect().isValid()) {
				return getConnect().getAnalyzedType();
			}
			return ResourceData.class;
		}

		@Override
		public Type getAssignableType() {
			return getInferedType();
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getConnect().rebuild();
			getUsing().rebuild();
		}

	}

	@DefineValidationRule
	public static class ConnectBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<ConnectAction> {
		public ConnectBindingIsRequiredAndMustBeValid() {
			super("'connect'_binding_is_not_valid", ConnectAction.class);
		}

		@Override
		public DataBinding<?> getBinding(ConnectAction object) {
			return object.getConnect();
		}

	}

	@DefineValidationRule
	public static class UsingBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<ConnectAction> {
		public UsingBindingIsRequiredAndMustBeValid() {
			super("'using'_binding_is_not_valid", ConnectAction.class);
		}

		@Override
		public DataBinding<?> getBinding(ConnectAction object) {
			return object.getUsing();
		}

	}

}
