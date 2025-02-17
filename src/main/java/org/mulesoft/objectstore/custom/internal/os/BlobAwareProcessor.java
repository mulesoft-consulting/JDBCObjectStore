/**
 * All code herein is provided "AS IS". Developer makes no warranties, 
 * express or implied, and hereby disclaims all implied warranties, 
 * including any warranty of merchantability and warranty of fitness for a particular purpose.
 */

package org.mulesoft.objectstore.custom.internal.os;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlobAwareProcessor extends BasicRowProcessor {
	
	protected static final Logger LOGGER = LoggerFactory.getLogger(BlobAwareProcessor.class);
	
	private String valueFieldName;
	
    @Override
    public Map<String, Object> toMap(ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();

        Map<String, Object> map = new HashMap<>();

        for (int index = 1; index <= columnCount; ++index) {
            String columnName = resultSetMetaData.getColumnName(index).toUpperCase();
            LOGGER.debug("Column Type: " + resultSetMetaData.getColumnTypeName(index) + " Field Name: " + columnName);
            Object object = null;
            if (columnName.equals(this.valueFieldName)) {
                Blob blob = resultSet.getBlob(index);
                // materialize BLOB as byte[]
                BlobData data = new BlobData();
                data.setBytes(blob.getBytes(1, (int) blob.length()));
                object = data;
            } else {
            	object = resultSet.getObject(index);
            }

            map.put(columnName, object);
        }

        return map;
    }

	public BlobAwareProcessor(String valueFieldName) {
		this.valueFieldName = valueFieldName.toUpperCase();
	}

}
