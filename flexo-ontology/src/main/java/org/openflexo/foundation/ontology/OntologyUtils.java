/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.foundation.ontology;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

public class OntologyUtils {

	private static final Logger logger = Logger.getLogger(OntologyUtils.class.getPackage().getName());

	public static <C extends IFlexoOntologyClass> C getMostSpecializedClass(Collection<C> someClasses) {

		if (someClasses.size() == 0) {
			return null;
		}
		if (someClasses.size() == 1) {
			return someClasses.iterator().next();
		}
		IFlexoOntologyClass<?>[] array = someClasses.toArray(new IFlexoOntologyClass[someClasses.size()]);

		for (int i = 0; i < someClasses.size(); i++) {
			for (int j = i + 1; j < someClasses.size(); j++) {
				IFlexoOntologyClass<?> c1 = array[i];
				IFlexoOntologyClass c2 = array[j];
				if (c1.isSuperClassOf(c2)) {
					someClasses.remove(c1);
					return getMostSpecializedClass(someClasses);
				}
				if (c2.isSuperClassOf(c1)) {
					someClasses.remove(c2);
					return getMostSpecializedClass(someClasses);
				}
			}
		}

		// No parent were found, take first item
		logger.warning("Undefined specializing criteria between " + someClasses);
		return someClasses.iterator().next();

	}

	public static <TA extends TechnologyAdapter<TA>, C extends IFlexoOntologyClass<TA>> IFlexoOntologyClass<TA> getFirstCommonAncestor(C c1,
			C c2) {
		Set<C> commonAncestors = new HashSet<>();
		Set<C> ancestors1 = (Set<C>) getAllSuperClasses(c1);
		ancestors1.add(c1);
		Set<C> ancestors2 = (Set<C>) getAllSuperClasses(c2);
		ancestors2.add(c2);
		for (C cl1 : ancestors1) {
			for (C cl2 : ancestors2) {
				if (cl1.equalsToConcept(cl2)) {
					commonAncestors.add(cl1);
				}
			}
		}
		return getMostSpecializedClass(commonAncestors);
	}

	/**
	 * Return all direct and inferred super classes of supplied class
	 * 
	 * @return
	 */
	public static <TA extends TechnologyAdapter<TA>> Set<IFlexoOntologyClass<TA>> getAllSuperClasses(IFlexoOntologyClass<TA> aClass) {
		Set<IFlexoOntologyClass<TA>> returned = new HashSet<>();
		for (IFlexoOntologyClass<TA> c : aClass.getSuperClasses()) {
			returned.add(c);
			returned.addAll(getAllSuperClasses(c));
		}
		return returned;
	}

	/**
	 * Return all direct and inferred super classes of supplied individual
	 * 
	 * @return
	 */
	public static Set<IFlexoOntologyClass<?>> getAllSuperClasses(IFlexoOntologyIndividual<?> anIndividual) {
		Set<IFlexoOntologyClass<?>> returned = new HashSet<>();
		for (IFlexoOntologyClass<?> c : anIndividual.getTypes()) {
			returned.add(c);
			returned.addAll(getAllSuperClasses(c));
		}
		return returned;
	}

	/**
	 * Return all direct and inferred imported ontology, including supplied ontology
	 * 
	 * @return
	 */
	public static <O extends IFlexoOntology<?>> Set<O> getAllImportedOntologies(O ontology) {
		Set<O> returned = new HashSet<>();
		appendImportedOntologies(ontology, returned);
		/*
		returned.add(ontology);
		// System.out.println("Add " + ontology + " still to import " + ontology.getImportedOntologies());
		if (ontology.getImportedOntologies() != null) {
			for (IFlexoOntology i : ontology.getImportedOntologies()) {
				if (i != ontology && !returned.contains(i)) { // Previous test to prevent loops in imports
					returned.addAll((Set<O>) getAllImportedOntologies(i));
				}
				// System.out.println("Add " + i);
				returned.add((O) i);
			}
		}*/
		return returned;
	}

	private static <O extends IFlexoOntology<?>> void appendImportedOntologies(O ontology, Set<O> s) {
		if (ontology == null) {
			return;
		}
		if (ontology.getImportedOntologies() != null) {
			for (IFlexoOntology<?> imported : ontology.getImportedOntologies()) {
				if (!s.contains(imported)) {
					s.add((O) imported);
					appendImportedOntologies(imported, (Set) s);
				}
			}
		}
	}

}
