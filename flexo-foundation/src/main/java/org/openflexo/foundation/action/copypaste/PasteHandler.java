package org.openflexo.foundation.action.copypaste;

import java.util.List;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.model.factory.Clipboard;

/**
 * An handler which is used to intercept and translate paste actions from/to the right pasting context<br>
 * The entire life cycle of a clipboard operation might be tracked using this object.<br>
 * All phases of paste are managed by a specific {@link PasteHandler}.
 * <ul>
 * <li>responds to isPastable() when adequate, @see {@link PasteHandler#isPastable(Clipboard, FlexoObject, List)}</li>
 * <li>prepare clipboard for pasting, @see {@link #prepareClipboardForPasting(FlexoClipboard, PastingContext)}</li>
 * <li>effective paste operation, @see #paste(FlexoClipboard, PastingContext)</li>
 * <li>paste operation finalization, @see {@link #finalizePasting(FlexoClipboard, PastingContext)}</li> </u>
 * 
 * @author sylvain
 * 
 * @param <T>
 *            type of target object where this handler applies
 */
public interface PasteHandler<T extends FlexoObject> {

	/**
	 * Return the type of pasting point holder this paste handler might handle
	 * 
	 * @return
	 */
	public Class<T> getPastingPointHolderType();

	/**
	 * Return boolean indicating if supplied clipboard may be pasted in supplied context
	 * 
	 * @param clipboard
	 *            Clipboard to paste
	 * @param pasteContext
	 *            Context where to paste clipboard (an object receiving the paste operation)
	 * @return
	 */
	public boolean isPastable(FlexoClipboard clipboard, PastingContext<T> pastingContext);

	/**
	 * Return a {@link PastingContext} if current selection and clipboard allows it.<br>
	 * Otherwise return null
	 * 
	 * @param focusedObject
	 * @param globalSelection
	 * @param clipboard
	 * @return
	 */
	public PastingContext<T> retrievePastingContext(FlexoObject focusedObject, List<FlexoObject> globalSelection, FlexoClipboard clipboard);

	/**
	 * This is a hook to set and/or translate some properties of clipboard beeing pasted
	 * 
	 */
	public void prepareClipboardForPasting(FlexoClipboard clipboard, PastingContext<T> pastingContext);

	/**
	 * Paste supplied clipboard in supplied context<br>
	 * Return pasted objects (a single object for a single contents clipboard, and a list of objects for a multiple contents)
	 * 
	 * @param clipboard
	 *            Clipboard to paste
	 * @param pasteContext
	 *            Context where to paste clipboard (an object receiving the paste operation)
	 * @return a single object for a single contents clipboard, and a list of objects for a multiple contents
	 */
	public Object paste(FlexoClipboard clipboard, PastingContext<T> pastingContext);

	/**
	 * This is a hook to finalize paste operation after clipboard has beeing pasted
	 * 
	 */
	public void finalizePasting(FlexoClipboard clipboard, PastingContext<T> pastingContext);

}
