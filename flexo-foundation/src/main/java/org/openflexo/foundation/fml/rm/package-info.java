/**
 * The resources of FML: what a {@code .fml} directory holds, and the objects managing it.
 * <ul>
 * <li>{@link CompilationUnitResource}: the resource of a {@code Xxx.fml/} directory, storing the FML source parsed into a
 * {@link org.openflexo.foundation.fml.FMLCompilationUnit}</li>
 * <li>{@link CompilationUnitResourceFactory}: creates and retrieves those resources, registers them in the
 * {@link org.openflexo.foundation.fml.CompilationUnitRepository} of their resource center and in the
 * {@link org.openflexo.foundation.fml.VirtualModelLibrary}, then explores the VirtualModels they contain</li>
 * <li>{@link FIBComponentResource} and {@link LocalizedDictionaryResource}: the user interface components ({@code .fib},
 * {@code .inspector}) and the {@code Localized/} dictionary stored in that same directory, which are resources of their own</li>
 * </ul>
 * A compilation unit is stored as a directory, which also holds the resources contained in it:
 *
 * <pre>
 * Library.fml/                 the CompilationUnitResource
 *     Library.fml              the FML source
 *     Catalog.fml/             a contained VirtualModel, itself a CompilationUnitResource
 *         Catalog.fml
 * </pre>
 *
 * The URI of a compilation unit is the one declared by {@code @URI}; without it, the URI is computed from the resource center (its default
 * base URI followed by the relative path of the directory). The information needed before parsing — URI, version, used model slots,
 * concepts — is read from the header of the source and cached in the metadata of the resource center.
 * <p>
 * Note that the implementation of {@link CompilationUnitResource} lives in another module (flexo-foundation-rm) and is bound reflectively
 * by {@link CompilationUnitResourceFactory}.
 * <p>
 * The instances of virtual models are stored in {@code .fml.rt} resources: see {@link org.openflexo.foundation.fml.rt.rm}.
 */
package org.openflexo.foundation.fml.rm;
