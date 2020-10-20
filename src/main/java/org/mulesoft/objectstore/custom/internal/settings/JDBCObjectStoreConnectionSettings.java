package org.mulesoft.objectstore.custom.internal.settings;

import javax.sql.DataSource;

import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class JDBCObjectStoreConnectionSettings {
	

	@Parameter
	@DisplayName("Spring DataSource Name")
	@Placement(tab = Placement.DEFAULT_TAB, order = 1)
	@Summary("The Name of the Spring DataSource to Obtain the Database Connection")
	private DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}

}
