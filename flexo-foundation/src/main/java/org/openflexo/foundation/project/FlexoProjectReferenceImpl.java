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

package org.openflexo.foundation.project;

import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProjectObject.FlexoProjectObjectImpl;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.Setter;

public abstract class FlexoProjectReferenceImpl extends FlexoProjectObjectImpl implements FlexoProjectReference, PropertyChangeListener {

	private static final Logger logger = FlexoLogger.getLogger(FlexoObjectReference.class.getPackage().getName());

	private String _projectURI = null;

	public static interface ReferenceOwner {

		public void projectDeleted(FlexoProjectReference reference);

	}

	/**
	 * Returns URI of referenced project
	 * 
	 * @return
	 */
	@Override
	public String getURI() {
		if (getReferencedProject() != null) {
			return getReferencedProject().getProjectURI();
		}
		return _projectURI;
	}

	/**
	 * Sets
	 * 
	 * @param uri
	 */
	@Override
	@Setter(value = URI)
	public void setURI(String uri) {
		_projectURI = uri;
	}

	/*@Override
	public FlexoProject getReferredProject(boolean force) {
		FlexoProject project = getInternalReferredProject();
		if (project == null && getReferencedProject() != null) {
			project = getReferencedProject().loadProjectReference(this, !force);
			if (project != null) {
				setReferredProject(project);
			}
		}
		return project;
	}
	
	private FlexoProject getInternalReferredProject() {
		return (FlexoProject) performSuperGetter(REFERRED_PROJECT);
	}
	
	@Override
	public void setReferredProject(FlexoProject project) {
		if (project != null) {
			String knownURI = (String) performSuperGetter(URI);
			if (knownURI != null && !knownURI.equals(project.getURI())) {
				throw new RuntimeException("Expecting a project with URI " + knownURI + " but received a project with URI "
						+ project.getURI());
			}
		}
		performSuperSetter(REFERRED_PROJECT, project);
	}
	
	private void firePropertyChange(String name, Object old, Object value) {
		if (old == null && value == old) {
			return;
		}
		if (old != null && old.equals(value)) {
			return;
		}
		setModified(true);
		getPropertyChangeSupport().firePropertyChange(name, old, value);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == getReferredProject()) {
			setModified(true);
		}
	}
	
	@Override
	public boolean delete(Object... context) {
		return performSuperDelete(context);
	}
	
	@Override
	public String toString() {
		return "FlexoProjectReference " + getName() + " " + getRevision() + " " + getURI();
	}
	
	@Override
	public String getName() {
		if (getReferredProject() != null) {
			return getReferredProject().getDisplayName();
		}
		return getInternalName();
	}
	
	private String getInternalName() {
		return (String) performSuperGetter(NAME);
	}
	
	@Override
	public String getURI() {
		if (getReferredProject() != null) {
			return getReferredProject().getProjectURI();
		}
		return (String) performSuperGetter(URI);
	}
	
	@Override
	public FlexoVersion getVersion() {
		if (getReferredProject() != null) {
			return getReferredProject().getVersion();
		}
		return (FlexoVersion) performSuperGetter(VERSION);
	}
	
	@Override
	public Long getRevision() {
		if (getReferredProject() != null) {
			return getReferredProject().getRevision();
		}
		return getInternalRevision();
	}
	
	private Long getInternalRevision() {
		return (Long) performSuperGetter(REVISION);
	}
	
	public File getFile() {
		if (getReferredProject() != null) {
			return getReferredProject().getProjectDirectory();
		} else {
			return ((FileIODelegate) getIODelegate()).getFile();
		}
	
		// return (File) performSuperGetter(FILE);
	}
	
	@Override
	public Class<FlexoProject> getResourceDataClass() {
		return FlexoProject.class;
	}
	*/
}
