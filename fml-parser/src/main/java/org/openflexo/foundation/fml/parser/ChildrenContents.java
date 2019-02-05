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

package org.openflexo.foundation.fml.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate.PrettyPrintContext;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.parser.RawSource.RawSourcePosition;
import org.openflexo.toolbox.StringUtils;

/**
 * Specification of the pretty-print of a collection of children objects
 * 
 * @author sylvain
 *
 * @param <T>
 */
public class ChildrenContents<T extends FMLPrettyPrintable> extends PrettyPrintableContents<T> {

	private FMLObjectNode<?, ?> parentNode;
	private Class<T> objectType;
	private Supplier<List<? extends T>> childrenObjectsSupplier;

	private List<FMLObjectNode<?, T>> lastParsedNodes;
	private List<FMLObjectNode<?, T>> childrenNodes;

	private RawSourcePosition defaultInsertionPoint;

	public ChildrenContents(String prelude, Supplier<List<? extends T>> childrenObjects, String postlude, int identationLevel,
			FMLObjectNode<?, ?> parentNode, Class<T> objectType) {
		super(prelude, postlude, identationLevel);
		this.parentNode = parentNode;
		this.objectType = objectType;
		// setFragment(childNode.getLastParsedFragment());

		childrenObjectsSupplier = childrenObjects;

		System.out.println("Tous les children: " + parentNode.getChildren());
		System.out.println("Type: " + objectType);
		lastParsedNodes = new ArrayList<>();
		for (FMLObjectNode<?, ?> objectNode : parentNode.getChildren()) {
			if (TypeUtils.isOfType(objectNode.getFMLObject(), objectType)) {
				lastParsedNodes.add((FMLObjectNode<?, T>) objectNode);
			}
		}
		System.out.println("Tous les nodes qu'on considere: " + lastParsedNodes);

		// RawSourceFragment fragment = null;

		for (FMLObjectNode<?, T> objectNode : lastParsedNodes) {
			System.out.println("> fragment " + objectNode.getLastParsedFragment());
		}

		if (lastParsedNodes.size() > 0) {
			defaultInsertionPoint = lastParsedNodes.get(0).getLastParsedFragment().getStartPosition();
		}
		else {
			defaultInsertionPoint = parentNode.getDefaultInsertionPoint();
		}

	}

	public List<FMLObjectNode<?, T>> getChildrenNodes() {
		return childrenNodes;
	}

	@Override
	public String getNormalizedPrettyPrint(PrettyPrintContext context) {
		StringBuffer sb = new StringBuffer();

		for (T childObject : childrenObjectsSupplier.get()) {
			FMLObjectNode<?, T> childNode = parentNode.getObjectNode(childObject);
			if (childNode == null) {
				childNode = parentNode.makeObjectNode(childObject);
				parentNode.addToChildren(childNode);
			}
			String childPrettyPrint = childNode.getNormalizedFMLRepresentation(context.derive(getRelativeIndentation()));
			if (StringUtils.isNotEmpty(childPrettyPrint)) {
				if (StringUtils.isNotEmpty(getPrelude())) {
					sb.append(getPrelude());
				}
				sb.append(childPrettyPrint);
				if (StringUtils.isNotEmpty(getPostlude())) {
					sb.append(getPostlude());
				}
			}
		}

		return sb.toString();
	}

	@Override
	public void updatePrettyPrint(DerivedRawSource derivedRawSource, PrettyPrintContext context) {

		System.out.println("Tous les children: " + parentNode.getChildren());
		System.out.println("Type: " + objectType);
		List<FMLObjectNode<?, T>> nodesToBeRemoved = new ArrayList<>();
		nodesToBeRemoved.addAll(lastParsedNodes);

		System.out.println("Tous les nodes qu'on considere: " + nodesToBeRemoved);

		RawSourcePosition insertionPoint = defaultInsertionPoint;

		System.out.println("Insertion point pour commencer: " + insertionPoint);

		PrettyPrintContext derivedContext = context.derive(getRelativeIndentation());

		for (T childObject : childrenObjectsSupplier.get()) {
			FMLObjectNode<?, T> childNode = parentNode.getObjectNode(childObject);
			if (childNode == null) {
				childNode = parentNode.makeObjectNode(childObject);
				parentNode.addToChildren(childNode);
				System.out.println("Nouveau childNode for " + childObject);
				System.out.println("ASTNode " + childNode.getASTNode());
				System.out.println("FML= " + childNode.getFMLRepresentation(context));
				String insertThis = (getPrelude() != null ? getPrelude() : "") + childNode.getFMLRepresentation(derivedContext)
						+ (getPostlude() != null ? getPostlude() : "");
				derivedRawSource.insert(insertionPoint, insertThis);
			}
			else {
				if (lastParsedNodes.contains(childNode)) {
					// OK, this is an update
					derivedRawSource.replace(childNode.getLastParsedFragment(), childNode.getFMLRepresentation(context));
					insertionPoint = childNode.getLastParsedFragment().getEndPosition();
				}
				else {
					String insertThis = (getPrelude() != null ? getPrelude() : "") + childNode.getFMLRepresentation(derivedContext)
							+ (getPostlude() != null ? getPostlude() : "");
					derivedRawSource.insert(insertionPoint, insertThis);
				}
			}
			nodesToBeRemoved.remove(childNode);
		}

		for (FMLObjectNode<?, T> removedNode : nodesToBeRemoved) {
			derivedRawSource.remove(removedNode.getLastParsedFragment());
		}

		/*FMLObjectNode<?, ?> childNode = getObjectNode(childObject);
		if (childNode == null) {
			childNode = makeObjectNode(childObject);
			addToChildren(childNode);
		}*/

		/*System.out.println("> Pour ChildContents " + childNode.getFMLObject() + " c'est plus complique");
		System.out.println("Et on calcule la nouvelle valeur:");
		derivedRawSource.replace(getFragment(), childNode.computeFMLRepresentation(context));*/
	}

}
