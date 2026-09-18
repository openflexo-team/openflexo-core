/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.rt;

import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;

/**
 * The FML@RT execution engine, obtained from the editor (see {@link RunTimeEvaluationContext#getFMLRunTimeEngine()}); the default editor
 * uses a {@link SynchronousFMLRunTimeEngine}, which executes FML in the calling thread.
 * <p>
 * The engine:
 * <ul>
 * <li>registers the virtual model instances being executed ({@link #addToExecutionContext(VirtualModelInstance, RunTimeEvaluationContext)}),
 * which starts the listening of the event listeners ({@code listen Event from expression}) declared by their virtual model and its
 * concepts</li>
 * <li>executes behaviours ({@link #execute(FlexoBehaviourAction)})</li>
 * <li>dispatches the fired events to the listeners subscribed to them ({@link #receivedEvent(FlexoEventInstance)})</li>
 * </ul>
 *
 * @author sylvain
 *
 */
public interface FMLRunTimeEngine {

	public void addToExecutionContext(VirtualModelInstance<?, ?> vmi, RunTimeEvaluationContext evaluationContext);

	public void removeFromExecutionContext(VirtualModelInstance<?, ?> vmi, RunTimeEvaluationContext evaluationContext);

	public void execute(FlexoBehaviourAction<?, ?, ?> behaviourExecution);

	public void receivedEvent(FlexoEventInstance event);

}
