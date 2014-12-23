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
package org.openflexo.fml.rt.controller;

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
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.FlexoRole.RoleCloningStrategy;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.PamelaResource;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.exceptions.ModelExecutionException;
import org.openflexo.model.factory.Clipboard;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Paste Handler suitable for pasting something into a VirtualModelInstance<br>
 * This is the federated models paste handler (when you want to copy/paste {@link FlexoConceptInstance} inside a {@link VirtualModel})
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
					if (msi.getAccessedResourceData() != null && msi.getAccessedResourceData().getResource() instanceof PamelaResource) {
						PamelaResource modelSlotInstanceResource = (PamelaResource) msi.getAccessedResourceData().getResource();
						ModelFactory factory = modelSlotInstanceResource.getFactory();

						Clipboard modelSlotInstanceClipboard = clipboard.getClipboard(modelSlotInstanceResource);

						// Take care to not do it for the leader clipboard
						// For him, the paste will be performed in PasteAction
						if (modelSlotInstanceClipboard != null && modelSlotInstanceClipboard != clipboard.getLeaderClipboard()) {

							// Some objects needs to be cloned in this model slot

							Object pastingPointHolder = getModelSlotSpecificPastingPointHolder(msi, this);

							if (factory.isPastable(modelSlotInstanceClipboard, pastingPointHolder)) {

								prepareModelSlotSpecificClipboard(modelSlotInstanceClipboard, msi, this);

								Map<Object, Object> copiedObjects = new HashMap<Object, Object>();

								// We retain the references for copied objects in this model slot, BEFORE the paste
								Object[] lastReferenceContents = modelSlotInstanceClipboard.getLastReferenceContents();

								// Then the paste for the model slot in the right technical space
								Object copy = factory.paste(modelSlotInstanceClipboard, pastingPointHolder);

								// Now we try to establish references between referenced object before the paste and the copied obects after
								// the paste
								if (modelSlotInstanceClipboard.isSingleObject()) {
									copiedObjects.put(lastReferenceContents[0], copy);
								} else {
									List copyList = (List) copy;
									for (int i = 0; i < lastReferenceContents.length; i++) {
										copiedObjects.put(lastReferenceContents[i], copyList.get(i));
									}
								}

								// Now we have to replace all ActorReferences, according to the copiedObjects map
								for (FlexoConceptInstance fci : fciList) {
									for (ActorReference actor : fci.getActors()) {
										if (actor.getFlexoRole().getModelSlot() == msi.getModelSlot()
												&& actor.getFlexoRole().getCloningStrategy() == RoleCloningStrategy.Clone
												&& (!(actor.getFlexoRole() instanceof PrimitiveRole))) {
											actor.setModellingElement(copiedObjects.get(actor.getModellingElement()));
										}
									}
								}

							} else {
								System.out
										.println("Cannot paste " + modelSlotInstanceClipboard.getTypes()[0] + " in " + pastingPointHolder);
							}

						}

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
