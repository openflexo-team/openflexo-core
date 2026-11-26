package org.openflexo.foundation.fml.parser.lexer;

import java.io.IOException;
import java.io.PushbackReader;

import org.openflexo.foundation.fml.parser.node.Token;

public class CustomLexer extends Lexer {

	/**
	 * The size of the pushback buffer a reader given to this lexer needs. The lexer reads ahead as long as a longer token may match, and
	 * gives back what it read when none does: an escaped identifier <code>{"..."}</code> is one token (CORE-F-5), so a <code>{</code>
	 * is followed by a read ahead up to the end of a string literal of the same line. The buffer must hold that line
	 */
	public static final int PUSHBACK_BUFFER_SIZE = 1 << 16;

	/**
	 * The pushback buffer for an input of known length: the lexer never gives back more than it read
	 */
	public static int pushbackBufferSize(int inputLength) {
		return Math.max(1, Math.min(PUSHBACK_BUFFER_SIZE, inputLength + 1));
	}

	public enum EntryPointKind {
		CompilationUnit {
			@Override
			public Token getToken(Lexer l) {
				return l.new0("", 0, 0);
			}

			@Override
			public State getState(Lexer l) {
				return State.NORMAL;
			}
		},
		Binding {
			@Override
			public Token getToken(Lexer l) {
				return l.new1("", 0, 0);
			}

			@Override
			public State getState(Lexer l) {
				return State.BINDING;
			}
		},
		Command {
			@Override
			public Token getToken(Lexer l) {
				return l.new2("", 0, 0);
			}

			@Override
			public State getState(Lexer l) {
				return State.COMMAND;
			}
		},
		Script {
			@Override
			public Token getToken(Lexer l) {
				return l.new3("", 0, 0);
			}

			@Override
			public State getState(Lexer l) {
				return State.SCRIPT;
			}
		};

		public abstract Token getToken(Lexer l);

		public abstract State getState(Lexer l);
	}

	private boolean started = false;
	private EntryPointKind entryPoint;

	public CustomLexer(PushbackReader in, EntryPointKind ep) {
		super(in);
		this.entryPoint = ep;
	}

	@Override
	public Token getToken() throws IOException, LexerException {
		if (!started) {
			this.started = true;
			this.state = this.entryPoint.getState(this);
			return this.entryPoint.getToken(this);
		}
		Token returned = super.getToken();
		// System.out.println("Return token " + returned + " of " + returned.getClass() + " state=" + getStateAsString(state));
		return returned;
	}

	private String getStateAsString(State state) {
		switch (state.id()) {
			case 0:
				return "NORMAL";
			case 1:
				return "BINDING";
			case 2:
				return "COMMAND";
			case 3:
				return "SCRIPT";
		}
		return "?";
	}

}
