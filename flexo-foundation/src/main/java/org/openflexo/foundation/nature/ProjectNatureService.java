/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.nature;

import java.util.List;

import org.openflexo.foundation.FlexoService;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Getter.Cardinality;

/**
 * This service provides management layer for {@link ProjectNature}<br>
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(DefaultProjectNatureService.class)
public interface ProjectNatureService extends FlexoService {
	public static final String PROJECT_NATURES = "projectNatures";

	@Getter(value = PROJECT_NATURES, cardinality = Cardinality.LIST, ignoreType = true)
	public List<ProjectNatureFactory<?>> getProjectNatureFactories();

	@Setter(PROJECT_NATURES)
	public void setProjectNatureFactories(List<ProjectNatureFactory<?>> projectNatureFactories);

	@Adder(PROJECT_NATURES)
	public void addToProjectNatureFactories(ProjectNatureFactory<?> projectNatureFactory);

	@Remover(PROJECT_NATURES)
	public void removeFromProjectNatureFactories(ProjectNatureFactory<?> projectNatureFactory);

	/**
	 * Return project nature mapping supplied class<br>
	 * 
	 * @param projectNatureClass
	 * @return
	 */
	public <F extends ProjectNatureFactory<N>, N extends ProjectNature<N>> F getProjectNatureFactory(Class<N> projectNatureClass);

	/**
	 * Return project nature mapping supplied class<br>
	 * 
	 * @param projectNatureClass
	 * @return
	 */
	public ProjectNatureFactory<?> getProjectNatureFactory(String projectNatureClassName);

}
