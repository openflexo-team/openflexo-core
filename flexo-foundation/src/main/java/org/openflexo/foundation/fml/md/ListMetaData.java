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

package org.openflexo.foundation.fml.md;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * A {@link ListMetaData} represent a list of meta-data associated with a key
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(ListMetaData.ListMetaDataImpl.class)
@XMLElement
public interface ListMetaData extends FMLMetaData {

	@PropertyIdentifier(type = FMLMetaData.class, cardinality = Cardinality.LIST)
	public static final String METADATA_LIST_KEY = "metaDataList";

	/**
	 * Return list of meta-data declared for this object
	 * 
	 * @return
	 */
	@Getter(value = METADATA_LIST_KEY, cardinality = Cardinality.LIST, inverse = FMLMetaData.OWNER_KEY)
	public List<FMLMetaData> getMetaDataList();

	@Adder(METADATA_LIST_KEY)
	public void addToMetaDataList(FMLMetaData metaData);

	@Remover(METADATA_LIST_KEY)
	public void removeFromMetaDataList(FMLMetaData metaData);

	public FMLMetaData getSingleMetaData(String key);

	public List<FMLMetaData> getMultipleMetaData(String key);

	public List<FMLMetaData> getMultipleMetaData(List<String> keys);

	public static abstract class ListMetaDataImpl extends FMLMetaDataImpl implements ListMetaData {

		@Override
		public void setKey(String aKey) {
			performSuperSetter(KEY_KEY, aKey);
			if (aKey.equals("TextField")) {
				System.out.println("Ca vient d'ou");
				Thread.dumpStack();
				System.exit(-1);
			}
		}

		@Override
		public List<FMLMetaData> getMetaData() {
			return getMetaDataList();
		}

		@Override
		public void addToMetaData(FMLMetaData metaData) {
			addToMetaDataList(metaData);
		}

		@Override
		public void removeFromMetaData(FMLMetaData metaData) {
			removeFromMetaDataList(metaData);
		}

		@Override
		public FMLMetaData getMetaData(String key) {
			return getSingleMetaData(key);
		}

		@Override
		public FMLMetaData getSingleMetaData(String key) {
			for (FMLMetaData metaData : getMetaDataList()) {
				if (metaData.getKey().equals(key)) {
					return metaData;
				}
			}
			return null;
		}

		@Override
		public List<FMLMetaData> getMultipleMetaData(String key) {
			List<FMLMetaData> returned = new ArrayList<>();
			for (FMLMetaData metaData : getMetaDataList()) {
				if (metaData.getKey().equals(key)) {
					returned.add(metaData);
				}
			}
			return returned;
		}

		@Override
		public List<FMLMetaData> getMultipleMetaData(List<String> keys) {
			List<FMLMetaData> returned = new ArrayList<>();
			for (FMLMetaData metaData : getMetaDataList()) {
				for (String key : keys) {
					if (metaData.getKey().equals(key)) {
						returned.add(metaData);
					}
				}
			}
			return returned;
		}

		@Override
		public <T> T getSingleMetaData(String key, Class<T> type) {
			if (getSingleMetaData(key) instanceof SingleMetaData) {
				return ((SingleMetaData<T>) getMetaData(key)).getValue(type);
			}
			return null;
		}

		@Override
		public <T> void setSingleMetaData(String key, T value, Class<T> type) {
			if (value != null) {
				if (getMetaData(key) instanceof SingleMetaData) {
					((SingleMetaData<T>) getMetaData(key)).setValue(value, type);
				}
				else {
					SingleMetaData<T> newMD = getFMLModelFactory().newSingleMetaData(key, value, type);
					addToMetaDataList(newMD);
				}
			}
			else {
				if (getMetaData(key) != null) {
					removeFromMetaDataList(getMetaData(key));
				}
			}
		}

	}

}
