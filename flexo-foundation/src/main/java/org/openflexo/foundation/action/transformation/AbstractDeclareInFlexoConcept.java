/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
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

package org.openflexo.foundation.action.transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;

/**
 * This abstract class is a base action that allows to create or transform a {@link FlexoConcept} from a {@link TechnologyObject}<br>
 * 3 kind of transformations are proposed from the selection of a {@link TechnologyObject}:
 * <ul>
 * <li>a {@link FlexoConceptCreationStrategy} which allows to create a new {@link FlexoConcept}</li>
 * <li>a {@link FlexoRoleCreationStrategy} which allows to create a new {@link FlexoRole} in existing {@link FlexoConcept}</li>
 * <li>a {@link FlexoRoleSettingStrategy} which allows to set an existing {@link FlexoRole} in existing {@link FlexoConcept}</li>
 * </ul>
 * Some strategies should be implemented for each of that choices, defined as primary choices<br>
 * 
 * Note that to be valid, this class must be externally set with {@link VirtualModelResource}
 * 
 * @author Sylvain, Vincent
 * 
 * @param <A>
 *            type of action
 * @param <T1>
 *            type of focused object (technology object beeing used in operation)
 * @param <T2>
 *            type of glocal selection (technology object beeing used in operation)
 */
public abstract class AbstractDeclareInFlexoConcept<A extends AbstractDeclareInFlexoConcept<A, T1, T2>, T1 extends TechnologyObject<?>, T2 extends TechnologyObject<?>>
		extends TransformationAction<A, T1, T2> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractDeclareInFlexoConcept.class.getPackage().getName());

	private VirtualModelResource virtualModelResource;

	/**
	 * Stores the model slot used as source of information (data) in pattern proposal
	 */
	private ModelSlot<?> informationSourceModelSlot;

	/**
	 * Reference the {@link FlexoConcept} on which we want to apply transformation
	 */
	private FlexoConcept flexoConcept;

	private FlexoConceptCreationStrategy<A> flexoConceptCreationStrategy;
	private FlexoRoleCreationStrategy<A, FlexoRole<T1>, T1, T2> flexoRoleCreationStrategy;
	private FlexoRoleSettingStrategy<A, FlexoRole<T1>, T1, T2> flexoRoleSettingStrategy;

	/**
	 * Constructor for this class
	 * 
	 * @param actionType
	 * @param focusedObject
	 * @param globalSelection
	 * @param editor
	 */
	protected AbstractDeclareInFlexoConcept(FlexoActionType<A, T1, T2> actionType, T1 focusedObject, Vector<T2> globalSelection,
			FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		// Get the set of model slots that are available from the current virtual model
		List<ModelSlot<?>> availableModelSlots = getAvailableModelSlots();
		if (availableModelSlots != null && availableModelSlots.size() > 0) {
			informationSourceModelSlot = availableModelSlots.get(0);
		}
	}

	/**
	 * Returns the strategy to be choosen as concept creation strategy
	 */
	public FlexoConceptCreationStrategy<A> getFlexoConceptCreationStrategy() {
		if (flexoConceptCreationStrategy == null && getAvailableFlexoConceptCreationStrategies().size() > 0) {
			return getAvailableFlexoConceptCreationStrategies().get(0);
		}
		return flexoConceptCreationStrategy;
	}

	/**
	 * Sets the strategy to be choosen as concept creation strategy
	 */
	public void setFlexoConceptCreationStrategy(FlexoConceptCreationStrategy<A> flexoConceptCreationStrategy) {
		if (flexoConceptCreationStrategy != this.flexoConceptCreationStrategy) {
			FlexoConceptCreationStrategy<A> oldValue = this.flexoConceptCreationStrategy;
			this.flexoConceptCreationStrategy = flexoConceptCreationStrategy;
			getPropertyChangeSupport().firePropertyChange("flexoConceptCreationStrategy", oldValue, flexoConceptCreationStrategy);
		}
	}

	private final List<FlexoConceptCreationStrategy<A>> availableFlexoConceptCreationStrategies = new ArrayList<FlexoConceptCreationStrategy<A>>();
	private final List<FlexoRoleCreationStrategy<A, ? extends FlexoRole<T1>, T1, T2>> availableFlexoRoleCreationStrategies = new ArrayList<FlexoRoleCreationStrategy<A, ? extends FlexoRole<T1>, T1, T2>>();
	private final List<FlexoRoleSettingStrategy<A, ? extends FlexoRole<T1>, T1, T2>> availableFlexoRoleSettingStrategies = new ArrayList<FlexoRoleSettingStrategy<A, ? extends FlexoRole<T1>, T1, T2>>();

	/**
	 * Return the list of all available {@link FlexoConceptCreationStrategy}
	 * 
	 * @return
	 */
	public List<FlexoConceptCreationStrategy<A>> getAvailableFlexoConceptCreationStrategies() {
		return availableFlexoConceptCreationStrategies;
	}

	/**
	 * Returns the strategy to be choosen as {@link FlexoRole} creation strategy
	 */
	public FlexoRoleCreationStrategy<A, ? extends FlexoRole<T1>, T1, T2> getFlexoRoleCreationStrategy() {
		if (flexoRoleCreationStrategy == null && getAvailableFlexoRoleCreationStrategies().size() > 0) {
			return getAvailableFlexoRoleCreationStrategies().get(0);
		}
		return flexoRoleCreationStrategy;
	}

	/**
	 * Sets the strategy to be choosen as {@link FlexoRole} creation strategy
	 */
	public void setFlexoRoleCreationStrategy(FlexoRoleCreationStrategy<A, FlexoRole<T1>, T1, T2> flexoRoleCreationStrategy) {
		if ((flexoRoleCreationStrategy == null && this.flexoRoleCreationStrategy != null)
				|| (flexoRoleCreationStrategy != null && !flexoRoleCreationStrategy.equals(this.flexoRoleCreationStrategy))) {
			FlexoRoleCreationStrategy<A, FlexoRole<T1>, T1, T2> oldValue = this.flexoRoleCreationStrategy;
			this.flexoRoleCreationStrategy = flexoRoleCreationStrategy;
			getPropertyChangeSupport().firePropertyChange("flexoRoleCreationStrategy", oldValue, flexoRoleCreationStrategy);
		}
	}

	/**
	 * Return the list of all available {@link FlexoRoleCreationStrategy}
	 * 
	 * @return
	 */
	public List<FlexoRoleCreationStrategy<A, ? extends FlexoRole<T1>, T1, T2>> getAvailableFlexoRoleCreationStrategies() {
		return availableFlexoRoleCreationStrategies;
	}

	/**
	 * Returns the strategy to be choosen as {@link FlexoRole} setting strategy
	 */
	public FlexoRoleSettingStrategy<A, ? extends FlexoRole<T1>, T1, T2> getFlexoRoleSettingStrategy() {
		if (flexoRoleSettingStrategy == null && getAvailableFlexoRoleSettingStrategies().size() > 0) {
			return getAvailableFlexoRoleSettingStrategies().get(0);
		}
		return flexoRoleSettingStrategy;
	}

	/**
	 * Sets the strategy to be choosen as {@link FlexoRole} setting strategy
	 */
	public void setFlexoRoleSettingStrategy(FlexoRoleSettingStrategy<A, FlexoRole<T1>, T1, T2> flexoRoleSettingStrategy) {
		if ((flexoRoleSettingStrategy == null && this.flexoRoleSettingStrategy != null)
				|| (flexoRoleSettingStrategy != null && !flexoRoleSettingStrategy.equals(this.flexoRoleSettingStrategy))) {
			FlexoRoleSettingStrategy<A, FlexoRole<T1>, T1, T2> oldValue = this.flexoRoleSettingStrategy;
			this.flexoRoleSettingStrategy = flexoRoleSettingStrategy;
			getPropertyChangeSupport().firePropertyChange("flexoRoleSettingStrategy", oldValue, flexoRoleSettingStrategy);
		}
	}

	/**
	 * Return the list of all available {@link FlexoRoleSettingStrategy}
	 * 
	 * @return
	 */
	public List<FlexoRoleSettingStrategy<A, ? extends FlexoRole<T1>, T1, T2>> getAvailableFlexoRoleSettingStrategies() {
		return availableFlexoRoleSettingStrategies;
	}

	/**
	 * Return the VirtualModel on which we are working<br>
	 * This {@link VirtualModel} must be set with external API.
	 */
	public VirtualModel getVirtualModel() {
		if (getVirtualModelResource() != null) {
			return getVirtualModelResource().getVirtualModel();
		}
		return null;
	}

	/**
	 * Return the VirtualModelResource on which we are working<br>
	 * This {@link VirtualModelResource} must be set with external API.
	 */
	public VirtualModelResource getVirtualModelResource() {
		return virtualModelResource;
	}

	/**
	 * Sets the VirtualModelResource on which we are working<br>
	 * This {@link VirtualModelResource} must be set with external API.
	 */
	public void setVirtualModelResource(VirtualModelResource virtualModelResource) {
		if (this.virtualModelResource != virtualModelResource) {
			VirtualModelResource oldValue = this.virtualModelResource;
			this.virtualModelResource = virtualModelResource;
			getPropertyChangeSupport().firePropertyChange("virtualModelResource", oldValue, virtualModelResource);
		}
	}

	public static enum DeclareInFlexoConceptChoices {
		CREATES_FLEXO_CONCEPT, CREATE_ELEMENT_IN_EXISTING_FLEXO_CONCEPT, REPLACE_ELEMENT_IN_EXISTING_FLEXO_CONCEPT
	}

	private DeclareInFlexoConceptChoices primaryChoice = DeclareInFlexoConceptChoices.CREATES_FLEXO_CONCEPT;

	public DeclareInFlexoConceptChoices getPrimaryChoice() {
		return primaryChoice;
	}

	public void setPrimaryChoice(DeclareInFlexoConceptChoices primaryChoice) {
		if (this.primaryChoice != primaryChoice) {
			DeclareInFlexoConceptChoices oldValue = this.primaryChoice;
			this.primaryChoice = primaryChoice;
			getPropertyChangeSupport().firePropertyChange("primaryChoice", oldValue, primaryChoice);
		}
	}

	@Override
	public TransformationStrategy<A> getTransformationStrategy() {
		switch (getPrimaryChoice()) {
			case CREATES_FLEXO_CONCEPT:
				return getFlexoConceptCreationStrategy();
			case CREATE_ELEMENT_IN_EXISTING_FLEXO_CONCEPT:
				return getFlexoRoleCreationStrategy();
			case REPLACE_ELEMENT_IN_EXISTING_FLEXO_CONCEPT:
				return getFlexoRoleSettingStrategy();
			default:
				return null;
		}
	}

	@Override
	public boolean isValid() {
		if (getFocusedObject() == null) {
			return false;
		}
		if (getVirtualModelResource() == null) {
			return false;
		}
		if (!super.isValid()) {
			return false;
		}
		return true;
	}

	public FlexoConcept getFlexoConcept() {
		return flexoConcept;
	}

	public void setFlexoConcept(FlexoConcept flexoConcept) {
		if ((flexoConcept == null && this.flexoConcept != null) || (flexoConcept != null && !flexoConcept.equals(this.flexoConcept))) {
			FlexoConcept oldValue = this.flexoConcept;
			this.flexoConcept = flexoConcept;
			getPropertyChangeSupport().firePropertyChange("flexoConcept", oldValue, flexoConcept);
		}
	}

	/**
	 * Return the model slot used as source of information (data) in pattern proposal
	 * 
	 * @return
	 */
	public ModelSlot<?> getInformationSourceModelSlot() {
		return informationSourceModelSlot;
	}

	/**
	 * Sets the model slot used as source of information (data) in pattern proposal
	 * 
	 * @return
	 */
	public void setInformationSourceModelSlot(ModelSlot<?> modelSlot) {
		this.informationSourceModelSlot = modelSlot;
	}

	/**
	 * Return the list of all model slots declared in virtual model where this action is defined
	 * 
	 * @return
	 */
	public List<ModelSlot<?>> getAvailableModelSlots() {
		if (getVirtualModel() != null) {
			return getVirtualModel().getModelSlots();
		}
		return null;
	}

	/**
	 * Return a virtual model adressed by a model slot
	 * 
	 * @return
	 */
	public VirtualModel getAdressedVirtualModel() {
		if (isVirtualModelModelSlot()) {
			FMLRTModelSlot<?, ?> virtualModelModelSlot = (FMLRTModelSlot<?, ?>) getInformationSourceModelSlot();
			return virtualModelModelSlot.getAccessedVirtualModel();
		}
		return null;
	}

	/**
	 * Return a virtual model adressed by a model slot
	 * 
	 * @return
	 */
	public FlexoMetaModel<?> getAdressedFlexoMetaModel() {
		if (isTypeAwareModelSlot()) {
			TypeAwareModelSlot<?, ?> typeAwareModelSlot = (TypeAwareModelSlot<?, ?>) getInformationSourceModelSlot();
			if (typeAwareModelSlot != null && typeAwareModelSlot.getMetaModelResource() != null) {
				return typeAwareModelSlot.getMetaModelResource().getMetaModelData();
			}
		}
		return null;
	}

	private List<FMLRTModelSlot<?, ?>> virtualModelModelSlots;
	private List<TypeAwareModelSlot<?, ?>> typeAwareModelSlots;

	public List<FMLRTModelSlot<?, ?>> getVirtualModelModelSlots() {
		if (virtualModelModelSlots == null) {
			virtualModelModelSlots = new ArrayList<FMLRTModelSlot<?, ?>>();
		}
		if (!virtualModelModelSlots.isEmpty()) {
			virtualModelModelSlots.clear();
		}
		if (getVirtualModel() != null) {
			for (ModelSlot<?> modelSlot : getVirtualModel().getModelSlots()) {
				if (modelSlot instanceof FMLRTModelSlot) {
					virtualModelModelSlots.add((FMLRTModelSlot<?, ?>) modelSlot);
				}
			}
		}
		return virtualModelModelSlots;
	}

	public List<TypeAwareModelSlot<?, ?>> getTypeAwareModelSlots() {
		if (typeAwareModelSlots == null) {
			typeAwareModelSlots = new ArrayList<TypeAwareModelSlot<?, ?>>();
		}
		if (!typeAwareModelSlots.isEmpty()) {
			typeAwareModelSlots.clear();
		}
		if (getVirtualModel() != null) {
			for (ModelSlot<?> modelSlot : getVirtualModel().getModelSlots()) {
				if (modelSlot instanceof TypeAwareModelSlot) {
					typeAwareModelSlots.add((TypeAwareModelSlot<?, ?>) modelSlot);
				}
			}
		}
		return typeAwareModelSlots;
	}

	/**
	 * Return flag indicating if currently selected {@link ModelSlot} is a {@link TypeAwareModelSlot}
	 * 
	 * @return
	 */
	public boolean isTypeAwareModelSlot() {
		if (getInformationSourceModelSlot() instanceof TypeAwareModelSlot) {
			return true;
		}
		return false;
	}

	/**
	 * Return flag indicating if currently selected {@link ModelSlot} is a {@link FMLRTModelSlot}
	 * 
	 * @return
	 */
	public boolean isVirtualModelModelSlot() {
		if (getInformationSourceModelSlot() instanceof FMLRTModelSlot) {
			return true;
		}
		return false;
	}

	/**
	 * Return the FMLModelFactory to used in action context
	 * 
	 * @return
	 */
	public FMLModelFactory getFactory() {
		if (getFlexoConcept() != null) {
			return getFlexoConcept().getFMLModelFactory();
		}
		else if (getVirtualModelResource() != null) {
			return getVirtualModelResource().getFactory();
		}
		return null;
	}

}
