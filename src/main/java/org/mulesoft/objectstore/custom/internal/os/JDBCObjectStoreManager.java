/**
 * All code herein is provided "AS IS". Developer makes no warranties, 
 * express or implied, and hereby disclaims all implied warranties, 
 * including any warranty of merchantability and warranty of fitness for a particular purpose.
 */

package org.mulesoft.objectstore.custom.internal.os;

import org.mule.runtime.api.store.ObjectStore;
import org.mule.runtime.api.store.ObjectStoreManager;
import org.mule.runtime.api.store.ObjectStoreSettings;
import org.mulesoft.objectstore.custom.internal.settings.JDBCObjectStoreConnectionSettings;
import org.mulesoft.objectstore.custom.internal.settings.JDBCObjectStoreSQLSettings;

import java.io.Serializable;

public class JDBCObjectStoreManager implements ObjectStoreManager {
	
	private final JDBCObjectStore jdbcObjectStore;

	public JDBCObjectStoreManager(ObjectStoreManager objectStoreManager, JDBCObjectStoreSQLSettings settings, JDBCObjectStoreConnectionSettings conSettings) {
		this.jdbcObjectStore = new JDBCObjectStore(objectStoreManager, settings, conSettings);
	}

	@Override
	public <T extends ObjectStore<? extends Serializable>> T getObjectStore(String s) {
		return (T) jdbcObjectStore;
	}

	@Override
	public <T extends ObjectStore<? extends Serializable>> T createObjectStore(String s,
			ObjectStoreSettings objectStoreSettings) {
		return (T) jdbcObjectStore;
	}

	@Override
	public <T extends ObjectStore<? extends Serializable>> T getOrCreateObjectStore(String s,
			ObjectStoreSettings objectStoreSettings) {
		return (T) jdbcObjectStore;
	}

	@Override
	public void disposeStore(String s) {
		// Do nothing
	}

}
