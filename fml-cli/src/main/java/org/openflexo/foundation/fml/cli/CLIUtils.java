/*
 * CommandInterpreter.java -  Provide the basic command line interface.
 *
 * Copyright (c) 1996 Chuck McManis, All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies.
 *
 * CHUCK MCMANIS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. CHUCK MCMANIS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package org.openflexo.foundation.fml.cli;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;

/**
 * Utilities for FML command-line interpreter
 */
public class CLIUtils {

	/**
	 * Generic method used to render a FlexoObject in the context of FML command-line interpreter
	 * 
	 * @param object
	 * @return
	 */
	public static String renderObject(FlexoObject object) {
		if (object instanceof VirtualModel) {
			return ((VirtualModel) object).getName() + ".fml";
		}
		else if (object instanceof FlexoConcept) {
			return ((FlexoConcept) object).getDeclaringVirtualModel().getName() + ".fml/" + ((FlexoConcept) object).getName();
		}
		return object.toString();
	}

}
