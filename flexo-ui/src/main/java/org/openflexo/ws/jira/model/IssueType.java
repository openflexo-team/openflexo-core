package org.openflexo.ws.jira.model;

import java.util.Map;

public class IssueType extends JIRAObject<IssueType> {

	public static final String STACKTRACE_FIELD = "customfield_10000";
	public static final String SYSTEM_PROPERTIES_FIELD = "customfield_10001";

	/**
	 * 
	 */
	private static final long serialVersionUID = -4127968691089024145L;

	private String name;

	private Boolean subtask;

	private String iconUrl;

	private String description;

	private Map<String, JIRAField> fields;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean isSubtask() {
		return subtask;
	}

	public void setSubtask(Boolean subtask) {
		this.subtask = subtask;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, JIRAField> getFields() {
		return fields;
	}

	public void setFields(Map<String, JIRAField> fields) {
		this.fields = fields;
	}

	public JIRAField<JIRAPriority> getPriorityField() {
		if (getFields() != null) {
			try {
				JIRAField<?> jiraField = getFields().get("priority");
				if (jiraField != null) {
					return jiraField.mutateToFieldOfType(JIRAPriority.class);
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public JIRAField<JIRAVersion> getVersionField() {
		if (getFields() != null) {
			try {
				JIRAField<?> jiraField = getFields().get("versions");
				if (jiraField != null) {
					return jiraField.mutateToFieldOfType(JIRAVersion.class);
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public JIRAField<JIRAComponent> getComponentField() {
		if (getFields() != null) {
			try {
				JIRAField<?> jiraField = getFields().get("components");
				if (jiraField != null) {
					return jiraField.mutateToFieldOfType(JIRAComponent.class);
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public boolean hasStacktraceField() {
		return getFields() != null && getFields().get(STACKTRACE_FIELD) != null;
	}

	public boolean hasSystemPropertiesField() {
		return getFields() != null && getFields().get(SYSTEM_PROPERTIES_FIELD) != null;
	}

	@Override
	public String toString() {
		return getName() + " " + super.toString();
	}
}
