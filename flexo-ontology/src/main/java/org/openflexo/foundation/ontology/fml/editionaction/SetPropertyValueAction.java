/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.ontology.fml.editionaction;

import java.lang.reflect.Type;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.ontology.IFlexoOntologyConcept;
import org.openflexo.foundation.ontology.IFlexoOntologyStructuralProperty;
import org.openflexo.foundation.technologyadapter.ModelSlot;

/**
 * Interface implemented by all {@link EditionAction} setting a value to an object (interface used to share GUI)
 * 
 * @author sylvain
 *
 * @param <ST>
 *            type of "property value" (the object reifying the statement)
 * @param <S>
 *            type of the subject
 * @param <P>
 *            type of the property
 */
public interface SetPropertyValueAction<ST, S extends IFlexoOntologyConcept<?>, P extends IFlexoOntologyStructuralProperty<?>> {

	public Type getSubjectType();

	public DataBinding<S> getSubject();

	public void setSubject(DataBinding<S> subject);

	public P getProperty();

	public void setProperty(P aProperty);

	@Deprecated
	public ModelSlot getInferedModelSlot();

	// public DataBinding<? super T> getAssignation();

	// public DataBinding<Boolean> getConditional();

}
