/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

import org.openflexo.connie.BindingModel;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.FixProposal;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.model.validation.ValidationWarning;

@ModelEntity
@ImplementationClass(ViewPointLocalizedEntry.LocalizedEntryImpl.class)
@XMLElement(xmlTag = "Localized")
public interface ViewPointLocalizedEntry extends FMLObject {

	@PropertyIdentifier(type = ViewPointLocalizedDictionary.class)
	public static final String LOCALIZED_DICTIONARY_KEY = "localizedDictionary";
	@PropertyIdentifier(type = String.class)
	public static final String KEY_KEY = "key";
	@PropertyIdentifier(type = String.class)
	public static final String LANGUAGE_KEY = "language";
	@PropertyIdentifier(type = String.class)
	public static final String VALUE_KEY = "value";

	@Getter(value = KEY_KEY)
	@XMLAttribute
	public String getKey();

	@Setter(KEY_KEY)
	public void setKey(String key);

	@Getter(value = LANGUAGE_KEY)
	@XMLAttribute(xmlTag = "lang")
	public String getLanguage();

	@Setter(LANGUAGE_KEY)
	public void setLanguage(String language);

	@Getter(value = VALUE_KEY)
	public String getValue();

	@Setter(VALUE_KEY)
	public void setValue(String value);

	@Override
	@Getter(value = LOCALIZED_DICTIONARY_KEY /*, inverse = ViewPointLocalizedDictionary.ENTRIES_KEY*/)
	public ViewPointLocalizedDictionary getLocalizedDictionary();

	@Setter(LOCALIZED_DICTIONARY_KEY)
	public void setLocalizedDictionary(ViewPointLocalizedDictionary owner);

	public static abstract class LocalizedEntryImpl extends FMLObjectImpl implements ViewPointLocalizedEntry {

		private ViewPointLocalizedDictionary _dictionary;

		private String key;
		private String language;
		private String value;

		public LocalizedEntryImpl() {
			super();
		}

		public LocalizedEntryImpl(ViewPointLocalizedDictionary localizedDictionary, String key, String language, String value) {
			super();
			setLocalizedDictionary(localizedDictionary);
			this.key = key;
			this.language = language;
			this.value = value;
		}

		@Override
		public void setLocalizedDictionary(ViewPointLocalizedDictionary dict) {
			_dictionary = dict;
		}

		@Override
		public ViewPointLocalizedDictionary getLocalizedDictionary() {
			return _dictionary;
		}

		@Override
		public ViewPoint getResourceData() {
			if (getLocalizedDictionary() != null) {
				return getLocalizedDictionary().getViewPoint();
			} else {
				return null;
			}
		}

		/*@Override
		public ViewPoint getViewPoint() {
			if (getLocalizedDictionary() != null) {
				return getLocalizedDictionary().getViewPoint();
			} else {
				return null;
			}
		}*/

		@Override
		public String getURI() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public void setKey(String key) {
			this.key = key;
		}

		@Override
		public String getLanguage() {
			return language;
		}

		@Override
		public void setLanguage(String language) {
			this.language = language;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public BindingModel getBindingModel() {
			return getViewPoint().getBindingModel();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			return "<not_implemented:" + getStringRepresentation() + ">";
		}

	}

	@DefineValidationRule
	public static class LocalizedEntryShouldNotBeRegisteredTwice extends
			ValidationRule<LocalizedEntryShouldNotBeRegisteredTwice, ViewPointLocalizedEntry> {
		public LocalizedEntryShouldNotBeRegisteredTwice() {
			super(ViewPointLocalizedEntry.class, "localized_entry_should_not_be_registered_twice");
		}

		@Override
		public ValidationIssue<LocalizedEntryShouldNotBeRegisteredTwice, ViewPointLocalizedEntry> applyValidation(
				ViewPointLocalizedEntry entry) {

			if (entry.getLocalizedDictionary() != null) {
				if (entry.getLocalizedDictionary().getLocalizedEntries().indexOf(entry) != entry.getLocalizedDictionary()
						.getLocalizedEntries().lastIndexOf(entry)) {
					RemoveExtraReferences fixProposal = new RemoveExtraReferences(entry);
					return new ValidationWarning<LocalizedEntryShouldNotBeRegisteredTwice, ViewPointLocalizedEntry>(this, entry,
							"localized_entry_is_registered_twice", fixProposal);
				}
			}
			return null;
		}

		protected static class RemoveExtraReferences extends FixProposal<LocalizedEntryShouldNotBeRegisteredTwice, ViewPointLocalizedEntry> {

			private final ViewPointLocalizedEntry entry;

			public RemoveExtraReferences(ViewPointLocalizedEntry entry) {
				super("remove_duplicated_references");
				this.entry = entry;
			}

			@Override
			protected void fixAction() {
				ViewPointLocalizedDictionary dict = entry.getLocalizedDictionary();
				if (dict != null) {
					while (dict.getLocalizedEntries().indexOf(entry) != dict.getLocalizedEntries().lastIndexOf(entry)) {
						System.out.println("remove " + entry);
						entry.getLocalizedDictionary().removeFromLocalizedEntries(entry);
					}
				}
			}

		}

	}

}
