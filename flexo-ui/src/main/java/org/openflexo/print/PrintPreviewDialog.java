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

package org.openflexo.print;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openflexo.diana.ScreenshotBuilder;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.view.FlexoDialog;
import org.openflexo.view.controller.FlexoController;

public class PrintPreviewDialog extends FlexoDialog {

	protected static final Logger logger = Logger.getLogger(PrintPreviewDialog.class.getPackage().getName());

	public enum ReturnedStatus {
		CONTINUE_PRINTING, CANCELLED
	};

	protected PrintManagingController _controller;
	protected ReturnedStatus status = ReturnedStatus.CONTINUE_PRINTING;
	protected FlexoPrintableComponent _printableComponent;

	private final JTextField scaleTF;
	private final JLabel pagesLabel;
	private final JScrollPane scrollPane;

	public PrintPreviewDialog(PrintManagingController controller, FlexoPrintableComponent printableProcessView) {
		super(controller.getFlexoFrame(), true);
		_controller = controller;
		_printableComponent = printableProcessView;

		setTitle(FlexoLocalization.getMainLocalizer().localizedForKey("print_preview"));

		JPanel topPanel = new JPanel(new FlowLayout());
		JButton plusScale = new JButton("+");
		JButton minusScale = new JButton("-");

		final JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 0, 500, 100);
		slider.setMajorTickSpacing(100);
		slider.setMinorTickSpacing(20);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);

		pagesLabel = new JLabel("?? x ?? pages");
		scaleTF = new JTextField("" + (int) (printableProcessView.getPrintableDelegate().getScale() * 100) + "%");

		topPanel.add(minusScale);
		topPanel.add(slider);
		topPanel.add(scaleTF);
		topPanel.add(plusScale);
		topPanel.add(pagesLabel);

		final ChangeListener sliderChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (slider.getValue() > 0) {
					_printableComponent.getPrintableDelegate().setScale((double) slider.getValue() / 100);
					update();
					_printableComponent.getPrintableDelegate().refresh();
				}
			}
		};

		plusScale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_printableComponent.getPrintableDelegate().setScale(_printableComponent.getPrintableDelegate().getScale() * 1.1);
				update();
				slider.removeChangeListener(sliderChangeListener);
				slider.setValue((int) (_printableComponent.getPrintableDelegate().getScale() * 100));
				slider.addChangeListener(sliderChangeListener);
				_printableComponent.getPrintableDelegate().refresh();
			}
		});
		minusScale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_printableComponent.getPrintableDelegate().setScale(_printableComponent.getPrintableDelegate().getScale() * 0.9);
				update();
				slider.removeChangeListener(sliderChangeListener);
				slider.setValue((int) (_printableComponent.getPrintableDelegate().getScale() * 100));
				slider.addChangeListener(sliderChangeListener);
				_printableComponent.getPrintableDelegate().refresh();
			}
		});

		slider.addChangeListener(sliderChangeListener);

		printableProcessView.getPrintableDelegate().preview(controller.getPrintManager().getPageFormat());
		scrollPane = new JScrollPane((Component) printableProcessView);
		scrollPane.getViewport().setPreferredSize(new Dimension(700, 500));

		final JSlider previewScaleSlider = new JSlider(SwingConstants.VERTICAL, 0, 200, 100);
		previewScaleSlider.setMajorTickSpacing(50);
		previewScaleSlider.setMinorTickSpacing(10);
		previewScaleSlider.setPaintTicks(true);
		previewScaleSlider.setPaintLabels(true);

		final ChangeListener previewScaleChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (previewScaleSlider.getValue() > 0) {
					_printableComponent.getPrintableDelegate().setPreviewScale((double) previewScaleSlider.getValue() / 100);
					update();
					_printableComponent.getPrintableDelegate().refresh();
				}
			}
		};
		previewScaleSlider.addChangeListener(previewScaleChangeListener);

		final JCheckBox showPages = new JCheckBox(FlexoLocalization.getMainLocalizer().localizedForKey("show_pages"),
				_printableComponent.getPrintableDelegate().showPages());
		showPages.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_printableComponent.getPrintableDelegate().setShowPages(showPages.isSelected());
				update();
				_printableComponent.getPrintableDelegate().refresh();
			}
		});
		final JCheckBox showTitle = new JCheckBox(FlexoLocalization.getMainLocalizer().localizedForKey("show_title"),
				_printableComponent.getPrintableDelegate().showTitles());
		showTitle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_printableComponent.getPrintableDelegate().setShowTitles(showTitle.isSelected());
				update();
				_printableComponent.getPrintableDelegate().refresh();
			}
		});
		final JTextField titleTF = new JTextField(30);
		titleTF.setText(_printableComponent.getPrintableDelegate().getPrintTitle());
		titleTF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_printableComponent.getPrintableDelegate().setPrintTitle(titleTF.getText());
				update();
				_printableComponent.getPrintableDelegate().refresh();
			}
		});

		JPanel paramsPanel = new JPanel(new FlowLayout());
		paramsPanel.add(showPages);
		paramsPanel.add(showTitle);
		paramsPanel.add(titleTF);

		JPanel centerPane = new JPanel(new BorderLayout());
		centerPane.add(previewScaleSlider, BorderLayout.WEST);
		centerPane.add(scrollPane, BorderLayout.CENTER);
		centerPane.add(paramsPanel, BorderLayout.SOUTH);

		JPanel bottomPanel = new JPanel(new FlowLayout());
		JButton printButton = new JButton();
		printButton.setText(FlexoLocalization.getMainLocalizer().localizedForKey("print", printButton));
		printButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				status = ReturnedStatus.CONTINUE_PRINTING;
				print();
				dispose();
			}
		});

		JButton saveAsJPGButton = new JButton();
		saveAsJPGButton.setText(FlexoLocalization.getMainLocalizer().localizedForKey("save_as_image", saveAsJPGButton));
		saveAsJPGButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				status = ReturnedStatus.CANCELLED;
				saveAsJpeg();
				dispose();
			}
		});

		JButton cancelButton = new JButton();
		cancelButton.setText(FlexoLocalization.getMainLocalizer().localizedForKey("cancel", cancelButton));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				status = ReturnedStatus.CANCELLED;
				dispose();
			}
		});

		JButton pageSetupButton = new JButton();
		pageSetupButton.setText(FlexoLocalization.getMainLocalizer().localizedForKey("page_setup", pageSetupButton));
		pageSetupButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_printableComponent.getPrintableDelegate().setPageFormat(_controller.getPrintManager().pageSetup());
				update();
				slider.removeChangeListener(sliderChangeListener);
				slider.setValue((int) (_printableComponent.getPrintableDelegate().getScale() * 100));
				slider.addChangeListener(sliderChangeListener);
				_printableComponent.getPrintableDelegate().refresh();
			}
		});

		JButton fitToPageButton = new JButton();
		fitToPageButton.setText(FlexoLocalization.getMainLocalizer().localizedForKey("fit_to_page", fitToPageButton));
		fitToPageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_printableComponent.getPrintableDelegate().fitToPage();
				update();
				slider.removeChangeListener(sliderChangeListener);
				slider.setValue((int) (_printableComponent.getPrintableDelegate().getScale() * 100));
				slider.addChangeListener(sliderChangeListener);
				_printableComponent.getPrintableDelegate().refresh();
			}
		});

		bottomPanel.add(printButton);
		bottomPanel.add(saveAsJPGButton);
		bottomPanel.add(cancelButton);
		bottomPanel.add(pageSetupButton);
		bottomPanel.add(fitToPageButton);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(topPanel, BorderLayout.NORTH);
		contentPane.add(centerPane, BorderLayout.CENTER);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(printButton);
		getContentPane().add(contentPane);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		validate();
		pack();
		update();
		setVisible(true);
	}

	protected void update() {
		scaleTF.setText("" + (int) (_printableComponent.getPrintableDelegate().getScale() * 100) + "%");
		pagesLabel.setText(_printableComponent.getPrintableDelegate().getWidthPageNb() + " x "
				+ _printableComponent.getPrintableDelegate().getHeightPageNb() + " "
				+ FlexoLocalization.getMainLocalizer().localizedForKey("pages"));
		scrollPane.getViewport().reshape(scrollPane.getViewport().getViewPosition().x, scrollPane.getViewport().getViewPosition().y,
				_printableComponent.getWidth(), _printableComponent.getHeight());
		scrollPane.revalidate();
		scrollPane.repaint();
	}

	public ReturnedStatus getStatus() {
		return status;
	}

	public void print() {
		_controller.getPrintManager().printPageable(_printableComponent.getPrintableDelegate());
	}

	private File dest = null;

	public void saveAsJpeg() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setDialogTitle(FlexoLocalization.getMainLocalizer().localizedForKey("save_as_image", chooser));

		int returnVal = chooser.showSaveDialog(null);
		if (returnVal == JFileChooser.CANCEL_OPTION) {
			return;
		}
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if (isValidProjectName(chooser.getSelectedFile().getName())) {
				dest = chooser.getSelectedFile();
				if (!dest.getName().toLowerCase().endsWith(".png")) {
					dest = new File(dest.getAbsolutePath() + ".png");
				}
			}
			else {
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Invalid file name. The following characters are not allowed: "
							+ FileUtils.BAD_CHARACTERS_FOR_FILE_NAME_REG_EXP);
				}
				FlexoController
						.notify(FlexoLocalization.getMainLocalizer().localizedForKey("file_name_cannot_contain_\\___&_#_{_}_[_]_%_~"));
			}
		}
		else {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("No project specified !");
			}
		}
		if (dest == null) {
			return;
		}

		ScreenshotBuilder builder = new ScreenshotBuilder<FlexoObject>() {
			@Override
			public JComponent getScreenshotComponent(FlexoObject object) {
				return (JComponent) getPrintableComponent();
			}

			@Override
			public String getScreenshotName(FlexoObject o) {
				return dest.getName();
			}
		};

		ScreenshotBuilder.ScreenshotImage<?> image = builder.getImage(getPrintableComponent().getFlexoObject());

		try {
			if (!dest.exists()) {
				FileUtils.createNewFile(dest);
			}
			ImageIO.write(image.image, "png", dest);
		} catch (Exception e) {
			e.printStackTrace();
			FlexoController.showError(e.getMessage());
		}
	}

	private static boolean isValidProjectName(String absolutePath) {
		return absolutePath != null && absolutePath.trim().length() > 0
				&& !FileUtils.BAD_CHARACTERS_FOR_FILE_NAME_PATTERN.matcher(absolutePath).find();
	}

	public FlexoPrintableComponent getPrintableComponent() {
		return _printableComponent;
	}
}
