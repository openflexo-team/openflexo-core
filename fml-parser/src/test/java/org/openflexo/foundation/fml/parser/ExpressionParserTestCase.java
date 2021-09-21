package org.openflexo.foundation.fml.parser;

import org.junit.BeforeClass;
import org.openflexo.connie.BindingEvaluationContext;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.ContextualizedBindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DefaultContextualizedBindable;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.Constant;
import org.openflexo.connie.expr.Expression;
import org.openflexo.connie.expr.ExpressionEvaluator;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.DefaultFlexoServiceManager;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FMLTypingSpace;
import org.openflexo.foundation.fml.expr.FMLExpressionEvaluator;
import org.openflexo.foundation.fml.expr.FMLPrettyPrinter;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.localization.LocalizationService;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.pamela.exceptions.ModelDefinitionException;

import junit.framework.TestCase;

/**
 * Generic utility class used to test FML expression parsing
 * 
 * @author sylvain
 *
 */
public abstract class ExpressionParserTestCase extends TestCase {

	protected static FlexoServiceManager serviceManager;
	private static FMLPrettyPrinter prettyPrinter = new FMLPrettyPrinter();

	private AbstractFMLTypingSpace typingSpace;
	private ContextualizedBindable bindable;

	@BeforeClass
	public static void initServiceManager() {
		instanciateTestServiceManager();
	}

	protected static FlexoServiceManager instanciateTestServiceManager() {
		serviceManager = new DefaultFlexoServiceManager(null, true) {

			@Override
			protected LocalizationService createLocalizationService(String relativePath) {
				LocalizationService returned = super.createLocalizationService(relativePath);
				returned.setAutomaticSaving(false);
				return returned;
			}

			@Override
			protected FlexoEditingContext createEditingContext() {
				// In unit tests, we do NOT want to be warned against unexpected
				// edits
				return FlexoEditingContext.createInstance(false);
			}

			@Override
			protected DefaultFlexoEditor createApplicationEditor() {
				return new DefaultFlexoEditor(null, this);
			}

			@Override
			protected ProjectLoader createProjectLoaderService() {
				return new ProjectLoader();
			}

		};

		serviceManager.getLocalizationService().setAutomaticSaving(false);

		// Activate both FML and FML@RT technology adapters
		TechnologyAdapterService taService = serviceManager.getTechnologyAdapterService();
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLTechnologyAdapter.class), true);
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class), true);

		return serviceManager;
	}

	public AbstractFMLTypingSpace getTypingSpace() {
		return typingSpace;
	}

	protected Expression tryToParse(String anExpression, String expectedEvaluatedExpression,
			Class<? extends Expression> expectedExpressionClass, Object expectedEvaluation, FlexoServiceManager serviceManager,
			boolean shouldFail) {

		System.out.println("Parsing FML expression " + anExpression);
		FMLModelFactory fmlModelFactory;
		try {
			fmlModelFactory = new FMLModelFactory(null, serviceManager);

			typingSpace = new FMLTypingSpace(null);
			bindable = new DefaultContextualizedBindable(typingSpace) {
				@Override
				public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
				}

				@Override
				public void notifiedBindingChanged(DataBinding<?> dataBinding) {
				}

				@Override
				public BindingModel getBindingModel() {
					return null;
				}

				@Override
				public BindingFactory getBindingFactory() {
					return null;
				}

			};
			System.out.println("Parsing... " + anExpression);

			Expression parsed = FMLExpressionParser.parse(anExpression, bindable, typingSpace, fmlModelFactory);
			System.out.println("parsed=" + parsed);
			if (parsed == null) {
				fail("Cannot parse: " + anExpression);
			}
			Expression evaluated = parsed.evaluate(new BindingEvaluationContext() {
				@Override
				public Object getValue(BindingVariable variable) {
					return null;
				}

				@Override
				public ExpressionEvaluator getEvaluator() {
					return new FMLExpressionEvaluator(this);
				}
			});

			System.out.println("evaluated=" + evaluated);
			System.out.println("Successfully parsed as : " + parsed.getClass().getSimpleName());
			System.out.println("prettyPrinter:" + prettyPrinter);
			System.out.println("Normalized: " + prettyPrinter.getStringRepresentation(parsed, bindable));
			System.out.println("Evaluated: " + prettyPrinter.getStringRepresentation(evaluated, bindable));
			if (shouldFail) {
				fail();
			}
			assertTrue(expectedExpressionClass.isAssignableFrom(parsed.getClass()));
			if (expectedEvaluatedExpression != null) {
				assertEquals(expectedEvaluatedExpression, prettyPrinter.getStringRepresentation(evaluated, bindable));
			}
			if (expectedEvaluation != null) {
				if (!(evaluated instanceof Constant)) {
					fail("Evaluated value is not a constant (expected: " + expectedEvaluation + ") but " + expectedEvaluation);
				}
				if (expectedEvaluation instanceof Number) {
					Object value = ((Constant<?>) evaluated).getValue();
					if (value instanceof Number) {
						assertEquals(((Number) expectedEvaluation).doubleValue(), ((Number) value).doubleValue());
					}
					else {
						fail("Evaluated value is not a number (expected: " + expectedEvaluation + ") but " + expectedEvaluation);
					}
				}
				else {
					assertEquals(expectedEvaluation, ((Constant<?>) evaluated).getValue());
				}
			}
			return parsed;
		} catch (ParseException e) {
			if (!shouldFail) {
				e.printStackTrace();
				fail();
			}
			else {
				System.out.println("Parsing " + anExpression + " has failed as expected: " + e.getMessage());
			}
			return null;
		} catch (TypeMismatchException e) {
			if (!shouldFail) {
				e.printStackTrace();
				fail();
			}
			else {
				System.out.println("Parsing " + anExpression + " has failed as expected: " + e.getMessage());
			}
			return null;
		} catch (NullReferenceException e) {
			if (!shouldFail) {
				e.printStackTrace();
				fail();
			}
			else {
				System.out.println("Parsing " + anExpression + " has failed as expected: " + e.getMessage());
			}
			return null;
		} catch (ReflectiveOperationException e) {
			fail();
			return null;
		} catch (ModelDefinitionException e) {
			fail();
			return null;
		}

	}

}
