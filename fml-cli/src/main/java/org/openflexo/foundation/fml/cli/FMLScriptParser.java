/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.cli;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.parser.lexer.CustomLexer;
import org.openflexo.foundation.fml.parser.lexer.CustomLexer.EntryPointKind;
import org.openflexo.foundation.fml.parser.lexer.LexerException;
import org.openflexo.foundation.fml.parser.node.Start;
import org.openflexo.foundation.fml.parser.parser.Parser;
import org.openflexo.foundation.fml.parser.parser.ParserException;
import org.openflexo.p2pp.RawSource;
import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * This class provides the parsing service for FML scripts. This includes syntactic and semantics analyzer.<br>
 * 
 * SableCC is used to generate the grammar located in fml-parser.<br>
 * 
 * @author sylvain
 */
public class FMLScriptParser {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FMLScriptParser.class.getPackage().getName());

	/**
	 * This is the method to invoke to perform a parsing.<br>
	 * Syntactic and semantics analyzer are performed and returned value is a {@link FMLScript}
	 * 
	 * @param inputStream
	 *            source stream
	 * @return
	 * @throws ParseException
	 *             if parsing expression lead to an error
	 * @throws ModelDefinitionException
	 */
	public FMLScript parse(InputStream inputStream, FMLModelFactory modelFactory, AbstractCommandInterpreter commandInterpreter)
			throws ParseException, IOException, ModelDefinitionException {

		// InputStream rawSourceInputStream = IOUtils.toBufferedInputStream(inputStream);
		// inputStream.reset();

		byte[] buf = IOUtils.toByteArray(inputStream);
		InputStream inputStream1 = new ByteArrayInputStream(buf);
		InputStream inputStream2 = new ByteArrayInputStream(buf);

		return parse(new InputStreamReader(inputStream1), new InputStreamReader(inputStream2), modelFactory, commandInterpreter);
	}

	public static FMLScript parse(Reader reader, Reader rawSourceReader, FMLModelFactory modelFactory,
			AbstractCommandInterpreter commandInterpreter) throws ParseException, IOException, ModelDefinitionException {
		try {
			// System.out.println("Parsing: " + anExpression);

			RawSource rawSource = readRawSource(rawSourceReader);

			// Create a Parser instance.
			Parser p = new Parser(new CustomLexer(new PushbackReader(reader), EntryPointKind.Script));

			// Parse the input.
			Start tree = p.parse();

			// Apply the semantics analyzer.
			if (commandInterpreter != null) {
				ScriptSemanticsAnalyzer t = new ScriptSemanticsAnalyzer(commandInterpreter, tree, rawSource);
				tree.apply(t);
				return t.getScript();
			}
			else {
				return null;
			}

		} catch (ParserException e) {
			e.printStackTrace();
			logger.info("ParserException token:" + e.getToken() + " line:" + e.getToken().getLine() + " length:"
					+ e.getToken().getText().length());
			throw new ParseException(e.getMessage(), e.getToken().getLine(), e.getToken().getPos(), e.getToken().getText().length());
		} catch (LexerException e) {
			throw new ParseException(e.getMessage(), e.getToken().getLine(), e.getToken().getPos(), e.getToken().getText().length());
		}
	}

	/**
	 * Read raw source of the file
	 * 
	 * @param ioDelegate
	 * @throws IOException
	 */
	private static RawSource readRawSource(Reader reader) throws IOException {
		return new RawSource(reader);
	}

}
