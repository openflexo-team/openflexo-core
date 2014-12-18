package org.openflexo.foundation;

import java.lang.reflect.Type;
import java.util.List;

import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;

/**
 * This is the default non-abstract implementation of {@link FlexoObject}.<br>
 * Use it only when you don't want to encode your model using PAMELA
 * 
 * TODO: This class should not be used anymore
 * 
 * @author sylvain
 * 
 */
@Deprecated
public class DefaultFlexoObject extends FlexoObjectImpl {

	// Following code relative to modified management is a temporary hack to manage modified status
	// TODO: DefaultFlexoObject should not be used anymore and PAMELA should be used instead
	private boolean localModified = false;

	@Override
	public boolean isModified() {
		return localModified;
	}

	@Override
	public void performSuperSetModified(boolean modified) {
		localModified = modified;
	}

	/*@Override
	public void setModified(boolean modified) {
		super.setModified(modified);
		localModified = modified;
	}

	@Override
	public synchronized void setIsModified() {
		super.setIsModified();
		localModified = true;
	}

	@Override
	public synchronized void clearIsModified() {
		super.clearIsModified();
		localModified = false;
	}*/

	@Override
	public void setCustomProperties(List<FlexoProperty> customProperties) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object performSuperGetter(String propertyIdentifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void performSuperSetter(String propertyIdentifier, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void performSuperAdder(String propertyIdentifier, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void performSuperRemover(String propertyIdentifier, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object performSuperGetter(String propertyIdentifier, Class<?> modelEntityInterface) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void performSuperSetter(String propertyIdentifier, Object value, Class<?> modelEntityInterface) {
		// TODO Auto-generated method stub

	}

	@Override
	public void performSuperAdder(String propertyIdentifier, Object value, Class<?> modelEntityInterface) {
		// TODO Auto-generated method stub

	}

	@Override
	public void performSuperRemover(String propertyIdentifier, Object value, Class<?> modelEntityInterface) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object performSuperFinder(String finderIdentifier, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object performSuperFinder(String finderIdentifier, Object value, Class<?> modelEntityInterface) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSerializing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDeserializing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean equalsObject(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performSuperDelete(Object... context) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean performSuperUndelete(boolean restoreProperties) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void performSuperDelete(Class<?> modelEntityInterface, Object... context) {
		// TODO Auto-generated method stub

	}

	private boolean isDeleted = false;

	/**
	 * Abstract implementation of delete<br>
	 * This method should be overriden.<br>
	 * At this level, only manage {@link #isDeleted()} feature
	 * 
	 * @return flag indicating if deletion has successfully been performed
	 */
	@Override
	public boolean delete(Object... context) {

		if (isDeleted()) {
			return false;
		}

		setChanged();
		notifyObservers(new ObjectDeleted(this));
		isDeleted = true;
		return true;
	}

	@Override
	public boolean undelete(boolean restoreProperties) {
		isDeleted = false;
		return true;
	}

	/**
	 * Return a flag indicating if this object was deleted
	 * 
	 * @return
	 */
	@Override
	public boolean isDeleted() {
		return isDeleted;
	}

	@Override
	public Object cloneObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object cloneObject(Object... context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCreatedByCloning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBeingCloned() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasKey(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Return object matching supplied key, if this object responses to this key
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public Object objectForKey(String key) {
		return null;
	}

	/**
	 * Sets an object matching supplied key, if this object responses to this key
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public void setObjectForKey(Object value, String key) {
	}

	/**
	 * Return type of key/value pair identified by supplied key identifier
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public Type getTypeForKey(String key) {
		return null;
	}

}
