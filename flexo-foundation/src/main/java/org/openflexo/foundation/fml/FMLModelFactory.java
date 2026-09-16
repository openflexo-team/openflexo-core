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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.PamelaResourceModelFactory;
import org.openflexo.foundation.action.FlexoUndoManager;
import org.openflexo.foundation.fml.FMLModelContext.FMLProperty;
import org.openflexo.foundation.fml.annotations.DeclareEditionActions;
import org.openflexo.foundation.fml.annotations.DeclareFetchRequests;
import org.openflexo.foundation.fml.annotations.DeclareFlexoBehaviours;
import org.openflexo.foundation.fml.annotations.DeclareFlexoRoles;
import org.openflexo.foundation.fml.binding.CreationSchemePathElement;
import org.openflexo.foundation.fml.binding.FlexoPropertyPathElement;
import org.openflexo.foundation.fml.binding.ModelSlotPathElement;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.EmptyControlGraph;
import org.openflexo.foundation.fml.controlgraph.ExpressionIterationAction;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.IncrementalIterationAction;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.controlgraph.WhileAction;
import org.openflexo.foundation.fml.editionaction.AbstractFetchRequest;
import org.openflexo.foundation.fml.editionaction.AddClassInstance;
import org.openflexo.foundation.fml.editionaction.AddToListAction;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ConnectAction;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;
import org.openflexo.foundation.fml.editionaction.LogAction;
import org.openflexo.foundation.fml.editionaction.NotifyProgressAction;
import org.openflexo.foundation.fml.editionaction.NotifyPropertyChangedAction;
import org.openflexo.foundation.fml.editionaction.RemoveFromListAction;
import org.openflexo.foundation.fml.editionaction.ReturnStatement;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.foundation.fml.md.BasicMetaData;
import org.openflexo.foundation.fml.md.ListMetaData;
import org.openflexo.foundation.fml.md.MetaDataKeyValue;
import org.openflexo.foundation.fml.md.MultiValuedMetaData;
import org.openflexo.foundation.fml.md.SingleMetaData;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstanceParameter;
import org.openflexo.foundation.fml.rt.editionaction.AddVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.CreateFlexoConceptInstanceParameter;
import org.openflexo.foundation.fml.rt.editionaction.CreateTopLevelVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.DeleteFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.DeleteFlexoConceptInstanceParameter;
import org.openflexo.foundation.fml.rt.editionaction.ExecuteBehaviourParameter;
import org.openflexo.foundation.fml.rt.editionaction.FinalizeMatching;
import org.openflexo.foundation.fml.rt.editionaction.FireEvent;
import org.openflexo.foundation.fml.rt.editionaction.FireEventAction;
import org.openflexo.foundation.fml.rt.editionaction.InitiateMatching;
import org.openflexo.foundation.fml.rt.editionaction.MatchCondition;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.MatchingCriteria;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectUniqueFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectUniqueVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectVirtualModelInstance;
import org.openflexo.foundation.fml.ta.FMLModelSlot;
import org.openflexo.foundation.resource.PamelaResourceImpl.IgnoreLoadingEdits;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.utils.FlexoObjectReferenceConverter;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.converter.DataBindingConverter;
import org.openflexo.pamela.converter.FlexoVersionConverter;
import org.openflexo.pamela.converter.RelativePathResourceConverter;
import org.openflexo.pamela.converter.TypeConverter;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.EditingContext;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * The {@link PamelaModelFactory} building the objects of an FML compilation unit: one instance per {@link CompilationUnitResource}.
 * <p>
 * Its PAMELA meta-model is not fixed: it is computed from the model slots used by that compilation unit ({@code use ... as ...;}), adding
 * for each of them the roles, behaviours, edition actions and fetch requests it declares by annotation. A compilation unit using a new
 * technology therefore needs a new factory, which {@link CompilationUnitResource#updateFMLModelFactory(java.util.List)} builds.
 * <p>
 * This factory also carries the converters used to (de)serialize types, bindings, versions, resource paths and object references, and it
 * provides the {@code newXxx()} methods with which the parser and the editor build every FML object.
 *
 * @author sylvain
 *
 */
public class FMLModelFactory extends PamelaModelFactory implements PamelaResourceModelFactory<CompilationUnitResource> {

	protected static final Logger logger = Logger.getLogger(FMLModelFactory.class.getPackage().getName());

	private final CompilationUnitResource compilationUnitResource;
	private final FlexoServiceManager serviceManager;

	private IgnoreLoadingEdits ignoreHandler = null;
	private FlexoUndoManager undoManager = null;

	private TypeConverter typeConverter;
	private FlexoObjectReferenceConverter referenceConverter;

	// TODO: the factory should be instantiated and managed by the ProjectNatureService, which should react to the registering
	// of a new TA, and which is responsible to update the VirtualModelFactory of all VirtualModelResource

	private RelativePathResourceConverter relativePathResourceConverter;

	public FMLModelFactory(CompilationUnitResource compilationUnitResource, FlexoServiceManager serviceManager)
			throws ModelDefinitionException {
		this(compilationUnitResource, serviceManager, serviceManager.getTechnologyAdapterService());
	}

	public FMLModelFactory(CompilationUnitResource compilationUnitResource, FlexoServiceManager serviceManager,
			TechnologyAdapterService taService) throws ModelDefinitionException {
		super(PamelaMetaModelLibrary.retrieveMetaModel(compilationUnitResource != null
				? retrieveTechnologySpecificClasses(compilationUnitResource.getVirtualModelClass(),
						compilationUnitResource.getUsedModelSlots())
				: retrieveTechnologySpecificClasses(taService != null ? taService : serviceManager.getTechnologyAdapterService())));
		this.serviceManager = serviceManager;
		if (taService == null) {
			taService = serviceManager.getTechnologyAdapterService();
		}
		setEditingContext(serviceManager.getEditingContext());
		addConverter(typeConverter = new TypeConverter(taService.getCustomTypeFactories()));
		addConverter(new DataBindingConverter());
		addConverter(new FlexoVersionConverter());
		addConverter(relativePathResourceConverter = new RelativePathResourceConverter(null));
		addConverter(referenceConverter = new FlexoObjectReferenceConverter(serviceManager.getResourceManager()));
		this.compilationUnitResource = compilationUnitResource;
		if (compilationUnitResource != null && compilationUnitResource.getIODelegate() != null
				&& compilationUnitResource.getIODelegate().getSerializationArtefactAsResource() != null) {
			relativePathResourceConverter
					.setContainerResource(compilationUnitResource.getIODelegate().getSerializationArtefactAsResource().getContainer());
		}
		for (TechnologyAdapter<?> ta : taService.getTechnologyAdapters()) {
			ta.initFMLModelFactory(this);
		}

		// Init technology specific type registering
		// TODO: do it only for required technology adapters

	}

	public FlexoServiceManager getServiceManager() {
		return serviceManager;
	}

	@Override
	public CompilationUnitResource getResource() {
		return getVirtualModelResource();
	}

	public CompilationUnitResource getVirtualModelResource() {
		return compilationUnitResource;
	}

	/**
	 * Return the classes to expose in the PAMELA meta-model for ALL model slots of all technology adapters: the model slot classes
	 * themselves and, for each of them, the roles, behaviours, edition actions and fetch requests it declares by annotation
	 *
	 * @param taService
	 *            the technology adapter service providing the technology adapters to consider
	 * @return the classes to expose
	 * @throws ModelDefinitionException
	 */
	public static List<Class<?>> retrieveTechnologySpecificClasses(TechnologyAdapterService taService) throws ModelDefinitionException {
		List<Class<?>> classes = new ArrayList<>();
		classes.add(FMLCompilationUnit.class);
		retrieveTechnologySpecificClassesForModelSlot(FMLModelSlot.class, classes);
		retrieveTechnologySpecificClassesForModelSlot(FMLRTModelSlot.class, classes);
		for (TechnologyAdapter<?> ta : taService.getTechnologyAdapters()) {
			for (Class<? extends ModelSlot<?, ?>> modelSlotClass : new ArrayList<>(ta.getAvailableModelSlotTypes())) {
				if (modelSlotClass != null) {
					retrieveTechnologySpecificClassesForModelSlot(modelSlotClass, classes);
				}
			}
		}

		return classes;
	}

	/**
	 * Return the classes to expose in the PAMELA meta-model for the supplied USED model slots only: each model slot class and the roles,
	 * behaviours, edition actions and fetch requests it declares by annotation. This is the variant used for a compilation unit, whose used
	 * model slots are known from its {@code use} declarations.
	 *
	 * @param baseClass
	 *            the {@link VirtualModel} class of the compilation unit
	 * @param usedModelSlots
	 *            the model slot classes used by the compilation unit
	 * @return the classes to expose
	 * @throws ModelDefinitionException
	 */
	public static List<Class<?>> retrieveTechnologySpecificClasses(Class<? extends VirtualModel> baseClass,
			List<Class<? extends ModelSlot<?, ?>>> usedModelSlots) throws ModelDefinitionException {
		List<Class<?>> classes = new ArrayList<>();
		classes.add(FMLCompilationUnit.class);
		retrieveTechnologySpecificClassesForModelSlot(FMLModelSlot.class, classes);
		retrieveTechnologySpecificClassesForModelSlot(FMLRTModelSlot.class, classes);
		if (usedModelSlots != null) {
			for (Class<? extends ModelSlot<?, ?>> modelSlotClass : usedModelSlots) {
				retrieveTechnologySpecificClassesForModelSlot(modelSlotClass, classes);
			}
		}

		return classes;
	}

	private static void retrieveTechnologySpecificClassesForModelSlot(Class<? extends ModelSlot<?, ?>> modelSlotClass,
			List<Class<?>> classes) {
		classes.add(modelSlotClass);

		DeclareFlexoRoles prDeclarations = modelSlotClass.getAnnotation(DeclareFlexoRoles.class);
		if (prDeclarations != null) {
			for (Class<? extends FlexoRole> roleClass : prDeclarations.value()) {
				classes.add(roleClass);
			}
		}
		DeclareFlexoBehaviours fbDeclarations = modelSlotClass.getAnnotation(DeclareFlexoBehaviours.class);
		if (fbDeclarations != null) {
			for (Class<? extends FlexoBehaviour> behaviourClass : fbDeclarations.value()) {
				classes.add(behaviourClass);
			}
		}
		DeclareEditionActions eaDeclarations = modelSlotClass.getAnnotation(DeclareEditionActions.class);
		if (eaDeclarations != null) {
			for (Class<? extends EditionAction> editionActionClass : eaDeclarations.value()) {
				classes.add(editionActionClass);
			}
		}
		DeclareFetchRequests frDeclarations = modelSlotClass.getAnnotation(DeclareFetchRequests.class);
		if (frDeclarations != null) {
			for (Class<? extends AbstractFetchRequest> fetchRequestClass : frDeclarations.value()) {
				classes.add(fetchRequestClass);
			}
		}
	}

	public FMLCompilationUnit newCompilationUnit() {
		return newInstance(FMLCompilationUnit.class);
	}

	public JavaImportDeclaration newJavaImportDeclaration() {
		return newInstance(JavaImportDeclaration.class);
	}

	public ElementImportDeclaration newElementImportDeclaration() {
		return newInstance(ElementImportDeclaration.class);
	}

	public TypeDeclaration newTypeDeclaration() {
		return newInstance(TypeDeclaration.class);
	}

	public VirtualModel newVirtualModel() {
		VirtualModel returned = newInstance(VirtualModel.class);
		returned.initializeDeserialization(this);
		return returned;
	}

	public BasicMetaData newBasicMetaData(String key) {
		BasicMetaData returned = newInstance(BasicMetaData.class);
		returned.initializeDeserialization(this);
		returned.setKey(key);
		return returned;
	}

	public <T> SingleMetaData<T> newSingleMetaData(String key) {
		SingleMetaData<T> returned = newInstance(SingleMetaData.class);
		returned.initializeDeserialization(this);
		returned.setKey(key);
		return returned;
	}

	public <T> SingleMetaData<T> newSingleMetaData(String key, T value, Class<T> type) {
		SingleMetaData<T> returned = newInstance(SingleMetaData.class);
		returned.initializeDeserialization(this);
		returned.setKey(key);
		returned.setValue(value, type);
		return returned;
	}

	public MultiValuedMetaData newMultiValuedMetaData(String key) {
		MultiValuedMetaData returned = newInstance(MultiValuedMetaData.class);
		returned.initializeDeserialization(this);
		returned.setKey(key);
		return returned;
	}

	public <T> MetaDataKeyValue<T> newMetaDataKeyValue(String key) {
		MetaDataKeyValue<T> returned = newInstance(MetaDataKeyValue.class);
		returned.initializeDeserialization(this);
		returned.setKey(key);
		return returned;
	}

	public <T> MetaDataKeyValue<T> newMetaDataKeyValue(String key, T value, Class<T> type) {
		MetaDataKeyValue<T> returned = newInstance(MetaDataKeyValue.class);
		returned.initializeDeserialization(this);
		returned.setKey(key);
		returned.setValue(value, type);
		return returned;
	}

	public ListMetaData newListMetaData(String key) {
		ListMetaData returned = newInstance(ListMetaData.class);
		returned.initializeDeserialization(this);
		returned.setKey(key);
		return returned;
	}

	public <MS extends ModelSlot<?, ?>> UseModelSlotDeclaration newUseModelSlotDeclaration(Class<MS> modelSlotClass) {
		UseModelSlotDeclaration returned = newInstance(UseModelSlotDeclaration.class);
		returned.setModelSlotClass(modelSlotClass);
		return returned;
	}

	public NamespaceDeclaration newNamespaceDeclaration() {
		return newInstance(NamespaceDeclaration.class);
	}

	public SynchronizationScheme newSynchronizationScheme() {
		return newInstance(SynchronizationScheme.class);
	}

	public SimpleInvariant newSimpleInvariant() {
		return newInstance(SimpleInvariant.class);
	}

	public IterationInvariant newIterationInvariant() {
		return newInstance(IterationInvariant.class);
	}

	public CreationScheme newCreationScheme() {
		return newInstance(CreationScheme.class);
	}

	public CloningScheme newCloningScheme() {
		return newInstance(CloningScheme.class);
	}

	public ActionScheme newActionScheme() {
		return newInstance(ActionScheme.class);
	}

	public NavigationScheme newNavigationScheme() {
		return newInstance(NavigationScheme.class);
	}

	public DeletionScheme newDeletionScheme() {
		return newInstance(DeletionScheme.class);
	}

	public EventListener newEventListener() {
		return newInstance(EventListener.class);
	}

	public AbstractProperty<?> newAbstractProperty() {
		return newInstance(AbstractProperty.class);
	}

	public PrimitiveRole<?> newPrimitiveRole() {
		return newInstance(PrimitiveRole.class);
	}

	public JavaRole<?> newJavaRole() {
		return newInstance(JavaRole.class);
	}

	public ExpressionProperty<?> newExpressionProperty() {
		return newInstance(ExpressionProperty.class);
	}

	public GetProperty<?> newGetProperty() {
		GetProperty<?> returned = newInstance(GetProperty.class);
		returned.setGetControlGraph(newEmptyControlGraph());
		return returned;
	}

	public GetSetProperty<?> newGetSetProperty() {
		GetSetProperty<?> returned = newInstance(GetSetProperty.class);
		returned.setGetControlGraph(newEmptyControlGraph());
		returned.setSetControlGraph(newEmptyControlGraph());
		return returned;
	}

	public Sequence newSequence() {
		Sequence returned = newInstance(Sequence.class);
		return returned;
	}

	public Sequence newSequence(FMLControlGraph cg1, FMLControlGraph cg2) {
		Sequence returned = newInstance(Sequence.class);
		returned.setControlGraph1(cg1);
		returned.setControlGraph2(cg2);
		return returned;
	}

	public EmptyControlGraph newEmptyControlGraph() {
		return newInstance(EmptyControlGraph.class);
	}

	public AddFlexoConceptInstanceParameter newAddFlexoConceptInstanceParameter(FlexoBehaviourParameter p) {
		AddFlexoConceptInstanceParameter returned = newInstance(AddFlexoConceptInstanceParameter.class);
		returned.setParam(p);
		return returned;
	}

	public DeleteFlexoConceptInstanceParameter newDeleteFlexoConceptInstanceParameter(FlexoBehaviourParameter p) {
		DeleteFlexoConceptInstanceParameter returned = newInstance(DeleteFlexoConceptInstanceParameter.class);
		returned.setParam(p);
		return returned;
	}

	public FlexoConceptBehaviouralFacet newFlexoConceptBehaviouralFacet(FlexoConcept flexoConcept) {
		FlexoConceptBehaviouralFacet returned = newInstance(FlexoConceptBehaviouralFacet.class);
		returned.setFlexoConcept(flexoConcept);
		return returned;
	}

	public FlexoConceptStructuralFacet newFlexoConceptStructuralFacet(FlexoConcept flexoConcept) {
		FlexoConceptStructuralFacet returned = newInstance(FlexoConceptStructuralFacet.class);
		returned.setFlexoConcept(flexoConcept);
		return returned;
	}

	public InnerConceptsFacet newInnerConceptsFacet(FlexoConcept flexoConcept) {
		InnerConceptsFacet returned = newInstance(InnerConceptsFacet.class);
		returned.setFlexoConcept(flexoConcept);
		return returned;
	}

	public FlexoBehaviourParameter newParameter(FlexoBehaviour behaviour) {
		FlexoBehaviourParameter returned = newInstance(FlexoBehaviourParameter.class);
		returned.initializeDeserialization(this);
		if (behaviour != null) {
			behaviour.addToParameters(returned);
		}
		return returned;
	}

	public FetchRequestCondition newFetchRequestCondition() {
		return newInstance(FetchRequestCondition.class);
	}

	public MatchCondition newMatchCondition() {
		return newInstance(MatchCondition.class);
	}

	public FlexoConceptInspector newFlexoConceptInspector(FlexoConcept ep) {
		FlexoConceptInspector returned = newInstance(FlexoConceptInspector.class);
		returned.setFlexoConcept(ep);
		return returned;
	}

	public InspectorEntry newInspectorEntry() {
		return newInstance(InspectorEntry.class);
	}

	public InspectorEntry newInspectorEntry(FlexoConceptInspector inspector) {
		InspectorEntry returned = newInspectorEntry();
		inspector.addToEntries(returned);
		return returned;
	}

	public CreateFlexoConceptInstanceParameter newCreateFlexoConceptInstanceParameter(FlexoBehaviourParameter p) {
		CreateFlexoConceptInstanceParameter returned = newInstance(CreateFlexoConceptInstanceParameter.class);
		returned.setParam(p);
		return returned;
	}

	public ExecuteBehaviourParameter newExecuteBehaviourParameter(FlexoBehaviourParameter p) {
		ExecuteBehaviourParameter returned = newInstance(ExecuteBehaviourParameter.class);
		returned.setParam(p);
		return returned;
	}

	public MatchingCriteria newMatchingCriteria(FlexoProperty<?> pr) {
		MatchingCriteria returned = newInstance(MatchingCriteria.class);
		returned.setFlexoProperty(pr);
		return returned;
	}

	public FlexoConceptInstanceRole newFlexoConceptInstanceRole() {
		return newInstance(FlexoConceptInstanceRole.class);
	}

	public AddFlexoConceptInstance<?> newAddFlexoConceptInstance() {
		return newInstance(AddFlexoConceptInstance.class);
	}

	public AddClassInstance newAddClassInstance() {
		return newInstance(AddClassInstance.class);
	}

	public AddVirtualModelInstance newAddVirtualModelInstance() {
		return newInstance(AddVirtualModelInstance.class);
	}

	public CreateTopLevelVirtualModelInstance newCreateTopLevelVirtualModelInstance() {
		return newInstance(CreateTopLevelVirtualModelInstance.class);
	}

	public SelectFlexoConceptInstance<?> newSelectFlexoConceptInstance() {
		return newInstance(SelectFlexoConceptInstance.class);
	}

	public SelectUniqueFlexoConceptInstance<?> newSelectUniqueFlexoConceptInstance() {
		return newInstance(SelectUniqueFlexoConceptInstance.class);
	}

	public SelectVirtualModelInstance<?> newSelectVirtualModelInstance() {
		return newInstance(SelectVirtualModelInstance.class);
	}

	public SelectUniqueVirtualModelInstance<?> newSelectUniqueVirtualModelInstance() {
		return newInstance(SelectUniqueVirtualModelInstance.class);
	}

	public FlexoConcept newFlexoConcept() {
		return newInstance(FlexoConcept.class);
	}

	public FlexoEvent newFlexoEvent() {
		return newInstance(FlexoEvent.class);
	}

	public FlexoEnum newFlexoEnum() {
		return newInstance(FlexoEnum.class);
	}

	public FlexoEnumValue newFlexoEnumValue() {
		return newInstance(FlexoEnumValue.class);
	}

	public FlexoEnumValue newFlexoEnumValue(FlexoEnum flexoEnum) {
		FlexoEnumValue returned = newInstance(FlexoEnumValue.class);
		flexoEnum.addToValues(returned);
		return returned;
	}

	public DeleteAction newDeleteAction() {
		return newInstance(DeleteAction.class);
	}

	public DeleteFlexoConceptInstance newDeleteFlexoConceptInstanceAction() {
		return newInstance(DeleteFlexoConceptInstance.class);
	}

	public MatchFlexoConceptInstance newMatchFlexoConceptInstance() {
		return newInstance(MatchFlexoConceptInstance.class);
	}

	public InitiateMatching newInitiateMatching() {
		return newInstance(InitiateMatching.class);
	}

	public FinalizeMatching newFinalizeMatching() {
		return newInstance(FinalizeMatching.class);
	}

	@SuppressWarnings("unchecked")
	public <T> RemoveFromListAction<T> newRemoveFromListAction() {
		return newInstance(RemoveFromListAction.class);
	}

	@SuppressWarnings("unchecked")
	public <T> AddToListAction<T> newAddToListAction() {
		return newInstance(AddToListAction.class);
	}

	@SuppressWarnings("unchecked")
	public <T> AssignationAction<T> newAssignationAction() {
		return newInstance(AssignationAction.class);
	}

	public <T> AssignationAction<T> newAssignationAction(AssignableAction<T> assignableAction) {
		AssignationAction<T> returned = newAssignationAction();
		returned.setAssignableAction(assignableAction);
		return returned;
	}

	public <T> AssignationAction<T> newAssignationAction(String expressionAsString) {
		AssignationAction<T> returned = newAssignationAction();
		returned.setAssignableAction(newExpressionAction(expressionAsString));
		return returned;
	}

	public <T> DeclarationAction<T> newDeclarationAction() {
		return newInstance(DeclarationAction.class);
	}

	public <T> DeclarationAction<T> newDeclarationAction(String variableName, AssignableAction<T> assignableAction) {
		DeclarationAction<T> returned = newDeclarationAction();
		returned.setVariableName(variableName);
		returned.setAssignableAction(assignableAction);
		return returned;
	}

	public <T> DeclarationAction<T> newDeclarationAction(String variableName, String expressionAsString) {
		DeclarationAction<T> returned = newDeclarationAction();
		returned.setVariableName(variableName);
		returned.setAssignableAction(newExpressionAction(expressionAsString));
		return returned;
	}

	public <T> ReturnStatement<T> newReturnStatement() {
		return newInstance(ReturnStatement.class);
	}

	public <T> ReturnStatement<T> newReturnStatement(AssignableAction<T> assignableAction) {
		ReturnStatement<T> returned = newReturnStatement();
		returned.setAssignableAction(assignableAction);
		return returned;
	}

	public LogAction newLogAction() {
		return newInstance(LogAction.class);
	}

	public ConnectAction newConnectAction() {
		return newInstance(ConnectAction.class);
	}

	public FireEvent newFireEvent() {
		return newInstance(FireEvent.class);
	}

	public NotifyPropertyChangedAction newNotifyPropertyChangedAction() {
		return newInstance(NotifyPropertyChangedAction.class);
	}

	public NotifyProgressAction newNotifyProgressAction() {
		return newInstance(NotifyProgressAction.class);
	}

	public FireEventAction newFireEventAction() {
		return newInstance(FireEventAction.class);
	}

	public <T> ExpressionAction<T> newExpressionAction() {
		return newInstance(ExpressionAction.class);
	}

	public <T> ExpressionAction<T> newExpressionAction(String expressionAsString) {
		ExpressionAction<T> returned = newExpressionAction();
		DataBinding<T> expression = new DataBinding<>(expressionAsString, null);
		returned.setExpression(expression);
		return returned;
	}

	public <T> ExpressionAction<T> newExpressionAction(DataBinding<T> expression) {
		ExpressionAction<T> returned = newExpressionAction();
		returned.setExpression(expression);
		return returned;
	}

	public ConditionalAction newConditionalAction() {
		ConditionalAction returned = newInstance(ConditionalAction.class);
		returned.setThenControlGraph(newEmptyControlGraph());
		return returned;
	}

	public IterationAction newIterationAction() {
		IterationAction returned = newInstance(IterationAction.class);
		returned.setControlGraph(newEmptyControlGraph());
		return returned;
	}

	public ExpressionIterationAction newExpressionIterationAction() {
		ExpressionIterationAction returned = newInstance(ExpressionIterationAction.class);
		returned.setControlGraph(newEmptyControlGraph());
		return returned;
	}

	public WhileAction newWhileAction() {
		WhileAction returned = newInstance(WhileAction.class);
		returned.setControlGraph(newEmptyControlGraph());
		return returned;
	}

	public IncrementalIterationAction newIncrementalIterationAction() {
		IncrementalIterationAction returned = newInstance(IncrementalIterationAction.class);
		returned.setControlGraph(newEmptyControlGraph());
		return returned;
	}

	public SelectFlexoConceptInstance newSelectFlexoConceptInstanceAction() {
		return newInstance(SelectFlexoConceptInstance.class);
	}

	public <P extends FlexoProperty<?>> FlexoPropertyPathElement<P> newFlexoPropertyPathElement(IBindingPathElement parent, P property,
			Bindable bindable) {
		FlexoPropertyPathElement<P> returned = newInstance(FlexoPropertyPathElement.class);
		returned.setParent(parent);
		returned.setProperty(property);
		returned.setBindable(bindable);
		return returned;
	}

	public <P extends FlexoProperty<?>> FlexoPropertyPathElement<P> newFlexoPropertyPathElement(IBindingPathElement parent,
			String propertyName, Bindable bindable) {
		FlexoPropertyPathElement<P> returned = newInstance(FlexoPropertyPathElement.class);
		returned.setParent(parent);
		returned.setPropertyName(propertyName);
		returned.setBindable(bindable);
		return returned;
	}

	public <MS extends ModelSlot<?, ?>> ModelSlotPathElement<MS> newModelSlotPathElement(IBindingPathElement parent, MS modelSlot,
			Bindable bindable) {
		ModelSlotPathElement<MS> returned = newInstance(ModelSlotPathElement.class);
		returned.setParent(parent);
		returned.setProperty(modelSlot);
		returned.setBindable(bindable);
		return returned;
	}

	public <MS extends ModelSlot<?, ?>> ModelSlotPathElement<MS> newModelSlotPathElement(IBindingPathElement parent, String modelSlotName,
			Bindable bindable) {
		ModelSlotPathElement<MS> returned = newInstance(ModelSlotPathElement.class);
		returned.setParent(parent);
		returned.setPropertyName(modelSlotName);
		returned.setBindable(bindable);
		return returned;
	}

	public CreationSchemePathElement newCreationSchemePathElement(FlexoConceptInstanceType type, IBindingPathElement parent,
			String constructorName, List<DataBinding<?>> args, Bindable bindable) {
		CreationSchemePathElement returned = newInstance(CreationSchemePathElement.class);
		returned.setType(type);
		returned.setParent(parent);
		returned.setMethodName(constructorName);
		returned.setBindable(bindable);
		returned.setArguments(args);
		return returned;
	}

	public CreationSchemePathElement newCreationSchemePathElement(IBindingPathElement parent, AbstractCreationScheme creationScheme,
			List<DataBinding<?>> args, Bindable bindable) {
		CreationSchemePathElement returned = newInstance(CreationSchemePathElement.class);
		if (creationScheme != null && creationScheme.getFlexoConcept() != null) {
			returned.setType(creationScheme.getFlexoConcept().getInstanceType());
		}
		returned.setParent(parent);
		returned.setFunction(creationScheme);
		returned.setBindable(bindable);
		returned.setArguments(args);
		return returned;
	}

	public <M extends FMLObject, T> FMLSimplePropertyValue<M, T> newSimplePropertyValue() {
		FMLSimplePropertyValue<M, T> returned = newInstance(FMLSimplePropertyValue.class);
		return returned;
	}

	public <M extends FMLObject, T> FMLSimplePropertyValue<M, T> newSimplePropertyValue(FMLProperty<M, T> property, M object, T value) {
		FMLSimplePropertyValue<M, T> returned = newInstance(FMLSimplePropertyValue.class);
		returned.setProperty(property);
		returned.setObject(object);
		returned.setValue(value);
		return returned;
	}

	public <M extends FMLObject, T extends Type> FMLTypePropertyValue<M, T> newTypePropertyValue() {
		FMLTypePropertyValue<M, T> returned = newInstance(FMLTypePropertyValue.class);
		return returned;
	}

	public <M extends FMLObject, T extends Type> FMLTypePropertyValue<M, T> newTypePropertyValue(FMLProperty<M, T> property, M object,
			T value) {
		FMLTypePropertyValue<M, T> returned = newInstance(FMLTypePropertyValue.class);
		returned.setProperty(property);
		returned.setObject(object);
		returned.setType(value);
		return returned;
	}

	public <M extends FMLObject, E extends Enum<E>> FMLEnumPropertyValue<M, E> newEnumPropertyValue() {
		FMLEnumPropertyValue<M, E> returned = newInstance(FMLEnumPropertyValue.class);
		return returned;
	}

	public <M extends FMLObject, T extends FMLObject> FMLInstancePropertyValue<M, T> newInstancePropertyValue() {
		FMLInstancePropertyValue<M, T> returned = newInstance(FMLInstancePropertyValue.class);
		return returned;
	}

	public <M extends FMLObject, T extends FMLObject> FMLInstancePropertyValue<M, T> newInstancePropertyValue(FMLProperty<M, T> property,
			M object) {
		FMLInstancePropertyValue<M, T> returned = newInstance(FMLInstancePropertyValue.class);
		returned.setProperty(property);
		returned.setObject(object);
		return returned;
	}

	public <M extends FMLObject, T extends FMLObject> FMLInstancesListPropertyValue<M, T> newInstancesListPropertyValue() {
		FMLInstancesListPropertyValue<M, T> returned = newInstance(FMLInstancesListPropertyValue.class);
		return returned;
	}

	public <M extends FMLObject, T extends FMLObject> FMLInstancesListPropertyValue<M, T> newInstancesListPropertyValue(
			FMLProperty<M, List<T>> property, M object) {
		FMLInstancesListPropertyValue<M, T> returned = newInstance(FMLInstancesListPropertyValue.class);
		returned.setProperty(property);
		returned.setObject(object);
		return returned;
	}

	public WrappedFMLObject<?> newWrappedFMLObject() {
		return newInstance(WrappedFMLObject.class);
	}

	private <O extends FMLObject> WrappedFMLObject<O> newWrappedFMLObject(O object) {
		WrappedFMLObject<O> returned = newInstance(WrappedFMLObject.class);
		returned.setObject(object);
		return returned;
	}

	private Map<FMLObject, WrappedFMLObject<?>> wrappedObjects = new HashMap<>();

	public <O extends FMLObject> WrappedFMLObject<O> getWrappedFMLObject(O object) {
		WrappedFMLObject<O> returned = (WrappedFMLObject<O>) wrappedObjects.get(object);
		if (returned == null) {
			returned = newWrappedFMLObject(object);
			wrappedObjects.put(object, returned);
		}
		return returned;
	}

	@Override
	public synchronized void startDeserializing() {

		typeConverter.startDeserializing();

		EditingContext editingContext = getResource().getServiceManager().getEditingContext();

		if (editingContext != null && editingContext.getUndoManager() instanceof FlexoUndoManager) {
			undoManager = (FlexoUndoManager) editingContext.getUndoManager();
			undoManager.addToIgnoreHandlers(ignoreHandler = new IgnoreLoadingEdits(getResource()));
		}

	}

	@Override
	public synchronized void stopDeserializing() {

		typeConverter.stopDeserializing();

		if (ignoreHandler != null) {
			undoManager.removeFromIgnoreHandlers(ignoreHandler);
		}

	}

	@Override
	public <I> void objectHasBeenDeserialized(I newlyCreatedObject, Class<I> implementedInterface) {
		super.objectHasBeenDeserialized(newlyCreatedObject, implementedInterface);
		if (getResource() != null) {
			if (newlyCreatedObject instanceof FlexoObject) {
				getResource().setLastID(((FlexoObject) newlyCreatedObject).getFlexoID());
			}
		}
		else {
			logger.warning("Could not access resource beeing deserialized");
		}
		// No localized dictionary is created here any more: a compilation unit gets one on demand, through "Localize..."
	}

	@Override
	public <I> void objectHasBeenCreated(final I newlyCreatedObject, final Class<I> implementedInterface) {
		super.objectHasBeenCreated(newlyCreatedObject, implementedInterface);
	}

	public TypeConverter getTypeConverter() {
		return typeConverter;
	}

	public FlexoObjectReferenceConverter getReferenceConverter() {
		return referenceConverter;
	}

}
