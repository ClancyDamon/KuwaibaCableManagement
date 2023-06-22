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

package org.neotropic.kuwaiba.core.apis.persistence;

import java.util.List;
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ConnectionException;

/**
 * Interface providing the general methods to manage the db/backend connection
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 * @param <T> The type of database handler being used. In the default implementation using Neo4J, this is a GraphDatabaseService instance
 */
public interface ConnectionManager<T> {
    /**
     * Opens a database connection (an probably initialize the connection handler)
     * @throws ConnectionException If the attempt to open the connection to the database failed
     */
    public void openConnection() throws ConnectionException;
    /**
     * Closes the connection with the database
     */
    public void closeConnection();
    /**
     * Starts a transaction (if supported by the backend)
     */
    public void startTransaction();
    /**
     * Commits the current transaction (if supported by the backend) 
     */
    public void commitTransaction();
    /**
     * Rolls back the current transaction (if supported by the backend)
     */
    public void rollbackTransaction();
    /**
     * Creates a new connection from the original one
     * @return Another connection manager
     */
    public ConnectionManager spawnConnection();
    /**
     * Tells if the current manager is one spawned from another.
     * @return 
     */
    public boolean isSpawned();
    /**
     * Gets the list of spawned connections
     * @return The list of spawned connections
     */
    public List<ConnectionManager> getConnectionPool();
    /**
     * Returns the connection handler, which depends on the kind of backed being used.
     * @return 
     */
    public T getConnectionHandler();
    /**
     * Releases the resources associated to this ConnectionManager, assuming that closeConnection didn't do it completely.
     */
    public void shutDown();
    /**
     * Textual description a bout the connection
     * @return 
     */
    public String getConnectionDetails();
    /**
     * Sets the properties necessary to establish and manage the connection with the database (login, passwd, hostname, etc). It is typically called before openConnection
     * @param properties A properties object with the pairs name-value of all properties to be set.
     */
    public void setConfiguration(Properties properties);
}