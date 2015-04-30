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

package org.openflexo.ssl;

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

public final class DenaliSecurityProvider extends Provider {

	private final static String PROVIDER_ID = "DenaliSecurityProvider";

	public DenaliSecurityProvider() {
		super(PROVIDER_ID, 1.0, "Denali security provider");

		AccessController.doPrivileged(new SecurityPrivilegedAction());
	}

	public static void insertSecurityProvider() throws Exception {
		if (Security.getProvider(PROVIDER_ID) == null) {
			Security.addProvider(new DenaliSecurityProvider());
			Security.setProperty("ssl.TrustManagerFactory.algorithm", "DenaliX509");
		}
	}

	protected final class SecurityPrivilegedAction implements PrivilegedAction<Object> {
		@Override
		public Object run() {
			put("TrustManagerFactory.DenaliX509", TrustManagerFactoryImpl.class.getName());
			return null;
		}
	}

	public final static class TrustManagerFactoryImpl extends TrustManagerFactorySpi {
		protected final class DenaliX509TrustManager implements X509TrustManager {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) {

			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) {
				// TODO: check in some way the certificates here.
			}
		}

		@Override
		protected void engineInit(KeyStore keystore) throws KeyStoreException {

		}

		@Override
		protected void engineInit(ManagerFactoryParameters mgrparams) throws InvalidAlgorithmParameterException {

		}

		@Override
		protected TrustManager[] engineGetTrustManagers() {
			return new TrustManager[] { new DenaliX509TrustManager() };
		}
	}
}
