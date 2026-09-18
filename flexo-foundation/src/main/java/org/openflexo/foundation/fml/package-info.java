/**
 * The FML meta-model: the object form of a FML source ({@code .fml} file), defined as PAMELA entities.
 * <p>
 * A {@code .fml} file is parsed into a {@link FMLCompilationUnit} (the parser and the pretty-printer live in the {@code fml-parser} module,
 * the grammar in {@code fml.sablecc}). The main structure is:
 * <ul>
 * <li>{@link FMLCompilationUnit}: namespace, use, import and typedef declarations, followed by exactly one {@link VirtualModel}</li>
 * <li>{@link VirtualModel} (keyword {@code model}): the root {@link FlexoConcept} of the compilation unit</li>
 * <li>{@link FlexoConcept} (keyword {@code concept}): declares {@link FlexoProperty properties}, {@link FlexoBehaviour behaviours},
 * invariants and nested concepts</li>
 * <li>{@link FlexoBehaviour}: its body is a control graph ({@link org.openflexo.foundation.fml.controlgraph.FMLControlGraph}) whose
 * leaves are edition actions ({@link org.openflexo.foundation.fml.editionaction.EditionAction})</li>
 * </ul>
 * Example (excerpt of {@code FML/Library.fml} in the {@code flexo-test-resources} test resource center, run by
 * {@code TestLibrary.fmlscript}):
 *
 * <pre>
 * use org.openflexo.foundation.fml.rt.FMLRTModelSlot as FMLRT;
 *
 * import java.util.List;
 *
 * &#64;URI("http://openflexo.org/test/TestResourceCenter/Library.fml")
 * &#64;Version("1.0")
 * public model Library {
 *
 *     public List&lt;Shelf&gt; allShelves() {
 *         return select Shelf from this;
 *     }
 *
 *     public concept Shelf {
 *         String label;
 *         ...
 *         public concept Book {
 *             String title;
 *             ...
 *         }
 *
 *         public concept Novel extends Book {
 *             String author;
 *             ...
 *         }
 *     }
 * }
 * </pre>
 *
 * Sub-packages:
 * <ul>
 * <li>{@code controlgraph}: control structures of behaviour bodies (sequence, conditional, iterations)</li>
 * <li>{@code editionaction}: atomic actions (declarations, assignations, fetch requests, technology-specific actions)</li>
 * <li>{@code rt}: FML@RT, the run-time model (instances of virtual models and concepts) and the execution of FML</li>
 * <li>{@code binding}: binding models and path elements giving Connie expressions access to FML objects</li>
 * <li>{@code rm}: resources storing compilation units and the artefacts contained in their directory</li>
 * <li>{@code md}: metadata, the annotations ({@code @Xxx(...)}) attached to FML declarations</li>
 * <li>{@code expr}, {@code validation}, {@code inspector}, {@code action}, {@code ta}: FML expressions, validation rules, concept
 * inspectors, editing actions, and FML exposed as a technology</li>
 * </ul>
 * FML must not be confused with FML-script, the imperative command language used to drive FML models.
 */
package org.openflexo.foundation.fml;
