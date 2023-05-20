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

package org.openflexo.foundation.fml;

/**
 * Contains all FML reserved keywords
 * 
 * @author sylvain
 */
public enum FMLKeywords {

	Abstract {
		@Override
		public String getKeyword() {
			return "abstract";
		}
	},
	Action {
		@Override
		public String getKeyword() {
			return "action";
		}
	},
	As {
		@Override
		public String getKeyword() {
			return "as";
		}
	},
	Assert {
		@Override
		public String getKeyword() {
			return "assert";
		}
	},
	Begin {
		@Override
		public String getKeyword() {
			return "begin";
		}
	},
	Concept {
		@Override
		public String getKeyword() {
			return "concept";
		}
	},
	Create {
		@Override
		public String getKeyword() {
			return "create";
		}
	},
	Default {
		@Override
		public String getKeyword() {
			return "default";
		}
	},
	Delete {
		@Override
		public String getKeyword() {
			return "delete";
		}
	},
	Do {
		@Override
		public String getKeyword() {
			return "do";
		}
	},
	Else {
		@Override
		public String getKeyword() {
			return "else";
		}
	},
	End {
		@Override
		public String getKeyword() {
			return "end";
		}
	},
	Enum {
		@Override
		public String getKeyword() {
			return "enum";
		}
	},
	Event {
		@Override
		public String getKeyword() {
			return "event";
		}
	},
	Extends {
		@Override
		public String getKeyword() {
			return "extends";
		}
	},
	For {
		@Override
		public String getKeyword() {
			return "for";
		}
	},
	From {
		@Override
		public String getKeyword() {
			return "from";
		}
	},
	Get {
		@Override
		public String getKeyword() {
			return "get";
		}
	},
	If {
		@Override
		public String getKeyword() {
			return "if";
		}
	},
	Import {
		@Override
		public String getKeyword() {
			return "import";
		}
	},
	In {
		@Override
		public String getKeyword() {
			return "in";
		}
	},
	Inside {
		@Override
		public String getKeyword() {
			return "inside";
		}
	},
	InstanceOf {
		@Override
		public String getKeyword() {
			return "instanceOf";
		}
	},
	Listen {
		@Override
		public String getKeyword() {
			return "listen";
		}
	},
	Log {
		@Override
		public String getKeyword() {
			return "log";
		}
	},
	Match {
		@Override
		public String getKeyword() {
			return "match";
		}
	},
	Model {
		@Override
		public String getKeyword() {
			return "model";
		}
	},
	Namespace {
		@Override
		public String getKeyword() {
			return "namespace";
		}
	},
	New {
		@Override
		public String getKeyword() {
			return "new";
		}
	},
	Notify {
		@Override
		public String getKeyword() {
			return "notify";
		}
	},
	OnFailure {
		@Override
		public String getKeyword() {
			return "onfailure";
		}
	},
	Private {
		@Override
		public String getKeyword() {
			return "private";
		}
	},
	Protected {
		@Override
		public String getKeyword() {
			return "protected";
		}
	},
	Public {
		@Override
		public String getKeyword() {
			return "public";
		}
	},
	Receive {
		@Override
		public String getKeyword() {
			return "receive";
		}
	},
	Required {
		@Override
		public String getKeyword() {
			return "required";
		}
	},
	Return {
		@Override
		public String getKeyword() {
			return "return";
		}
	},
	Select {
		@Override
		public String getKeyword() {
			return "select";
		}
	},
	Set {
		@Override
		public String getKeyword() {
			return "set";
		}
	},
	Super {
		@Override
		public String getKeyword() {
			return "super";
		}
	},
	Then {
		@Override
		public String getKeyword() {
			return "then";
		}
	},
	Type {
		@Override
		public String getKeyword() {
			return "type";
		}
	},
	Unique {
		@Override
		public String getKeyword() {
			return "unique";
		}
	},
	Use {
		@Override
		public String getKeyword() {
			return "use";
		}
	},
	Values {
		@Override
		public String getKeyword() {
			return "values";
		}
	},
	Where {
		@Override
		public String getKeyword() {
			return "where";
		}
	},
	While {
		@Override
		public String getKeyword() {
			return "while";
		}
	},
	With {
		@Override
		public String getKeyword() {
			return "with";
		}
	};

	public abstract String getKeyword();

	public static boolean isKeyword(String aString) {
		for (FMLKeywords kw : FMLKeywords.values()) {
			if (kw.getKeyword().equals(aString)) {
				return true;
			}
		}
		return false;
	}

}
