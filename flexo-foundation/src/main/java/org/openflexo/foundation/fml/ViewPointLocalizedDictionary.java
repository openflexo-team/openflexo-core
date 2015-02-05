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

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.localization.Language;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.HTMLUtils;
import org.openflexo.toolbox.StringUtils;

@ModelEntity
@ImplementationClass(ViewPointLocalizedDictionary.ViewPointLocalizedDictionaryImpl.class)
@XMLElement(xmlTag = "ViewPointLocalizedDictionary")
public interface ViewPointLocalizedDictionary extends FMLObject, org.openflexo.localization.LocalizedDelegate {

	@PropertyIdentifier(type = ViewPoint.class)
	public static final String OWNER_KEY = "owner";
	@PropertyIdentifier(type = List.class)
	public static final String LOCALIZED_ENTRIES_KEY = "localizedEntries";

	@Getter(value = LOCALIZED_ENTRIES_KEY, cardinality = Cardinality.LIST, inverse = ViewPointLocalizedEntry.LOCALIZED_DICTIONARY_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<ViewPointLocalizedEntry> getLocalizedEntries();

	@Setter(LOCALIZED_ENTRIES_KEY)
	public void setLocalizedEntries(List<ViewPointLocalizedEntry> entries);

	@Adder(LOCALIZED_ENTRIES_KEY)
	public void addToLocalizedEntries(ViewPointLocalizedEntry aEntrie);

	@Remover(LOCALIZED_ENTRIES_KEY)
	public void removeFromLocalizedEntries(ViewPointLocalizedEntry aEntrie);

	@Getter(value = OWNER_KEY, inverse = ViewPoint.LOCALIZED_DICTIONARY_KEY)
	public ViewPoint getOwner();

	@Setter(OWNER_KEY)
	public void setOwner(ViewPoint owner);

	@Override
	public List<DynamicEntry> getEntries();

	public void searchNewEntries();

	public DynamicEntry addEntry();

	public void deleteEntry(DynamicEntry entry);

	public static abstract class ViewPointLocalizedDictionaryImpl extends FMLObjectImpl implements ViewPointLocalizedDictionary {

		private static final Logger logger = Logger.getLogger(ViewPointLocalizedDictionary.class.getPackage().getName());

		// private Vector<LocalizedEntry> _entries;
		private final Hashtable<Language, Hashtable<String, String>> _values;
		private List<DynamicEntry> entries = null;

		public ViewPointLocalizedDictionaryImpl() {
			super();
			// _entries = new Vector<LocalizedEntry>();
			_values = new Hashtable<Language, Hashtable<String, String>>();
			/*if (builder != null) {
				owner = builder.getVirtualModel();
			}*/
		}

		@Override
		public String getURI() {
			// TODO Auto-generated method stub
			return null;
		}

		/*public ViewPointLocalizedDictionaryImpl() {
			super();
			_entries = new Vector<ViewPointLocalizedEntry>();
			_values = new Hashtable<Language, Hashtable<String, String>>();
			if (builder != null) {
				owner = builder.getViewPoint();
			}
		}*/

		@Override
		public ViewPoint getResourceData() {
			return getOwner();
		}

		/*@Override
		public ViewPoint getViewPoint() {
			return getOwner();
		}*/

		/*@Override
		public Vector<ViewPointLocalizedEntry> getEntries() {
			return _entries;
		}

		public void setEntries(Vector<LocalizedEntry> someEntries) {
			_entries = someEntries;
		}*/

		@Override
		public void addToLocalizedEntries(ViewPointLocalizedEntry entry) {
			performSuperAdder(LOCALIZED_ENTRIES_KEY, entry);
			// entry.setLocalizedDictionary(this);
			// _entries.add(entry);
			logger.fine("Add entry key:" + entry.getKey() + " lang=" + entry.getLanguage() + " value:" + entry.getValue());
			Language lang = Language.retrieveLanguage(entry.getLanguage());
			if (lang == null) {
				logger.warning("Undefined language: " + entry.getLanguage());
				return;
			}
			getDictForLang(lang).put(entry.getKey(), entry.getValue());
		}

		/*@Override
		public void removeFromEntries(ViewPointLocalizedEntry entry) {
			entry.setLocalizedDictionary(null);
			_entries.remove(entry);
		}*/

		private ViewPointLocalizedEntry getLocalizedEntry(Language language, String key) {
			for (ViewPointLocalizedEntry entry : getLocalizedEntries()) {
				if (Language.retrieveLanguage(entry.getLanguage()) == language && key.equals(entry.getKey())) {
					return entry;
				}
			}
			return null;
		}

		private Hashtable<String, String> getDictForLang(Language lang) {
			Hashtable<String, String> dict = _values.get(lang);
			if (dict == null) {
				dict = new Hashtable<String, String>();
				_values.put(lang, dict);
			}
			return dict;
		}

		/*public String getDefaultValue(String key, Language language) {
			// logger.info("Searched default value for key "+key+" return "+FlexoLocalization.localizedForKey(key));
			if (getParent() != null) {
				return FlexoLocalization.localizedForKeyAndLanguage(getParent(), key, language);
			}
			return key;
		}*/

		@Override
		public String getLocalizedForKeyAndLanguage(String key, Language language, boolean createsNewEntriesIfNonExistant) {
			return getLocalizedForKeyAndLanguage(key, language);
		}

		@Override
		public String getLocalizedForKeyAndLanguage(String key, Language language) {
			// if (isSearchingNewEntries) logger.info("-------> called localizedForKeyAndLanguage() key="+key+" lang="+language);
			return getDictForLang(language).get(key);

			/*String returned = getDictForLang(language).get(key);
			if (returned == null) {
				String defaultValue = getDefaultValue(key, language);
				if (handleNewEntry(key, language)) {
					if (!key.equals(defaultValue)) {
						addToEntries(new ViewPointLocalizedEntry(this, key, language.getName(), defaultValue));
						logger.fine("Calc ViewPointLocalizedDictionary: store value " + defaultValue + " for key " + key + " for language " + language);
					} else {
						getDictForLang(language).put(key, defaultValue);
						logger.fine("Calc ViewPointLocalizedDictionary: undefined value for key " + key + " for language " + language);
					}
					// dynamicEntries = null;
				}
				return defaultValue;
			}
			return returned;*/
		}

		public void setLocalizedForKeyAndLanguage(String key, String value, Language language) {
			getDictForLang(language).put(key, value);
			ViewPointLocalizedEntry entry = getLocalizedEntry(language, key);
			if (entry == null) {
				ViewPointLocalizedEntry newEntry = getViewPoint().getFMLModelFactory().newInstance(ViewPointLocalizedEntry.class);
				newEntry.setLanguage(language.getName());
				newEntry.setKey(key);
				newEntry.setValue(value);
				addToLocalizedEntries(newEntry);
			} else {
				entry.setValue(value);
			}
		}

		private boolean handleNewEntry = false;

		@Override
		public boolean handleNewEntry(String key, Language language) {
			return handleNewEntry;
		}

		// This method is really not efficient, but only called in the context of locales editor
		// This issue is not really severe.
		private Vector<String> buildAllKeys() {
			Vector<String> returned = new Vector<String>();
			for (Language l : _values.keySet()) {
				for (String key : _values.get(l).keySet()) {
					if (!returned.contains(key)) {
						returned.add(key);
					}
				}
			}
			return returned;
		}

		// This method is really not efficient, but only called in the context of locales editor
		// Impact of this issue is not really severe.
		@Override
		public List<DynamicEntry> getEntries() {
			if (entries == null) {
				entries = new Vector<DynamicEntry>();
				for (String key : buildAllKeys()) {
					entries.add(new DynamicEntryImpl(key));
				}
				Collections.sort(entries, new Comparator<DynamicEntry>() {
					@Override
					public int compare(DynamicEntry o1, DynamicEntry o2) {
						return Collator.getInstance().compare(o1.getKey(), o2.getKey());
					}
				});
			}
			return entries;
		}

		private DynamicEntry getEntry(String key) {
			if (key == null) {
				return null;
			}
			for (DynamicEntry entry : getEntries()) {
				if (key.equals(entry.getKey())) {
					return entry;
				}
			}
			return null;
		}

		public void refresh() {
			logger.fine("Refresh called on ViewPointLocalizedDictionary " + Integer.toHexString(hashCode()));
			entries = null;
			setChanged();
			notifyObservers();
		}

		@Override
		public DynamicEntry addEntry() {
			String newKey = "key";
			Vector<String> allKeys = buildAllKeys();
			boolean keyAlreadyExists = allKeys.contains(newKey);
			int index = 1;
			while (keyAlreadyExists) {
				newKey = "key" + index;
				keyAlreadyExists = allKeys.contains(newKey);
				index++;
			}

			ViewPointLocalizedEntry newEntry = getViewPoint().getFMLModelFactory().newInstance(ViewPointLocalizedEntry.class);
			newEntry.setLanguage(FlexoLocalization.getCurrentLanguage().getName());
			newEntry.setKey(newKey);
			newEntry.setValue(newKey);
			addToLocalizedEntries(newEntry);
			refresh();
			return getEntry(newKey);
		}

		@Override
		public void deleteEntry(DynamicEntry entry) {
			for (Language l : Language.availableValues()) {
				_values.get(l).remove(entry.getKey());
				ViewPointLocalizedEntry e = getLocalizedEntry(l, entry.getKey());
				if (e != null) {
					removeFromLocalizedEntries(e);
					// _entries.remove(e);
				}
			}
			refresh();
		}

		@Override
		public void searchNewEntries() {
			logger.info("Search new entries");
			for (VirtualModel vm : getViewPoint().getVirtualModels()) {
				for (FlexoConcept ep : vm.getFlexoConcepts()) {
					checkAndRegisterLocalized(ep.getName());
					for (FlexoBehaviour es : ep.getFlexoBehaviours()) {
						checkAndRegisterLocalized(es.getLabel());
						checkAndRegisterLocalized(es.getDescription());
						for (FlexoBehaviourParameter p : es.getParameters()) {
							checkAndRegisterLocalized(p.getLabel());
						}
						for (InspectorEntry entry : ep.getInspector().getEntries()) {
							checkAndRegisterLocalized(entry.getLabel());
						}
					}
				}
			}
			entries = null;
			getViewPoint().setChanged();
			getViewPoint().notifyObservers();
		}

		private void checkAndRegisterLocalized(String key) {
			handleNewEntry = true;
			FlexoLocalization.localizedForKey(this, key);
			// getLocalizedForKeyAndLanguage(key, FlexoLocalization.getCurrentLanguage());
			handleNewEntry = false;
		}

		@Override
		public BindingModel getBindingModel() {
			return getViewPoint().getBindingModel();
		}

		@Override
		public boolean registerNewEntry(String key, Language language, String value) {
			System.out.println("Register entry key=" + key + " lang=" + language + " value=" + value);
			setLocalizedForKeyAndLanguage(key, value, language);
			return true;
		}

		@Override
		public LocalizedDelegate getParent() {
			return FlexoLocalization.getMainLocalizer();
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			return "<not_implemented:" + getStringRepresentation() + ">";
		}

		@Override
		public File getLocalizedDirectory() {
			return null;
		}

		@Override
		public void searchLocalized() {
			searchNewEntries();
			getPropertyChangeSupport().firePropertyChange("entries", null, getEntries());
		}

		@Override
		public void searchTranslation(LocalizedEntry entry) {
			if (getParent() != null) {
				String englishTranslation = FlexoLocalization.localizedForKeyAndLanguage(getParent(), entry.getKey(), Language.ENGLISH,
						false);
				if (entry.getKey().equals(englishTranslation)) {
					englishTranslation = automaticEnglishTranslation(entry.getKey());
				}
				entry.setEnglish(englishTranslation);
				String dutchTranslation = FlexoLocalization.localizedForKeyAndLanguage(getParent(), entry.getKey(), Language.DUTCH, false);
				if (entry.getKey().equals(dutchTranslation)) {
					dutchTranslation = automaticDutchTranslation(entry.getKey());
				}
				entry.setDutch(dutchTranslation);
				String frenchTranslation = FlexoLocalization
						.localizedForKeyAndLanguage(getParent(), entry.getKey(), Language.FRENCH, false);
				if (entry.getKey().equals(frenchTranslation)) {
					frenchTranslation = automaticFrenchTranslation(entry.getKey());
				}
				entry.setFrench(frenchTranslation);
			} else {
				String englishTranslation = entry.getKey().toString();
				englishTranslation = englishTranslation.replace("_", " ");
				englishTranslation = englishTranslation.substring(0, 1).toUpperCase() + englishTranslation.substring(1);
				entry.setEnglish(englishTranslation);
				entry.setDutch(englishTranslation);
			}
		}

		private String automaticEnglishTranslation(String key) {
			String englishTranslation = key.toString();
			englishTranslation = englishTranslation.replace("_", " ");
			englishTranslation = englishTranslation.substring(0, 1).toUpperCase() + englishTranslation.substring(1);
			return englishTranslation;
		}

		private String automaticDutchTranslation(String key) {
			return key;
			// return automaticEnglishTranslation(key);
		}

		private String automaticFrenchTranslation(String key) {
			return key;
			// return automaticEnglishTranslation(key);
		}

		public class DynamicEntryImpl implements DynamicEntry {

			private String key;
			private final PropertyChangeSupport pcSupport;

			protected DynamicEntryImpl(String aKey) {
				pcSupport = new PropertyChangeSupport(this);
				key = aKey;
			}

			@Override
			public PropertyChangeSupport getPropertyChangeSupport() {
				return pcSupport;
			}

			@Override
			public String getDeletedProperty() {
				// TODO
				return null;
			}

			@Override
			public void delete() {
				if (entries != null) {
					entries.remove(this);
					for (Language l : Language.getAvailableLanguages()) {
						ViewPointLocalizedEntry e = getLocalizedEntry(l, key);
						if (e != null) {
							System.out.println("Removing " + e.getValue() + " for key " + key + " language=" + l);
							removeFromLocalizedEntries(e);
						}
					}
				}
			}

			@Override
			public String getKey() {
				return key;
			}

			@Override
			public void setKey(String aNewKey) {
				String oldKey = key;
				for (Language l : Language.availableValues()) {
					String oldValue = _values.get(l).get(oldKey);
					_values.get(l).remove(oldKey);
					if (oldValue != null) {
						_values.get(l).put(aNewKey, oldValue);
					}
					ViewPointLocalizedEntry e = getLocalizedEntry(l, oldKey);
					if (e != null) {
						e.setKey(aNewKey);
					}
				}
				key = aNewKey;
			}

			/*@Override
			public void setKey(String aKey) {
				String englishValue = getEnglish();
				String frenchValue = getFrench();
				String dutchValue = getDutch();
				key = aKey;
				setEnglish(englishValue);
				setFrench(frenchValue);
				setDutch(dutchValue);
			}*/

			@Override
			public String getEnglish() {
				// The locale might be found in parent localizer
				String returned = FlexoLocalization
						.localizedForKeyAndLanguage(ViewPointLocalizedDictionaryImpl.this, key, Language.ENGLISH);
				if (returned == null) {
					returned = key;
				}
				return returned;
			}

			@Override
			public void setEnglish(String value) {
				String oldValue = getEnglish();
				setLocalizedForKeyAndLanguage(key, value, Language.ENGLISH);
				getPropertyChangeSupport().firePropertyChange("english", oldValue, getEnglish());
			}

			@Override
			public String getFrench() {
				// The locale might be found in parent localizer
				String returned = FlexoLocalization.localizedForKeyAndLanguage(ViewPointLocalizedDictionaryImpl.this, key, Language.FRENCH);
				if (returned == null) {
					returned = key;
				}
				return returned;
			}

			@Override
			public void setFrench(String value) {
				String oldValue = getFrench();
				setLocalizedForKeyAndLanguage(key, value, Language.FRENCH);
				getPropertyChangeSupport().firePropertyChange("french", oldValue, getFrench());
			}

			@Override
			public String getDutch() {
				// The locale might be found in parent localizer
				String returned = FlexoLocalization.localizedForKeyAndLanguage(ViewPointLocalizedDictionaryImpl.this, key, Language.DUTCH);
				if (returned == null) {
					returned = key;
				}
				return returned;
			}

			@Override
			public void setDutch(String value) {
				String oldValue = getDutch();
				setLocalizedForKeyAndLanguage(key, value, Language.DUTCH);
				getPropertyChangeSupport().firePropertyChange("dutch", oldValue, getDutch());
			}

			@Override
			public boolean getIsHTML() {
				return getFrench().startsWith("<html>") || getEnglish().startsWith("<html>") || getDutch().startsWith("<html>");
			}

			@Override
			public void setIsHTML(boolean flag) {
				if (flag) {
					setEnglish(addHTMLSupport(getEnglish()));
					setFrench(addHTMLSupport(getFrench()));
					setDutch(addHTMLSupport(getDutch()));
				} else {
					setEnglish(removeHTMLSupport(getEnglish()));
					setFrench(removeHTMLSupport(getFrench()));
					setDutch(removeHTMLSupport(getDutch()));
				}
				getPropertyChangeSupport().firePropertyChange("isHTML", !flag, flag);
			}

			private String addHTMLSupport(String value) {
				return "<html>" + StringUtils.LINE_SEPARATOR + "<head>" + StringUtils.LINE_SEPARATOR + "</head>"
						+ StringUtils.LINE_SEPARATOR + "<body>" + StringUtils.LINE_SEPARATOR + value + StringUtils.LINE_SEPARATOR
						+ "</body>" + StringUtils.LINE_SEPARATOR + "</html>";
			}

			private String removeHTMLSupport(String value) {
				return HTMLUtils.convertHTMLToPlainText(HTMLUtils.extractBodyContent(value, true).trim(), true);
				/*System.out.println("From " + value);
				System.out.println("To" + HTMLUtils.extractSourceFromEmbeddedTag(value));
				return HTMLUtils.extractSourceFromEmbeddedTag(value);*/
			}

			@Override
			public boolean hasInvalidValue() {
				return !isFrenchValueValid() || !isEnglishValueValid() || !isDutchValueValid();
			}

			@Override
			public boolean isFrenchValueValid() {
				return isValueValid(getKey(), getFrench());
			}

			@Override
			public boolean isEnglishValueValid() {
				return isValueValid(getKey(), getEnglish());
			}

			@Override
			public boolean isDutchValueValid() {
				return isValueValid(getKey(), getDutch());
			}

			public boolean isValueValid(String aKey, String aValue) {
				if (aValue == null || aValue.length() == 0) {
					return false;
				} // null or empty value is not valid
				if (aValue.equals(aKey)) {
					return false;
				} // not the same value > means not translated
				if (aValue.lastIndexOf("_") > -1) {
					return false;
				} // should not contains UNDERSCORE char
				return true;
			}

			@Override
			public String toString() {
				return "(key=" + key + "{en=" + getEnglish() + ";fr=" + getFrench() + ";du=" + getDutch() + "})";
			}

		}

	}

	public interface DynamicEntry extends LocalizedEntry {

		@Override
		public String getKey();

		public void setKey(String aNewKey);

		@Override
		public String getEnglish();

		@Override
		public void setEnglish(String value);

		@Override
		public String getFrench();

		@Override
		public void setFrench(String value);

		@Override
		public String getDutch();

		@Override
		public void setDutch(String value);

	}

}
