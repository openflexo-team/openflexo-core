/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.icon;

import javax.swing.ImageIcon;

import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.ImageIconResource;

public class JavaIconLibrary {
	

	public static final ImageIcon FJP_JAVA_FILE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/JavaFile.gif"));
	public static final ImageIcon FJP_PACKAGE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/Package.gif"));
	public static final ImageIcon FJP_IMPORTS_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/Imports.gif"));
	public static final ImageIcon FJP_IMPORT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/Import.gif"));
	public static final ImageIcon FJP_STATIC_MARKER_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/StaticMarker.gif"));
	public static final ImageIcon FJP_FINAL_MARKER_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/FinalMarker.gif"));
	public static final ImageIcon FJP_SYNCHRONIZED_MARKER_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/SynchronizedMarker.gif"));
	public static final ImageIcon FJP_ABSTRACT_MARKER_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/AbstractMarker.gif"));
	public static final ImageIcon FJP_CONSTRUCTOR_MARKER_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/ConstructorMarker.gif"));
	public static final ImageIcon FJP_CLASS_DEFAULT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/ClassDefault.gif"));
	public static final ImageIcon FJP_CLASS_PUBLIC_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/ClassPublic.gif"));
	public static final ImageIcon FJP_CLASS_PROTECTED_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/ClassProtected.gif"));
	public static final ImageIcon FJP_CLASS_PRIVATE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/ClassPrivate.gif"));
	public static final ImageIcon FJP_INTERFACE_DEFAULT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/InterfaceDefault.gif"));
	public static final ImageIcon FJP_INTERFACE_PUBLIC_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/InterfacePublic.gif"));
	public static final ImageIcon FJP_INTERFACE_PROTECTED_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/InterfaceProtected.gif"));
	public static final ImageIcon FJP_INTERFACE_PRIVATE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/InterfacePrivate.gif"));
	public static final ImageIcon FJP_ENUM_DEFAULT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/EnumDefault.gif"));
	public static final ImageIcon FJP_ENUM_PUBLIC_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/EnumPublic.gif"));
	public static final ImageIcon FJP_ENUM_PROTECTED_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/EnumProtected.gif"));
	public static final ImageIcon FJP_ENUM_PRIVATE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/EnumPrivate.gif"));
	public static final ImageIcon FJP_METHOD_DEFAULT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/MethodDefault.gif"));
	public static final ImageIcon FJP_METHOD_PUBLIC_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/MethodPublic.gif"));
	public static final ImageIcon FJP_METHOD_PROTECTED_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/MethodProtected.gif"));
	public static final ImageIcon FJP_METHOD_PRIVATE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/MethodPrivate.gif"));
	public static final ImageIcon FJP_FIELD_DEFAULT_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/FieldDefault.gif"));
	public static final ImageIcon FJP_FIELD_PUBLIC_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/FieldPublic.gif"));
	public static final ImageIcon FJP_FIELD_PROTECTED_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/FieldProtected.gif"));
	public static final ImageIcon FJP_FIELD_PRIVATE_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Java/FieldPrivate.gif"));

	public static final IconMarker STATIC_MARKER = new IconMarker(FJP_STATIC_MARKER_ICON, 12, 0);
	public static final IconMarker ABSTRACT_MARKER = new IconMarker(FJP_ABSTRACT_MARKER_ICON, 17, 0);
	public static final IconMarker FINAL_MARKER = new IconMarker(FJP_FINAL_MARKER_ICON, 17, 0);
	public static final IconMarker CONSTRUCTOR_MARKER = new IconMarker(FJP_CONSTRUCTOR_MARKER_ICON, 17, 0);
	public static final IconMarker SYNCHRONIZED_MARKER = new IconMarker(FJP_SYNCHRONIZED_MARKER_ICON, 12, 10);

}
