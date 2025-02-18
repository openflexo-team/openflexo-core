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

package org.openflexo.foundation.fml.rt;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.DeclareActorReferences;
import org.openflexo.foundation.fml.annotations.DeclareFetchRequests;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectVirtualModelInstance;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * A {@link ModelSlot} provided by an alternative technology which allows to access data through a general {@link VirtualModel} contract
 * (data are reflected as instances of {@link FlexoConcept}) through a technology-specific resource<br>
 * 
 * 
 * Accessed resource data is a {@link VirtualModelInstance} (generally subclassed for a given technology).
 * 
 * @author sylvain
 *
 * @param <VMI>
 *            type of {@link VirtualModelInstance} presented by this model slot
 * @param <R>
 *            type of resource beeing interpreted as an instance of a FML {@link VirtualModel}
 * @param <TA>
 *            technology providing this model slot
 */
@DeclareFetchRequests({ SelectFlexoConceptInstance.class, SelectVirtualModelInstance.class })
@DeclareActorReferences({ ReflectedFMLRTModelSlotInstance.class }) // TODO : this declaration is not taken under account
@ModelEntity(isAbstract = true)
@ImplementationClass(ReflectedFMLRTModelSlot.ReflectedFMLRTModelSlotImpl.class)
public interface ReflectedFMLRTModelSlot<VMI extends ReflectedVirtualModelInstance<VMI, R, TA>, R extends TechnologyAdapterResource<?, TA>, TA extends TechnologyAdapter<TA>>
		extends AbstractFMLRTModelSlot<VMI, TA> {

	public static abstract class ReflectedFMLRTModelSlotImpl<VMI extends ReflectedVirtualModelInstance<VMI, R, TA>, R extends TechnologyAdapterResource<?, TA>, TA extends TechnologyAdapter<TA>>
			extends AbstractFMLRTModelSlotImpl<VMI, TA> implements ReflectedFMLRTModelSlot<VMI, R, TA> {

		private static final Logger logger = Logger.getLogger(ReflectedFMLRTModelSlot.class.getPackage().getName());

		@Override
		public ReflectedFMLRTModelSlotInstance<VMI, R, TA> makeActorReference(VMI object, FlexoConceptInstance fci) {
			AbstractVirtualModelInstanceModelFactory<?> factory = fci.getFactory();
			ReflectedFMLRTModelSlotInstance<VMI, R, TA> returned = factory.newInstance(ReflectedFMLRTModelSlotInstance.class);
			returned.setModelSlot(this);
			returned.setFlexoConceptInstance(fci);
			returned.setReflectedResource(object.getReflectedResource());
			return returned;
		}

		/*@Override
		public FreeModelSlotInstance<?, RD> connectTo(FlexoResource<?> resource, FlexoConceptInstance context) {
			FreeModelSlotInstance<?, RD> modelSlotInstance;
			try {
				modelSlotInstance = makeActorReference((RD) resource.getResourceData(), context);
				context.addToActors(modelSlotInstance);
				return modelSlotInstance;
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
			return null;
		}*/

		@Override
		public ReflectedFMLRTModelSlotInstance<VMI, R, TA> connectTo(FlexoResource<?> resource, FlexoConceptInstance context) {
			System.out.println("Alors la ca rigole moins on dirait, hein ????");
			System.exit(-1);
			return null;
		}

	}

}
