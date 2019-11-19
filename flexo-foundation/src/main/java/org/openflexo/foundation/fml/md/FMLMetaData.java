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

import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;

/**
 * Represents a meta-data related to a FMLObject
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = false)
@ImplementationClass(FMLMetaData.FMLMetaDataImpl.class)
@Imports({ @Import(SingleMetaData.class), @Import(BasicMetaData.class), @Import(MultiValuedMetaData.class), @Import(ListMetaData.class) })
public interface FMLMetaData extends FMLObject, FMLPrettyPrintable {

	@PropertyIdentifier(type = String.class)
	public static final String KEY_KEY = "key";
	@PropertyIdentifier(type = FMLObject.class)
	public static final String OWNER_KEY = "owner";

	@Getter(value = KEY_KEY)
	public String getKey();

	@Setter(KEY_KEY)
	public void setKey(String aKey);

	@Getter(value = OWNER_KEY, ignoreForEquality = true)
	public FMLObject getOwner();

	@Setter(OWNER_KEY)
	public void setOwner(FMLObject anObject);

	public static abstract class FMLMetaDataImpl extends FMLObjectImpl implements FMLMetaData {

		@Override
		public String getURI() {
			// Not applicable
			// TODO
			return null;
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			// Not applicable
			// TODO
			return null;
		}

		@Override
		public BindingModel getBindingModel() {
			if (getOwner() != null) {
				return getOwner().getBindingModel();
			}
			return null;
		}

		@Override
		public FMLCompilationUnit getResourceData() {
			if (getOwner() != null) {
				return getOwner().getResourceData();
			}
			return null;
		}

	}

}
