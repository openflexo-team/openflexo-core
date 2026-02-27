/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Pamela-core, a component of the software infrastructure 
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

package org.openflexo.foundation.doc;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.model.StringConverterLibrary.Converter;

public class TextSelectionConverter extends Converter<TextSelection<?, ?>> {

	private static final java.util.logging.Logger logger = org.openflexo.logging.FlexoLogger
			.getLogger(TextSelectionConverter.class.getPackage().getName());

	private FlexoServiceManager serviceManager;

	public TextSelectionConverter() {
		super(TextSelection.class);
	}

	public TextSelectionConverter(FlexoServiceManager serviceManager) {
		this();
		this.serviceManager = serviceManager;
	}

	class ParseTextSelectionPart {
		String elementId;
		int run = -1;
		int character = -1;

		ParseTextSelectionPart(String s) {
			if (s.contains(":")) {
				elementId = s.substring(0, s.indexOf(":"));
				String s2 = s.substring(s.indexOf(":") + 1, s.length());
				if (s2.contains(":")) {
					run = Integer.parseInt(s2.substring(0, s2.indexOf(":")));
					character = Integer.parseInt(s2.substring(s2.indexOf(":") + 1, s2.length()));
				}
				else {
					run = Integer.parseInt(s2);
				}
			}
			else {
				elementId = s;
			}
		}
	}

	@Override
	public TextSelection<?, ?> convertFromString(String value, PamelaModelFactory factory) {

		String start = value.substring(0, value.indexOf("-"));
		String end = value.substring(value.indexOf("-") + 1, value.length());

		ParseTextSelectionPart p1 = new ParseTextSelectionPart(start);
		ParseTextSelectionPart p2 = new ParseTextSelectionPart(end);

		TextSelection<?, ?> returned = factory.newInstance(TextSelection.class);
		returned.setStartElementIdentifier(p1.elementId);
		returned.setStartRunIndex(p1.run);
		returned.setStartCharacterIndex(p1.character);
		returned.setEndElementIdentifier(p2.elementId);
		returned.setEndRunIndex(p2.run);
		returned.setEndCharacterIndex(p2.character);

		return returned;
	}

	@Override
	public String convertToString(TextSelection<?, ?> textSelection) {
		return "\"" + textSelection.toString() + "\"";
	}

}
