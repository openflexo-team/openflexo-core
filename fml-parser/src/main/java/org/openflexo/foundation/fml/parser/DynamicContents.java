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

import java.util.function.Supplier;

import org.openflexo.foundation.fml.FMLPrettyPrintDelegate.PrettyPrintContext;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.parser.RawSource.RawSourceFragment;
import org.openflexo.toolbox.StringUtils;

/**
 * Represent a part of information of the underlying model object
 * 
 * @author sylvain
 *
 * @param <T>
 */
public class DynamicContents<T extends FMLPrettyPrintable> extends PrettyPrintableContents<T> {

	private final Supplier<String> stringRepresentationSupplier;

	/**
	 * Build a new {@link DynamicContents}, whose value is intented to replace text determined with supplied fragment
	 * 
	 * @param prelude
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 * @param fragment
	 */
	public DynamicContents(String prelude, Supplier<String> stringRepresentationSupplier, String postlude, RawSourceFragment fragment) {
		super(prelude, postlude, 0);
		this.stringRepresentationSupplier = stringRepresentationSupplier;
		setFragment(fragment);
	}

	@Override
	public String getNormalizedPrettyPrint(PrettyPrintContext context) {
		StringBuffer sb = new StringBuffer();
		String dynamicValue = stringRepresentationSupplier.get();
		if (StringUtils.isNotEmpty(dynamicValue)) {
			if (StringUtils.isNotEmpty(getPrelude())) {
				sb.append(getPrelude());
			}
			sb.append(dynamicValue);
			if (StringUtils.isNotEmpty(getPostlude())) {
				sb.append(getPostlude());
			}
		}
		return sb.toString();
	}

	@Override
	public void updatePrettyPrint(DerivedRawSource derivedRawSource, PrettyPrintContext context) {
		// System.out.println("> Pour DynamicContents, faudrait passer " + getFragment() + " a " + stringRepresentationSupplier.get());
		derivedRawSource.replace(getFragment(), stringRepresentationSupplier.get());
	}

}
