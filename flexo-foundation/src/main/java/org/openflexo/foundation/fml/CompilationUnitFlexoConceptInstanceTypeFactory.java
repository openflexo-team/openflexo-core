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
package org.openflexo.foundation.fml;

import org.openflexo.foundation.fml.FlexoConceptInstanceType.FlexoConceptInstanceTypeFactory;

public class CompilationUnitFlexoConceptInstanceTypeFactory extends
TechnologyAdapterTypeFactory<FlexoConceptInstanceType, FMLTechnologyAdapter> implements FlexoConceptInstanceTypeFactory {

	private FMLCompilationUnit compilationUnit;
	
	@Override
	public Class<FlexoConceptInstanceType> getCustomType() {
		return FlexoConceptInstanceType.class;
	}

	public CompilationUnitFlexoConceptInstanceTypeFactory(FMLCompilationUnit compilationUnit) {
		super(null);
		this.compilationUnit = compilationUnit;
	}

	@Override
	public FlexoConceptInstanceType makeCustomType(String configuration) {

		if ("null".equals(configuration)) {
			configuration = null;
		}

		FlexoConcept concept = null;

		if (configuration != null) {
			concept = compilationUnit.getVirtualModel().getFlexoConcept(configuration);
		}

		if (concept != null)
			return concept.getInstanceType();
		// We don't return UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE because we want here a mutable type
		// if FlexoConcept might be resolved later
		return new FlexoConceptInstanceType(configuration, this);
	}

	@Override
	public void configureFactory(FlexoConceptInstanceType type) {
	}

	@Override
	public FlexoConcept resolveFlexoConcept(FlexoConceptInstanceType typeToResolve) {
		return compilationUnit.getVirtualModel().getFlexoConcept(typeToResolve.conceptURI);
	}

}
