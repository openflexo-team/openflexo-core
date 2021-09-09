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

import java.beans.PropertyChangeSupport;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.expr.Constant;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.parser.node.AModelDecl;
import org.openflexo.foundation.fml.parser.node.ASingleAnnotationAnnotation;
import org.openflexo.foundation.fml.parser.node.AUseDecl;
import org.openflexo.foundation.fml.rm.CompilationUnitResource.VirtualModelInfo;
import org.openflexo.foundation.technologyadapter.ModelSlot;

/**
 * A visitor allowing to find some infos on a {@link VirtualModel}
 * 
 * @author sylvain
 * 
 */
public class VirtualModelInfoExplorer extends DepthFirstAdapter implements Bindable {

	private final MainSemanticsAnalyzer analyzer;

	private VirtualModelInfo info;

	public VirtualModelInfoExplorer(MainSemanticsAnalyzer analyzer) {
		super();
		this.analyzer = analyzer;
		info = new VirtualModelInfo();

		System.out.println(analyzer.getRawSource().debug());
	}

	public VirtualModelInfo getVirtualModelInfo() {
		return info;
	}

	@Override
	public void inAModelDecl(AModelDecl node) {
		super.inAModelDecl(node);
		info.name = node.getUidentifier().getText();
	}

	@Override
	public void inASingleAnnotationAnnotation(ASingleAnnotationAnnotation node) {
		super.inASingleAnnotationAnnotation(node);

		String key = analyzer.makeFullQualifiedIdentifier(node.getIdentifier());
		DataBinding<?> valueExpression = ExpressionFactory.makeDataBinding(node.getConditionalExp(), this, BindingDefinitionType.GET,
				Object.class, analyzer);
		if (valueExpression.getExpression() instanceof Constant) {
			String text = analyzer.getText(node.getConditionalExp());
			if (text.startsWith("\"") && text.endsWith("\"")) {
				text = text.substring(1, text.length() - 1);
			}
			if (key.equalsIgnoreCase(VirtualModel.URI_KEY)) {
				info.uri = text;
			}
			if (key.equals(VirtualModel.VERSION_KEY)) {
				info.version = text;
			}
			System.out.println("Hop " + key + "=" + text);
			// returned.setSerializationRepresentation(analyzer.getText(node.getConditionalExp()));
		}
		/*else {
			returned.setValueExpression((DataBinding) valueExpression);
		}*/

	}

	@Override
	public void inAUseDecl(AUseDecl node) {
		// TODO Auto-generated method stub
		super.inAUseDecl(node);
		Class<? extends ModelSlot<?>> modelSlotClass = null;
		try {
			modelSlotClass = (Class<? extends ModelSlot<?>>) Class.forName(analyzer.makeFullQualifiedIdentifier(node.getIdentifier()));
			info.requiredModelSlotList = (info.requiredModelSlotList == null ? modelSlotClass.getName()
					: info.requiredModelSlotList + "," + modelSlotClass.getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDeletedProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BindingModel getBindingModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BindingFactory getBindingFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void notifiedBindingChanged(DataBinding<?> dataBinding) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
		// TODO Auto-generated method stub

	}

}
