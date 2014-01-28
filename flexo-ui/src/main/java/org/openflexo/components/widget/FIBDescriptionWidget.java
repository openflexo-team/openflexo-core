/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.components.widget;

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.fib.controller.FIBController;
import org.openflexo.fib.model.FIBComponent;
import org.openflexo.fib.view.widget.DefaultFIBCustomComponent;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.toolbox.FileResource;

/**
 * Widget allowing to represent/edit specific descriptions related to a {@link FlexoObject} instance
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBDescriptionWidget extends DefaultFIBCustomComponent<FlexoObject> {

	static final Logger logger = Logger.getLogger(FIBDescriptionWidget.class.getPackage().getName());

	public static final FileResource FIB_FILE = new FileResource("Fib/DescriptionWidget.fib");

	public FIBDescriptionWidget(FlexoObject editedObject) {
		super(FIB_FILE, editedObject, FlexoLocalization.getMainLocalizer());
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
			super(c);
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
