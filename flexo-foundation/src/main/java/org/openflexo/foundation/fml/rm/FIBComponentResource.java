/**
 * 
 * Copyright (c) 2014-2026, Openflexo
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

package org.openflexo.foundation.fml.rm;

import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * The resource of a GINA component (<code>.fib</code> or <code>.inspector</code>) belonging to the platform.
 *
 * <p>
 * A component stored in the <code>Xxx.fml/</code> container of a VirtualModel is a first-class resource, exactly as a <code>.fml.rt</code>
 * stored there is: {@link FIBComponentResourceFactory} registers it and links it back into the {@link CompilationUnitResource} of that
 * container, so it shows up under its VirtualModel in the browsers and is deleted with it.
 *
 * <p>
 * This replaces <code>gina-ta</code>'s <code>GINAFIBComponentResource</code>. The file behind it is a <b>plain</b> GINA component - see
 * {@link FMLFIBComponent} for why a holder still stands between the resource and the {@link FIBComponent}.
 *
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(FIBComponentResourceImpl.class)
public interface FIBComponentResource extends TechnologyAdapterResource<FMLFIBComponent, FMLTechnologyAdapter> {

	/**
	 * The component this resource describes, loading the resource when required, or null when it cannot be loaded.
	 */
	public FIBComponent getComponent();

	/**
	 * The {@link FlexoConcept} that drives this component, i.e. the one the container convention resolves to it, or null when this
	 * component is a fragment nothing names.
	 *
	 * <p>
	 * This is the reverse of {@link FlexoConcept#getUIComponentResource()}, and it is what gives the component its typing space: the
	 * <code>data</code> it is shown with is an instance of that concept, not a bare FlexoConceptInstance.
	 */
	public FlexoConcept getDrivingConcept();
}
