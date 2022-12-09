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
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.lexer.CustomLexer;
import org.openflexo.foundation.fml.parser.lexer.CustomLexer.EntryPointKind;
import org.openflexo.foundation.fml.parser.lexer.LexerException;
import org.openflexo.foundation.fml.parser.node.Start;
import org.openflexo.foundation.fml.parser.parser.Parser;
import org.openflexo.foundation.fml.parser.parser.ParserException;
import org.openflexo.foundation.fml.rm.CompilationUnitResource.VirtualModelInfo;
import org.openflexo.foundation.technologyadapter.ModelSlot;
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
public class FMLCompilationUnitParser {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FMLCompilationUnitParser.class.getPackage().getName());

	private FMLCompilationUnitSemanticsAnalyzer semanticsAnalyzer;

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
	public FMLCompilationUnit parse(String data, FMLModelFactory modelFactory,
			Function<List<Class<? extends ModelSlot<?>>>, FMLModelFactory> modelFactoryUpdater, boolean finalizeDeserialization)
			throws ParseException, IOException {
		return parse(new StringReader(data), new StringReader(data), modelFactory, modelFactoryUpdater, finalizeDeserialization);
	}

	/**
	 * This is the method to invoke to perform a parsing.<br>
	 * Syntactic and semantics analyzer are performed and returned value is a {@link FMLCompilationUnit}
	 * 
	 * @param inputStream
	 *            {@link InputStream} source of data to parse
	 * @param modelFactory
	 *            the {@link FMLModelFactory} to be used to build {@link FMLCompilationUnit}
	 * @param modelFactoryUpdater
	 *            a function returning a {@link FMLModelFactory} given a {@link List} of {@link ModelSlot} classes
	 * @param finalizeDeserialization
	 *            a boolean indicating if deserialization finalizing should be performed now, or will be done in a future second pass
	 * @return the newly created {@link FMLCompilationUnit}
	 * @throws ParseException
	 *             if parsing expression cannot be parsed
	 * @throws IOException
	 *             if an IOException occurs during parsing
	 */
	public FMLCompilationUnit parse(InputStream inputStream, FMLModelFactory modelFactory,
			Function<List<Class<? extends ModelSlot<?>>>, FMLModelFactory> modelFactoryUpdater, boolean finalizeDeserialization)
			throws ParseException, IOException {

		// InputStream rawSourceInputStream = IOUtils.toBufferedInputStream(inputStream);
		// inputStream.reset();

		byte[] buf = IOUtils.toByteArray(inputStream);
		InputStream inputStream1 = new ByteArrayInputStream(buf);
		InputStream inputStream2 = new ByteArrayInputStream(buf);

		try {
			return parse(new InputStreamReader(inputStream1), new InputStreamReader(inputStream2), modelFactory, modelFactoryUpdater,
					finalizeDeserialization);
		} finally {
			inputStream1.close();
			inputStream2.close();
		}
	}

	/**
	 * This is the method to invoke to perform a parsing.<br>
	 * Syntactic and semantics analyzer are performed and returned value is a {@link FMLCompilationUnit}
	 * 
	 * @param file
	 *            source {@link File} of data to parse
	 * @param modelFactory
	 *            the {@link FMLModelFactory} to be used to build {@link FMLCompilationUnit}
	 * @param modelFactoryUpdater
	 *            a function returning a {@link FMLModelFactory} given a {@link List} of {@link ModelSlot} classes
	 * @param finalizeDeserialization
	 *            a boolean indicating if deserialization finalizing should be performed now, or will be done in a future second pass
	 * @return the newly created {@link FMLCompilationUnit}
	 * @throws ParseException
	 *             if parsing expression cannot be parsed
	 * @throws IOException
	 *             if an IOException occurs during parsing
	 */
	public FMLCompilationUnit parse(File file, FMLModelFactory modelFactory,
			Function<List<Class<? extends ModelSlot<?>>>, FMLModelFactory> modelFactoryUpdater, boolean finalizeDeserialization)
			throws ParseException, IOException {

		return parse(new FileInputStream(file), modelFactory, modelFactoryUpdater, finalizeDeserialization);
	}

	/**
	 * Internal parsing method
	 * 
	 * @param reader
	 *            a Reader for input source
	 * @param rawSourceReader
	 *            a duplicated Reader used to build {@link RawSource}
	 * @param modelFactory
	 *            the {@link FMLModelFactory} to be used to build {@link FMLCompilationUnit}
	 * @param modelFactoryUpdater
	 *            a function returning a {@link FMLModelFactory} given a {@link List} of {@link ModelSlot} classes
	 * @param finalizeDeserialization
	 *            a boolean indicating if deserialization finalizing should be performed now, or will be done in a future second pass
	 * @return the newly created {@link FMLCompilationUnit}
	 * @throws ParseException
	 *             if parsing expression cannot be parsed
	 * @throws IOException
	 *             if an IOException occurs during parsing
	 */
	private FMLCompilationUnit parse(Reader reader, Reader rawSourceReader, FMLModelFactory modelFactory,
			Function<List<Class<? extends ModelSlot<?>>>, FMLModelFactory> modelFactoryUpdater, boolean finalizeDeserialization)
			throws ParseException, IOException {

		RawSource rawSource = readRawSource(rawSourceReader);

		try {
			// System.out.println("Parsing: " + anExpression);

			// RawSource rawSource = readRawSource(rawSourceReader);

			// System.out.println(rawSource.debug());

			// Create a Parser instance.
			Parser p = new Parser(new CustomLexer(new PushbackReader(reader), EntryPointKind.CompilationUnit));
			// Parser p = new Parser(new CustomLexer(new PushbackReader(reader), entryPointKind));

			// Parse the input.
			Start tree;
			tree = p.parse();

			// Print the AST
			// new ASTDebugger(tree);

			// Creates the semantics analyzer.
			semanticsAnalyzer = new FMLCompilationUnitSemanticsAnalyzer(modelFactory, tree, rawSource);

			// Find uses declarations
			UseDeclarationsExplorer e = new UseDeclarationsExplorer(semanticsAnalyzer);
			tree.apply(e);
			FMLModelFactory updatedModelFactory = modelFactoryUpdater.apply(e.getModelSlotClasses());
			if (updatedModelFactory != null) {
				semanticsAnalyzer.setModelFactory(updatedModelFactory);
			}

			// Apply the semantics analyzer.
			if (tree != null) {
				tree.apply(semanticsAnalyzer);
				semanticsAnalyzer.initializePrettyPrint();
				if (finalizeDeserialization) {
					// When deserialization required, do it now
					semanticsAnalyzer.finalizeDeserialization();
				}
				// Otherwise, do it in a future second pass
			}

			return semanticsAnalyzer.getCompilationUnit();
		} catch (ParserException e) {
			// e.printStackTrace();
			logger.info("ParserException token:" + e.getToken() + " line:" + e.getToken().getLine() + " length:"
					+ e.getToken().getText().length());
			throw new ParseException(e.getMessage(), e.getToken().getLine(), e.getToken().getPos(), e.getToken().getText().length(),
					rawSource);
		} catch (LexerException e) {
			throw new ParseException(e.getMessage(), e.getToken().getLine(), e.getToken().getPos(), e.getToken().getText().length(),
					rawSource);
		} finally {
			reader.close();
			rawSourceReader.close();
		}
	}

	/**
	 * Extract and return {@link VirtualModelInfo} given an input stream and a {@link FMLModelFactory}
	 * 
	 * @param inputStream
	 * @param modelFactory
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public VirtualModelInfo findVirtualModelInfo(InputStream inputStream, FMLModelFactory modelFactory) throws ParseException, IOException {
		byte[] buf = IOUtils.toByteArray(inputStream);
		InputStream inputStream1 = new ByteArrayInputStream(buf);
		InputStream inputStream2 = new ByteArrayInputStream(buf);

		return extractVirtualModelInfo(new InputStreamReader(inputStream1), new InputStreamReader(inputStream2), modelFactory);

	}

	private static VirtualModelInfo extractVirtualModelInfo(Reader reader, Reader rawSourceReader, FMLModelFactory modelFactory)
			throws ParseException, IOException {

		RawSource rawSource = readRawSource(rawSourceReader);

		try {
			// System.out.println("Parsing: " + anExpression);

			// Create a Parser instance.
			Parser p = new Parser(new CustomLexer(new PushbackReader(reader), EntryPointKind.CompilationUnit));
			// Parser p = new Parser(new Lexer(new PushbackReader(reader)));
			// Parser p = new Parser(new CustomLexer(new PushbackReader(reader), entryPointKind));

			// System.out.println(rawSource.debug());

			// Parse the input.
			Start tree;
			tree = p.parse();

			// Print the AST
			// new ASTDebugger(tree);

			// Creates the semantics analyzer.
			FMLCompilationUnitSemanticsAnalyzer analyzer = new FMLCompilationUnitSemanticsAnalyzer(modelFactory, tree, rawSource);

			// Find infos
			VirtualModelInfoExplorer e = new VirtualModelInfoExplorer(tree, analyzer);

			return e.getVirtualModelInfo();
		} catch (ParserException e) {
			// e.printStackTrace();
			logger.info("New exception token:" + e.getToken() + " line:" + e.getToken().getLine() + " length:"
					+ e.getToken().getText().length());
			// throw new ParseException(e.getMessage(), e.getToken().getLine(), e.getToken().getPos(), e.getToken().getText().length(),
			// rawSource);
			return attemptToRetrieveVirtualModelInfo(rawSource);
		} catch (LexerException e) {
			// throw new ParseException(e.getMessage(), e.getToken().getLine(), e.getToken().getPos(), e.getToken().getText().length(),
			// rawSource);
			return attemptToRetrieveVirtualModelInfo(rawSource);
		}
	}

	private static VirtualModelInfo attemptToRetrieveVirtualModelInfo(RawSource rawSource) {
		// TODO: on peut faire mieux !
		VirtualModelInfo vmi = new VirtualModelInfo();
		vmi.setName("Prout");
		vmi.setURI("http://prout");
		return vmi;
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
	 * Initialize pretty-print of a {@link FMLCompilationUnit}, if this one has not been obtained from an input stream parsing
	 * 
	 * @param fmlCompilationUnit
	 */
	public void initPrettyPrint(FMLCompilationUnit fmlCompilationUnit) {
		semanticsAnalyzer = new FMLCompilationUnitSemanticsAnalyzer(fmlCompilationUnit);
		FMLCompilationUnitNode fmlCompilationUnitNode = new FMLCompilationUnitNode(fmlCompilationUnit, semanticsAnalyzer);
		// fmlCompilationUnitNode.finalizeDeserialization();
	}

	public FMLCompilationUnitSemanticsAnalyzer getSemanticsAnalyzer() {
		return semanticsAnalyzer;
	}

	public FMLCompilationUnitNode getFMLCompilationUnitNode() {
		if (semanticsAnalyzer != null) {
			return semanticsAnalyzer.getCompilationUnitNode();
		}
		return null;
	}
}
