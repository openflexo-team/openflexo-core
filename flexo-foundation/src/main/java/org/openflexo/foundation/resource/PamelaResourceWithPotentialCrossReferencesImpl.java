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

package org.openflexo.foundation.resource;

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.PamelaResourceModelFactory;
import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * Default implementation for a {@link PamelaResource} implementing {@link ResourceWithPotentialCrossReferences} (a resource potentially
 * requiring cross-references and that should be loaded using a two-passes algorithm)
 * 
 * @param <RD>
 *            the type of the resource data referenced by this resource
 * @author Sylvain
 * 
 */
public abstract class PamelaResourceWithPotentialCrossReferencesImpl<RD extends ResourceData<RD> & AccessibleProxyObject, F extends PamelaModelFactory & PamelaResourceModelFactory>
		extends PamelaResourceImpl<RD, F> implements ResourceWithPotentialCrossReferences<RD> {

	private static final Logger logger = Logger.getLogger(PamelaResourceWithPotentialCrossReferencesImpl.class.getPackage().getName());

	public abstract <I> void forceUpdateDependencies(FlexoResourceCenter<I> resourceCenter);

	/**
	 * Returns the &quot;real&quot; resource data of this resource. This may cause the loading of the resource data.
	 * 
	 * Implements the two-passes algorithm required to handle potential cross references
	 * 
	 * @return the resource data.
	 * @throws ResourceLoadingCancelledException
	 */
	/*	@Override
		public RD getResourceData()
				throws ResourceLoadingCancelledException, ResourceLoadingCancelledException, FileNotFoundException, FlexoException {
	
			if (isDeleted()) {
				return null;
			}
			if (isUnloading) {
				return null;
			}
			if (isLoading()) {
				// Avoid stack overflow, but this should never happen
				logger.warning("Preventing StackOverflow while loading resource " + this);
				Thread.dumpStack();
				return resourceData;
			}
	
			if (resourceData == null && isLoadable() && !isLoading()) {
				// The resourceData is null, we try to load it
			
				// Make sure all the dependencies are up-to-date
				forceUpdateDependencies(getResourceCenter());
			
				// Now load the non cross-reference dependencies
				for (FlexoResource<?> dependency : getNonCrossReferenceDependencies()) {
					// System.out.println("While loaading " + this + " load non cross-referenced dependency " + dependency);
					dependency.loadResourceData();
				}
			
				logger.info("Bon j'arrive la pour charger " + this + " isLoading=" + isLoading());
			
				// Then really load
				setLoading(true);
			
				// Applies the two-passes algorithm
			
				// First pass on this resource
				resourceData = ((ResourceWithPotentialCrossReferences<RD>) this).initializeLoadResourceData();
			
				// Now first pass on cross-reference dependencies
				for (ResourceWithPotentialCrossReferences<?> dependency : getCrossReferenceDependencies()) {
					// System.out.println("While loading " + this + " load cross-referenced dependency " + dependency);
					dependency.initializeLoadResourceData();
				}
			
				// Now second pass on cross-reference dependencies
				for (ResourceWithPotentialCrossReferences<?> dependency : getCrossReferenceDependencies()) {
					dependency.finalizeLoadResourceData();
				}
			
				// Second pass on this resource
				((ResourceWithPotentialCrossReferences<RD>) this).finalizeLoadResourceData();
			
				setLoading(false);
			}
			return resourceData;
	
			return performLoadResourceData();
	
		}*/

	/**
	 * Internally used to perform loading of this {@link FlexoResource}
	 * 
	 * Implements the two-passes algorithm required to handle potential cross references
	 * 
	 * This method is multi-thread safe and prevent loading from multiple threads (synchronized keyword)
	 * 
	 * @return
	 * @throws ResourceLoadingCancelledException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 * @throws FlexoException
	 */

	@Override
	protected synchronized RD performLoadResourceData()
			throws ResourceLoadingCancelledException, ResourceLoadingCancelledException, FileNotFoundException, FlexoException {

		if (resourceData != null || isLoading()) {
			return resourceData;
		}
		if (resourceData == null && isLoadable() && !isLoading()) {
			// The resourceData is null, we try to load it

			// Make sure all the dependencies are up-to-date
			forceUpdateDependencies(getResourceCenter());

			// Now load the non cross-reference dependencies
			for (FlexoResource<?> dependency : getNonCrossReferenceDependencies()) {
				// System.out.println("While loaading " + this + " load non cross-referenced dependency " + dependency);
				dependency.loadResourceData();
			}

			// Then really load
			setLoading(true);

			// Applies the two-passes algorithm

			// First pass on this resource
			resourceData = ((ResourceWithPotentialCrossReferences<RD>) this).initializeLoadResourceData();

			// Now first pass on cross-reference dependencies
			for (ResourceWithPotentialCrossReferences<?> dependency : getCrossReferenceDependencies()) {
				// System.out.println("While loading " + this + " load cross-referenced dependency " + dependency);
				dependency.initializeLoadResourceData();
			}

			// Now second pass on cross-reference dependencies
			for (ResourceWithPotentialCrossReferences<?> dependency : getCrossReferenceDependencies()) {
				dependency.finalizeLoadResourceData();
			}

			// Second pass on this resource
			((ResourceWithPotentialCrossReferences<RD>) this).finalizeLoadResourceData();

			setLoading(false);
		}
		return resourceData;

	}

}
