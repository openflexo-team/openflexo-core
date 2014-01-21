package org.openflexo.foundation;

import java.lang.reflect.Type;
import java.util.List;

import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.validation.ValidationModel;

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

	@Override
	public FlexoServiceManager getServiceManager() {
		// TODO Auto-generated method stub
		return null;
	}

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
	public void performSuperSetModified(boolean modified) {
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
	public void setModified(boolean modified) {
		// TODO Auto-generated method stub

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
	public boolean performSuperUndelete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void performSuperDelete(Class<?> modelEntityInterface, Object... context) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean delete(Object... context) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean undelete() {
		// TODO Auto-generated method stub
		return false;
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
	public ValidationModel getDefaultValidationModel() {
		// TODO Auto-generated method stub
		return null;
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
