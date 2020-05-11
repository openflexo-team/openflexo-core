/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.task;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openflexo.foundation.task.ThreadPoolFlexoTaskManager;
import org.openflexo.foundation.test.task.ErrorTask;
import org.openflexo.foundation.test.task.ExampleTask;
import org.openflexo.foundation.test.task.InfiniteTask;

public class TestFlexoTaskManagerWithGUI {

	public static void main(String[] args) {

		final ThreadPoolFlexoTaskManager taskManager = ThreadPoolFlexoTaskManager.createInstance();

		TaskManagerPanel panel = new TaskManagerPanel(taskManager);
		panel.setVisible(true);

		for (int i = 0; i < 10; i++) {
			ExampleTask task = new ExampleTask("ExampleTask" + i);
			taskManager.scheduleExecution(task);
		}

		JFrame frame = new JFrame();
		JPanel contentPane = new JPanel(new BorderLayout());
		JButton newTaskButton = new JButton("NewTask");
		newTaskButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				taskManager.scheduleExecution(new ExampleTask("ManuallyLaunchedTask"));
			}
		});
		JButton newTaskButton2 = new JButton("InfiniteTask");
		newTaskButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				taskManager.scheduleExecution(new InfiniteTask("InfiniteTask"));
			}
		});
		JButton newTaskButton3 = new JButton("SequentialTasks");
		newTaskButton3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExampleTask sequentialTask1 = new ExampleTask("SequentialTask1");
				ExampleTask sequentialTask2 = new ExampleTask("SequentialTask2");
				ExampleTask sequentialTask3 = new ExampleTask("SequentialTask3");
				taskManager.scheduleExecution(sequentialTask1, sequentialTask2, sequentialTask3);
			}
		});
		JButton newTaskButton4 = new JButton("ErrorTask");
		newTaskButton4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				taskManager.scheduleExecution(new ErrorTask("ErrorTask"));
			}
		});
		JButton exitButton = new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				taskManager.shutdownAndExecute(() -> {
					System.out.println("OK now i quit");
					System.exit(0);
				});
				/*taskManager.scheduleExecution(new FlexoTask("Exiting") {
					@Override
					public void run() {
						taskManager.shutdown();
						System.out.println("OK c'est fini");
					}
				});*/
			}
		});

		JPanel controlPanel = new JPanel(new FlowLayout());
		controlPanel.add(newTaskButton);
		controlPanel.add(newTaskButton2);
		controlPanel.add(newTaskButton3);
		controlPanel.add(newTaskButton4);
		controlPanel.add(exitButton);

		contentPane.add(new JLabel("Click button to launch some tasks"), BorderLayout.CENTER);
		contentPane.add(controlPanel, BorderLayout.SOUTH);

		frame.getContentPane().add(contentPane);

		frame.validate();
		frame.pack();
		frame.setVisible(true);

		// taskManager.shutdown();

	}
}
