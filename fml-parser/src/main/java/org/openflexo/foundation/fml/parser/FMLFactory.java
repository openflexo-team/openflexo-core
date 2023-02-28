/**
 * 
 * Copyright (c) 2019, Openflexo
 * 
 * This file is part of FML-parser, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.parser;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.parser.node.TCidentifier;
import org.openflexo.foundation.fml.parser.node.TUidentifier;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * 
 * This factory is responsible for handling FML data structures exposed by a {@link TechnologyAdapter}:
 * <ul>
 * <li>{@link ModelSlot}</li>
 * <li>{@link FlexoRole}</li>
 * <li>{@link FlexoBehaviour}</li>
 * <li>{@link EditionAction}</li>
 * </ul>
 * 
 * @author sylvain
 * 
 */
public class FMLFactory extends SemanticsAnalyzerFactory {

	private static final Logger logger = Logger.getLogger(FMLFactory.class.getPackage().getName());

	public FMLFactory(FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(analyzer);
	}

	public Class<? extends FlexoRole<?>> getRoleClass(TUidentifier roleIdentifier) {
		if (roleIdentifier.getText().equals(AbstractFMLTypingSpace.MODEL_INSTANCE)) {
			return FMLRTVirtualModelInstanceModelSlot.class;
		}
		if (roleIdentifier.getText().equals(AbstractFMLTypingSpace.CONCEPT_INSTANCE)) {
			return FlexoConceptInstanceRole.class;
		}
		for (UseModelSlotDeclaration useModelSlotDeclaration : getAnalyzer().getCompilationUnit().getUseDeclarations()) {
			Class<? extends FlexoRole<?>> roleClass = getRoleClass(roleIdentifier, useModelSlotDeclaration.getModelSlotClass());
			if (roleClass != null) {
				return roleClass;
			}
		}
		return null;
	}

	public Class<? extends FlexoRole<?>> getRoleClass(TCidentifier taIdentifier, TUidentifier roleIdentifier) {
		Class<? extends ModelSlot<?>> modelSlotClass = getModelSlotClass(taIdentifier);
		if (modelSlotClass != null) {
			return getRoleClass(roleIdentifier, modelSlotClass);
		}
		return null;
	}

	private Class<? extends ModelSlot<?>> getModelSlotClass(TCidentifier taIdentifier) {
		for (UseModelSlotDeclaration useModelSlotDeclaration : getAnalyzer().getCompilationUnit().getUseDeclarations()) {
			if (taIdentifier.getText().equals(useModelSlotDeclaration.getAbbrev())) {
				return useModelSlotDeclaration.getModelSlotClass();
			}
		}
		return null;
	}

	private Class<? extends FlexoRole<?>> getRoleClass(TUidentifier roleIdentifier, Class<? extends ModelSlot<?>> modelSlotClass) {

		return getServiceManager().getTechnologyAdapterService().getFlexoRole(modelSlotClass, roleIdentifier.getText());

		/*if (roleIdentifier.getText().equals(modelSlotClass.getSimpleName())) {
			return modelSlotClass;
		}
		
		for (Class<? extends FlexoRole<?>> roleClass : getServiceManager().getTechnologyAdapterService()
				.getAvailableFlexoRoleTypes(modelSlotClass)) {
			if (roleIdentifier.getText().equals(roleClass.getSimpleName())) {
				return roleClass;
			}
		}
		
		return null;*/
	}

	public String serializeTAId(ModelSlot<?> modelSlot) {
		return serializeTAId((Class) modelSlot.getClass());
	}

	public String serializeTAId(Class<? extends ModelSlot<?>> modelSlotClass) {
		for (UseModelSlotDeclaration useModelSlotDeclaration : getAnalyzer().getCompilationUnit().getUseDeclarations()) {
			if (useModelSlotDeclaration.getModelSlotClass().isAssignableFrom(modelSlotClass)) {
				return useModelSlotDeclaration.getAbbrev();
			}
		}
		return null;
	}

	public String serializeTAId(FlexoRole<?> role) {
		Class<? extends ModelSlot<?>> modelSlotClass = getModelSlotClass(role);
		return serializeTAId(modelSlotClass);

	}

	public String serializeTAId(FlexoBehaviour behaviour) {
		Class<? extends ModelSlot<?>> modelSlotClass = getModelSlotClass(behaviour);
		return serializeTAId(modelSlotClass);

	}

	public String serializeTAId(TechnologySpecificAction<?, ?> editionAction) {
		Class<? extends ModelSlot<?>> modelSlotClass = getModelSlotClass(editionAction);
		return serializeTAId(modelSlotClass);

	}

	public Class<? extends ModelSlot<?>> getModelSlotClass(FlexoRole<?> role) {
		if (role instanceof ModelSlot) {
			return (Class<? extends ModelSlot<?>>) role.getClass();
		}
		for (TechnologyAdapter<?> ta : getAnalyzer().getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
			for (Class<? extends ModelSlot<?>> modelSlotClass : ta.getAvailableModelSlotTypes()) {
				for (Class<? extends FlexoRole<?>> roleClass : getAnalyzer().getServiceManager().getTechnologyAdapterService()
						.getAvailableFlexoRoleTypes(modelSlotClass)) {
					if (roleClass.isAssignableFrom(role.getClass())) {
						return modelSlotClass;
					}
				}

			}
		}
		return null;
	}

	public Class<? extends ModelSlot<?>> getModelSlotClass(FlexoBehaviour behaviour) {
		for (TechnologyAdapter<?> ta : getAnalyzer().getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
			for (Class<? extends ModelSlot<?>> modelSlotClass : ta.getAvailableModelSlotTypes()) {
				for (Class<? extends FlexoBehaviour> behaviourClass : getAnalyzer().getServiceManager().getTechnologyAdapterService()
						.getAvailableFlexoBehaviourTypes(modelSlotClass)) {
					if (behaviourClass.isAssignableFrom(behaviour.getClass())) {
						return modelSlotClass;
					}
				}

			}
		}
		return null;
	}

	public Class<? extends ModelSlot<?>> getModelSlotClass(TechnologySpecificAction<?, ?> editionAction) {
		for (TechnologyAdapter<?> ta : getAnalyzer().getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
			for (Class<? extends ModelSlot<?>> modelSlotClass : ta.getAvailableModelSlotTypes()) {
				for (Class<? extends EditionAction> editionActionClass : getAnalyzer().getServiceManager().getTechnologyAdapterService()
						.getAvailableEditionActionTypes(modelSlotClass)) {
					if (editionActionClass.isAssignableFrom(editionAction.getClass())) {
						return modelSlotClass;
					}
				}

			}
		}
		return null;
	}

	public Class<? extends FlexoBehaviour> getBehaviourClass(TUidentifier behaviourIdentifier) {
		for (UseModelSlotDeclaration useModelSlotDeclaration : getAnalyzer().getCompilationUnit().getUseDeclarations()) {
			Class<? extends FlexoBehaviour> behaviourClass = getBehaviourClass(behaviourIdentifier,
					useModelSlotDeclaration.getModelSlotClass());
			if (behaviourClass != null) {
				return behaviourClass;
			}
		}
		return null;
	}

	public Class<? extends FlexoBehaviour> getBehaviourClass(TCidentifier taIdentifier, TUidentifier behaviourIdentifier) {
		Class<? extends ModelSlot<?>> modelSlotClass = getModelSlotClass(taIdentifier);
		if (modelSlotClass != null) {
			return getBehaviourClass(behaviourIdentifier, modelSlotClass);
		}
		return null;
	}

	private Class<? extends FlexoBehaviour> getBehaviourClass(TUidentifier behaviourIdentifier,
			Class<? extends ModelSlot<?>> modelSlotClass) {
		return getServiceManager().getTechnologyAdapterService().getFlexoBehaviour(modelSlotClass, behaviourIdentifier.getText());
	}

	public Class<? extends TechnologySpecificAction<?, ?>> getEditionActionClass(TCidentifier taIdentifier, TUidentifier roleIdentifier) {
		Class<? extends ModelSlot<?>> modelSlotClass = getModelSlotClass(taIdentifier);
		if (modelSlotClass != null) {
			return getEditionActionClass(roleIdentifier, modelSlotClass);
		}
		return null;
	}

	private Class<? extends TechnologySpecificAction<?, ?>> getEditionActionClass(TUidentifier editionActionIdentifier,
			Class<? extends ModelSlot<?>> modelSlotClass) {
		return getServiceManager().getTechnologyAdapterService().getEditionAction(modelSlotClass, editionActionIdentifier.getText());
	}

	/*public <O extends FMLObject> Class<O> getFMLObjectClass(FMLObject modelObject, String instanceType) {
		if (modelObject.getFMLModelFactory() != null) {
			Iterator<ModelEntity> entities = modelObject.getFMLModelFactory().getModelContext().getEntities();
			while (entities.hasNext()) {
				ModelEntity entity = entities.next();
				Class implementedInterface = entity.getImplementedInterface();
				if (implement)
			}
		}
		return null;
	}
	
	public <O extends FMLObject> Class<O> getFMLObjectClass(FMLObject modelObject, String taID, String instanceType) {
		// TODO Auto-generated method stub
		return null;
	}*/

	public Class<? extends FMLObject> getFMLObjectClass(TUidentifier objectIdentifier) {
		for (UseModelSlotDeclaration useModelSlotDeclaration : getAnalyzer().getCompilationUnit().getUseDeclarations()) {
			Class<? extends FMLObject> objectClass = getFMLObjectClass(objectIdentifier, useModelSlotDeclaration.getModelSlotClass());
			if (objectClass != null) {
				return objectClass;
			}
		}
		return null;
	}

	public Class<? extends FMLObject> getFMLObjectClass(TCidentifier taIdentifier, TUidentifier objectIdentifier) {
		Class<? extends ModelSlot<?>> modelSlotClass = getModelSlotClass(taIdentifier);
		if (modelSlotClass != null) {
			return getFMLObjectClass(objectIdentifier, modelSlotClass);
		}
		return null;
	}

	private Class<? extends FMLObject> getFMLObjectClass(TUidentifier objectIdentifier, Class<? extends ModelSlot<?>> modelSlotClass) {

		return getServiceManager().getTechnologyAdapterService().getFMLObject(modelSlotClass, objectIdentifier.getText());

	}

}
