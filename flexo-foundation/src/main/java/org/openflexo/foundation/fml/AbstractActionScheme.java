/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.fml;

import java.lang.reflect.InvocationTargetException;

import java.util.Objects;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.DataBinding.CachingStrategy;
import org.openflexo.connie.DefaultBindable;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.md.FMLMetaData;
import org.openflexo.foundation.fml.md.SingleMetaData;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.AbstractActionSchemeActionFactory;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.toolbox.StringUtils;

/**
 * Base of the behaviours executed on an existing {@link FlexoConceptInstance} ({@link ActionScheme}, {@link DeletionScheme},
 * {@link NavigationScheme}...), as opposed to the creation schemes (see {@link AbstractCreationScheme}).
 *
 * @author sylvain
 *
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractActionScheme.AbstractActionSchemeImpl.class)
public abstract interface AbstractActionScheme extends FlexoBehaviour {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONDITIONAL_KEY = "conditional";

	// Not sure this is a good idea in the language
	@Deprecated
	@Getter(value = CONDITIONAL_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getConditional();

	@Deprecated
	@Setter(CONDITIONAL_KEY)
	public void setConditional(DataBinding<Boolean> conditional);

	/**
	 * Whether this behaviour is enabled on supplied instance: its {@link #getEnable() @Enable} expression, and the deprecated
	 * {@link #getConditional() conditional}, both of which are true when not set
	 */
	public boolean evaluateCondition(FlexoConceptInstance flexoConceptInstance);

	/**
	 * Whether this behaviour is offered on supplied instance: its {@link #getVisible() @Visible} expression, true when not set
	 */
	public boolean evaluateVisible(FlexoConceptInstance flexoConceptInstance);

	/** Metadata (<code>@Visible(expression)</code>) holding {@link #getVisible()} */
	public static final String VISIBLE_METADATA = "Visible";
	/** Metadata (<code>@Enable(expression)</code>) holding {@link #getEnable()} */
	public static final String ENABLE_METADATA = "Enable";

	public static final String VISIBLE_KEY = "visible";
	public static final String ENABLE_KEY = "enable";

	/**
	 * The expression telling whether this behaviour is offered (menus, browsers, buttons...) on an instance: when it evaluates to false the
	 * behaviour is hidden. Stored as the {@link #VISIBLE_METADATA} metadata (the <code>@Visible</code> annotation). It is expressed in the
	 * binding model of the concept owning the behaviour (see {@link #getAvailabilityContext()}), and evaluated on the instance.
	 */
	public DataBinding<Boolean> getVisible();

	public void setVisible(DataBinding<Boolean> visible);

	/**
	 * The expression telling whether this behaviour can be performed on an instance: when it evaluates to false the behaviour is offered
	 * but disabled. Stored as the {@link #ENABLE_METADATA} metadata (the <code>@Enable</code> annotation). Same context as
	 * {@link #getVisible()}. It supersedes the deprecated {@link #getConditional()}.
	 */
	public DataBinding<Boolean> getEnable();

	public void setEnable(DataBinding<Boolean> enable);

	/**
	 * The {@link Bindable} in which {@link #getVisible()} and {@link #getEnable()} are expressed: the binding model of the concept owning
	 * this behaviour
	 */
	public Bindable getAvailabilityContext();

	AbstractActionSchemeActionFactory<?, ?, ?> getActionFactory(FlexoConceptInstance fci);

	public static abstract class AbstractActionSchemeImpl extends FlexoBehaviourImpl implements AbstractActionScheme {

		private DataBinding<Boolean> conditional;

		public AbstractActionSchemeImpl() {
			super();
		}

		// TODO: we had to set caching strategy to NO_CACHING in dynamic instantiation context (CTA project)
		@Override
		@Deprecated
		public DataBinding<Boolean> getConditional() {
			if (conditional == null) {
				conditional = new DataBinding<>(this, Boolean.class, DataBinding.BindingDefinitionType.GET);
				conditional.setCachingStrategy(CachingStrategy.NO_CACHING);
				conditional.setBindingName("conditional");
			}
			return conditional;
		}

		// TODO: we had to set caching strategy to NO_CACHING in dynamic instantiation context (CTA project)
		@Override
		@Deprecated
		public void setConditional(DataBinding<Boolean> conditional) {
			if (conditional != null) {
				conditional.setOwner(this);
				conditional.setDeclaredType(Boolean.class);
				conditional.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				conditional.setCachingStrategy(CachingStrategy.NO_CACHING);
				conditional.setBindingName("conditional");
			}
			this.conditional = conditional;
		}

		@Override
		public boolean evaluateCondition(FlexoConceptInstance flexoConceptInstance) {
			return evaluate(getConditional(), flexoConceptInstance) && evaluate(getEnable(), flexoConceptInstance);
		}

		@Override
		public boolean evaluateVisible(FlexoConceptInstance flexoConceptInstance) {
			return evaluate(getVisible(), flexoConceptInstance);
		}

		/**
		 * Evaluate supplied expression on supplied instance: true when it is not set, not valid, null, or fails
		 */
		private boolean evaluate(DataBinding<Boolean> expression, FlexoConceptInstance flexoConceptInstance) {
			if (expression.isSet() && expression.isValid()) {
				try {
					Boolean returned = expression.getBindingValue(flexoConceptInstance);
					if (returned != null) {
						return returned;
					}
					return true;
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					// A path through a null: nothing to say about the behaviour
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
			return true;
		}

		private AvailabilityContext availabilityContext;
		private final AvailabilityExpression visible = new AvailabilityExpression(VISIBLE_METADATA, VISIBLE_KEY);
		private final AvailabilityExpression enable = new AvailabilityExpression(ENABLE_METADATA, ENABLE_KEY);

		@Override
		public Bindable getAvailabilityContext() {
			if (availabilityContext == null) {
				availabilityContext = new AvailabilityContext();
			}
			return availabilityContext;
		}

		@Override
		public DataBinding<Boolean> getVisible() {
			return visible.get();
		}

		@Override
		public void setVisible(DataBinding<Boolean> visible) {
			this.visible.set(visible);
		}

		@Override
		public DataBinding<Boolean> getEnable() {
			return enable.get();
		}

		@Override
		public void setEnable(DataBinding<Boolean> enable) {
			this.enable.set(enable);
		}

		/**
		 * One of the expressions of this behaviour stored as a metadata (the same way as the <code>@Renderer</code> of a concept): read
		 * from it, written back to it when edited, and read again from it when the metadata is edited (FML source edited then merged)
		 */
		private class AvailabilityExpression {

			private final String metadataKey;
			private final String key;
			private DataBinding<Boolean> binding;
			private FMLMetaData listenedMetaData;
			private boolean listeningToMetaDataList = false;
			/** True while this expression writes its own metadata, which must not be taken for an edition of the FML text */
			private boolean writing = false;
			/**
			 * Text of the expression as last seen: the metadata holds the very same {@link DataBinding}, which is edited in place, so what
			 * the metadata says cannot tell an edition from a mere read (analyzing an expression notifies a change although nothing was
			 * edited: CORE-D-26)
			 */
			private String lastExpression;

			AvailabilityExpression(String metadataKey, String key) {
				this.metadataKey = metadataKey;
				this.key = key;
			}

			private void prepare(DataBinding<Boolean> expression) {
				expression.setOwner(getAvailabilityContext());
				expression.setDeclaredType(Boolean.class);
				expression.setBindingDefinitionType(BindingDefinitionType.GET);
				expression.setBindingName(key);
				// Evaluated on the instance of the moment, for a menu that may stay open: no cache (see conditional)
				expression.setCachingStrategy(CachingStrategy.NO_CACHING);
			}

			private DataBinding<Boolean> readFromMetaData() {
				DataBinding<Boolean> returned = getSingleMetaData(metadataKey, DataBinding.class);
				if (returned == null) {
					// Declared but carrying no decodable value (yet)
					returned = new DataBinding<>(getAvailabilityContext(), Boolean.class, BindingDefinitionType.GET);
				}
				prepare(returned);
				lastExpression = returned.toString();
				return returned;
			}

			DataBinding<Boolean> get() {
				if (binding == null) {
					listenToMetaDataList();
					if (hasMetaData(metadataKey)) {
						listen();
						binding = readFromMetaData();
					}
					else {
						binding = new DataBinding<>(getAvailabilityContext(), Boolean.class, BindingDefinitionType.GET);
						binding.setBindingName(key);
						binding.setCachingStrategy(CachingStrategy.NO_CACHING);
						lastExpression = binding.toString();
					}
				}
				return binding;
			}

			void set(DataBinding<Boolean> expression) {
				listenToMetaDataList();
				if (expression != null) {
					prepare(expression);
				}
				binding = expression;
				String previousExpression = lastExpression;
				lastExpression = expression != null ? expression.toString() : null;
				write();
				listen();
				getPropertyChangeSupport().firePropertyChange(key, null, expression);
				if (!Objects.equals(previousExpression, lastExpression)) {
					// An edition (the binding selector of the inspector hands a new binding over): the FML text and the resource must follow
					AbstractActionSchemeImpl.this.notifiedBindingChanged(expression);
				}
			}

			private void write() {
				writing = true;
				try {
					setSingleMetaData(metadataKey, binding, DataBinding.class);
				} finally {
					writing = false;
				}
			}

			private void listen() {
				FMLMetaData metaData = hasMetaData(metadataKey) ? getMetaData(metadataKey) : null;
				if (metaData == null || metaData == listenedMetaData) {
					return;
				}
				listenedMetaData = metaData;
				metaData.getPropertyChangeSupport().addPropertyChangeListener(evt -> {
					if (SingleMetaData.SERIALIZATION_REPRESENTATION_KEY.equals(evt.getPropertyName())) {
						binding = readFromMetaData();
						getPropertyChangeSupport().firePropertyChange(key, null, binding);
					}
				});
			}

			/**
			 * The metadata itself may appear or disappear (the annotation was typed in, or deleted from, the FML source, which was then
			 * merged): the expression must follow, whether or not it was read before
			 */
			private void listenToMetaDataList() {
				if (listeningToMetaDataList) {
					return;
				}
				listeningToMetaDataList = true;
				getPropertyChangeSupport().addPropertyChangeListener(META_DATA_KEY, evt -> {
					if (writing) {
						return;
					}
					FMLMetaData metaData = hasMetaData(metadataKey) ? getMetaData(metadataKey) : null;
					if (metaData == listenedMetaData && binding != null) {
						return;
					}
					binding = null;
					listenedMetaData = null;
					get();
					getPropertyChangeSupport().firePropertyChange(key, null, binding);
				});
			}

			/**
			 * Called when the expression was edited (in place, by the inspector for instance): writes it back to the metadata and
			 * notifies the FML text and the inspector
			 */
			void bindingChanged() {
				String expression = binding != null ? binding.toString() : null;
				if (Objects.equals(expression, lastExpression)) {
					return;
				}
				lastExpression = expression;
				if (!hasMetaData(metadataKey) && StringUtils.isEmpty(expression)) {
					return;
				}
				write();
				listen();
				getPropertyChangeSupport().firePropertyChange(key, null, binding);
				AbstractActionSchemeImpl.this.notifiedBindingChanged(binding);
			}
		}

		/**
		 * The {@link Bindable} owning the {@link AbstractActionScheme#getVisible()} and {@link AbstractActionScheme#getEnable()}
		 * expressions: they see the binding model of the concept owning the behaviour (<code>this</code>, its properties), not the
		 * parameters of the behaviour, which are not known when a behaviour is offered
		 */
		public class AvailabilityContext extends DefaultBindable {

			@Override
			public BindingFactory getBindingFactory() {
				return AbstractActionSchemeImpl.this.getBindingFactory();
			}

			@Override
			public BindingModel getBindingModel() {
				return getFlexoConcept() != null ? getFlexoConcept().getBindingModel() : null;
			}

			@Override
			public void notifiedBindingChanged(DataBinding<?> dataBinding) {
				if (dataBinding == visible.binding) {
					visible.bindingChanged();
				}
				else if (dataBinding == enable.binding) {
					enable.bindingChanged();
				}
			}

			@Override
			public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
				if (dataBinding == visible.binding || dataBinding == enable.binding) {
					AbstractActionSchemeImpl.this.notifiedBindingDecoded(dataBinding);
				}
			}
		}

		@Override
		public AbstractActionSchemeActionFactory<?, ?, ?> getActionFactory(FlexoConceptInstance fci) {
			return null;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getConditional().rebuild();
			getVisible().rebuild();
			getEnable().rebuild();
		}

	}
}
