/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.foundation.fml.rm;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * The resource data of a {@link FIBComponentResource}: a holder around the GINA {@link FIBComponent} the file describes.
 *
 * <p>
 * The holder exists only because {@link FlexoResource} requires its data to be a {@link ResourceData} <i>and</i> a {@link TechnologyObject},
 * neither of which a plain {@link FIBComponent} - a gina type that knows nothing of the resource layer - can be.
 *
 * <p>
 * It is <b>purely a runtime wrapper</b> and is deliberately NOT serialized: unlike <code>gina-ta</code>'s <code>GINAFIBComponent</code>,
 * whose <code>&lt;GINAFIBComponent&gt;</code> element had to be the root of the file, the artefact behind this resource is an
 * <b>ordinary</b> <code>.fib</code> / <code>.inspector</code>, identical to the hundreds the platform already ships. Everything asked of it
 * is delegated to the component, which is what actually holds state.
 *
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(FMLFIBComponent.FMLFIBComponentImpl.class)
public interface FMLFIBComponent extends TechnologyObject<FMLTechnologyAdapter>, ResourceData<FMLFIBComponent> {

	public static final String COMPONENT_KEY = "component";

	/**
	 * The component this resource holds, as loaded by the GINA FIBLibrary.
	 */
	@Getter(value = COMPONENT_KEY, ignoreType = true)
	public FIBComponent getComponent();

	@Setter(COMPONENT_KEY)
	public void setComponent(FIBComponent component);

	/**
	 * Narrowed to the resource that actually holds this component, so callers reach {@link FIBComponentResource#getDrivingConcept()} without
	 * a cast.
	 */
	@Override
	public FIBComponentResource getResource();

	/**
	 * Build a holder around supplied component.
	 *
	 * <p>
	 * The holder is a PAMELA entity (it has to be a FlexoObject) but carries no state of its own and no serialization, so one factory for the
	 * whole platform is enough.
	 */
	public static FMLFIBComponent newInstance(FIBComponent component) {
		FMLFIBComponent returned = HOLDER_FACTORY.newInstance(FMLFIBComponent.class);
		returned.setComponent(component);
		return returned;
	}

	static final PamelaModelFactory HOLDER_FACTORY = makeHolderFactory();

	static PamelaModelFactory makeHolderFactory() {
		try {
			return new PamelaModelFactory(FMLFIBComponent.class);
		} catch (ModelDefinitionException e) {
			throw new IllegalStateException("Cannot build the FMLFIBComponent factory", e);
		}
	}

	public static abstract class FMLFIBComponentImpl extends FlexoObjectImpl implements FMLFIBComponent {

		@Override
		public FMLTechnologyAdapter getTechnologyAdapter() {
			if (getResource() instanceof FIBComponentResource) {
				return ((FIBComponentResource) getResource()).getTechnologyAdapter();
			}
			return null;
		}

		@Override
		public boolean isModified() {
			return getComponent() != null && getComponent().isModified();
		}

		@Override
		public String toString() {
			return getResource() != null ? getResource().getName() : super.toString();
		}
	}
}
