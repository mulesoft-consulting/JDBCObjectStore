/**
 * All code herein is provided "AS IS". Developer makes no warranties, 
 * express or implied, and hereby disclaims all implied warranties, 
 * including any warranty of merchantability and warranty of fitness for a particular purpose.
 */

package org.mulesoft.objectstore.custom.internal.settings;


import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;


public class JDBCObjectStoreSQLSettings {
	
    @Parameter
    @DisplayName("Object Store Partition Name")
    @Placement(tab = Placement.DEFAULT_TAB, order = 1)
    @Summary("The DB Table Hosting the Key/Value Pairs Needs the Abilty for Multiple Different Object Stores.  This Partition Name is Part of the Primary Key to Separate")
    private String objectStorePartition;

    @Parameter
    @DisplayName("Database Table Name")
    @Placement(tab = Placement.DEFAULT_TAB, order = 2)
	@Summary("Table Name in the Database used for Storing Key/Value Pairs")
    private String objectStoreTableName;
    
    @Parameter
    @DisplayName("Key Field Name")
    @Placement(tab = Placement.DEFAULT_TAB, order = 3)
	@Summary("The Name of the Table Field Holding the 'Keys' of the Key/Value Pairs")
    private String tableKeyFieldName;

    @Parameter
    @DisplayName("Value Field Name")
    @Placement(tab = Placement.DEFAULT_TAB, order = 4)
	@Summary("The Name of the Table Field Holding the 'Values' of the Key/Value Pairs")
    private String tableValueFieldName;   

    @Parameter
    @DisplayName("Partition Field Name")
    @Placement(tab = Placement.DEFAULT_TAB, order = 5)
	@Summary("The Name of the Table Field Holding the Partition Names of the Key/Value Pairs")
    private String tablePartitionFieldName;       
    
	public String getObjectStoreTableName() {
		return objectStoreTableName;
	}

	public String getTableKeyFieldName() {
		return tableKeyFieldName;
	}

	public String getTableValueFieldName() {
		return tableValueFieldName;
	}

	public String getTablePartitionFieldName() {
		return tablePartitionFieldName;
	}

	public String getObjectStorePartition() {
		return objectStorePartition;
	}

}
