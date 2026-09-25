/**
 *
 * Copyright (c) 2026, Openflexo
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
 *          of EPL 1.0, the licensors of this Program grant you additional
 *          permission to convey the resulting work. *
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

package org.openflexo.foundation.resource;

/**
 * An object of a resource's data that registers itself outside of that data - as a listener of objects of other models, or in a cache of
 * the metamodel - and must be unregistered when the resource is unloaded.<br>
 *
 * Releasing is not deleting: it runs no behaviour, changes no property and notifies no deletion. It only undoes the registrations the
 * object made, so that the unloaded objects can be garbage collected and stop reacting to changes of the models that stay loaded.
 *
 * A resource releases its resource data only: an object containing others releases them in turn.
 * 
 * @see PamelaResourceImpl#unloadResourceData(boolean)
 */
public interface ReleasableObject {

	/**
	 * Undo every registration this object made outside of its resource data
	 */
	public void release();

}
