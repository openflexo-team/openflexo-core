/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.view.controller;

import java.awt.Dimension;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.annotations.NotificationUnsafe;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourActionType;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.FIBVariable;
import org.openflexo.gina.model.FIBWidget;
import org.openflexo.gina.model.container.FIBPanel;
import org.openflexo.gina.model.container.FIBPanel.Border;
import org.openflexo.gina.model.container.FIBPanel.FlowLayoutAlignment;
import org.openflexo.gina.model.container.FIBPanel.Layout;
import org.openflexo.gina.model.container.layout.BorderLayoutConstraints;
import org.openflexo.gina.model.container.layout.BorderLayoutConstraints.BorderLayoutLocation;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints;
import org.openflexo.gina.model.container.layout.TwoColsLayoutConstraints.TwoColsLayoutLocation;
import org.openflexo.gina.model.widget.FIBButton;
import org.openflexo.gina.model.widget.FIBLabel;
import org.openflexo.gina.model.widget.FIBLabel.Align;
import org.openflexo.gina.swing.utils.JFIBDialog;
import org.openflexo.gina.swing.view.SwingViewFactory;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.toolbox.StringUtils;

public class ParametersRetriever<ES extends FlexoBehaviour> {

	private static final Logger logger = Logger.getLogger(ParametersRetriever.class.getPackage().getName());

	private final FlexoBehaviourAction<?, ES, ?> action;
	private final ApplicationContext applicationContext;

	private FIBModelFactory fibModelFactory;

	public ParametersRetriever(FlexoBehaviourAction<?, ES, ?> action, ApplicationContext applicationContext) {
		this.action = action;
		this.applicationContext = applicationContext;
		try {
			fibModelFactory = new FIBModelFactory(applicationContext != null ? applicationContext.getTechnologyAdapterService() : null);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		if (action != null) {
			action.retrieveDefaultParameters();
		}
	}

	public boolean isSkipable() {
		boolean successfullyRetrievedDefaultParameters = action.retrieveDefaultParameters();

		if (successfullyRetrievedDefaultParameters && action.getFlexoBehaviour().getSkipConfirmationPanel()) {
			return true;
		}

		return false;
	}

	public boolean retrieveParameters() {

		FIBComponent component = makeFIB(true, true);
		JFIBDialog<?> dialog = JFIBDialog.instanciateDialog(component, action,
				applicationContext.getModuleLoader().getActiveModule().getFlexoFrame(), true, new ParametersRetrieverController(component,
						SwingViewFactory.INSTANCE, applicationContext.getModuleLoader().getActiveModule().getController()));
		dialog.setTitle(action.getLocalizedName());
		if (!action.getFlexoBehaviour().getDefinePopupDefaultSize()) {
			dialog.setMinimumSize(new Dimension(500, 50));
		}

		dialog.showDialog();
		return dialog.getStatus() == Status.VALIDATED;
	}

	private FIBComponent makeWidget(final FlexoBehaviourParameter parameter, FIBPanel panel, int index) {
		if (applicationContext != null) {
			for (TechnologyAdapter ta : applicationContext.getTechnologyAdapterService().getTechnologyAdapters()) {
				TechnologyAdapterController<?> tac = applicationContext.getTechnologyAdapterControllerService()
						.getTechnologyAdapterController(ta);
				boolean[] expand = { true, false };
				FIBComponent returned = tac.makeWidget(parameter, action, fibModelFactory, "data", expand);

				((FIBWidget) returned).setData(new DataBinding<>("data.parameters." + parameter.getName()));
				if (returned instanceof FIBWidget) {
					((FIBWidget) returned).setValueChangedAction(new DataBinding<>("controller.parameterValueChanged(data)"));
				}
				panel.addToSubComponents(returned, new TwoColsLayoutConstraints(TwoColsLayoutLocation.right, expand[0], expand[1]), index);
				if (returned != null) {
					return returned;
				}
			}
		}
		return null;

	}

	protected FIBComponent makeFIB(boolean addTitle, boolean addControls) {

		if (action == null) {
			return fibModelFactory.newFIBPanel();
		}

		final FlexoBehaviour flexoBehaviour = action.getFlexoBehaviour();

		FIBPanel returned = fibModelFactory.newFIBPanel();

		FIBVariable<?> dataVariable = fibModelFactory.newFIBVariable(returned, "data");
		dataVariable.setType(FlexoBehaviourActionType.getFlexoBehaviourActionType(action.getFlexoBehaviour()));

		/*FIBVariable<?> dataVariable = fibModelFactory.newFIBVariable(returned, "data");
		dataVariable.setType(FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoBehaviour.getFlexoConcept()));
		FIBVariable<?> behaviourVariable = fibModelFactory.newFIBVariable(returned, "behaviour");
		behaviourVariable.setType(FlexoBehaviourActionType.getFlexoBehaviourActionType(action.getFlexoBehaviour()));*/

		returned.setBindingFactory(action.getFlexoBehaviour().getBindingFactory());

		returned.setLayout(Layout.twocols);
		returned.setBorder(Border.empty);
		returned.setBorderTop(10);
		returned.setBorderBottom(5);
		returned.setBorderRight(10);
		returned.setBorderLeft(10);
		returned.setControllerClass(ParametersRetrieverController.class);

		if (flexoBehaviour.getDefinePopupDefaultSize()) {
			returned.setMinWidth(flexoBehaviour.getWidth());
			returned.setMinHeight(flexoBehaviour.getHeight());
		}

		LocalizedDelegate dict = flexoBehaviour.getDeclaringCompilationUnit().getLocalizedDictionary();

		int index = 0;
		if (addTitle) {
			FIBLabel titleLabel = fibModelFactory.newFIBLabel();
			titleLabel.setAlign(Align.center);
			titleLabel.setLabel(dict.localizedForKey(flexoBehaviour.getName()));
			returned.addToSubComponents(titleLabel, new TwoColsLayoutConstraints(TwoColsLayoutLocation.center, true, false), 0);
			index++;

			if (StringUtils.isNotEmpty(flexoBehaviour.getDescription())) {
				FIBPanel descriptionPanel = fibModelFactory.newFIBPanel();
				descriptionPanel.setLayout(Layout.twocols);
				descriptionPanel.setBorder(Border.rounded3d);
				descriptionPanel.setLayout(Layout.border);
				descriptionPanel.setBorderTop(10);
				descriptionPanel.setBorderBottom(10);

				FIBLabel descriptionLabel = fibModelFactory.newFIBLabel();
				descriptionLabel.setAlign(Align.center);
				descriptionLabel.setLabel("<html><i>" + flexoBehaviour.getDescription() + "</i></html>");
				descriptionPanel.addToSubComponents(descriptionLabel, new BorderLayoutConstraints(BorderLayoutLocation.center));
				returned.addToSubComponents(descriptionPanel, new TwoColsLayoutConstraints(TwoColsLayoutLocation.center, true, false), 1);
				index++;
			}
			else {
				((TwoColsLayoutConstraints) titleLabel.getConstraints()).setInsetsBottom(10);
			}
		}

		Hashtable<FlexoBehaviourParameter, FIBComponent> widgets = new Hashtable<>();
		for (final FlexoBehaviourParameter parameter : flexoBehaviour.getParameters()) {
			FIBLabel label = fibModelFactory.newFIBLabel();
			label.setLabel(dict.localizedForKey(parameter.getName()));
			returned.addToSubComponents(label, new TwoColsLayoutConstraints(TwoColsLayoutLocation.left, false, false), index++);
			FIBComponent widget = makeWidget(parameter, returned, index);
			if (widget != null) {
				widgets.put(parameter, widget);
				index++;
			}
			else {
				logger.warning("Cannot instanciate widget for " + parameter + " of " + (parameter != null ? parameter.getClass() : "null"));
			}
		}
		/*for (final FlexoBehaviourParameter parameter : flexoBehaviour.getParameters()) {
			if (parameter instanceof URIParameter) {
				FIBPanel uriPanel = (FIBPanel) widgets.get(parameter);
				List<FlexoBehaviourParameter> dependancies = ((URIParameter) parameter).getDependancies();
				if (dependancies != null) {
					for (FlexoBehaviourParameter dep : dependancies) {
						FIBComponent dependingComponent = widgets.get(dep);
						uriPanel.getComponentNamed("tf").addToExplicitDependancies(fibModelFactory.newFIBDependancy(dependingComponent));
					}
				}
			}
		}*/

		if (addControls) {
			FIBPanel buttonsPanel = fibModelFactory.newFIBPanel();

			buttonsPanel.setLayout(Layout.flow);
			buttonsPanel.setFlowAlignment(FlowLayoutAlignment.CENTER);
			buttonsPanel.setHGap(0);
			buttonsPanel.setVGap(5);
			buttonsPanel.setBorderTop(5);
			buttonsPanel.setBorder(Border.empty);
			FIBButton validateButton = fibModelFactory.newFIBButton();
			validateButton.setLabel("validate");
			validateButton.setLocalize(true);
			validateButton.setAction(new DataBinding<>("controller.validateAndDispose()"));
			validateButton.setEnable(new DataBinding<>("controller.isValidable(data)"));
			for (FIBComponent widget : widgets.values()) {
				validateButton.addToExplicitDependancies(fibModelFactory.newFIBDependancy(widget));
			}
			buttonsPanel.addToSubComponents(validateButton);
			FIBButton cancelButton = fibModelFactory.newFIBButton();
			cancelButton.setLabel("cancel");
			cancelButton.setLocalize(true);
			cancelButton.setAction(new DataBinding<>("controller.cancelAndDispose()"));
			buttonsPanel.addToSubComponents(cancelButton);

			returned.addToSubComponents(buttonsPanel, new TwoColsLayoutConstraints(TwoColsLayoutLocation.center, true, false), index++);
		}
		ValidationReport validationReport;
		try {
			validationReport = returned.validate();
			for (ValidationError<?, ?> error : validationReport.getAllErrors()) {
				logger.warning("Parameters retriever FIBComponent validation error: Object: " + error.getValidable() + " message: "
						+ validationReport.getValidationModel().localizedIssueMessage(error) + " detais="
						+ validationReport.getValidationModel().localizedIssueDetailedInformations(error));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return returned;
	}

	/*public FIBPanel makeURIPanel(final URIParameter parameter) {
		FIBPanel returned = fibModelFactory.newFIBPanel();
		returned.setName(parameter.getName() + "URIPanel");
		returned.setLayout(Layout.gridbag);
		FIBTextField tf = fibModelFactory.newFIBTextField();
		tf.setName("tf");
		FIBLabel uriLabel = fibModelFactory.newFIBLabel("http://xxxxxx.owl");
		uriLabel.setName("uriLabel");
		Font f = uriLabel.retrieveValidFont();
		if (f != null) {
			uriLabel.setFont(f.deriveFont(10f));
		} else {
			uriLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		}
		uriLabel.setData(new DataBinding<Object>("data.retrieveFullURI" + "(data.parametersDefinitions" + "." + parameter.getName() + ")"));
		returned.addToSubComponents(tf, new GridBagLayoutConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE,
				GridBagConstraints.REMAINDER, 1, 1.0, 0, AnchorType.west, FillType.horizontal, 0, 0, 0, 0, 0, 0));
		returned.addToSubComponents(uriLabel, new GridBagLayoutConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE,
				GridBagConstraints.REMAINDER, 1, 1.0, 0, AnchorType.west, FillType.horizontal, -3, 0, 0, 0, 0, 0));
		tf.setData(new DataBinding<Object>("data.parameters." + parameter.getName()));
		return returned;
	}*/

	/*public class URIPanel extends FIBPanel {
	public FIBTextField tf;
	public FIBLabel uriLabel;
	
	public URIPanel(final URIParameter parameter) {
		super();
		setName(parameter.getName() + "URIPanel");
		setLayout(Layout.gridbag);
		tf = new FIBTextField();
		uriLabel = new FIBLabel("http://xxxxxx.owl");
		Font f = uriLabel.retrieveValidFont();
		if (f != null) {
			uriLabel.setFont(f.deriveFont(10f));
		} else {
			uriLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		}
		uriLabel.setData(new DataBinding<Object>("data.retrieveFullURI" + "(data.parametersDefinitions" + "." + parameter.getName()
				+ ")"));
		addToSubComponents(tf, new GridBagLayoutConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE,
				GridBagConstraints.REMAINDER, 1, 1.0, 0, AnchorType.west, FillType.horizontal, 0, 0, 0, 0, 0, 0));
		addToSubComponents(uriLabel, new GridBagLayoutConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE,
				GridBagConstraints.REMAINDER, 1, 1.0, 0, AnchorType.west, FillType.horizontal, -3, 0, 0, 0, 0, 0));
		tf.setData(new DataBinding<Object>("data.parameters." + parameter.getName()));
	}
	}*/

	public static class ParametersRetrieverController extends FlexoFIBController {

		public ParametersRetrieverController(FIBComponent component, GinaViewFactory<?> viewFactory) {
			super(component, viewFactory);
		}

		public ParametersRetrieverController(FIBComponent component, GinaViewFactory<?> viewFactory, FlexoController flexoController) {
			super(component, viewFactory);
			setFlexoController(flexoController);
		}

		@NotificationUnsafe
		public boolean isValidable(FlexoBehaviourAction<?, ?, ?> action) {
			if (action == null) {
				// Called during initialization, don't care
				return false;
			}
			return action.areRequiredParametersSetAndValid();
		}

		public boolean parameterValueChanged(FlexoBehaviourAction<?, ?, ?> action) {
			action.parameterValueChanged();
			return true;
		}

	}
}
