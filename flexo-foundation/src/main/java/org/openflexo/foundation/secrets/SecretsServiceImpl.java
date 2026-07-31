package org.openflexo.foundation.secrets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.toolbox.FileUtils;

/**
 * Default implementation of {@link SecretsService}.
 * <p>
 * The local store is a dedicated {@code secrets.properties} file in the application data directory, separate from
 * {@code Flexo.prefs}: this service must be usable from a headless launch (e.g. {@code fml-cli}), which does not have the
 * {@code openflexo-ui} preferences machinery on its classpath.
 */
public class SecretsServiceImpl extends FlexoServiceImpl implements SecretsService {

	private static final Logger logger = Logger.getLogger(SecretsServiceImpl.class.getPackage().getName());

	private static final String SECRETS_FILE_NAME = "secrets.properties";
	private static final String SYSTEM_PROPERTY_PREFIX = "openflexo.secret.";
	private static final String ENVIRONMENT_VARIABLE_PREFIX = "OPENFLEXO_SECRET_";

	private final Properties localSecrets = new Properties();

	@Override
	public void initialize() {
		load();
	}

	@Override
	public String getServiceName() {
		return "SecretsService";
	}

	@Override
	public Optional<String> getSecret(String key) {
		String systemProperty = System.getProperty(SYSTEM_PROPERTY_PREFIX + key);
		if (systemProperty != null) {
			return Optional.of(systemProperty);
		}
		String environmentVariable = System.getenv(ENVIRONMENT_VARIABLE_PREFIX + toEnvironmentVariableName(key));
		if (environmentVariable != null) {
			return Optional.of(environmentVariable);
		}
		return Optional.ofNullable(localSecrets.getProperty(key));
	}

	@Override
	public void setSecret(String key, String value) {
		localSecrets.setProperty(key, value);
		save();
	}

	@Override
	public void removeSecret(String key) {
		localSecrets.remove(key);
		save();
	}

	@Override
	public List<String> getSecretKeys() {
		List<String> keys = new ArrayList<>();
		for (Object key : localSecrets.keySet()) {
			keys.add((String) key);
		}
		return keys;
	}

	private static String toEnvironmentVariableName(String key) {
		return key.toUpperCase().replaceAll("[^A-Z0-9]", "_");
	}

	private File getSecretsFile() {
		return new File(FileUtils.getApplicationDataDirectory(), SECRETS_FILE_NAME);
	}

	private void load() {
		File secretsFile = getSecretsFile();
		if (!secretsFile.exists()) {
			return;
		}
		try (FileInputStream fis = new FileInputStream(secretsFile)) {
			localSecrets.load(fis);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not read secrets file " + secretsFile.getAbsolutePath(), e);
		}
	}

	private void save() {
		File secretsFile = getSecretsFile();
		File parentDir = secretsFile.getParentFile();
		if (!parentDir.exists() && !parentDir.mkdirs()) {
			logger.warning("Could not create application data directory " + parentDir.getAbsolutePath());
			return;
		}
		try (FileOutputStream fos = new FileOutputStream(secretsFile)) {
			localSecrets.store(fos, "OpenFlexo local secrets - do not commit this file");
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not write secrets file " + secretsFile.getAbsolutePath(), e);
			return;
		}
		restrictPermissions(secretsFile);
	}

	private void restrictPermissions(File secretsFile) {
		try {
			Files.setPosixFilePermissions(secretsFile.toPath(), PosixFilePermissions.fromString("rw-------"));
		} catch (UnsupportedOperationException | IOException e) {
			// Not a POSIX filesystem (e.g. Windows): rely on the platform's default ACLs for the user's application data directory.
		}
	}

}
