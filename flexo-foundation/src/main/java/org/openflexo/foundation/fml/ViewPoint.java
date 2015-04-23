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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.binding.FlexoConceptBindingFactory;
import org.openflexo.foundation.fml.binding.ViewPointBindingModel;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.ViewPointResourceImpl;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.utils.XMLUtils;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.Validable;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.StringUtils;

/**
 * In the Openflexo Viewpoint Architecture a {@link ViewPoint} is the metamodel level of model federation.<br>
 * A {@link View} (run-time context of model federation) is conform to a {@link ViewPoint}.<br>
 * 
 * A viewpoint partitions the set preoccupations of the stakeholders so that issues related to such preoccupation subsets can be addressed
 * separately. Viewpoints provide the convention, rules and modelling technologies for constructing, presenting and analysing Views. It can
 * address one or several existing sources of informations (in which we can find models or metamodels).<br>
 * 
 * Viewpoints also propose dedicated tools for presenting and manipulating data in the particular context of some stakeholderâs
 * preoccupations.
 * 
 * An Openflexo View is the instantiation of a particular Viewpoint with its own Objective relevant to some of the preoccupations of the
 * Viewpoint.
 * 
 * A Viewpoint addresses some preoccupations of the real world. A View is defined for a given objective and for a particular stakeholder or
 * observer.
 * 
 * A Viewpoint provides:
 * <ul>
 * <li>model extensions to model information relevant to a given context;</li>
 * <li>manipulation primitives (EditionSchemes) involving one or many models;</li>
 * <li>tools to create and edit models using model manipulation primitives;</li>
 * <li>tools to import existing models;</li>
 * <li>graphical representation of manipulated models, with dedicated graphical editors (diagrams, tabular and textual views).</li>
 * </ul>
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(ViewPoint.ViewPointImpl.class)
@XMLElement(xmlTag = "ViewPoint")
public interface ViewPoint extends AbstractVirtualModel<ViewPoint> {

	@PropertyIdentifier(type = String.class)
	public static final String VIEW_POINT_URI_KEY = "viewPointURI";
	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String VERSION_KEY = "version";
	@PropertyIdentifier(type = FlexoVersion.class)
	public static final String MODEL_VERSION_KEY = "modelVersion";
	@PropertyIdentifier(type = ViewPointLocalizedDictionary.class)
	public static final String LOCALIZED_DICTIONARY_KEY = "localizedDictionary";
	@PropertyIdentifier(type = VirtualModel.class, cardinality = Cardinality.LIST)
	public static final String VIRTUAL_MODELS_KEY = "virtualModels";

	@Getter(value = VIEW_POINT_URI_KEY)
	@XMLAttribute(xmlTag = "uri")
	public String getViewPointURI();

	@Setter(VIEW_POINT_URI_KEY)
	public void setViewPointURI(String viewPointURI);

	@Override
	@Getter(value = VERSION_KEY, isStringConvertable = true)
	@XMLAttribute
	public FlexoVersion getVersion();

	@Override
	@Setter(VERSION_KEY)
	public void setVersion(FlexoVersion version);

	@Override
	@Getter(value = MODEL_VERSION_KEY, isStringConvertable = true)
	@XMLAttribute
	public FlexoVersion getModelVersion();

	@Override
	@Setter(MODEL_VERSION_KEY)
	public void setModelVersion(FlexoVersion modelVersion);

	@Override
	@Getter(value = LOCALIZED_DICTIONARY_KEY, inverse = ViewPointLocalizedDictionary.OWNER_KEY)
	public ViewPointLocalizedDictionary getLocalizedDictionary();

	@Setter(LOCALIZED_DICTIONARY_KEY)
	public void setLocalizedDictionary(ViewPointLocalizedDictionary localizedDictionary);

	/**
	 * Retrieves the type of a View conform to this ViewPoint
	 */
	public ViewType getViewType();

	/**
	 * Return FlexoConcept matching supplied id represented as a string, which could be either the name of FlexoConcept, or its URI
	 * 
	 * @param flexoConceptId
	 * @return
	 */
	@Override
	public FlexoConcept getFlexoConcept(String flexoConceptId);

	/**
	 * Return all {@link VirtualModel} defined in this {@link ViewPoint}
	 * 
	 * @return
	 */
	@Getter(value = VIRTUAL_MODELS_KEY, cardinality = Cardinality.LIST, inverse = VirtualModel.VIEW_POINT_KEY, ignoreType = true)
	public List<VirtualModel> getVirtualModels();

	@Setter(VIRTUAL_MODELS_KEY)
	public void setVirtualModels(List<VirtualModel> virtualModels);

	@Adder(VIRTUAL_MODELS_KEY)
	public void addToVirtualModels(VirtualModel virtualModel);

	@Remover(VIRTUAL_MODELS_KEY)
	public void removeFromVirtualModels(VirtualModel virtualModel);

	public VirtualModel getVirtualModelNamed(String virtualModelNameOrURI);

	public boolean hasNature(ViewPointNature nature);

	@Override
	public ViewPointBindingModel getBindingModel();

	/**
	 * Default implementation for {@link ViewPoint}
	 * 
	 * @author sylvain
	 * 
	 */
	public static abstract class ViewPointImpl extends AbstractVirtualModelImpl<ViewPoint>implements ViewPoint {

		private static final Logger logger = Logger.getLogger(ViewPoint.class.getPackage().getName());

		private ViewPointLocalizedDictionary localizedDictionary;
		// private ViewPointLibrary _library;
		// private List<ModelSlot> modelSlots;
		private List<VirtualModel> virtualModels;
		private ViewPointResource resource;
		private ViewPointBindingModel bindingModel;
		private final FlexoConceptBindingFactory bindingFactory = new FlexoConceptBindingFactory(this);

		private ViewType viewType = null;

		// TODO: move this code to the ViewPointResource
		public static ViewPoint newViewPoint(String baseName, String viewpointURI, File containerDir, ViewPointLibrary library) {
			ViewPointResource vpRes = ViewPointResourceImpl.makeViewPointResource(baseName, viewpointURI, containerDir,
					library.getServiceManager());
			ViewPointImpl viewpoint = (ViewPointImpl) vpRes.getFactory().newInstance(ViewPoint.class);
			vpRes.setResourceData(viewpoint);
			viewpoint.setResource(vpRes);
			// And register it to the library
			library.registerViewPoint(vpRes);
			viewpoint.init(baseName, library);
			try {
				vpRes.save(null);
			} catch (SaveResourceException e) {
				e.printStackTrace();
			}
			return viewpoint;
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

		// Used during deserialization, do not use it
		public ViewPointImpl() {
			super();
			virtualModels = new ArrayList<VirtualModel>();
		}

		public void init(String baseName, /* File viewpointDir, File xmlFile,*/ViewPointLibrary library/*, ViewPointFolder folder*/) {
			logger.info("Registering viewpoint " + baseName + " URI=" + getViewPointURI());

			setName(baseName);
		}

		@Override
		public final boolean hasNature(ViewPointNature nature) {
			return nature.hasNature(this);
		}

		@Override
		public String getStringRepresentation() {
			return getURI();
		}

		/**
		 * Return the URI of the {@link ViewPoint}<br>
		 * The convention for URI are following: <viewpoint_uri>/<virtual_model_name>#<flexo_concept_name>.<edition_scheme_name> <br>
		 * eg<br>
		 * http://www.mydomain.org/MyViewPoint/MyVirtualModel#MyFlexoConcept.MyEditionScheme
		 * 
		 * @return String representing unique URI of this object
		 */
		@Override
		public String getURI() {
			return getViewPointURI();
		}

		@Override
		public String getViewPointURI() {
			if (getResource() != null) {
				return getResource().getURI();
			}
			return null;
		}

		@Override
		public void setViewPointURI(String vpURI) {
			if (vpURI != null) {
				// We prevent ',' so that we can use it as a delimiter in tags.
				vpURI = vpURI.replace(",", "");
			}
			if (getResource() != null) {
				getResource().setURI(vpURI);
			}
		}

		@Override
		public ViewPointLibrary getViewPointLibrary() {
			if (getResource() != null) {
				return getResource().getViewPointLibrary();
			}
			return null;
		}

		private boolean isLoading = false;

		/**
		 * Load eventually unloaded VirtualModels<br>
		 * After this call return, we can assert that all {@link VirtualModel} are loaded.
		 */
		private void loadVirtualModelsWhenUnloaded() {
			if (isLoading) {
				return;
			}
			if (!isLoading) {
				isLoading = true;
				if (getResource() != null) {
					for (org.openflexo.foundation.resource.FlexoResource<?> r : getResource().getContents()) {
						if (r instanceof VirtualModelResource) {
							VirtualModel vm = ((VirtualModelResource) r).getVirtualModel();
						}
					}
				}
			}

			isLoading = false;
		}

		/**
		 * Return all VirtualModel of a given class.<br>
		 * If onlyFinalInstances is set to true, only instances of supplied class (and not specialized classes) are retrieved
		 * 
		 * @return
		 */
		public <VM extends VirtualModel> List<VM> getVirtualModels(Class<VM> virtualModelClass, boolean onlyFinalInstances) {
			List<VM> returned = new ArrayList<VM>();
			for (VirtualModel vm : getVirtualModels()) {
				if (onlyFinalInstances) {
					if (virtualModelClass.equals(vm.getClass())) {
						returned.add((VM) vm);
					}
				} else {
					if (virtualModelClass.isAssignableFrom(vm.getClass())) {
						returned.add((VM) vm);
					}
				}
			}
			return returned;
		}

		/**
		 * Return all "plain" {@link VirtualModel} defined in this {@link ViewPoint} (does NOT return subclasses of {@link VirtualModel})
		 * 
		 * @return
		 */
		public List<VirtualModel> getPlainVirtualModels() {
			return getVirtualModels(VirtualModel.class, true);
		}

		/**
		 * Return all {@link VirtualModel} defined in this {@link ViewPoint}
		 * 
		 * @return
		 */
		@Override
		public List<VirtualModel> getVirtualModels() {
			loadVirtualModelsWhenUnloaded();
			return virtualModels;
		}

		@Override
		public void setVirtualModels(List<VirtualModel> virtualModels) {
			loadVirtualModelsWhenUnloaded();
			this.virtualModels = virtualModels;
		}

		@Override
		public void addToVirtualModels(VirtualModel virtualModel) {
			virtualModel.setViewPoint(this);
			virtualModels.add(virtualModel);
			getPropertyChangeSupport().firePropertyChange(VIRTUAL_MODELS_KEY, null, virtualModel);
		}

		@Override
		public void removeFromVirtualModels(VirtualModel virtualModel) {
			virtualModel.setViewPoint(null);
			virtualModels.remove(virtualModel);
			getPropertyChangeSupport().firePropertyChange(VIRTUAL_MODELS_KEY, virtualModel, null);
		}

		/**
		 * Return {@link VirtualModel} with supplied name or URI
		 * 
		 * @return
		 */
		@Override
		public VirtualModel getVirtualModelNamed(String virtualModelNameOrURI) {

			for (VirtualModelResource vmRes : getResource().getContents(VirtualModelResource.class)) {
				if (vmRes.getName().equals(virtualModelNameOrURI)) {
					return vmRes.getVirtualModel();
				}
				if (vmRes.getURI().equals(virtualModelNameOrURI)) {
					return vmRes.getVirtualModel();
				}
			}

			/*loadVirtualModelsWhenUnloaded();
			for (VirtualModel vm : getVirtualModels()) {
				if (vm.getName().equals(virtualModelNameOrURI)) {
					return vm;
				}
				if (vm.getURI().equals(virtualModelNameOrURI)) {
					return vm;
				}
			}*/
			return null;
		}

		/**
		 * Return FlexoConcept matching supplied id represented as a string, which could be either the name of FlexoConcept, or its URI
		 * 
		 * @param flexoConceptId
		 * @return
		 */
		@Override
		public FlexoConcept getFlexoConcept(String flexoConceptId) {

			if (StringUtils.isEmpty(flexoConceptId)) {
				return null;
			}

			// Implemented lazy loading for VirtualModel while searching FlexoConcept from URI

			if (flexoConceptId.indexOf("#") > -1) {
				String virtualModelURI = flexoConceptId.substring(0, flexoConceptId.indexOf("#"));
				VirtualModelResource vmRes = getResource().getContentWithURI(VirtualModelResource.class, virtualModelURI);
				if (vmRes != null) {
					return vmRes.getVirtualModel().getFlexoConcept(flexoConceptId);
				}
			}

			return null;
		}

		@Override
		public ViewPointLocalizedDictionary getLocalizedDictionary() {
			return localizedDictionary;
		}

		@Override
		public void setLocalizedDictionary(ViewPointLocalizedDictionary localizedDictionary) {
			if (localizedDictionary != null) {
				localizedDictionary.setOwner(this);
			}
			this.localizedDictionary = localizedDictionary;
		}

		@Deprecated
		public static String findOntologyImports(File aFile) {
			Document document;
			try {
				logger.fine("Try to find URI for " + aFile);
				document = XMLUtils.readXMLFile(aFile);
				Element root = XMLUtils.getElement(document, "Ontology");
				if (root != null) {
					Element importElement = XMLUtils.getElement(document, "imports");
					if (importElement != null) {
						Iterator it = importElement.getAttributes().iterator();
						while (it.hasNext()) {
							Attribute at = (Attribute) it.next();
							if (at.getName().equals("resource")) {
								// System.out.println("Returned " + at.getValue());
								String returned = at.getValue();
								if (StringUtils.isNotEmpty(returned)) {
									return returned;
								}
							}
						}
					}
				}
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.fine("Returned null");
			return null;
		}

		@Override
		public ViewPointBindingModel getBindingModel() {
			if (bindingModel == null) {
				bindingModel = new ViewPointBindingModel(this);
			}
			return bindingModel;
		}

		@Override
		public FlexoConceptBindingFactory getBindingFactory() {
			return bindingFactory;
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			// Voir du cote de GeneratorFormatter pour formatter tout ca
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);

			/*for (FlexoMetaModelResource<?, ?> mm : getAllMetaModels()) {
				out.append("import " + mm.getURI() + ";", context);
				out.append(StringUtils.LINE_SEPARATOR, context);
			}*/

			out.append("ViewDefinition " + getName() + " uri=\"" + getURI() + "\"", context);
			out.append(" {" + StringUtils.LINE_SEPARATOR, context);

			/*for (ModelSlot modelSlot : getModelSlots()) {
				// if (modelSlot.getMetaModelResource() != null) {
				out.append(modelSlot.getFMLRepresentation(context), context);
				// }
			}*/

			out.append(StringUtils.LINE_SEPARATOR, context);
			if (getVirtualModels() != null) {
				for (VirtualModel vm : getVirtualModels()) {
					out.append(vm.getFMLRepresentation(context), context, 1);
					out.append(StringUtils.LINE_SEPARATOR, context, 1);
				}
			}
			out.append("}" + StringUtils.LINE_SEPARATOR, context);
			return out.toString();
		}

		@Override
		public ViewPointResource getResource() {
			return resource;
		}

		@Override
		public void setResource(org.openflexo.foundation.resource.FlexoResource<ViewPoint> resource) {
			this.resource = (ViewPointResource) resource;
		}

		@Override
		public boolean delete(Object... context) {

			logger.info("Deleting ViewPoint " + this);

			// Unregister the viewpoint resource from the viewpoint library
			if (getResource() != null && getViewPointLibrary() != null) {
				getViewPointLibrary().unregisterViewPoint(getResource());
			}

			if (bindingModel != null) {
				bindingModel.delete();
			}

			// Delete viewpoint
			performSuperDelete(context);

			// Delete observers
			deleteObservers();

			return true;
		}

		/**
		 * Retrieves the type of a View conform to this ViewPoint
		 */
		@Override
		public ViewType getViewType() {
			if (viewType == null) {
				viewType = new ViewType(this);
			}
			return viewType;
		}

		@Override
		public FMLTechnologyAdapter getTechnologyAdapter() {
			if (getResource() != null) {
				return getResource().getTechnologyAdapter();
			}
			return null;
		}

		@Override
		public Collection<? extends Validable> getEmbeddedValidableObjects() {
			return getVirtualModels();
		}

	}

	@DefineValidationRule
	public static class ViewPointMustHaveAName extends ValidationRule<ViewPointMustHaveAName, ViewPoint> {
		public ViewPointMustHaveAName() {
			super(ViewPoint.class, "viewpoint_must_have_a_name");
		}

		@Override
		public ValidationIssue<ViewPointMustHaveAName, ViewPoint> applyValidation(ViewPoint vp) {
			if (StringUtils.isEmpty(vp.getName())) {
				return new ValidationError<ViewPointMustHaveAName, ViewPoint>(this, vp, "viewpoint_has_no_name");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class ViewPointURIMustBeValid extends ValidationRule<ViewPointURIMustBeValid, ViewPoint> {
		public ViewPointURIMustBeValid() {
			super(ViewPoint.class, "viewpoint_uri_must_be_valid");
		}

		@Override
		public ValidationIssue<ViewPointURIMustBeValid, ViewPoint> applyValidation(ViewPoint vp) {
			if (StringUtils.isEmpty(vp.getURI())) {
				return new ValidationError<ViewPointURIMustBeValid, ViewPoint>(this, vp, "viewpoint_has_no_uri");
			} else {
				try {
					new URL(vp.getURI());
				} catch (MalformedURLException e) {
					return new ValidationError<ViewPointURIMustBeValid, ViewPoint>(this, vp, "viewpoint_uri_is_not_valid");
				}
			}
			return null;
		}
	}

}
