/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
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
