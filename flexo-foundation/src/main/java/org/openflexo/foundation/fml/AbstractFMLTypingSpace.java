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
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.java.JavaTypingSpace;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.connie.type.UnresolvedType;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.rt.action.MatchingSet;
import org.openflexo.foundation.fml.ta.FlexoConceptType;
import org.openflexo.foundation.resource.ResourceData;

/**
 * FML typing space, build on top of {@link JavaTypingSpace}
 * 
 * Support import of VirtualModels
 * 
 * @author sylvain
 *
 */
public abstract class AbstractFMLTypingSpace extends JavaTypingSpace {

	private static final Logger logger = Logger.getLogger(AbstractFMLTypingSpace.class.getPackage().getName());

	public static final String CONCEPT = "Concept";
	public static final String CONCEPT_INSTANCE = "ConceptInstance";
	public static final String ENUM_INSTANCE = "EnumInstance";
	public static final String MODEL_INSTANCE = "ModelInstance";
	public static final String MATCHING_SET = "MatchingSet";
	public static final String RESOURCE = "Resource";

	private FlexoServiceManager serviceManager;

	public AbstractFMLTypingSpace(FlexoServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	public FlexoServiceManager getServiceManager() {
		return serviceManager;
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
		if (typeAsString == null) {
			return null;
		}
		if (typeAsString.equals(CONCEPT_INSTANCE)) {
			return FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE;
		}
		if (typeAsString.equals(MATCHING_SET)) {
			return MatchingSet.class;
		}
		if (getFMLCompilationUnit() != null) {
			for (TypeDeclaration typeDeclaration : getFMLCompilationUnit().getTypeDeclarations()) {
				if (typeAsString.equals(typeDeclaration.getAbbrev())) {
					return typeDeclaration.getReferencedType();
				}
			}
		}
		return super.resolveType(typeAsString);
	}

	/**
	 * Attempt to resolve unresolved parameterized types
	 * 
	 * @param baseType
	 * @param typeArguments
	 * @return
	 */
	public Type attemptToResolveType(UnresolvedType unresolvedBaseType, List<Type> typeArguments) {
		if (unresolvedBaseType.getUnresolvedTypeName().equals(RESOURCE) && typeArguments.size() == 1) {
			if (typeArguments.get(0) instanceof Class && ResourceData.class.isAssignableFrom((Class) typeArguments.get(0))) {
				// This matches a Resource<..> signature
				return getServiceManager().getTechnologyAdapterService().getResourceTypeForDataClass((Class<?>) typeArguments.get(0));
			}
			else {
				logger.warning("Not handled: " + typeArguments.get(0));
			}

		}
		if (unresolvedBaseType.getUnresolvedTypeName().equals(CONCEPT) && typeArguments.size() == 1
				&& typeArguments.get(0) instanceof FMLRTType) {
			// This matches a FlexoConceptType signature
			return new FlexoConceptType((FMLRTType) typeArguments.get(0));
		}
		if (unresolvedBaseType.getUnresolvedTypeName().equals(CONCEPT) && typeArguments.size() == 1
				&& typeArguments.get(0) instanceof WildcardType) {
			// This matches a FlexoConceptType signature
			return new FlexoConceptType(FMLRTWildcardType.fromWilcard((WildcardType) typeArguments.get(0)));
		}
		return new ParameterizedTypeImpl(unresolvedBaseType, typeArguments.toArray(new Type[typeArguments.size()]));
	}

	public abstract FMLCompilationUnit getFMLCompilationUnit();

}
