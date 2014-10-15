package org.openflexo.task;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openflexo.foundation.task.ExampleTask;
import org.openflexo.foundation.task.FlexoTaskManager;
import org.openflexo.foundation.task.InfiniteTask;

public class TestFlexoTaskManagerWithGUI {

	public static void main(String[] args) {

		final FlexoTaskManager taskManager = new FlexoTaskManager();

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
		JButton exitButton = new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				taskManager.shutdownAndExecute(new Runnable() {
					@Override
					public void run() {
						System.out.println("OK now i quit");
						System.exit(0);
					}
				});
				/*taskManager.scheduleExecution(new FlexoTask("Exiting") {
					@Override
					public void run() {
						taskManager.shutdown();
						System.out.println("OK c'est fini");
						System.exit(-1);
					}
				});*/
			}
		});

		JPanel controlPanel = new JPanel(new FlowLayout());
		controlPanel.add(newTaskButton);
		controlPanel.add(newTaskButton2);
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