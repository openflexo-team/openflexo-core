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

package org.openflexo.foundation.fml;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class provinding usefull methods in FML context
 * 
 * @author sylvain
 *
 */
public class FMLUtils {

	public FlexoConcept getMostSpecializedConcept(FlexoConcept concept1, FlexoConcept concept2) {
		List<FlexoConcept> l = new ArrayList<>();
		l.add(concept1);
		l.add(concept2);
		return getMostSpecializedConcept(l);
	}

	public static FlexoConcept getMostSpecializedConcept(List<FlexoConcept> someConcepts) {

		if (someConcepts.size() == 0) {
			return null;
		}
		if (someConcepts.size() == 1) {
			return someConcepts.iterator().next();
		}

		FlexoConcept[] array = someConcepts.toArray(new FlexoConcept[someConcepts.size()]);

		for (int i = 0; i < someConcepts.size(); i++) {
			for (int j = i + 1; j < someConcepts.size(); j++) {
				FlexoConcept c1 = array[i];
				FlexoConcept c2 = array[j];
				if (c1.isAssignableFrom(c2)) {
					someConcepts.remove(c1);
					return getMostSpecializedConcept(someConcepts);
				}
				if (c2.isAssignableFrom(c1)) {
					someConcepts.remove(c2);
					return getMostSpecializedConcept(someConcepts);
				}
			}
		}

		return someConcepts.iterator().next();

	}

	public static FlexoConcept getMostSpecializedAncestor(FlexoConcept concept1, FlexoConcept concept2) {
		if (concept1 == null || concept2 == null) {
			return null;
		}
		if (concept1 == concept2) {
			return concept1;
		}
		if (concept1.getParentFlexoConcepts().size() == 0 && concept2.getParentFlexoConcepts().size() == 0) {
			// nothing in common
			return null;
		}
		if (concept1.isAssignableFrom(concept2)) {
			return concept1;
		}
		if (concept2.isAssignableFrom(concept1)) {
			return concept2;
		}
		FlexoConcept pivot = null;
		FlexoConcept iterated = null;
		if (concept1.getParentFlexoConcepts().size() > 0) {
			pivot = concept1;
			iterated = concept2;
		}
		else {
			pivot = concept2;
			iterated = concept1;
		}
		for (FlexoConcept parent : pivot.getParentFlexoConcepts()) {
			if (parent.isAssignableFrom(iterated)) {
				return parent;
			}
		}
		for (FlexoConcept parent : pivot.getParentFlexoConcepts()) {
			FlexoConcept returned = getMostSpecializedAncestor(parent, iterated);
			if (returned != null) {
				return returned;
			}
		}
		return null;

	}

}
