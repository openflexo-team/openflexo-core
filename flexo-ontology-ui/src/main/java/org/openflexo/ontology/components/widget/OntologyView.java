/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.ontology.components.widget;

import java.util.List;
import java.util.Vector;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.selection.SelectionListener;
import org.openflexo.view.SelectionSynchronizedModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * This class represent the module view for an ontology.<br>
 * Underlying representation is supported by FIBOntologyEditor implementation.
 * 
 * @author sylvain
 * 
 */
@SuppressWarnings("serial")
public abstract class OntologyView<T extends FlexoObject & IFlexoOntology<TA>, TA extends TechnologyAdapter<TA>>
		extends FIBOntologyEditor<TA> implements SelectionSynchronizedModuleView<T> {

	private final FlexoPerspective declaredPerspective;

	public OntologyView(T object, FlexoController controller, FlexoPerspective perspective, LocalizedDelegate locales) {
		super(object, controller, locales);
		declaredPerspective = perspective;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getOntology() {
		return (T) super.getOntology();
	}

	@Override
	public FlexoPerspective getPerspective() {
		return declaredPerspective;
	}

	@Override
	public void deleteView() {
		deleteModuleView();
	}

	@Override
	public void deleteModuleView() {
		super.deleteView();
		getFlexoController().removeModuleView(this);
	}

	@Override
	public List<SelectionListener> getSelectionListeners() {
		Vector<SelectionListener> reply = new Vector<>();
		reply.add(this);
		return reply;
	}

	@Override
	public void willHide() {
	}

	@Override
	public void willShow() {
	}

	@Override
	public void show(FlexoController controller, FlexoPerspective perspective) {
	}

	@Override
	public T getRepresentedObject() {
		return getOntology();
	}

}
