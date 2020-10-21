/**
 * All code herein is provided "AS IS". Developer makes no warranties, 
 * express or implied, and hereby disclaims all implied warranties, 
 * including any warranty of merchantability and warranty of fitness for a particular purpose.
 */

package org.mulesoft.objectstore.custom.internal.connection;

import static org.mule.runtime.api.connection.ConnectionValidationResult.success;

//import org.mule.extension.db.internal.domain.connection.DbConnection;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.store.ObjectStoreManager;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.stereotype.Stereotype;
import org.mulesoft.objectstore.custom.internal.os.JDBCObjectStoreManager;
import org.mulesoft.objectstore.custom.internal.settings.JDBCObjectStoreConnectionSettings;
import org.mulesoft.objectstore.custom.internal.settings.JDBCObjectStoreSQLSettings;

import javax.inject.Inject;

@Stereotype(JDBCStoreConnectionStereotype.class)
@Alias("jdbc-connection")
@DisplayName("JDBC Connection")
public class JDBCObjectStoreManagerProvider implements ConnectionProvider<JDBCObjectStoreManager> {

	@Inject
	private ObjectStoreManager objectStoreManager;

	private JDBCObjectStoreManager JDBCObjectStoreManager;

	@ParameterGroup(name = "Connection Settings")
	@Placement(order = 1)
	private JDBCObjectStoreConnectionSettings jdbcObjectStoreConnectionSettings;
	
	@ParameterGroup(name = "ObjectStore Database SQL")
	@Placement(order = 2)
	private JDBCObjectStoreSQLSettings jdbcObjectStoreSettings;

	@Override
	public JDBCObjectStoreManager connect() {
		if (JDBCObjectStoreManager == null) {
			//JDBCObjectStoreManager = new JDBCObjectStoreManager(objectStoreManager, jdbcObjectStoreSettings, dbConnectionProvider);
			JDBCObjectStoreManager = new JDBCObjectStoreManager(objectStoreManager, jdbcObjectStoreSettings, jdbcObjectStoreConnectionSettings);
		}
		return JDBCObjectStoreManager;
	}

	@Override
	public void disconnect(JDBCObjectStoreManager objectStoreManager) {
		// Do nothing.
	}

	@Override
	public ConnectionValidationResult validate(JDBCObjectStoreManager objectStoreManager) {
		return success();
	}
}
