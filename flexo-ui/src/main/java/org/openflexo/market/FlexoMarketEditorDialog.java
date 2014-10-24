package org.openflexo.market;

import java.awt.Window;
import java.util.logging.Logger;

import org.openflexo.fib.FIBLibrary;
import org.openflexo.fib.controller.FIBDialog;
import org.openflexo.foundation.remoteresources.FlexoUpdateService;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;


public class FlexoMarketEditorDialog extends FIBDialog<FlexoMarketEditor>{

	
	static final Logger logger = Logger.getLogger(FlexoMarketEditorDialog.class.getPackage().getName());

	public static final Resource FLEXO_MARKET_EDITOR_FIB = ResourceLocator.locateResource("Fib/FlexoMarketEditor.fib");

	private static FlexoMarketEditor flexoMarketEditor = null;

	private static FlexoMarketEditorDialog dialog;

	public static FlexoMarketEditorDialog getFlexoMarketEditorDialog(FlexoUpdateService service, Window parent) {
		System.out.println("showFlexoMarketEditorDialog with " + service);

		if (dialog == null) {
			dialog = new FlexoMarketEditorDialog(service, parent);
		}

		return dialog;
	}

	public static void showFlexoMarketEditorDialog(FlexoUpdateService service, Window parent) {
		System.out.println("showFlexoMarketEditor with " + service);

		if (dialog == null) {
			dialog = getFlexoMarketEditorDialog(service, parent);
		}
		dialog.showDialog();
	}

	public static FlexoMarketEditor getFlexoMarketEditor(FlexoUpdateService service) {
		if (flexoMarketEditor == null) {
			flexoMarketEditor = new FlexoMarketEditor(service);
		}
		return flexoMarketEditor;
	}

	public FlexoMarketEditorDialog(FlexoUpdateService service, Window parent) {

		super(FIBLibrary.instance().retrieveFIBComponent(FLEXO_MARKET_EDITOR_FIB,true), getFlexoMarketEditor(service), parent,
				true, FlexoLocalization.getMainLocalizer());
		getData().setOwner(this);
		setTitle("Flexo Market Editor");
	}

}
	
