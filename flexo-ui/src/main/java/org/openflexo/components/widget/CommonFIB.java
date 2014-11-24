package org.openflexo.components.widget;

import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * This class stores all references
 * 
 * @author sylvain
 * 
 */
public class CommonFIB {

	// Saving operations
	public static Resource REVIEW_UNSAVED_DIALOG_FIB = ResourceLocator.locateResource("Fib/Dialog/ReviewUnsavedDialog.fib");

	// General components
	public static Resource VIEWPOINT_VIEW_FIB = ResourceLocator.locateResource("Fib/VPM/ViewPointView.fib");
	public static Resource STANDARD_FLEXO_CONCEPT_VIEW_FIB = ResourceLocator.locateResource("Fib/VPM/StandardFlexoConceptView.fib");
	public static Resource VIRTUAL_MODEL_VIEW_FIB = ResourceLocator.locateResource("Fib/VPM/VirtualModelView.fib");

	@Deprecated
	public static Resource CHOOSE_AND_CONFIGURE_CREATION_SCHEME_DIALOG_FIB = ResourceLocator
			.locateResource("Fib/Dialog/ChooseAndConfigureCreationSchemeDialog.fib");

	// General
	public static Resource ONTOLOGY_VIEW_FIB = ResourceLocator.locateResource("Fib/OntologyView.fib");
	public static Resource VIRTUAL_MODEL_INSTANCE_VIEW_FIB = ResourceLocator.locateResource("Fib/VirtualModelInstanceView.fib");

	// View/VirtualModelInstance edition
	// Should be removed
	@Deprecated
	public static Resource CREATE_VIEW_DIALOG_FIB = ResourceLocator.locateResource("Fib/Dialog/CreateViewDialog.fib");
	@Deprecated
	public static Resource CREATE_VIRTUAL_MODEL_INSTANCE_DIALOG_FIB = ResourceLocator
			.locateResource("Fib/Dialog/CreateVirtualModelInstanceDialog.fib");
	@Deprecated
	public static Resource CONFIGURE_FREE_MODEL_SLOT_INSTANCE_DIALOG_FIB = ResourceLocator
			.locateResource("Fib/Dialog/ConfigureFreeModelSlotInstanceDialog.fib");
	@Deprecated
	public static Resource CONFIGURE_TYPE_AWARE_MODEL_SLOT_INSTANCE_DIALOG_FIB = ResourceLocator
			.locateResource("Fib/Dialog/ConfigureTypeAwareModelSlotInstanceDialog.fib");
	@Deprecated
	public static Resource CONFIGURE_VIRTUAL_MODEL_SLOT_INSTANCE_DIALOG_FIB = ResourceLocator
			.locateResource("Fib/Dialog/ConfigureVirtualModelSlotInstanceDialog.fib");

}
