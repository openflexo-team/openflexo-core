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

package org.openflexo.foundation.fml.action;

import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.GetProperty;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.controlgraph.AbstractIterationAction;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.EmptyControlGraph;
import org.openflexo.foundation.fml.controlgraph.WhileAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;

/**
 * This action allows to sequentially add edition action
 * 
 * @author sylvain
 *
 */
public class CreateContextualEditionAction extends FlexoAction<CreateContextualEditionAction, FMLObject, FMLObject>
		implements TechnologySpecificFlexoAction<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(CreateContextualEditionAction.class.getPackage().getName());

	private enum CreateEditionActionContext {
		SEQUENTIAL, THEN, ELSE, ITERATION, WHILE, GET, SET
	}

	public static FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject> sequentialActionType = new FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject>(
			"add_edition_action", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		@Override
		public CreateContextualEditionAction makeNewAction(FMLObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateContextualEditionAction(this, focusedObject, CreateEditionActionContext.SEQUENTIAL, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return object instanceof FlexoBehaviour;
		}

		@Override
		public boolean isEnabledForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	public static FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject> thenActionType = new FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject>(
			"add_edition_action_in_then", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		@Override
		public CreateContextualEditionAction makeNewAction(FMLObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateContextualEditionAction(this, focusedObject, CreateEditionActionContext.THEN, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return object instanceof ConditionalAction;
		}

		@Override
		public boolean isEnabledForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	public static FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject> elseActionType = new FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject>(
			"add_edition_action_in_else", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		@Override
		public CreateContextualEditionAction makeNewAction(FMLObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateContextualEditionAction(this, focusedObject, CreateEditionActionContext.ELSE, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return object instanceof ConditionalAction;
		}

		@Override
		public boolean isEnabledForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	public static FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject> iterationActionType = new FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject>(
			"add_edition_action_in_iteration", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup,
			FlexoActionFactory.ADD_ACTION_TYPE) {

		@Override
		public CreateContextualEditionAction makeNewAction(FMLObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateContextualEditionAction(this, focusedObject, CreateEditionActionContext.ITERATION, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return object instanceof AbstractIterationAction;
		}

		@Override
		public boolean isEnabledForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	public static FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject> whileActionType = new FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject>(
			"add_edition_action_in_while", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup,
			FlexoActionFactory.ADD_ACTION_TYPE) {

		@Override
		public CreateContextualEditionAction makeNewAction(FMLObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateContextualEditionAction(this, focusedObject, CreateEditionActionContext.WHILE, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return object instanceof WhileAction;
		}

		@Override
		public boolean isEnabledForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	public static FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject> getActionType = new FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject>(
			"add_edition_action_in_get", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		@Override
		public CreateContextualEditionAction makeNewAction(FMLObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateContextualEditionAction(this, focusedObject, CreateEditionActionContext.GET, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return object instanceof GetProperty;
		}

		@Override
		public boolean isEnabledForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	public static FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject> setActionType = new FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject>(
			"add_edition_action_in_set", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		@Override
		public CreateContextualEditionAction makeNewAction(FMLObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateContextualEditionAction(this, focusedObject, CreateEditionActionContext.SET, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return object instanceof GetSetProperty;
		}

		@Override
		public boolean isEnabledForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateContextualEditionAction.sequentialActionType, FlexoBehaviour.class);
		FlexoObjectImpl.addActionForClass(CreateContextualEditionAction.thenActionType, ConditionalAction.class);
		FlexoObjectImpl.addActionForClass(CreateContextualEditionAction.elseActionType, ConditionalAction.class);
		FlexoObjectImpl.addActionForClass(CreateContextualEditionAction.iterationActionType, AbstractIterationAction.class);
		FlexoObjectImpl.addActionForClass(CreateContextualEditionAction.whileActionType, WhileAction.class);
		FlexoObjectImpl.addActionForClass(CreateContextualEditionAction.getActionType, GetProperty.class);
		FlexoObjectImpl.addActionForClass(CreateContextualEditionAction.setActionType, GetSetProperty.class);
	}

	private CreateContextualEditionAction(FlexoActionFactory<CreateContextualEditionAction, FMLObject, FMLObject> factory,
			FMLObject focusedObject, CreateEditionActionContext actionContext, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(factory, focusedObject, globalSelection, editor);
		this.actionContext = actionContext;
	}

	@Override
	public Class<? extends FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	private EditionAction newAction;
	private CreateEditionActionContext actionContext;

	@Override
	protected void doAction(Object context) {
		logger.info("Add edition action with actionContext " + actionContext);
		switch (actionContext) {
			case SEQUENTIAL:
				if (getFocusedObject() instanceof FlexoBehaviour) {
					FlexoBehaviour behaviour = (FlexoBehaviour) getFocusedObject();
					if (behaviour.getControlGraph() == null) {
						EmptyControlGraph cg = behaviour.getFMLModelFactory().newEmptyControlGraph();
						behaviour.setControlGraph(cg);
					}
					CreateEditionAction createEditionAction = CreateEditionAction.actionType
							.makeNewEmbeddedAction(behaviour.getControlGraph(), null, this);
					createEditionAction.setForceExecuteConfirmationPanel(true);
					createEditionAction.doAction();
					newAction = createEditionAction.getNewEditionAction();
				}
				break;
			case THEN:
				if (getFocusedObject() instanceof ConditionalAction) {
					ConditionalAction conditionalAction = (ConditionalAction) getFocusedObject();
					if (conditionalAction.getThenControlGraph() == null) {
						EmptyControlGraph cg = conditionalAction.getFMLModelFactory().newEmptyControlGraph();
						conditionalAction.setThenControlGraph(cg);
					}
					CreateEditionAction createEditionAction = CreateEditionAction.actionType
							.makeNewEmbeddedAction(conditionalAction.getThenControlGraph(), null, this);
					createEditionAction.setForceExecuteConfirmationPanel(true);
					createEditionAction.doAction();
					newAction = createEditionAction.getNewEditionAction();
				}
				break;
			case ELSE:
				if (getFocusedObject() instanceof ConditionalAction) {
					ConditionalAction conditionalAction = (ConditionalAction) getFocusedObject();
					if (conditionalAction.getElseControlGraph() == null) {
						EmptyControlGraph cg = conditionalAction.getFMLModelFactory().newEmptyControlGraph();
						conditionalAction.setElseControlGraph(cg);
					}
					CreateEditionAction createEditionAction = CreateEditionAction.actionType
							.makeNewEmbeddedAction(conditionalAction.getElseControlGraph(), null, this);
					createEditionAction.setForceExecuteConfirmationPanel(true);
					createEditionAction.doAction();
					newAction = createEditionAction.getNewEditionAction();
				}
				break;
			case ITERATION:
				if (getFocusedObject() instanceof AbstractIterationAction) {
					AbstractIterationAction iterationAction = (AbstractIterationAction) getFocusedObject();
					if (iterationAction.getControlGraph() == null) {
						EmptyControlGraph cg = iterationAction.getFMLModelFactory().newEmptyControlGraph();
						iterationAction.setControlGraph(cg);
					}
					CreateEditionAction createEditionAction = CreateEditionAction.actionType
							.makeNewEmbeddedAction(iterationAction.getControlGraph(), null, this);
					createEditionAction.setForceExecuteConfirmationPanel(true);
					createEditionAction.doAction();
					newAction = createEditionAction.getNewEditionAction();
				}
				break;
			case WHILE:
				if (getFocusedObject() instanceof WhileAction) {
					WhileAction whileAction = (WhileAction) getFocusedObject();
					if (whileAction.getControlGraph() == null) {
						EmptyControlGraph cg = whileAction.getFMLModelFactory().newEmptyControlGraph();
						whileAction.setControlGraph(cg);
					}
					CreateEditionAction createEditionAction = CreateEditionAction.actionType
							.makeNewEmbeddedAction(whileAction.getControlGraph(), null, this);
					createEditionAction.setForceExecuteConfirmationPanel(true);
					createEditionAction.doAction();
					newAction = createEditionAction.getNewEditionAction();
				}
				break;
			case GET:
				if (getFocusedObject() instanceof GetProperty) {
					GetProperty<?> getProperty = (GetProperty<?>) getFocusedObject();
					if (getProperty.getGetControlGraph() == null) {
						EmptyControlGraph cg = getProperty.getFMLModelFactory().newEmptyControlGraph();
						getProperty.setGetControlGraph(cg);
					}
					CreateEditionAction createEditionAction = CreateEditionAction.actionType
							.makeNewEmbeddedAction(getProperty.getGetControlGraph(), null, this);
					createEditionAction.setForceExecuteConfirmationPanel(true);
					createEditionAction.doAction();
					newAction = createEditionAction.getNewEditionAction();
				}
				break;
			case SET:
				if (getFocusedObject() instanceof GetSetProperty) {
					GetSetProperty<?> setProperty = (GetSetProperty<?>) getFocusedObject();
					if (setProperty.getSetControlGraph() == null) {
						EmptyControlGraph cg = setProperty.getFMLModelFactory().newEmptyControlGraph();
						setProperty.setSetControlGraph(cg);
					}
					CreateEditionAction createEditionAction = CreateEditionAction.actionType
							.makeNewEmbeddedAction(setProperty.getSetControlGraph(), null, this);
					createEditionAction.setForceExecuteConfirmationPanel(true);
					createEditionAction.doAction();
					newAction = createEditionAction.getNewEditionAction();
				}
				break;
			default:
				break;
		}
	}

	public EditionAction getNewEditionAction() {
		return newAction;
	}

}
