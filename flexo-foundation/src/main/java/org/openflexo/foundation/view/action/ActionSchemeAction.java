/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.view.action;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.view.VirtualModelInstanceObject;
import org.openflexo.foundation.viewpoint.AbstractActionScheme;
import org.openflexo.foundation.viewpoint.FlexoBehaviour;
import org.openflexo.foundation.viewpoint.binding.PatternRoleBindingVariable;

public class ActionSchemeAction extends FlexoBehaviourAction<ActionSchemeAction, AbstractActionScheme, FlexoConceptInstance> {

    private static final Logger          logger = Logger.getLogger(ActionSchemeAction.class.getPackage().getName());

    private final ActionSchemeActionType actionType;

    public ActionSchemeAction(ActionSchemeActionType actionType, FlexoConceptInstance focusedObject,
            Vector<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
        super(actionType, focusedObject, globalSelection, editor);
        this.actionType = actionType;
    }

    public AbstractActionScheme getActionScheme() {
        if (actionType != null) {
            return actionType.getActionScheme();
        }
        return null;
    }

    /**
     * Return the {@link FlexoConceptInstance} on which this
     * {@link FlexoBehaviour} is applied.<br>
     * 
     * @return
     */
    @Override
    public FlexoConceptInstance getFlexoConceptInstance() {
        if (actionType != null) {
            return actionType.getFlexoConceptInstance();
        }
        return null;
    }

    @Override
    public AbstractActionScheme getEditionScheme() {
        return getActionScheme();
    }

    @Override
    protected void doAction(Object context) throws FlexoException {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Perform action " + actionType);
        }

        if (getActionScheme() != null && getActionScheme().evaluateCondition(actionType.getFlexoConceptInstance())) {
            applyEditionActions();
        }
    }

    @Override
    public VirtualModelInstance retrieveVirtualModelInstance() {
        /*if (getFocusedObject() instanceof DiagramElement<?>) {
        	return ((DiagramElement<?>) getFocusedObject()).getDiagram();
        }*/
        if (getFlexoConceptInstance() instanceof VirtualModelInstance) {
            return (VirtualModelInstance) getFlexoConceptInstance();
        }
        if (getFlexoConceptInstance() != null) {
            return getFlexoConceptInstance().getVirtualModelInstance();
        }
        /*if (getFocusedObject() instanceof DiagramElement<?>) {
        	return ((DiagramElement<?>) getFocusedObject()).getDiagram();
        }*/
        return null;
    }

    @Override
    public Object getValue(BindingVariable variable) {
        if (variable instanceof PatternRoleBindingVariable) {
            return getFlexoConceptInstance().getFlexoActor(((PatternRoleBindingVariable) variable).getFlexoRole());
        }
        else if (variable.getVariableName().equals(FlexoBehaviour.FLEXO_BEHAVIOUR_INSTANCE)) {
            return getFlexoConceptInstance();
        }
        return super.getValue(variable);
    }

    @Override
    public void setValue(Object value, BindingVariable variable) {
        if (variable instanceof PatternRoleBindingVariable) {
            getFlexoConceptInstance().setFlexoActor(value, ((PatternRoleBindingVariable) variable).getFlexoRole());
            return;
        }
        super.setValue(value, variable);
    }

}
