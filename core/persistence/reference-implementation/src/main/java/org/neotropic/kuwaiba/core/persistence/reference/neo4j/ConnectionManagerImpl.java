/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.core.persistence.reference.neo4j;

import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ConnectionException;
import java.io.File;
import java.util.List;
import java.util.Properties;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.configuration.BoltConnector;
import org.neo4j.kernel.configuration.Settings;
import org.springframework.stereotype.Service;
import static org.neo4j.kernel.configuration.Settings.buildSetting;

/**
 * ConnectionManager reference implementation using Neo4J as DBMS
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Service
public class ConnectionManagerImpl implements ConnectionManager<GraphDatabaseService> {
    /**
     * Default db path
     */
    private static final String DEFAULT_DB_PATH = "/data/db/kuwaiba.db";
    /**
     * Default db host (used by the Bolt protocol)
     */
    private static final String DEFAULT_DB_HOST = "localhost";
    /**
     * Default port host (used by the Bolt protocol)
     */
    private static final int DEFAULT_DB_PORT = 7070;
    /**
     * Custom configuration entry to disable the N4J's Usage Data Collector. 
     */
    public static final Setting<Boolean> udc_enabled =
            buildSetting("dbms.udc.enabled", Settings.BOOLEAN, Settings.FALSE).build();
    /**
     * Database path
     */
    private Properties configuration;
    /**
     * Neo4J Database handler
     */
    private GraphDatabaseService graphDb;
    /**
     * Neo4J Transaction handler
     */
    private Transaction tx;
    

    public ConnectionManagerImpl() {
        configuration = new Properties();
    }

    @Override
    public void closeConnection() {
        if (graphDb != null)
            graphDb.shutdown();
    }

    @Override
    public void commitTransaction() {
        tx.success();
    }

    @Override
    public List<ConnectionManager> getConnectionPool() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSpawned() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void openConnection() throws ConnectionException {
        try {
            String dbPathString = configuration.getProperty("dbPath", DEFAULT_DB_PATH);
            String dbHost = configuration.getProperty("dbHost", DEFAULT_DB_HOST);
            int dbPort = (Integer)(configuration.getOrDefault("dbPort", DEFAULT_DB_PORT));
            
            File dbFile = new File(dbPathString);
            if (!dbFile.exists() || !dbFile.canWrite())
                throw new Exception(String.format("Path %s does not exist or is not writeable", dbFile.getAbsolutePath()));
            BoltConnector bolt = new BoltConnector();
            graphDb = new GraphDatabaseFactory()
                        .newEmbeddedDatabaseBuilder(dbFile)
                        .setConfig(bolt.type, "BOLT")
                        .setConfig(bolt.enabled, "true")
                        .setConfig(bolt.listen_address,  dbHost + ":" + dbPort)
                        .setConfig(udc_enabled, "false")
                        .newGraphDatabase();
        }catch(Exception e) {
            throw new ConnectionException(e.getMessage());
        }
    }

    @Override
    public void rollbackTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ConnectionManager spawnConnection() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public GraphDatabaseService getConnectionHandler() {
        return graphDb;
    }

    @Override
    public void shutDown() {
        System.out.println( "Shutting down database..." );
        graphDb.shutdown();
        System.out.println( "Database shut down" );
    }
    
    @Override
    public String getConnectionDetails() {
        return graphDb.toString();
    }

    @Override
    public void setConfiguration(Properties properties) {
        this.configuration = properties;
    }
}
