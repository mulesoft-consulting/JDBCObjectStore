/**
 * All code herein is provided "AS IS". Developer makes no warranties, 
 * express or implied, and hereby disclaims all implied warranties, 
 * including any warranty of merchantability and warranty of fitness for a particular purpose.
 */

package org.mulesoft.objectstore.custom.internal;

import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mulesoft.objectstore.custom.internal.connection.JDBCObjectStoreManagerProvider;

@Xml(prefix = "jdbc-object-store")
@Extension(name = "jdbc-object-store")
@ConnectionProviders(JDBCObjectStoreManagerProvider.class)
public class JDBCObjectStoreConnector {

}
