/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.UnresolvedType;
import org.openflexo.foundation.FlexoServiceManager;

/**
 * FML typing space, related to a {@link FMLCompilationUnit}
 * 
 * Support FMLCompilationUnit context and imports semantics
 * 
 * @author sylvain
 *
 */
public class FMLTypingSpace extends AbstractFMLTypingSpace {

	private final FMLCompilationUnit compilationUnit;

	private List<CustomType> typesToResolve = new ArrayList<>();

	public FMLTypingSpace(FMLCompilationUnit compilationUnit) {
		super(compilationUnit.getServiceManager());
		this.compilationUnit = compilationUnit;
	}

	public FMLTypingSpace(FlexoServiceManager serviceManager) {
		super(serviceManager);
		compilationUnit = null;
	}

	public FMLCompilationUnit getFMLCompilationUnit() {
		return compilationUnit;
	}

	/**
	 * Return boolean indicating if supplied {@link Type} is actually in current typing space
	 * 
	 * @param type
	 * @return
	 */
	@Override
	public boolean isTypeImported(Type type) {
		return super.isTypeImported(type);
	}

	/**
	 * Import supplied type in this typing space
	 * 
	 * @param type
	 */
	@Override
	public void importType(Type type) {
		super.importType(type);
	}

	/**
	 * Resolve {@link Type} according to current typing space using supplied type {@link String} representation
	 * 
	 * @param typeAsString
	 * @return
	 */
	@Override
	public Type resolveType(String typeAsString) {
		Type returned = super.resolveType(typeAsString);
		if (returned instanceof UnresolvedType) {
			if (compilationUnit != null) {
				// Try to look up a FlexoConcept
				FlexoConcept lookedUpConcept = compilationUnit.lookupFlexoConceptWithName(typeAsString);
				if (lookedUpConcept != null) {
					// Yes ! a concept was found
					return lookedUpConcept.getInstanceType();
				}
				returned = compilationUnit.lookupClassInUseDeclarations(typeAsString);
				if (returned != null) {
					return returned;
				}
			}
		}
		return returned;

	}

	@Override
	public String toString() {
		return "FMLTypingSpace";
	}

	public void addToTypesToResolve(CustomType typeToResolve) {
		typesToResolve.add(typeToResolve);
	}

	public void resolveUnresolvedTypes() {
		for (CustomType typeToResolve : typesToResolve) {
			typeToResolve.resolve();
		}
		typesToResolve.clear();
	}
}
