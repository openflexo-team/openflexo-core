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

import java.awt.Component;
import java.awt.Frame;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;

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
import org.openflexo.model.undo.CompoundEdit;
import org.openflexo.toolbox.HTMLUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * A dictionary of locales attached to a {@link VirtualModel} (see {@link #getOwner()})
 * 
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(FMLLocalizedDictionary.ViewPointLocalizedDictionaryImpl.class)
@XMLElement(xmlTag = "FMLLocalizedDictionary", deprecatedXMLTags = "FMLLocalizedDictionary")
public interface FMLLocalizedDictionary extends FMLObject, org.openflexo.localization.LocalizedDelegate {

	@PropertyIdentifier(type = VirtualModel.class)
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

	@Getter(value = OWNER_KEY, inverse = VirtualModel.LOCALIZED_DICTIONARY_KEY)
	public VirtualModel getOwner();

	@Setter(OWNER_KEY)
	public void setOwner(VirtualModel owner);

	@Override
	public List<DynamicEntry> getEntries();

	public void searchNewEntries();

	public DynamicEntry addEntry(String key);

	@Override
	public DynamicEntry addEntry();

	public void deleteEntry(DynamicEntry entry);

	public static abstract class ViewPointLocalizedDictionaryImpl extends FMLObjectImpl implements FMLLocalizedDictionary {

		private static final Logger logger = Logger.getLogger(FMLLocalizedDictionary.class.getPackage().getName());

		// private Vector<LocalizedEntry> _entries;
		private final Map<Language, Hashtable<String, String>> _values;
		private Map<String, DynamicEntry> entriesMap = null;
		private List<DynamicEntry> entries = null;

		private final WeakHashMap<Component, String> _storedLocalizedForComponents = new WeakHashMap<>();
		private final WeakHashMap<JComponent, String> _storedLocalizedForComponentTooltips = new WeakHashMap<>();
		private final WeakHashMap<TitledBorder, String> _storedLocalizedForBorders = new WeakHashMap<>();
		private final WeakHashMap<TableColumn, String> _storedLocalizedForTableColumn = new WeakHashMap<>();

		public ViewPointLocalizedDictionaryImpl() {
			super();
			_values = new HashMap<>();
			entriesMap = new HashMap<>();
		}

		@Override
		public String getURI() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public VirtualModel getResourceData() {
			return getOwner();
		}

		@Override
		public void addToLocalizedEntries(ViewPointLocalizedEntry entry) {
			performSuperAdder(LOCALIZED_ENTRIES_KEY, entry);
			// entry.setLocalizedDictionary(this);
			// _entries.add(entry);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Add entry key:" + entry.getKey() + " lang=" + entry.getLanguage() + " value:" + entry.getValue());
			}
			Language lang = Language.retrieveLanguage(entry.getLanguage());
			if (lang == null) {
				logger.warning("Undefined language: " + entry.getLanguage());
				return;
			}
			getDictForLang(lang).put(entry.getKey(), entry.getValue());
		}

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
				dict = new Hashtable<>();
				_values.put(lang, dict);
			}
			return dict;
		}

		/**
		 * Return String matching specified key and language set as default language<br>
		 * 
		 * This is general and main method to use localized in Flexo.<br>
		 * Applicable language is chosen from the one defined in FlexoLocalization (configurable from GeneralPreferences).<br>
		 * Use english names for keys, such as 'some_english_words'<br>
		 * 
		 * Usage example: <code>localizedForKey("some_english_words")</code>
		 * 
		 * @param key
		 * @return String matching specified key and language defined as default in {@link FlexoLocalization}
		 */
		@Override
		public String localizedForKey(String key) {
			return localizedForKeyAndLanguage(key, FlexoLocalization.getCurrentLanguage());
		}

		/**
		 * Return String matching specified key and language<br>
		 * 
		 * @param key
		 * @param language
		 * @return
		 */
		@Override
		public String localizedForKeyAndLanguage(String key, Language language) {
			return localizedForKeyAndLanguage(key, language, handleNewEntry(key, language));
		}

		/**
		 * Return String matching specified key and language<br>
		 * If #createsNewEntryInFirstEditableParent set to true, will try to enter a new traduction.<br>
		 * LocalizedDelegate are recursively requested to their parents, and the first one who respond true to
		 * {@link #handleNewEntry(String, Language)} will add a new entry
		 * 
		 * @param key
		 * @param language
		 * @return
		 */
		// TODO: duplicated code as in LocalizedDelegateImpl, please refactor this to avoid code duplication
		@Override
		public String localizedForKeyAndLanguage(String key, Language language, boolean createsNewEntryInFirstEditableParent) {

			if (key == null || StringUtils.isEmpty(key)) {
				return null;
			}

			String localized = getDictForLang(language).get(key);

			if (localized == null) {
				// Not found in this localizer what about parent ?
				if (getParent() != null) {
					if (getParent().hasKey(key, language, true)) {
						// This is defined in parent localizer
						// Nice, we forward the request to the parent
						return getParent().localizedForKeyAndLanguage(key, language, false);
					}
					else if (createsNewEntryInFirstEditableParent && handleNewEntry(key, language)) {
						addEntry(key);
						return getDictForLang(language).get(key);
					}
					else {
						return getParent().localizedForKeyAndLanguage(key, language, true);
					}
				}
				else {
					// parent is null
					if (handleNewEntry(key, language)) {
						addEntry(key);
						return getDictForLang(language).get(key);
					}
					return key;
				}
			}

			return localized;
		}

		/**
		 * Return boolean indicating if this delegate defines a translation for supplied key and language
		 * 
		 * @return
		 */
		@Override
		public boolean hasKey(String key, Language language, boolean recursive) {
			String localized = getDictForLang(language).get(key);
			if (localized != null) {
				return true;
			}
			if (recursive && getParent() != null) {
				return getParent().hasKey(key, language, recursive);
			}
			return false;
		}

		// TODO: duplicated code as in LocalizedDelegateImpl, please refactor this to avoid code duplication
		@Override
		public String localizedForKeyWithParams(String key, Object... object) {
			String base = localizedForKey(key);
			return FlexoLocalization.replaceAllParamsInString(base, object);
		}

		// TODO: duplicated code as in LocalizedDelegateImpl, please refactor this to avoid code duplication
		@Override
		public String localizedForKey(String key, Component component) {
			if (logger.isLoggable(Level.FINE)) {
				logger.finest("localizedForKey called with " + key + " for " + component.getClass().getName());
			}
			_storedLocalizedForComponents.put(component, key);
			return localizedForKey(key);
		}

		// TODO: duplicated code as in LocalizedDelegateImpl, please refactor this to avoid code duplication
		@Override
		public String localizedTooltipForKey(String key, JComponent component) {
			if (logger.isLoggable(Level.FINE)) {
				logger.finest("localizedForKey called with " + key + " for " + component.getClass().getName());
			}
			_storedLocalizedForComponentTooltips.put(component, key);
			return localizedForKey(key);
		}

		// TODO: duplicated code as in LocalizedDelegateImpl, please refactor this to avoid code duplication
		@Override
		public String localizedForKey(String key, TitledBorder border) {
			if (logger.isLoggable(Level.FINE)) {
				logger.finest("localizedForKey called with " + key + " for border " + border.getClass().getName());
			}
			_storedLocalizedForBorders.put(border, key);
			return localizedForKey(key);
		}

		// TODO: duplicated code as in LocalizedDelegateImpl, please refactor this to avoid code duplication
		@Override
		public String localizedForKey(String key, TableColumn column) {
			if (logger.isLoggable(Level.FINE)) {
				logger.finest("localizedForKey called with " + key + " for border " + column.getClass().getName());
			}
			_storedLocalizedForTableColumn.put(column, key);
			return localizedForKey(key);
		}

		// TODO: duplicated code as in LocalizedDelegateImpl, please refactor this to avoid code duplication
		@Override
		public void clearStoredLocalizedForComponents() {
			_storedLocalizedForComponents.clear();
			_storedLocalizedForBorders.clear();
			// _storedAdditionalStrings.clear();
			_storedLocalizedForTableColumn.clear();
			// localizationListeners.clear();
		}

		// TODO: duplicated code as in LocalizedDelegateImpl, please refactor this to avoid code duplication
		@Override
		public void updateGUILocalized() {
			for (Map.Entry<Component, String> e : _storedLocalizedForComponents.entrySet()) {
				Component component = e.getKey();
				String string = e.getValue();
				String text = localizedForKey(string);
				/*String additionalString = _storedAdditionalStrings.get(component);
				if (additionalString != null) {
					text = text + additionalString;
				}*/
				if (component instanceof AbstractButton) {
					((AbstractButton) component).setText(text);
				}
				if (component instanceof JLabel) {
					((JLabel) component).setText(text);
				}
				component.setName(text);
				if (component.getParent() instanceof JTabbedPane) {
					if (((JTabbedPane) component.getParent()).indexOfComponent(component) > -1) {
						((JTabbedPane) component.getParent()).setTitleAt(((JTabbedPane) component.getParent()).indexOfComponent(component),
								text);
					}
				}
				if (component.getParent() != null && component.getParent().getParent() instanceof JTabbedPane) {
					if (((JTabbedPane) component.getParent().getParent()).indexOfComponent(component) > -1) {
						((JTabbedPane) component.getParent().getParent())
								.setTitleAt(((JTabbedPane) component.getParent().getParent()).indexOfComponent(component), text);
					}
				}
			}
			for (Map.Entry<JComponent, String> e : _storedLocalizedForComponentTooltips.entrySet()) {
				JComponent component = e.getKey();
				String string = e.getValue();
				String text = localizedForKey(string);
				component.setToolTipText(text);
			}
			for (Map.Entry<TitledBorder, String> e : _storedLocalizedForBorders.entrySet()) {
				String string = e.getValue();
				String text = localizedForKey(string);
				e.getKey().setTitle(text);
			}
			for (Map.Entry<TableColumn, String> e : _storedLocalizedForTableColumn.entrySet()) {
				String string = e.getValue();
				String text = localizedForKey(string);
				e.getKey().setHeaderValue(text);
			}
			for (Frame f : Frame.getFrames()) {
				f.repaint();
			}

		}

		public void setLocalizedForKeyAndLanguage(String key, String value, Language language) {
			getDictForLang(language).put(key, value);
			ViewPointLocalizedEntry entry = getLocalizedEntry(language, key);
			if (entry == null) {
				ViewPointLocalizedEntry newEntry = getOwner().getFMLModelFactory().newInstance(ViewPointLocalizedEntry.class);
				newEntry.setLanguage(language.getName());
				newEntry.setKey(key);
				newEntry.setValue(value);
				addToLocalizedEntries(newEntry);
			}
			else {
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
		private List<String> buildAllKeys() {
			List<String> returned = new ArrayList<>();
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
				entries = new Vector<>();
				for (String key : buildAllKeys()) {
					entries.add(getEntry(key, true));
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

		private DynamicEntry getEntry(String key, boolean createKeyWhenMissing) {
			if (key == null) {
				return null;
			}
			DynamicEntry entry = entriesMap.get(key);
			if (entry == null && createKeyWhenMissing) {
				entry = new DynamicEntryImpl(key);
				entriesMap.put(key, entry);
			}
			return entry;
		}

		public void refresh() {
			logger.fine("Refresh called on FMLLocalizedDictionary " + Integer.toHexString(hashCode()));
			entries = null;
			setChanged();
			notifyObservers();
		}

		@Override
		public DynamicEntry addEntry(String aKey) {

			DynamicEntry returned = getEntry(aKey, false);
			if (returned != null) {
				searchTranslation(returned);
				refresh();
			}
			else {
				for (Language l : FlexoLocalization.getAvailableLanguages()) {
					ViewPointLocalizedEntry newEntry = getOwner().getFMLModelFactory().newInstance(ViewPointLocalizedEntry.class);
					newEntry.setLanguage(l.getName());
					newEntry.setKey(aKey);
					newEntry.setValue(aKey);
					addToLocalizedEntries(newEntry);
				}
				entries = null;
				returned = getEntry(aKey, true);
				if (returned != null) {
					searchTranslation(returned);
					refresh();
				}
				else {
					logger.warning("Could not created localization key " + aKey);
				}
			}

			return returned;
		}

		@Override
		public DynamicEntry addEntry() {

			CompoundEdit ce = null;
			FMLModelFactory factory = null;

			if (getOwner() != null) {
				factory = getOwner().getFMLModelFactory();
				if (factory != null) {
					if (!factory.getEditingContext().getUndoManager().isBeeingRecording()) {
						ce = factory.getEditingContext().getUndoManager().startRecording("add_localized_entry");
					}
				}
			}

			String keyToCreate = "key";
			boolean keyAlreadyExists = (getEntry(keyToCreate, false) != null);
			int index = 1;
			while (keyAlreadyExists) {
				keyToCreate = "key" + index;
				keyAlreadyExists = (getEntry(keyToCreate, false) != null);
				index++;
			}

			DynamicEntry returned = addEntry(keyToCreate);

			if (factory != null) {
				if (ce != null) {
					factory.getEditingContext().getUndoManager().stopRecording(ce);
				}
				else if (factory.getEditingContext().getUndoManager().isBeeingRecording()) {
					factory.getEditingContext().getUndoManager()
							.stopRecording(factory.getEditingContext().getUndoManager().getCurrentEdition());
				}
			}

			return returned;
		}

		@Override
		public void deleteEntry(DynamicEntry entry) {
			if (entry.getKey() != null) {
				entriesMap.remove(entry.getKey());
			}
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
			logger.info("Search new entries for " + getOwner());

			CompoundEdit ce = null;
			FMLModelFactory factory = null;

			if (getOwner() != null) {
				factory = getOwner().getFMLModelFactory();
				if (factory != null) {
					if (!factory.getEditingContext().getUndoManager().isBeeingRecording()) {
						ce = factory.getEditingContext().getUndoManager().startRecording("localize_viewpoint");
					}
				}
			}

			handleNewEntry = true;
			getOwner().loadContainedVirtualModelsWhenUnloaded();
			for (VirtualModel vm : getOwner().getVirtualModels()) {
				for (FlexoConcept concept : vm.getFlexoConcepts()) {
					checkAndRegisterLocalized(concept.getName());
					for (FlexoBehaviour es : concept.getFlexoBehaviours()) {
						checkAndRegisterLocalized(es.getLabel());
						checkAndRegisterLocalized(es.getDescription());
						for (FlexoBehaviourParameter p : es.getParameters()) {
							checkAndRegisterLocalized(p.getName());
						}
						for (InspectorEntry entry : concept.getInspector().getEntries()) {
							checkAndRegisterLocalized(entry.getLabel());
						}
					}
				}
			}
			entries = null;
			handleNewEntry = false;

			if (factory != null) {
				if (ce != null) {
					factory.getEditingContext().getUndoManager().stopRecording(ce);
				}
				else if (factory.getEditingContext().getUndoManager().isBeeingRecording()) {
					factory.getEditingContext().getUndoManager()
							.stopRecording(factory.getEditingContext().getUndoManager().getCurrentEdition());
				}
			}

			// getViewPoint().setChanged();
			// getViewPoint().notifyObservers();
		}

		private void checkAndRegisterLocalized(String key) {
			// System.out.println("checkAndRegisterLocalized for " + key);
			if (StringUtils.isEmpty(key)) {
				return;
			}
			if (getEntry(key, false) != null) {
				searchTranslation(getEntry(key, false));
				return;
			}
			else {
				addEntry(key);
			}
		}

		@Override
		public BindingModel getBindingModel() {
			return getOwner().getBindingModel();
		}

		@Override
		public boolean registerNewEntry(String key, Language language, String value) {
			// System.out.println("Register entry key=" + key + " lang=" + language + " value=" + value);
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
			// System.out.println("Retrieving locales to be translated from " + this);
			searchNewEntries();
			getPropertyChangeSupport().firePropertyChange("entries", null, getEntries());
		}

		@Override
		public void searchTranslation(LocalizedEntry entry) {
			if (getParent() != null) {
				String englishTranslation = getParent().localizedForKeyAndLanguage(entry.getKey(), Language.ENGLISH, false);
				if (entry.getKey().equals(englishTranslation)) {
					englishTranslation = automaticEnglishTranslation(entry.getKey());
				}
				entry.setEnglish(englishTranslation);
				String dutchTranslation = getParent().localizedForKeyAndLanguage(entry.getKey(), Language.DUTCH, false);
				if (entry.getKey().equals(dutchTranslation)) {
					dutchTranslation = automaticDutchTranslation(entry.getKey());
				}
				entry.setDutch(dutchTranslation);
				String frenchTranslation = getParent().localizedForKeyAndLanguage(entry.getKey(), Language.FRENCH, false);
				if (entry.getKey().equals(frenchTranslation)) {
					frenchTranslation = automaticFrenchTranslation(entry.getKey());
				}
				entry.setFrench(frenchTranslation);
			}
			else {
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
			public final String getDeletedProperty() {
				return DELETED_PROPERTY;
			}

			@Override
			public void delete() {
				deleteEntry(this);

				/*System.out.println("Suppression de l'entree " + getKey());
				System.out.println("entries=" + entries);
				if (entries != null) {
					System.out.println("contains=" + entries.contains(this));
					entries.remove(this);
				}
				for (Language l : Language.getAvailableLanguages()) {
					ViewPointLocalizedEntry e = getLocalizedEntry(l, key);
					if (e != null) {
						System.out.println("Removing " + e.getValue() + " for key " + key + " language=" + l);
						removeFromLocalizedEntries(e);
					}
				}
				*/
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

			@Override
			public String getEnglish() {
				// The locale might be found in parent localizer
				String returned = localizedForKeyAndLanguage(key, Language.ENGLISH);
				if (returned == null) {
					returned = key;
				}
				return returned;
			}

			@Override
			public void setEnglish(String value) {
				String oldValue = getEnglish();
				if ((value == null && oldValue != null) || (value != null && !value.equals(oldValue))) {
					setLocalizedForKeyAndLanguage(key, value, Language.ENGLISH);
					getPropertyChangeSupport().firePropertyChange("english", oldValue, getEnglish());
				}
			}

			@Override
			public String getFrench() {
				// The locale might be found in parent localizer
				String returned = localizedForKeyAndLanguage(key, Language.FRENCH);
				if (returned == null) {
					returned = key;
				}
				return returned;
			}

			@Override
			public void setFrench(String value) {
				String oldValue = getFrench();
				if ((value == null && oldValue != null) || (value != null && !value.equals(oldValue))) {
					setLocalizedForKeyAndLanguage(key, value, Language.FRENCH);
					getPropertyChangeSupport().firePropertyChange("french", oldValue, getFrench());
				}
			}

			@Override
			public String getDutch() {
				// The locale might be found in parent localizer
				String returned = localizedForKeyAndLanguage(key, Language.DUTCH);
				if (returned == null) {
					returned = key;
				}
				return returned;
			}

			@Override
			public void setDutch(String value) {
				String oldValue = getDutch();
				if ((value == null && oldValue != null) || (value != null && !value.equals(oldValue))) {
					setLocalizedForKeyAndLanguage(key, value, Language.DUTCH);
					getPropertyChangeSupport().firePropertyChange("dutch", oldValue, getDutch());
				}
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
				}
				else {
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
