/**
 * The control graph of FML: the structure of the body of a behaviour, of the get and set blocks of a property, and of an invariant (see
 * {@link FMLControlGraph}).
 * <p>
 * The FML statements of a body map to the following classes:
 * <ul>
 * <li>a block of statements {@code { a; b; c; }}: nested {@link Sequence}s, each one holding two control graphs;
 * {@link FMLControlGraph#getFlattenedSequence()} returns the flat list of statements</li>
 * <li>an empty block: {@link EmptyControlGraph}</li>
 * <li>{@code if (condition) ... [else ...]}: {@link ConditionalAction}</li>
 * <li>{@code while (condition) ...}: {@link WhileAction}</li>
 * <li>{@code for (Type item : expression) ...}, including {@code for (Type item : select ...)}: {@link IterationAction}</li>
 * <li>{@code for (init; condition; update) ...}: currently parsed as an {@link ExpressionIterationAction}</li>
 * </ul>
 * {@link IncrementalIterationAction} is not yet produced by the FML parser: it is not implemented yet, and will be in a future version.
 * <p>
 * Control structures are {@link ControlStructureAction}s, hence {@link org.openflexo.foundation.fml.editionaction.EditionAction}s, whereas
 * a {@link Sequence} is not. The atomic actions are described in {@link org.openflexo.foundation.fml.editionaction}.
 * <p>
 * Example (excerpt of {@code FML/Library.fml} in the {@code flexo-test-resources} test resource center):
 *
 * <pre>
 * public int countNovels() {
 *     int count = 0;
 *     for (Book book : books) {
 *         if (book instanceof Novel) {
 *             count = count + 1;
 *         }
 *     }
 *     return count;
 * }
 * </pre>
 *
 * The body is a {@link Sequence} of a declaration, an {@link IterationAction} whose body is a {@link ConditionalAction}, and a return
 * statement.
 */
package org.openflexo.foundation.fml.controlgraph;
