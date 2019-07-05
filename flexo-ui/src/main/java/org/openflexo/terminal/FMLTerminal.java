package org.openflexo.terminal;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.InputMap;
import javax.swing.JFrame;
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
import org.openflexo.toolbox.StringUtils;

//TODO: Keep a global StringBuilder to decrease memory footprint

public class FMLTerminal {
	private JFrame frm = new JFrame("FMLTerminal");
	private JTextPane txtArea = new JTextPane();
	private JScrollPane scrollPane = new JScrollPane();
	private final String LINE_SEPARATOR = System.lineSeparator();
	private static final Font DEFAULT_FONT = new Font("Monospaced", Font.PLAIN, 11);

	private FMLCommandInterpreter commandInterpreter;
	private KeyListener keyListener;

	public FMLTerminal(FlexoServiceManager serviceManager, File userDir) {
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.getContentPane().add(scrollPane);
		scrollPane.setViewportView(txtArea);
		txtArea.addKeyListener(keyListener = new KeyListener());
		txtArea.setFont(DEFAULT_FONT);
		disableArrowKeys(txtArea.getInputMap());

		TextPaneOutputStream out = new TextPaneOutputStream(txtArea, Color.BLACK);
		TextPaneOutputStream err = new TextPaneOutputStream(txtArea, Color.RED);

		try {
			commandInterpreter = new FMLCommandInterpreter(serviceManager, out, err, userDir);
			commandInterpreter.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void disableArrowKeys(InputMap inputMap) {
		String[] keystrokeNames = { "UP", "DOWN", "LEFT", "RIGHT", "HOME" };
		for (int i = 0; i < keystrokeNames.length; ++i)
			inputMap.put(KeyStroke.getKeyStroke(keystrokeNames[i]), "none");
	}

	public void open(final int xLocation, final int yLocation, final int width, final int height) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frm.setBounds(xLocation, yLocation, width, height);
				frm.setVisible(true);
				showPrompt();
			}
		});
	}

	public void close() {
		frm.dispose();
	}

	public void clear() {
		txtArea.setText("");
		showPrompt();
	}

	private void showPrompt() {
		try {
			Document doc = txtArea.getDocument();

			SimpleAttributeSet attributes = new SimpleAttributeSet();
			attributes = new SimpleAttributeSet();
			attributes.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);
			attributes.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.FALSE);
			attributes.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.BLUE);

			doc.insertString(doc.getLength(), commandInterpreter.getPrompt(), attributes);
			doc.insertString(doc.getLength(), " > ", null);
			txtArea.setCaretPosition(doc.getLength());
			keyListener.minCursorPosition = txtArea.getCaretPosition();
		} catch (BadLocationException e) {
			UIManager.getLookAndFeel().provideErrorFeedback(txtArea);
		}
	}

	private void showNewLine() {
		// txtArea.setText(txtArea.getText() + LINE_SEPARATOR);
		try {
			Document doc = txtArea.getDocument();
			doc.insertString(doc.getLength(), LINE_SEPARATOR, null);
		} catch (BadLocationException e) {
			UIManager.getLookAndFeel().provideErrorFeedback(txtArea);
		}
	}

	private class KeyListener extends KeyAdapter {
		private final int ENTER_KEY = KeyEvent.VK_ENTER;
		private final int BACK_SPACE_KEY = KeyEvent.VK_BACK_SPACE;
		private final String BACK_SPACE_KEY_BINDING = getKeyBinding(txtArea.getInputMap(), "BACK_SPACE");
		private final int TAB_KEY = KeyEvent.VK_TAB;

		private boolean isKeysDisabled;
		private int minCursorPosition;

		private String getKeyBinding(InputMap inputMap, String name) {
			return (String) inputMap.get(KeyStroke.getKeyStroke(name));
		}

		private int getTerminalWidth() {
			return 40;
		}

		@Override
		public void keyPressed(KeyEvent evt) {
			int keyCode = evt.getKeyCode();
			if (keyCode == BACK_SPACE_KEY) {
				int cursorPosition = txtArea.getCaretPosition();
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
					Document doc = txtArea.getDocument();
					doc.insertString(doc.getLength(), LINE_SEPARATOR, null);
					txtArea.setCaretPosition(doc.getLength());
				} catch (BadLocationException e) {
					UIManager.getLookAndFeel().provideErrorFeedback(txtArea);
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
				System.out.println("On appuie sur TAB");
				String commandStart = extractCommand();
				List<String> availableCompletion = commandInterpreter.getAvailableCompletion(commandStart);
				String commonCompletion = commandInterpreter.getCommonCompletion(availableCompletion);
				System.out.println("Available: " + availableCompletion);
				System.out.println("Common: " + commonCompletion);
				String toAdd = commonCompletion.substring(commandStart.length());
				System.out.println("Ajouter donc " + toAdd);
				if (toAdd.length() > 0) {
					try {
						Document doc = txtArea.getDocument();
						doc.insertString(doc.getLength(), toAdd, null);
						txtArea.setCaretPosition(doc.getLength());
					} catch (BadLocationException e) {
						UIManager.getLookAndFeel().provideErrorFeedback(txtArea);
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
					int cols = getTerminalWidth() / (maxLength + 1);
					if (cols == 0) {
						cols = 1;
					}
					try {
						Document doc = txtArea.getDocument();
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
						txtArea.setCaretPosition(doc.getLength());
					} catch (BadLocationException e) {
						UIManager.getLookAndFeel().provideErrorFeedback(txtArea);
					}
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						enableTerminal();
					}
				});
			}
		}

		@Override
		public void keyReleased(KeyEvent evt) {
			int keyCode = evt.getKeyCode();
			/*if (keyCode == ENTER_KEY) {
				txtArea.setCaretPosition(txtArea.getCaretPosition() - 1);
				setMinCursorPosition();
			}*/
		}

		private void disableBackspaceKey() {
			isKeysDisabled = true;
			txtArea.getInputMap().put(KeyStroke.getKeyStroke("BACK_SPACE"), "none");
		}

		private void enableBackspaceKey() {
			isKeysDisabled = false;
			txtArea.getInputMap().put(KeyStroke.getKeyStroke("BACK_SPACE"), BACK_SPACE_KEY_BINDING);
		}

		/*private void setMinCursorPosition() {
			minCursorPosition = txtArea.getCaretPosition();
		}*/
	}

	public void enableTerminal() {
		txtArea.setEnabled(true);
	}

	public void disableTerminal() {
		txtArea.setEnabled(false);
	}

	private void executeCommand(String commandString) {
		try {
			commandInterpreter.executeCommand(commandString);
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
		String terminalText = txtArea.getText();
		int lastPromptIndex = terminalText.lastIndexOf(commandInterpreter.getPrompt()) + commandInterpreter.getPrompt().length() + 3;
		if (lastPromptIndex < 0 || lastPromptIndex >= terminalText.length())
			return "";
		else
			return terminalText.substring(lastPromptIndex);
	}

}
