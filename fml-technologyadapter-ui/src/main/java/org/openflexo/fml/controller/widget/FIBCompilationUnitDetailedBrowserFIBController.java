/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.fml.controller.FMLFIBController;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.view.controller.FlexoController;

/**
 * Represents a controller with basic FML edition facilities<br>
 * Extends FlexoFIBController by supporting features relative to FML edition
 * 
 * @author sylvain
 */
public class FIBCompilationUnitDetailedBrowserFIBController extends FMLFIBController {

	protected static final Logger logger = FlexoLogger
			.getLogger(FIBCompilationUnitDetailedBrowserFIBController.class.getPackage().getName());

	private FIBCompilationUnitDetailedBrowser browser;

	public FIBCompilationUnitDetailedBrowserFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
		super(component, viewFactory);

	}

	public FIBCompilationUnitDetailedBrowserFIBController(FIBComponent component, GinaViewFactory<?> viewFactory,
			FlexoController controller) {
		super(component, viewFactory, controller);
	}

	public FIBCompilationUnitDetailedBrowser getBrowser() {
		return browser;
	}

	public void setBrowser(FIBCompilationUnitDetailedBrowser browser) {
		this.browser = browser;
	}

	@Override
	public void singleClick(Object object) {
		super.singleClick(object);
		if (getFlexoController() == null) {
			browser.getFMLEditor().clearHighlights();
			if (object instanceof FMLPrettyPrintable) {
				browser.getFMLEditor().highlightObject((FMLPrettyPrintable) object);
			}
		}
	}

	public List<FMLPrettyPrintable> getContents(FMLObject container) {
		List<FMLPrettyPrintable> returned = new ArrayList<>();
		if (container instanceof FlexoConcept) {
			FlexoConcept concept = (FlexoConcept) container;
			if (container instanceof VirtualModel) {
				returned.addAll(((VirtualModel) concept).getFlexoConcepts());
			}
			else {
				returned.addAll(concept.getEmbeddedFlexoConcepts());
			}
			returned.addAll(concept.getDeclaredProperties());
			returned.addAll(concept.getDeclaredFlexoBehaviours());
		}
		Collections.sort(returned, new Comparator<FMLPrettyPrintable>() {
			@Override
			public int compare(FMLPrettyPrintable o1, FMLPrettyPrintable o2) {
				if (o1.getPrettyPrintDelegate() == null || o2.getPrettyPrintDelegate() == null) {
					return 0;
				}
				if (o1.getPrettyPrintDelegate().getStartLocation() == null || o2.getPrettyPrintDelegate().getStartLocation() == null) {
					return 0;
				}
				return o1.getPrettyPrintDelegate().getStartLocation().compareTo(o2.getPrettyPrintDelegate().getStartLocation());
			}
		});
		return returned;
	}

}
