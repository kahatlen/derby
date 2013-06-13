/*
 
   Derby - Class org.apache.derby.jdbc.Driver40
 
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 
 */

package org.apache.derby.jdbc;

import java.sql.DatabaseMetaData;
import org.apache.derby.iapi.jdbc.BrokeredConnection;
import org.apache.derby.iapi.jdbc.BrokeredConnectionControl;
import org.apache.derby.iapi.jdbc.BrokeredConnection40;
import org.apache.derby.iapi.sql.ResultSet;
import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.impl.jdbc.EmbedCallableStatement;
import org.apache.derby.impl.jdbc.EmbedConnection;
import org.apache.derby.impl.jdbc.EmbedConnection40;
import org.apache.derby.impl.jdbc.EmbedPreparedStatement;
import org.apache.derby.impl.jdbc.EmbedResultSet;
import org.apache.derby.impl.jdbc.EmbedResultSet40;
import org.apache.derby.impl.jdbc.EmbedStatement;
import org.apache.derby.impl.jdbc.EmbedDatabaseMetaData40;
import org.apache.derby.impl.jdbc.SQLExceptionFactory40;
import org.apache.derby.impl.jdbc.EmbedResultSetMetaData40;
import org.apache.derby.iapi.jdbc.ResourceAdapter;
import org.apache.derby.impl.jdbc.Util;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.derby.iapi.sql.ResultColumnDescriptor;

/** -- jdbc 2.0. extension -- */
import javax.sql.PooledConnection;
import javax.sql.XAConnection;

public class Driver40 extends Driver30 {
    
    public Connection getNewNestedConnection(EmbedConnection conn) {
        return new EmbedConnection40(conn);
    }
    
    
    protected EmbedConnection getNewEmbedConnection(String url, Properties info)
    throws SQLException {
        return new EmbedConnection40(this, url, info);
    }
    
    /**
     * returns a new EmbedStatement
     * @param  conn                 the EmbedConnection class associated with  
     *                              this statement object
     * @param  forMetaData          boolean
     * @param  resultSetType        int
     * @param  resultSetConcurrency int
     * @param  resultSetHoldability int
     * @return Statement            a new java.sql.Statement implementation
     * 
     */
    public java.sql.Statement newEmbedStatement(
				EmbedConnection conn,
				boolean forMetaData,
				int resultSetType,
				int resultSetConcurrency,
				int resultSetHoldability)
	{
        return new EmbedStatement(conn, forMetaData, resultSetType,
                resultSetConcurrency, resultSetHoldability);
	}
    
    public PreparedStatement
        newEmbedPreparedStatement(
        EmbedConnection conn,
        String stmt,
        boolean forMetaData,
        int resultSetType,
        int resultSetConcurrency,
        int resultSetHoldability,
        int autoGeneratedKeys,
        int[] columnIndexes,
        String[] columnNames)
        throws SQLException {
        return new EmbedPreparedStatement(conn,
            stmt,
            forMetaData,
            resultSetType,
            resultSetConcurrency,
            resultSetHoldability,
            autoGeneratedKeys,
            columnIndexes,
            columnNames);
    }
    
    
    public CallableStatement newEmbedCallableStatement(
        EmbedConnection conn,
        String stmt,
        int resultSetType,
        int resultSetConcurrency,
        int resultSetHoldability)
        throws SQLException {
        return new EmbedCallableStatement(conn,
            stmt,
            resultSetType,
            resultSetConcurrency,
            resultSetHoldability);
    }

    public BrokeredConnection newBrokeredConnection(
            BrokeredConnectionControl control) throws SQLException {
        return new BrokeredConnection40(control);
    }
    
    public EmbedResultSet newEmbedResultSet(EmbedConnection conn, ResultSet results, boolean forMetaData, org.apache.derby.impl.jdbc.EmbedStatement statement,boolean isAtomic) throws SQLException {
        return new EmbedResultSet40(conn, results, forMetaData, statement,
            isAtomic);
    }
    
    /**
     * Overwriting the super class boot method to set exception factory
     * @see InternalDriver#boot
     */

	public void boot(boolean create, Properties properties) 
          throws StandardException {
        Util.setExceptionFactory (new SQLExceptionFactory40 ());
        super.boot (create, properties);
    }

    public DatabaseMetaData newEmbedDatabaseMetaData(EmbedConnection conn, String dbname) 
        throws SQLException {
		return new EmbedDatabaseMetaData40(conn,dbname);
    }
    
        /**
         * Returns a new java.sql.ResultSetMetaData for this implementation
         *
         * @param  columnInfo a ResultColumnDescriptor that stores information 
         *                    about the columns in a ResultSet
         * @return ResultSetMetaData
         */
        public EmbedResultSetMetaData40 newEmbedResultSetMetaData
                             (ResultColumnDescriptor[] columnInfo) {
            return new EmbedResultSetMetaData40(columnInfo);
        }

    /**
     * Create and return an EmbedPooledConnection from the received instance
     * of EmbeddedDataSource.
     */
    protected PooledConnection getNewPooledConnection(
        EmbeddedBaseDataSource eds, String user, String password,
        boolean requestPassword) throws SQLException
    {
        return new EmbedPooledConnection40(
            eds, user, password, requestPassword);
    }

    /**
     * Create and return an EmbedXAConnection from the received instance
     * of EmbeddedBaseDataSource.
     */
    protected XAConnection getNewXAConnection(
        EmbeddedBaseDataSource eds, ResourceAdapter ra,
        String user, String password, boolean requestPassword)
        throws SQLException
    {
        return new EmbedXAConnection40(
            eds, ra, user, password, requestPassword);
    }

    ////////////////////////////////////////////////////////////////////
    //
    // INTRODUCED BY JDBC 4.1 IN JAVA 7
    //
    ////////////////////////////////////////////////////////////////////

    public  Logger getParentLogger()
        throws SQLFeatureNotSupportedException
    {
        throw (SQLFeatureNotSupportedException) Util.notImplemented( "getParentLogger()" );
    }

}
