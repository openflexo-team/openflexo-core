/**
 * The resources storing the instances of virtual models.
 * <ul>
 * <li>{@link FMLRTVirtualModelInstanceResource}: the resource of a {@link org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance},
 * conform to the {@link org.openflexo.foundation.fml.rm.CompilationUnitResource} of the VirtualModel it instantiates</li>
 * <li>{@link FMLRTVirtualModelInstanceResourceFactory}: creates and retrieves them, and registers them in the repository of their resource
 * center</li>
 * </ul>
 * An instance is stored as a directory holding one XML file:
 *
 * <pre>
 * AcmeModel.fml.rt/
 *     AcmeModel.fml.rt.xml     the instance, its concept instances and their actor references
 * </pre>
 *
 * Unlike a compilation unit, which is stored as FML source, an instance is serialized in XML. An instance may contain other instances,
 * stored in its own directory; an instance stored in the {@code .fml} directory of a VirtualModel is a resource contained in that
 * compilation unit.
 */
package org.openflexo.foundation.fml.rt.rm;
