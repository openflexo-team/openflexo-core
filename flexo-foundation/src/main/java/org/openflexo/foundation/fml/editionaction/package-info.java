/**
 * The edition actions of FML: the actions of which the body of a behaviour is made (see {@link EditionAction}).
 * <p>
 * The FML statements map to the following classes:
 * <ul>
 * <li>{@code Type name = value;}: {@link DeclarationAction}</li>
 * <li>{@code target = value;}: {@link AssignationAction}</li>
 * <li>an expression used as a statement, such as {@code books.add(book);}: {@link ExpressionAction}</li>
 * <li>{@code return [value];}: {@link ReturnStatement}</li>
 * <li>{@code delete expression;}: {@link DeleteAction}</li>
 * <li>{@code log expression;}: {@link LogAction}</li>
 * <li>{@code notify expression;}: {@link NotifyPropertyChangedAction}</li>
 * <li>{@code connect target using expression;}: {@link ConnectAction}</li>
 * <li>{@code select [unique] Type from (expression) [where (...)]}: a fetch request ({@link AbstractFetchRequest})</li>
 * <li>{@code TA::Action(...)}: a {@link TechnologySpecificAction} contributed by a technology adapter</li>
 * </ul>
 * The actions producing a value are {@link AssignableAction}s: they are used as right-hand side of a declaration, of an assignation or of a
 * return statement. Note that an instance creation {@code new Book(...)} is an expression, not a dedicated action.
 * <p>
 * The actions dedicated to FML@RT (fetch requests on concept instances, {@code match}, {@code fire}...) live in
 * {@link org.openflexo.foundation.fml.rt.editionaction}; control structures live in {@link org.openflexo.foundation.fml.controlgraph}.
 * <p>
 * Example (excerpt of {@code FML/Library.fml} in the {@code flexo-test-resources} test resource center):
 *
 * <pre>
 * public Book newBook(required String title) {
 *     Book book = new Book(parameters.title);  // DeclarationAction
 *     books.add(book);                         // ExpressionAction
 *     return book;                             // ReturnStatement
 * }
 * </pre>
 */
package org.openflexo.foundation.fml.editionaction;
