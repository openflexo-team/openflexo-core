package org.openflexo.foundation.view;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.viewpoint.PatternRole;
import org.openflexo.toolbox.StringUtils;

/**
 * Represents run-time-level object encoding reference to object
 * 
 * @author sylvain
 * 
 * @param <T>
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(ActorReference.ActorReferenceImpl.class)
public abstract interface ActorReference<T> extends VirtualModelInstanceObject{

@PropertyIdentifier(type=String.class)
public static final String PATTERN_ROLE_NAME_KEY = "patternRoleName";

@Getter(value=PATTERN_ROLE_NAME_KEY)
@XMLAttribute
public String getPatternRoleName();

@Setter(PATTERN_ROLE_NAME_KEY)
public void setPatternRoleName(String patternRoleName);


public static abstract  abstract class ActorReference<T>Impl extends VirtualModelInstanceObjectImpl implements ActorReference<T>
{
	private PatternRole<T> patternRole;
	private String patternRoleName;
	private ModelSlot modelSlot;
	private EditionPatternInstance epi;

	protected ActorReference(FlexoProject project) {
		super(project);
	}

	public ModelSlot getModelSlot() {
		return modelSlot;
	}

	public void setModelSlot(ModelSlot modelSlot) {
		this.modelSlot = modelSlot;
	}

	@Override
	public VirtualModelInstance getResourceData() {
		if (getEditionPatternInstance() != null) {
			return getEditionPatternInstance().getResourceData();
		}
		return null;
	}

	public abstract T retrieveObject();

	public EditionPatternInstance getEditionPatternInstance() {
		return epi;
	}

	public void setEditionPatternInstance(EditionPatternInstance epi) {
		this.epi = epi;
	}

	public PatternRole<T> getPatternRole() {
		if (patternRole == null && epi != null && StringUtils.isNotEmpty(patternRoleName)) {
			patternRole = (PatternRole<T>) epi.getFlexoConcept().getPatternRole(patternRoleName);
		}
		return patternRole;
	}

	public void setPatternRole(PatternRole<T> patternRole) {
		this.patternRole = patternRole;
	}

	public String getPatternRoleName() {
		if (patternRole != null) {
			return patternRole.getPatternRoleName();
		}
		return patternRoleName;
	}

	public void setPatternRoleName(String patternRoleName) {
		this.patternRoleName = patternRoleName;
	}

	@Override
	public VirtualModelInstance getVirtualModelInstance() {
		if (getEditionPatternInstance() != null) {
			return getEditionPatternInstance().getVirtualModelInstance();
		}
		return null;
	}

	public ModelSlotInstance<?, ?> getModelSlotInstance() {
		if (getVirtualModelInstance() != null) {
			return getVirtualModelInstance().getModelSlotInstance(getPatternRole().getModelSlot());
		}
		return null;
	}

}}
