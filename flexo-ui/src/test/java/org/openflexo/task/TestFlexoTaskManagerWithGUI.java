package org.openflexo.task;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openflexo.foundation.task.ErrorTask;
import org.openflexo.foundation.task.ExampleTask;
import org.openflexo.foundation.task.InfiniteTask;
import org.openflexo.foundation.task.ThreadPoolFlexoTaskManager;

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