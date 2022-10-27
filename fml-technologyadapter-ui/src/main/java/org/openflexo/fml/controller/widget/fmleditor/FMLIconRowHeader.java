package org.openflexo.fml.controller.widget.fmleditor;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.FoldingAwareIconRowHeader;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;

public class FMLIconRowHeader extends FoldingAwareIconRowHeader implements MouseMotionListener {

	static final Logger logger = Logger.getLogger(FMLIconRowHeader.class.getPackage().getName());

	public FMLIconRowHeader(FMLRSyntaxTextArea textArea) {
		super(textArea);
		textArea.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (currentlyShownNotice != -1) {
					hideTooltip(currentlyShownNotice);
				}
			}
		});
		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (currentlyShownNotice != -1) {
					hideTooltip(currentlyShownNotice);
				}
			}
		});

	}

	@Override
	protected void paintComponent(Graphics g) {
		try {
			super.paintComponent(g);
		} catch (IndexOutOfBoundsException e) {
			// May happen, proceed silently
			logger.info("Silently handle exception : " + e);
		}
	}

	public FMLRSyntaxTextArea getTextArea() {
		return (FMLRSyntaxTextArea) textArea;
	}

	@Override
	protected void init() {
		super.init();
		addMouseMotionListener(this);
	}

	public FMLEditor getEditor() {
		return getTextArea().getEditor();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		System.out.println("On appuie sur le bouton");

		if (e.getButton() == MouseEvent.BUTTON3) {
			System.out.println("click droit...");
			try {
				int line = viewToModelLine(e.getPoint()) + 1;
				/*if (line>-1) {
					toggleBookmark(line);
				}*/
				System.out.println("Ligne: " + line);
				List<ParserNotice> parserNotices = getParserNotices(line);
				System.out.println("Notices: " + parserNotices);
			} catch (BadLocationException ble) {
				ble.printStackTrace(); // Never happens
			}

		}

		super.mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		System.out.println("On relache le bouton");
		super.mouseReleased(e);
	}

	/**
	 * Returns the line rendered at the specified location.
	 *
	 * @param p
	 *            The location in this row header.
	 * @return The corresponding line in the editor.
	 * @throws BadLocationException
	 *             ble If an error occurs.
	 */
	private int viewToModelLine(Point p) throws BadLocationException {
		int offs = textArea.viewToModel(p);
		return offs > -1 ? textArea.getLineOfOffset(offs) : -1;
	}

	public List<ParserNotice> getParserNotices(int line) {
		List<ParserNotice> returned = new ArrayList<>();
		for (ParserNotice parserNotice : getTextArea().getParserNotices()) {
			if (parserNotice.getLine() == line) {
				returned.add(parserNotice);
			}
		}
		return returned;
	}

	@Override
	public void mouseEntered(MouseEvent e) {

		super.mouseEntered(e);
		// System.out.println("mouseEntered with " + e);
		int focused = getFocusedNoticesLineNumber(e);

		/*System.out.println("------> mouseEntered()");
		System.out.println("focused=" + focused);
		System.out.println("currentlyShownNotice=" + currentlyShownNotice);
		System.out.println("currentlyFocusedNotice=" + currentlyFocusedNotice);*/

		if (focused != currentlyFocusedNotice) {
			if (focused != -1) {
				triggerTooltipAppearing(focused, e);
			}
		}

		currentlyFocusedNotice = focused;

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int focused = getFocusedNoticesLineNumber(e);

		/*System.out.println("------> mouseMoved()");
		System.out.println("focused=" + focused);
		System.out.println("currentlyShownNotice=" + currentlyShownNotice);
		System.out.println("currentlyFocusedNotice=" + currentlyFocusedNotice);*/

		if (currentlyShownNotice != -1) {
			if (focused != currentlyShownNotice) {
				hideTooltip(currentlyShownNotice);
				setCursor(Cursor.getDefaultCursor());
			}
		}
		else {
			if (focused != -1) {
				triggerTooltipAppearing(focused, e);
			}
			else {
				setCursor(Cursor.getDefaultCursor());
			}
		}

		currentlyFocusedNotice = focused;

	}

	@Override
	public void mouseExited(MouseEvent e) {
		int focused = getFocusedNoticesLineNumber(e);

		/*System.out.println("------> mouseExited()");
		System.out.println("focused=" + focused);
		System.out.println("currentlyShownNotice=" + currentlyShownNotice);
		System.out.println("currentlyFocusedNotice=" + currentlyFocusedNotice);*/

		if (focused == -1 && currentlyShownNotice != -1) {
			hideTooltip(currentlyShownNotice);
		}
		setCursor(Cursor.getDefaultCursor());
	}

	private List<ParserNotice> getFocusedNotices(MouseEvent e) {
		try {
			int line = viewToModelLine(e.getPoint()) + 1;
			return getParserNotices(line);
		} catch (BadLocationException ble) {
			ble.printStackTrace(); // Never happens
		}
		return null;
	}

	private int getFocusedNoticesLineNumber(MouseEvent e) {
		try {
			int line = viewToModelLine(e.getPoint()) + 1;
			List<ParserNotice> parserNotices = getParserNotices(line);
			if (parserNotices.size() > 0) {
				return line;
			}
		} catch (BadLocationException ble) {
			ble.printStackTrace(); // Never happens
		}
		return -1;
	}

	private int currentlyFocusedNotice = -1;
	private int currentlyShownNotice = -1;
	private int noticeScheludedForShowing = -1;

	private void triggerTooltipAppearing(int lineNumber, MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		if (lineNumber == noticeScheludedForShowing) {
			return;
		}
		noticeScheludedForShowing = lineNumber;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					noticeScheludedForShowing = -1;
					if (lineNumber == currentlyFocusedNotice) {
						Point point = new Point(e.getPoint());
						SwingUtilities.convertPointToScreen(point, (Component) e.getSource());
						showToolTip(lineNumber, (int) point.getX(), (int) point.getY());
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	private Map<Integer, JToolTip> toolTipMap = new HashMap();
	private Map<Integer, Popup> toolTipContainerMap = new HashMap();

	private void showToolTip(int lineNumber, int x, int y) {
		JToolTip tooltip = toolTipMap.get(lineNumber);

		// System.out.println("showToolTip with " + notice.getMessage());

		currentlyShownNotice = lineNumber;

		if (tooltip == null) {
			tooltip = createToolTip();
			toolTipMap.put(lineNumber, tooltip);
			PopupFactory popupFactory = PopupFactory.getSharedInstance();
			tooltip.setTipText(getTooltipText(lineNumber));
			final Popup tooltipContainer = popupFactory.getPopup(this, tooltip, x, y);
			toolTipContainerMap.put(lineNumber, tooltipContainer);
			tooltipContainer.show();
			tooltip.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					hideTooltip(lineNumber);
				}
			});
		}
	}

	private void hideTooltip(int lineNumber) {
		// JToolTip tooltip = toolTipMap.get(lineNumber);
		Popup tooltipContainer = toolTipContainerMap.get(lineNumber);
		if (tooltipContainer != null) {
			tooltipContainer.hide();
		}
		toolTipMap.remove(lineNumber);
		toolTipContainerMap.remove(lineNumber);
		currentlyShownNotice = -1;
		setCursor(Cursor.getDefaultCursor());
		// Thread.dumpStack();
	}

	private String getTooltipText(int lineNumber) {
		List<ParserNotice> notices = getParserNotices(lineNumber);
		if (notices.size() == 1) {
			return notices.get(0).getMessage();
		}
		else if (notices.size() > 1) {
			StringBuffer sb = new StringBuffer();
			sb.append("<html>");
			sb.append(getEditor().getFlexoLocalizer().localizedForKey("many_marks_on_this_line:"));
			sb.append("<br>");
			for (ParserNotice notice : notices) {
				sb.append("&nbsp; - " + notice.getMessage() + "<br>");
			}
			sb.append("</html>");
			return sb.toString();
		}
		return null;
	}

}
