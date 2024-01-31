/*
 * 03/21/2010
 *
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSTALanguageSupport.License.txt file for details.
 */
package org.openflexo.fml.rstasupport.classreader.constantpool;

import java.io.DataInputStream;
import java.io.IOException;

import org.openflexo.fml.rstasupport.classreader.ClassFile;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantClassInfo;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantDoubleInfo;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantFieldrefInfo;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantFloatInfo;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantIntegerInfo;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantInterfaceMethodrefInfo;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantInvokeDynamicInfo;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantLongInfo;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantMethodHandleInfo;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantMethodTypeInfo;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantMethodrefInfo;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantNameAndTypeInfo;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantPoolInfo;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantStringInfo;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantTypes;
import org.openflexo.fml.rstasupport.classreader.constantpool.ConstantUtf8Info;
import org.openflexo.fml.rstasupport.classreader.*;


/**
 * A factory for constant pool information.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public final class ConstantPoolInfoFactory implements ConstantTypes {


	/**
	 * Private constructor to prevent instantiation.
	 */
	private ConstantPoolInfoFactory() {
	}


	/**
	 * Reads constant pool information.
	 *
	 * @param cf The class file being parsed.
	 * @param in The input stream to read from.
	 * @return The next constant pool record.
	 * @throws IOException If an IO error occurs.
	 */
	public static ConstantPoolInfo readConstantPoolInfo(ClassFile cf,
							DataInputStream in) throws IOException {

		ConstantPoolInfo cpi;
		int tag = in.read();

		switch (tag) {

			case CONSTANT_Class:
				int nameIndex = in.readUnsignedShort();
				cpi = new ConstantClassInfo(nameIndex);
				break;

			case CONSTANT_Double:
				int highBytes = in.readInt();
				int lowBytes = in.readInt();
				cpi = new ConstantDoubleInfo(highBytes, lowBytes);
				break;

			case CONSTANT_Fieldref:
				int classIndex = in.readUnsignedShort();
				int nameAndTypeIndex = in.readUnsignedShort();
				cpi = new ConstantFieldrefInfo(classIndex, nameAndTypeIndex);
				break;

			case CONSTANT_Float:
				int bytes = in.readInt();
				cpi = new ConstantFloatInfo(bytes);
				break;

			case CONSTANT_Integer:
				bytes = in.readInt();
				cpi = new ConstantIntegerInfo(bytes);
				break;

			case CONSTANT_InterfaceMethodref:
				classIndex = in.readUnsignedShort();
				nameAndTypeIndex = in.readUnsignedShort();
				cpi = new ConstantInterfaceMethodrefInfo(classIndex, nameAndTypeIndex);
				break;

			case CONSTANT_Long:
				highBytes = in.readInt();
				lowBytes = in.readInt();
				cpi = new ConstantLongInfo(highBytes, lowBytes);
				break;

			case CONSTANT_Methodref:
				classIndex = in.readUnsignedShort();
				nameAndTypeIndex = in.readUnsignedShort();
				cpi = new ConstantMethodrefInfo(classIndex, nameAndTypeIndex);
				break;

			case CONSTANT_NameAndType:
				nameIndex = in.readUnsignedShort();
				int descriptorIndex = in.readUnsignedShort();
				cpi = new ConstantNameAndTypeInfo(nameIndex, descriptorIndex);
				break;

			case CONSTANT_String:
				int stringIndex = in.readUnsignedShort();
				cpi = new ConstantStringInfo(cf, stringIndex);
				break;

			case CONSTANT_Utf8:
				int count = in.readUnsignedShort();
				byte[] byteArray = new byte[count];
				in.readFully(byteArray);
				cpi = new ConstantUtf8Info(byteArray);
				break;

			case CONSTANT_MethodHandle:
			    int referenceKind = in.read();
			    int referenceIndex = in.readUnsignedShort();
			    cpi = new ConstantMethodHandleInfo(referenceKind, referenceIndex);
			    break;

			case CONSTANT_MethodType:
			    descriptorIndex = in.readUnsignedShort();
			    cpi = new ConstantMethodTypeInfo(descriptorIndex);
			    break;

			case CONSTANT_InvokeDynamic:
			    int bootstrapMethodAttrIndex = in.readUnsignedShort();
			    nameAndTypeIndex = in.readUnsignedShort();
			    cpi = new ConstantInvokeDynamicInfo(bootstrapMethodAttrIndex, nameAndTypeIndex);
				break;

			default:
				throw new IOException("Unknown tag for constant pool info: " + tag);

		}

		return cpi;

	}


}
