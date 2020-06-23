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

package org.openflexo.prefs;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.openflexo.Flexo;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.converter.AWTRectangleConverter;
import org.openflexo.swing.FlexoFont;
import org.openflexo.swing.LookAndFeel;
import org.openflexo.swing.converter.LookAndFeelConverter;
import org.openflexo.toolbox.ToolBox;

/**
 * Encodes preferences relative to presentation for the whole application
 * 
 * @author sguerin
 * 
 */
@ModelEntity
@ImplementationClass(PresentationPreferences.PresentationPreferencesImpl.class)
@XMLElement(xmlTag = "PresentationPreferences")
@Preferences(
		shortName = "Presentation",
		longName = "Presentation preferences",
		FIBPanel = "Fib/Prefs/PresentationPreferences.fib",
		smallIcon = "Icons/Common/PresentationPreferences.png",
		bigIcon = "Icons/Common/PresentationPreferences_64x64.png")
public interface PresentationPreferences extends PreferencesContainer {

	public static final String SYNCHRONIZED_BROWSER = "synchronizedBrowser";
	public static final String INSPECTOR_VISIBLE = "inspectorVisible";
	public static final String INSPECTOR_ON_TOP = "inspectorAlwaysOnTop";
	public static final String HIDE_EMPTY_FOLDERS = "hideEmptyFolders";
	public static final String CLOSE_POPUP_ON_CLICK_OUT = "closePopupOnClickOut";
	public static final String BOUNDS_FOR_FRAME = "BoundsForFrame_";
	public static final String SHOW_LEFT_VIEW = "showBrowserIn";
	public static final String SHOW_RIGHT_VIEW = "showPaletteIn";
	public static final String STATE_FOR_FRAME = "StateForFrame_";
	public static final String LAYOUT_FOR = "LayoutFor_";
	public static final String SPLIT_DIVIDER_LOCATION = "SPLIT_DIVIDER_LOCATION_";
	public static final String BROWSERFONT_KEY = "browser_font";
	public static final String CLOSE_POPUP_ON_MOUSE_OUT = "close_popup_on_mouse_out";
	public static final String LOOK_AND_FEEL = "look_and_feel";
	public static final String HIDE_FILTERED_OBJECTS = "hideFilteredObjects";

	@Getter(value = SYNCHRONIZED_BROWSER, defaultValue = "true")
	@XMLAttribute
	public boolean getSynchronizedBrowser();

	@Setter(SYNCHRONIZED_BROWSER)
	public void setSynchronizedBrowser(boolean synchronizedBrowser);

	@Getter(value = INSPECTOR_VISIBLE, defaultValue = "false")
	@XMLAttribute
	public boolean getInspectorVisible();

	@Setter(INSPECTOR_VISIBLE)
	public void setInspectorVisible(boolean inspectorVisible);

	@Getter(value = INSPECTOR_ON_TOP, defaultValue = "true")
	@XMLAttribute
	public boolean getInspectorAlwaysOnTop();

	@Setter(INSPECTOR_ON_TOP)
	public void setInspectorAlwaysOnTop(boolean inspectorAlwaysOnTop);

	@Getter(value = HIDE_EMPTY_FOLDERS, defaultValue = "true")
	@XMLAttribute
	public boolean hideEmptyFolders();

	@Setter(HIDE_EMPTY_FOLDERS)
	public void setHideEmptyFolders(boolean hideEmptyFolders);

	public Rectangle getBoundForFrameWithID(String id);

	public void setBoundForFrameWithID(String id, Rectangle bounds);

	public boolean getShowLeftView(String id);

	public void setShowLeftView(String id, boolean status);

	public boolean getShowRightView(String id);

	public void setShowRightView(String id, boolean status);

	public Integer getFrameStateForFrameWithID(String id);

	/**
	 * @param extendedState
	 */
	public void setFrameStateForFrameWithID(String id, Integer extendedState);

	public String getLayoutFor(String id);

	/**
	 * @param extendedState
	 */
	public void setLayoutFor(String layout, String id);

	public int getDividerLocationForSplitPaneWithID(String id);

	public void setDividerLocationForSplitPaneWithID(int value, String id);

	@Getter(value = BROWSERFONT_KEY, defaultValue = "Sans Serif,0,11", isStringConvertable = true)
	@XMLAttribute
	public FlexoFont getBrowserFont();

	@Setter(BROWSERFONT_KEY)
	public void setBrowserFont(FlexoFont font);

	@Getter(value = CLOSE_POPUP_ON_MOUSE_OUT, defaultValue = "false")
	@XMLAttribute
	public boolean getCloseOnMouseOut();

	@Setter(CLOSE_POPUP_ON_MOUSE_OUT)
	public void setCloseOnMouseOut(boolean closeOnMouseOut);

	@Getter(value = LOOK_AND_FEEL, isStringConvertable = true)
	@XMLAttribute
	public LookAndFeel getLookAndFeel();

	@Setter(LOOK_AND_FEEL)
	public void setLookAndFeel(LookAndFeel value);

	public String getLookAndFeelAsString();

	public List<LookAndFeelInfo> getAvailableLookAndFeels();

	public LookAndFeelInfo getLookAndFeelInfo();

	public void setLookAndFeelInfo(LookAndFeelInfo lafInfo);

	@Getter(value = HIDE_FILTERED_OBJECTS, defaultValue = "true")
	@XMLAttribute
	public boolean hideFilteredObjects();

	@Setter(HIDE_FILTERED_OBJECTS)
	public void setHideFilteredObjects(boolean enabled);

	public abstract class PresentationPreferencesImpl extends PreferencesContainerImpl implements PresentationPreferences {

		private static final Logger logger = Logger.getLogger(PresentationPreferences.class.getPackage().getName());

		private final AWTRectangleConverter RECTANGLE_CONVERTER = new AWTRectangleConverter();
		private final LookAndFeelConverter LAF_CONVERTER = new LookAndFeelConverter();

		@Override
		public String toString() {
			return "PresentationPreferences: " + super.toString();
		}

		@Override
		public Rectangle getBoundForFrameWithID(String id) {
			return RECTANGLE_CONVERTER.convertFromString(assertProperty(BOUNDS_FOR_FRAME + id).getValue(), null);
		}

		@Override
		public void setBoundForFrameWithID(String id, Rectangle bounds) {
			assertProperty(BOUNDS_FOR_FRAME + id).setValue(RECTANGLE_CONVERTER.convertToString(bounds));
		}

		@Override
		public boolean getShowLeftView(String id) {
			return assertProperty(SHOW_LEFT_VIEW + id).booleanValue(true);
		}

		@Override
		public void setShowLeftView(String id, boolean status) {
			assertProperty(SHOW_LEFT_VIEW + id).setBooleanValue(status);
		}

		@Override
		public boolean getShowRightView(String id) {
			return assertProperty(SHOW_RIGHT_VIEW + id).booleanValue(true);
		}

		@Override
		public void setShowRightView(String id, boolean status) {
			assertProperty(SHOW_RIGHT_VIEW + id).setBooleanValue(status);
		}

		@Override
		public Integer getFrameStateForFrameWithID(String id) {
			return assertProperty(SHOW_RIGHT_VIEW + id).getIntegerValue();
		}

		/**
		 * @param extendedState
		 */
		@Override
		public void setFrameStateForFrameWithID(String id, Integer extendedState) {
			assertProperty(STATE_FOR_FRAME + id).setIntegerValue(extendedState);
		}

		@Override
		public int getDividerLocationForSplitPaneWithID(String id) {
			return assertProperty(SPLIT_DIVIDER_LOCATION + id).integerValue();
		}

		@Override
		public void setDividerLocationForSplitPaneWithID(int value, String id) {
			assertProperty(SPLIT_DIVIDER_LOCATION + id).setIntegerValue(value);
		}

		@Override
		public String getLayoutFor(String id) {
			return assertProperty(LAYOUT_FOR + id).getValue();
		}

		/**
		 * @param extendedState
		 */
		@Override
		public void setLayoutFor(String layout, String id) {
			assertProperty(LAYOUT_FOR + id).setValue(layout);
		}

		@Override
		public LookAndFeel getLookAndFeel() {
			PreferenceProperty p = assertProperty(ToolBox.getPLATFORM() + LOOK_AND_FEEL);
			String returned = p.getValue();
			if (returned == null) {
				p = assertProperty(ToolBox.getPLATFORM() + LOOK_AND_FEEL);
				returned = p.getValue();
			}
			if (returned == null) {
				returned = UIManager.getSystemLookAndFeelClassName();
			}
			// setLookAndFeel(LAF_CONVERTER.convertFromString(returned, null));
			return LAF_CONVERTER.convertFromString(returned, null);
		}

		@Override
		public String getLookAndFeelAsString() {
			return LAF_CONVERTER.convertToString(getLookAndFeel());
		}

		@Override
		public void setLookAndFeel(LookAndFeel value) {
			if (value == null) {
				value = LookAndFeel.getDefaultLookAndFeel();
			}
			if (value != null && !value.getClassName().equals(getLookAndFeel().getClassName())) {
				assertProperty(ToolBox.getPLATFORM() + LOOK_AND_FEEL).setValue(LAF_CONVERTER.convertToString(value));
				Flexo.initUILAF(LAF_CONVERTER.convertToString(value));
			}
		}

		@Override
		public List<LookAndFeelInfo> getAvailableLookAndFeels() {
			UIManager.LookAndFeelInfo[] lafInfo = UIManager.getInstalledLookAndFeels();
			return Arrays.asList(lafInfo);
		}

		@Override
		public LookAndFeelInfo getLookAndFeelInfo() {
			if (getLookAndFeel() == null) {
				return null;
			}
			for (LookAndFeelInfo lafInfo : getAvailableLookAndFeels()) {
				if (lafInfo.getClassName().equals(getLookAndFeel().getClassName())) {
					return lafInfo;
				}
			}
			return null;
		}

		@Override
		public void setLookAndFeelInfo(LookAndFeelInfo lafInfo) {
			LookAndFeel laf = LAF_CONVERTER.convertFromString(lafInfo.getClassName(), null);
			setLookAndFeel(laf);
		}
	}

}
