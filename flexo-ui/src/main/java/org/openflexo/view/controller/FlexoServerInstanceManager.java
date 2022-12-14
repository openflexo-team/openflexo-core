/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.view.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jdom2.JDOMException;
import org.openflexo.ApplicationContext;
import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.toolbox.FileUtils;

public class FlexoServerInstanceManager extends FlexoServiceImpl {

	private FlexoServerAddressBook addressBook;

	private PamelaModelFactory factory;

	public FlexoServerInstanceManager() {
		try {
			factory = new PamelaModelFactory(FlexoServerAddressBook.class);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ApplicationContext getServiceManager() {
		return (ApplicationContext) super.getServiceManager();
	}

	public List<FlexoServerInstance> getInstances() {
		return getAddressBook().getInstances();
	}

	public FlexoServerInstance getOtherInstance() {
		FlexoServerInstance other = factory.newInstance(FlexoServerInstance.class);
		other.setID(FlexoServerInstance.OTHER_ID);
		other.setName("Other");
		other.setURL(FlexoLocalization.getMainLocalizer().localizedForKey("manual_entry"));
		other.setWSURL("");
		return other;
	}

	public File getFlexoServerInstanceFile() {
		return new File(FileUtils.getApplicationDataDirectory(), "flexoserverinstances.xml");
	}

	public static FlexoServerAddressBook getDefaultAddressBook() {
		PamelaModelFactory factory;
		try {
			factory = new PamelaModelFactory(FlexoServerAddressBook.class);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			throw new Error("FlexoServerAddressBook model is not properly configured", e);
		}
		FlexoServerAddressBook addressBook = factory.newInstance(FlexoServerAddressBook.class);
		FlexoServerInstance prod = factory.newInstance(FlexoServerInstance.class);
		prod.setID("prod");
		prod.setURL("https://server.openflexo.com/");
		prod.setRestURL("https://server.openflexo.com/Flexo/rest");
		prod.setWSURL("https://server.openflexo.com/Flexo/WebObjects/FlexoServer.woa/ws/PPMWebService");
		prod.setName("Production server");
		FlexoServerInstance trial = factory.newInstance(FlexoServerInstance.class);
		trial.setID("trial");
		trial.setURL("https://trialserver.openflexo.com/");
		trial.setRestURL("https://trialserver.openflexo.com/Flexo/rest");
		trial.setWSURL("https://trialserver.openflexo.com/Flexo/WebObjects/FlexoServer.woa/ws/PPMWebService");
		trial.setName("Free trial server");
		FlexoServerInstance test = factory.newInstance(FlexoServerInstance.class);
		test.setID("test");
		test.setRestURL("https://test.openflexo.com/");
		test.setURL("https://test.openflexo.com/Flexo/rest");
		test.setWSURL("https://test.openflexo.com/Flexo/WebObjects/FlexoServer.woa/ws/PPMWebService");
		test.setName("Test server");
		// test.addToUserTypes(UserType.DEVELOPER.getIdentifier());
		// test.addToUserTypes(UserType.MAINTAINER.getIdentifier());
		addressBook.addToInstances(prod);
		addressBook.addToInstances(trial);
		addressBook.addToInstances(test);
		return addressBook;
	}

	public FlexoServerAddressBook getAddressBook() {
		if (addressBook == null) {
			synchronized (this) {
				if (addressBook == null) {
					URL url = null;
					try {
						url = new URL(getServiceManager().getAdvancedPrefs().getFlexoServerInstanceURL());
					} catch (MalformedURLException e1) {
						e1.printStackTrace();
					}
					File serverInstanceFile = getFlexoServerInstanceFile();
					String fileContent = FileUtils.createOrUpdateFileFromURL(url, serverInstanceFile);
					if (fileContent != null) {
						try {
							addressBook = (FlexoServerAddressBook) factory.deserialize(fileContent);
						} catch (IOException e) {
							e.printStackTrace();
						} catch (JDOMException e) {
							e.printStackTrace();
						} catch (InvalidDataException e) {
							e.printStackTrace();
						} catch (ModelDefinitionException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
							logger.warning("Unhandled Exception");
						}

					}
					if (addressBook != null) {
						FlexoServerInstance other = getOtherInstance();
						addressBook.addToInstances(other);
						filterAddressBook(addressBook);
					}
					else {
						FlexoServerAddressBook defaultAddressBook = getDefaultAddressBook();
						FlexoServerInstance other = getOtherInstance();
						defaultAddressBook.addToInstances(other);
						filterAddressBook(defaultAddressBook);
						return defaultAddressBook;
					}
				}
			}
		}
		return addressBook;
	}

	private void filterAddressBook(FlexoServerAddressBook book) {
		// Unused for (FlexoServerInstance instance : new ArrayList<>(book.getInstances())) {
		/*if (instance.getUserTypes().size() > 0) {
			boolean keepIt = false;
			for (String userType : instance.getUserTypes()) {
				UserType u = UserType.getUserTypeNamed(userType);
				if (UserType.getCurrentUserType().equals(u)) {
					keepIt = true;
					break;
				}
			}
			if (!keepIt) {
				book.removeFromInstances(instance);
			} else if (StringUtils.isEmpty(instance.getRestURL())) {
				if (instance.getURL() != null) {
					instance.setRestURL(instance.getURL() + "Flexo/rest");
				}
			}
		}*/
		// Unused }
	}

	@Override
	public String getServiceName() {
		return "FlexoServerInstanceManager";
	}

	@Override
	public void initialize() {
		logger.info("Initialized FlexoServerInstanceManager service");
		status = Status.Started;
	}

}
