/**
 * 
 * Copyright (c) 2026, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */


package org.openflexo.foundation.fml.rm;

/**
 * Scope of an update of a FML compilation unit from a text that was parsed again (the FML editor does it at each parse).<br>
 * 
 * Parsing a text again and merging it with <code>updateWith()</code> sets, on every object, values that are not editions: a new
 * pretty-print delegate, new IDs, types translated to the typing space, even when PAMELA finds the same value (it calls
 * <code>setModified(true)</code> after any overridden persistent setter). Those cannot be filtered setter by setter: the resource of a
 * FML opened in the editor, and not edited, was marked modified (87 of the 127 FML of the test resource center).<br>
 * 
 * The text is the criterion: when it is the same as the one of the previous parse (or the one the editor was opened on), the update is
 * not an edition, and a resource that was not modified before is left unmodified when the scope is closed. Any difference in the text,
 * a comment or a blank included, is an edition (it is saved with the text), so the resource stays modified.
 * 
 * <pre>
 * try (TextUpdate update = new TextUpdate(resource, text.equals(lastParsedText))) {
 * 	compilationUnit.updateWith(parsedCompilationUnit);
 * 	...
 * }
 * </pre>
 * 
 * @author sylvain
 */
public class TextUpdate implements AutoCloseable {

	private final CompilationUnitResource resource;
	private final boolean textUnchanged;
	private final boolean wasModified;

	/**
	 * @param resource
	 *            the resource being updated
	 * @param textUnchanged
	 *            true when the text is the same as the one the compilation unit was last updated with
	 */
	public TextUpdate(CompilationUnitResource resource, boolean textUnchanged) {
		this.resource = resource;
		this.textUnchanged = textUnchanged;
		this.wasModified = resource.isModified();
	}

	/**
	 * Leaves the resource unmodified when the update was not an edition: unchanged text, and the resource was not modified before
	 */
	@Override
	public void close() {
		if (textUnchanged && !wasModified && resource.isLoaded() && resource.isModified()) {
			resource.getLoadedResourceData().clearIsModified();
		}
	}
}
