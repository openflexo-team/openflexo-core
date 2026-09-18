/**
 * 
 * Copyright (c) 2014-2026, Openflexo
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

package org.openflexo.foundation.fml.rm;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.resource.FlexoResourceImpl;
import org.openflexo.foundation.resource.FileWritingLock;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.StreamIODelegate;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.FIBLibrary;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.utils.FIBInspector;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;

/**
 * Default implementation of {@link FIBComponentResource}.
 *
 * <p>
 * Loading goes through the GINA {@link FIBLibrary} rather than through a PAMELA deserialization of our own: the library is what gives the
 * component its <code>getFIBLibrary()</code> back-reference, which a <code>FIBReferencedComponent</code> needs in order to resolve what it
 * points at.
 *
 * @author sylvain
 */
public abstract class FIBComponentResourceImpl extends FlexoResourceImpl<FMLFIBComponent> implements FIBComponentResource {

	private static final Logger logger = Logger.getLogger(FIBComponentResourceImpl.class.getPackage().getName());

	@Override
	public FMLTechnologyAdapter getTechnologyAdapter() {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		}
		return null;
	}

	@Override
	public Class<FMLFIBComponent> getResourceDataClass() {
		return FMLFIBComponent.class;
	}

	/**
	 * Notify that this resource now holds data, whichever way it got it.
	 *
	 * <p>
	 * {@link FlexoResourceImpl#setResourceData} tells the listeners of <code>isLoaded</code>, but not the rest of what
	 * {@link #notifyResourceLoaded()} reaches - among which <code>component</code>, below. And this resource gets its data here on BOTH
	 * paths: loaded from disk (see {@link #loadResourceData()}), and created with empty contents by
	 * <code>FlexoResourceFactory.createEmptyContents()</code>, which notifies nothing itself.
	 */
	@Override
	public void setResourceData(FMLFIBComponent resourceData) {
		super.setResourceData(resourceData);
		if (resourceData != null) {
			notifyResourceLoaded();
		}
	}

	/**
	 * On top of what {@link FlexoResourceImpl#notifyResourceLoaded()} tells - <code>isLoaded</code> among others - the property the
	 * browsers read a component through, the same way <code>CompilationUnitResourceImpl</code> fires <code>compilationUnit</code>.
	 */
	@Override
	public void notifyResourceLoaded() {
		super.notifyResourceLoaded();
		getPropertyChangeSupport().firePropertyChange("component", null, getLoadedResourceData());
	}

	@Override
	public FIBComponent getComponent() {
		try {
			FMLFIBComponent resourceData = getResourceData();
			return resourceData != null ? resourceData.getComponent() : null;
		} catch (FlexoException | ResourceLoadingCancelledException | java.io.FileNotFoundException e) {
			logger.log(Level.WARNING, "Could not load component " + getURI(), e);
			return null;
		}
	}

	@Override
	public FMLFIBComponent loadResourceData() throws FlexoException {

		Resource artefact = getIODelegate() != null ? getIODelegate().getSerializationArtefactAsResource() : null;
		if (artefact == null) {
			throw new FlexoException("No serialization artefact for " + getURI());
		}

		FIBComponent component = getFIBLibrary().retrieveFIBComponent(artefact, false, makeModelFactory());
		if (component == null) {
			// retrieveFIBComponent only prints the stack trace of what went wrong, and answers null
			throw new FlexoException("Could not load GINA component " + getURI());
		}

		FMLFIBComponent returned = FMLFIBComponent.newInstance(component);
		returned.setResource(this);
		setResourceData(returned);

		return returned;
	}

	@Override
	public FlexoConcept getDrivingConcept() {

		if (!(getContainer() instanceof CompilationUnitResource)) {
			return null;
		}

		VirtualModel virtualModel = ((CompilationUnitResource) getContainer()).getCompilationUnit().getVirtualModel();
		if (virtualModel == null) {
			return null;
		}

		// The VirtualModel is itself a FlexoConcept, and Xxx.fml/Xxx.fib is its own component
		if (isDrivenBy(virtualModel)) {
			return virtualModel;
		}
		for (FlexoConcept concept : virtualModel.getFlexoConcepts()) {
			if (isDrivenBy(concept)) {
				return concept;
			}
		}
		return null;
	}

	/**
	 * Every variant counts, not just the default one: a concept may drive this component under a name such as
	 * <code>compact</code>, and the editor still has to know which concept types its <code>data</code>.
	 */
	private boolean isDrivenBy(FlexoConcept concept) {

		for (String variant : concept.getUIComponentVariants()) {
			if (this == concept.getUIComponentFlexoResource(variant)) {
				return true;
			}
		}
		for (String variant : concept.getInspectorComponentVariants()) {
			if (this == concept.getInspectorComponentFlexoResource(variant)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void save() throws SaveResourceException {

		if (getComponent() == null || getIODelegate() == null) {
			return;
		}

		// The write MUST be bracketed by the IO delegate's protocol. willWriteOnDisk() is what tells the resource center
		// that this file is being written BY the platform, so its DirectoryWatcher ignores it. Writing behind that
		// protocol makes the watcher discover the file as if a stranger had dropped it there, and register a SECOND
		// resource for the same artefact - which shows up as a duplicate in the browsers and throws
		// DuplicateURIException.
		FileWritingLock lock = getIODelegate() instanceof StreamIODelegate ? ((StreamIODelegate<?>) getIODelegate()).willWriteOnDisk()
				: null;

		try {
			// The SAME factory the component was read with. Without it FIBLibrary builds one knowing the entities reachable from
			// FIBComponent only, and a FIBInspector - which is not one of them - comes back out as its upper entity FIBPanel, under a
			// p:modelEntity attribute standing in for the name that factory could not resolve. Reading and writing a component are
			// symmetric operations and take the same factory.
			getFIBLibrary().save(getComponent(), getIODelegate().getSerializationArtefactAsResource(), makeModelFactory());
		} catch (FlexoException e) {
			throw new SaveResourceException(getIODelegate(), e);
		} finally {
			if (lock != null) {
				((StreamIODelegate<?>) getIODelegate()).hasWrittenOnDisk(lock);
			}
		}

		notifyResourceSaved();
	}

	/**
	 * The application-wide library, which the running application has already created with the technology adapter service as custom type
	 * manager. Outside an application (a headless test) this creates one configured the same way.
	 */
	private FIBLibrary getFIBLibrary() {
		return ApplicationFIBLibraryImpl.instance(getServiceManager().getTechnologyAdapterService());
	}

	/**
	 * Two things here fail in ways that point nowhere near the cause:
	 * <ul>
	 * <li>the custom type manager MUST be the technology adapter service. A component driven by FML declares FML types
	 * (<code>VirtualModelInstanceType&lt;…&gt;</code>, <code>FlexoConceptInstanceType&lt;…&gt;</code>) on its variables and browser
	 * iterators; without those factories deserialization dies with <i>"No custom type factories found while deserializing …"</i>;</li>
	 * <li>{@link FIBInspector} must be declared, or a <code>.inspector</code> deserializes as a plain panel and never merges into the
	 * inspector of the inspected class.</li>
	 * </ul>
	 * The factory is rooted at the container so a relative reference the component carries resolves inside the <code>Xxx.fml/</code>
	 * directory.
	 */
	@Override
	public FIBModelFactory makeModelFactory() throws FlexoException {

		Resource artefact = getIODelegate() != null ? getIODelegate().getSerializationArtefactAsResource() : null;
		if (artefact == null) {
			throw new FlexoException("No serialization artefact for " + getURI());
		}

		try {
			return new FIBModelFactory(artefact.getContainer(), getServiceManager().getTechnologyAdapterService(), FIBInspector.class);
		} catch (ModelDefinitionException e) {
			throw new FlexoException("Could not build a model factory for " + getURI(), e);
		}
	}
}
