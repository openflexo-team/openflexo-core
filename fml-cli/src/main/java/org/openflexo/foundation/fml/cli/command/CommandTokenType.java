package org.openflexo.foundation.fml.cli.command;

public enum CommandTokenType {
	Expression {
		@Override
		public String syntaxKeyword() {
			return "<expression>";
		}
	},
	Path {
		@Override
		public String syntaxKeyword() {
			return "<path>";
		}
	},
	TA {
		@Override
		public String syntaxKeyword() {
			return "<ta>";
		}
	},
	RC {
		@Override
		public String syntaxKeyword() {
			return "<rc>";
		}
	},
	Resource {
		@Override
		public String syntaxKeyword() {
			return "<resource>";
		}
	},
	Service {
		@Override
		public String syntaxKeyword() {
			return "<service>";
		}
	},
	Operation {
		@Override
		public String syntaxKeyword() {
			return "<operation>";
		}
	};

	public abstract String syntaxKeyword();

	public static CommandTokenType getType(String syntaxKeyword) {
		for (CommandTokenType commandTokenType : values()) {
			if (commandTokenType.syntaxKeyword().equals(syntaxKeyword)) {
				return commandTokenType;
			}
		}
		System.err.println("Unexpected CommandTokenType: " + syntaxKeyword);
		return null;
	}
}
