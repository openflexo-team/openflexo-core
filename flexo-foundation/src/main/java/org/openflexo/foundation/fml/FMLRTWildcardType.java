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

import java.util.ArrayList;
import java.util.List;

import org.openflexo.connie.type.TypingSpace;
import org.openflexo.connie.type.WildcardTypeImpl;

public class FMLRTWildcardType extends WildcardTypeImpl<FMLRTType> implements FMLRTType {

	public static FMLRTWildcardType makeUpperBoundWilcard(FMLRTType upperBound) {
		FMLRTWildcardType returned = new FMLRTWildcardType();
		returned.addUpperBound(upperBound);
		return returned;
	}

	public static FMLRTWildcardType makeLowerBoundWilcard(FMLRTType lowerBound) {
		FMLRTWildcardType returned = new FMLRTWildcardType();
		returned.addLowerBound(lowerBound);
		return returned;
	}

	public FMLRTWildcardType() {
		super();
	}

	public FMLRTWildcardType(List<FMLRTType> upperBounds, List<FMLRTType> lowerBounds) {
		super(upperBounds, lowerBounds);
	}

	public FMLRTWildcardType(FMLRTType[] upperBounds, FMLRTType[] lowerBounds) {
		super(upperBounds, lowerBounds);
	}

	@Override
	public Class<FMLRTType> getTypeClass() {
		return FMLRTType.class;
	}

	@Override
	public FMLRTWildcardType translateTo(TypingSpace typingSpace) {
		if (hasConnieTypeArguments()) {
			List<FMLRTType> newUpper = new ArrayList<>();
			for (FMLRTType t : getUpperBounds()) {
				newUpper.add(t.translateTo(typingSpace));
			}
			List<FMLRTType> newLower = new ArrayList<>();
			for (FMLRTType t : getLowerBounds()) {
				newLower.add(t.translateTo(typingSpace));
			}
			return new FMLRTWildcardType(newUpper, newLower);
		}
		return this;
	}
}
