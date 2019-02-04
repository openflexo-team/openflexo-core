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
import org.openflexo.toolbox.StringUtils;

/**
 * A static contents (a keyword for example)
 * 
 * @author sylvain
 *
 * @param <T>
 */
public class StaticContents<T extends FMLPrettyPrintable> extends PrettyPrintableContents<T> {

	final String staticContents;

	public StaticContents(String staticContents, RawSourceFragment fragment) {
		super(0);
		this.staticContents = staticContents;
	}

	public StaticContents(String prelude, String staticContents, RawSourceFragment fragment) {
		super(prelude, null, 0);
		this.staticContents = staticContents;
	}

	public StaticContents(String prelude, String staticContents, String postlude, RawSourceFragment fragment) {
		super(prelude, postlude, 0);
		this.staticContents = staticContents;
	}

	public String getStaticContents() {
		return staticContents;
	}

	@Override
	public String getNormalizedPrettyPrint(PrettyPrintContext context) {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isNotEmpty(getPrelude())) {
			sb.append(getPrelude());
		}
		if (StringUtils.isNotEmpty(getStaticContents())) {
			sb.append(getStaticContents());
		}
		if (StringUtils.isNotEmpty(getPostlude())) {
			sb.append(getPostlude());
		}
		return sb.toString();
	}

	@Override
	public void updatePrettyPrint(DerivedRawSource derivedRawSource, PrettyPrintContext context) {
		System.out.println("> Rien a faire pour staticContents=[" + getStaticContents() + "]");
	}

}
