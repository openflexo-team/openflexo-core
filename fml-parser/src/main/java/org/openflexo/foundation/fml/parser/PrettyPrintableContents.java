/**
 * 
 * Copyright (c) 2019, Openflexo
 * 
 * This file is part of FML-parser, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.parser;

import org.openflexo.foundation.fml.FMLPrettyPrintDelegate.PrettyPrintContext;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.parser.RawSource.RawSourceFragment;

/**
 * Specification of a part of pretty-print for an object of type T
 * 
 * @author sylvain
 *
 * @param <T>
 */
public abstract class PrettyPrintableContents<T extends FMLPrettyPrintable> {

	private final String prelude;
	private final String postlude;
	private final int indentationLevel;

	private RawSourceFragment fragment = null;
	// private RawSourcePosition startPosition = null;
	// private RawSourcePosition endPosition = null;

	public PrettyPrintableContents(String prelude, String postlude, int indentationLevel) {
		super();
		this.prelude = prelude;
		this.postlude = postlude;
		this.indentationLevel = indentationLevel;
	}

	public PrettyPrintableContents(int indentationLevel) {
		super();
		this.prelude = null;
		this.postlude = null;
		this.indentationLevel = indentationLevel;
	}

	public String getPrelude() {
		return prelude;
	}

	public String getPostlude() {
		return postlude;
	}

	public int getIndentationLevel() {
		return indentationLevel;
	}

	/*public RawSourcePosition getStartPosition() {
		if (fragment != null) {
			return fragment.getStartPosition();
		}
		return startPosition;
	}
	
	protected void setStartPosition(RawSourcePosition startPosition) {
		this.startPosition = startPosition;
		fragment = null;
	}
	
	public RawSourcePosition getEndPosition() {
		if (fragment != null) {
			return fragment.getEndPosition();
		}
		return endPosition;
	}
	
	protected void setEndPosition(RawSourcePosition endPosition) {
		this.endPosition = endPosition;
		fragment = null;
	}*/

	/**
	 * Called when a {@link DynamicContents} is registered at a location where no current contents was parsed in initial raw source
	 * 
	 * @param expectedLocation
	 */
	/*protected void setExpectedLocation(RawSourcePosition expectedLocation) {
		setStartPosition(expectedLocation);
		setEndPosition(expectedLocation);
	}*/

	public RawSourceFragment getFragment() {
		/*if (fragment == null && getStartPosition() != null && getEndPosition() != null) {
			fragment = getStartPosition().RawSource.this.makeFragment(getStartPosition(), getEndPosition());
		}*/
		return fragment;
	}

	/**
	 * Called when a {@link DynamicContents} is registered to the place determined with supplied fragment
	 * 
	 * @param fragment
	 */
	protected void setFragment(RawSourceFragment fragment) {
		// setStartPosition(fragment.getStartPosition());
		// setEndPosition(fragment.getEndPosition());
		this.fragment = fragment;
	}

	public abstract String getNormalizedPrettyPrint(PrettyPrintContext context);

	public abstract void updatePrettyPrint(DerivedRawSource derivedRawSource, PrettyPrintContext context);
}
