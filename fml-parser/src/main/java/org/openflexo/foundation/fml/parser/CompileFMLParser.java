/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-parser, a component of the software infrastructure 
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

import java.io.File;

import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class CompileFMLParser {

	public static void main(String[] args) {
		File grammar = ((FileResourceImpl) ResourceLocator.locateResource("FML/fml-grammar.sablecc")).getFile();
		System.out.println("file : " + grammar.getAbsolutePath());
		System.out.println("exist=" + grammar.exists());
		File output = grammar.getParentFile().getParentFile().getParentFile().getParentFile();
		output = new File(output, "src/main/java");
		System.out.println("output=" + output);
		// File output = ((FileResourceImpl) ResourceLocator.locateResource("fml-parser")).getFile();
		try {
			// SableCC.processGrammar(grammar, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
