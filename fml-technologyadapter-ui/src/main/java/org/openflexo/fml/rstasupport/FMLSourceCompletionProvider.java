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
package org.openflexo.fml.rstasupport;

import java.awt.Cursor;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DefaultBindable;
import org.openflexo.connie.binding.FunctionPathElement;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.fml.controller.FMLTechnologyAdapterController;
import org.openflexo.fml.rstasupport.buildpath.LibraryInfo;
import org.openflexo.fml.rstasupport.buildpath.SourceLocation;
import org.openflexo.fml.rstasupport.classreader.ClassFile;
import org.openflexo.fml.rstasupport.classreader.FieldInfo;
import org.openflexo.fml.rstasupport.classreader.MemberInfo;
import org.openflexo.fml.rstasupport.classreader.MethodInfo;
import org.openflexo.fml.rstasupport.rjc.ast.CodeBlock;
import org.openflexo.fml.rstasupport.rjc.ast.FormalParameter;
import org.openflexo.fml.rstasupport.rjc.ast.LocalVariable;
import org.openflexo.fml.rstasupport.rjc.ast.Method;
import org.openflexo.fml.rstasupport.rjc.ast.TypeDeclaration;
import org.openflexo.fml.rstasupport.rjc.lang.Type;
import org.openflexo.fml.rstasupport.rjc.lang.TypeArgument;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.NamespaceDeclaration;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.md.FMLMetaData;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoBehaviourNode;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.p2pp.RawSource.RawSourcePosition;
import org.openflexo.toolbox.StringUtils;

/**
 * Parses a Java AST for code completions. It currently scans the following:
 *
 * <ul>
 * <li>Import statements
 * <li>Method names
 * <li>Field names
 * </ul>
 *
 * Also, if the caret is inside a method, local variables up to the caret position are also returned.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FMLSourceCompletionProvider extends DefaultCompletionProvider {

	protected static final Logger logger = FlexoLogger.getLogger(FMLSourceCompletionProvider.class.getPackage().getName());

	/**
	 * The parent completion provider.
	 */
	private FMLCompletionProvider fmlProvider;

	/**
	 * Used to get information about what classes match imports.
	 */
	private JarManager jarManager;

	/**
	 * The {@link FMLTechnologyAdapterController}
	 */
	private final FMLTechnologyAdapterController fmlTAController;

	private static final String JAVA_LANG_PACKAGE = "java.lang.*";
	private static final String THIS = "this";

	// Shorthand completions (templates and comments)
	private FMLDeclarationsShorthandCompletionCache declShorthandCache;
	private FMLControlGraphShorthandCompletionCache cgShorthandCache;

	/**
	 * Constructor.
	 *
	 * @param jarManager
	 *            The jar manager for this provider.
	 */
	FMLSourceCompletionProvider(JarManager jarManager, FMLTechnologyAdapterController fmlTAController) {
		this.fmlTAController = fmlTAController;
		if (jarManager == null) {
			jarManager = new JarManager();
		}
		this.jarManager = jarManager;
		setParameterizedCompletionParams('(', ", ", ')');
		setAutoActivationRules(false, "."); // Default - only activate after '.'
		setParameterChoicesProvider(new SourceParamChoicesProvider());
	}

	public FMLTechnologyAdapterController getFMLTechnologyAdapterController() {
		return fmlTAController;
	}

	public FlexoServiceManager getServiceManager() {
		if (getFMLTechnologyAdapterController() != null) {
			return getFMLTechnologyAdapterController().getServiceManager();
		}
		return null;
	}

	public FMLCompletionProvider getFMLProvider() {
		return fmlProvider;
	}

	public FMLCompilationUnit getCompilationUnit() {
		return getFMLProvider().getCompilationUnit();
	}

	/**
	 * Represents the context in which a completion is computed
	 * 
	 * It relies on the last parsed FML object where the caret is located
	 * 
	 */
	class CompletionContext {
		final FMLObjectNode<?, FMLPrettyPrintable, ?> enclosingObjectNode;
		final Bindable context;

		/**
		 * Build a CompletionContext based on the caret location
		 * 
		 * @param cu
		 * @param textArea
		 */
		CompletionContext(FMLCompilationUnit cu, JTextComponent textArea) {
			this.enclosingObjectNode = getEnclosingFMLObjectNode(cu, textArea);
			if (enclosingObjectNode != null) {
				FMLPrettyPrintable enclosingObject = enclosingObjectNode.getModelObject();
				System.out.println("enclosingObject=" + enclosingObject);
				if (enclosingObject instanceof Sequence) {
					// System.out.println("Returning FML:\n" + (((Sequence) enclosingObject).getControlGraph1()).getFMLPrettyPrint());
					context = ((Sequence) enclosingObject).getControlGraph1();
				}
				else if (enclosingObject instanceof UseModelSlotDeclaration) {
					context = ((UseModelSlotDeclaration) enclosingObject).getCompilationUnit();
				}
				else if (enclosingObject instanceof NamespaceDeclaration) {
					context = ((NamespaceDeclaration) enclosingObject).getCompilationUnit();
				}
				else if (enclosingObject instanceof JavaImportDeclaration) {
					context = ((JavaImportDeclaration) enclosingObject).getCompilationUnit();
				}
				else if (enclosingObject instanceof ElementImportDeclaration) {
					context = ((ElementImportDeclaration) enclosingObject).getCompilationUnit();
				}
				else if (enclosingObject instanceof org.openflexo.foundation.fml.TypeDeclaration) {
					context = ((org.openflexo.foundation.fml.TypeDeclaration) enclosingObject).getCompilationUnit();
				}
				else if (enclosingObject instanceof FMLMetaData) {
					FMLObject owner = ((FMLMetaData) enclosingObject).getOwner();
					if (owner instanceof VirtualModel) {
						context = owner.getDeclaringCompilationUnit();
					}
					else if (owner instanceof FlexoConcept) {
						if (((FlexoConcept) owner).getContainerFlexoConcept() != null) {
							context = ((FlexoConcept) owner).getContainerFlexoConcept();
						}
						else {
							context = ((FlexoConcept) owner).getOwner();
						}
					}
					else if (owner instanceof FlexoProperty) {
						context = ((FlexoProperty<?>) owner).getFlexoConcept();
					}
					else if (owner instanceof FlexoBehaviour) {
						context = ((FlexoBehaviour) owner).getFlexoConcept();
					}
					else {
						context = enclosingObject;
					}
				}
				else if (enclosingObject instanceof FlexoBehaviour) {
					// System.out.println("Bon, ben :\n" + enclosingObjectNode.debug());
					FlexoBehaviourNode<?, ?> behaviourNode = (FlexoBehaviourNode<?, ?>) enclosingObjectNode;
					RawSourcePosition lBrcPosition = behaviourNode.getLBrcPosition();
					RawSourcePosition rBrcPosition = behaviourNode.getRBrcPosition();
					RawSourcePosition currentPosition = enclosingObjectNode.getRawSource().new RawSourcePosition(
							((RSyntaxTextArea) textArea).getCaretLineNumber() + 1,
							((RSyntaxTextArea) textArea).getCaretOffsetFromLineStart());
					// System.out.println("Current:" + currentPosition);
					// System.out.println("lBrcPosition:" + lBrcPosition);
					// System.out.println("rBrcPosition:" + rBrcPosition);
					if (lBrcPosition != null && rBrcPosition != null && currentPosition.isAfter(lBrcPosition)
							&& currentPosition.isBefore(rBrcPosition)) {
						// Special case where a FlexoBehaviour is targeted, but inside its control graph
						context = ((FlexoBehaviour) enclosingObject).getControlGraph();
					}
					else {
						context = enclosingObject;
					}
				}
				else {
					context = enclosingObject;
				}
			}
			else {
				context = null;
			}
			System.out.println("context=" + context);
		}

		/**
		 * Return context type, as represented by the context object
		 * 
		 * @return
		 */
		ContextType getContextType() {
			if (context instanceof FMLCompilationUnit) {
				return ContextType.COMPILATION_UNIT_DECLARATION;
			}
			else if (context instanceof FlexoConcept || context instanceof FlexoProperty || context instanceof FlexoBehaviour) {
				return ContextType.DECLARATION;
			}
			else if (context instanceof FMLControlGraph) {
				return ContextType.CONTROL_GRAPH;
			}
			else {
				logger.warning("Unexpected context: " + context);
				return ContextType.DECLARATION;
			}
		}

		/**
		 * Return the {@link BindingModel} applicable in this context
		 * 
		 * @return
		 */
		public BindingModel getBindingModel() {
			if (context instanceof FMLControlGraph) {
				return ((FMLControlGraph) context).getInferedBindingModel();
			}
			if (context != null) {
				return context.getBindingModel();
			}
			return null;
		}

		/**
		 * Return the {@link BindingFactory} applicable in this context
		 * 
		 * @return
		 */
		public BindingFactory getBindingFactory() {
			if (context != null) {
				return context.getBindingFactory();
			}
			return null;
		}

		/**
		 * Build a dummy {@link Bindable} with applicable {@link BindingModel} and {@link BindingFactory}
		 * 
		 * @return
		 */
		Bindable makeDefaultBindable() {
			return new DefaultBindable() {
				@Override
				public BindingModel getBindingModel() {
					return CompletionContext.this.getBindingModel();
				}

				@Override
				public BindingFactory getBindingFactory() {
					return CompletionContext.this.getBindingFactory();
				}

				@Override
				public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
				}

				@Override
				public void notifiedBindingChanged(DataBinding<?> dataBinding) {
				}
			};
		}

	}

	/**
	 * Completion context is basically classified into three kinds:
	 * <ul>
	 * <li>COMPILATION_UNIT_DECLARATION : declarations outside {@link VirtualModel}</li>
	 * <li>DECLARATION : declarations relative to a {@link VirtualModel} or a {@link FlexoConcept} (outside any {@link FlexoBehaviour})</li>
	 * <li>CONTROL_GRAPH : imperative code inside a {@link FlexoBehaviour} control graph</li>
	 * </ul>
	 */
	enum ContextType {
		COMPILATION_UNIT_DECLARATION, DECLARATION, CONTROL_GRAPH
	}

	private CompletionContext getRelevantContext(FMLCompilationUnit cu, JTextComponent textArea) {
		return new CompletionContext(cu, textArea);
	}

	private FMLObjectNode<?, FMLPrettyPrintable, ?> getEnclosingFMLObjectNode(FMLCompilationUnit cu, JTextComponent textArea) {
		if (textArea instanceof RSyntaxTextArea) {
			FMLCompilationUnitNode cuNode = (FMLCompilationUnitNode) cu.getPrettyPrintDelegate();
			if (cuNode != null) {
				// System.out.println("At line=" + (((RSyntaxTextArea) textArea).getCaretLineNumber() + 1) + " row="
				// + ((RSyntaxTextArea) textArea).getCaretOffsetFromLineStart());
				return cuNode.getFMLObjectNodeAtLocation(((RSyntaxTextArea) textArea).getCaretLineNumber() + 1,
						((RSyntaxTextArea) textArea).getCaretOffsetFromLineStart(), FMLPrettyPrintable.class);
			}
		}
		return null;
	}

	private void addCompletionsForStaticMembers(Set<Completion> set, FMLCompilationUnit cu, ClassFile cf, String pkg) {

		// Check us first, so if we override anything, we get the "newest"
		// version.
		int methodCount = cf.getMethodCount();
		for (int i = 0; i < methodCount; i++) {
			MethodInfo info = cf.getMethodInfo(i);
			if (isAccessible(info, pkg) && info.isStatic()) {
				MethodCompletion mc = new MethodCompletion(this, info);
				set.add(mc);
			}
		}

		int fieldCount = cf.getFieldCount();
		for (int i = 0; i < fieldCount; i++) {
			FieldInfo info = cf.getFieldInfo(i);
			if (isAccessible(info, pkg) && info.isStatic()) {
				FieldCompletion fc = new FieldCompletion(this, info);
				set.add(fc);
			}
		}

		ClassFile superClass = getClassFileFor(cu, cf.getSuperClassName(true));
		if (superClass != null) {
			addCompletionsForStaticMembers(set, cu, superClass, pkg);
		}

	}

	/**
	 * Adds completions for accessible methods and fields of super classes. This is only called when the caret is inside of a class. TODO:
	 * Handle accessibility correctly!
	 *
	 * @param set
	 *            The set of completions to add to.
	 * @param cu
	 *            The compilation unit.
	 * @param cf
	 *            A class in the chain of classes that a type being parsed inherits from.
	 * @param pkg
	 *            The package of the source being parsed.
	 * @param typeParamMap
	 *            A mapping of type parameters to type arguments for the object whose fields/methods/etc. are currently being
	 *            code-completed.
	 */
	private void addCompletionsForExtendedClass(Set<Completion> set, FMLCompilationUnit cu, ClassFile cf, String pkg,
			Map<String, String> typeParamMap) {

		// Reset this class's type-arguments-to-type-parameters map, so that
		// when methods and fields need to know type arguments, they can query
		// for them.
		cf.setTypeParamsToTypeArgs(typeParamMap);

		// Check us first, so if we override anything, we get the "newest"
		// version.
		int methodCount = cf.getMethodCount();
		for (int i = 0; i < methodCount; i++) {
			MethodInfo info = cf.getMethodInfo(i);
			// Don't show constructors
			if (isAccessible(info, pkg) && !info.isConstructor()) {
				MethodCompletion mc = new MethodCompletion(this, info);
				set.add(mc);
			}
		}

		int fieldCount = cf.getFieldCount();
		for (int i = 0; i < fieldCount; i++) {
			FieldInfo info = cf.getFieldInfo(i);
			if (isAccessible(info, pkg)) {
				FieldCompletion fc = new FieldCompletion(this, info);
				set.add(fc);
			}
		}

		// Add completions for any non-overridden super-class methods.
		ClassFile superClass = getClassFileFor(cu, cf.getSuperClassName(true));
		if (superClass != null) {
			addCompletionsForExtendedClass(set, cu, superClass, pkg, typeParamMap);
		}

		// Add completions for any interface methods, in case this class is
		// abstract and hasn't implemented some of them yet.
		// TODO: Do this only if "top-level" class is declared abstract
		for (int i = 0; i < cf.getImplementedInterfaceCount(); i++) {
			String inter = cf.getImplementedInterfaceName(i, true);
			cf = getClassFileFor(cu, inter);
			addCompletionsForExtendedClass(set, cu, cf, pkg, typeParamMap);
		}

	}

	/**
	 * Adds completions for all methods and public fields of a local variable. This will add nothing if the local variable is a primitive
	 * type.
	 *
	 * @param cu
	 *            The compilation unit being parsed.
	 * @param var
	 *            The local variable.
	 * @param retVal
	 *            The set to add completions to.
	 */
	private void addCompletionsForLocalVarsMethods(FMLCompilationUnit cu, LocalVariable var, Set<Completion> retVal) {

		// A IMPLEMENTER

		/*
		Type type = var.getType();
		String pkg = cu.getPackageName();
		
		if (type.isArray()) {
			ClassFile cf = getClassFileFor(cu, "java.lang.Object");
			addCompletionsForExtendedClass(retVal, cu, cf, pkg, null);
			FieldCompletion fc = FieldCompletion.createLengthCompletion(this, type);
			retVal.add(fc);
		}
		
		else if (!type.isBasicType()) {
			String typeStr = type.getName(true, false);
			ClassFile cf = getClassFileFor(cu, typeStr);
			if (cf != null) {
				Map<String, String> typeParamMap = createTypeParamMap(type, cf);
				addCompletionsForExtendedClass(retVal, cu, cf, pkg, typeParamMap);
			}
		}*/

	}

	/**
	 * Adds simple shorthand completions relevant to DECLARATION context
	 *
	 * @param set
	 *            The set to add to.
	 */
	private void addFMLDeclarationsShorthandCompletions(Set<Completion> set) {
		if (declShorthandCache != null) {
			set.addAll(declShorthandCache.getShorthandCompletions());
		}
	}

	/**
	 * Adds simple shorthand completions relevant to CONTROL_GRAPH.
	 *
	 * @param set
	 *            The set to add to.
	 */
	private void addFMLControlGraphShorthandCompletions(Set<Completion> set) {
		if (cgShorthandCache != null) {
			set.addAll(cgShorthandCache.getShorthandCompletions());
		}
	}

	/**
	 * Set template completion cache in the context of DECLARATIONS
	 *
	 * @param shorthandCache
	 *            The new cache.
	 */
	public void setFMLDeclarationsShorthandCompletionCache(FMLDeclarationsShorthandCompletionCache shorthandCache) {
		this.declShorthandCache = shorthandCache;
	}

	/**
	 * Set template completion cache in the context of CONTROL_GRAPH
	 *
	 * @param shorthandCache
	 *            The new cache.
	 */
	public void setFMLControlGraphShorthandCompletionCache(FMLControlGraphShorthandCompletionCache shorthandCache) {
		this.cgShorthandCache = shorthandCache;
	}

	/**
	 * Gets the {@link ClassFile} for a class.
	 *
	 * @param cu
	 *            The compilation unit being parsed.
	 * @param className
	 *            The name of the class (fully qualified or not).
	 * @return The {@link ClassFile} for the class, or <code>null</code> if <code>cf</code> represents <code>java.lang.Object</code> (or if
	 *         the super class could not be determined).
	 */
	private ClassFile getClassFileFor(FMLCompilationUnit cu, String className) {

		// A IMPLEMENTER

		/*
		// System.err.println(">>> Getting class file for: " + className);
		if (className == null) {
			return null;
		}
		
		ClassFile superClass = null;
		
		// Determine the fully qualified class to grab
		if (!Util.isFullyQualified(className)) {
		
			// Check in this source file's package first
			String pkg = cu.getPackageName();
			if (pkg != null) {
				String temp = pkg + "." + className;
				superClass = jarManager.getClassEntry(temp);
			}
		
			// Next, go through the imports (order is important)
			if (superClass == null) {
				Iterator<ImportDeclaration> i = cu.getImportIterator();
				while (i.hasNext()) {
					ImportDeclaration id = i.next();
					String imported = id.getName();
					if (imported.endsWith(".*")) {
						String temp = imported.substring(0, imported.length() - 1) + className;
						superClass = jarManager.getClassEntry(temp);
						if (superClass != null) {
							break;
						}
					}
					else if (imported.endsWith("." + className)) {
						superClass = jarManager.getClassEntry(imported);
						break;
					}
				}
			}
		
			// Finally, try java.lang
			if (superClass == null) {
				String temp = "java.lang." + className;
				superClass = jarManager.getClassEntry(temp);
			}
		
		}
		
		else {
			superClass = jarManager.getClassEntry(className);
		}
		
		return superClass;
		 */
		return null;
	}

	/**
	 * Adds completions for local variables in a method.
	 *
	 * @param set
	 *            The set of completions to add to.
	 * @param method
	 *            The method being examined.
	 * @param offs
	 *            The caret's offset into the source. This should be inside of <code>method</code>.
	 */
	private void addLocalVarCompletions(Set<Completion> set, Method method, int offs) {

		for (int i = 0; i < method.getParameterCount(); i++) {
			FormalParameter param = method.getParameter(i);
			set.add(new LocalVariableCompletion(this, param));
		}

		CodeBlock body = method.getBody();
		if (body != null) {
			addLocalVarCompletions(set, body, offs);
		}

	}

	/**
	 * Adds completions for local variables in a code block inside a method.
	 *
	 * @param set
	 *            The set of completions to add to.
	 * @param block
	 *            The code block.
	 * @param offs
	 *            The caret's offset into the source. This should be inside of <code>block</code>.
	 */
	private void addLocalVarCompletions(Set<Completion> set, CodeBlock block, int offs) {

		for (int i = 0; i < block.getLocalVarCount(); i++) {
			LocalVariable var = block.getLocalVar(i);
			if (var.getNameEndOffset() <= offs) {
				set.add(new LocalVariableCompletion(this, var));
			}
			else { // This and all following declared after offs
				break;
			}
		}

		for (int i = 0; i < block.getChildBlockCount(); i++) {
			CodeBlock child = block.getChildBlock(i);
			if (child.containsOffset(offs)) {
				addLocalVarCompletions(set, child, offs);
				break; // All other blocks are past this one
			}
			// If we've reached a block that's past the offset we're
			// searching for...
			else if (child.getNameStartOffset() > offs) {
				break;
			}
		}

	}

	/**
	 * Adds a jar to read from.
	 *
	 * @param info
	 *            The jar to add. If this is <code>null</code>, then the current JVM's main JRE jar (rt.jar, or classes.jar on OS X) will be
	 *            added. If this jar has already been added, adding it again will do nothing (except possibly update its attached source
	 *            location).
	 * @throws IOException
	 *             If an IO error occurs.
	 * @see #getJars()
	 * @see #removeJar(File)
	 */
	public void addJar(LibraryInfo info) throws IOException {
		jarManager.addClassFileSource(info);
	}

	/**
	 * Checks whether the user is typing a completion for a String member after a String literal.
	 *
	 * @param comp
	 *            The text component.
	 * @param alreadyEntered
	 *            The text already entered.
	 * @param cu
	 *            The compilation unit being parsed.
	 * @param set
	 *            The set to add possible completions to.
	 * @return Whether the user is indeed typing a completion for a String literal member.
	 */
	private boolean checkStringLiteralMember(JTextComponent comp, String alreadyEntered, FMLCompilationUnit cu, Set<Completion> set) {

		// A IMPLEMENTER

		/*
		boolean stringLiteralMember = false;
		
		int offs = comp.getCaretPosition() - alreadyEntered.length() - 1;
		if (offs > 1) {
			RSyntaxTextArea textArea = (RSyntaxTextArea) comp;
			RSyntaxDocument doc = (RSyntaxDocument) textArea.getDocument();
			try {
				// System.out.println(doc.charAt(offs) + ", " + doc.charAt(offs+1));
				if (doc.charAt(offs) == '"' && doc.charAt(offs + 1) == '.') {
					int curLine = textArea.getLineOfOffset(offs);
					Token list = textArea.getTokenListForLine(curLine);
					Token prevToken = RSyntaxUtilities.getTokenAtOffset(list, offs);
					if (prevToken != null && prevToken.getType() == Token.LITERAL_STRING_DOUBLE_QUOTE) {
						ClassFile cf = getClassFileFor(cu, "java.lang.String");
						addCompletionsForExtendedClass(set, cu, cf, cu.getPackageName(), null);
						stringLiteralMember = true;
					}
					else {
						System.out.println(prevToken);
					}
				}
			} catch (BadLocationException ble) { // Never happens
				ble.printStackTrace();
			}
		}
		
		return stringLiteralMember;*/

		return false;

	}

	/**
	 * Removes all jars from the "build path".
	 *
	 * @see #removeJar(File)
	 * @see #addJar(LibraryInfo)
	 * @see #getJars()
	 */
	public void clearJars() {
		jarManager.clearClassFileSources();
		// The memory used by the completions can be quite large, so go ahead
		// and clear out the completions list so no-longer-needed ones are
		// eligible for GC.
		clear();
	}

	/**
	 * Creates and returns a mapping of type parameters to type arguments.
	 *
	 * @param type
	 *            The type of a variable/field/etc. whose fields/methods/etc. are being code completed, as declared in the source. This
	 *            includes type arguments.
	 * @param cf
	 *            The <code>ClassFile</code> representing the actual type of the variable/field/etc. being code completed
	 * @return A mapping of type parameter names to type arguments (both Strings).
	 */
	private Map<String, String> createTypeParamMap(Type type, ClassFile cf) {
		Map<String, String> typeParamMap = null;
		List<TypeArgument> typeArgs = type.getTypeArguments(type.getIdentifierCount() - 1);
		if (typeArgs != null) {
			typeParamMap = new HashMap<>();
			List<String> paramTypes = cf.getParamTypes();
			// Should be the same size! Otherwise, the source code has
			// too many/too few type arguments listed for this type.
			int min = Math.min(paramTypes == null ? 0 : paramTypes.size(), typeArgs.size());
			for (int i = 0; i < min; i++) {
				TypeArgument typeArg = typeArgs.get(i);
				typeParamMap.put(paramTypes.get(i), typeArg.toString());
			}
		}
		return typeParamMap;
	}

	@Override
	public List<Completion> getCompletionsAt(JTextComponent tc, Point p) {
		getCompletionsImpl(tc); // Force loading of completions
		return super.getCompletionsAt(tc, p);
	}

	/**
	 * This is the main method where completions are computed
	 */
	@Override
	protected List<Completion> getCompletionsImpl(JTextComponent comp) {

		// System.out.println("getCompletionsImpl() in SourceCompletionProvider");

		comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {

			completions = new ArrayList<>();// completions.clear();

			FMLCompilationUnit cu = fmlProvider.getCompilationUnit();

			// System.out.println("ici FMLCompilationUnit=" + cu);

			if (cu == null) {
				return completions; // empty
			}

			// TODO

			Set<Completion> set = new TreeSet<>();

			// Cut down the list to just those matching what we've typed.
			// Note: getAlreadyEnteredText() never returns null
			String alreadyEntered = getAlreadyEnteredText(comp);

			// int lastDot = text.lastIndexOf('.');
			// boolean qualified = lastDot > -1;
			// String prefix = qualified ? text.substring(0, lastDot) : null;
			CompletionContext context = getRelevantContext(cu, comp);

			System.out.println("Computing completions, context is " + context.context + " of " + context.getContextType());

			// Special case - end of a String literal
			boolean stringLiteralMember = checkStringLiteralMember(comp, alreadyEntered, cu, set);

			// System.out.println("stringLiteralMember=" + stringLiteralMember);

			// Not after a String literal - regular code completion
			if (!stringLiteralMember) {

				// Don't add shorthand completions if they're typing something
				// qualified
				if (alreadyEntered.indexOf('.') == -1) {
					switch (context.getContextType()) {
						case COMPILATION_UNIT_DECLARATION:
							loadUsesCompletions(cu, comp, alreadyEntered, set);
							break;
						case DECLARATION:
							loadModelSlotCompletions(cu, comp, alreadyEntered, set);
							loadFlexoRoleCompletions(cu, comp, alreadyEntered, set);
							addFMLDeclarationsShorthandCompletions(set);
							break;
						case CONTROL_GRAPH:
							addFMLControlGraphShorthandCompletions(set);
							loadCompletionsForCaretPosition(cu, comp, alreadyEntered, set);
							break;
						default:
							break;
					}
				}
				else {
					// TODO: gerer mieux tout ca !!!
					loadCompletionsForCaretPosition(cu, comp, alreadyEntered, set);
				}

				// loadCompletionsForCaretPosition(cu, comp, alreadyEntered, set);

				loadImportCompletions(set, alreadyEntered, cu);

				// Add completions for fully-qualified stuff (e.g. "com.sun.jav")
				// long startTime = System.currentTimeMillis();
				jarManager.addCompletions(this, alreadyEntered, set);
				// long time = System.currentTimeMillis() - startTime;
				// System.out.println("jar completions loaded in: " + time);

				// Loop through all types declared in this source, and provide
				// completions depending on in what type/method/etc. the caret's in.
				// loadCompletionsForCaretPosition(cu, comp, alreadyEntered, set);

			}

			// Do a final sort of all of our completions and we're good to go!
			completions = new ArrayList<>(set);
			Collections.sort(completions);

			// Only match based on stuff after the final '.', since that's what is
			// displayed for all of our completions.
			alreadyEntered = alreadyEntered.substring(alreadyEntered.lastIndexOf('.') + 1);

			@SuppressWarnings("unchecked")
			int start = Collections.binarySearch(completions, alreadyEntered, comparator);
			if (start < 0) {
				start = -(start + 1);
			}
			else {
				// There might be multiple entries with the same input text.
				while (start > 0 && comparator.compare(completions.get(start - 1), alreadyEntered) == 0) {
					start--;
				}
			}

			@SuppressWarnings("unchecked")
			int end = Collections.binarySearch(completions, alreadyEntered + '{', comparator);
			end = -(end + 1);

			return completions.subList(start, end);

		} finally {
			comp.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		}

	}

	/**
	 * Returns the jars on the "build path".
	 *
	 * @return A list of {@link LibraryInfo}s. Modifying a <code>LibraryInfo</code> in this list will have no effect on this completion
	 *         provider; in order to do that, you must re-add the jar via {@link #addJar(LibraryInfo)}. If there are no jars on the "build
	 *         path," this will be an empty list.
	 * @see #addJar(LibraryInfo)
	 */
	public List<LibraryInfo> getJars() {
		return jarManager.getClassFileSources();
	}

	public SourceLocation getSourceLocForClass(String className) {
		return jarManager.getSourceLocForClass(className);
	}

	/**
	 * Returns whether a method defined by a super class is accessible to this class.
	 *
	 * @param info
	 *            Information about the member.
	 * @param pkg
	 *            The package of the source currently being parsed.
	 * @return Whether the method is accessible.
	 */
	private boolean isAccessible(MemberInfo info, String pkg) {

		boolean accessible = false;
		int access = info.getAccessFlags();

		if (org.openflexo.fml.rstasupport.classreader.Util.isPublic(access)
				|| org.openflexo.fml.rstasupport.classreader.Util.isProtected(access)) {
			accessible = true;
		}
		else if (org.openflexo.fml.rstasupport.classreader.Util.isDefault(access)) {
			String pkg2 = info.getClassFile().getPackageName();
			accessible = (pkg == null && pkg2 == null) || (pkg != null && pkg.equals(pkg2));
		}

		return accessible;

	}

	@Override
	protected boolean isValidChar(char ch) {
		return Character.isJavaIdentifierPart(ch) || ch == '.';
	}

	/**
	 * Loads completions based on the current caret location in the source. In other words:
	 *
	 * <ul>
	 * <li>If the caret is anywhere in a class, the names of all methods and fields in the class are loaded. Methods and fields in super
	 * classes are also loaded. TODO: Get super methods/fields added correctly by access!
	 * <li>If the caret is in a field, local variables currently accessible are loaded.
	 * </ul>
	 *
	 * @param cu
	 *            The compilation unit being parsed.
	 * @param comp
	 *            The text component.
	 * @param alreadyEntered
	 *            The already-entered text.
	 * @param retVal
	 *            The set of values to add to.
	 */
	private void loadCompletionsForCaretPosition(FMLCompilationUnit cu, JTextComponent comp, String alreadyEntered,
			Set<Completion> retVal) {

		int lastDot = alreadyEntered.lastIndexOf('.');
		boolean qualified = lastDot > -1;
		String prefix = qualified ? alreadyEntered.substring(0, lastDot) : null;
		CompletionContext context = getRelevantContext(cu, comp);

		System.out.println("alreadyEntered=" + alreadyEntered);
		System.out.println("prefix=" + prefix);
		System.out.println("context=" + context);
		/*RawSourceFragment f = ((FMLObjectNode) enclosingObject.getPrettyPrintDelegate()).getFragment();
		System.out.println("fragment: " + f);
		System.out.println("[" + f.getRawText() + "]");
		
		System.out.println("FML:\n" + enclosingObject.getFMLPrettyPrint());*/

		if (StringUtils.isNotEmpty(prefix)) {
			// If prefix is not empty, we are about to complete a BindingPath
			// First build a bindable disconnected from "real" model with same BindingModel and BindingFactory
			Bindable defaultBindable = context.makeDefaultBindable();
			// First build existing path
			DataBinding<?> existingBinding = new DataBinding<>(prefix, defaultBindable);
			// System.out.println("Hop le binding " + existingBinding);
			// System.out.println("valid: " + existingBinding.isValid());
			// System.out.println("reason: " + existingBinding.invalidBindingReason());
			// System.out.println("type: " + existingBinding.getAnalyzedType());
			if (existingBinding.isValid()) {
				loadCompletionsForType(context, existingBinding.getAnalyzedType(), retVal);
				// Thread.dumpStack();
			}
		}
		else {

			if (context == cu) {
				/*String template = "import tutu.Prout as ${PROUT};";
				// String template = "System.out.println(${});${cursor}";
				JavaTemplateCompletion completion = new JavaTemplateCompletion(this, "prout", "prout", template, "un petit prout",
						"un gros prout");
				retVal.add(completion);
				System.out.println("Ici");*/
				// System.exit(-1);
				loadUsesCompletions(cu, comp, alreadyEntered, retVal);
			}

			loadRootCompletions(context, retVal);
		}

		/*
		Iterator<TypeDeclaration> i = cu.getTypeDeclarationIterator();
		while (i.hasNext()) {
		
			TypeDeclaration td = i.next();
			start = td.getBodyStartOffset();
			end = td.getBodyEndOffset();
		
			if (caret > start && caret <= end) {
				loadCompletionsForCaretPosition(cu, comp, alreadyEntered, retVal, td, prefix, caret);
			}
		
			else if (caret < start) {
				break; // We've passed any type declarations we could be in
			}
		
		}
		
		// long time = System.currentTimeMillis() - startTime;
		// System.out.println("methods/fields/localvars loaded in: " + time);
		 */

	}

	private void loadUsesCompletions(FMLCompilationUnit cu, JTextComponent comp, String alreadyEntered, Set<Completion> retVal) {

		if (cu.getTechnologyAdapterService() != null) {
			TechnologyAdapterService taService = cu.getTechnologyAdapterService();
			for (TechnologyAdapter<?> ta : taService.getTechnologyAdapters()) {
				for (Class<? extends ModelSlot<?>> msType : ta.getAvailableModelSlotTypes()) {
					if (!cu.uses(msType)) {
						// System.out.println(" >>>> " + msType);
						retVal.add(new UseCompletion<>(this, alreadyEntered, msType));
					}
				}
			}
		}

	}

	private void loadModelSlotCompletions(FMLCompilationUnit cu, JTextComponent comp, String alreadyEntered, Set<Completion> retVal) {

		for (UseModelSlotDeclaration useModelSlotDeclaration : cu.getAccessibleUseDeclarations()) {
			retVal.add(new ModelSlotDeclarationCompletion<>(this, alreadyEntered, useModelSlotDeclaration.getModelSlotClass()));
		}
	}

	private void loadFlexoRoleCompletions(FMLCompilationUnit cu, JTextComponent comp, String alreadyEntered, Set<Completion> retVal) {

		for (UseModelSlotDeclaration useModelSlotDeclaration : cu.getAccessibleUseDeclarations()) {
			for (Class<? extends FlexoRole<?>> roleClass : getServiceManager().getTechnologyAdapterService()
					.getAvailableFlexoRoleTypes(useModelSlotDeclaration.getModelSlotClass())) {
				retVal.add(new FlexoRoleDeclarationCompletion<>(this, alreadyEntered, roleClass));
			}
		}
	}

	private void loadRootCompletions(CompletionContext context, Set<Completion> retVal) {
		if (context == null) {
			return;
		}

		BindingModel bindingModel = context.getBindingModel();

		if (context instanceof FMLControlGraph) {
			bindingModel = ((FMLControlGraph) context).getInferedBindingModel();
		}

		if (bindingModel != null) {
			for (int i = 0; i < bindingModel.getBindingVariablesCount(); i++) {
				BindingVariable bv = bindingModel.getBindingVariableAt(i);
				// System.out.println(" > " + bv + " of " + bv.getClass().getSimpleName());
				retVal.add(BindingVariableCompletionFactory.makeBindingVariableCompletion(this, bv));
			}
		}
	}

	private void loadCompletionsForType(CompletionContext context, java.lang.reflect.Type type, Set<Completion> retVal) {
		BindingFactory bf = context.getBindingFactory();

		BindingVariable dummyBV = new BindingVariable("temp", type);
		List<? extends SimplePathElement<?>> accessibleSimplePathElements = bf.getAccessibleSimplePathElements(dummyBV, context.context);
		List<? extends FunctionPathElement<?>> accessibleFunctionPathElements = bf.getAccessibleFunctionPathElements(dummyBV,
				context.context);

		for (SimplePathElement<?> simplePathElement : accessibleSimplePathElements) {
			// System.out.println(" > " + simplePathElement);
			retVal.add(BindingPathElementCompletionFactory.makeSimplePathElementCompletion(this, simplePathElement));
		}
		for (FunctionPathElement<?> functionPathElement : accessibleFunctionPathElements) {
			// System.out.println(" > " + functionPathElement);
			retVal.add(BindingPathElementCompletionFactory.makeFunctionPathElementCompletion(this, functionPathElement));
		}
	}

	/**
	 * Loads completions based on the current caret location in the source. This method is called when the caret is found to be in a
	 * specific type declaration. This method checks if the caret is in a child type declaration first, then adds completions for itself
	 * next.
	 *
	 * <ul>
	 * <li>If the caret is anywhere in a class, the names of all methods and fields in the class are loaded. Methods and fields in super
	 * classes are also loaded. TODO: Get super methods/fields added correctly by access!
	 * <li>If the caret is in a field, local variables currently accessible are loaded.
	 * </ul>
	 *
	 * @param cu
	 *            The compilation unit.
	 * @param comp
	 *            The text component being analyzed.
	 * @param alreadyEntered
	 *            The already-entered text.
	 * @param retVal
	 *            The set of returned completions.
	 * @param td
	 *            The type declaration.
	 * @param prefix
	 *            The prefix.
	 * @param caret
	 *            The caret position.
	 */
	private void loadCompletionsForCaretPosition(FMLCompilationUnit cu, JTextComponent comp, String alreadyEntered, Set<Completion> retVal,
			TypeDeclaration td, String prefix, int caret) {

		// A IMPLEMENTER

		/*
		// Do any child types first, so if any vars, etc. have duplicate names,
		// we pick up the one "closest" to us first.
		for (int i = 0; i < td.getChildTypeCount(); i++) {
			TypeDeclaration childType = td.getChildType(i);
			loadCompletionsForCaretPosition(cu, comp, alreadyEntered, retVal, childType, prefix, caret);
		}
		
		Method currentMethod = null;
		
		Map<String, String> typeParamMap = new HashMap<>();
		if (td instanceof NormalClassDeclaration) {
			NormalClassDeclaration ncd = (NormalClassDeclaration) td;
			List<TypeParameter> typeParams = ncd.getTypeParameters();
			if (typeParams != null) {
				for (TypeParameter typeParam : typeParams) {
					String typeVar = typeParam.getName();
					// For non-qualified completions, use type var name.
					typeParamMap.put(typeVar, typeVar);
				}
			}
		}
		
		// Get completions for this class's methods, fields and local
		// vars. Do this before checking super classes so that, if
		// we overrode anything, we get the "newest" version.
		String pkg = cu.getPackageName();
		Iterator<Member> j = td.getMemberIterator();
		while (j.hasNext()) {
			Member m = j.next();
			if (m instanceof Method) {
				Method method = (Method) m;
				if (prefix == null || THIS.equals(prefix)) {
					retVal.add(new MethodCompletion(this, method));
				}
				if (caret >= method.getBodyStartOffset() && caret < method.getBodyEndOffset()) {
					currentMethod = method;
					// Don't add completions for local vars if there is
					// a prefix, even "this".
					if (prefix == null) {
						addLocalVarCompletions(retVal, method, caret);
					}
				}
			}
			else if (m instanceof Field) {
				if (prefix == null || THIS.equals(prefix)) {
					Field field = (Field) m;
					retVal.add(new FieldCompletion(this, field));
				}
			}
		}
		
		// Completions for superclass methods.
		// TODO: Implement me better
		if (prefix == null || THIS.equals(prefix)) {
			if (td instanceof NormalClassDeclaration) {
				NormalClassDeclaration ncd = (NormalClassDeclaration) td;
				Type extended = ncd.getExtendedType();
				if (extended != null) { // e.g., not java.lang.Object
					String superClassName = extended.toString();
					ClassFile cf = getClassFileFor(cu, superClassName);
					if (cf != null) {
						addCompletionsForExtendedClass(retVal, cu, cf, pkg, null);
					}
					else {
						System.out.println("[DEBUG]: Couldn't find ClassFile for: " + superClassName);
					}
				}
			}
		}
		
		// Completions for methods of fields, return values of methods,
		// static fields/methods, etc.
		if (prefix != null && !THIS.equals(prefix)) {
			loadCompletionsForCaretPositionQualified(cu, alreadyEntered, retVal, td, currentMethod, prefix, caret);
		}
		 */
	}

	/**
	 * Loads completions for the text at the current caret position, if there is a "prefix" of chars and at least one '.' character in the
	 * text up to the caret. This is currently very limited and needs to be improved.
	 *
	 * @param cu
	 *            The compilation unit being examined.
	 * @param alreadyEntered
	 *            The already-entered text.
	 * @param retVal
	 *            The return value.
	 * @param td
	 *            The type declaration the caret is in.
	 * @param currentMethod
	 *            The method the caret is in, or <code>null</code> if none.
	 * @param prefix
	 *            The text up to the current caret position. This is guaranteed to be non-<code>null</code> not equal to "<tt>this</tt>".
	 * @param offs
	 *            The offset of the caret in the document.
	 */
	private void loadCompletionsForCaretPositionQualified(FMLCompilationUnit cu, String alreadyEntered, Set<Completion> retVal,
			TypeDeclaration td, Method currentMethod, String prefix, int offs) {

		// A IMPLEMENTER

		/*
		
		// TODO: Remove this restriction.
		int dot = prefix.indexOf('.');
		if (dot > -1) {
			System.out.println("[DEBUG]: Qualified non-this completions currently only go 1 level deep");
			return;
		}
		
		// TODO: Remove this restriction.
		else if (!prefix.matches("[A-Za-z_][A-Za-z0-9_\\$]*")) {
			System.out.println("[DEBUG]: Only identifier non-this completions are currently supported");
			return;
		}
		
		String pkg = cu.getPackageName();
		boolean matched = false;
		
		for (Iterator<Member> j = td.getMemberIterator(); j.hasNext();) {
		
			Member m = j.next();
		
			// The prefix might be a field in the local class.
			if (m instanceof Field) {
		
				Field field = (Field) m;
		
				if (field.getName().equals(prefix)) {
					// System.out.println("FOUND: " + prefix + " (" + pkg + ")");
					Type type = field.getType();
					if (type.isArray()) {
						ClassFile cf = getClassFileFor(cu, "java.lang.Object");
						addCompletionsForExtendedClass(retVal, cu, cf, pkg, null);
						FieldCompletion fc = FieldCompletion.createLengthCompletion(this, type);
						retVal.add(fc);
					}
					else if (!type.isBasicType()) {
						String typeStr = type.getName(true, false);
						ClassFile cf = getClassFileFor(cu, typeStr);
						// Add completions for extended class type chain
						if (cf != null) {
							Map<String, String> typeParamMap = createTypeParamMap(type, cf);
							addCompletionsForExtendedClass(retVal, cu, cf, pkg, typeParamMap);
							// Add completions for all implemented interfaces
							// TODO: Only do this if type is abstract!
							for (int i = 0; i < cf.getImplementedInterfaceCount(); i++) {
								String inter = cf.getImplementedInterfaceName(i, true);
								cf = getClassFileFor(cu, inter);
								System.out.println(cf);
							}
						}
					}
					matched = true;
					break;
				}
		
			}
		
		}
		
		// The prefix might be for a local variable in the current method.
		if (currentMethod != null) {
		
			boolean found = false;
		
			// Check parameters to the current method
			for (int i = 0; i < currentMethod.getParameterCount(); i++) {
				FormalParameter param = currentMethod.getParameter(i);
				String name = param.getName();
				// Assuming prefix is "one level deep" and contains no '.'...
				if (prefix.equals(name)) {
					addCompletionsForLocalVarsMethods(cu, param, retVal);
					found = true;
					break;
				}
			}
		
			// If a formal param wasn't matched, check local variables.
			if (!found) {
				CodeBlock body = currentMethod.getBody();
				if (body != null) {
					loadCompletionsForCaretPositionQualifiedCodeBlock(cu, retVal, td, body, prefix, offs);
				}
			}
		
			matched |= found;
		
		}
		
		// Could be a class name, in which case we'll need to add completions
		// for static fields and methods.
		if (!matched) {
			List<ImportDeclaration> imports = cu.getImports();
			List<ClassFile> matches = jarManager.getClassesWithUnqualifiedName(prefix, imports);
			if (matches != null) {
				for (ClassFile cf : matches) {
					addCompletionsForStaticMembers(retVal, cu, cf, pkg);
				}
			}
		}
		 */
	}

	private void loadCompletionsForCaretPositionQualifiedCodeBlock(FMLCompilationUnit cu, Set<Completion> retVal, TypeDeclaration td,
			CodeBlock block, String prefix, int offs) {

		boolean found = false;

		for (int i = 0; i < block.getLocalVarCount(); i++) {
			LocalVariable var = block.getLocalVar(i);
			if (var.getNameEndOffset() <= offs) {
				// TODO: This assumes prefix is "1 level deep"
				if (prefix.equals(var.getName())) {
					addCompletionsForLocalVarsMethods(cu, var, retVal);
					found = true;
					break;
				}
			}
			else { // This and all following declared after offs
				break;
			}
		}

		if (found) {
			return;
		}

		for (int i = 0; i < block.getChildBlockCount(); i++) {
			CodeBlock child = block.getChildBlock(i);
			if (child.containsOffset(offs)) {
				loadCompletionsForCaretPositionQualifiedCodeBlock(cu, retVal, td, child, prefix, offs);
				break; // All other blocks are past this one
			}
			// If we've reached a block that's past the offset we're
			// searching for...
			else if (child.getNameStartOffset() > offs) {
				break;
			}
		}

	}

	/**
	 * Loads completions for a single import statement.
	 *
	 * @param importStr
	 *            The import statement.
	 * @param pkgName
	 *            The package of the source currently being parsed.
	 */
	private void loadCompletionsForImport(Set<Completion> set, String importStr, String pkgName) {

		if (importStr.endsWith(".*")) {
			String pkg = importStr.substring(0, importStr.length() - 2);
			boolean inPkg = pkg.equals(pkgName);
			List<ClassFile> classes = jarManager.getClassesInPackage(pkg, inPkg);
			for (ClassFile cf : classes) {
				set.add(new ClassCompletion(this, cf));
			}
		}

		else {
			ClassFile cf = jarManager.getClassEntry(importStr);
			if (cf != null) {
				set.add(new ClassCompletion(this, cf));
			}
		}

	}

	/**
	 * Loads completions for all import statements.
	 *
	 * @param cu
	 *            The compilation unit being parsed.
	 */
	private void loadImportCompletions(Set<Completion> set, String text, FMLCompilationUnit cu) {

		// A IMPLEMENTER

		/*
		// Fully-qualified completions are handled elsewhere, so no need to
		// duplicate the work here
		if (text.indexOf('.') > -1) {
			return;
		}
		
		// long startTime = System.currentTimeMillis();
		
		String pkgName = cu.getPackageName();
		loadCompletionsForImport(set, JAVA_LANG_PACKAGE, pkgName);
		for (Iterator<ImportDeclaration> i = cu.getImportIterator(); i.hasNext();) {
			ImportDeclaration id = i.next();
			String name = id.getName();
			if (!JAVA_LANG_PACKAGE.equals(name)) {
				loadCompletionsForImport(set, name, pkgName);
			}
		}
		// Collections.sort(completions);
		
		// long time = System.currentTimeMillis() - startTime;
		// System.out.println("imports loaded in: " + time);
		 */
	}

	/**
	 * Removes a jar from the "build path".
	 *
	 * @param jar
	 *            The jar to remove.
	 * @return Whether the jar was removed. This will be <code>false</code> if the jar was not on the build path.
	 * @see #addJar(LibraryInfo)
	 * @see #getJars()
	 * @see #clearJars()
	 */
	public boolean removeJar(File jar) {
		boolean removed = jarManager.removeClassFileSource(jar);
		// The memory used by the completions can be quite large, so go ahead
		// and clear out the completions list so no-longer-needed ones are
		// eligible for GC.
		if (removed) {
			clear();
		}
		return removed;
	}

	/**
	 * Sets the parent Java provider.
	 *
	 * @param javaProvider
	 *            The parent completion provider.
	 */
	void setJavaProvider(FMLCompletionProvider javaProvider) {
		this.fmlProvider = javaProvider;
	}

}
