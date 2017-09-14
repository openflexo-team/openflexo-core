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

package org.openflexo.view.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.help.CSH;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.openflexo.components.AboutDialog;
import org.openflexo.drm.DocItem;
import org.openflexo.drm.TrackComponentCHForHelpSubmission;
import org.openflexo.drm.TrackComponentCHForHelpView;
import org.openflexo.help.FlexoHelp;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.module.Module;
import org.openflexo.toolbox.ToolBox;
import org.openflexo.view.controller.FlexoController;

/**
 * Automatic builded 'Help' menu for modules
 * 
 * @author sguerin
 */
@SuppressWarnings("serial")
public class HelpMenu extends FlexoMenu implements Observer {

	private JMenuItem generalHelp;

	private JMenuItem flexoHelp;

	private JMenuItem modelingHelp;

	private JMenuItem helpOn;

	private JMenuItem submitHelpFor;

	private JMenuItem aboutFlexo;

	private JMenuItem[] modulesHelp;

	private ActionListener helpActionListener;

	public HelpMenu(final FlexoController controller) {
		super("help", controller);

		helpActionListener = new CSH.DisplayHelpFromSource(FlexoHelp.getHelpBroker());

		if (getController().getApplicationContext().getDocResourceManager() != null
				&& getController().getApplicationContext().getDocResourceManager().getDocResourceCenter() != null) {

			generalHelp = new JMenuItem();
			generalHelp.setAccelerator(ToolBox.getPLATFORM() == ToolBox.MACOS ? KeyStroke.getKeyStroke(KeyEvent.VK_HELP, 0)
					: KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
			generalHelp.setText(FlexoLocalization.getMainLocalizer().localizedForKey("general_help", generalHelp));
			DocItem drcItem = getController().getApplicationContext().getDocResourceManager().getDocResourceCenterItem();
			if (drcItem != null) {
				CSH.setHelpIDString(generalHelp, drcItem.getIdentifier());
			}
			generalHelp.addActionListener(helpActionListener);
			add(generalHelp);

			flexoHelp = new JMenuItem();
			flexoHelp.setText(FlexoLocalization.getMainLocalizer().localizedForKey("flexo_help", flexoHelp));
			CSH.setHelpIDString(flexoHelp,
					getController().getApplicationContext().getDocResourceManager().getFlexoToolSetItem().getIdentifier());
			flexoHelp.addActionListener(helpActionListener);
			add(flexoHelp);

			modelingHelp = new JMenuItem();
			modelingHelp.setText(FlexoLocalization.getMainLocalizer().localizedForKey("modeling_help", modelingHelp));
			CSH.setHelpIDString(modelingHelp,
					getController().getApplicationContext().getDocResourceManager().getFlexoModelItem().getIdentifier());
			modelingHelp.addActionListener(helpActionListener);
			add(modelingHelp);

			addSeparator();
			modulesHelp = new JMenuItem[controller.getApplicationContext().getModuleLoader().getKnownModules().size()];
			int i = 0;
			for (Module<?> module : controller.getApplicationContext().getModuleLoader().getKnownModules()) {
				modulesHelp[i] = new JMenuItem();
				modulesHelp[i].setText(FlexoLocalization.getMainLocalizer().localizedForKey(module.getName(), modulesHelp[i]));
				CSH.setHelpIDString(modulesHelp[i], module.getHelpTopic());
				modulesHelp[i].addActionListener(helpActionListener);
				add(modulesHelp[i]);
				i++;
			}

			addSeparator();
			helpOn = new JMenuItem();
			helpOn.setText(FlexoLocalization.getMainLocalizer().localizedForKey("help_on", helpOn));
			// helpOn.addActionListener(new CSH.DisplayHelpAfterTracking(FlexoHelp.getHelpBroker()));
			helpOn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new TrackComponentCHForHelpView(controller.getFlexoFrame(), controller.getApplicationContext());
				}
			});
			add(helpOn);

			if (getModuleLoader().allowsDocSubmission()) {
				submitHelpFor = new JMenuItem();
				submitHelpFor.setText(FlexoLocalization.getMainLocalizer().localizedForKey("submit_help_for", submitHelpFor));
				// helpOn.addActionListener(new CSH.DisplayHelpAfterTracking(FlexoHelp.getHelpBroker()));
				submitHelpFor.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						new TrackComponentCHForHelpSubmission(controller.getFlexoFrame(), controller.getApplicationContext());
					}
				});
				add(submitHelpFor);
			}
		}

		if (ToolBox.getPLATFORM() != ToolBox.MACOS) {
			addSeparator();
			aboutFlexo = new JMenuItem();
			aboutFlexo.setText(FlexoLocalization.getMainLocalizer().localizedForKey("about_flexo", aboutFlexo));
			aboutFlexo.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					new AboutDialog();
				}

			});
			add(aboutFlexo);
		}

		FlexoHelp.instance.addObserver(this);
	}

	@Override
	public void dispose() {
		FlexoHelp.instance.deleteObserver(this);
		super.dispose();
	}

	@Override
	public void update(Observable observable, Object arg) {
		if (observable instanceof FlexoHelp) {
			generalHelp.removeActionListener(helpActionListener);
			flexoHelp.removeActionListener(helpActionListener);
			modelingHelp.removeActionListener(helpActionListener);
			for (JMenuItem item : modulesHelp) {
				item.removeActionListener(helpActionListener);
			}
			helpActionListener = new CSH.DisplayHelpFromSource(FlexoHelp.getHelpBroker());
			generalHelp.addActionListener(helpActionListener);
			flexoHelp.addActionListener(helpActionListener);
			modelingHelp.addActionListener(helpActionListener);
			for (JMenuItem item : modulesHelp) {
				item.addActionListener(helpActionListener);
			}
		}
	}
}
