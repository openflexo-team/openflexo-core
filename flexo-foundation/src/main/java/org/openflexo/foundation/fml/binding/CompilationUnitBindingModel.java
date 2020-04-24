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

package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.NamespaceDeclaration;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.toolbox.StringUtils;

/**
 * This is the {@link BindingModel} exposed by a {@link FMLCompilationUnit}<br>
 * 
 * Provides access to the named imports (see {@link ElementImportDeclaration}
 * 
 * Note that default {@link RunTimeEvaluationContext} corresponding to this {@link BindingModel} is
 * {@link FMLCompilationUnit#getReflectedBindingEvaluationContext()}
 * 
 * 
 * @author sylvain
 * 
 */
public class CompilationUnitBindingModel extends BindingModel {

	private final FMLCompilationUnit compilationUnit;

	private final Map<ElementImportDeclaration, NamedImportBindingVariable> namedImportVariablesMap;
	private final Map<NamespaceDeclaration, NamespaceBindingVariable> namespaceVariablesMap;

	public CompilationUnitBindingModel(FMLCompilationUnit compilationUnit) {
		super();
		this.compilationUnit = compilationUnit;
		if (compilationUnit != null && compilationUnit.getPropertyChangeSupport() != null) {
			compilationUnit.getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		namespaceVariablesMap = new HashMap<>();
		namedImportVariablesMap = new HashMap<>();

		updateNamespaceVariables();
		updateNamedImportsVariables();
	}

	public FMLCompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == getBaseBindingModel()) {
			updateNamedImportsVariables();
		}
		if (evt.getSource() == compilationUnit) {
			if (evt.getPropertyName().equals(FMLCompilationUnit.ELEMENT_IMPORTS_KEY)) {
				updateNamedImportsVariables();
			}
			if (evt.getPropertyName().equals(FMLCompilationUnit.NAMESPACES_KEY)) {
				updateNamespaceVariables();
			}
		}
	}

	protected void updateNamedImportsVariables() {

		List<ElementImportDeclaration> importsToBeDeleted = new ArrayList<>(namedImportVariablesMap.keySet());

		for (ElementImportDeclaration importDeclaration : compilationUnit.getElementImports()) {
			if (importsToBeDeleted.contains(importDeclaration)) {
				importsToBeDeleted.remove(importDeclaration);
			}
			else if (namedImportVariablesMap.get(importDeclaration) == null && StringUtils.isNotEmpty(importDeclaration.getAbbrev())) {
				NamedImportBindingVariable bv = new NamedImportBindingVariable(importDeclaration);
				addToBindingVariables(bv);
				namedImportVariablesMap.put(importDeclaration, bv);
			}
		}

		for (ElementImportDeclaration r : importsToBeDeleted) {
			NamedImportBindingVariable bvToRemove = namedImportVariablesMap.get(r);
			removeFromBindingVariables(bvToRemove);
			namedImportVariablesMap.remove(r);
			bvToRemove.delete();
		}
	}

	protected void updateNamespaceVariables() {

		List<NamespaceDeclaration> nsToBeDeleted = new ArrayList<>(namespaceVariablesMap.keySet());

		for (NamespaceDeclaration nsDeclaration : compilationUnit.getNamespaces()) {
			if (nsToBeDeleted.contains(nsDeclaration)) {
				nsToBeDeleted.remove(nsDeclaration);
			}
			else if (namespaceVariablesMap.get(nsDeclaration) == null && StringUtils.isNotEmpty(nsDeclaration.getAbbrev())) {
				NamespaceBindingVariable bv = new NamespaceBindingVariable(nsDeclaration);
				addToBindingVariables(bv);
				namespaceVariablesMap.put(nsDeclaration, bv);
			}
		}

		for (NamespaceDeclaration r : nsToBeDeleted) {
			NamespaceBindingVariable bvToRemove = namespaceVariablesMap.get(r);
			removeFromBindingVariables(bvToRemove);
			namespaceVariablesMap.remove(r);
			bvToRemove.delete();
		}
	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		if (compilationUnit != null && compilationUnit.getPropertyChangeSupport() != null) {
			compilationUnit.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}
}
