package org.openflexo.fml.controller.widget.fmleditor;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.FoldingAwareIconRowHeader;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;

public class FMLIconRowHeader extends FoldingAwareIconRowHeader implements MouseMotionListener {

	public FMLIconRowHeader(FMLRSyntaxTextArea textArea) {
		super(textArea);
	}

	public FMLRSyntaxTextArea getTextArea() {
		return (FMLRSyntaxTextArea) textArea;
	}

	@Override
	protected void init() {
		super.init();
		addMouseMotionListener(this);
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
		ParserNotice focused = getFocusedNotice(e);

		if (focused != currentlyFocusedNotice) {
			if (focused != null) {
				triggerTooltipAppearing(focused, e);
			}
		}

		currentlyFocusedNotice = focused;

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		ParserNotice focused = getFocusedNotice(e);

		if (currentlyShownNotice != null) {
			if (focused != currentlyShownNotice) {
				hideTooltip(currentlyShownNotice);
			}
		}
		else if (focused != currentlyFocusedNotice) {
			if (focused != null) {
				triggerTooltipAppearing(focused, e);
			}
		}

		currentlyFocusedNotice = focused;

	}

	@Override
	public void mouseExited(MouseEvent e) {
		super.mouseExited(e);
		// System.out.println("mouseExited with " + e);
		if (currentlyShownNotice != null) {
			hideTooltip(currentlyShownNotice);
		}
	}

	private ParserNotice getFocusedNotice(MouseEvent e) {
		try {
			int line = viewToModelLine(e.getPoint()) + 1;
			List<ParserNotice> parserNotices = getParserNotices(line);
			if (parserNotices.size() > 0) {
				return parserNotices.get(0);
			}
		} catch (BadLocationException ble) {
			ble.printStackTrace(); // Never happens
		}
		return null;
	}

	private ParserNotice currentlyFocusedNotice = null;
	private ParserNotice currentlyShownNotice = null;

	private void triggerTooltipAppearing(ParserNotice notice, MouseEvent e) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					if (notice == currentlyFocusedNotice) {
						Point point = new Point(e.getPoint());
						SwingUtilities.convertPointToScreen(point, (Component) e.getSource());
						showToolTip(notice, (int) point.getX(), (int) point.getY());
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

	private Map<ParserNotice, JToolTip> toolTipMap = new HashMap();
	private Map<ParserNotice, Popup> toolTipContainerMap = new HashMap();

	private void showToolTip(ParserNotice notice, int x, int y) {
		JToolTip tooltip = toolTipMap.get(notice);

		// System.out.println("showToolTip with " + notice.getMessage());

		currentlyShownNotice = notice;

		if (tooltip == null) {
			tooltip = createToolTip();
			toolTipMap.put(notice, tooltip);
			PopupFactory popupFactory = PopupFactory.getSharedInstance();
			tooltip.setTipText(notice.getToolTipText());
			final Popup tooltipContainer = popupFactory.getPopup(this, tooltip, x, y);
			toolTipContainerMap.put(notice, tooltipContainer);
			tooltipContainer.show();
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			tooltip.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					hideTooltip(notice);
				}
			});
		}
	}

	private void hideTooltip(ParserNotice notice) {
		JToolTip tooltip = toolTipMap.get(notice);
		Popup tooltipContainer = toolTipContainerMap.get(notice);
		tooltipContainer.hide();
		toolTipMap.remove(notice);
		toolTipContainerMap.remove(notice);
		currentlyShownNotice = null;
		setCursor(Cursor.getDefaultCursor());
	}

}
