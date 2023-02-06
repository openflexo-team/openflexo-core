package org.openflexo.terminal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.cli.ParseException;
import org.openflexo.foundation.fml.cli.command.AbstractCommand;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.cli.command.directive.ExitDirective;
import org.openflexo.foundation.fml.cli.command.directive.QuitDirective;
import org.openflexo.icon.IconLibrary;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;

//TODO: Keep a global StringBuilder to decrease memory footprint

public class FMLTerminal extends JFrame {
	private JTextPane textPane;
	private JScrollPane scrollPane;
	private final String LINE_SEPARATOR = System.lineSeparator();
	private static final Font DEFAULT_FONT2 = new Font("Monospaced", Font.PLAIN, 11);

	private int terminalWidth = 80;
	private Font font;

	private FMLCommandInterpreter commandInterpreter;
	private KeyListener keyListener;

	private List<String> history = new ArrayList<String>();
	private int historyIndex;

	private int minCursorPosition;

	public FMLTerminal(FlexoServiceManager serviceManager, File userDir) {
		setTitle("FMLTerminal");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		textPane = new JTextPane();
		JPanel mainPane = new JPanel(new BorderLayout());
		JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		JButton closeButton = new JButton(FlexoLocalization.getMainLocalizer().localizedForKey("close"));
		closeButton.setIcon(IconLibrary.CLOSE_ICON);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		JButton clearButton = new JButton(FlexoLocalization.getMainLocalizer().localizedForKey("clear"));
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});
		clearButton.setIcon(IconLibrary.CLEAR_ICON);
		buttonsPane.add(closeButton);
		buttonsPane.add(clearButton);
		mainPane.add(buttonsPane, BorderLayout.NORTH);
		scrollPane = new JScrollPane();
		mainPane.add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(mainPane);
		scrollPane.setViewportView(textPane);
		textPane.addKeyListener(keyListener = new KeyListener());
		font = DEFAULT_FONT2;
		textPane.setFont(font);
		disableArrowKeys(mainPane.getInputMap());
		disableArrowKeys(textPane.getInputMap());

		// Prevent conflict between completion and focus traversal policy
		preventIntempestiveTabManagement();

		// Listen to component resize to adapt terminal width
		listenToComponentResize();

		// Prevent UP and DOWN key to control scrollbar
		disableScrollbarKeyManagement();

		// Prevent select a position before prompt
		textPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (textPane.getCaretPosition() < minCursorPosition) {
					textPane.setCaretPosition(minCursorPosition/* textPane.getDocument().getLength() */);
				}
			}
		});

		TextPaneOutputStream out = new TextPaneOutputStream(textPane, Color.BLACK);
		TextPaneOutputStream err = new TextPaneOutputStream(textPane, Color.RED);

		// Initialize and start Command Interpreter
		try {
			commandInterpreter = new FMLCommandInterpreter(serviceManager, out, err, userDir, this);
			commandInterpreter.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Return terminal width in chars according to frame size
	 * 
	 * @return
	 */
	private int getTerminalWidth() {
		return terminalWidth;
	}

	/**
	 * Disable arrow keys
	 * 
	 * @param inputMap
	 */
	private void disableArrowKeys(InputMap inputMap) {
		String[] keystrokeNames = { "UP", "DOWN", "LEFT", "RIGHT", "HOME" };
		for (int i = 0; i < keystrokeNames.length; ++i)
			inputMap.put(KeyStroke.getKeyStroke(keystrokeNames[i]), "none");
	}

	/**
	 * Prevent UP and DOWN key to control scrollbar
	 */
	private void disableScrollbarKeyManagement() {
		InputMap actionMap = (InputMap) UIManager.getDefaults().get("ScrollPane.ancestorInputMap");
		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});

		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
	}

	private void listenToComponentResize() {
		addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
			}

			@Override
			public void componentResized(ComponentEvent e) {
				terminalWidth = getBounds().width / font.getSize() * 3 / 2;
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
	}

	/**
	 * Prevent conflict between completion and focus traversal policy
	 */
	private void preventIntempestiveTabManagement() {
		setFocusTraversalPolicy(new FocusTraversalPolicy() {
			@Override
			public Component getLastComponent(Container aContainer) {
				return textPane;
			}

			@Override
			public Component getFirstComponent(Container aContainer) {
				return textPane;
			}

			@Override
			public Component getDefaultComponent(Container aContainer) {
				return textPane;
			}

			@Override
			public Component getComponentBefore(Container aContainer, Component aComponent) {
				return textPane;
			}

			@Override
			public Component getComponentAfter(Container aContainer, Component aComponent) {
				return textPane;
			}
		});
	}

	/**
	 * Shows the frame at specified location with specified size
	 * 
	 * @param xLocation
	 * @param yLocation
	 * @param width
	 * @param height
	 */
	public void open(final int xLocation, final int yLocation, final int width, final int height) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setBounds(xLocation, yLocation, width, height);
				setVisible(true);
				showPrompt();
				textPane.requestFocus();
			}
		});
	}

	/**
	 * Close and dispose the FML terminal
	 */
	public void close() {
		commandInterpreter.stop();
		dispose();
	}

	/**
	 * Clear FML terminal (erase all contents)
	 */
	public void clear() {
		textPane.setText("");
		showPrompt();
	}

	private void appendText(String text) {
		try {
			Document doc = textPane.getDocument();
			doc.insertString(doc.getLength(), text, null);
			textPane.setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {
			UIManager.getLookAndFeel().provideErrorFeedback(textPane);
		}
	}

	private void appendText(String text, boolean bold, boolean italic, Color color) {
		try {
			Document doc = textPane.getDocument();
			SimpleAttributeSet attributes = new SimpleAttributeSet();
			attributes = new SimpleAttributeSet();
			attributes.addAttribute(StyleConstants.CharacterConstants.Bold, bold);
			attributes.addAttribute(StyleConstants.CharacterConstants.Italic, italic);
			attributes.addAttribute(StyleConstants.CharacterConstants.Foreground, color);
			doc.insertString(doc.getLength(), text, attributes);
			textPane.setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {
			UIManager.getLookAndFeel().provideErrorFeedback(textPane);
		}
	}

	private void appendNewLine() {
		appendText(LINE_SEPARATOR);
	}

	private void showPrompt() {
		appendText(commandInterpreter.getPrompt(), true, false, Color.BLUE);
		appendText(" > ");
		minCursorPosition = textPane.getCaretPosition();
	}

	private void processEnterPressed() {
		disableTerminal();
		appendNewLine();
		String command = extractCommand();
		if (StringUtils.isNotEmpty(command.trim())) {
			Thread executeCommandThread = new Thread() {
				@Override
				public void run() {
					executeCommand(command);
					showPrompt();
					enableTerminal();
				}
			};
			executeCommandThread.start();

			/*SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					executeCommand(command);
					showPrompt();
					enableTerminal();
				}
			});*/
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					showPrompt();
					enableTerminal();
				}
			});
		}
	}

	private void processTabPressed() {
		disableTerminal();
		// System.out.println("On appuie sur TAB");
		String commandStart = extractCommand();
		List<String> availableCompletion = commandInterpreter.getAvailableCompletion(commandStart);
		if (availableCompletion.size() == 0) {
			UIManager.getLookAndFeel().provideErrorFeedback(textPane);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					enableTerminal();
				}
			});
			return;
		}
		String commonCompletion = commandInterpreter.getCommonCompletion(availableCompletion);
		// System.out.println("commandStart: " + commandStart);
		// System.out.println("Available: " + availableCompletion);
		// System.out.println("Common: " + commonCompletion);
		String toAdd = commonCompletion.substring(commandStart.length());
		// System.out.println("Ajouter donc " + toAdd);
		if (toAdd.length() > 0) {
			appendText(toAdd);
		}
		else {
			appendNewLine();
			int maxLength = -1;
			for (String completion : availableCompletion) {
				if (completion.length() > maxLength) {
					maxLength = completion.length();
				}
			}
			int cols = maxLength > -1 ? getTerminalWidth() / (maxLength + 1) : 1;
			if (cols == 0) {
				cols = 1;
			}
			Document doc = textPane.getDocument();
			int i = 0;
			for (String completion : availableCompletion) {
				appendText(completion + StringUtils.buildWhiteSpaceIndentation(maxLength + 1 - completion.length()));
				i++;
				if (i % cols == 0) {
					appendNewLine();
				}
			}
			if (i % cols != 0) {
				appendNewLine();
			}
			showPrompt();
			appendText(commandStart);
			textPane.setCaretPosition(doc.getLength());
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				enableTerminal();
			}
		});
	}

	private void replaceWithCommand(String command) {
		try {
			Document doc = textPane.getDocument();
			String current = extractCommand();
			doc.remove(doc.getLength() - current.length(), current.length());
			doc.insertString(doc.getLength(), command, null);
			textPane.setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {
			UIManager.getLookAndFeel().provideErrorFeedback(textPane);
		}
	}

	private void processUpPressed() {
		// System.out.println("On fait UP");
		if (historyIndex == history.size()) {
			// Memorize current command
			String current = extractCommand();
			if (StringUtils.isNotEmpty(current.trim())) {
				history.add(current);
				// System.out.println("On ajoute: {" + current + "}");
			}
		}
		if (historyIndex > 0) {
			historyIndex--;
			// System.out.println("On affiche: " + history.get(historyIndex));
			replaceWithCommand(history.get(historyIndex));
		}
		else {
			UIManager.getLookAndFeel().provideErrorFeedback(textPane);
		}
	}

	private void processDownPressed() {
		// System.out.println("On fait DOWN");
		if (historyIndex < history.size() - 1) {
			historyIndex++;
			// System.out.println("On affiche: " + history.get(historyIndex));
			replaceWithCommand(history.get(historyIndex));
		}
		else {
			UIManager.getLookAndFeel().provideErrorFeedback(textPane);
		}
	}

	private class KeyListener extends KeyAdapter {
		private final int ENTER_KEY = KeyEvent.VK_ENTER;
		private final int BACK_SPACE_KEY = KeyEvent.VK_BACK_SPACE;
		private final String BACK_SPACE_KEY_BINDING = getKeyBinding(textPane.getInputMap(), "BACK_SPACE");
		private final int TAB_KEY = KeyEvent.VK_TAB;
		private final int UP_KEY = KeyEvent.VK_UP;
		private final int DOWN_KEY = KeyEvent.VK_DOWN;

		private boolean isKeysDisabled;

		private String getKeyBinding(InputMap inputMap, String name) {
			return (String) inputMap.get(KeyStroke.getKeyStroke(name));
		}

		@Override
		public void keyPressed(KeyEvent evt) {
			int keyCode = evt.getKeyCode();
			if (keyCode == BACK_SPACE_KEY) {
				int cursorPosition = textPane.getCaretPosition();
				if (cursorPosition == minCursorPosition && !isKeysDisabled) {
					disableBackspaceKey();
				}
				else if (cursorPosition > minCursorPosition && isKeysDisabled) {
					enableBackspaceKey();
				}
			}
			else if (keyCode == ENTER_KEY) {
				processEnterPressed();
			}
			else if (keyCode == TAB_KEY) {
				processTabPressed();
			}
			else if (keyCode == UP_KEY) {
				processUpPressed();
			}
			else if (keyCode == DOWN_KEY) {
				processDownPressed();
			}
		}

		@Override
		public void keyReleased(KeyEvent evt) {
			int keyCode = evt.getKeyCode();
			/*
			 * if (keyCode == ENTER_KEY) {
			 * textPane.setCaretPosition(textPane.getCaretPosition() - 1);
			 * setMinCursorPosition(); }
			 */
		}

		private void disableBackspaceKey() {
			isKeysDisabled = true;
			textPane.getInputMap().put(KeyStroke.getKeyStroke("BACK_SPACE"), "none");
		}

		private void enableBackspaceKey() {
			isKeysDisabled = false;
			textPane.getInputMap().put(KeyStroke.getKeyStroke("BACK_SPACE"), BACK_SPACE_KEY_BINDING);
		}

	}

	public void enableTerminal() {
		textPane.setEnabled(true);
	}

	public void disableTerminal() {
		textPane.setEnabled(false);
	}

	private List<String> executeCommand(String commandString) {
		String commandWithoutLineSeparator = commandString.substring(0, commandString.indexOf(LINE_SEPARATOR));
		List<String> output = null;
		try {
			boolean hadFocusedObject = (commandInterpreter.getFocusedObject() != null);
			AbstractCommand executeCommand = commandInterpreter.executeCommand(commandWithoutLineSeparator);
			output = commandInterpreter.getOutput();
			if (executeCommand instanceof QuitDirective) {
				close();
			}
			if (executeCommand instanceof ExitDirective && !hadFocusedObject) {
				close();
			}
		} catch (ParseException e) {
			commandInterpreter.getErrStream().println("Syntax error : " + e.getMessage());
		} catch (FMLCommandExecutionException e) {
			commandInterpreter.getErrStream().println("Execution error : " + e.getMessage());
		}
		if (StringUtils.isNotEmpty(commandWithoutLineSeparator.trim())) {
			history.add(commandWithoutLineSeparator);
			historyIndex = history.size();
		}

		return output;
	}

	private String extractCommand() {
		// removeLastLineSeparator();
		String newCommand = stripPreviousCommands();
		return newCommand;
	}

	private String stripPreviousCommands() {
		String terminalText = textPane.getText();
		int lastPromptIndex = terminalText.lastIndexOf(commandInterpreter.getPrompt()) + commandInterpreter.getPrompt().length() + 3;
		if (lastPromptIndex < 0 || lastPromptIndex >= terminalText.length())
			return "";
		else
			return terminalText.substring(lastPromptIndex);
	}

	public void displayHistory() {
		// String commandStart = extractCommand();
		for (String command : history) {
			appendText(command);
			appendNewLine();
		}
		// showPrompt();
		// appendText(commandStart);
	}

}
