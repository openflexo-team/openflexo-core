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

package org.openflexo.foundation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.action.SelectAllAction.SelectAllActionType;
import org.openflexo.foundation.action.copypaste.CopyAction.CopyActionType;
import org.openflexo.foundation.action.copypaste.CutAction.CutActionType;
import org.openflexo.foundation.action.copypaste.DefaultPasteHandler;
import org.openflexo.foundation.action.copypaste.FlexoClipboard;
import org.openflexo.foundation.action.copypaste.PasteAction.PasteActionType;
import org.openflexo.foundation.action.copypaste.PasteHandler;
import org.openflexo.foundation.action.copypaste.PastingContext;
import org.openflexo.pamela.factory.Clipboard;
import org.openflexo.pamela.factory.EditingContext;
import org.openflexo.pamela.factory.EditingContextImpl;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;
import org.openflexo.toolbox.StringUtils;

/**
 * The {@link FlexoEditingContext} represents the {@link EditingContext} for the whole application<br>
 * 
 * This {@link EditingContext} is a {@link FlexoService} instanciated in Openflexo service architecture
 * 
 * @author sylvain
 * 
 */
public class FlexoEditingContext extends EditingContextImpl implements FlexoService {

	protected static final Logger logger = Logger.getLogger(FlexoEditingContext.class.getPackage().getName());

	protected Status status = Status.Registered;

	private FlexoServiceManager serviceManager;
	private FlexoUndoManager undoManager;

	private CopyActionType copyActionType;
	private CutActionType cutActionType;
	private PasteActionType pasteActionType;
	private SelectAllActionType selectAllActionType;

	private FlexoClipboard clipboard;

	private final Map<Class<?>, List<PasteHandler<? extends FlexoObject>>> pasteHandlers;

	private final PasteHandler<?> defaultPasteHandler;

	private boolean warnOnUnexpectedEdits = true;

	public static FlexoEditingContext createInstance() {
		return new FlexoEditingContext(true);
	}

	public static FlexoEditingContext createInstance(boolean warnOnUnexpectedEdits) {
		return new FlexoEditingContext(warnOnUnexpectedEdits);
	}

	/**
	 * Return a flag indicating if we should warn about edits being raised outside declared UNDO scope
	 * 
	 * @return
	 */
	public boolean warnOnUnexpectedEdits() {
		return warnOnUnexpectedEdits;
	}

	private FlexoEditingContext(boolean warnOnUnexpectedEdits) {
		this.warnOnUnexpectedEdits = warnOnUnexpectedEdits;
		pasteHandlers = new HashMap<>();
		defaultPasteHandler = new DefaultPasteHandler();
	}

	@Override
	public FlexoUndoManager getUndoManager() {
		return undoManager;
	}

	@Override
	public String getServiceName() {
		return "FlexoEditingContext";
	}

	@Override
	public void initialize() {
		logger.info("Initialized FlexoEditingContext...");
		undoManager = new FlexoUndoManager(this);
		copyActionType = new CopyActionType(this);
		FlexoObjectImpl.addActionForClass(copyActionType, FlexoObject.class);
		cutActionType = new CutActionType(this);
		FlexoObjectImpl.addActionForClass(cutActionType, FlexoObject.class);
		pasteActionType = new PasteActionType(this);
		FlexoObjectImpl.addActionForClass(pasteActionType, FlexoObject.class);
		selectAllActionType = new SelectAllActionType(this);
		FlexoObjectImpl.addActionForClass(selectAllActionType, FlexoObject.class);
		status = Status.Started;
	}

	@Override
	public void receiveNotification(FlexoService caller, ServiceNotification notification) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(getClass().getSimpleName() + " service received notification " + notification + " from " + caller);
		}
	}

	@Override
	public void register(FlexoServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	@Override
	public FlexoServiceManager getServiceManager() {
		return serviceManager;
	}

	public CopyActionType getCopyActionType() {
		return copyActionType;
	}

	public CutActionType getCutActionType() {
		return cutActionType;
	}

	public PasteActionType getPasteActionType() {
		return pasteActionType;
	}

	public SelectAllActionType getSelectAllActionType() {
		return selectAllActionType;
	}

	public FlexoClipboard getClipboard() {
		return clipboard;
	}

	public void setClipboard(FlexoClipboard clipboard) {
		this.clipboard = clipboard;
	}

	public void registerPasteHandler(PasteHandler<?> pasteHandler) {
		// logger.info("%%%%%%%%% registerPasteHandler " + pasteHandler + " for " + pasteHandler.getPastingPointHolderType());
		List<PasteHandler<?>> handlersList = pasteHandlers.get(pasteHandler.getPastingPointHolderType());
		if (handlersList == null) {
			handlersList = new ArrayList<>();
			pasteHandlers.put(pasteHandler.getPastingPointHolderType(), handlersList);
		}
		if (!handlersList.contains(pasteHandler)) {
			handlersList.add(pasteHandler);
		}
	}

	public void unregisterPasteHandler(PasteHandler<?> pasteHandler) {
		// logger.info("%%%%%%%%% unregisterPasteHandler " + pasteHandler);
		List<PasteHandler<?>> handlersList = pasteHandlers.get(pasteHandler.getPastingPointHolderType());
		if (handlersList != null && handlersList.contains(pasteHandler)) {
			handlersList.remove(pasteHandler);
			if (handlersList.isEmpty()) {
				pasteHandlers.remove(pasteHandler.getPastingPointHolderType());
			}
		}
	}

	/**
	 * Implements PasteHandler lookup<br>
	 * Return the best PastHandler adapter to current paste operation<br>
	 * The lookup is performed relatively to the current selection and focused object, and the type of objects stored in Clipboard.
	 * 
	 * @param focusedObject
	 * @param globalSelection
	 * @param event
	 * @return
	 */
	public PasteHandler<?> getPasteHandler(FlexoObject focusedObject, List<FlexoObject> globalSelection) {

		// System.out.println("********* Requesting PasteHandler for " + focusedObject);

		// System.out.println("clipboard=");
		// System.out.println(clipboard.debug());

		/*System.out.println("Available paste handlers");
		for (Class c : pasteHandlers.keySet()) {
			System.out.println("* " + c);
			List<PasteHandler<?>> hList = pasteHandlers.get(c);
			for (PasteHandler<?> h : hList) {
				System.out.println("> " + h);
			}
		}*/

		// We will store all matching handlers in a map where the key is the pasting point holder type
		Map<Class<?>, List<PasteHandler<?>>> matchingHandlers = new HashMap<>();

		Clipboard masterClipboard = clipboard.getLeaderClipboard();

		PamelaModelFactory factory = masterClipboard.getModelFactory();

		// Iterate on all available handlers
		for (List<PasteHandler<?>> hList : pasteHandlers.values()) {
			for (PasteHandler<?> h : hList) {

				// System.out.println("Examining Paste handler: " + h + " pastingPointHolderType=" + h.getPastingPointHolderType());

				PastingContext potentialPastingContext = h.retrievePastingContext(focusedObject, globalSelection, getClipboard());

				boolean correctlyTyped = potentialPastingContext == null
						|| h.getPastingPointHolderType().isInstance(potentialPastingContext.getPastingPointHolder());
				if (correctlyTyped && h.isPastable(clipboard, potentialPastingContext)) {
					// System.out.println("OK, this is pastable...");

					// System.out.println("potentialPastingContext=" + potentialPastingContext);

					if (potentialPastingContext != null) {
						// System.out.println("Found PasteHandler " + h + " for " + focusedObject);
						List<PasteHandler<?>> l = matchingHandlers.get(h.getPastingPointHolderType());
						if (l == null) {
							l = new ArrayList<>();
							matchingHandlers.put(h.getPastingPointHolderType(), l);
						}
						l.add(h);
					}

				}
				else {
					// Sorry, cannot proceed to paste for pastingPointHolderEntity
					// System.out.println("Sorry, cannot paste for (handler=" + h + ")");
				}

			}
		}

		if (matchingHandlers.size() == 1) {
			// System.out.println("Found paste handler: " + matchingHandlers.values().iterator().next());
			List<PasteHandler<?>> l = matchingHandlers.values().iterator().next();
			PasteHandler<?> returned = getMostSpecializedPasteHander(l);
			if (returned != null) {
				return returned;
			}
		}
		else if (matchingHandlers.size() > 0) {
			// System.out.println("Found multiple paste handler:");
			/*for (List<PasteHandler<?>> hList : matchingHandlers.values()) {
				System.out.println("> " + hList);
			}*/

			Class<?> mostSpecializedClass = TypeUtils.getMostSpecializedClass(matchingHandlers.keySet());
			// System.out.println("Select the one for class: " + mostSpecializedClass);

			// Return most specialized one
			List<PasteHandler<?>> l = matchingHandlers.get(mostSpecializedClass);
			PasteHandler<?> returned = getMostSpecializedPasteHander(l);
			if (returned != null) {
				return returned;
			}
		}

		// No matches
		// Try with default one

		ModelEntity<?> pastingPointHolderEntity = factory.getModelContext().getModelEntity(focusedObject.getImplementedInterface());
		if (pastingPointHolderEntity != null) {
			// Entity was found in this PamelaModelFactory, we can proceed
			if (ProxyMethodHandler.isPastable(masterClipboard, pastingPointHolderEntity)) {
				Object potentialPastingContext = defaultPasteHandler.retrievePastingContext(focusedObject, globalSelection, getClipboard());
				if (potentialPastingContext != null) {
					// System.out.println("Returning default paste handler");
					return defaultPasteHandler;
				}
			}
		}

		return null;
	}

	private static PasteHandler<?> getMostSpecializedPasteHander(List<PasteHandler<?>> l) {
		if (l.size() == 0) {
			return null;
		}
		else if (l.size() == 1) {
			return l.get(0);
		}
		else if (l.size() > 1) {
			// In this case, this is not easy, we have to define a strategy
			// Lets' try to return the most specialized class
			Map<Class<?>, PasteHandler<?>> handlerClasses = new HashMap<>();
			for (PasteHandler<?> h : l) {
				handlerClasses.put(h.getClass(), h);
			}
			Class<?> mostSpecializedClass = TypeUtils.getMostSpecializedClass(handlerClasses.keySet());
			// logger.warning("Multiple paste handler found: " + l + " returning most specialized one: "
			// + handlerClasses.get(mostSpecializedClass));
			return handlerClasses.get(mostSpecializedClass);
		}
		return null;
	}

	@Override
	public void stop() {
		// Fixed memory leak
		FlexoObjectImpl.removeActionFromClass(copyActionType, FlexoObject.class);
		FlexoObjectImpl.removeActionFromClass(cutActionType, FlexoObject.class);
		FlexoObjectImpl.removeActionFromClass(pasteActionType, FlexoObject.class);
		FlexoObjectImpl.removeActionFromClass(selectAllActionType, FlexoObject.class);
		status = Status.Stopped;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	/**
	 * Return indicating general status of this FlexoService<br>
	 * This is the display value of 'service <service> status' as given in FML command-line interpreter
	 * 
	 * @return
	 */
	@Override
	public String getDisplayableStatus() {
		return getServiceName() + StringUtils.buildWhiteSpaceIndentation(30 - getServiceName().length()) + getStatus();
	}

	private List<ServiceOperation<?>> availableServiceOperations = null;

	/**
	 * Return collection of all available {@link ServiceOperation} available for this {@link FlexoService}
	 * 
	 * @return
	 */
	@Override
	public Collection<ServiceOperation<?>> getAvailableServiceOperations() {
		if (availableServiceOperations == null) {
			availableServiceOperations = new ArrayList<>();
			availableServiceOperations.add(HELP_ON_SERVICE);
			availableServiceOperations.add(DISPLAY_SERVICE_STATUS);
			availableServiceOperations.add(START_SERVICE);
			availableServiceOperations.add(STOP_SERVICE);
		}
		return availableServiceOperations;
	}

	@Override
	public void addToAvailableServiceOperations(ServiceOperation<?> serviceOperation) {
		getAvailableServiceOperations().add(serviceOperation);
	}

}
