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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.openflexo.fml.controller.FMLFIBController;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.view.widget.JFIBBrowserWidget;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.view.controller.FlexoController;

/**
 * Represents a controller with basic FML edition facilities<br>
 * Extends FlexoFIBController by supporting features relative to FML edition
 * 
 * @author sylvain
 */
public class FIBCompilationUnitDetailedBrowserFIBController extends FMLFIBController implements PropertyChangeListener {

	protected static final Logger logger = FlexoLogger
			.getLogger(FIBCompilationUnitDetailedBrowserFIBController.class.getPackage().getName());

	private FIBCompilationUnitDetailedBrowser browser;

	private List<FlexoConcept> listenedConcepts = new ArrayList<>();

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

	@Override
	public void setDataObject(Object anObject) {
		if (anObject instanceof FMLCompilationUnit) {
			listenTo(((FMLCompilationUnit) anObject).getVirtualModel());
		}
		super.setDataObject(anObject);
	}

	private void listenTo(FlexoConcept concept) {
		if (concept == null) {
			return;
		}
		if (!listenedConcepts.contains(concept)) {
			listenedConcepts.add(concept);
			concept.getPropertyChangeSupport().addPropertyChangeListener(this);
			if (concept instanceof VirtualModel) {
				for (FlexoConcept c : ((VirtualModel) concept).getFlexoConcepts()) {
					listenTo(c);
				}
			}
			else {
				for (FlexoConcept c : concept.getEmbeddedFlexoConcepts()) {
					listenTo(c);
				}
			}
		}
	}

	private void stopListenTo(FlexoConcept concept) {
		if (concept == null) {
			return;
		}
		if (listenedConcepts.contains(concept)) {
			listenedConcepts.remove(concept);
			concept.getPropertyChangeSupport().removePropertyChangeListener(this);
			if (concept instanceof VirtualModel) {
				for (FlexoConcept c : ((VirtualModel) concept).getFlexoConcepts()) {
					stopListenTo(c);
				}
			}
			else {
				for (FlexoConcept c : concept.getEmbeddedFlexoConcepts()) {
					stopListenTo(c);
				}
			}
		}
	}

	@Override
	public void delete() {
		for (FlexoConcept concept : listenedConcepts) {
			if (concept.getPropertyChangeSupport() != null) {
				concept.getPropertyChangeSupport().removePropertyChangeListener(this);
			}
		}
		super.delete();
	}

	// TODO: this does not work -> investigate
	private void tryToExpand(FlexoConcept concept) {
		SwingUtilities.invokeLater(() -> {
			if (viewForComponent("browser") instanceof JFIBBrowserWidget) {
				JFIBBrowserWidget<FMLObject> browser = (JFIBBrowserWidget<FMLObject>) viewForComponent("browser");
				browser.performExpand(concept);
			}
		});

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof FlexoConcept) {
			if (evt.getPropertyName().equals(FlexoConcept.EMBEDDED_FLEXO_CONCEPT_KEY)
					|| evt.getPropertyName().equals(VirtualModel.FLEXO_CONCEPTS_KEY)) {
				if (evt.getOldValue() instanceof FlexoConcept) {
					stopListenTo((FlexoConcept) evt.getOldValue());
				}
				if (evt.getNewValue() instanceof FlexoConcept) {
					listenTo((FlexoConcept) evt.getNewValue());
				}
			}

			if (evt.getPropertyName().equals(FlexoConcept.FLEXO_PROPERTIES_KEY)
					|| evt.getPropertyName().equals(FlexoConcept.FLEXO_BEHAVIOURS_KEY)
					|| evt.getPropertyName().equals(FlexoConcept.EMBEDDED_FLEXO_CONCEPT_KEY)
					|| evt.getPropertyName().equals(VirtualModel.FLEXO_CONCEPTS_KEY)) {
				FIBCompilationUnitDetailedBrowserFIBController.this.getPropertyChangeSupport().firePropertyChange("getContents(FMLObject)",
						false, true);
			}

			if (evt.getPropertyName().equals(FlexoConcept.EMBEDDED_FLEXO_CONCEPT_KEY)
					|| evt.getPropertyName().equals(VirtualModel.FLEXO_CONCEPTS_KEY)) {
				if (evt.getNewValue() instanceof FlexoConcept) {
					tryToExpand(((FlexoConcept) evt.getNewValue()).getOwningVirtualModel());
					tryToExpand((FlexoConcept) evt.getNewValue());
				}
			}

		}
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
				returned.addAll(((VirtualModel) concept).getAllRootFlexoConcepts());
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
