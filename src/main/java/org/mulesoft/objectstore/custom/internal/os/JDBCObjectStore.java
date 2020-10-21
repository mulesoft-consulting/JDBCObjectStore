/**
 * All code herein is provided "AS IS". Developer makes no warranties, 
 * express or implied, and hereby disclaims all implied warranties, 
 * including any warranty of merchantability and warranty of fitness for a particular purpose.
 */

package org.mulesoft.objectstore.custom.internal.os;


import org.mule.runtime.api.i18n.I18nMessageFactory;
import org.mule.runtime.api.store.ObjectDoesNotExistException;
import org.mule.runtime.api.store.ObjectStore;
import org.mule.runtime.api.store.ObjectStoreException;
import org.mule.runtime.api.store.ObjectStoreManager;
import org.mule.runtime.api.store.ObjectStoreNotAvailableException;
import org.mule.runtime.extension.api.annotation.param.stereotype.Stereotype;
import org.mulesoft.objectstore.custom.internal.settings.JDBCObjectStoreConnectionSettings;
import org.mulesoft.objectstore.custom.internal.settings.JDBCObjectStoreSQLSettings;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.serial.SerialBlob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stereotype(JDBCStereotype.class)
public class JDBCObjectStore implements ObjectStore<Serializable> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(JDBCObjectStore.class);

	private JDBCObjectStoreSQLSettings settings;
	private QueryRunner queryRunner;
	private String databaseName;

	public JDBCObjectStore(ObjectStoreManager osManager, JDBCObjectStoreSQLSettings settings,
			JDBCObjectStoreConnectionSettings conSettings) {
		LOGGER.debug("In the Constructor for the JDBC ObjectStore.");
		this.settings = settings;
		queryRunner = new QueryRunner(conSettings.getDataSource());
		try {
			this.databaseName = conSettings.getDataSource().getConnection().getMetaData().getDatabaseProductName();
			LOGGER.info("Database Name: " + this.databaseName);
		} catch (SQLException e) {
			LOGGER.debug(e.getMessage());
		}
	}

	/**
	 * Returns whether the key exists by querying the JDBC DataSource
	 * <p>
	 * NOTE: If the system is running offline, this method will throw an
	 * ObjectStoreNotAvailableException which will in turn be a OS:STORE_NOT_AVAILABLE in Mule.
	 *
	 * @param String key.
	 * @return true/false.
	 */
	@Override
	public boolean contains(String key) throws ObjectStoreNotAvailableException {
		LOGGER.debug("Checking if Database Contains key: " + key);
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT COUNT(*) FROM ")
			.append(this.settings.getObjectStoreTableName())
			.append(" WHERE ")
			.append(this.settings.getTableKeyFieldName())
			.append(" = ? AND ")
			.append(this.settings.getTablePartitionFieldName())
			.append(" = ?");

		try {
			Object[] row = (Object[]) ((Object[]) this.queryRunner.query(sb.toString(),
					new ArrayHandler(), new Object[] { key, this.settings.getObjectStorePartition() }));

			if (row == null) {
				return false;
			} else {
				try {
					Integer count = Integer.parseInt(row[0].toString());
					return (count.intValue() > 0) ? true : false;
				} catch (java.lang.ArrayIndexOutOfBoundsException ae) {
					return false;
				}
			}

		} catch (SQLException e) {
			LOGGER.debug("Error while checking if key exists in JDBCObjectStore" + e.getMessage());
			throw new ObjectStoreNotAvailableException(I18nMessageFactory
			        .createStaticMessage("Could Not Check if key is available: " + e.getMessage()));
		}
	}

	/**
	 * Always returns true. By nature of this being a JDBC Object store it is
	 * persistent
	 *
	 * @return true.
	 */
	@Override
	public boolean isPersistent() {
		return true;
	}

	/**
	 * Removes all of the Key/Value pairs in the JDBC DataSource
	 * <p>
	 * NOTE: If the system is running offline, this method will throw an
	 * ObjectStoreNotAvailableException which will in turn be a OS:STORE_NOT_AVAILABLE in Mule.
	 *
	 */
	@Override
	public void clear() throws ObjectStoreNotAvailableException {
		LOGGER.debug("Removing all keys from the JDBC Object Store");
		StringBuffer sb = new StringBuffer();
		sb.append("DELETE FROM ")
			.append(this.settings.getObjectStoreTableName())
			.append(" WHERE ")
			.append(this.settings.getTablePartitionFieldName())
			.append(" = ?");
		
		Object[] arguments = new Object[1];
		arguments[0] = this.settings.getObjectStorePartition();

		try {
			int reply = this.queryRunner.execute(sb.toString(), arguments);
			
			LOGGER.debug("Reply count: " + reply);
		} catch (SQLException e) {
			LOGGER.debug("Error while removing all keys from JDBCObjectStore" + e.getMessage());
			throw new ObjectStoreNotAvailableException(I18nMessageFactory
			        .createStaticMessage("Could Not Remove all Values: " + e.getMessage()));
		}
	}

	@Override
	public void open() {
		// Do nothing
	}

	@Override
	public void close() {
		// Do nothing
	}

	/**
	 * Returns all of the Object Store keys by querying the JDBC DataSource
	 * <p>
	 * NOTE: If the system is running offline, this method will throw an
	 * ObjectStoreNotAvailableException which will in turn be a OS:STORE_NOT_AVAILABLE in Mule.
	 *
	 * @param String key.
	 * @return List of Strings / null.
	 */
	@Override
	public List<String> allKeys() throws ObjectStoreException, ObjectStoreNotAvailableException {
		LOGGER.debug("Retrieving data from ObjectStore Database.");
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ")
			.append(this.settings.getTableKeyFieldName())
			.append(" FROM ")
			.append(this.settings.getObjectStoreTableName())
			.append(" WHERE ")
			.append(this.settings.getTablePartitionFieldName())
			.append(" = ?");

		try {
			List<Map<String, Object>> rows = this.queryRunner.query(sb.toString(),
					new MapListHandler(new BlobAwareProcessor(this.settings.getTableValueFieldName())), new Object[] { this.settings.getObjectStorePartition() });

			if (rows == null) {
				throw new Exception();
			} else {
				try {
					LOGGER.debug("Key Count: " + rows.size());
					ArrayList<String> data = new ArrayList<String>();
					//List<String> data = new List<String>();
					Iterator<Map<String, Object>> it = rows.iterator();
					while (it.hasNext()) {
						data.add((String)it.next().get(this.settings.getTableKeyFieldName().toUpperCase()));
					}
					return (List<String>)data; 
				} catch (java.lang.IndexOutOfBoundsException ae) {
					throw new Exception();
				}
			}

		} catch (SQLException se) {
			LOGGER.debug("Error while retrieving all keys from JDBCObjectStore" + se.getMessage());
			throw new ObjectStoreNotAvailableException(I18nMessageFactory
			        .createStaticMessage("Could Not Retrieve Values: " + se.getMessage()));				
		} catch (Exception e) {
			LOGGER.debug("Error while retrieving all keys from JDBCObjectStore" + e.getMessage());
			throw new ObjectStoreException(I18nMessageFactory
			        .createStaticMessage("Could Not Retrieve Values: " + e.getMessage()));
		}
	}

	/**
	 * Returns the value of the key from the Object Store by querying the JDBC
	 * DataSource
	 * <p>
	 * If the system is running offline, this method will throw an
	 * ObjectStoreNotAvailableException which will in turn be a OS:STORE_NOT_AVAILABLE in Mule.
	 * <p>
	 * If the key is not present in the database, this method will throw an
	 * ObjectDoesNotExistException which will in turn be a OS:KEY_NOT_FOUND in Mule.
	 *
	 * @param String key.
	 * @return Serializable value.
	 */
	@Override
	public Serializable retrieve(String key) throws ObjectDoesNotExistException, ObjectStoreNotAvailableException {
		LOGGER.debug("Retrieving data from ObjectStore Database.");
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ")
			.append(this.settings.getTableValueFieldName())
			.append(" FROM ")
			.append(this.settings.getObjectStoreTableName())
			.append(" WHERE ")
			.append(this.settings.getTableKeyFieldName())
			.append(" = ? AND ")
			.append(this.settings.getTablePartitionFieldName())
			.append(" = ?");

		try {
			List<Map<String, Object>> row = this.queryRunner.query(sb.toString(),
					new MapListHandler(new BlobAwareProcessor(this.settings.getTableValueFieldName())), new Object[] { key, this.settings.getObjectStorePartition() });

			if (row == null) {
				throw new Exception();
			} else {
				try {
					return (Serializable)this.fromByteArray((BlobData)row.get(0).get(this.settings.getTableValueFieldName().toUpperCase()));
				} catch (java.lang.IndexOutOfBoundsException ae) {
					throw new Exception();
				}
			}

		} catch (SQLException se) {
			//se.printStackTrace();
			LOGGER.debug("Error while retrieving value from JDBCObjectStore" + se.getMessage());
			throw new ObjectStoreNotAvailableException(I18nMessageFactory
			        .createStaticMessage("Could Not Get Key: " + se.getMessage()));			
		} catch (Exception e) {
			//e.printStackTrace();
			LOGGER.debug("Error while retrieving value from JDBCObjectStore" + e.getMessage());
			throw new ObjectDoesNotExistException(I18nMessageFactory
			        .createStaticMessage(key + " was not found in Database Object Store."));
		}
	}

	/**
	 * Stores a new key/value pair in the JDBC DataSource
	 * <p>
	 * NOTE: If the system is running offline, this method will throw an
	 * ObjectStoreNotAvailableException which will in turn be a OS:STORE_NOT_AVAILABLE in Mule.
	 * 
	 * @param String       key.
	 * @param Serializable value
	 * @return void.
	 */
	@Override
	public void store(String key, Serializable value) throws ObjectStoreException, ObjectStoreNotAvailableException {

		try {
			switch (this.databaseName) {
				case "Apache Derby": {
					LOGGER.debug("About to call store generic.");
					this.storeGeneric(key, value);
					break;
				}
				case "MySQL": {
					LOGGER.debug("About to call store msql.");
					this.storeMySQL(key, value);
					break;
				}
				case "Microsoft SQL Server": {
					LOGGER.debug("About to call store generic.");
					this.storeSqlServer(key, value);
					break;
				}
				case "Oracle": {
					LOGGER.debug("About to call store oracle.");
					this.storeOracle(key, value);
					break;
				}
				default: {
					LOGGER.debug("About to call store generic.");
					this.storeGeneric(key, value);					
				}
			}
			return;

		} catch (SQLException var4) {
			LOGGER.debug("Error while storing key/value pair in JDBCObjectStore" + var4.getMessage());
			throw new ObjectStoreNotAvailableException(I18nMessageFactory
			        .createStaticMessage("Could Not Store Value: " + var4.getMessage()));	
		} catch (IOException var5) {
			throw new ObjectStoreException(var5);
		}
	}

	private void storeSqlServer(String key, Serializable value) throws SQLException, IOException, ObjectStoreNotAvailableException {

		LOGGER.debug("In Store Sql Server.  databasename: " + this.databaseName);
		
		StringBuffer sb = new StringBuffer();
		sb.append("BEGIN TRY INSERT INTO ")
			.append(this.settings.getObjectStoreTableName())
			.append(" (")
			.append(this.settings.getTableValueFieldName())
			.append(", ")
			.append(this.settings.getTableKeyFieldName())
			.append(", ")
			.append(this.settings.getTablePartitionFieldName())
			.append(") values (?, ?, ?); END TRY BEGIN CATCH IF ERROR_NUMBER() IN (2601, 2627) UPDATE ")
			.append(this.settings.getObjectStoreTableName())
			.append(" SET ")
			.append(this.settings.getTableValueFieldName())
			.append(" = ? WHERE ")
			.append(this.settings.getTableKeyFieldName())
			.append(" = ? AND ")
			.append(this.settings.getTablePartitionFieldName())
			.append(" = ?; END CATCH");
	
		LOGGER.debug("Upsert Statement for " + this.databaseName + ": " + sb.toString());

		Object[] arguments = new Object[6];
		byte[] data = this.toByteArray(value);
		arguments[0] = data;
		arguments[1] = key;
		arguments[2] = this.settings.getObjectStorePartition();
		arguments[3] = data;
		arguments[4] = key;
		arguments[5] = this.settings.getObjectStorePartition();
		
		int reply = Integer.valueOf(this.queryRunner.execute(sb.toString(), arguments));

		LOGGER.debug("Reply from Upsert for Apache Derby Database: " + reply);
		return;
		
	}

	private void storeOracle(String key, Serializable value) throws SQLException, IOException, ObjectStoreNotAvailableException {

		Connection con = null;
		try {
			LOGGER.debug("In Store Oracle.  databasename: " + this.databaseName);
			
			StringBuffer sb = new StringBuffer();
			sb.append("begin insert into ")
				.append(this.settings.getObjectStoreTableName())
				.append(" (")
				.append(this.settings.getTableValueFieldName())
				.append(", ")
				.append(this.settings.getTableKeyFieldName())
				.append(", ")
				.append(this.settings.getTablePartitionFieldName())
				.append(") values (?, ?, ?); exception when dup_val_on_index then update ")
				.append(this.settings.getObjectStoreTableName())
				.append(" set ")
				.append(this.settings.getTableValueFieldName())
				.append(" = ? where ")
				.append(this.settings.getTableKeyFieldName())
				.append(" = ? and ")
				.append(this.settings.getTablePartitionFieldName())
				.append(" = ?; end;");
	
			
			LOGGER.debug("Upsert Statement for " + this.databaseName + ": " + sb.toString());
	
			con = this.queryRunner.getDataSource().getConnection();
			PreparedStatement pstmt = con.prepareStatement(sb.toString());
			
			pstmt.setBinaryStream(1, new ByteArrayInputStream(this.toByteArray(value)));
			pstmt.setString(2,  key);
			pstmt.setString(3,  this.settings.getObjectStorePartition());
			pstmt.setBinaryStream(4, new ByteArrayInputStream(this.toByteArray(value)));
			pstmt.setString(5,  key);
			pstmt.setString(6,  this.settings.getObjectStorePartition());
			boolean reply = pstmt.execute();
	
			//int reply = Integer.valueOf(this.queryRunner.execute(sb.toString(), arguments));
	
			LOGGER.debug("Reply from Upsert for Apache Derby Database: " + reply);
			return;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		} finally {
			if (con != null) {
				con.close();
			}
		}
		
	}
	
	private void storeMySQL(String key, Serializable value) throws SQLException, IOException, ObjectStoreNotAvailableException {

		LOGGER.debug("In Store MySQL.  databasename: " + this.databaseName);
		
		StringBuffer sb = new StringBuffer();
		sb.append("insert into ")
			.append(this.settings.getObjectStoreTableName())
			.append(" (")
			.append(this.settings.getTableValueFieldName())
			.append(", ")
			.append(this.settings.getTableKeyFieldName())
			.append(", ")
			.append(this.settings.getTablePartitionFieldName())
			.append(") values (?, ?, ?) ON DUPLICATE KEY UPDATE ")
			.append(this.settings.getTableValueFieldName())
			.append(" = ?");

		
		LOGGER.debug("Upsert Statement for " + this.databaseName + ": " + sb.toString());

		Object[] arguments = new Object[4];
		byte[] data = this.toByteArray(value);
		arguments[0] = data;
		arguments[1] = key;
		arguments[2] = this.settings.getObjectStorePartition();
		arguments[3] = data;

		int reply = Integer.valueOf(this.queryRunner.execute(sb.toString(), arguments));

		LOGGER.debug("Reply from Upsert for Apache Derby Database: " + reply);
		return;
		
	}
	
	
	private void storeGeneric(String key, Serializable value) throws SQLException, IOException, ObjectStoreNotAvailableException {
		
		LOGGER.debug("In Store Generic.  databasename: " + this.databaseName);
		StringBuffer sb = new StringBuffer();
		if (this.contains(key)) {
			sb.append("update ")
				.append(this.settings.getObjectStoreTableName())
				.append(" set ")
				.append(this.settings.getTableValueFieldName())
				.append(" = ? where ")
				.append(this.settings.getTableKeyFieldName())
				.append(" = ? and ")
				.append(this.settings.getTablePartitionFieldName())
				.append(" = ?");
		} else {
			sb.append("insert into ")
				.append(this.settings.getObjectStoreTableName())
				.append(" (")
			.append(this.settings.getTableValueFieldName())
			.append(", ")
			.append(this.settings.getTableKeyFieldName())
			.append(", ")
			.append(this.settings.getTablePartitionFieldName())
			.append(") values (?, ?, ?)");
		}

		LOGGER.debug("Upsert Statement for " + this.databaseName + ": " + sb.toString());

		Object[] arguments = new Object[3];
		arguments[0] = new SerialBlob(this.toByteArray(value));
		arguments[1] = key;
		arguments[2] = this.settings.getObjectStorePartition();

		int reply = Integer.valueOf(this.queryRunner.execute(sb.toString(), arguments));

		LOGGER.debug("Reply from Upsert for Apache Derby Database: " + reply);
		return;
		
	}

	/**
	 * Returns all of the Object Store values by querying the JDBC DataSource
	 * <p>
	 * NOTE: If the system is running offline, this method will throw an
	 * ObjectStoreNotAvailableException which will in turn be a OS:STORE_NOT_AVAILABLE in Mule.
	 *
	 * @return Map<String, Serializable> / null.
	 */
	@Override
	public Map<String, Serializable> retrieveAll() throws ObjectStoreException, ObjectStoreNotAvailableException {
		LOGGER.debug("Retrieving all data from ObjectStore Database.");
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ")
			.append(this.settings.getTableKeyFieldName())
			.append(", ")
			.append(this.settings.getTableValueFieldName())
			.append(" FROM ")
			.append(this.settings.getObjectStoreTableName())
			.append(" WHERE ")
			.append(this.settings.getTablePartitionFieldName())
			.append(" = ?");

		try {
			List<Map<String, Object>> rows = this.queryRunner.query(sb.toString(),
					new MapListHandler(new BlobAwareProcessor(this.settings.getTableValueFieldName())), new Object[] { this.settings.getObjectStorePartition() });

			if (rows == null) {
				throw new Exception();
			} else {
				try {
					LOGGER.debug("Key Count: " + rows.size());
					Map<String, Serializable> data = new HashMap<String, Serializable>();
					Map<String, Object> currRow = null;
					//List<String> data = new List<String>();
					Iterator<Map<String, Object>> it = rows.iterator();
					while (it.hasNext()) {
						currRow = it.next();
						data.put((String)currRow.get(this.settings.getTableKeyFieldName().toUpperCase())
								, (Serializable)this.fromByteArray((BlobData)currRow.get(this.settings.getTableValueFieldName().toUpperCase())));
					}
					return data;
				} catch (java.lang.IndexOutOfBoundsException ae) {
					throw new Exception();
				}
			}

		} catch (SQLException se) {
			LOGGER.debug("Error while retrieving all key/value pairs from JDBCObjectStore" + se.getMessage());
			throw new ObjectStoreNotAvailableException(I18nMessageFactory
			        .createStaticMessage("Could Not Retrieve Values: " + se.getMessage()));				
		} catch (Exception e) {
			LOGGER.debug("Error while retrieving all key/value pairs from JDBCObjectStore" + e.getMessage());
			throw new ObjectStoreException(I18nMessageFactory
			        .createStaticMessage("Could Not Retrieve Values: " + e.getMessage()));
		}
	}

	/**
	 * Removes an Object Store entry from the JDBC DataSource
	 * <p>
	 * If the system is running offline, this method will throw an
	 * ObjectStoreNotAvailableException which will in turn be a OS:STORE_NOT_AVAILABLE in Mule.
	 * <p>
	 * If the key is not present in the database, this method will throw an
	 * ObjectDoesNotExistException which will in turn be a OS:KEY_NOT_FOUND in Mule.
	 *
	 * @param String key.
	 * @return String<Serializable> the key passed in.
	 */
	@Override
	public Serializable remove(String key) throws ObjectStoreException, ObjectDoesNotExistException, ObjectStoreNotAvailableException {
		try {

			LOGGER.debug("About to remove key: " + key);
			StringBuffer sb = new StringBuffer();
			sb.append("DELETE FROM ")
				.append(this.settings.getObjectStoreTableName())
				.append(" WHERE ")
				.append(this.settings.getTableKeyFieldName())
				.append(" = ? AND ")
				.append(this.settings.getTablePartitionFieldName())
				.append(" = ?");

			Object[] arguments = new Object[2];
			arguments[0] = key;
			arguments[1] = this.settings.getObjectStorePartition();

			int reply = this.queryRunner.execute(sb.toString(), arguments);
			
			LOGGER.debug("Reply count: " + reply);
			
			if (reply < 1) {
				LOGGER.debug("Key " + key + " not found, therefore nothing to remove. Throwing ObjectDoesNotExistException");
				throw new ObjectDoesNotExistException(I18nMessageFactory
				        .createStaticMessage("Could Not Remove Key: " + key + ". Not found in JDBC Object Store"));
			}
			
			return (Serializable)key;
			
		} catch (SQLException se) {
			LOGGER.debug("Error while removing key from JDBCObjectStore" + se.getMessage());
			throw new ObjectStoreNotAvailableException(I18nMessageFactory
			        .createStaticMessage("Could Not Remove Key: " + se.getMessage()));			
		}
	}

	private byte[] toByteArray(Serializable object) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(object);
			out.flush();
			return bos.toByteArray();
		} finally {
			try {
				bos.close();
			} catch (IOException ex) {
				// ignore close exception
			}
		}
	}

	private Object fromByteArray(BlobData data) throws IOException, ClassNotFoundException{
		
		ByteArrayInputStream bis = new ByteArrayInputStream(data.getBytes());
		ObjectInput in = null;

		try {

			//LOGGER.info(data.getBytes(1, (int)data.length()).toString());
			
			in = new ObjectInputStream(bis);
			return in.readObject();

		} finally {

			try {

				if (in != null) {
					in.close();
				}

			} catch (IOException ex) {
				// ignore close exception
			}

		}
	}

}
