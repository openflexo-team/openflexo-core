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
import java.util.Collections;
import java.util.List;

import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Represents a GitHub repository in the openflexo-team organization.
 * Replaces JIRAProject in the bug reporting workflow.
 */
public class GitHubRepository implements HasPropertyChangeSupport {

	private final PropertyChangeSupport pcSupport = new PropertyChangeSupport(this);

	// Fields deserialized from GitHub API /orgs/openflexo-team/repos
	private long id;
	private String name;
	private String full_name;
	private String description;
	private String html_url;
	private boolean archived;
	private boolean fork;
	private String default_branch;

	// Loaded separately via listMilestones / listLabels
	private transient List<GitHubMilestone> milestones;
	private transient List<GitHubLabel> labels;

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullName() {
		return full_name;
	}

	public void setFullName(String fullName) {
		this.full_name = fullName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHtmlUrl() {
		return html_url;
	}

	public void setHtmlUrl(String htmlUrl) {
		this.html_url = htmlUrl;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public boolean isFork() {
		return fork;
	}

	public void setFork(boolean fork) {
		this.fork = fork;
	}

	public String getDefaultBranch() {
		return default_branch;
	}

	public void setDefaultBranch(String defaultBranch) {
		this.default_branch = defaultBranch;
	}

	public List<GitHubMilestone> getMilestones() {
		return milestones != null ? milestones : Collections.emptyList();
	}

	public void setMilestones(List<GitHubMilestone> milestones) {
		this.milestones = milestones;
	}

	public List<GitHubLabel> getLabels() {
		return labels != null ? labels : Collections.emptyList();
	}

	public void setLabels(List<GitHubLabel> labels) {
		this.labels = labels;
	}

	@Override
	public String toString() {
		return name != null ? name : full_name;
	}
}
