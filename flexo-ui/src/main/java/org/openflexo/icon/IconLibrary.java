/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.icon;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.project.FlexoProjectResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.validation.ConsistencySuccessfullyChecked;
import org.openflexo.pamela.validation.InformationIssue;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationWarning;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

/**
 * Utility class containing all icons used in whole application
 * 
 * @author sylvain
 * 
 */
public class IconLibrary {

	// Panels
	public static final ImageIcon PROGRESS_BACKGROUND = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Panels/ProgressBackground.jpg"));
	public static final ImageIcon WELCOME_BACKGROUND = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Panels/WelcomeBackground.jpg"));
	public static final ImageIcon SPLASH_IMAGE = new ImageIconResource(ResourceLocator.locateResource("Icons/Panels/SplashPanel.jpg"));

	// Flexo icons
	public static final ImageIcon BUSINESS_PLUS_32_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Flexo/BusinessPlus32.png"));
	public static final ImageIcon BUSINESS_32_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Flexo/Business32.png"));
	public static final ImageIcon ENTERPRISE_32_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Flexo/Entreprise32.png"));

	public static final ImageIcon OPENFLEXO_NOTEXT_16 = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Flexo/OpenflexoNoText_16.png"));
	public static final ImageIcon OPENFLEXO_NOTEXT_32 = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Flexo/OpenflexoNoText_32.png"));
	public static final ImageIcon OPENFLEXO_NOTEXT_64 = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Flexo/OpenflexoNoText_64.png"));
	public static final ImageIcon OPENFLEXO_NOTEXT_128 = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Flexo/OpenflexoNoText_128.png"));
	public static final ImageIcon DEV_TEAM_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Flexo/Flexo_DT.jpg"));

	public static final ImageIcon OPENFLEXO_TEXT_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Flexo/OpenflexoText_230x60.png"));
	public static final ImageIcon OPENFLEXO_TEXT_SMALL_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Flexo/OpenflexoText_150x40.png"));

	// Common icons
	public static final ImageIcon HOME_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Common/Home.png"));
	public static final ImageIcon FOLDER_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Common/Folder.gif"));
	public static final ImageIcon SMALL_EXCEL_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Common/SmallExcel.gif"));
	public static final ImageIcon BIG_EXCEL_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Common/BigExcel.png"));
	public static final ImageIcon INFORMATION_SPACE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Common/InformationSpace.png"));
	public static final ImageIcon RESOURCE_CENTER_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Common/ResourceCenter.png"));

	public static final ImageIcon IN_PROGRESS_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Common/Progress.gif"));
	public static final ImageIcon VALID_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Common/Valid.png"));
	public static final ImageIcon INVALID_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Common/Invalid.png"));

	public static final ImageIconResource FIX_PROPOSAL_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Common/Validation/FixProposal.gif"));
	public static final ImageIconResource INFO_ISSUE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Common/Validation/Info.gif"));
	public static final ImageIconResource FIXABLE_ERROR_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Common/Validation/FixableError.gif"));
	public static final ImageIconResource UNFIXABLE_ERROR_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Common/Validation/UnfixableError.gif"));
	public static final ImageIconResource FIXABLE_WARNING_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Common/Validation/FixableWarning.gif"));
	public static final ImageIconResource UNFIXABLE_WARNING_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Common/Validation/UnfixableWarning.gif"));

	public static final ImageIcon MARKET16x16_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Market/market_16x16.png"));
	public static final ImageIcon MARKET32x32_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Market/market_32x32.png"));

	// Actions icons
	public static final ImageIcon UNDO_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Undo.png"));
	public static final ImageIcon REDO_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Redo.png"));
	public static final ImageIcon COPY_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Copy.png"));
	public static final ImageIcon PASTE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Paste.png"));
	public static final ImageIcon CUT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Cut.png"));
	public static final ImageIcon DELETE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Delete.png"));
	public static final ImageIcon HELP_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Help.png"));
	public static final ImageIcon IMPORT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Import.png"));
	public static final ImageIcon EXPORT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Export.png"));
	public static final ImageIcon OPEN_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Open.png"));
	public static final ImageIcon NEW_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/New.png"));
	public static final ImageIcon EDIT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Edit.png"));
	public static final ImageIcon PRINT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Print.png"));
	public static final ImageIcon SAVE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Save.png"));
	public static final ImageIcon SAVE_DISABLED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Actions/Save-disabled.png"));
	public static final ImageIcon SAVE_AS_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Save-as.png"));
	public static final ImageIcon SAVE_ALL_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Save-all.png"));
	public static final ImageIcon NETWORK_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Network.png"));
	public static final ImageIcon INFO_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Info.png"));
	public static final ImageIcon INSPECT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Inspect.png"));
	public static final ImageIcon REFRESH_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Refresh.png"));
	public static final ImageIcon REFRESH_DISABLED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Actions/Refresh-disabled.png"));
	public static final ImageIcon TIME_TRAVEL_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Actions/AutoSaveRestore.gif"));
	public static final ImageIcon CLEAR_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Clear.png"));
	public static final ImageIcon CLOSE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Actions/Close.png"));

	// GUI icons

	public static final ImageIcon DEFAULT_PERSPECTIVE_ACTIVE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/DefaultPerspective_A.png"));
	public static final ImageIcon LIST_PERSPECTIVE_ACTIVE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/ListPerspective_A.png"));

	public static final ImageIcon BROWSER_PLUS_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Browser/BrowserPlus.gif"));
	public static final ImageIcon BROWSER_PLUS_DISABLED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Browser/BrowserPlusDisabled.gif"));
	public static final ImageIcon BROWSER_PLUS_SELECTED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Browser/BrowserPlusSelected.gif"));
	public static final ImageIcon BROWSER_MINUS_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Browser/BrowserMinus.gif"));
	public static final ImageIcon BROWSER_MINUS_DISABLED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Browser/BrowserMinusDisabled.gif"));
	public static final ImageIcon BROWSER_MINUS_SELECTED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Browser/BrowserMinusSelected.gif"));
	public static final ImageIcon BROWSER_OPTIONS_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Browser/BrowserOptions.gif"));
	public static final ImageIcon BROWSER_OPTIONS_DISABLED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Browser/BrowserOptionsDisabled.gif"));
	public static final ImageIcon BROWSER_OPTIONS_SELECTED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Browser/BrowserOptionsSelected.gif"));

	public static final Icon TOGGLE_ARROW_BOTTOM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Controls/toggleArrowBottom.png"));
	public static final Icon TOGGLE_ARROW_BOTTOM_SELECTED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Controls/toggleArrowBottomSelected.png"));
	public static final Icon TOGGLE_ARROW_TOP_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Controls/toggleArrowTop.png"));
	public static final Icon TOGGLE_ARROW_TOP_SELECTED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Controls/toggleArrowTopSelected.png"));
	public static final Icon TOGGLE_ARROW_LEFT_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Controls/toggleArrowLeft.png"));
	public static final Icon TOGGLE_ARROW_LEFT_SELECTED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Controls/toggleArrowLeftSelected.png"));
	public static final Icon TOGGLE_ARROW_RIGHT_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Controls/toggleArrowRight.png"));
	public static final Icon TOGGLE_ARROW_RIGHT_SELECTED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Controls/toggleArrowRightSelected.png"));

	public static final Icon NAVIGATION_BACKWARD_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Navigation/MenuNAV_Fleche_01.png"));
	public static final Icon NAVIGATION_FORWARD_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Navigation/MenuNAV_Fleche_03.png"));
	public static final Icon NAVIGATION_UP_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/GUI/Navigation/MenuNAV_Fleche_02.png"));

	public static final Icon COLLAPSE_ALL_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/GUI/Actions/CollapseAll.png"));

	// Utils
	public static final ImageIconResource SMALL_PLUS_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Utils/Markers/Plus.png"));
	public static final ImageIconResource SMALL_MINUS_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Utils/Markers/Minus.png"));
	public static final ImageIconResource SMALL_DELETE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Utils/Markers/Delete.png"));
	public static final ImageIconResource SMALL_SET_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Utils/Markers/Sync.png"));

	// Markers
	public static final IconMarker POSITIVE_MARKER = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Plus.png")), 8, 8);
	public static final IconMarker NEGATIVE_MARKER = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Minus.png")), 8, 8);
	public static final IconMarker WARNING = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Warning.gif")), 0, 9);
	public static final IconMarker IMPORT = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Import.gif")), 10, 7);
	public static final IconMarker GENERATE = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Generate.png")), 10, 7);
	public static final IconMarker REINJECT = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Reinject.png")), 10, 7);
	public static final IconMarker ERROR = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Error.gif")), 0, 9);
	public static final IconMarker ERROR2 = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Error2.gif")), 0, 9);
	public static final IconMarker QUESTION = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Question.gif")), 12, 9);
	public static final IconMarker MERGE_OK = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/OK.gif")), 12, 1);
	public static final IconMarker DELETE = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Delete.png")), 8, 8);
	public static final IconMarker DUPLICATE = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Plus.png")), 0, 0);
	public static final IconMarker LOCKED = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Locked.png")), 0, 5);
	public static final IconMarker SYNC = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Sync.png")), 10, 7);
	public static final ImageIcon QUESTION_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Question.gif"));
	public static final IconMarker NEW_MARKER = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Plus.png")), 8, 0);
	public static final IconMarker NOTIFY_MARKER = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Notify.png")), 6, 0);

	public static final IconMarker BIG_NEW_MARKER = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/Plus16x16.png")), 45, 0);
	public static final IconMarker BIG_GENERATE_MARKER = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Markers/GenerateBig.gif")), 45, 0);

	// Cursors
	public static final ImageIcon DROP_OK_CURSOR = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Utils/Cursors/DropOKCursor.gif"));
	public static final ImageIcon DROP_KO_CURSOR = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Utils/Cursors/DropKOCursor.gif"));
	public static final ImageIcon HELP_CURSOR = new ImageIconResource(ResourceLocator.locateResource("Icons/Utils/Cursors/HelpCursor.gif"));

	// Palette
	public static final ImageIcon ADD_PALETTE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Actions/Palette/AddPalettePanel.gif"));
	public static final ImageIcon ADD_PALETTE_DISABLED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Actions/Palette/AddPalettePanelDisabled.gif"));
	public static final ImageIcon EDIT_PALETTE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Actions/Palette/EditPalette.gif"));
	public static final ImageIcon EDIT_PALETTE_DISABLED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Actions/Palette/EditPaletteDisabled.gif"));
	public static final ImageIcon SAVE_PALETTE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Actions/Palette/SavePalette.gif"));
	public static final ImageIcon SAVE_PALETTE_DISABLED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Actions/Palette/SavePaletteDisabled.gif"));
	public static final ImageIcon CLOSE_EDITION_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Actions/Palette/CloseEdition.gif"));
	public static final ImageIcon CLOSE_EDITION_DISABLED_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Actions/Palette/CloseEditionDisabled.gif"));

	// Model icons
	// public static final ImageIcon PROJECT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Model/Project.gif"));

	public static ImageIcon getIconForValidationIssue(ValidationIssue<?, ?> issue) {
		if (issue instanceof ValidationWarning) {
			return ((ValidationWarning<?, ?>) issue).isFixable() ? IconLibrary.FIXABLE_WARNING_ICON : IconLibrary.UNFIXABLE_WARNING_ICON;
		}
		else if (issue instanceof ValidationError) {
			return ((ValidationError<?, ?>) issue).isFixable() ? IconLibrary.FIXABLE_ERROR_ICON : IconLibrary.UNFIXABLE_ERROR_ICON;
		}
		else if (issue instanceof ConsistencySuccessfullyChecked) {
			return IconLibrary.VALID_ICON;
		}
		else if (issue instanceof InformationIssue) {
			return IconLibrary.INFO_ISSUE_ICON;
		}
		return null;
	}

	/*public static ImageIcon getIconForResourceType(ResourceType resourceType) {
		if (resourceType == ResourceType.COMPONENT_LIBRARY) {
			return SEIconLibrary.COMPONENT_LIBRARY_ICON;
		} else if (resourceType == ResourceType.DKV_MODEL) {
			return SEIconLibrary.DOMAIN_ICON;
		} else if (resourceType == ResourceType.DATA_MODEL) {
			return DMEIconLibrary.DM_MODEL_ICON;
		} else if (resourceType == ResourceType.GENERATED_CODE) {
			return CGIconLibrary.GENERATED_CODE_ICON;
		} else if (resourceType == ResourceType.GENERATED_DOC) {
			return DGIconLibrary.GENERATED_DOC_ICON;
		} else if (resourceType == ResourceType.GENERATED_SOURCES) {
			return CGIconLibrary.GENERATED_CODE_ICON;
		} else if (resourceType == ResourceType.IMPLEMENTATION_MODEL) {
			return CGIconLibrary.GENERATED_CODE_ICON;
		} else if (resourceType == ResourceType.MONITORING_COMPONENT) {
			return SEIconLibrary.SCREEN_COMPONENT_ICON;
		} else if (resourceType == ResourceType.MONITORING_SCREEN) {
			return SEIconLibrary.SCREEN_COMPONENT_ICON;
		} else if (resourceType == ResourceType.OPERATION_COMPONENT) {
			return SEIconLibrary.SCREEN_COMPONENT_ICON;
		} else if (resourceType == ResourceType.TAB_COMPONENT) {
			return SEIconLibrary.SCREEN_COMPONENT_ICON;
		} else if (resourceType == ResourceType.POPUP_COMPONENT) {
			return SEIconLibrary.SCREEN_COMPONENT_ICON;
		} else if (resourceType == ResourceType.PROCESS) {
			return WKFIconLibrary.PROCESS_ICON;
		} else if (resourceType == ResourceType.RM) {
			return OPENFLEXO_NOTEXT_16;
		} else if (resourceType == ResourceType.SCREENSHOT) {
			return FilesIconLibrary.SMALL_IMAGE_ICON;
		} else if (resourceType == ResourceType.WORKFLOW) {
			return WKFIconLibrary.WORKFLOW_ICON;
		}
	
		return FilesIconLibrary.smallIconForFileFormat(resourceType.getFormat());
	}*/

	/**
	 * Return the technology-specific controller for supplied technology adapter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public static <TA extends TechnologyAdapter<TA>> TechnologyAdapterController<TA> getTechnologyAdapterController(TA technologyAdapter) {
		if (technologyAdapter != null) {
			try {
				FlexoServiceManager sm = technologyAdapter.getTechnologyAdapterService().getServiceManager();
				if (sm != null) {
					TechnologyAdapterControllerService service = sm.getService(TechnologyAdapterControllerService.class);
					if (service != null) {
						return service.getTechnologyAdapterController(technologyAdapter);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static ImageIcon getIconForResource(FlexoResource<?> resource) {
		if (resource instanceof FlexoProjectResource) {
			return OPENFLEXO_NOTEXT_16;
		}
		else if (resource instanceof CompilationUnitResource) {
			return FMLIconLibrary.VIRTUAL_MODEL_ICON;
		}
		return null;
	}

}
