/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.foundation;

import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * This class represents an observable object, or "data" in the model-view paradigm. It can be subclassed to represent an object that the
 * application wants to have observed.
 * <p>
 * An observable object can have one or more observers. An observer may be any object that implements interface <tt>FlexoObserver</tt>.
 * After an observable instance changes, an application calling the <code>FlexoObservable</code>'s <code>notifyObservers</code> method
 * causes all of its observers to be notified of the change by a call to their <code>update</code> method.
 * <p>
 * The order in which notifications will be delivered is unspecified. The default implementation provided in the Observerable class will
 * notify Observers in the order in which they registered interest, but subclasses may change this order, use no guaranteed order, deliver
 * notifications on separate threads, or may guarantee that their subclass follows this order, as they choose.
 * <p>
 * Note that this notification mechanism is has nothing to do with threads and is completely separate from the <tt>wait</tt> and
 * <tt>notify</tt> mechanism of class <tt>Object</tt>.
 * <p>
 * When an observable object is newly created, its set of observers is empty. Two observers are considered the same if and only if the
 * <tt>equals</tt> method returns true for them.
 * <p>
 * Additionnaly, this class manages other observers than <tt>FlexoObserver</tt> instances with a delegate <tt>Observable</tt> instance: this
 * is because FlexoInspector doesn't not use <tt>FlexoObserver</tt> class, as this is a Flexo-external project.
 * <p>
 * Features allowing to totally enable or disable observing scheme (or some classes) have also been included.
 * 
 * <br>
 * NB: this class has been rewrited from {@link java.util.Observable}, because Java doesn't support multiple inheritance.
 * 
 * @author sguerin
 * @see FlexoObservable#notifyObservers()
 * @see FlexoObserver
 *
 * 
 */
public abstract class FlexoObservable extends KVCFlexoObject implements HasPropertyChangeSupport {

	private static final Logger logger = Logger.getLogger(FlexoObservable.class.getPackage().getName());

	public static final String DELETED_PROPERTY = "deleted";

	private boolean changed = false;

	private final ArrayList<WeakReference<FlexoObserver>> flexoObservers = new ArrayList<>();

	private final PropertyChangeSupport pcSupport = new PropertyChangeSupport(this);

	/**
	 * This hastable stores for all classes encountered as observers for this observable a property coded as a Boolean indicating if
	 * notifications should be fired.
	 */
	protected final HashMap<Class<?>, Boolean> observerClasses = new HashMap<>();

	/**
	 * This flag codes the necessity to fire notifications or not
	 */
	protected boolean enableObserving = true;

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	/**
	 * Adds an observer to the set of observers for this object, provided that it is not the same as some observer already in the set. The
	 * order in which notifications will be delivered to multiple observers is not specified. See the class comment.
	 * 
	 * @param o
	 *            an observer to be added.
	 * @throws NullPointerException
	 *             if the parameter o is null.
	 */
	public void addObserver(FlexoObserver o) {
		if (o == null) {
			throw new NullPointerException();
		}
		synchronized (flexoObservers) {

			if (!isObservedBy(o)) {
				flexoObservers.add(new WeakReference<>(o));
				if (observerClasses.get(o.getClass()) == null) {
					// Add an entry for this kind of observer
					observerClasses.put(o.getClass(), Boolean.TRUE);
				}
			}
		}
	}

	/**
	 * Deletes an observer from the set of observers of this object.
	 * 
	 * @param o
	 *            the observer to be deleted.
	 */
	public void deleteObserver(FlexoObserver o) {
		synchronized (flexoObservers) {
			Iterator<WeakReference<FlexoObserver>> i = flexoObservers.iterator();
			while (i.hasNext()) {
				WeakReference<FlexoObserver> reference = i.next();
				if (reference.get() == null) {
					i.remove();
				}
				else if (reference.get() == o) {
					i.remove();
					break;
				}
			}
		}
	}

	/**
	 * If this object has changed, as indicated by the <code>hasChanged</code> method, then notify all of its observers and then call the
	 * <code>clearChanged</code> method to indicate that this object has no longer changed.
	 * <p>
	 * Each observer has its <code>update</code> method called with two arguments: this observable object and <code>null</code>. In other
	 * words, this method is equivalent to: <blockquote><tt>
	 * notifyFlexoObservers(null)</tt></blockquote>
	 * 
	 * @see java.util.Observable#clearChanged()
	 * @see java.util.Observable#hasChanged()
	 */
	public void notifyObservers() {
		notifyObservers(null);
	}

	/**
	 * If this object has changed, as indicated by the <code>hasChanged</code> method, then notify all of its observers and then call the
	 * <code>clearChanged</code> method to indicate that this object has no longer changed.
	 * <p>
	 * Each observer has its <code>update</code> method called with two arguments: this observable object and the <code>arg</code> argument.
	 * 
	 * @param arg
	 *            any object.
	 * @see java.util.Observable#clearChanged()
	 * @see java.util.Observable#hasChanged()
	 */
	public void notifyObservers(DataModification<?> arg) {

		if (enableObserving) {
			/*
			 * a temporary array buffer, used as a snapshot of the state of
			 * current FlexoObservers.
			 */
			@SuppressWarnings("unchecked")
			WeakReference<FlexoObserver>[] arrLocal1 = new WeakReference[flexoObservers.size()];

			synchronized (this) {
				/*
				 * We don't want the FlexoObserver doing callbacks into
				 * arbitrary code while holding its own Monitor. The code where
				 * we extract each Observable from the Vector and store the
				 * state of the FlexoObserver needs synchronization, but
				 * notifying observers does not (should not). The worst result
				 * of any potential race-condition here is that: 1) a
				 * newly-added FlexoObserver will miss a notification in
				 * progress 2) a recently unregistered FlexoObserver will be
				 * wrongly notified when it doesn't care
				 */
				if (!changed /*&& !(arg instanceof ResourceStatusModification)*/) {
					return;
				}
				arrLocal1 = flexoObservers.toArray(arrLocal1);
				clearChanged();
			}

			// Notify all Flexo observers of observing for this kind of observer
			// is enabled
			for (int i = arrLocal1.length - 1; i >= 0; i--) {
				WeakReference<FlexoObserver> weakRef = arrLocal1[i];
				if (weakRef != null) {
					FlexoObserver o = arrLocal1[i].get();
					if (o == null) {
						flexoObservers.remove(arrLocal1[i]);
						continue;
					}
					if (observerClasses.get(o.getClass()).booleanValue()) {
						o.update(this, arg);
					}
				}
			}

			if (arg != null) {
				pcSupport.firePropertyChange(arg.propertyName() != null ? arg.propertyName() : "", arg.oldValue(), arg.newValue());
			}
		}

	}

	/**
	 * Build and return a Vector of all current observers, as a snapshot of the state of current FlexoObservers and Inspector observers. Be
	 * careful with method such as indexOf, contains, etc... which usually rely on equals() method. They have been overridden to use
	 * explicitly the == operator.
	 */
	public Vector<FlexoObserver> getAllObservers() {
		Vector<FlexoObserver> returned = new Vector<FlexoObserver>() {

			@Override
			public synchronized int indexOf(Object o, int index) {
				for (int i = index; i < size(); i++) {
					if (elementData[i] == o) {
						return i;
					}
				}
				return -1;
			}

		};
		Iterator<WeakReference<FlexoObserver>> i = flexoObservers.iterator();
		while (i.hasNext()) {
			WeakReference<FlexoObserver> reference = i.next();
			if (reference.get() == null) {
				i.remove();
			}
			else {
				returned.add(reference.get());
			}
		}
		return returned;
	}

	/**
	 * Prints array of all current observers, as a snapshot of the state of current FlexoObservers.
	 */
	public void printObservers() {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Observers of: " + getClass().getName() + " / " + this);
		}
		int i = 0;
		for (FlexoObserver o : getAllObservers()) {
			if (logger.isLoggable(Level.INFO)) {
				logger.info(" * " + i + " hash= " + Integer.toHexString(o.hashCode()) + " FlexoObserver: " + o.getClass().getName() + " / "
						+ o);
			}
			i++;
		}
	}

	/**
	 * Clears the observer list so that this object no longer has any observers.
	 */
	public synchronized void deleteObservers() {
		synchronized (flexoObservers) {
			flexoObservers.clear();
		}
	}

	/**
	 * Marks this <tt>Observable</tt> object as having been changed; the <tt>hasChanged</tt> method will now return <tt>true</tt>.
	 */
	public synchronized void setChanged() {
		changed = true;
	}

	/**
	 * Indicates that this object has no longer changed, or that it has already notified all of its observers of its most recent change, so
	 * that the <tt>hasChanged</tt> method will now return <tt>false</tt>. This method is called automatically by the
	 * <code>notifyFlexoObservers</code> methods.
	 * 
	 */
	public synchronized void clearChanged() {
		changed = false;
	}

	/**
	 * Tests if this object has changed.
	 * 
	 * @return <code>true</code> if and only if the <code>setChanged</code> method has been called more recently than the
	 *         <code>clearChanged</code> method on this object; <code>false</code> otherwise.
	 * @see java.util.Observable#clearChanged()
	 * @see java.util.Observable#setChanged()
	 */
	public boolean hasChanged() {
		return changed;
	}

	/**
	 * Returns the number of observers of this <tt>Observable</tt> object.
	 * 
	 * @return the number of observers of this object.
	 */
	public int countObservers() {
		return flexoObservers.size();
	}

	/**
	 * Enable observing. Does not affect disabled observing classes
	 */
	public synchronized void enableObserving() {
		enableObserving = true;
	}

	/**
	 * Disable observing.
	 */
	public synchronized void disableObserving() {
		enableObserving = false;
	}

	/**
	 * Enable observing for all observers of class 'observerClass' and all related subclasses
	 */
	public synchronized void enableObserving(Class<?> observerClass) {
		for (Class<?> aClass : observerClasses.keySet()) {
			if (observerClass.isAssignableFrom(aClass)) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Enable observing for " + ((Class<?>) aClass).getName());
				}
				observerClasses.put(aClass, true);
			}
		}
	}

	/**
	 * Disable observing for all observers of class 'observerClass' and all related subclasses
	 */
	public synchronized void disableObserving(Class<?> observerClass) {
		for (Class<?> aClass : observerClasses.keySet()) {
			if (observerClass.isAssignableFrom(aClass)) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Disable observing for " + ((Class<?>) aClass).getName());
				}
				observerClasses.put(aClass, false);
			}
		}
	}

	public static boolean areSameValue(Object o1, Object o2) {
		if (o1 == null)
			return o2 == null;
		if (o1.equals(o2))
			return true;
		if (o1 instanceof Number && o2 instanceof Number) {
			Number n1 = (Number) o1;
			Number n2 = (Number) o2;
			long l1 = n1.longValue();
			long l2 = n2.longValue();
			if (l1 != l2)
				return false;
			return n1.doubleValue() == n2.doubleValue();
		}
		return false;
	}

	public boolean isObservedBy(FlexoObserver observer) {
		synchronized (flexoObservers) {
			Iterator<WeakReference<FlexoObserver>> i = flexoObservers.iterator();
			while (i.hasNext()) {
				WeakReference<FlexoObserver> reference = i.next();
				if (reference.get() == null) {
					i.remove();
				}
				else if (reference.get() == observer) {
					return true;
				}
			}
			return false;
		}
	}
}
