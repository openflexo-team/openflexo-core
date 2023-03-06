/**
 * 
 * Copyright (c) 2014, Openflexo
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

import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.DeletionSchemeActionFactory;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

@ModelEntity
@ImplementationClass(DeletionScheme.DeletionSchemeImpl.class)
@XMLElement
public interface DeletionScheme extends AbstractActionScheme {

	public static final String DEFAULT_DELETION_SCHEME_NAME = "DefaultDeletionScheme";

	@PropertyIdentifier(type = boolean.class)
	public static final String IS_ANONYMOUS_KEY = "isAnonymous";

	@Getter(value = IS_ANONYMOUS_KEY)
	@XMLAttribute
	public Boolean isAnonymous();

	@Setter(IS_ANONYMOUS_KEY)
	public void setAnonymous(Boolean isAnonymous);

	public static abstract class DeletionSchemeImpl extends AbstractActionSchemeImpl implements DeletionScheme {

		private Boolean isAnonymous;

		@Override
		public Boolean isAnonymous() {
			if (isAnonymous == null) {
				String name = (String) performSuperGetter(FMLObject.NAME_KEY);
				isAnonymous = (name == null || name.equals(DEFAULT_DELETION_SCHEME_NAME));
			}
			return isAnonymous;
		}

		@Override
		public void setAnonymous(Boolean isAnonymous) {
			this.isAnonymous = isAnonymous;
		}

		@Override
		public String getName() {
			if (isAnonymous()) {
				return DEFAULT_DELETION_SCHEME_NAME;
			}
			return super.getName();
		}

		@Override
		public void setName(String name) throws InvalidNameException {
			super.setName(name);
			if (name != null && name.equals(DEFAULT_DELETION_SCHEME_NAME)) {
				isAnonymous = true;
			}
		}

		@Override
		protected String getDisplayName() {
			if (isAnonymous()) {
				return "delete";
			}
			return super.getDisplayName();
		}

		@Override
		public DeletionSchemeActionFactory getActionFactory(FlexoConceptInstance fci) {
			return new DeletionSchemeActionFactory(this, fci);
		}

	}
}
