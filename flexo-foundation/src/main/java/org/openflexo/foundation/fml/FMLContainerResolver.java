/**
 * 
 * Copyright (c) 2026, Openflexo
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

import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;

/**
 * Resolution of the container of a {@code new} (CORE-F-18): the instance is created in the nearest object, found by climbing the
 * containment tree from the object designated by the container path of the {@code new} (or {@code this} when there is none), that can
 * host it.
 * 
 * <ul>
 * <li>A concept nested in a container concept ({@link FlexoConcept#getApplicableContainerFlexoConcept()}) is hosted by the nearest
 * instance, starting with the starting instance itself, whose concept is that container concept or a specialization of it.</li>
 * <li>A root concept has no container concept: it is hosted by the nearest {@link VirtualModelInstance} whose {@link VirtualModel}
 * declares it, or inherits it.</li>
 * <li>A {@link FlexoEvent} and a {@link FlexoException} are concepts like the others: the same rules apply to them. The host is also
 * the {@link VirtualModelInstance} that is the source of the event or of the exception.</li>
 * <li>A {@link VirtualModel} that declares a container VirtualModel is hosted by the nearest {@link VirtualModelInstance} of that
 * VirtualModel. One that declares none is not hosted by an instance: its instantiation does not climb.</li>
 * </ul>
 * 
 * Both sides apply the same rule: {@link #resolve(FlexoConcept, FlexoConcept)} on the concepts, when the FML is validated, and
 * {@link #findHost(FlexoConceptInstance, FlexoConcept)} on the instances, when it runs.
 * 
 * @author sylvain
 */
public final class FMLContainerResolver {

	private FMLContainerResolver() {
	}

	/**
	 * Result of the resolution on concepts
	 */
	public static final class Resolution {
		private final boolean resolved;
		private final List<FlexoConcept> climbed;

		private Resolution(boolean resolved, List<FlexoConcept> climbed) {
			this.resolved = resolved;
			this.climbed = climbed;
		}

		/**
		 * Return true when a host can be reached
		 */
		public boolean isResolved() {
			return resolved;
		}

		/**
		 * The concepts that were looked at, nearest first, the starting one included
		 */
		public List<FlexoConcept> getClimbed() {
			return climbed;
		}

		/**
		 * The names of the concepts that were looked at, for a message
		 */
		public String describeClimbed() {
			StringBuilder sb = new StringBuilder();
			for (FlexoConcept c : climbed) {
				sb.append(sb.length() > 0 ? " > " : "").append(c.getName());
			}
			return sb.toString();
		}
	}

	/**
	 * Return the {@link VirtualModel} that contains the supplied concept (the concept itself when it is a {@link VirtualModel})
	 */
	public static VirtualModel getEnclosingVirtualModel(FlexoConcept concept) {
		if (concept instanceof VirtualModel) {
			return (VirtualModel) concept;
		}
		if (concept != null && concept.getDeclaringCompilationUnit() != null) {
			return concept.getDeclaringCompilationUnit().getVirtualModel();
		}
		return null;
	}

	/**
	 * Return the {@link VirtualModel} that declares the supplied concept: the one a {@link VirtualModelInstance} must be an instance
	 * of, or of a specialization of
	 */
	private static VirtualModel getDeclaringVirtualModel(FlexoConcept concept) {
		if (concept.getDeclaringCompilationUnit() != null) {
			return concept.getDeclaringCompilationUnit().getVirtualModel();
		}
		return null;
	}

	/**
	 * Return true when a {@link VirtualModelInstance} of the supplied {@link VirtualModel} can host the supplied concept, a root concept, an
	 * event or an exception: the concept is declared by that VirtualModel or by an ancestor of it
	 */
	private static boolean canHost(VirtualModel hostVirtualModel, VirtualModel declaring) {
		return declaring == null || hostVirtualModel == null || declaring.isAssignableFrom(hostVirtualModel);
	}

	/**
	 * Return true when the concept is not nested in a container concept: it is hosted by a {@link VirtualModelInstance}
	 */
	private static boolean isHostedByVirtualModelInstance(FlexoConcept concept) {
		return concept.getApplicableContainerFlexoConcept() == null;
	}

	/**
	 * Resolution on concepts: can an instance of <code>concept</code> be created from an instance of <code>start</code>, or from the
	 * container reached by climbing from it ?
	 * 
	 * @param start
	 *            the concept of the object designated by the container path of the {@code new}, or the concept of the behaviour for a
	 *            {@code new} with no container path
	 * @param concept
	 *            the concept to instantiate
	 */
	public static Resolution resolve(FlexoConcept start, FlexoConcept concept) {

		List<FlexoConcept> climbed = new ArrayList<>();

		if (concept instanceof VirtualModel) {
			VirtualModel containerVirtualModel = ((VirtualModel) concept).getContainerVirtualModel();
			if (containerVirtualModel == null) {
				// A root VirtualModel is not hosted by an instance: it may be instantiated anywhere, and does not climb
				return new Resolution(true, climbed);
			}
			for (VirtualModel vm = getEnclosingVirtualModel(start); vm != null; vm = vm.getContainerVirtualModel()) {
				climbed.add(vm);
				// A behaviour of an ancestor may run on an instance of a specialization: it succeeds when it does
				if (containerVirtualModel.isAssignableFrom(vm) || vm.isAssignableFrom(containerVirtualModel)) {
					return new Resolution(true, climbed);
				}
			}
			return new Resolution(false, climbed);
		}

		if (isHostedByVirtualModelInstance(concept)) {
			VirtualModel declaring = getDeclaringVirtualModel(concept);
			for (VirtualModel vm = getEnclosingVirtualModel(start); vm != null; vm = vm.getContainerVirtualModel()) {
				climbed.add(vm);
				if (canHost(vm, declaring) || vm.isAssignableFrom(declaring)) {
					return new Resolution(true, climbed);
				}
			}
			return new Resolution(false, climbed);
		}

		FlexoConcept required = concept.getApplicableContainerFlexoConcept();
		if (start instanceof VirtualModel) {
			// A model holds no nested concept: nothing to climb, but the message names where it was looked at
			climbed.add(start);
		}
		for (FlexoConcept c = start; c != null && !(c instanceof VirtualModel); c = c.getApplicableContainerFlexoConcept()) {
			if (climbed.contains(c)) {
				// A concept that is its own applicable container (a root concept extending a concept nested in itself): stop climbing
				break;
			}
			climbed.add(c);
			if (required.isAssignableFrom(c)) {
				return new Resolution(true, climbed);
			}
		}
		// A behaviour of a parent concept may create a concept nested in one of its children: it succeeds when run on an instance of that
		// child
		if (start != null && !(start instanceof VirtualModel) && start.isAssignableFrom(required)) {
			return new Resolution(true, climbed);
		}
		return new Resolution(false, climbed);
	}

	/**
	 * Resolution on instances: return the nearest instance that can host an instance of <code>concept</code>, climbing from
	 * <code>start</code> (itself included). The host is a {@link VirtualModelInstance} when the concept is a root concept, an event, an
	 * exception or a {@link VirtualModel}. Return null when there is none, and also for a {@link VirtualModel} that declares no container
	 * VirtualModel, which is not hosted by an instance.
	 */
	public static FlexoConceptInstance findHost(FlexoConceptInstance start, FlexoConcept concept) {

		if (start == null || concept == null) {
			return null;
		}

		if (concept instanceof VirtualModel) {
			VirtualModel containerVirtualModel = ((VirtualModel) concept).getContainerVirtualModel();
			if (containerVirtualModel == null) {
				return null;
			}
			for (VirtualModelInstance<?, ?> vmi = start.getVirtualModelInstance(); vmi != null; vmi = vmi.getContainerVirtualModelInstance()) {
				if (vmi.getVirtualModel() != null && containerVirtualModel.isAssignableFrom(vmi.getVirtualModel())) {
					return vmi;
				}
			}
			return null;
		}

		if (isHostedByVirtualModelInstance(concept)) {
			VirtualModel declaring = getDeclaringVirtualModel(concept);
			for (VirtualModelInstance<?, ?> vmi = start.getVirtualModelInstance(); vmi != null; vmi = vmi.getContainerVirtualModelInstance()) {
				if (canHost(vmi.getVirtualModel(), declaring)) {
					return vmi;
				}
			}
			return null;
		}

		FlexoConcept required = concept.getApplicableContainerFlexoConcept();
		for (FlexoConceptInstance o = start; o != null && !(o instanceof VirtualModelInstance); o = o.getContainerFlexoConceptInstance()) {
			if (o.getFlexoConcept() != null && required.isAssignableFrom(o.getFlexoConcept())) {
				return o;
			}
		}
		return null;
	}
}
