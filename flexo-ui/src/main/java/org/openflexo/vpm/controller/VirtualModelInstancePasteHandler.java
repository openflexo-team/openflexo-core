/*
 * (c) Copyright 2013-2014 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.vpm.controller;

import java.awt.Event;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoClipboard;
import org.openflexo.foundation.action.PasteAction.DefaultPastingContext;
import org.openflexo.foundation.action.PasteAction.PasteHandler;
import org.openflexo.foundation.action.PasteAction.PastingContext;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.foundation.view.ActorReference;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.view.ModelSlotInstance;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.viewpoint.FlexoRole.RoleCloningStrategy;
import org.openflexo.foundation.viewpoint.PrimitiveRole;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.exceptions.ModelExecutionException;
import org.openflexo.model.factory.Clipboard;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Paste Handler suitable for pasting something into a VirtualModelInstance
 * 
 * @author sylvain
 * 
 */
public class VirtualModelInstancePasteHandler implements PasteHandler<VirtualModelInstance> {

	private static final Logger logger = Logger.getLogger(VirtualModelInstancePasteHandler.class.getPackage().getName());

	public static final String COPY_SUFFIX = "-copy";

	@Override
	public Class<VirtualModelInstance> getPastingPointHolderType() {
		return VirtualModelInstance.class;
	}

	@Override
	public PastingContext<VirtualModelInstance> retrievePastingContext(FlexoObject focusedObject, List<FlexoObject> globalSelection,
			FlexoClipboard clipboard, Event event) {

		if (focusedObject instanceof VirtualModelInstance) {
			return new HeterogeneousPastingContext((VirtualModelInstance) focusedObject, event);

		}

		if (focusedObject instanceof FlexoConceptInstance) {
			return new HeterogeneousPastingContext(((FlexoConceptInstance) focusedObject).getVirtualModelInstance(), event);

		}

		return null;
	}

	@Override
	public void prepareClipboardForPasting(FlexoClipboard clipboard, PastingContext<VirtualModelInstance> pastingContext) {

		System.out.println("********** prepareClipboardForPasting in " + pastingContext);

		if (pastingContext instanceof HeterogeneousPastingContext) {
			((HeterogeneousPastingContext) pastingContext).prepareClipboardForPasting(clipboard);
		}
	}

	@Override
	public void finalizePasting(FlexoClipboard clipboard, PastingContext<VirtualModelInstance> pastingContext) {

		System.out.println("Trying to notify.........");

		if (pastingContext instanceof HeterogeneousPastingContext) {

			for (ModelSlotInstance<?, ?> msi : pastingContext.getPastingPointHolder().getModelSlotInstances()) {
				Object pastingPointHolder = getModelSlotSpecificPastingPointHolder(msi, (HeterogeneousPastingContext) pastingContext);

				// Well, not easy to understand here
				// Some modelling elements may have been added to the resource, but they were added BEFORE federation.
				// Thus, Model/View modelling paradigm may have lead some view to be in sync with a non-federated context
				// We renotify again to be sure that all view might react to federation activation
				if (pastingPointHolder instanceof HasPropertyChangeSupport) {
					System.out.println("notify " + pastingPointHolder);
					((HasPropertyChangeSupport) pastingPointHolder).getPropertyChangeSupport().firePropertyChange("invalidate", null,
							pastingPointHolder);
				}
			}
		}

		System.out.println("Done notify.........");
	}

	public Object getModelSlotSpecificPastingPointHolder(ModelSlotInstance<?, ?> modelSlotInstance,
			HeterogeneousPastingContext pastingContext) {
		return pastingContext.getPastingPointHolder();
	}

	public void prepareModelSlotSpecificClipboard(Clipboard modelSlotSpecificClipboard, ModelSlotInstance<?, ?> modelSlotInstance,
			HeterogeneousPastingContext pastingContext) {
	}

	/**
	 * A pasting context used in the context of model federation and involving many technical spaces
	 * 
	 * @author sylvain
	 * 
	 */
	public class HeterogeneousPastingContext extends DefaultPastingContext<VirtualModelInstance> {

		// private final Map<ModelSlotInstance<?, ?>, Clipboard> modelSlotClipboards;

		private FlexoClipboard clipboard;

		public HeterogeneousPastingContext(VirtualModelInstance holder, Event event) {
			super(holder, event);
			// modelSlotClipboards = new HashMap<ModelSlotInstance<?, ?>, Clipboard>();
		}

		public void prepareClipboardForPasting(FlexoClipboard clipboard) {

			this.clipboard = clipboard;
			Clipboard leaderClipboard = clipboard.getLeaderClipboard();

			// First put all FCI in a list
			List<FlexoConceptInstance> fciList = new ArrayList<FlexoConceptInstance>();

			if (leaderClipboard.isSingleObject()) {
				if (leaderClipboard.getSingleContents() instanceof FlexoConceptInstance) {
					fciList.add((FlexoConceptInstance) leaderClipboard.getSingleContents());
				}
			} else {
				for (Object o : leaderClipboard.getMultipleContents()) {
					if (o instanceof FlexoConceptInstance) {
						fciList.add((FlexoConceptInstance) o);
					}
				}
			}

			try {

				for (ModelSlotInstance<?, ?> msi : getPastingPointHolder().getModelSlotInstances()) {
					System.out.println("Voyons pour le model slot " + msi);
					if (msi.getAccessedResourceData() != null && msi.getAccessedResourceData().getResource() instanceof PamelaResource) {
						System.out.println("Hop, je m'occupe de la resource " + msi.getResource());
						PamelaResource modelSlotInstanceResource = (PamelaResource) msi.getAccessedResourceData().getResource();
						ModelFactory factory = modelSlotInstanceResource.getFactory();

						Clipboard modelSlotInstanceClipboard = clipboard.getClipboard(modelSlotInstanceResource);

						// Take care to not do it for the leader clipboard
						// For him, the paste will be performed in PasteAction
						if (modelSlotInstanceClipboard != null && modelSlotInstanceClipboard != clipboard.getLeaderClipboard()) {

							// Some objects needs to be cloned in this model slot

							// modelSlotClipboards.put(msi, modelSlotInstanceClipboard);

							System.out.println("Pour la resource " + msi.getResource());
							System.out.println("" + modelSlotInstanceClipboard.debug(modelSlotInstanceResource.toString()));

							Object pastingPointHolder = getModelSlotSpecificPastingPointHolder(msi, this);

							System.out.println("Pour le MS: " + msi);
							System.out.println("pastingPointHolder=" + pastingPointHolder);
							System.out.println("On est dans " + getClass());

							if (factory.isPastable(modelSlotInstanceClipboard, pastingPointHolder)) {

								prepareModelSlotSpecificClipboard(modelSlotInstanceClipboard, msi, this);

								Map<Object, Object> copiedObjects = new HashMap<Object, Object>();

								System.out.println("!!!!!!!!! JUSTE AVANT le paste dans " + modelSlotInstanceResource);
								modelSlotInstanceClipboard.debug(modelSlotInstanceResource.toString());

								Object[] lastCopiedContents = modelSlotInstanceClipboard.getLastReferenceContents();

								Object copy = factory.paste(modelSlotInstanceClipboard, pastingPointHolder);

								System.out.println("les nouveaux objets: " + copy);

								if (modelSlotInstanceClipboard.isSingleObject()) {
									copiedObjects.put(lastCopiedContents[0], copy);
									System.out.println("Pour " + lastCopiedContents[0] + " j'ai maintenant " + copy);
								} else {
									List copyList = (List) copy;
									for (int i = 0; i < lastCopiedContents.length; i++) {
										copiedObjects.put(lastCopiedContents[i], copyList.get(i));
										System.out.println("Pour " + lastCopiedContents[i] + " j'ai maintenant " + copyList.get(i));
									}
								}

								System.out.println("fciList=" + fciList);

								System.out.println("copiedObjects=");
								for (Object key : copiedObjects.keySet()) {
									System.out.println("for key=" + key + " copiedObject=" + copiedObjects.get(key));
								}

								// Now replace all ActorReferences !!!
								for (FlexoConceptInstance fci : fciList) {
									System.out.println(">> for " + fci);
									for (ActorReference actor : fci.getActors()) {
										System.out.println("    > actor " + actor.getRoleName() + " = " + actor.getModellingElement());
										if (actor.getFlexoRole().getModelSlot() == msi.getModelSlot()
												&& actor.getFlexoRole().getCloningStrategy() == RoleCloningStrategy.Clone
												&& (!(actor.getFlexoRole() instanceof PrimitiveRole))) {
											System.out.println("Maintenant, j'essaie de remplacer " + actor.getModellingElement());
											System.out.println("Par: " + copiedObjects.get(actor.getModellingElement()));
											actor.setModellingElement(copiedObjects.get(actor.getModellingElement()));
										}
									}
								}

							} else {
								System.out
										.println("Cannot paste " + modelSlotInstanceClipboard.getTypes()[0] + " in " + pastingPointHolder);
							}

						}

						/*ModelFactory factory = resource.getFactory();
						List<Object> objectsToClone = new ArrayList<Object>();
						for (FlexoConceptInstance fci : fciList) {
							for (ActorReference<?> actor : fci.getActors()) {
								if (actor.getFlexoRole().getModelSlot() == msi.getModelSlot()
										&& actor.getFlexoRole().getCloningStrategy() == RoleCloningStrategy.Clone
										&& (!(actor.getFlexoRole() instanceof PrimitiveRole))) {
									if (!objectsToClone.contains(actor.getModellingElement())) {
										objectsToClone.add(actor.getModellingElement());
									}
								}
							}
						}*/

						// Now add objects which were eventually in the original copy
						// (may contains other objects that are not playing any role in any flexo concept instances
						/*Clipboard originalClipboard = clipboard.getClipboard(resource);

						System.out.println("ok, maintenant je regarde s'il n'y avait pas d'autres trucs...");
						System.out.println("msi=" + msi);
						System.out.println("resource=" + resource);
						System.out.println("originalClipboard=" + originalClipboard);
						System.out.println(originalClipboard.debug());

						if (originalClipboard != null) {
							if (originalClipboard.isSingleObject()) {
								if (!objectsToClone.contains(originalClipboard.getSingleContents())) {
									System.out.println("Yes, j'ai trouve un autre truc qui n'est pas federe, je le rajoute: "
											+ originalClipboard.getSingleContents());
									objectsToClone.add(originalClipboard.getSingleContents());
								}
							} else {
								for (Object o : originalClipboard.getMultipleContents()) {
									if (o instanceof FlexoConceptInstance) {
										if (!objectsToClone.contains(o)) {
											System.out.println("Yes, j'ai trouve un autre truc qui n'est pas federe, je le rajoute: " + o);
											objectsToClone.add(o);
											System.out.println("c'est un: " + o.getClass());
										}
									}
								}
							}
						}

						System.out.println("Pour la resource " + msi.getResource());
						System.out.println("Je dois cloner tous ces objects:");

						for (Object o : objectsToClone) {
							System.out.println("> " + o);
						}

						if (objectsToClone.size() > 0) {
							// Some objects needs to be cloned in this model slot
							Clipboard modelSlotClipboard = factory.copy(objectsToClone.toArray());
							modelSlotClipboards.put(msi, modelSlotClipboard);

							System.out.println("Pour la resource " + msi.getResource());
							System.out.println("" + modelSlotClipboard.debug());

							Object pastingPointHolder = getModelSlotSpecificPastingPointHolder(msi, this);

							System.out.println("Pour le MS: " + msi);
							System.out.println("pastingPointHolder=" + pastingPointHolder);
							System.out.println("On est dans " + getClass());

							if (factory.isPastable(modelSlotClipboard, pastingPointHolder)) {

								prepareModelSlotSpecificClipboard(modelSlotClipboard, msi, this);

								Map<Object, Object> copiedObjects = new HashMap<Object, Object>();
								Object copy = factory.paste(modelSlotClipboard, pastingPointHolder);
								if (modelSlotClipboard.isSingleObject()) {
									copiedObjects.put(modelSlotClipboard.getOriginalContents()[0], copy);
									System.out.println("Pour " + modelSlotClipboard.getOriginalContents()[0] + " j'ai maintenant " + copy);
								} else {
									List copyList = (List) copy;
									for (int i = 0; i < modelSlotClipboard.getOriginalContents().length; i++) {
										copiedObjects.put(modelSlotClipboard.getOriginalContents()[i], copyList.get(i));
										System.out.println("Pour " + modelSlotClipboard.getOriginalContents()[i] + " j'ai maintenant "
												+ copyList.get(i));
									}
								}

								// Now replace all ActorReferences !!!
								for (FlexoConceptInstance fci : fciList) {
									for (ActorReference actor : fci.getActors()) {
										if (actor.getFlexoRole().getModelSlot() == msi.getModelSlot()
												&& actor.getFlexoRole().getCloningStrategy() == RoleCloningStrategy.Clone
												&& (!(actor.getFlexoRole() instanceof PrimitiveRole))) {
											System.out.println("Maintenant, j'essaie de remplacer " + actor.getModellingElement());
											System.out.println("Par: " + copiedObjects.get(actor.getModellingElement()));
											actor.setModellingElement(copiedObjects.get(actor.getModellingElement()));
										}
									}
								}

							} else {
								System.out.println("Zut, je ne peux pas paster " + modelSlotClipboard.getTypes()[0] + " dans "
										+ pastingPointHolder);
							}
						}*/

					} else {
						if (msi.getAccessedResourceData() != null
								&& !(msi.getAccessedResourceData().getResource() instanceof PamelaResource)) {
							logger.severe("Unexpected resource " + msi.getAccessedResourceData().getResource());
						}
					}
				}

			} catch (ModelExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ModelDefinitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public Clipboard getModelSlotClipboard(ModelSlotInstance<?, ?> msi) {
			if (msi.getAccessedResourceData() != null && msi.getAccessedResourceData().getResource() instanceof PamelaResource) {
				PamelaResource<?, ?> modelSlotInstanceResource = (PamelaResource<?, ?>) msi.getAccessedResourceData().getResource();
				return clipboard.getClipboard(modelSlotInstanceResource);
			}
			return null;
		}
	}
}
