/**
 *
 * Copyright (c) 2013-2014, Openflexo
 *
 * This file is part of Flexo-ui, a component of the software infrastructure
 * developed at Openflexo.
 *
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either
 * version 1.1 of the License, or any later version ), which is available at
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 *
 * You can redistribute it and/or modify under the terms of either of these licenses
 *
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 *
 */

package org.openflexo.br.github.model;

import java.beans.PropertyChangeSupport;
import java.util.List;

import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Represents a GitHub issue to be created via the Issues API.
 * Replaces JIRAIssue in the bug reporting workflow.
 *
 * Fields title, body, labels, milestone are serialized to JSON for the API POST.
 * Fields description, stacktrace, systemProperties are transient and used to
 * build the markdown body before submission.
 */
public class GitHubIssue implements HasPropertyChangeSupport {

	private transient final PropertyChangeSupport pcSupport = new PropertyChangeSupport(this);

	// Serialized to JSON for POST /repos/{owner}/{repo}/issues
	private String title;
	private String body;
	private List<String> labels;
	private Integer milestone;

	// Transient: used to compose the markdown body
	private transient String description;
	private transient String stacktrace;
	private transient String systemProperties;

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		String old = this.title;
		this.title = title;
		pcSupport.firePropertyChange("title", old, title);
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public Integer getMilestone() {
		return milestone;
	}

	public void setMilestone(Integer milestone) {
		Integer old = this.milestone;
		this.milestone = milestone;
		pcSupport.firePropertyChange("milestone", old, milestone);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		String old = this.description;
		this.description = description;
		pcSupport.firePropertyChange("description", old, description);
	}

	public String getStacktrace() {
		return stacktrace;
	}

	public void setStacktrace(String stacktrace) {
		this.stacktrace = stacktrace;
	}

	public String getSystemProperties() {
		return systemProperties;
	}

	public void setSystemProperties(String systemProperties) {
		this.systemProperties = systemProperties;
	}
}
