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

package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openflexo.antar.binding.BindingModel;
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;

/**
 * This is the {@link BindingModel} exposed by a {@link FetchRequestCondition}<br>
 * 
 * @author sylvain
 * 
 */
public class FetchRequestConditionBindingModel extends BindingModel implements PropertyChangeListener {

	private final FetchRequestConditionSelectedBindingVariable selectedBindingVariable;

	private final FetchRequestCondition fetchRequestCondition;

	public FetchRequestConditionBindingModel(FetchRequestCondition fetchRequestCondition) {
		super(fetchRequestCondition.getAction() != null ? fetchRequestCondition.getAction().getBindingModel() : null);

		this.fetchRequestCondition = fetchRequestCondition;
		selectedBindingVariable = new FetchRequestConditionSelectedBindingVariable(fetchRequestCondition);
		addToBindingVariables(selectedBindingVariable);

		if (fetchRequestCondition != null && fetchRequestCondition.getPropertyChangeSupport() != null) {
			fetchRequestCondition.getPropertyChangeSupport().addPropertyChangeListener(this);
		}

	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		if (fetchRequestCondition != null && fetchRequestCondition.getPropertyChangeSupport() != null) {
			fetchRequestCondition.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == fetchRequestCondition && evt.getPropertyName().equals(FetchRequestCondition.ACTION_KEY)) {
			setBaseBindingModel(fetchRequestCondition.getAction() != null ? fetchRequestCondition.getAction().getBindingModel() : null);
		}
	}

	public FetchRequestCondition getFetchRequestCondition() {
		return fetchRequestCondition;
	}

	public FetchRequestConditionSelectedBindingVariable getSelectedBindingVariable() {
		return selectedBindingVariable;
	}
}
