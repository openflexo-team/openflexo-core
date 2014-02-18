package org.openflexo.foundation.viewpoint;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.foundation.view.EditionPatternInstance;
import org.openflexo.foundation.view.ModelObjectActorReference;
import org.openflexo.foundation.view.VirtualModelInstanceModelFactory;
import org.openflexo.foundation.viewpoint.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@ImplementationClass(EditionPatternInstancePatternRole.EditionPatternInstancePatternRoleImpl.class)
@XMLElement
public interface EditionPatternInstancePatternRole extends PatternRole<EditionPatternInstance> {

	@PropertyIdentifier(type = String.class)
	public static final String EDITION_PATTERN_TYPE_URI_KEY = "editionPatternTypeURI";
	@PropertyIdentifier(type = String.class)
	public static final String CREATION_SCHEME_URI_KEY = "creationSchemeURI";

	@Getter(value = EDITION_PATTERN_TYPE_URI_KEY)
	@XMLAttribute
	public String _getEditionPatternTypeURI();

	@Setter(EDITION_PATTERN_TYPE_URI_KEY)
	public void _setEditionPatternTypeURI(String editionPatternTypeURI);

	@Getter(value = CREATION_SCHEME_URI_KEY)
	@XMLAttribute
	public String _getCreationSchemeURI();

	@Setter(CREATION_SCHEME_URI_KEY)
	public void _setCreationSchemeURI(String creationSchemeURI);

	public CreationScheme getCreationScheme();

	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	public static abstract class EditionPatternInstancePatternRoleImpl extends PatternRoleImpl<EditionPatternInstance> implements
			EditionPatternInstancePatternRole {

		private static final Logger logger = Logger.getLogger(EditionPatternInstancePatternRole.class.getPackage().getName());

		private FlexoConcept flexoConceptType;
		private CreationScheme creationScheme;
		private String _creationSchemeURI;
		private String _editionPatternTypeURI;

		public EditionPatternInstancePatternRoleImpl() {
			super();
			// logger.severe("############# Created EditionPatternInstancePatternRole " + Integer.toHexString(hashCode()) +
			// " model version="
			// + builder.getModelVersion() + " file=" + builder.resource.getFile().getAbsolutePath());
		}

		/*@Override
		public void finalizeDeserialization(Object builder) {
			super.finalizeDeserialization(builder);
			logger.severe("############# Finalized EditionPatternInstancePatternRole " + Integer.toHexString(hashCode()) + toString());
		}*/

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("PatternRole " + getName() + " as EditionPatternInstance conformTo " + getPreciseType() + ";", context);
			return out.toString();
		}

		@Override
		public Type getType() {
			return this.getViewPoint().getInstanceType(getFlexoConceptType());
		}

		@Override
		public String getPreciseType() {
			if (getFlexoConceptType() != null) {
				return getFlexoConceptType().getName();
			}
			return "FlexoConcept";
		}

		/*@Override
		public boolean getIsPrimaryRole() {
			return false;
		}

		@Override
		public void setIsPrimaryRole(boolean isPrimary) {
			// Not relevant
		}*/

		@Override
		public FlexoConcept getFlexoConceptType() {
			if (getCreationScheme() != null) {
				return getCreationScheme().getFlexoConcept();
			}
			if (flexoConceptType == null && _editionPatternTypeURI != null && getViewPoint() != null) {
				flexoConceptType = getViewPoint().getFlexoConcept(_editionPatternTypeURI);
			}
			return flexoConceptType;
		}

		@Override
		public void setFlexoConceptType(FlexoConcept flexoConceptType) {
			if (flexoConceptType != this.flexoConceptType) {
				this.flexoConceptType = flexoConceptType;
				if (getCreationScheme() != null && getCreationScheme().getFlexoConcept() != flexoConceptType) {
					setCreationScheme(null);
				}
				if (getFlexoConcept() != null) {
					for (EditionScheme s : getFlexoConcept().getEditionSchemes()) {
						s.updateBindingModels();
					}
				}
			}
		}

		@Override
		public void finalizePatternRoleDeserialization() {
			super.finalizePatternRoleDeserialization();
			if (flexoConceptType == null && _editionPatternTypeURI != null && getViewPoint() != null) {
				flexoConceptType = getViewPoint().getFlexoConcept(_editionPatternTypeURI);
			}
		}

		@Override
		public String _getCreationSchemeURI() {
			if (getCreationScheme() != null) {
				return getCreationScheme().getURI();
			}
			return _creationSchemeURI;
		}

		@Override
		public void _setCreationSchemeURI(String uri) {
			if (getViewPointLibrary() != null) {
				creationScheme = (CreationScheme) getViewPointLibrary().getEditionScheme(uri);
				for (EditionScheme s : getFlexoConcept().getEditionSchemes()) {
					s.updateBindingModels();
				}
			}
			_creationSchemeURI = uri;
		}

		@Override
		public String _getEditionPatternTypeURI() {
			if (getFlexoConceptType() != null) {
				return getFlexoConceptType().getURI();
			}
			return _editionPatternTypeURI;
		}

		@Override
		public void _setEditionPatternTypeURI(String uri) {
			if (getViewPoint() != null) {
				flexoConceptType = getViewPoint().getFlexoConcept(uri);
			}
			_editionPatternTypeURI = uri;
		}

		@Override
		public CreationScheme getCreationScheme() {
			if (creationScheme == null && _creationSchemeURI != null && getViewPointLibrary() != null) {
				creationScheme = (CreationScheme) getViewPointLibrary().getEditionScheme(_creationSchemeURI);
			}
			return creationScheme;
		}

		public void setCreationScheme(CreationScheme creationScheme) {
			this.creationScheme = creationScheme;
			if (creationScheme != null) {
				_creationSchemeURI = creationScheme.getURI();
			}
		}

		@Override
		public boolean defaultBehaviourIsToBeDeleted() {
			return false;
		}

		@Override
		public ModelObjectActorReference<EditionPatternInstance> makeActorReference(EditionPatternInstance object,
				EditionPatternInstance epi) {
			VirtualModelInstanceModelFactory factory = epi.getFactory();
			ModelObjectActorReference<EditionPatternInstance> returned = factory.newInstance(ModelObjectActorReference.class);
			returned.setPatternRole(this);
			returned.setEditionPatternInstance(epi);
			returned.setModellingElement(object);
			return returned;
		}

		@Override
		public VirtualModelModelSlot getModelSlot() {
			VirtualModelModelSlot returned = (VirtualModelModelSlot) super.getModelSlot();
			if (returned == null) {
				if (getVirtualModel() != null && getVirtualModel().getModelSlots(VirtualModelModelSlot.class).size() > 0) {
					return getVirtualModel().getModelSlots(VirtualModelModelSlot.class).get(0);
				}
			}
			return returned;
		}

		public VirtualModelModelSlot getVirtualModelModelSlot() {
			return getModelSlot();
		}

		public void setVirtualModelModelSlot(VirtualModelModelSlot modelSlot) {
			setModelSlot(modelSlot);
		}

	}
}
