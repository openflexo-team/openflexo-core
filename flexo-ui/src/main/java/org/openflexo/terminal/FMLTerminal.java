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
import java.io.File;
import java.io.IOException;
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
import org.openflexo.foundation.fml.cli.command.directive.ExitDirective;
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

		TextPaneOutputStream out = new TextPaneOutputStream(textPane, Color.BLACK);
		TextPaneOutputStream err = new TextPaneOutputStream(textPane, Color.RED);

		// Initialize and start Command Interpreter
		try {
			commandInterpreter = new FMLCommandInterpreter(serviceManager, out, err, userDir);
			commandInterpreter.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

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
				// System.out.println("terminalWidth=" + terminalWidth);
				/*String hop = StringUtils.buildString('A', terminalWidth);
				try {
					Document doc = textPane.getDocument();
					doc.insertString(doc.getLength(), hop, null);
					showNewLine();
				} catch (BadLocationException e2) {
					UIManager.getLookAndFeel().provideErrorFeedback(textPane);
				}*/
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

	public void close() {
		commandInterpreter.stop();
		dispose();
	}

	public void clear() {
		textPane.setText("");
		showPrompt();
	}

	private void showPrompt() {
		try {
			Document doc = textPane.getDocument();

			SimpleAttributeSet attributes = new SimpleAttributeSet();
			attributes = new SimpleAttributeSet();
			attributes.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
			attributes.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.FALSE);
			attributes.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.BLUE);

			doc.insertString(doc.getLength(), commandInterpreter.getPrompt(), attributes);
			doc.insertString(doc.getLength(), " > ", null);
			textPane.setCaretPosition(doc.getLength());
			keyListener.minCursorPosition = textPane.getCaretPosition();
		} catch (BadLocationException e) {
			UIManager.getLookAndFeel().provideErrorFeedback(textPane);
		}
	}

	private void showNewLine() {
		// textPane.setText(textPane.getText() + LINE_SEPARATOR);
		try {
			Document doc = textPane.getDocument();
			doc.insertString(doc.getLength(), LINE_SEPARATOR, null);
		} catch (BadLocationException e) {
			UIManager.getLookAndFeel().provideErrorFeedback(textPane);
		}
	}

	private class KeyListener extends KeyAdapter {
		private final int ENTER_KEY = KeyEvent.VK_ENTER;
		private final int BACK_SPACE_KEY = KeyEvent.VK_BACK_SPACE;
		private final String BACK_SPACE_KEY_BINDING = getKeyBinding(textPane.getInputMap(), "BACK_SPACE");
		private final int TAB_KEY = KeyEvent.VK_TAB;
		private final int UP_KEY = KeyEvent.VK_UP;

		private boolean isKeysDisabled;
		private int minCursorPosition;

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
				disableTerminal();

				try {
					Document doc = textPane.getDocument();
					doc.insertString(doc.getLength(), LINE_SEPARATOR, null);
					textPane.setCaretPosition(doc.getLength());
				} catch (BadLocationException e) {
					UIManager.getLookAndFeel().provideErrorFeedback(textPane);
				}

				String command = extractCommand();
				executeCommand(command);
				// showNewLine();
				showPrompt();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						enableTerminal();
					}
				});
			}
			else if (keyCode == TAB_KEY) {
				disableTerminal();
				// System.out.println("On appuie sur TAB");
				String commandStart = extractCommand();
				List<String> availableCompletion = commandInterpreter.getAvailableCompletion(commandStart);
				String commonCompletion = commandInterpreter.getCommonCompletion(availableCompletion);
				// System.out.println("Available: " + availableCompletion);
				// System.out.println("Common: " + commonCompletion);
				String toAdd = commonCompletion.substring(commandStart.length());
				// System.out.println("Ajouter donc " + toAdd);
				if (toAdd.length() > 0) {
					try {
						Document doc = textPane.getDocument();
						doc.insertString(doc.getLength(), toAdd, null);
						textPane.setCaretPosition(doc.getLength());
					} catch (BadLocationException e) {
						UIManager.getLookAndFeel().provideErrorFeedback(textPane);
					}
				}
				else {
					showNewLine();
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
					try {
						Document doc = textPane.getDocument();
						int i = 0;
						for (String completion : availableCompletion) {
							doc.insertString(doc.getLength(),
									completion + StringUtils.buildWhiteSpaceIndentation(maxLength + 1 - completion.length()), null);
							i++;
							if (i % cols == 0) {
								showNewLine();
							}
						}
						if (i % cols != 0) {
							showNewLine();
						}
						showPrompt();
						doc.insertString(doc.getLength(), commandStart, null);
						textPane.setCaretPosition(doc.getLength());
					} catch (BadLocationException e) {
						UIManager.getLookAndFeel().provideErrorFeedback(textPane);
					}
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						enableTerminal();
					}
				});
			}
			else if (keyCode == UP_KEY) {
				System.out.println("On fait UP");
			}
		}

		@Override
		public void keyReleased(KeyEvent evt) {
			int keyCode = evt.getKeyCode();
			/*if (keyCode == ENTER_KEY) {
				textPane.setCaretPosition(textPane.getCaretPosition() - 1);
				setMinCursorPosition();
			}*/
		}

		private void disableBackspaceKey() {
			isKeysDisabled = true;
			textPane.getInputMap().put(KeyStroke.getKeyStroke("BACK_SPACE"), "none");
		}

		private void enableBackspaceKey() {
			isKeysDisabled = false;
			textPane.getInputMap().put(KeyStroke.getKeyStroke("BACK_SPACE"), BACK_SPACE_KEY_BINDING);
		}

		/*private void setMinCursorPosition() {
			minCursorPosition = textPane.getCaretPosition();
		}*/
	}

	public void enableTerminal() {
		textPane.setEnabled(true);
	}

	public void disableTerminal() {
		textPane.setEnabled(false);
	}

	private void executeCommand(String commandString) {
		try {
			AbstractCommand executeCommand = commandInterpreter.executeCommand(commandString);
			if (executeCommand instanceof ExitDirective) {
				close();
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

}
