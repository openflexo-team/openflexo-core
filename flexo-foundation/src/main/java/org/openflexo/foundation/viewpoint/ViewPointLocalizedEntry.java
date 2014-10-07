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
package org.openflexo.foundation.viewpoint;

import org.openflexo.antar.binding.BindingModel;
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
public interface ViewPointLocalizedEntry extends ViewPointObject {

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

	public static abstract class LocalizedEntryImpl extends ViewPointObjectImpl implements ViewPointLocalizedEntry {

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
		public ViewPoint getViewPoint() {
			return getLocalizedDictionary().getViewPoint();
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
