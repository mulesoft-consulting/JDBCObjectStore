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
