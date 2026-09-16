/**
 * The edition actions dedicated to FML@RT: selection of concept instances and VirtualModelInstances, matching, and events.
 * <p>
 * The FML statements map to the following classes:
 * <ul>
 * <li>{@code select [unique] Concept from (expression) [where (...)]}: {@link SelectFlexoConceptInstance} or
 * {@link SelectUniqueFlexoConceptInstance} (see {@link AbstractSelectFlexoConceptInstance}); for a VirtualModel,
 * {@link SelectVirtualModelInstance} or {@link SelectUniqueVirtualModelInstance}</li>
 * <li>{@code MatchingSet<Concept> matchingSet = begin match Concept from (expression);}: {@link InitiateMatching}</li>
 * <li>{@code match Concept in matchingSet from (expression) where (property=value, ...) unmatched: new Concept(...);}:
 * {@link MatchFlexoConceptInstance}, each criterion being a {@link MatchingCriteria}</li>
 * <li>{@code end match Concept in matchingSet unmatched: delete();}: {@link FinalizeMatching}</li>
 * <li>{@code fire expression;}: {@link FireEvent}</li>
 * </ul>
 * The instance creation {@code new Concept(...)} is an expression, not an edition action (see
 * {@link org.openflexo.foundation.fml.binding.CreationSchemePathElement}). The actions declared by
 * {@link org.openflexo.foundation.fml.rt.FMLRTModelSlot} can also be written {@code FMLRT::Action(...)} in a compilation unit using that
 * model slot ({@code use org.openflexo.foundation.fml.rt.FMLRTModelSlot as FMLRT;}); among them, {@link AddFlexoConceptInstance} and
 * {@link ExecuteFlexoBehaviour} are deprecated, as is {@link DeleteFlexoConceptInstance}.
 * <p>
 * Example (excerpt of {@code FML/Library.fml} in the {@code flexo-test-resources} test resource center):
 *
 * <pre>
 * public int synchronizeIndex() {
 *     MatchingSet&lt;IndexEntry&gt; entries = begin match IndexEntry from this;    // InitiateMatching
 *     for (Shelf shelf : select Shelf from this) {                           // SelectFlexoConceptInstance
 *         for (Book b : shelf.books) {
 *             match IndexEntry in entries from this where (book=b)           // MatchFlexoConceptInstance
 *                 unmatched: new IndexEntry(b);
 *         }
 *     }
 *     end match IndexEntry in entries unmatched: delete();                   // FinalizeMatching
 *     List&lt;IndexEntry&gt; all = select IndexEntry from this;
 *     return all.size;
 * }
 * </pre>
 */
package org.openflexo.foundation.fml.rt.editionaction;
