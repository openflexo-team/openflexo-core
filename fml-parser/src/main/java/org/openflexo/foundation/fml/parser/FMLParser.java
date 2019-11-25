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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.lexer.Lexer;
import org.openflexo.foundation.fml.parser.lexer.LexerException;
import org.openflexo.foundation.fml.parser.node.Start;
import org.openflexo.foundation.fml.parser.parser.Parser;
import org.openflexo.foundation.fml.parser.parser.ParserException;
import org.openflexo.p2pp.RawSource;

/**
 * This class provides the parsing service for FML.<br>
 * This includes syntactic and semantics analyzer.<br>
 * 
 * SableCC is used to generate the grammar located in src/main/resources<br>
 *
 * Compilation of the grammar is performed by gradle task. The grammar is located in src/main/resources/FML/fml-grammar.sablecc<br>
 * Generated code is located in org.openflexo.foundation.fml.parser.analysis, org.openflexo.foundation.fml.parser.lexer,
 * org.openflexo.foundation.fml.parser.node, org.openflexo.foundation.fml.parser.parser
 * 
 * @author sylvain
 */
public class FMLParser {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FMLParser.class.getPackage().getName());

	/**
	 * This is the method to invoke to perform a parsing.<br>
	 * Syntactic and semantics analyzer are performed and returned value is a {@link FMLCompilationUnit}
	 * 
	 * @param data
	 *            data to parse
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public FMLCompilationUnit parse(String data, FMLModelFactory modelFactory/*, EntryPointKind entryPointKind*/)
			throws ParseException, IOException {
		return parse(new StringReader(data), new StringReader(data), modelFactory/*, entryPointKind*/);
	}

	/**
	 * This is the method to invoke to perform a parsing.<br>
	 * Syntactic and semantics analyzer are performed and returned value is a {@link FMLCompilationUnit}
	 * 
	 * @param data
	 *            data to parse
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	// public static FMLCompilationUnit parse(String data, FMLModelFactory modelFactory) throws ParseException, IOException {
	// return parse(new StringReader(data), new StringReader(data), modelFactory/*, EntryPointKind.CompilationUnit*/);
	// }

	/**
	 * This is the method to invoke to perform a parsing.<br>
	 * Syntactic and semantics analyzer are performed and returned value is a {@link FMLCompilationUnit}
	 * 
	 * @param inputStream
	 *            source stream
	 * @return
	 * @throws ParseException
	 *             if parsing expression lead to an error
	 */
	// public static FMLCompilationUnit parse(InputStream inputStream, FMLModelFactory modelFactory/*, EntryPointKind entryPointKind*/)
	// throws ParseException, IOException {
	//
	// byte[] buf = IOUtils.toByteArray(inputStream);
	// InputStream inputStream1 = new ByteArrayInputStream(buf);
	// InputStream inputStream2 = new ByteArrayInputStream(buf);
	//
	// return parse(new InputStreamReader(inputStream1), new InputStreamReader(inputStream2), modelFactory/*, entryPointKind*/);
	// }

	/**
	 * This is the method to invoke to perform a parsing.<br>
	 * Syntactic and semantics analyzer are performed and returned value is a {@link FMLCompilationUnit}
	 * 
	 * @param inputStream
	 *            source stream
	 * @return
	 * @throws ParseException
	 *             if parsing expression lead to an error
	 */
	public FMLCompilationUnit parse(InputStream inputStream, FMLModelFactory modelFactory) throws ParseException, IOException {

		// InputStream rawSourceInputStream = IOUtils.toBufferedInputStream(inputStream);
		// inputStream.reset();

		byte[] buf = IOUtils.toByteArray(inputStream);
		InputStream inputStream1 = new ByteArrayInputStream(buf);
		InputStream inputStream2 = new ByteArrayInputStream(buf);

		return parse(new InputStreamReader(inputStream1), new InputStreamReader(inputStream2),
				modelFactory/*,
							EntryPointKind.CompilationUnit*/);
	}

	/**
	 * This is the method to invoke to perform a parsing.<br>
	 * Syntactic and semantics analyzer are performed and returned value is a {@link FMLCompilationUnit}
	 * 
	 * @param inputStream
	 *            source stream
	 * @return
	 * @throws ParseException
	 *             if parsing expression lead to an error
	 */
	public FMLCompilationUnit parse(File file, FMLModelFactory modelFactory) throws ParseException, IOException {

		return parse(new FileInputStream(file), modelFactory);
	}

	private static FMLCompilationUnit parse(Reader reader, Reader rawSourceReader,
			FMLModelFactory modelFactory/*,
										EntryPointKind entryPointKind*/) throws ParseException, IOException {
		try {
			// System.out.println("Parsing: " + anExpression);

			RawSource rawSource = readRawSource(rawSourceReader);

			// Create a Parser instance.
			Parser p = new Parser(new Lexer(new PushbackReader(reader)));
			// Parser p = new Parser(new CustomLexer(new PushbackReader(reader), entryPointKind));

			// Parse the input.
			Start tree;
			tree = p.parse();

			// Apply the semantics analyzer.
			MainSemanticsAnalyzer t = new MainSemanticsAnalyzer(modelFactory, tree, rawSource);
			// tree.apply(t);

			return t.getCompilationUnit();
		} catch (ParserException e) {
			e.printStackTrace();
			throw new ParseException(e.getMessage() + " while parsing " + reader);
		} catch (LexerException e) {
			throw new ParseException(e.getMessage() + " while parsing " + reader);
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

	/**
	 * 
	 * 
	 * 
	 * @param inputFile
	 * @param modelFactory
	 * @return
	 * @throws ParseException
	 */
	/*public static FMLCompilationUnit parse(File inputFile, FMLModelFactory modelFactory) throws ParseException {
	
		// TODO: handle close of all streams !!!!
	
		try (FileReader in = new FileReader(inputFile)) {
			System.out.println("Parsing: " + inputFile);
	
			// Create a Parser instance.
			Parser p = new Parser(new Lexer(new PushbackReader(new BufferedReader(in), 1024)));
	
			// Parse the input.
			Start tree = p.parse();
	
			// Apply the semantics analyzer.
			FMLSemanticsAnalyzer t = new FMLSemanticsAnalyzer(modelFactory, tree, readRawSource(new FileInputStream(inputFile)));
	
			return t.getCompilationUnit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ParseException(e.getMessage() + " while parsing " + inputFile);
		}
	}*/

	/**
	 * Read raw source of the file
	 * 
	 * @param ioDelegate
	 * @throws IOException
	 */
	/*private static RawSource readRawSource(InputStream inputStream) throws IOException {
		return new RawSource(inputStream);
	}*/

	public void initPrettyPrint(FMLCompilationUnit fmlCompilationUnit) {
		// System.out.println("fmlCompilationUnit=" + fmlCompilationUnit);
		MainSemanticsAnalyzer semanticsAnalyzer = new MainSemanticsAnalyzer(fmlCompilationUnit.getFMLModelFactory(), null, null);
		FMLCompilationUnitNode fmlCompilationUnitNode = new FMLCompilationUnitNode(fmlCompilationUnit, semanticsAnalyzer);
		// fmlCompilationUnitNode.finalizeDeserialization();
	}

}
