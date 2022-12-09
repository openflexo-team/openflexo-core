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

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.expr.Expression;
import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLBindingFactory;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.lexer.CustomLexer;
import org.openflexo.foundation.fml.parser.lexer.CustomLexer.EntryPointKind;
import org.openflexo.foundation.fml.parser.lexer.LexerException;
import org.openflexo.foundation.fml.parser.node.Start;
import org.openflexo.foundation.fml.parser.parser.Parser;
import org.openflexo.foundation.fml.parser.parser.ParserException;

/**
 * This class provides the parsing service for FML expressions<br>
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
public class FMLExpressionParser {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FMLExpressionParser.class.getPackage().getName());

	/**
	 * This is the method to invoke to perform a parsing.<br>
	 * Syntactic and semantics analyzer are performed and returned value is a {@link DataBinding}
	 * 
	 * @param data
	 *            data to parse
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static Expression parse(String data, Bindable bindable, AbstractFMLTypingSpace typingSpace, FMLModelFactory modelFactory)
			throws ParseException {
		return parse(new StringReader(data), new StringReader(data), bindable, typingSpace, modelFactory);
	}

	private static Expression parse(Reader reader, Reader rawSourceReader, Bindable bindable, AbstractFMLTypingSpace typingSpace,
			FMLModelFactory modelFactory) throws ParseException {
		try {
			// Create a Parser instance.
			Parser p = new Parser(new CustomLexer(new PushbackReader(reader), EntryPointKind.Binding));
			// Parser p = new Parser(new CustomLexer(new PushbackReader(reader), entryPointKind));

			// Parse the input.
			Start tree;
			tree = p.parse();

			// Print the AST
			// ASTDebugger.debug(tree);

			return ExpressionFactory.makeDataBinding(tree, bindable, BindingDefinitionType.GET, Object.class, modelFactory, typingSpace,
					new FMLBindingFactory(modelFactory)).getExpression();

		} catch (ParserException e) {
			// e.printStackTrace();
			logger.info("ParserException token:" + e.getToken() + " line:" + e.getToken().getLine() + " length:"
					+ e.getToken().getText().length());
			throw new ParseException(e.getMessage(), e.getToken().getLine(), e.getToken().getPos(), e.getToken().getText().length());
		} catch (LexerException e) {
			throw new ParseException(e.getMessage(), e.getToken().getLine(), e.getToken().getPos(), e.getToken().getText().length());
		} catch (IOException e) {
			throw new ParseException(e.getMessage(), -1, -1, -1);
		} finally {
			try {
				reader.close();
				rawSourceReader.close();
			} catch (IOException e) {
				throw new ParseException(e.getMessage(), -1, -1, -1);
			}
		}
	}

}
