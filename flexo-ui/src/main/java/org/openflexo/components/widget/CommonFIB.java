package org.openflexo.components.widget;

import java.io.File;

import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocatorDelegate;

/**
 * This class stores all references
 * 
 * @author sylvain
 * 
 */
public class CommonFIB {
	


	// Saving operations
	public static Resource REVIEW_UNSAVED_DIALOG_FIB = ResourceLocator.locateResource("Fib/Dialog/ReviewUnsavedDialog.fib");
	public static Resource DECLARE_SHAPE_IN_FLEXO_CONCEPT_DIALOG_FIB = ResourceLocator.locateResource("Fib/Widget/DeclareShapeInFlexoConceptDialog.fib");
	public static Resource DECLARE_CONNECTOR_IN_FLEXO_CONCEPT_DIALOG_FIB = 
			ResourceLocator.locateResource("Fib/Widget/DeclareConnectorInFlexoConceptDialog.fib");
	public static Resource PUSH_TO_PALETTE_DIALOG_FIB = ResourceLocator.locateResource("Fib/Dialog/PushToPaletteDialog.fib");

	// General components
	public static Resource VIEWPOINT_VIEW_FIB = ResourceLocator.locateResource("Fib/VPM/ViewPointView.fib");
	public static Resource STANDARD_FLEXO_CONCEPT_VIEW_FIB = ResourceLocator.locateResource("Fib/VPM/StandardFlexoConceptView.fib");
	public static Resource VIRTUAL_MODEL_VIEW_FIB = ResourceLocator.locateResource("Fib/VPM/VirtualModelView.fib");

	public static Resource CHOOSE_AND_CONFIGURE_CREATION_SCHEME_DIALOG_FIB = 
			ResourceLocator.locateResource("Fib/Dialog/ChooseAndConfigureCreationSchemeDialog.fib");

	// General
	public static Resource ONTOLOGY_VIEW_FIB = ResourceLocator.locateResource("Fib/OntologyView.fib");
	public static Resource VIRTUAL_MODEL_INSTANCE_VIEW_FIB = ResourceLocator.locateResource("Fib/VirtualModelInstanceView.fib");

	// View/VirtualModelInstance edition
	public static Resource CREATE_VIEW_DIALOG_FIB = ResourceLocator.locateResource("Fib/Dialog/CreateViewDialog.fib");
	public static Resource CREATE_VIRTUAL_MODEL_INSTANCE_DIALOG_FIB = ResourceLocator.locateResource("Fib/Dialog/CreateVirtualModelInstanceDialog.fib");
	public static Resource CONFIGURE_FREE_MODEL_SLOT_INSTANCE_DIALOG_FIB = 
			ResourceLocator.locateResource("Fib/Dialog/ConfigureFreeModelSlotInstanceDialog.fib");
	public static Resource CONFIGURE_TYPE_AWARE_MODEL_SLOT_INSTANCE_DIALOG_FIB = 
			ResourceLocator.locateResource("Fib/Dialog/ConfigureTypeAwareModelSlotInstanceDialog.fib");
	public static Resource CONFIGURE_VIRTUAL_MODEL_SLOT_INSTANCE_DIALOG_FIB = 
			ResourceLocator.locateResource("Fib/Dialog/ConfigureVirtualModelSlotInstanceDialog.fib");

}
