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

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.text.JTextComponent;

import org.fife.rsta.ac.ShorthandCompletionCache;
import org.fife.ui.autocomplete.AbstractCompletionProvider;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.LanguageAwareCompletionProvider;
import org.fife.ui.autocomplete.ParameterizedCompletion;
import org.openflexo.fml.controller.FMLTechnologyAdapterController;
import org.openflexo.fml.rstasupport.buildpath.LibraryInfo;
import org.openflexo.foundation.fml.FMLCompilationUnit;

/**
 * Completion provider for the Java programming language.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FMLCompletionProvider extends LanguageAwareCompletionProvider {

	/**
	 * The provider used for source code, kept here since it's used so much.
	 */
	private FMLSourceCompletionProvider sourceProvider;

	private FMLCompilationUnit cu;

	/**
	 * The {@link FMLTechnologyAdapterController}
	 */
	private final FMLTechnologyAdapterController fmlTAController;

	/**
	 * Constructor.
	 *
	 * @param jarManager
	 *            The jar manager to use when looking up completion choices. This can be passed in to share a single jar manager across
	 *            multiple <tt>RSyntaxTextArea</tt>s. This may also be <code>null</code>, in which case this completion provider will have a
	 *            unique <tt>JarManager</tt>.
	 */
	public FMLCompletionProvider(JarManager jarManager, FMLTechnologyAdapterController fmlTAController) {

		super(new FMLSourceCompletionProvider(jarManager, fmlTAController));
		this.fmlTAController = fmlTAController;
		this.sourceProvider = (FMLSourceCompletionProvider) getDefaultCompletionProvider();
		sourceProvider.setJavaProvider(this);
		setFMLDeclarationsShorthandCompletionCache(
				new FMLDeclarationsShorthandCompletionCache(sourceProvider, new DefaultCompletionProvider()));
		setFMLControlGraphShorthandCompletionCache(
				new FMLControlGraphShorthandCompletionCache(sourceProvider, new DefaultCompletionProvider()));
		setDocCommentCompletionProvider(new DocCommentCompletionProvider());
	}

	/**
	 * Adds a jar to the "build path".
	 *
	 * @param info
	 *            The jar to add. If this is <code>null</code>, then the current JVM's main JRE jar (rt.jar, or classes.jar on OS X) will be
	 *            added. If this jar has already been added, adding it again will do nothing (except possibly update its attached source
	 *            location).
	 * @throws IOException
	 *             If an IO error occurs.
	 * @see #removeJar(File)
	 * @see #getJars()
	 */
	public void addJar(LibraryInfo info) throws IOException {
		sourceProvider.addJar(info);
	}

	/**
	 * Removes all jars from the "build path".
	 *
	 * @see #removeJar(File)
	 * @see #addJar(LibraryInfo)
	 * @see #getJars()
	 */
	public void clearJars() {
		sourceProvider.clearJars();
	}

	public FMLTechnologyAdapterController getFMLTechnologyAdapterController() {
		return fmlTAController;
	}

	/**
	 * Defers to the source-analyzing completion provider.
	 *
	 * @return The already entered text.
	 */
	@Override
	public String getAlreadyEnteredText(JTextComponent comp) {
		return sourceProvider.getAlreadyEnteredText(comp);
	}

	public synchronized FMLCompilationUnit getCompilationUnit() {
		return cu;
	}

	@Override
	public List<Completion> getCompletionsAt(JTextComponent tc, Point p) {
		return sourceProvider.getCompletionsAt(tc, p);
	}

	@Override
	public List<Completion> getCompletions(JTextComponent comp) {
		return super.getCompletions(comp);
	}

	@Override
	protected List<Completion> getCompletionsImpl(JTextComponent comp) {
		return super.getCompletionsImpl(comp);
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
		return sourceProvider.getJars();
	}

	@Override
	public List<ParameterizedCompletion> getParameterizedCompletions(JTextComponent tc) {
		return null;
	}

	/**
	 * Removes a jar from the "build path".
	 *
	 * @param jar
	 *            The jar to remove.
	 * @return Whether the jar was removed. This will be <code>false</code> if the jar was not on the build path.
	 * @see #addJar(LibraryInfo)
	 */
	public boolean removeJar(File jar) {
		return sourceProvider.removeJar(jar);
	}

	private void setCommentCompletions(ShorthandCompletionCache shorthandCache) {
		AbstractCompletionProvider provider = shorthandCache.getCommentProvider();
		if (provider != null) {
			for (Completion c : shorthandCache.getCommentCompletions()) {
				provider.addCompletion(c);
			}
			setCommentCompletionProvider(provider);
		}
	}

	public synchronized void setCompilationUnit(FMLCompilationUnit cu) {
		if (cu == null) {
			System.out.println("setCompilationUnit with null ????");
			Thread.dumpStack();
		}
		this.cu = cu;
	}

	/**
	 * Set shorthand completion cache (template and comment completions).
	 *
	 * @param cache
	 *            The cache to use.
	 */
	public void setFMLDeclarationsShorthandCompletionCache(FMLDeclarationsShorthandCompletionCache cache) {
		sourceProvider.setFMLDeclarationsShorthandCompletionCache(cache);
		// reset comment completions too
		setCommentCompletions(cache);
	}

	/**
	 * Set shorthand completion cache (template and comment completions).
	 *
	 * @param cache
	 *            The cache to use.
	 */
	public void setFMLControlGraphShorthandCompletionCache(FMLControlGraphShorthandCompletionCache cache) {
		sourceProvider.setFMLControlGraphShorthandCompletionCache(cache);
	}

}
