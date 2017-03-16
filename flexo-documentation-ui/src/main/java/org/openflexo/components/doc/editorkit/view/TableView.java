package org.openflexo.components.doc.editorkit.view;

import javax.swing.SizeRequirements;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;

import org.openflexo.components.doc.editorkit.FlexoStyledDocument.TableElement;

/**
 * Represents view for a table element.
 *
 * @author Stanislav Lapitsky
 */
public class TableView extends BoxView {

	/**
	 * Constructs new view instance.
	 *
	 * @param elem
	 *            The parent table element.
	 * @param axis
	 *            either View.X_AXIS or View.Y_AXIS
	 */
	public TableView(Element elem, int axis) {
		super(elem, axis);
	}

	/**
	 * Constructs new view instance.
	 *
	 * @param elem
	 *            The parent cell element.
	 */
	public TableView(Element elem) {
		super(elem, View.Y_AXIS);
	}

	/**
	 * Determines base line requirement along axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @param r
	 *            Size requirements
	 */
	@Override
	protected SizeRequirements baselineRequirements(int axis, SizeRequirements r) {
		SizeRequirements sr = super.baselineRequirements(axis, r);
		TableElement table = (TableElement) getElement();
		if (axis == View.X_AXIS) {
			int align = StyleConstants.getAlignment(table.getAttributes());
			switch (align) {
				case StyleConstants.ALIGN_LEFT:
					sr.alignment = 0;
					break;
				case StyleConstants.ALIGN_RIGHT:
					sr.alignment = 1;
					break;
				default:
					sr.alignment = 0.5f;
			}
		}
		return sr;
	}

	/**
	 * Determines major requirement along axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @param r
	 *            Size requirements
	 */
	@Override
	protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
		SizeRequirements sr = super.calculateMajorAxisRequirements(axis, r);
		TableElement table = (TableElement) getElement();
		if (axis == View.X_AXIS) {
			int align = StyleConstants.getAlignment(table.getAttributes());
			switch (align) {
				case StyleConstants.ALIGN_LEFT:
					sr.alignment = 0;
					break;
				case StyleConstants.ALIGN_RIGHT:
					sr.alignment = 1;
					break;
				default:
					sr.alignment = 0.5f;
			}
		}
		return sr;
	}

	/**
	 * Determines minor requirement along axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @param r
	 *            Size requirements
	 */
	@Override
	protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
		SizeRequirements sr = super.calculateMinorAxisRequirements(axis, r);
		TableElement table = (TableElement) getElement();
		if (axis == View.X_AXIS) {
			int align = StyleConstants.getAlignment(table.getAttributes());
			switch (align) {
				case StyleConstants.ALIGN_LEFT:
					sr.alignment = 0;
					break;
				case StyleConstants.ALIGN_RIGHT:
					sr.alignment = 1;
					break;
				default:
					sr.alignment = 0.5f;
			}
		}
		return sr;
	}

	/**
	 * Determines the preferred span for this view along an axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @returns the span the view would like to be rendered into >= 0. Typically the view is told to render into the span that is returned,
	 *          although there is no guarantee. The parent may choose to resize or break the view.
	 */
	@Override
	public float getPreferredSpan(int axis) {
		float span = 0;
		span = super.getPreferredSpan(axis);
		TableElement table = (TableElement) getElement();
		if (axis == View.X_AXIS) {
			span = table.getWidth();
		}
		return span;
	}

	/**
	 * Determines the minimum span for this view along an axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @returns the span the view would like to be rendered into >= 0. Typically the view is told to render into the span that is returned,
	 *          although there is no guarantee. The parent may choose to resize or break the view.
	 */
	@Override
	public float getMinimumSpan(int axis) {
		float span = 0;
		span = super.getMinimumSpan(axis);
		TableElement table = (TableElement) getElement();
		if (axis == View.X_AXIS) {
			span = table.getWidth();
		}
		return span;
	}

	/**
	 * Determines the maximum span for this view along an axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @returns the span the view would like to be rendered into >= 0. Typically the view is told to render into the span that is returned,
	 *          although there is no guarantee. The parent may choose to resize or break the view.
	 */
	@Override
	public float getMaximumSpan(int axis) {
		float span = 0;
		span = super.getMaximumSpan(axis);
		TableElement table = (TableElement) getElement();
		if (axis == View.X_AXIS) {
			span = table.getWidth();
		}
		return span;
	}

	/**
	 * Determines the desired alignment for this view along an axis. This is implemented to give the alignment to the center of the first
	 * row along the y axis, and the default along the x axis.
	 *
	 * @param axis
	 *            may be either View.X_AXIS or View.Y_AXIS
	 * @returns the desired alignment. This should be a value between 0.0 and 1.0 inclusive, where 0 indicates alignment at the origin and
	 *          1.0 indicates alignment to the full span away from the origin. An alignment of 0.5 would be the center of the view.
	 */
	@Override
	public float getAlignment(int axis) {
		if (axis == View.X_AXIS) {
			int align = StyleConstants.getAlignment(getElement().getAttributes());
			float a;
			switch (align) {
				case StyleConstants.ALIGN_LEFT:
					a = 0;
					break;
				case StyleConstants.ALIGN_RIGHT:
					a = 1;
					break;
				default:
					a = 0.5f;
			}
			return a;
		}
		else {
			return super.getAlignment(axis);
		}
	}
}
