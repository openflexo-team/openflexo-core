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

package org.openflexo.foundation.fml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.binding.VirtualModelBindingModel;
import org.openflexo.foundation.fml.rm.AbstractVirtualModelResource;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstanceParameter;
import org.openflexo.foundation.fml.rt.editionaction.DeleteFlexoConceptInstanceParameter;
import org.openflexo.foundation.resource.CannotRenameException;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.foundation.technologyadapter.UseModelSlotDeclaration;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PastingPoint;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.undo.CompoundEdit;
import org.openflexo.model.validation.FixProposal;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.model.validation.ValidationWarning;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.toolbox.ToolBox;

/**
 * An {@link AbstractVirtualModel} is the specification of a model which will be instantied in a {@link View} as a set of federated models.
 * An {@link AbstractVirtualModel} is either a {@link VirtualModel} of a {@link ViewPoint}
 * 
 * The base modelling element of a {@link AbstractVirtualModel} is provided by {@link FlexoConcept} concept.
 * 
 * A {@link AbstractVirtualModel} instance contains a set of {@link FlexoConceptInstance}.
 * 
 * A {@link AbstractVirtualModel} is itself an {@link FlexoConcept}
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractVirtualModel.AbstractVirtualModelImpl.class)
@Imports({ @Import(FlexoConceptStructuralFacet.class), @Import(FlexoConceptBehaviouralFacet.class), @Import(InnerConceptsFacet.class),
		@Import(DeleteFlexoConceptInstanceParameter.class), @Import(AddFlexoConceptInstanceParameter.class) })
public interface AbstractVirtualModel<VM extends AbstractVirtualModel<VM>>
		extends FlexoConcept, VirtualModelObject, FlexoMetaModel<VM>, ResourceData<VM>, TechnologyObject<FMLTechnologyAdapter> {

	public static final String RESOURCE = "resource";

	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String VERSION_KEY = "version";
	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String MODEL_VERSION_KEY = "modelVersion";
	@PropertyIdentifier(type = FlexoConcept.class, cardinality = Cardinality.LIST)
	public static final String FLEXO_CONCEPTS_KEY = "flexoConcepts";
	@PropertyIdentifier(type = UseModelSlotDeclaration.class, cardinality = Cardinality.LIST)
	public static final String USE_DECLARATIONS_KEY = "useDeclarations";

	@Override
	public FMLModelFactory getFMLModelFactory();

	/**
	 * Return resource for this virtual model
	 * 
	 * @return
	 */
	@Override
	@Getter(value = RESOURCE, ignoreType = true)
	// @CloningStrategy(value = StrategyType.FACTORY, factory = "cloneResource()")
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoResource<VM> getResource();

	/**
	 * Sets resource for this virtual model
	 * 
	 * @param aName
	 */
	@Override
	@Setter(value = RESOURCE)
	public void setResource(FlexoResource<VM> aVirtualModelResource);

	/**
	 * Called to clone the resource of this {@link AbstractVirtualModel}
	 * 
	 * @return
	 */
	// public FlexoResource<VM> cloneResource();

	@Getter(value = VERSION_KEY, isStringConvertable = true)
	@XMLAttribute
	public FlexoVersion getVersion();

	@Setter(VERSION_KEY)
	public void setVersion(FlexoVersion version);

	@Getter(value = MODEL_VERSION_KEY, isStringConvertable = true)
	@XMLAttribute
	public FlexoVersion getModelVersion();

	@Setter(MODEL_VERSION_KEY)
	public void setModelVersion(FlexoVersion modelVersion);

	/**
	 * Return list of {@link UseModelSlotDeclaration} accessible from this {@link AbstractVirtualModel}<br>
	 * It includes the list of uses declarations accessible from parent and container
	 * 
	 * @return
	 */
	public List<UseModelSlotDeclaration> getAccessibleUseDeclarations();

	/**
	 * Return list of {@link UseModelSlotDeclaration} explicitely declared in this {@link AbstractVirtualModel}
	 * 
	 * @return
	 */
	@Getter(value = USE_DECLARATIONS_KEY, cardinality = Cardinality.LIST, inverse = UseModelSlotDeclaration.VIRTUAL_MODEL_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<UseModelSlotDeclaration> getUseDeclarations();

	@Setter(USE_DECLARATIONS_KEY)
	public void setUseDeclarations(List<UseModelSlotDeclaration> flexoConcepts);

	@Adder(USE_DECLARATIONS_KEY)
	@PastingPoint
	public void addToUseDeclarations(UseModelSlotDeclaration aFlexoConcept);

	@Remover(USE_DECLARATIONS_KEY)
	public void removeFromUseDeclarations(UseModelSlotDeclaration aFlexoConcept);

	/**
	 * Return boolean indicating if this VirtualModel uses supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	public <MS extends ModelSlot<?>> boolean uses(Class<MS> modelSlotClass);

	/**
	 * Declare use of supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	public <MS extends ModelSlot<?>> UseModelSlotDeclaration declareUse(Class<MS> modelSlotClass);

	/**
	 * Return all {@link FlexoConcept} defined in this {@link AbstractVirtualModel}
	 * 
	 * @return
	 */
	@Getter(value = FLEXO_CONCEPTS_KEY, cardinality = Cardinality.LIST, inverse = FlexoConcept.OWNER_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<FlexoConcept> getFlexoConcepts();

	@Setter(FLEXO_CONCEPTS_KEY)
	public void setFlexoConcepts(List<FlexoConcept> flexoConcepts);

	@Adder(FLEXO_CONCEPTS_KEY)
	@PastingPoint
	public void addToFlexoConcepts(FlexoConcept aFlexoConcept);

	@Remover(FLEXO_CONCEPTS_KEY)
	public void removeFromFlexoConcepts(FlexoConcept aFlexoConcept);

	/**
	 * Return FlexoConcept matching supplied id represented as a string, which could be either the name of FlexoConcept, or its URI
	 * 
	 * @param flexoConceptNameOrURI
	 * @return
	 */
	public FlexoConcept getFlexoConcept(String flexoConceptNameOrURI);

	/**
	 * Return true if URI is well formed and valid regarding its unicity (no one other object has same URI)
	 * 
	 * @param uri
	 * @return
	 */
	public boolean testValidURI(String ontologyURI, String conceptURI);

	/**
	 * Return true if URI is duplicated in the context of this project
	 * 
	 * @param uri
	 * @return
	 */
	public boolean isDuplicatedURI(String modelURI, String conceptURI);

	/**
	 * Retrieve metamodel referenced by its URI<br>
	 * Note that search is performed in the scope of current project only
	 * 
	 * @param modelURI
	 * @return
	 */
	public FlexoMetaModel<?> getMetaModel(String metaModelURI);

	/**
	 * Return all {@link FlexoConcept} defined in this {@link AbstractVirtualModel} which have no container (contaiment semantics)<br>
	 * (where container is the virtual model itself)
	 * 
	 * @return
	 */
	public List<FlexoConcept> getAllRootFlexoConcepts();

	/**
	 * Return all {@link FlexoConcept} defined in this {@link AbstractVirtualModel} which have no parent (inheritance semantics)
	 * 
	 * @return
	 */
	public List<FlexoConcept> getAllSuperFlexoConcepts();

	public boolean hasNature(VirtualModelNature nature);

	@Override
	public VirtualModelBindingModel getBindingModel();

	public InnerConceptsFacet getInnerConceptsFacet();

	/**
	 * Return the list of {@link TechnologyAdapter} used in the context of this {@link AbstractVirtualModel}
	 * 
	 * @return
	 */
	public List<TechnologyAdapter> getRequiredTechnologyAdapters();

	public static abstract class AbstractVirtualModelImpl<VM extends AbstractVirtualModel<VM>> extends FlexoConceptImpl
			implements AbstractVirtualModel<VM> {

		private static final Logger logger = Logger.getLogger(AbstractVirtualModel.class.getPackage().getName());

		private AbstractVirtualModelResource<VM> resource;

		private boolean readOnly = false;

		private final VirtualModelInstanceType vmInstanceType = new VirtualModelInstanceType(this);

		// Used during deserialization, do not use it
		public AbstractVirtualModelImpl() {
			super();
		}

		@Override
		public VirtualModelInstanceType getInstanceType() {
			return vmInstanceType;
		}

		@Override
		public FMLModelFactory getFMLModelFactory() {
			if (deserializationFactory != null /*isDeserializing()*/) {
				return deserializationFactory;
			}
			if (getResource() != null) {
				return getResource().getFactory();
			}
			else {
				return getDeserializationFactory();
			}
		}

		private FMLModelFactory deserializationFactory;

		@Override
		public void initializeDeserialization(FMLModelFactory factory) {
			deserializationFactory = factory;
		}

		@Override
		public void finalizeDeserialization() {
			for (FlexoConcept ep : getFlexoConcepts()) {
				ep.finalizeDeserialization();
			}
			super.finalizeDeserialization();
		}

		@Override
		public final boolean hasNature(VirtualModelNature nature) {
			return nature.hasNature(this);
		}

		/**
		 * Return the URI of the {@link AbstractVirtualModel}<br>
		 * The convention for URI are following: <viewpoint_uri>/<virtual_model_name >#<flexo_concept_name>.<edition_scheme_name> <br>
		 * eg<br>
		 * http://www.mydomain.org/MyViewPoint/MyVirtualModel#MyFlexoConcept. MyEditionScheme
		 * 
		 * @return String representing unique URI of this object
		 */
		@Override
		public String getURI() {
			if (getViewPoint() != null) {
				return getViewPoint().getURI() + "/" + getName();
			}
			return null;
		}

		@Override
		public String getName() {
			if (getResource() != null) {
				return getResource().getName();
			}
			return super.getName();
		}

		@Override
		public void setName(String name) {
			if (requireChange(getName(), name)) {
				String oldValue = getName();
				if (getResource() != null) {
					try {
						getResource().setName(name);
						getPropertyChangeSupport().firePropertyChange("name", oldValue, name);
					} catch (CannotRenameException e) {
						e.printStackTrace();
					}
				}
				else {
					super.setName(name);
				}
			}
		}

		@Override
		public FlexoVersion getVersion() {
			if (getResource() != null) {
				return getResource().getVersion();
			}
			return null;
		}

		@Override
		public void setVersion(FlexoVersion aVersion) {
			if (requireChange(getVersion(), aVersion)) {
				if (getResource() != null) {
					getResource().setVersion(aVersion);
				}
			}
		}

		@Override
		public String toString() {
			return "VirtualModel:" + getName();
		}

		/**
		 * Return all {@link FlexoConcept} defined in this {@link AbstractVirtualModel} which have no container (containment semantics)<br>
		 * (where container is the virtual model itself)
		 * 
		 * @return
		 */
		@Override
		public List<FlexoConcept> getAllRootFlexoConcepts() {

			Vector<FlexoConcept> returned = new Vector<>();
			for (FlexoConcept ep : getFlexoConcepts()) {
				if (ep.isRoot()) {
					returned.add(ep);
				}
			}
			return returned;
		}

		/**
		 * Return all {@link FlexoConcept} defined in this {@link AbstractVirtualModel} which have no parent (inheritance semantics)
		 * 
		 * @return
		 */
		@Override
		public List<FlexoConcept> getAllSuperFlexoConcepts() {
			ArrayList<FlexoConcept> returned = new ArrayList<>();
			for (FlexoConcept fc : getFlexoConcepts()) {
				if (fc.isSuperConcept()) {
					returned.add(fc);
				}
			}
			return returned;
		}

		// Override PAMELA internal call by providing custom notification support
		@Override
		public void addToFlexoConcepts(FlexoConcept aFlexoConcept) {
			performSuperAdder(FLEXO_CONCEPTS_KEY, aFlexoConcept);
			getPropertyChangeSupport().firePropertyChange("allRootFlexoConcepts", null, aFlexoConcept);
			getPropertyChangeSupport().firePropertyChange("allSuperFlexoConcepts", null, aFlexoConcept);
			if (aFlexoConcept.getParentFlexoConcepts() != null) {
				for (FlexoConcept parent : aFlexoConcept.getParentFlexoConcepts()) {
					parent.getPropertyChangeSupport().firePropertyChange(FlexoConcept.CHILD_FLEXO_CONCEPTS_KEY, null, aFlexoConcept);
				}
			}
			getInnerConceptsFacet().notifiedConceptsChanged();
		}

		// Override PAMELA internal call by providing custom notification support
		@Override
		public void removeFromFlexoConcepts(FlexoConcept aFlexoConcept) {
			performSuperRemover(FLEXO_CONCEPTS_KEY, aFlexoConcept);
			getPropertyChangeSupport().firePropertyChange("allRootFlexoConcepts", aFlexoConcept, null);
			getPropertyChangeSupport().firePropertyChange("allSuperFlexoConcepts", aFlexoConcept, null);
			if (aFlexoConcept.getParentFlexoConcepts() != null) {
				for (FlexoConcept parent : aFlexoConcept.getParentFlexoConcepts()) {
					parent.getPropertyChangeSupport().firePropertyChange(FlexoConcept.CHILD_FLEXO_CONCEPTS_KEY, aFlexoConcept, null);
				}
			}
			InnerConceptsFacet innerConceptsFacet = getInnerConceptsFacet();
			if (innerConceptsFacet != null)
				innerConceptsFacet.notifiedConceptsChanged();
		}

		/**
		 * Return FlexoConcept matching supplied id represented as a string, which could be either the name of FlexoConcept, or its URI
		 * 
		 * @param flexoConceptId
		 * @return
		 */
		@Override
		public FlexoConcept getFlexoConcept(String flexoConceptNameOrURI) {
			for (FlexoConcept flexoConcept : getFlexoConcepts()) {
				if (flexoConcept.getName() != null && flexoConcept.getName().equals(flexoConceptNameOrURI)) {
					return flexoConcept;
				}
				if (flexoConcept.getName() != null && flexoConcept.getURI().equals(flexoConceptNameOrURI)) {
					return flexoConcept;
				}
				// Special case to handle conversion from old VP version
				// TODO: to be removed when all VP are up-to-date
				if (getViewPoint() != null && flexoConcept != null) {
					if ((getViewPoint().getURI() + "#" + flexoConcept.getName()).equals(flexoConceptNameOrURI)) {
						return flexoConcept;
					}
				}

			}
			// logger.warning("Not found FlexoConcept:" + flexoConceptId);
			return null;
		}

		public SynchronizationScheme createSynchronizationScheme() {
			SynchronizationScheme newSynchronizationScheme = getFMLModelFactory().newSynchronizationScheme();
			newSynchronizationScheme.setSynchronizedVirtualModel(this);
			newSynchronizationScheme.setName("synchronization");
			addToFlexoBehaviours(newSynchronizationScheme);
			return newSynchronizationScheme;
		}

		/**
		 * Return true if URI is well formed and valid regarding its unicity (no one other object has same URI)
		 * 
		 * @param uri
		 * @return
		 */
		@Override
		public boolean testValidURI(String ontologyURI, String conceptURI) {
			if (StringUtils.isEmpty(conceptURI)) {
				return false;
			}
			if (StringUtils.isEmpty(conceptURI.trim())) {
				return false;
			}
			return conceptURI.equals(ToolBox.getJavaName(conceptURI, true, false)) && !isDuplicatedURI(ontologyURI, conceptURI);
		}

		/**
		 * Return true if URI is duplicated in the context of this project
		 * 
		 * @param uri
		 * @return
		 */
		@Override
		public boolean isDuplicatedURI(String modelURI, String conceptURI) {
			FlexoMetaModel<?> m = getMetaModel(modelURI);
			if (m != null) {
				return m.getObject(modelURI + "#" + conceptURI) != null;
			}
			return false;
		}

		/**
		 * Retrieve metamodel referenced by its URI<br>
		 * Note that search is performed in the scope of current project only
		 * 
		 * @param modelURI
		 * @return
		 */
		@Override
		public FlexoMetaModel<?> getMetaModel(String metaModelURI) {
			for (FlexoMetaModel<?> m : getAllReferencedMetaModels()) {
				if (m.getURI().equals(metaModelURI)) {
					return m;
				}
			}
			return null;
		}

		/**
		 * Return the list of all metamodels used in the scope of this virtual model
		 * 
		 * @return
		 */
		@Deprecated
		public Set<FlexoMetaModel<?>> getAllReferencedMetaModels() {
			HashSet<FlexoMetaModel<?>> returned = new HashSet<>();
			for (ModelSlot<?> modelSlot : getModelSlots()) {
				if (modelSlot instanceof TypeAwareModelSlot) {
					TypeAwareModelSlot<?, ?> tsModelSlot = (TypeAwareModelSlot<?, ?>) modelSlot;
					if (tsModelSlot.getMetaModelResource() != null) {
						returned.add(tsModelSlot.getMetaModelResource().getMetaModelData());
					}
				}
			}
			return returned;
		}

		@Override
		public boolean isReadOnly() {
			return readOnly;
		}

		@Override
		public void setIsReadOnly(boolean b) {
			readOnly = b;
		}

		@Override
		public FlexoVersion getModelVersion() {
			if (getResource() != null) {
				return getResource().getModelVersion();
			}
			return null;
		}

		@Override
		public void setModelVersion(FlexoVersion aVersion) {
			if (getResource() != null) {
				getResource().setModelVersion(aVersion);
			}
		}

		// Implementation of XMLStorageResourceData

		@Override
		public AbstractVirtualModelResource<VM> getResource() {
			return resource;
		}

		@Override
		public void setResource(FlexoResource<VM> resource) {
			this.resource = (AbstractVirtualModelResource<VM>) resource;
		}

		@Override
		public void save() {
			logger.info("Saving ViewPoint to " + getResource().getIODelegate().toString() + "...");

			try {
				getResource().save(null);
			} catch (SaveResourceException e) {
				e.printStackTrace();
			}
		}

		@Override
		public FMLTechnologyAdapter getTechnologyAdapter() {
			if (getResource() != null) {
				return getResource().getTechnologyAdapter();
			}
			return null;
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("VirtualModel " + getName() + " type=" + getImplementedInterface().getSimpleName() + " uri=\"" + getURI() + "\"",
					context);
			out.append(" {" + StringUtils.LINE_SEPARATOR, context);

			/*if (getModelSlots().size() > 0) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				for (ModelSlot<?> modelSlot : getModelSlots()) {
					// if (modelSlot.getMetaModelResource() != null) {
					out.append(modelSlot.getFMLRepresentation(context), context, 1);
					out.append(StringUtils.LINE_SEPARATOR, context, 1);
					// }
				}
			}*/

			if (getDeclaredProperties().size() > 0) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				for (FlexoProperty<?> pr : getDeclaredProperties()) {
					out.append(pr.getFMLRepresentation(context), context, 1);
					out.append(StringUtils.LINE_SEPARATOR, context);
				}
			}

			if (getFlexoBehaviours().size() > 0) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				for (FlexoBehaviour es : getFlexoBehaviours()) {
					out.append(es.getFMLRepresentation(context), context, 1);
					out.append(StringUtils.LINE_SEPARATOR, context);
				}
			}

			if (getFlexoConcepts().size() > 0) {
				out.append(StringUtils.LINE_SEPARATOR, context);
				for (FlexoConcept ep : getFlexoConcepts()) {
					out.append(ep.getFMLRepresentation(context), context, 1);
					out.append(StringUtils.LINE_SEPARATOR, context);
				}
			}
			out.append("}" + StringUtils.LINE_SEPARATOR, context);
			return out.toString();
		}

		@Override
		public AbstractVirtualModel<?> getResourceData() {
			return this;
		}

		// Developper's note: we implement here VirtualModelObject API
		// Do not consider getOwningVirtualModel()
		@Override
		public AbstractVirtualModel<?> getVirtualModel() {
			return this;
		}

		@Override
		public abstract VirtualModelBindingModel getBindingModel();

		@Override
		public Object getObject(String objectURI) {
			// TODO Auto-generated method stub
			return null;
		}

		private InnerConceptsFacet innerConceptsFacet;

		@Override
		public InnerConceptsFacet getInnerConceptsFacet() {
			FMLModelFactory factory = getFMLModelFactory();
			if (innerConceptsFacet == null && factory != null) {
				CompoundEdit ce = null;
				if (!factory.getEditingContext().getUndoManager().isBeeingRecording()) {
					ce = factory.getEditingContext().getUndoManager().startRecording("CREATE_INNER_CONCEPTS_FACET");
				}
				innerConceptsFacet = factory.newInnerConceptsFacet(this);
				if (ce != null) {
					factory.getEditingContext().getUndoManager().stopRecording(ce);
				}
			}
			return innerConceptsFacet;
		}

		/**
		 * Return the list of {@link TechnologyAdapter} used in the context of this {@link AbstractVirtualModel}
		 * 
		 * @return
		 */
		@Override
		public List<TechnologyAdapter> getRequiredTechnologyAdapters() {
			List<TechnologyAdapter> returned = new ArrayList<>();
			returned.add(getTechnologyAdapter());
			for (ModelSlot<?> ms : getModelSlots()) {
				if (!returned.contains(ms.getModelSlotTechnologyAdapter())) {
					returned.add(ms.getModelSlotTechnologyAdapter());
				}
			}
			return returned;
		}

		/**
		 * Return {@link FlexoProperty} identified by supplied name, which is to be retrieved in all accessible properties<br>
		 * Note that returned property is not necessary one of declared property, but might be inherited.
		 * 
		 * @param flexoPropertyName
		 * @return
		 * @see #getAccessibleProperties()
		 */
		@Override
		public FlexoProperty<?> getAccessibleProperty(String propertyName) {

			FlexoProperty<?> returned = super.getAccessibleProperty(propertyName);

			if (returned != null) {
				return returned;
			}

			for (FlexoProperty<?> p : getModelSlots()) {
				if (p.getName().equals(propertyName)) {
					return p;
				}
			}
			return null;
		}

		/**
		 * Return boolean indicating if this VirtualModel uses supplied modelSlotClass
		 * 
		 * @param modelSlotClass
		 * @return
		 */
		@Override
		public <MS extends ModelSlot<?>> boolean uses(Class<MS> modelSlotClass) {
			if (modelSlotClass == null) {
				return false;
			}
			for (UseModelSlotDeclaration useDecl : getUseDeclarations()) {
				if (modelSlotClass.equals(useDecl.getModelSlotClass())) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Declare use of supplied modelSlotClass
		 * 
		 * @param modelSlotClass
		 * @return
		 */
		@Override
		public <MS extends ModelSlot<?>> UseModelSlotDeclaration declareUse(Class<MS> modelSlotClass) {
			if (modelSlotClass == null) {
				return null;
			}
			UseModelSlotDeclaration useDeclaration = getFMLModelFactory().newUseModelSlotDeclaration(modelSlotClass);
			addToUseDeclarations(useDeclaration);
			return useDeclaration;
		}

		/**
		 * Return list of {@link UseModelSlotDeclaration} accessible from this {@link AbstractVirtualModel}<br>
		 * It includes the list of uses declarations accessible from parent and container
		 * 
		 * @return
		 */
		@Override
		public List<UseModelSlotDeclaration> getAccessibleUseDeclarations() {
			return getUseDeclarations();
		}

		@Override
		protected void notifiedPropertiesChanged(FlexoProperty<?> oldValue, FlexoProperty<?> newValue) {
			super.notifiedPropertiesChanged(oldValue, newValue);
			for (FlexoConcept embedded : getFlexoConcepts()) {
				((FlexoConceptImpl) embedded).notifiedPropertiesChanged(oldValue, newValue);
			}
		}

	}

	@DefineValidationRule
	public static class ShouldNotHaveReflexiveVirtualModelModelSlot
			extends ValidationRule<ShouldNotHaveReflexiveVirtualModelModelSlot, AbstractVirtualModel> {

		public ShouldNotHaveReflexiveVirtualModelModelSlot() {
			super(AbstractVirtualModel.class, "virtual_model_should_not_have_reflexive_model_slot_no_more");
		}

		@Override
		public ValidationIssue<ShouldNotHaveReflexiveVirtualModelModelSlot, AbstractVirtualModel> applyValidation(AbstractVirtualModel vm) {
			for (ModelSlot<?> ms : ((AbstractVirtualModel<?>) vm).getModelSlots()) {
				if (ms instanceof FMLRTModelSlot && "virtualModelInstance".equals(ms.getName())) {
					RemoveReflexiveVirtualModelModelSlot fixProposal = new RemoveReflexiveVirtualModelModelSlot(vm);
					return new ValidationWarning<>(this, vm, "virtual_model_should_not_have_reflexive_model_slot_no_more", fixProposal);

				}
			}
			return null;
		}

		protected static class RemoveReflexiveVirtualModelModelSlot
				extends FixProposal<ShouldNotHaveReflexiveVirtualModelModelSlot, AbstractVirtualModel> {

			private final AbstractVirtualModel<?> vm;

			public RemoveReflexiveVirtualModelModelSlot(AbstractVirtualModel vm) {
				super("remove_reflexive_modelslot");
				this.vm = vm;
			}

			@Override
			protected void fixAction() {
				for (ModelSlot<?> ms : new ArrayList<ModelSlot>(vm.getModelSlots())) {
					if ("virtualModelInstance".equals(ms.getName())) {
						vm.removeFromModelSlots(ms);
					}
				}
			}

		}

	}

}