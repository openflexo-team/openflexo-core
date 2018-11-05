/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.ws.jira.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JIRAIssue extends JIRAObject<JIRAIssue> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5078675682772647794L;

	private JIRAProject project;
	private String summary;
	private String description;
	private IssueType issuetype;
	private List<JIRAVersion> versions;
	private JIRAPriority priority;
	private List<JIRAComponent> components;
	private transient boolean membersHaveBeenReplaced;

	public JIRAProject getProject() {
		return project;
	}

	public void setProject(JIRAProject project) {
		this.project = project;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		if ((summary == null && this.summary != null) || (summary != null && !summary.equals(this.summary))) {
			String oldValue = this.summary;
			this.summary = summary;
			getPropertyChangeSupport().firePropertyChange("summary", oldValue, summary);
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if ((description == null && this.description != null) || (description != null && !description.equals(this.description))) {
			String oldValue = this.description;
			this.description = description;
			getPropertyChangeSupport().firePropertyChange("description", oldValue, description);
		}
	}

	public IssueType getIssuetype() {
		return issuetype;
	}

	public void setIssuetype(IssueType issuetype) {
		if ((issuetype == null && this.issuetype != null) || (issuetype != null && !issuetype.equals(this.issuetype))) {
			IssueType oldValue = this.issuetype;
			this.issuetype = issuetype;
			getPropertyChangeSupport().firePropertyChange("issuetype", oldValue, issuetype);
		}
	}

	public List<JIRAVersion> getVersions() {
		return versions;
	}

	public void setVersions(List<JIRAVersion> versions) {
		this.versions = versions;
	}

	public JIRAVersion getVersion() {
		if (versions != null && versions.size() > 0) {
			return versions.get(0);
		}
		else {
			return null;
		}
	}

	public void setVersion(JIRAVersion version) {
		JIRAVersion oldVersion = getVersion();
		if (oldVersion != version) {
			if (version != null) {
				setVersions(Arrays.asList(version));
			}
			else {
				setVersions(null);
			}
			getPropertyChangeSupport().firePropertyChange("version", oldVersion, version);
		}
	}

	// TODO: fix this
	// private JIRAVersion fixVersion;

	public JIRAVersion getFixVersion() {
		// return fixVersion;
		return null;
	}

	public void setFixVersion(JIRAVersion fixVersion) {
		// this.fixVersion = fixVersion;
	}

	public List<JIRAComponent> getComponents() {
		return components;
	}

	public void setComponents(List<JIRAComponent> components) {
		this.components = components;
	}

	public JIRAComponent getComponent() {
		if (components != null && components.size() > 0) {
			return components.get(0);
		}
		else {
			return null;
		}
	}

	public void setComponent(JIRAComponent component) {
		if (component != null) {
			setComponents(Arrays.asList(component));
		}
		else {
			setComponents(null);
		}
	}

	public JIRAPriority getPriority() {
		return priority;
	}

	public void setPriority(JIRAPriority priority) {
		this.priority = priority;
	}

	public <J extends JIRAObject<J>> void replaceMembersByIdentityMembers() {
		Class<?> klass = getClass();
		while (klass != null) {
			for (Field field : klass.getDeclaredFields()) {
				if (JIRAObject.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					try {
						J object = (J) field.get(this);
						if (object != null) {
							field.set(this, object.getAsIdentityObject());
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					}
				}
				else if (List.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType
						&& ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0] instanceof Class && JIRAObject.class
								.isAssignableFrom((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0])) {
					field.setAccessible(true);
					try {
						List<J> list = (List<J>) field.get(this);
						if (list != null) {
							List<J> newList = new ArrayList<>();
							for (J j : list) {
								newList.add(j.getAsIdentityObject());
							}
							field.set(this, newList);
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					}
				}
			}
			klass = klass.getSuperclass();
		}
		membersHaveBeenReplaced = true;
	}

	public <J extends JIRAObject<J>> void replaceMembersByOriginalMembers() {
		if (!membersHaveBeenReplaced) {
			return;
		}

		Class<?> klass = getClass();
		while (klass != null) {
			for (Field field : klass.getDeclaredFields()) {
				if (JIRAObject.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					try {
						J object = (J) field.get(this);
						if (object != null) {
							field.set(this, object.restoreObject());
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				else if (List.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType
						&& JIRAObject.class
								.isAssignableFrom(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].getClass())) {
					field.setAccessible(true);
					try {
						List<J> list = (List<J>) field.get(this);
						if (list != null) {
							List<J> newList = new ArrayList<>();
							for (J j : list) {
								newList.add(j.restoreObject());
							}
							field.set(this, newList);
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			klass = klass.getSuperclass();
		}
		membersHaveBeenReplaced = false;
	}

	public void makeValid() {
		if (getIssuetype() != null && getIssuetype().getFields() != null) {
			Class<?> klass = getClass();
			while (klass != HashMap.class) {
				for (Field field : klass.getDeclaredFields()) {
					if (!getIssuetype().getFields().containsKey(field.getName())) {
						if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())
								|| Modifier.isTransient(field.getModifiers())) {
							continue;
						}
						field.setAccessible(true);
						try {
							field.set(this, null);
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
				klass = klass.getSuperclass();
			}
			for (Map.Entry<String, Object> e : new HashMap<>(this).entrySet()) {
				if (!getIssuetype().getFields().containsKey(e.getKey())) {
					this.remove(e.getKey());
				}
			}
		}
	}

	public String getStacktrace() {
		return (String) get(IssueType.STACKTRACE_FIELD);
	}

	public void setStacktrace(String stacktrace) {
		put(IssueType.STACKTRACE_FIELD, stacktrace);
	}

	public String getSystemProperties() {
		return (String) get(IssueType.SYSTEM_PROPERTIES_FIELD);
	}

	public void setSystemProperties(String systemProperties) {
		put(IssueType.SYSTEM_PROPERTIES_FIELD, systemProperties);
	}

}
