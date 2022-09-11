/**
 * 
 * Copyright (c) 2014-2022, Openflexo
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
package org.openflexo.foundation.fml;

import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.pamela.validation.Validable;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * A {@link ValidationError} raised during semantics analysis phase of FML parsing
 * 
 * @author sylvain
 *
 * @param <R>
 * @param <V>
 */
public class SemanticAnalysisIssue<R extends ValidationRule<R, V>, V extends Validable> extends ValidationError<R, V> {
	private String message;
	private int line = -1;
	private int offset = -1;
	private int length = -1;

	public SemanticAnalysisIssue(V modelObject, String errorMessage, RawSourceFragment fragment) {
		super(null, modelObject, errorMessage);
		this.message = "" + modelObject + " : " + errorMessage;
		if (fragment != null) {
			this.line = fragment.getStartPosition().getLine();
			this.offset = fragment.getStartPosition().getOffset();
			this.length = fragment.getLength();
		}
	}

	@Override
	public String getMessage() {
		return message;
	}

	public int getLine() {
		return line;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}
}
