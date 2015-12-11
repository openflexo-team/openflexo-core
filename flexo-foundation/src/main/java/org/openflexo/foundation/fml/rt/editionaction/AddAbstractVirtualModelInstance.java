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

package org.openflexo.foundation.fml.rt.editionaction;

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.rm.AbstractVirtualModelResource;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;

/**
 * This action is used to explicitely instanciate a new {@link AbstractVirtualModelInstance} in an other
 * {@link AbstractVirtualModelInstance} with some parameters
 * 
 * @author sylvain
 * 
 * @param <FCI>
 *            type of {@link AbstractVirtualModelInstance} beeing created by this action
 */

@ModelEntity
@ImplementationClass(AddAbstractVirtualModelInstance.AddAbstractVirtualModelInstanceImpl.class)
public interface AddAbstractVirtualModelInstance<FCI extends AbstractVirtualModelInstance<FCI, ?>>
		extends AbstractAddFlexoConceptInstance<FCI, View> {

	public AbstractVirtualModelResource<?> getVirtualModelType();

	public void setVirtualModelType(AbstractVirtualModelResource<?> resource);

	public static abstract class AddAbstractVirtualModelInstanceImpl<FCI extends AbstractVirtualModelInstance<FCI, ?>>
			extends AbstractAddFlexoConceptInstanceImpl<FCI, View>implements AddAbstractVirtualModelInstance<FCI> {

		static final Logger logger = Logger.getLogger(AddAbstractVirtualModelInstance.class.getPackage().getName());

		@Override
		public Class<View> getVirtualModelInstanceClass() {
			return View.class;
		}

		@Override
		public FCI execute(RunTimeEvaluationContext evaluationContext) {
			System.out.println("Now create a AbstractVirtualModelInstance");
			return super.execute(evaluationContext);
		}

		@Override
		public AbstractVirtualModelResource<?> getVirtualModelType() {
			if (getFlexoConceptType() instanceof AbstractVirtualModel) {
				return (AbstractVirtualModelResource<?>) ((AbstractVirtualModel<?>) getFlexoConceptType()).getResource();
			}
			return null;
		}

		@Override
		public void setVirtualModelType(AbstractVirtualModelResource<?> resource) {
			try {
				setFlexoConceptType(resource.getResourceData(null));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ResourceLoadingCancelledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FlexoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
