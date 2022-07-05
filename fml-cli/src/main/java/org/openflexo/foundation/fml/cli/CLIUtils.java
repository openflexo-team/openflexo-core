/*
 * CommandInterpreter.java -  Provide the basic command line interface.
 *
 * Copyright (c) 1996 Chuck McManis, All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies.
 *
 * CHUCK MCMANIS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. CHUCK MCMANIS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package org.openflexo.foundation.fml.cli;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.DirectoryBasedIODelegate;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.ResourceData;

/**
 * Utilities for FML command-line interpreter
 */
public class CLIUtils {

	/**
	 * Generic method used to denote a FlexoObject in the context of FML command-line interpreter
	 * 
	 * @param object
	 * @return
	 */
	public static String denoteObject(Object object) {
		if (object instanceof ResourceData) {
			if (((ResourceData<?>) object).getResource().getIODelegate() instanceof DirectoryBasedIODelegate) {
				return ((DirectoryBasedIODelegate) ((ResourceData<?>) object).getResource().getIODelegate()).getDirectory().getName();
			}
		}
		if (object instanceof RepositoryFolder && ((RepositoryFolder) object).getSerializationArtefact() instanceof File) {
			return ((File) ((RepositoryFolder) object).getSerializationArtefact()).getName();
		}
		else if (object instanceof VirtualModel) {
			return ((VirtualModel) object).getName() + ".fml";
		}
		else if (object instanceof FlexoConcept) {
			return ((FlexoConcept) object).getDeclaringCompilationUnit().getName() + ".fml/" + ((FlexoConcept) object).getName();
		}
		else if (object instanceof FlexoConceptInstance) {
			return ((FlexoConceptInstance) object).getUserFriendlyIdentifier();
		}
		if (object != null) {
			return object.toString();
		}
		return "null";
	}

	/**
	 * Generic method used to denote a FlexoObject path in the context of FML command-line interpreter
	 * 
	 * @param object
	 * @return
	 */
	public static String denoteObjectPath(Object object) {
		if (object instanceof VirtualModel) {
			return ((VirtualModel) object).getName() + ".fml";
		}
		else if (object instanceof FlexoConcept) {
			return ((FlexoConcept) object).getDeclaringCompilationUnit().getName() + ".fml/" + ((FlexoConcept) object).getName();
		}
		else if (object instanceof VirtualModelInstance) {
			VirtualModelInstance<?, ?> vmi = (VirtualModelInstance<?, ?>) object;
			if (vmi.getContainerVirtualModelInstance() != null) {
				return denoteObjectPath(vmi.getContainerVirtualModelInstance()) + " > " + denoteObject(vmi);
			}
			return denoteObject(vmi);
		}
		else if (object instanceof FlexoConceptInstance) {
			FlexoConceptInstance fci = (FlexoConceptInstance) object;
			if (fci.getContainerFlexoConceptInstance() != null) {
				return denoteObjectPath(fci.getContainerFlexoConceptInstance()) + " > " + fci.getUserFriendlyIdentifier();
			}
			else if (fci.getOwningVirtualModelInstance() != null) {
				return denoteObjectPath(fci.getOwningVirtualModelInstance()) + " > " + denoteObject(fci);
			}
			return denoteObject(fci);
		}
		if (object != null) {
			return object.toString();
		}
		return "null";
	}

	/**
	 * Generic method used to retrieve type of a FlexoObject in the context of FML command-line interpreter
	 * 
	 * @param object
	 * @return
	 */
	public static Type typeOf(Object object) {
		if (object instanceof VirtualModel) {
			return VirtualModel.class;
		}
		else if (object instanceof FlexoConcept) {
			return FlexoConcept.class;
		}
		else if (object instanceof VirtualModelInstance) {
			return ((VirtualModelInstance<?, ?>) object).getVirtualModel().getVirtualModelInstanceType();
		}
		else if (object instanceof FlexoConceptInstance) {
			return ((FlexoConceptInstance) object).getFlexoConcept().getInstanceType();
		}
		return object.getClass();
	}

	/**
	 * Generic method used to render a FlexoObject in the context of FML command-line interpreter
	 * 
	 * @param object
	 * @return
	 */
	public static String renderObject(FlexoObject object) {
		if (object != null) {
			return object.render();
		}
		return "null";
	}

	/**
	 * Generic method used to list contained FlexoObject in the context of FML command-line interpreter
	 * 
	 * @param object
	 * @return
	 */
	public static List<? extends FlexoObject> getContainedObjects(FlexoObject object) {
		if (object instanceof VirtualModelInstance) {
			return ((VirtualModelInstance<?, ?>) object).getAllRootFlexoConceptInstances();
		}
		else if (object instanceof FlexoConceptInstance) {
			return ((FlexoConceptInstance) object).getEmbeddedFlexoConceptInstances();
		}

		return null;
	}
}
