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

package org.openflexo.components.widget;

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.gina.ApplicationFIBLibrary;
import org.openflexo.gina.controller.FIBController;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.utils.FIBJPanel;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;

/**
 * Widget allowing to represent/edit specific descriptions related to a {@link FlexoObject} instance
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBDescriptionWidget extends FIBJPanel<FlexoObject> {

	static final Logger logger = Logger.getLogger(FIBDescriptionWidget.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/DescriptionWidget.fib");

	public FIBDescriptionWidget(FlexoObject editedObject) {
		super(FIB_FILE, editedObject, ApplicationFIBLibrary.instance(), FlexoLocalization.getMainLocalizer());
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
	}

	@Override
	public Class<FlexoObject> getRepresentedType() {
		return FlexoObject.class;
	}

	@Override
	protected FIBController makeFIBController(FIBComponent fibComponent, LocalizedDelegate parentLocalizer) {
		// logger.info("************** Make DescriptionWidgetFIBController");
		return new DescriptionWidgetFIBController(fibComponent);
	}

	public class DescriptionWidgetFIBController extends FIBController {

		private String specificDescriptionKey;

		public DescriptionWidgetFIBController(FIBComponent c) {
			super(c, SwingViewFactory.INSTANCE);
		}

		public String getSpecificDescription() {
			if (specificDescriptionKey != null && getEditedObject() != null) {
				return getEditedObject().getSpecificDescriptionForKey(specificDescriptionKey);
			}
			return null;
		}

		public void setSpecificDescription(String specificDescription) {
			System.out.println("Sets " + specificDescription + " specificDescriptionKey=" + specificDescriptionKey);
			if (specificDescriptionKey != null && getEditedObject() != null) {
				System.out.println("For key " + specificDescriptionKey + " description=" + specificDescription);
				getEditedObject().setSpecificDescriptionsForKey(specificDescription, specificDescriptionKey);
			}
		}

		public String getSpecificDescriptionKey() {
			return specificDescriptionKey;
		}

		public void setSpecificDescriptionKey(String key) {
			System.out.println("setSpecificDescriptionKey " + key);
			this.specificDescriptionKey = key;
		}

		public List<String> getSpecificDescriptionKeys() {
			return getEditedObject().getSpecificDescriptionKeys();
		}
	}
}
