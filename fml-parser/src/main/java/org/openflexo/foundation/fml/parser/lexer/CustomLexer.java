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
				return State.COMMAND;
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
		return super.getToken();
	}

}
