/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.foundation.ontology;

import java.util.logging.Logger;

import org.openflexo.foundation.DefaultFlexoObject;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.localization.LocalizedDelegate;

/**
 * This is the default abstract implementation of all objects encoding models or metamodels conform to FlexoOntology layer
 * 
 * @author sylvain
 * 
 */
public abstract class FlexoOntologyObjectImpl<TA extends TechnologyAdapter> extends DefaultFlexoObject implements IFlexoOntologyObject<TA> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FlexoOntologyObjectImpl.class.getPackage().getName());

	public IFlexoOntology<TA> getOntology() {
		return getFlexoOntology();
	}

	/**
	 * Name of Object.
	 * 
	 * @return
	 */
	@Override
	public abstract String getName();

	public abstract IFlexoOntology<TA> getFlexoOntology();

	@Override
	public abstract String getDisplayableDescription();

	public final boolean isOntology() {
		return this instanceof IFlexoOntology;
	}

	public final boolean isOntologyClass() {
		return this instanceof IFlexoOntologyClass;
	}

	public final boolean isOntologyIndividual() {
		return this instanceof IFlexoOntologyIndividual;
	}

	public final boolean isOntologyObjectProperty() {
		return this instanceof IFlexoOntologyObjectProperty;
	}

	public final boolean isOntologyDataProperty() {
		return this instanceof IFlexoOntologyDataProperty;
	}

	@Override
	public LocalizedDelegate getLocales() {
		if (getTechnologyAdapter() != null) {
			return getTechnologyAdapter().getLocales();
		}
		return super.getLocales();
	}

}
