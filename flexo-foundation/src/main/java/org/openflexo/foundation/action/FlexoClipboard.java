package org.openflexo.foundation.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.fge.control.exceptions.CopyException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.InnerResourceData;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.view.ActorReference;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.view.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.viewpoint.FlexoRole.RoleCloningStrategy;
import org.openflexo.foundation.viewpoint.PrimitiveRole;
import org.openflexo.model.factory.Clipboard;
import org.openflexo.model.factory.ModelFactory;

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
 * @author sylvain
 * 
 */
public class FlexoClipboard {

	private final Map<PamelaResource<?, ?>, Clipboard> clipboards;
	private PamelaResource<?, ?> leaderResource;

	private FlexoClipboard() {
		clipboards = new HashMap<PamelaResource<?, ?>, Clipboard>();
	}

	public Clipboard getLeaderClipboard() {

		for (PamelaResource<?, ?> r : clipboards.keySet()) {
			if (r instanceof VirtualModelInstanceResource) {
				// This resource has always the priority, since this is the place where federation take place
				return clipboards.get(r);
			}
		}

		Clipboard returned;
		if (leaderResource != null) {
			returned = clipboards.get(leaderResource);
		} else {
			returned = clipboards.values().iterator().next();
		}

		return returned;
	}

	public Clipboard getClipboard(PamelaResource<?, ?> resource) {
		return clipboards.get(resource);
	}

	public static Map<PamelaResource<?, ?>, List<FlexoObject>> extendsMapOfObjectsToBeCopied(
			Map<PamelaResource<?, ?>, List<FlexoObject>> objectsToBeCopied) {

		// This map will be augmented by all modelling element which are actors of a FlexoRole
		Map<PamelaResource<?, ?>, List<FlexoObject>> extendedObjectsToBeCopied = new HashMap<PamelaResource<?, ?>, List<FlexoObject>>();
		for (PamelaResource<?, ?> pamelaResource : objectsToBeCopied.keySet()) {
			extendedObjectsToBeCopied.put(pamelaResource, new ArrayList<FlexoObject>(objectsToBeCopied.get(pamelaResource)));
		}

		// First lookup the VirtualModelInstanceResource
		for (PamelaResource<?, ?> pamelaResource : objectsToBeCopied.keySet()) {
			if (pamelaResource instanceof VirtualModelInstanceResource) {
				List<FlexoObject> objectsInVirtualModelInstanceResource = objectsToBeCopied.get(pamelaResource);
				for (FlexoObject o : objectsInVirtualModelInstanceResource) {
					// iteratate on all FlexoConceptInstance
					if (o instanceof FlexoConceptInstance) {
						FlexoConceptInstance fci = (FlexoConceptInstance) o;
						for (ActorReference<?> actor : fci.getActors()) {
							if (actor.getFlexoRole().getCloningStrategy() == RoleCloningStrategy.Clone
									&& (!(actor.getFlexoRole() instanceof PrimitiveRole))) {
								if ((actor.getModellingElement() instanceof InnerResourceData)
										&& (actor.getModellingElement() instanceof FlexoObject)
										&& (((InnerResourceData) actor.getModellingElement()).getResourceData() != null)
										&& (((InnerResourceData) actor.getModellingElement()).getResourceData().getResource() instanceof PamelaResource)) {
									// This actor is referenced by the flexo concept instance
									// Add it in the clipboard of associated PamelaResource
									PamelaResource<?, ?> modelSlotSpecificResource = (PamelaResource<?, ?>) ((InnerResourceData) actor
											.getModellingElement()).getResourceData().getResource();
									List<FlexoObject> alreadyExistingObjects = extendedObjectsToBeCopied.get(modelSlotSpecificResource);
									if (alreadyExistingObjects == null) {
										alreadyExistingObjects = new ArrayList<FlexoObject>();
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

	public static FlexoClipboard copy(Map<PamelaResource<?, ?>, List<FlexoObject>> objectsToBeCopied, FlexoObject leader, Object copyContext)
			throws CopyException {

		// This map will be augmented by all modelling element which are actors of a FlexoRole
		Map<PamelaResource<?, ?>, List<FlexoObject>> extendedObjectsToBeCopied = extendsMapOfObjectsToBeCopied(objectsToBeCopied);

		FlexoClipboard returned = new FlexoClipboard();
		for (PamelaResource<?, ?> pamelaResource : extendedObjectsToBeCopied.keySet()) {

			Clipboard clipboard;
			ModelFactory modelFactory = pamelaResource.getFactory();

			/*System.out.println("COPY");
			System.out.println("pamelaResource=" + pamelaResource);
			System.out.println("modelFactory=" + modelFactory);
			System.out.println("copyContext=" + copyContext);
			System.out.println("objectsToBeCopied=" + objectsToBeCopied);*/

			try {

				// System.out.println("--------- START COPY");

				System.out.println("########## For resource " + pamelaResource);
				System.out.println("We had: " + objectsToBeCopied.get(pamelaResource));
				System.out.println("We have now: " + extendedObjectsToBeCopied.get(pamelaResource));

				List<FlexoObject> objectsToCopyInThisResource = extendedObjectsToBeCopied.get(pamelaResource);

				clipboard = modelFactory.copy(objectsToCopyInThisResource.toArray(new Object[objectsToCopyInThisResource.size()]));
				clipboard.setCopyContext(copyContext);
				// System.out.println(clipboard.debug());
				// System.out.println("copyContext=" + copyContext);
				// TODO ?
				// notifyObservers(new SelectionCopied(clipboard));

				// System.out.println("--------- END COPY");

				returned.clipboards.put(pamelaResource, clipboard);

			} catch (Throwable e) {
				throw new CopyException(e, modelFactory);
			}
		}

		if (leader instanceof InnerResourceData) {
			if (((InnerResourceData) leader).getResourceData() != null) {
				FlexoResource<?> resource = ((InnerResourceData) leader).getResourceData().getResource();
				if (resource instanceof PamelaResource) {
					returned.leaderResource = (PamelaResource) resource;
				}
			}
		}

		return returned;
	}

	public static FlexoClipboard cut(Map<PamelaResource<?, ?>, List<FlexoObject>> objectsToBeCut, FlexoObject leader, Object copyContext)
			throws CopyException {

		// This map will be augmented by all modelling element which are actors of a FlexoRole
		Map<PamelaResource<?, ?>, List<FlexoObject>> extendedObjectsToBeCut = extendsMapOfObjectsToBeCopied(objectsToBeCut);

		FlexoClipboard returned = new FlexoClipboard();

		for (PamelaResource<?, ?> pamelaResource : extendedObjectsToBeCut.keySet()) {

			Clipboard clipboard;
			ModelFactory modelFactory = pamelaResource.getFactory();

			/*System.out.println("CUT");
			System.out.println("pamelaResource=" + pamelaResource);
			System.out.println("modelFactory=" + modelFactory);
			System.out.println("copyContext=" + copyContext);
			System.out.println("objectsToBeCopied=" + objectsToBeCopied);*/

			try {

				// System.out.println("--------- START CUT");

				clipboard = modelFactory.cut(extendedObjectsToBeCut.get(pamelaResource).toArray(new Object[extendedObjectsToBeCut.size()]));
				clipboard.setCopyContext(copyContext);
				// System.out.println(clipboard.debug());
				// System.out.println("copyContext=" + copyContext);
				// TODO ?
				// notifyObservers(new SelectionCopied(clipboard));

				// System.out.println("--------- END CUT");

				returned.clipboards.put(pamelaResource, clipboard);

			} catch (Throwable e) {
				throw new CopyException(e, modelFactory);
			}
		}

		if (leader instanceof InnerResourceData) {
			if (((InnerResourceData) leader).getResourceData() != null) {
				FlexoResource<?> resource = ((InnerResourceData) leader).getResourceData().getResource();
				if (resource instanceof PamelaResource) {
					returned.leaderResource = (PamelaResource) resource;
				}
			}
		}
		return returned;
	}

}