package org.openflexo.foundation.action.copypaste;

import java.util.List;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.pamela.annotations.PastingPoint;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.exceptions.ModelExecutionException;
import org.openflexo.pamela.factory.ProxyMethodHandler;
import org.openflexo.pamela.model.ModelEntity;

/**
 * This is the abstract default implementation of {@link PasteHandler} in Openflexo context<br>
 * Pasting context is retrieved as focused object, and default paste is performed without any data translation
 * 
 * Provide default implementation while using PAMELA framework clipboard operation directives. Also provides default hooks at different
 * phases of clipboard operation
 * 
 * This class should be overriden to get more specific behaviour
 * 
 * @author sylvain
 * 
 */
public abstract class FlexoPasteHandler<T extends FlexoObject> implements PasteHandler<T> {

	public static final String COPY_SUFFIX = "-copy";

	/**
	 * Default implementation of isPastable(), using PAMELA framwork directives
	 * 
	 * @see {@link PastingPoint}
	 */
	@Override
	public boolean isPastable(FlexoClipboard clipboard, PastingContext<T> pastingContext) {

		ModelEntity<?> pastingPointHolderEntity = clipboard.getLeaderClipboard().getModelFactory().getModelContext()
				.getModelEntity(getPastingPointHolderType());

		// System.out.println("factory=" + clipboard.getModelFactory());
		// System.out.println("pastingPointHolderEntity=" + pastingPointHolderEntity);

		if (pastingPointHolderEntity != null) {
			// Entity was found in this PamelaModelFactory, we can proceed

			// System.out.println("Found entity " + pastingPointHolderEntity);

			return (ProxyMethodHandler.isPastable(clipboard.getLeaderClipboard(), pastingPointHolderEntity));
		}
		return false;
	}

	/**
	 * Default implementation of {@link PastingContext} retrieving
	 */
	@Override
	public PastingContext<T> retrievePastingContext(FlexoObject focusedObject, List<FlexoObject> globalSelection,
			FlexoClipboard clipboard) {
		return new DefaultPastingContext(focusedObject);
	}

	/**
	 * Default implementation does nothing
	 */
	@Override
	public void prepareClipboardForPasting(FlexoClipboard clipboard, PastingContext<T> pastingContext) {
		PasteAction.logger.info("prepareClipboardForPasting() called in DefaultPasteHandler");
	}

	/**
	 * Default implementation of paste using PAMELA framework directives
	 */
	@Override
	public Object paste(FlexoClipboard clipboard, PastingContext<T> pastingContext) {
		// System.out.println("===========================>>>>>>>>>>>>> OK, we perform paste now with clipboard: ");
		// System.out.println(clipboard.debug());
		// System.out.println("Perform paste in pastingContext=" + pastingContext);

		try {
			return clipboard.getLeaderClipboard().getModelFactory().paste(clipboard.getLeaderClipboard(),
					pastingContext.getPastingPointHolder());
		} catch (ModelExecutionException e) {
			e.printStackTrace();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Default implementation does nothing
	 */
	@Override
	public void finalizePasting(FlexoClipboard clipboard, PastingContext<T> pastingContext) {
		PasteAction.logger.info("finalizePasting() called in DefaultPasteHandler");
		try {
			clipboard.getLeaderClipboard().consume();
		} catch (ModelExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModelDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
