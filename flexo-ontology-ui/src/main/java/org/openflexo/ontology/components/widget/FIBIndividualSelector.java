/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.ontology.components.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.components.widget.FIBFlexoObjectSelector;
import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.binding.FMLBindingFactory;
import org.openflexo.foundation.ontology.FlexoOntologyObjectImpl;
import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyIndividual;
import org.openflexo.foundation.ontology.IndividualOfClass;
import org.openflexo.foundation.ontology.OntologyUtils;
import org.openflexo.foundation.resource.ResourceManager;
import org.openflexo.foundation.technologyadapter.FlexoModelResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.ontology.controller.FlexoOntologyTechnologyAdapterController;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Widget allowing to select an IFlexoOntologyIndividual<br>
 * 
 * This widget provides many configuration options:
 * <ul>
 * <li>context: required, defines ontology context</li>
 * <li>strictMode: required, default is false, when true, indicates that properties are retrieved from declared context ontology only</li>
 * <li>hierarchicalMode: required, default is true, defines if properties are stored relative to a storage class, defined either as the
 * domain class, or the top-level class where this property is used as a restriction</li>
 * <li>type: when set, defines type class of searched individuals</li>
 * </ul>
 * 
 * Additionnaly, this widget provides a way to define custom renderers for different types of individuals. See
 * {@link #setRepresentationForIndividualOfClass(String, String, IFlexoOntologyClass)}
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBIndividualSelector extends FIBFlexoObjectSelector<IFlexoOntologyIndividual> implements Bindable {
	static final Logger logger = Logger.getLogger(FIBIndividualSelector.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/FIBIndividualSelector.fib");

	private ResourceManager resourceManager;

	protected OntologyBrowserModel model = null;
	private TechnologyAdapter technologyAdapter;

	private final BindingModel bindingModel;

	private String defaultRenderer = null;

	private static FMLBindingFactory FLEXO_CONCEPT_BINDING_FACTORY = new FMLBindingFactory(null);

	private final HashMap<IFlexoOntologyClass, DataBinding<String>> renderers;

	public FIBIndividualSelector(IFlexoOntologyIndividual editedObject) {
		super(editedObject);
		bindingModel = new BindingModel();
		renderers = new HashMap<IFlexoOntologyClass, DataBinding<String>>();
		model = makeBrowserModel(editedObject != null ? editedObject.getOntology() : null);
	}

	@Override
	public BindingFactory getBindingFactory() {
		return FLEXO_CONCEPT_BINDING_FACTORY;
	}

	@Override
	public BindingModel getBindingModel() {
		return bindingModel;
	}

	@Override
	public Resource getFIBResource() {
		return FIB_FILE;
	}

	@Override
	public Class<IFlexoOntologyIndividual> getRepresentedType() {
		return IFlexoOntologyIndividual.class;
	}

	public ResourceManager getResourceManager() {
		if (resourceManager == null && getServiceManager() != null) {
			resourceManager = getServiceManager().getResourceManager();
		}
		return resourceManager;
	}

	@CustomComponentParameter(name = "resourceManager", type = CustomComponentParameter.Type.MANDATORY)
	public void setResourceManager(ResourceManager resourceManager) {

		if (this.resourceManager != resourceManager) {
			ResourceManager oldValue = this.resourceManager;
			this.resourceManager = resourceManager;
			getPropertyChangeSupport().firePropertyChange("resourceManager", oldValue, resourceManager);
			updateCustomPanel(getEditedObject());
		}
	}

	public String getRenderer() {
		if (getType() != null) {
			if (renderers.get(getType()) != null) {
				return renderers.get(getType()).toString();
			}
		}
		return defaultRenderer;
	}

	@CustomComponentParameter(name = "renderer", type = CustomComponentParameter.Type.OPTIONAL)
	public void setRenderer(String renderer) {
		defaultRenderer = renderer;
		if (getType() != null) {
			setRepresentationForIndividualOfClass("individual", renderer, getType());
		}
	}

	public String renderObject(FlexoOntologyObjectImpl object) {
		if (object instanceof IFlexoOntologyIndividual) {
			System.out.println("For " + object + " render " + renderedString((IFlexoOntologyIndividual) object));
			return renderedString((IFlexoOntologyIndividual) object);
		}
		return object.getName();
	}

	/**
	 * Provides custom renderer for individuals of a given ontology class.<br>
	 * For example call: <code>
	 * 	selector.setRepresentationForIndividualOfClass("personne", "personne.nom+' '+personne.prenom",
	 * 		o.getClass("http://www.openflexo.org/test/Family.owl#Personne"));
	 * </code>
	 * 
	 * @param variableName
	 * @param expression
	 * @param type
	 */
	public void setRepresentationForIndividualOfClass(String variableName, String expression, IFlexoOntologyClass type) {
		if (renderers.get(type) == null || !renderers.get(type).toString().equals(expression)) {
			if (renderers.get(type) != null) {
				logger.info("Was " + renderers.get(type).toString() + " now " + expression);
			}
			// OntologyIndividualPathElement newPathElement = new OntologyIndividualPathElement(variableName, type, null);
			BindingVariable newPathElement = new BindingVariable(variableName, IndividualOfClass.getIndividualOfClass(type));
			if (bindingModel.bindingVariableNamed(variableName) != null) {
				logger.warning("Duplicated binding variable " + variableName);
				bindingModel.removeFromBindingVariables(bindingModel.bindingVariableNamed(variableName));
			}
			bindingModel.addToBindingVariables(newPathElement);
			DataBinding<String> db = new DataBinding<String>(expression, this, String.class, DataBinding.BindingDefinitionType.GET);
			renderers.put(type, db);
		}
	}

	protected DataBinding<String> getRenderer(IFlexoOntologyIndividual individual) {

		if (individual == null) {
			return null;
		}

		// If default renderer was not already applied to default type, then do it now
		if (StringUtils.isNotEmpty(defaultRenderer) && getType() != null && renderers.get(getType()) == null) {
			setRepresentationForIndividualOfClass("individual", defaultRenderer, getType());
		}

		if (renderers == null) {
			return null;
		}
		List<IFlexoOntologyClass> matchingClasses = new ArrayList<IFlexoOntologyClass>();
		for (IFlexoOntologyClass cl : renderers.keySet()) {
			if (cl.isSuperConceptOf(individual)) {
				matchingClasses.add(cl);
			}
		}
		IFlexoOntologyClass mostSpecializedClass = OntologyUtils.getMostSpecializedClass(matchingClasses);

		return renderers.get(mostSpecializedClass);
	}

	public class BindingEvaluator implements BindingEvaluationContext {

		public BindingEvaluator(IFlexoOntologyIndividual individual) {
		}

		@Override
		public Object getValue(BindingVariable variable) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Override
	public String renderedString(final IFlexoOntologyIndividual editedObject) {

		if (editedObject == null) {
			return null;
		}

		System.out.println("Trying to render " + editedObject + " renderer=" + getRenderer(editedObject));
		DataBinding<String> renderer = getRenderer(editedObject);

		if (renderer == null) {
			return editedObject.getName();
		}

		// FD remove code : test is a tautology because of previous test editedObject != null is true
		// if (editedObject != null) {
		try {
			String returned = renderer.getBindingValue(new BindingEvaluationContext() {
				@Override
				public Object getValue(BindingVariable variable) {
					return editedObject;
				}
			});
			return returned;
		} catch (Exception e) {
			return editedObject.getName();
		}
		// }
		// return editedObject.getName();
	}

	public String getContextOntologyURI() {
		if (getContext() != null) {
			return getContext().getURI();
		}
		return null;
	}

	@CustomComponentParameter(name = "contextOntologyURI", type = CustomComponentParameter.Type.MANDATORY)
	public void setContextOntologyURI(String ontologyURI) {
		// logger.info(">>>>>>>>>>>> Sets ontology with " + ontologyURI);
		if (getResourceManager() != null) {
			FlexoModelResource<?, ?, ?, ?> modelResource = getResourceManager().getModelWithURI(ontologyURI);
			if (modelResource != null && modelResource.getModel() instanceof IFlexoOntology) {
				setContext((IFlexoOntology) modelResource.getModel());
			}
		}
	}

	public IFlexoOntology getContext() {
		return getModel().getContext();
	}

	@CustomComponentParameter(name = "context", type = CustomComponentParameter.Type.MANDATORY)
	public void setContext(IFlexoOntology context) {
		IFlexoOntology oldValue = getContext();
		if (oldValue != context) {
			getModel().setContext(context);
			update();
			getPropertyChangeSupport().firePropertyChange("context", oldValue, context);
			getPropertyChangeSupport().firePropertyChange("ontology", oldValue, context);
		}
	}

	public IFlexoOntology getOntology() {
		return getContext();
	}

	public void setOntology(IFlexoOntology<?> ontology) {
		setContext(ontology);
	}

	public IFlexoOntologyClass getType() {
		return getModel().getRootClass();
	}

	@CustomComponentParameter(name = "type", type = CustomComponentParameter.Type.OPTIONAL)
	public void setType(IFlexoOntologyClass type) {
		IFlexoOntologyClass oldValue = getType();
		if (oldValue != type) {
			model.setRootClass(type);
			update();
			getPropertyChangeSupport().firePropertyChange("type", oldValue, type);
		}
	}

	public String getTypeURI() {
		if (getType() != null) {
			return getType().getURI();
		}
		return null;
	}

	@CustomComponentParameter(name = "typeURI", type = CustomComponentParameter.Type.MANDATORY)
	public void setTypeURI(String aClassURI) {
		// logger.info("Sets typeClassURI with " + aClassURI + " context=" + getContext());
		if (getContext() != null) {
			IFlexoOntologyClass typeClass = getContext().getClass(aClassURI);
			if (typeClass != null) {
				setType(typeClass);
			}
		}
	}

	public boolean getStrictMode() {
		return getModel().getStrictMode();
	}

	@CustomComponentParameter(name = "strictMode", type = CustomComponentParameter.Type.OPTIONAL)
	public void setStrictMode(boolean strictMode) {
		boolean oldValue = getStrictMode();
		if (oldValue != strictMode) {
			model.setStrictMode(strictMode);
			update();
			getPropertyChangeSupport().firePropertyChange("strictMode", oldValue, strictMode);
		}
	}

	public boolean getHierarchicalMode() {
		return getModel().getHierarchicalMode();
	}

	@CustomComponentParameter(name = "hierarchicalMode", type = CustomComponentParameter.Type.OPTIONAL)
	public void setHierarchicalMode(boolean hierarchicalMode) {
		boolean oldValue = getHierarchicalMode();
		if (oldValue != hierarchicalMode) {
			model.setHierarchicalMode(hierarchicalMode);
			update();
			getPropertyChangeSupport().firePropertyChange("hierarchicalMode", oldValue, hierarchicalMode);
		}
	}

	public TechnologyAdapter getTechnologyAdapter() {
		return technologyAdapter;
	}

	public void setTechnologyAdapter(TechnologyAdapter technologyAdapter) {
		this.technologyAdapter = technologyAdapter;
	}

	protected OntologyBrowserModel performBuildOntologyBrowserModel(IFlexoOntology ontology) {
		return new OntologyBrowserModel(ontology);
	}

	/**
	 * Build browser model<br>
	 * Override this method when required
	 * 
	 * @return
	 */
	protected OntologyBrowserModel makeBrowserModel(IFlexoOntology ontology) {
		OntologyBrowserModel returned = null;
		if (getTechnologyAdapter() != null) {
			// Use technology specific browser model
			TechnologyAdapterController<?> technologyAdapterController = getTechnologyAdapter().getTechnologyAdapterService()
					.getServiceManager().getService(TechnologyAdapterControllerService.class)
					.getTechnologyAdapterController(technologyAdapter);
			if (technologyAdapterController instanceof FlexoOntologyTechnologyAdapterController) {
				returned = ((FlexoOntologyTechnologyAdapterController) technologyAdapterController).makeOntologyBrowserModel(ontology);
			}
		}
		if (returned == null) {
			// Use default
			returned = performBuildOntologyBrowserModel(ontology);
		}

		returned.disableAutoUpdate();
		returned.setStrictMode(false);
		returned.setHierarchicalMode(true);
		returned.setDisplayPropertiesInClasses(false);
		returned.setShowClasses(false);
		returned.setShowIndividuals(true);
		returned.setShowObjectProperties(false);
		returned.setShowDataProperties(false);
		returned.setShowAnnotationProperties(false);
		returned.enableAutoUpdate();
		returned.recomputeStructure();

		return returned;
	}

	public OntologyBrowserModel<?> getModel() {
		return model;
	}

	public void update() {
		getPropertyChangeSupport().firePropertyChange("model", null, getModel());
	}

	@Override
	public void notifiedBindingChanged(DataBinding<?> dataBinding) {
	}

	@Override
	public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
	}

}
