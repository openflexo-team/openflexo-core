package org.openflexo.foundation.fml.parser;

/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PushbackReader;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.parser.lexer.Lexer;
import org.openflexo.foundation.fml.parser.node.Start;
import org.openflexo.foundation.fml.parser.parser.Parser;

/**
 * This class provides the parsing service for FML. This includes syntaxic and semantics analyzer.<br>
 * 
 * SableCC is used to perform this. To compile and generate the grammar, please invoke {@link CompileFMLParser} located in src/dev/java. The
 * grammar is located in src/main/resources/FML/fml-grammar.sablecc<br>
 * Generated code is located in org.openflexo.foundation.fml.parser.analysis, org.openflexo.foundation.fml.parser.lexer,
 * org.openflexo.foundation.fml.parser.node, org.openflexo.foundation.fml.parser.parser
 * 
 * @author sylvain
 */
public class FMLParser {

	private static final Logger LOGGER = Logger.getLogger(FMLParser.class.getPackage().getName());

	/**
	 * This is the method to invoke to perform a parsing. Syntaxic and (some) semantics analyzer are performed and returned value is an
	 * Expression conform to AnTAR expression abstract syntaxic tree
	 * 
	 * @param anExpression
	 * @return
	 * @throws ParseException
	 *             if expression was not parsable
	 */
	public static FMLCompilationUnit parse(File inputFile) throws ParseException {
		try {
			FileReader in = new FileReader(inputFile);
			// System.out.println("Parsing: " + anExpression);

			// Create a Parser instance.
			Parser p = new Parser(new Lexer(new PushbackReader(new BufferedReader(in), 1024)));

			// Parse the input.
			Start tree = p.parse();

			// Apply the semantics analyzer.
			FMLSemanticsAnalyzer t = new FMLSemanticsAnalyzer();
			tree.apply(t);

			return t.getCompilationUnit();
		} catch (Exception e) {
			throw new ParseException(e.getMessage() + " while parsing " + inputFile);
		}
	}

}
