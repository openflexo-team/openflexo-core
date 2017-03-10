/**
 * 
 */
package org.openflexo.components.doc;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.openflexo.foundation.doc.FlexoDocElement;
import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocRun;
import org.openflexo.foundation.doc.FlexoDocStyle;
import org.openflexo.foundation.doc.FlexoDocTable;
import org.openflexo.foundation.doc.FlexoDocTableCell;
import org.openflexo.foundation.doc.FlexoDocTableRow;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoDrawingRun;
import org.openflexo.foundation.doc.FlexoTextRun;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * @author Bruno Quercia
 *
 */
public class Translator<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> {

	/**
	 * 
	 */
	public Translator() {
	}

	// <span style='label:value'>
	// </span>

	// <span style="font-family: arial, geneva, helvetica, helv, sans-serif;">
	// <span style="font-family: courier">
	// <span style="color: #d9cc00">yellow</span>
	// <span style="font-weight: bold;">
	// <span style="font-style:italic;">
	// <span style="text-decoration: underline;">Name</span>
	// <span style="font-size: 1em;">this font is 1em</span>
	// <span style="font-size: .75em;">this font is .75em</span>
	// <span style="font-size: 1.25em;">this font is 1.25em</span>

	class Spans {
		String before;
		String after;
	}

	private Spans generateSpan(FlexoDocStyle<D, TA> style) {
		Map<String, String> propertiesValues = new HashMap<>();
		if (style.getFont() != null) {
			propertiesValues.put("font-family", style.getFont().getFontName());
		}
		if (style.getFontSize() != null) {
			double em = style.getFontSize() / 24.0;

			DecimalFormat df = new DecimalFormat("#.##em");
			String value = df.format(em);
			propertiesValues.put("font-size", value);
		}
		if (style.getFontColor() != null) {
			int r = style.getFontColor().getRed();
			int g = style.getFontColor().getGreen();
			int b = style.getFontColor().getBlue();
			String rH = (r < 16 ? "0" : "") + Integer.toHexString(r);
			String gH = (g < 16 ? "0" : "") + Integer.toHexString(g);
			String bH = (b < 16 ? "0" : "") + Integer.toHexString(b);
			propertiesValues.put("color", "#" + rH + gH + bH);
		}
		if (style.getBold() != null && style.getBold()) {
			propertiesValues.put("font-weight", "bold");
		}
		if (style.getItalic() != null && style.getItalic()) {
			propertiesValues.put("font-style", "italic");
		}
		if (style.getUnderline() != null && style.getUnderline()) {
			propertiesValues.put("text-decoration", "underline");
		}

		if (propertiesValues.size() > 0) {
			StringBuffer sbBefore = new StringBuffer();
			StringBuffer sbAfter = new StringBuffer();
			for (String key : propertiesValues.keySet()) {
				sbBefore.append("<span style='" + key + ":" + propertiesValues.get(key) + "'>");
				sbAfter.append("</span>");
			}
			Spans returned = new Spans();
			returned.before = sbBefore.toString();
			returned.after = sbAfter.toString();
			return returned;
		}

		return null;

	}

	public String generateHTML(FlexoDocument<D, TA> document) {
		String result = "";
		System.out.println("Je genere le HTML de " + document);
		for (FlexoDocElement<D, TA> e : document.getElements()) {
			// System.out.println("element: " + e);
			if (e instanceof FlexoDocParagraph) {
				Spans spans = null;
				if (((FlexoDocParagraph) e).getNamedStyle() != null) {
					spans = generateSpan(((FlexoDocParagraph) e).getNamedStyle());
				}
				result += "<p>";
				result += (spans != null ? spans.before : "");
				for (FlexoDocRun<D, TA> r : ((FlexoDocParagraph<D, TA>) e).getRuns()) {
					result += generateHTML(r);
				}
				result += (spans != null ? spans.after : "");
				result += "</p>";
			}
			else if (e instanceof FlexoDocTable) {
				FlexoDocTable<D, TA> table = (FlexoDocTable<D, TA>) e;
				result += "<table style='border-spacing: -0px;'>";
				for (FlexoDocTableRow<D, TA> r : table.getTableRows()) {
					result += "<tr>";
					for (FlexoDocTableCell<D, TA> c : r.getTableCells()) {
						result += "<td style='border:1px solid black;' colspan='" + c.getColSpan() + "' rowspan='" + c.getRowSpan() + "'>";
						for (FlexoDocParagraph<D, TA> p : c.getParagraphs()) {
							result += "<p>";
							for (FlexoDocRun<D, TA> run : p.getRuns()) {
								result += this.generateHTML(run);
							}
							result += "</p>";
						}
						result += "</td>";
					}
					result += "</tr>";
				}
				result += "</table>";
			}
		}
		return result;
	}

	private String generateHTML(FlexoDocRun<D, TA> r) {

		Spans spans = null;
		if (r.getStyle() != null) {
			spans = generateSpan(r.getStyle());
			/*if (r instanceof FlexoTextRun) {
				System.out.println("Run " + ((FlexoTextRun) r).getText() + " font-size=" + r.getStyle().getFontSize());
			}*/
		}
		String content = null;
		if (r instanceof FlexoTextRun) {
			content = ((FlexoTextRun<D, TA>) r).getText();
		}
		if (r instanceof FlexoDrawingRun) {
			content = "<img src='" + ((FlexoDrawingRun) r).getImageName() + "'/>";
		}
		return (spans != null ? spans.before : "") + content + (spans != null ? spans.after : "");

	}

}
