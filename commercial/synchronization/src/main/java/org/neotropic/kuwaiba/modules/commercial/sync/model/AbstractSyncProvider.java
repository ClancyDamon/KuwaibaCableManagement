/*
 * Copyright 2010-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.neotropic.kuwaiba.modules.commercial.sync.model;

import java.io.Serializable;
import java.util.List;

/**
 * This class describes the generic behavior of all the synchronization providers.
 * A sync provider is just a bunch of logic that actually performs the actions associated to a set of business rules when differences
 * are found between the information retrieved from sync data sources (SNMP-enabled devices, NMS, legacy systems, third-party monitoring systems, etc)
 * and the information in the inventory. The flow is basically the following:
 * <ul>
 * <li>A Sync Job (a simple Java EE batch job) is started.</li>
 * <li>The sync job connects to the sync data sources (devices, databases, etc) and retrieves
 * the information defined in the Sync Data Definition Artifact, which is a file or a Kuwaiba
 * database entry (depends on the implementation) that contains what information will be retrieved from the sync data sources (columns of a table, OIDs, etc)
 * </li>
 * <li>The next stage of the Sync Job asks the Sync Provider to find the differences between the info extracted
 * from the sync data source and the corresponding info in Kuwaiba. The sync provider will return the differences</li>
 * <li>The Sync Job either forward the differences to the user requesting for authorization to perform actions
 * (create/delete elements, update properties, etc) or will perform them automatically</li>
 * <li>The sync job performs the actions with external intervention (e.g. human approval)
 * or automatically, depending on how the sync provider is configured</li>
 * </ul>
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractSyncProvider implements Serializable {
    /**
     * Display name/description of what the providers does
     *
     * @return The configured name/description (typically a hard-coded string)
     */
    public abstract String getDisplayName();

    /**
     * A string that uniquely identifies the current sync provider
     *
     * @return
     */
    public abstract String getId();

    /**
     * Should the actions defined after finding the differences between the information
     * retrieved from the sync data sources and the objects in the inventory be performed automatically
     * or wait for approval
     *
     * @return True if it doesn't require approval, False otherwise
     */
    public abstract boolean isAutomated();

    /**
     * Some synchronization processes are related to a specific inventory object (for example when syncing the hardware of a given device).
     * But there are some other scenarios where the the sync process uses information from many sources and none of them are necessarily mapped to an inventory object.
     * A common example is when the sync process does not retrieve information from particular devices, but from an NMS, and the NMS provides information about the whole network,
     * e.g. the logical circuits between different nodes in the network. This method should be implemented for the latter.
     *
     * @param syncGroup The sync group to be synchronized
     * @return The raw information necessary to actually perform the synchronization process. This information is not related to any particular inventory object.
     */
    public abstract List<AbstractDataEntity> unmappedPoll(SynchronizationGroup syncGroup);

    /**
     * Some synchronization processes are related to a specific inventory object (for example when syncing the hardware of a given device).
     * But there are some other scenarios where the the sync process uses information from many sources and none of them are necessarily mapped to an inventory object.
     * A common example is when the sync process does not retrieve information from particular devices, but from an NMS, and the NMS provides information about the whole network,
     * e.g. the logical circuits between different nodes in the network. This method should be implemented for the former.
     *
     * @param syncGroup The sync group to be synchronized
     * @return The raw information necessary to actually perform the synchronization process. Each PollResult object contains the information of how the polling process
     * turned out per each one of the Sync Configurations contained in the Sync Group, that is, each PollResult maps an inventory object
     */
    public abstract PollResult mappedPoll(SynchronizationGroup syncGroup);

    /**
     * Some synchronization processes are related to a specific inventory object (for example when syncing the hardware of a given device).
     * But there are some other scenarios where the the sync process uses information from many sources and none of them are necessarily mapped to an inventory object.
     * A common example is when the sync process does not retrieve information from particular devices, but from an NMS, and the NMS provides information about the whole network,
     * e.g. the logical circuits between different nodes in the network. This method should be implemented for the former.
     *
     * @param syncDataSrc The sync data source to be used
     * @return The raw information necessary to actually perform the synchronization process. Each PollResult object contains the information of how the polling process
     * turned out per each one of the Sync Configurations contained in the Sync Group, that is, each PollResult maps an inventory object
     */
    public abstract PollResult fetchData(SyncDataSourceConfiguration syncDataSrc);

    /**
     * Implement this method if the supervised synchronization process will be associated to an object in the inventory, for example,
     * you will retrieve the hardware information about a network element and find what has changed overnight.
     *
     * @param pollResult A set of high-level representations of the info coming from the sync data source and the corresponding inventory object it should be mapped against
     *                   (for example, a Java matrix representing an SNMP table)
     * @return A set of results (e.g. new board on slot xxx, different serial number found for router yyyy)
     */
    public abstract List<SyncFinding> supervisedSync(PollResult pollResult);

    /**
     * Implement this method if the supervised synchronization process won't be associated to a single object in the inventory, for example,
     * if you want to see what virtual circuits were re-routed after switching to a backup link during a network failure
     *
     * @param originalData A set of high-level representations of the info coming from the sync data source
     *                     (for example, a Java list representing the hops of a virtual circuits)
     * @return A set of results (circuit YYY has a new route zzzz)
     */
    public abstract List<SyncFinding> supervisedSync(List<AbstractDataEntity> originalData);

    /**
     * Implement this method if the supervised synchronization process won't be associated to a single object in the inventory, for example,
     * if you want to see what virtual circuits were re-routed after switching to a backup link during a network failure
     *
     * @param originalData A set of high-level representations of the info coming from the sync data source
     *                     (for example, a Java list representing the hops of a virtual circuits)
     * @return A set of results (e.g. circuit YYY was re-routed to zzzz)
     */
    public abstract List<SyncResult> automatedSync(List<AbstractDataEntity> originalData);

    /**
     * Implement this method if the supervised synchronization process will be associated to an object in the inventory, for example,
     * you will retrieve the hardware information about a network element and find what has changed overnight.
     *
     * @param pollResult A set of high-level representations of the info coming from the sync data source and the corresponding inventory object it should be mapped against
     *                   (for example, a Java matrix representing an SNMP table)
     * @return A set of results (e.g. new board on slot xxx was created, different serial number was set to router yyyy)
     */
    public abstract List<SyncResult> automatedSync(PollResult pollResult);

    /**
     * Performs the actual actions
     *
     * @param actions
     * @return
     */
    public abstract List<SyncResult> finalize(List<SyncAction> actions);
}
