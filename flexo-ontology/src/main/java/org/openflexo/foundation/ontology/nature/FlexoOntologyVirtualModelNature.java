/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
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

package org.openflexo.foundation.ontology.nature;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelNature;
import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyDataProperty;
import org.openflexo.foundation.ontology.IFlexoOntologyIndividual;
import org.openflexo.foundation.ontology.IFlexoOntologyObject;
import org.openflexo.foundation.ontology.IFlexoOntologyObjectProperty;
import org.openflexo.foundation.ontology.IFlexoOntologyStructuralProperty;
import org.openflexo.foundation.ontology.technologyadapter.FlexoOntologyModelSlot;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;

/**
 * Define the "FlexoOntology" nature of a {@link VirtualModel}<br>
 * 
 * A {@link VirtualModel} with this nature has a least a {@link FlexoOntologyModelSlot}
 * 
 * @author sylvain
 * 
 */
public class FlexoOntologyVirtualModelNature implements VirtualModelNature {

	public static FlexoOntologyVirtualModelNature INSTANCE = new FlexoOntologyVirtualModelNature();

	// Prevent external instantiation
	protected FlexoOntologyVirtualModelNature() {
	}

	/**
	 * Return boolean indicating if supplied {@link VirtualModel} has at least a {@link FlexoOntologyModelSlot}
	 */
	@Override
	public boolean hasNature(VirtualModel virtualModel) {

		if (virtualModel == null) {
			return false;
		}

		// VirtualModel should have one and only one TypedDiagramModelSlot
		if (virtualModel.getModelSlots(FlexoOntologyModelSlot.class).size() > 0) {
			return true;
		}

		return false;
	}

	public static List<? extends FlexoOntologyModelSlot<?, ?, ?>> getFlexoOntologyModelSlots(VirtualModel virtualModel) {
		return INSTANCE._getFlexoOntologyModelSlots(virtualModel);
	}

	private static List<? extends FlexoOntologyModelSlot<?, ?, ?>> _getFlexoOntologyModelSlots(VirtualModel virtualModel) {
		if (virtualModel != null && virtualModel.getModelSlots(FlexoOntologyModelSlot.class).size() > 0) {
			return (List) virtualModel.getModelSlots(FlexoOntologyModelSlot.class);
		}
		return null;
	}

	/**
	 * Return the list of all metamodels used in the scope of this virtual model
	 * 
	 * @return a {@link Set} of {@link IFlexoOntology}
	 */
	public static Set<IFlexoOntology<?>> getAllReferencedMetaModels(VirtualModel virtualModel) {
		HashSet<IFlexoOntology<?>> returned = new HashSet<>();
		List<? extends FlexoOntologyModelSlot<?, ?, ?>> flexoOntologyModelSlots = getFlexoOntologyModelSlots(virtualModel);
		if (flexoOntologyModelSlots != null) {
			for (FlexoOntologyModelSlot<?, ?, ?> modelSlot : getFlexoOntologyModelSlots(virtualModel)) {
				if (modelSlot.getMetaModelResource() != null) {
					returned.add(modelSlot.getMetaModelResource().getMetaModelData());
				}
			}
		}
		return returned;
	}

	/**
	 * Retrieve object referenced by its URI, asserting that supplied {@link VirtualModel} has the
	 * {@link FlexoOntologyVirtualModelNature}<br>
	 * 
	 * @param uri
	 * @param virtualModel
	 *            the {@link VirtualModel} used to define search scope
	 * @return
	 */
	public static Object getObject(String uri, VirtualModel virtualModel) {
		for (IFlexoOntology<?> mm : getAllReferencedMetaModels(virtualModel)) {
			if (mm instanceof FlexoMetaModel) {
				Object o = ((FlexoMetaModel) mm).getObject(uri);
				if (o != null) {
					return o;
				}
			}
		}
		return null;
	}

	/**
	 * Retrieve {@link IFlexoOntologyObject} referenced by its URI, asserting that supplied {@link VirtualModel} has the
	 * {@link FlexoOntologyVirtualModelNature}<br>
	 * 
	 * @param uri
	 * @param virtualModel
	 *            the {@link VirtualModel} used to define search scope
	 * @return
	 */
	public static IFlexoOntologyObject<?> getOntologyObject(String uri, VirtualModel virtualModel) {
		Object returned = getObject(uri, virtualModel);
		if (returned instanceof IFlexoOntologyObject) {
			return (IFlexoOntologyObject<?>) returned;
		}
		return null;
	}

	/**
	 * Retrieve {@link IFlexoOntologyClass} referenced by its URI, asserting that supplied {@link VirtualModel} has the
	 * {@link FlexoOntologyVirtualModelNature}<br>
	 * 
	 * @param uri
	 * @param virtualModel
	 *            the {@link VirtualModel} used to define search scope
	 * @return
	 */
	public static IFlexoOntologyClass<?> getOntologyClass(String uri, VirtualModel virtualModel) {
		Object returned = getOntologyObject(uri, virtualModel);
		if (returned instanceof IFlexoOntologyClass) {
			return (IFlexoOntologyClass<?>) returned;
		}
		return null;
	}

	/**
	 * Retrieve {@link IFlexoOntologyIndividual} referenced by its URI, asserting that supplied {@link VirtualModel} has the
	 * {@link FlexoOntologyVirtualModelNature}<br>
	 * 
	 * @param uri
	 * @param virtualModel
	 *            the {@link VirtualModel} used to define search scope
	 * @return
	 */
	public static IFlexoOntologyIndividual<?> getOntologyIndividual(String uri, VirtualModel virtualModel) {
		Object returned = getOntologyObject(uri, virtualModel);
		if (returned instanceof IFlexoOntologyIndividual) {
			return (IFlexoOntologyIndividual<?>) returned;
		}
		return null;
	}

	/**
	 * Retrieve {@link IFlexoOntologyStructuralProperty} referenced by its URI, asserting that supplied {@link VirtualModel} has the
	 * {@link FlexoOntologyVirtualModelNature}<br>
	 * 
	 * @param uri
	 * @param virtualModel
	 *            the {@link VirtualModel} used to define search scope
	 * @return
	 */
	public static IFlexoOntologyStructuralProperty<?> getOntologyProperty(String uri, VirtualModel virtualModel) {
		Object returned = getOntologyObject(uri, virtualModel);
		if (returned instanceof IFlexoOntologyStructuralProperty) {
			return (IFlexoOntologyStructuralProperty<?>) returned;
		}
		return null;
	}

	/**
	 * Retrieve {@link IFlexoOntologyObjectProperty} referenced by its URI, asserting that supplied {@link VirtualModel} has the
	 * {@link FlexoOntologyVirtualModelNature}<br>
	 * 
	 * @param uri
	 * @param virtualModel
	 *            the {@link VirtualModel} used to define search scope
	 * @return
	 */
	public static IFlexoOntologyObjectProperty<?> getOntologyObjectProperty(String uri, VirtualModel virtualModel) {
		Object returned = getOntologyObject(uri, virtualModel);
		if (returned instanceof IFlexoOntologyObjectProperty) {
			return (IFlexoOntologyObjectProperty<?>) returned;
		}
		return null;
	}

	/**
	 * Retrieve {@link IFlexoOntologyDataProperty} referenced by its URI, asserting that supplied {@link VirtualModel} has the
	 * {@link FlexoOntologyVirtualModelNature}<br>
	 * 
	 * @param uri
	 * @param virtualModel
	 *            the {@link VirtualModel} used to define search scope
	 * @return
	 */
	public static IFlexoOntologyDataProperty<?> getOntologyDataProperty(String uri, VirtualModel virtualModel) {
		Object returned = getOntologyObject(uri, virtualModel);
		if (returned instanceof IFlexoOntologyDataProperty) {
			return (IFlexoOntologyDataProperty<?>) returned;
		}
		return null;
	}

}
