/*
 * FMLCommandInterpreter.java -  Provide the basic command line interface.
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

package org.openflexo.terminal;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.openflexo.connie.expr.ExpressionEvaluator;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.cli.AbstractCommandInterpreter;
import org.openflexo.foundation.fml.expr.FMLExpressionEvaluator;

/**
 * FML command-line interpreter<br>
 * 
 * An interpreter must be instantiated using a {@link FlexoServiceManager}
 * 
 */
public class FMLCommandInterpreter extends AbstractCommandInterpreter {

	private FMLTerminal terminal;

	/**
	 * Create a new command interpreter attached to the passed in streams.
	 * 
	 * @throws IOException
	 */
	public FMLCommandInterpreter(FlexoServiceManager serviceManager, OutputStream out, OutputStream err, File workingDirectory,
			FMLTerminal terminal) throws IOException {

		super(serviceManager, out, err, workingDirectory);
		this.terminal = terminal;
	}

	@Override
	public void displayHistory() {
		terminal.displayHistory();
	}

	@Override
	public ExpressionEvaluator getEvaluator() {
		return new FMLExpressionEvaluator(this);
	}

}
