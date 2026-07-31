package org.openflexo.foundation.secrets;

import java.util.List;
import java.util.Optional;

import org.openflexo.foundation.FlexoService;

/**
 * Manages application-local secrets (API tokens, passwords, etc.) that must never be stored in a versioned project resource.
 * <p>
 * Secrets are addressed by a flat, application-wide name (e.g. "github-token"), not by a hierarchical path. A consumer (e.g. a
 * technology adapter connection) stores that name as a reference and resolves the actual value at runtime through
 * {@link #getSecret(String)}.
 * <p>
 * Resolution order for a given key (first match wins):
 * <ol>
 * <li>System property {@code openflexo.secret.<key>}</li>
 * <li>Environment variable {@code OPENFLEXO_SECRET_<KEY>} (key upper-cased, non alphanumeric characters replaced with {@code _})</li>
 * <li>Local secrets store, edited from the desktop application's Preferences</li>
 * </ol>
 * This lets a CI/headless launch (e.g. from Jenkins, where credentials are typically injected as environment variables) override or
 * supply a secret without ever touching the local store file.
 */
public interface SecretsService extends FlexoService {

	/**
	 * Resolve the value of the secret identified by <code>key</code>, honouring the system property / environment variable / local
	 * store resolution order.
	 */
	Optional<String> getSecret(String key);

	/**
	 * Store <code>value</code> under <code>key</code> in the local secrets store and persist it.
	 */
	void setSecret(String key, String value);

	/**
	 * Remove the secret identified by <code>key</code> from the local secrets store and persist the removal.
	 */
	void removeSecret(String key);

	/**
	 * Return the names of the secrets held in the local secrets store (values are never returned by this method).
	 */
	List<String> getSecretKeys();

}
