package org.openflexo.foundation.fml.parser.lexer;

import java.io.IOException;
import java.io.PushbackReader;

import org.openflexo.foundation.fml.parser.node.Token;

public class CustomLexer extends Lexer {

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
