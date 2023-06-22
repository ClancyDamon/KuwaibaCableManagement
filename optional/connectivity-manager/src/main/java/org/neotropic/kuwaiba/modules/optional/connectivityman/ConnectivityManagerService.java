/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.modules.optional.connectivityman;

import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to manage the persistence operations in the Connectivity Manager
 * Module
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Service
public class ConnectivityManagerService {

    /**
     * Reference to Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Translate Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * A side of the OSP last mile circuit
     */
    public static final String OSP_LAST_MILE_CIRCUIT_ENDPOINT_A = "ospLastMileCircuitEndpointA"; //NO18N
    /**
     * B side of the OSP last mile circuit
     */
    public static final String OSP_LAST_MILE_CIRCUIT_ENDPOINT_B = "ospLastMileCirtuitEndpointB"; //NOI18N

    /**
     * Creates a last mile circuit tributary link.
     *
     * @param endpointAClass The class name of the Endpoint A
     * @param endpointAId The id of the Endpoint A
     * @param endpointBClass The class name of the Endpoint B
     * @param endpointBId The id of the Endpoint B
     * @param linkClass Link Class. The class must be subclass of
     * GenericLastMileTributaryLink
     * @param linkAttributes Attributes for link
     * @param userName The user name who execute the action
     * @throws InvalidArgumentException If the parameters are null. Or if the
     * link class is not subclass of GenericLastMileTributaryLink.
     * @throws MetadataObjectNotFoundException If the link class are null.
     * @throws BusinessObjectNotFoundException If cannot created the link. If
     * attribute values cannot be found.
     * @throws OperationNotPermittedException if cannot created the link. If
     * cannot related the new link with the endpoints.
     * @throws ApplicationObjectNotFoundException If the attribute values cannot
     * be found. If an activity log cannot be created. If link cannot be
     * created.
     * @return The new link id.
     */
    public String createLastMileLink(
            String endpointAClass, String endpointAId, String endpointBClass, String endpointBId,
            String linkClass, HashMap<String, String> linkAttributes, String userName) throws
            MetadataObjectNotFoundException, InvalidArgumentException,
            BusinessObjectNotFoundException, OperationNotPermittedException,
            ApplicationObjectNotFoundException {

        if (!mem.isSubclassOf(Constants.CLASS_GENERICLASTMILECIRCUIT, linkClass)) {
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.ospman.last-mile-circuit.class.is-not-subclass"), linkClass, Constants.CLASS_GENERICLASTMILECIRCUIT));
        }
        if (endpointAClass == null) {
            throw new InvalidArgumentException(ts.getTranslatedString("module.ospman.last-mile-circuit.endpoint-a.class.not-null"));
        }
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPORT, endpointAClass)) {
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.ospman.last-mile-circuit.class.is-not-subclass"), endpointAClass, Constants.CLASS_GENERICPORT));
        }
        if (endpointAId == null) {
            throw new InvalidArgumentException(ts.getTranslatedString("module.ospman.last-mile-circuit.endpoint-a.id.not-null"));
        }
        if (endpointBClass == null) {
            throw new InvalidArgumentException(ts.getTranslatedString("module.ospman.last-mile-circuit.endpoint-b.class.not-null"));
        }
        if (!mem.isSubclassOf(Constants.CLASS_GENERICPORT, endpointBClass)) {
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.ospman.last-mile-circuit.class.is-not-subclass"), endpointBClass, Constants.CLASS_GENERICPORT));
        }
        if (endpointBId == null) {
            throw new InvalidArgumentException(ts.getTranslatedString("module.ospman.last-mile-circuit.endpoint-b.id.not-null"));
        }

        String endpointAName = bem.getAttributeValueAsString(endpointAClass, endpointAId, Constants.PROPERTY_NAME);
        if (bem.getFirstParentOfClass(endpointAClass, endpointAId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT) == null) {
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.ospman.last-mile-circuit.port-not-located-in-communications-equipment"), endpointAName, endpointAClass));
        }

        String endpointBName = bem.getAttributeValueAsString(endpointBClass, endpointBId, Constants.PROPERTY_NAME);
        if (bem.getFirstParentOfClass(endpointBClass, endpointBId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT) == null) {
            throw new InvalidArgumentException(String.format(ts.getTranslatedString("module.ospman.last-mile-circuit.port-not-located-in-communications-equipment"), endpointBName, endpointBClass));
        }

        String linkId = null;
        try {
            linkId = bem.createSpecialObject(linkClass, null, "-1", linkAttributes, null);
            aem.createGeneralActivityLogEntry(userName,
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT,
                    String.format(ts.getTranslatedString("module.ospman.last-mile-circuit.link-created"), linkAttributes.get(Constants.PROPERTY_NAME), linkClass)
            );
            // Side A
            bem.createSpecialRelationship(linkClass, linkId, endpointAClass, endpointAId, OSP_LAST_MILE_CIRCUIT_ENDPOINT_A, true);
            aem.createGeneralActivityLogEntry(userName,
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT,
                    String.format("%s [%s] - %s - %s [%s]", linkAttributes.get(Constants.PROPERTY_NAME), linkClass, OSP_LAST_MILE_CIRCUIT_ENDPOINT_A, endpointAName, endpointAClass)
            );
            // Side B
            bem.createSpecialRelationship(linkClass, linkId, endpointBClass, endpointBId, OSP_LAST_MILE_CIRCUIT_ENDPOINT_B, true);
            aem.createGeneralActivityLogEntry(userName,
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT,
                    String.format("%s [%s] - %s - %s [%s]", linkAttributes.get(Constants.PROPERTY_NAME), linkClass, OSP_LAST_MILE_CIRCUIT_ENDPOINT_B, endpointBName, endpointBClass)
            );
        } catch (Exception ex) {
            if (linkId != null) {
                bem.deleteObject(linkClass, linkId, true);
            }
            throw new InvalidArgumentException(ex.getLocalizedMessage());
        }
        return linkId;
    }
}
