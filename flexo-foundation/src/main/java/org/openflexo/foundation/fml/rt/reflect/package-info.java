/**
 * Reflected FML@RT: presenting the data of a technology-specific resource as instances of a {@link org.openflexo.foundation.fml.VirtualModel}.
 * <p>
 * Where a {@link org.openflexo.foundation.fml.rt.FMLRTModelSlot} gives access to a native instance stored in its own {@code .fml.rt}
 * resource, a {@link ReflectedFMLRTModelSlot} gives access to a foreign artefact — an XML document, a spreadsheet, a database, an HTTP
 * service — read through the contract of a VirtualModel:
 * <ul>
 * <li>{@link ReflectedFMLRTModelSlot}: the model slot, subclassed by each technology which offers this view</li>
 * <li>{@link ReflectedVirtualModelInstance}: the VirtualModelInstance reflecting the resource, built by
 * {@link ReflectedFMLRTModelSlot#reflectVirtualModelInstance}</li>
 * <li>{@link ReflectedFlexoConceptInstance}: a concept instance reflecting one object of the resource (its support object)</li>
 * <li>{@link ReflectedFMLRTModelSlotInstance}: the binding of the model slot to a concrete resource</li>
 * <li>{@link ReflectedVirtualModelInstanceModelFactory}: the factory building those objects</li>
 * </ul>
 * Such an instance is never serialized: it is a view over the foreign artefact, and only the URI of the reflected resource is stored, so
 * the instance is rebuilt on deserialization.
 */
package org.openflexo.foundation.fml.rt.reflect;
