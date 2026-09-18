/**
 * FML@RT: the run-time model of FML, made of the instances of the virtual models and concepts defined in FML, and of the machinery executing
 * FML on them.
 * <ul>
 * <li>{@link VirtualModelInstance} and {@link FlexoConceptInstance}: the instances of {@link org.openflexo.foundation.fml.VirtualModel}s and
 * of {@link org.openflexo.foundation.fml.FlexoConcept}s; the native implementation of a virtual model instance is
 * {@link FMLRTVirtualModelInstance}, stored in a {@code .fml.rt} resource</li>
 * <li>{@link ActorReference}: the value of a role in an instance; {@link ModelSlotInstance}: the value of a model slot</li>
 * <li>{@link RunTimeEvaluationContext}: a context in which FML is executed; {@link FMLRunTimeEngine}: the engine executing behaviours and
 * dispatching events</li>
 * </ul>
 * Sub-packages:
 * <ul>
 * <li>{@code action}: the executions of behaviours ({@link org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction}, one kind per kind
 * of behaviour)</li>
 * <li>{@code editionaction}: the edition actions dedicated to FML@RT (selection and deletion of instances, {@code match}, {@code fire}...)</li>
 * <li>{@code rm}: the resources storing {@link FMLRTVirtualModelInstance}s</li>
 * <li>{@code reflect}: the virtual model instances reflecting technology-specific resources</li>
 * <li>{@code logging}: the FML console</li>
 * </ul>
 * FML@RT must not be confused with FML-script, the command language which can be used to drive it.
 * <p>
 * Example (excerpt of {@code AutomatedTests/TestLibrary.fmlscript} in the {@code flexo-test-resources} test resource center, running the
 * {@code Library} model):
 *
 * <pre>
 * library = new Library() with (name="library");
 * libraryCatalog = library.createCatalog();
 * assert libraryCatalog.containerVirtualModelInstance == library;
 * fiction = library.newShelf("Fiction");
 * emma = fiction.newBook("Emma");
 * assert emma.container == fiction;
 * </pre>
 */
package org.openflexo.foundation.fml.rt;
