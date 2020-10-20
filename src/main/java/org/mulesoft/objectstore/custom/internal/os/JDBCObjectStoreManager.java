/*
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright law. All use of this software is subject to
 * MuleSoft's Master Subscription Agreement (or other Terms of Service) separately entered into between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mulesoft.objectstore.custom.internal.os;

//import org.mule.extension.db.internal.domain.connection.DbConnection;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.store.ObjectStore;
import org.mule.runtime.api.store.ObjectStoreManager;
import org.mule.runtime.api.store.ObjectStoreSettings;
import org.mulesoft.objectstore.custom.internal.settings.JDBCObjectStoreConnectionSettings;
//import com.mulesoft.connectors.commons.template.connection.ConnectorConnection;
import org.mulesoft.objectstore.custom.internal.settings.JDBCObjectStoreSQLSettings;

import java.io.Serializable;

//import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class JDBCObjectStoreManager implements ObjectStoreManager {
	
	private final JDBCObjectStore jdbcObjectStore;

	//public JDBCObjectStoreManager(ObjectStoreManager objectStoreManager, JDBCObjectStoreSettings settings, ConnectionProvider<DbConnection> dbConnectionProvider) {
	public JDBCObjectStoreManager(ObjectStoreManager objectStoreManager, JDBCObjectStoreSQLSettings settings, JDBCObjectStoreConnectionSettings conSettings) {
		//this.oAuth2ClientStore = new JDBCObjectStore(objectStoreManager, settings, dbConnectionProvider);
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
