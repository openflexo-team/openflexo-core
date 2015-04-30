/* This file is part of the Java 1.5 grammar for SableCC.
 *
 * Copyright 2006 Etienne M. Gagnon <egagnon@j-meg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.java5;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

import org.sablecc.grammars.java_1_5.unicodepreprocessor.analysis.AnalysisAdapter;
import org.sablecc.grammars.java_1_5.unicodepreprocessor.lexer.LexerException;
import org.sablecc.grammars.java_1_5.unicodepreprocessor.node.EOF;
import org.sablecc.grammars.java_1_5.unicodepreprocessor.node.TErroneousEscape;
import org.sablecc.grammars.java_1_5.unicodepreprocessor.node.TEvenBackslash;
import org.sablecc.grammars.java_1_5.unicodepreprocessor.node.TRawInputCharacter;
import org.sablecc.grammars.java_1_5.unicodepreprocessor.node.TSub;
import org.sablecc.grammars.java_1_5.unicodepreprocessor.node.TUnicodeEscape;

public class UnicodePreprocessor extends Reader {
	private final UnicodeLexer lexer;
	private final ProcessToken processor = new ProcessToken();

	public UnicodePreprocessor(PushbackReader in) {
		lexer = new UnicodeLexer(in);
	}

	@Override
	public int read() throws IOException {
		if (available == 0) {
			try {
				lexer.next().apply(processor);
			} catch (LexerException e) {
				throw new RuntimeException(e.toString());
			}
		}

		if (available == 0) {
			return -1;
		}

		char c = buffer[0];
		buffer[0] = buffer[1];
		available--;

		return c;
	}

	@Override
	public int read(char cbuf[], int off, int len) throws IOException {
		for (int i = 0; i < len; i++) {
			int c = read();

			if (c == -1) {
				if (i == 0) {
					return -1;
				} else {
					return i;
				}
			}

			cbuf[off + i] = (char) c;
		}

		return len;
	}

	int available;
	char[] buffer = new char[2];

	private class ProcessToken extends AnalysisAdapter {
		@Override
		public void caseTEvenBackslash(TEvenBackslash node) {
			buffer[0] = '\\';
			buffer[1] = '\\';
			available = 2;
		}

		@Override
		public void caseTUnicodeEscape(TUnicodeEscape node) {
			String text = node.getText();
			buffer[0] = (char) Integer.parseInt(text.substring(text.length() - 4), 16);
			available = 1;
		}

		@Override
		public void caseTErroneousEscape(TErroneousEscape node) {
			throw new RuntimeException("Erroneous escape: " + node);
		}

		@Override
		public void caseTSub(TSub node) {
			buffer[0] = node.getText().charAt(0);
			available = 1;
		}

		@Override
		public void caseTRawInputCharacter(TRawInputCharacter node) {
			buffer[0] = node.getText().charAt(0);
			available = 1;
		}

		@Override
		public void caseEOF(EOF node) {
			available = 0;
		}
	}

	@Override
	public void close() {
	}
}
