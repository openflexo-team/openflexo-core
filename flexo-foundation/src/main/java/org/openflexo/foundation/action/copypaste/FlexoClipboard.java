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

package org.openflexo.foundation.action.copypaste;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.exceptions.CopyException;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.fml.FlexoRole.RoleCloningStrategy;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.pamela.factory.Clipboard;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * Clipboard used in the context Openflexo infrastructure.<br>
 * Support model federation features involving many technical spaces<br>
 * 
 * Clipboarded {@link FlexoConceptInstance} are watched for their actual actors living in a PamelaResource. Those actors are clipboarded in
 * all respective clipboards
 * 
 * From a technical point of view, a {@link FlexoClipboard} is an aggregation of {@link Clipboard} instances, associated to a
 * {@link PamelaResource}.<br>
 * A particular {@link Clipboard} plays leader role, and should be the only one driving the whole clipboard operation
 * 
 * 
 * @author sylvain
 * 
 */
public class FlexoClipboard {

	private final Map<PamelaResource<?, ?>, Clipboard> clipboards;
	private PamelaResource<?, ?> leaderResource;
	private FlexoEditor editor;

	private FlexoClipboard(FlexoEditor editor) {
		clipboards = new HashMap<>();
	}

	public Clipboard getLeaderClipboard() {

		for (PamelaResource<?, ?> r : clipboards.keySet()) {
			if (r instanceof AbstractVirtualModelInstanceResource) {
				// This resource has always the priority, since this is the place where federation take place
				return clipboards.get(r);
			}
		}

		Clipboard returned;
		if (leaderResource != null) {
			returned = clipboards.get(leaderResource);
		}
		else {
			returned = clipboards.values().iterator().next();
		}

		return returned;
	}

	public FlexoEditor getEditor() {
		return editor;
	}

	public Clipboard getClipboard(PamelaResource<?, ?> resource) {
		return clipboards.get(resource);
	}

	public static Map<PamelaResource<?, ?>, List<FlexoObject>> extendsMapOfObjectsToBeCopied(
			Map<PamelaResource<?, ?>, List<FlexoObject>> objectsToBeCopied) {

		// This map will be augmented by all modelling element which are actors of a FlexoRole
		Map<PamelaResource<?, ?>, List<FlexoObject>> extendedObjectsToBeCopied = new HashMap<>();
		for (PamelaResource<?, ?> pamelaResource : objectsToBeCopied.keySet()) {
			extendedObjectsToBeCopied.put(pamelaResource, new ArrayList<>(objectsToBeCopied.get(pamelaResource)));
		}

		// First lookup the VirtualModelInstanceResource
		for (PamelaResource<?, ?> pamelaResource : objectsToBeCopied.keySet()) {
			if (pamelaResource instanceof AbstractVirtualModelInstanceResource) {
				List<FlexoObject> objectsInVirtualModelInstanceResource = objectsToBeCopied.get(pamelaResource);
				for (FlexoObject o : objectsInVirtualModelInstanceResource) {
					// iteratate on all FlexoConceptInstance
					if (o instanceof FlexoConceptInstance) {
						FlexoConceptInstance fci = (FlexoConceptInstance) o;
						for (ActorReference<?> actor : fci.getActors()) {
							if (actor.getFlexoRole() != null && actor.getFlexoRole().getCloningStrategy() == RoleCloningStrategy.Clone
									&& (!(actor.getFlexoRole() instanceof PrimitiveRole))) {
								if ((actor.getModellingElement() instanceof InnerResourceData)
										&& (actor.getModellingElement() instanceof FlexoObject)
										&& (((InnerResourceData<?>) actor.getModellingElement()).getResourceData() != null)
										&& (((InnerResourceData<?>) actor.getModellingElement()).getResourceData()
												.getResource() instanceof PamelaResource)) {
									// This actor is referenced by the flexo concept instance
									// Add it in the clipboard of associated PamelaResource
									PamelaResource<?, ?> modelSlotSpecificResource = (PamelaResource<?, ?>) ((InnerResourceData<?>) actor
											.getModellingElement()).getResourceData().getResource();
									List<FlexoObject> alreadyExistingObjects = extendedObjectsToBeCopied.get(modelSlotSpecificResource);
									if (alreadyExistingObjects == null) {
										alreadyExistingObjects = new ArrayList<>();
										extendedObjectsToBeCopied.put(modelSlotSpecificResource, alreadyExistingObjects);
									}
									System.out.println("Adding in modelSlotSpecificResource " + modelSlotSpecificResource + " : "
											+ actor.getModellingElement());
									alreadyExistingObjects.add((FlexoObject) actor.getModellingElement());
								}
							}
						}
					}
				}
			}
		}

		return extendedObjectsToBeCopied;
	}

	public static FlexoClipboard copy(Map<PamelaResource<?, ?>, List<FlexoObject>> objectsToBeCopied, FlexoObject leader,
			Object copyContext, FlexoEditor editor) throws CopyException {

		// This map will be augmented by all modelling element which are actors of a FlexoRole
		Map<PamelaResource<?, ?>, List<FlexoObject>> extendedObjectsToBeCopied = extendsMapOfObjectsToBeCopied(objectsToBeCopied);

		FlexoClipboard returned = new FlexoClipboard(editor);
		for (PamelaResource<?, ?> pamelaResource : extendedObjectsToBeCopied.keySet()) {

			Clipboard clipboard;
			PamelaModelFactory pamelaModelFactory = pamelaResource.getFactory();

			/*System.out.println("COPY");
			System.out.println("pamelaResource=" + pamelaResource);
			System.out.println("modelFactory=" + modelFactory);
			System.out.println("copyContext=" + copyContext);
			System.out.println("objectsToBeCopied=" + objectsToBeCopied);*/

			try {

				// System.out.println("--------- START COPY");

				// System.out.println("########## For resource " + pamelaResource);
				// System.out.println("We had: " + objectsToBeCopied.get(pamelaResource));

				/*for (FlexoObject o : objectsToBeCopied.get(pamelaResource)) {
					if (o instanceof FlexoBehaviour) {
						System.out.println("J'avais ca: ");
						System.out.println(((FlexoBehaviour) o).getFMLRepresentation());
					}
				}*/

				List<FlexoObject> objectsToCopyInThisResource = extendedObjectsToBeCopied.get(pamelaResource);

				/*for (FlexoObject o : objectsToCopyInThisResource) {
					if (o instanceof ResourceData) {
						// TODO: handle resource copy
					}
					else if (o instanceof InnerResourceData) {
						// TODO: handle resource copy
					}
				}*/

				clipboard = pamelaModelFactory.copy(objectsToCopyInThisResource.toArray(new Object[objectsToCopyInThisResource.size()]));
				clipboard.setCopyContext(copyContext);

				returned.clipboards.put(pamelaResource, clipboard);

			} catch (Throwable e) {
				throw new CopyException(e);
			}
		}

		if (leader instanceof InnerResourceData) {
			if (((InnerResourceData<?>) leader).getResourceData() != null) {
				FlexoResource<?> resource = ((InnerResourceData<?>) leader).getResourceData().getResource();
				if (resource instanceof PamelaResource) {
					returned.leaderResource = (PamelaResource<?, ?>) resource;
				}
			}
		}

		return returned;
	}

	public static FlexoClipboard cut(Map<PamelaResource<?, ?>, List<FlexoObject>> objectsToBeCut, FlexoObject leader, Object copyContext,
			FlexoEditor editor) throws CopyException {

		// This map will be augmented by all modelling element which are actors of a FlexoRole
		Map<PamelaResource<?, ?>, List<FlexoObject>> extendedObjectsToBeCut = extendsMapOfObjectsToBeCopied(objectsToBeCut);

		FlexoClipboard returned = new FlexoClipboard(editor);

		for (PamelaResource<?, ?> pamelaResource : extendedObjectsToBeCut.keySet()) {

			Clipboard clipboard;
			PamelaModelFactory pamelaModelFactory = pamelaResource.getFactory();

			/*System.out.println("CUT");
			System.out.println("pamelaResource=" + pamelaResource);
			System.out.println("modelFactory=" + modelFactory);
			System.out.println("copyContext=" + copyContext);
			System.out.println("objectsToBeCopied=" + objectsToBeCopied);*/

			try {

				// System.out.println("--------- START CUT");

				List<FlexoObject> objects = extendedObjectsToBeCut.get(pamelaResource);
				clipboard = pamelaModelFactory.cut(objects.toArray(new Object[objects.size()]));
				clipboard.setCopyContext(copyContext);
				// System.out.println(clipboard.debug());
				// System.out.println("copyContext=" + copyContext);
				// TODO ?
				// notifyObservers(new SelectionCopied(clipboard));

				// System.out.println("--------- END CUT");

				returned.clipboards.put(pamelaResource, clipboard);

			} catch (Throwable e) {
				throw new CopyException(e);
			}
		}

		if (leader instanceof InnerResourceData) {
			if (((InnerResourceData<?>) leader).getResourceData() != null) {
				FlexoResource<?> resource = ((InnerResourceData<?>) leader).getResourceData().getResource();
				if (resource instanceof PamelaResource) {
					returned.leaderResource = (PamelaResource<?, ?>) resource;
				}
			}
		}
		return returned;
	}

	public String debug() {
		StringBuffer returned = new StringBuffer();
		returned.append("*************** FlexoClipboard ****************\n");
		for (PamelaResource<?, ?> r : clipboards.keySet()) {
			returned.append(clipboards.get(r).debug(r.toString()));
		}
		return returned.toString();
	}

}
