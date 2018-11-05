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

import java.util.List;

public class JIRAProject extends JIRAObject<JIRAProject> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5973875816103952556L;

	private String name;
	private String description;
	private JIRAUser lead;
	private List<IssueType> issueTypes;
	private List<JIRAComponent> components;
	private List<JIRAVersion> versions;

	private String iconUrl;

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<IssueType> getIssueTypes() {
		return issueTypes;
	}

	public void setIssueTypes(List<IssueType> issuetypes) {
		this.issueTypes = issuetypes;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public JIRAUser getLead() {
		return lead;
	}

	public void setLead(JIRAUser lead) {
		this.lead = lead;
	}

	public List<JIRAVersion> getVersions() {
		return versions;
	}

	public void setVersions(List<JIRAVersion> versions) {
		this.versions = versions;
	}

	public JIRAVersion getLastReleasedVersion() {
		JIRAVersion lastReleased = null;
		for (JIRAVersion v : getVersions()) {
			if (v.isReleased()) {
				lastReleased = v;
			}
		}
		return lastReleased;
	}

	public JIRAVersion getNextUnreleasedVersion() {
		for (JIRAVersion v : getVersions()) {
			if (!v.isReleased()) {
				return v;
			}
		}
		return null;
	}

	public List<JIRAComponent> getComponents() {
		return components;
	}

	public void setComponents(List<JIRAComponent> components) {
		this.components = components;
	}

	@Override
	public String toString() {
		return "Project " + getName() + " " + getDescription() + " lead=" + getLead() + " issues=" + getIssueTypes() + " iconURL="
				+ getIconUrl() + " id=" + getId() + " key=" + getKey() + " self=" + getSelf();
	}
}
