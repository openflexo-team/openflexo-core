/**
 * 
 */
package org.openflexo.components.doc;

import java.util.LinkedList;

import org.openflexo.foundation.doc.FlexoDocElement;
import org.openflexo.foundation.doc.FlexoDocParagraph;
import org.openflexo.foundation.doc.FlexoDocRun;
import org.openflexo.foundation.doc.FlexoDocTable;
import org.openflexo.foundation.doc.FlexoDocTableCell;
import org.openflexo.foundation.doc.FlexoDocTableRow;
import org.openflexo.foundation.doc.FlexoDocument;
import org.openflexo.foundation.doc.FlexoDrawingRun;
import org.openflexo.foundation.doc.FlexoTextRun;
import org.openflexo.foundation.doc.Property;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * @author Bruno Quercia
 *
 */
public class Translator<D extends FlexoDocument<D, TA>, TA extends TechnologyAdapter> {

	private LinkedList<Rule> rules;

	/**
	 * 
	 */
	public Translator() {
		this.rules = new LinkedList<>();
	}

	public void addRule(Rule r) {
		this.rules.add(r);
	}

	public String generateHTML(FlexoDocument<D, TA> document) {
		String result = "";
		for (FlexoDocElement<D, TA> e : document.getElements()) {
			if (e instanceof FlexoDocParagraph) {
				result += "<p>";
				for (FlexoDocRun<D, TA> r : ((FlexoDocParagraph<D, TA>) e).getRuns()) {
					result += generateHTML(r);
				}
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
		String before = "";
		String after = "";
		String content = "";
		if (r.getStyle() != null) {
			for (Property p : r.getStyle().getProperties()) {
				if (!p.hasPossibleValues()) {
					Rule rule = new Rule(p);
					before += rule.getBefore();
					after = rule.getAfter() + after;
				}
				else if (rules.contains(new Rule(p, "", ""))) {
					Rule rule = rules.get(rules.indexOf(new Rule(p, "", "")));
					before += rule.getBefore();
					after = rule.getAfter() + after;
				}
				else
					System.out.println("property not found");
			}
		}
		if (r instanceof FlexoTextRun) {
			content = ((FlexoTextRun<D, TA>) r).getText();
		}
		if (r instanceof FlexoDrawingRun) {
			content = "<img src='" + ((FlexoDrawingRun) r).getImageName() + "'/>";
		}
		return before + content + after;

	}

}
