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
package org.neotropic.kuwaiba.modules.commercial.sync.components;

import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.StringProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A class that build property sets given sync data source configurations.
 *
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class SyncDataSourceConfigurationPropertySheet {

    /**
     * Display sync data source configuration properties; used in sync framework
     *
     * @param syncDataSourceConfiguration; Synchronization Data Source Configuration
     * @param ts;                          Translation Service
     * @param bem;                         Business Entity Manager
     * @return object properties
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public static List<AbstractProperty> propertiesFromSyncDataSourceConfiguration(SyncDataSourceConfiguration syncDataSourceConfiguration,
                                                                                   TranslationService ts, BusinessEntityManager bem)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        ArrayList<AbstractProperty> objectProperties = new ArrayList<>();
        AbstractProperty property;

        // Parameter list
        HashMap<String, String> parameters = syncDataSourceConfiguration.getParameters();

        // --> General Properties
        property = new StringProperty(
                Constants.PROPERTY_NAME,
                Constants.PROPERTY_NAME,
                Constants.PROPERTY_NAME,
                syncDataSourceConfiguration.getName() == null
                        || syncDataSourceConfiguration.getName().isEmpty()
                        ? AbstractProperty.NULL_LABEL : syncDataSourceConfiguration.getName(),
                ts);
        objectProperties.add(property);

        property = new StringProperty(
                Constants.PROPERTY_DEVICE,
                Constants.PROPERTY_DEVICE,
                Constants.PROPERTY_DEVICE,
                parameters.get(Constants.PROPERTY_DEVICE) == null
                        || parameters.get(Constants.PROPERTY_DEVICE).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_DEVICE),
                ts,
                true);
        objectProperties.add(property);

        property = new StringProperty(
                Constants.PROPERTY_DEVICE_ID,
                Constants.PROPERTY_DEVICE_ID,
                Constants.PROPERTY_DEVICE_ID,
                parameters.get(Constants.PROPERTY_DEVICE_ID) == null
                        || parameters.get(Constants.PROPERTY_DEVICE_ID).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_DEVICE_ID),
                ts,
                true);
        objectProperties.add(property);

        property = new StringProperty(
                Constants.PROPERTY_IP_ADDRESS,
                Constants.PROPERTY_IP_ADDRESS,
                Constants.PROPERTY_IP_ADDRESS,
                parameters.get(Constants.PROPERTY_IP_ADDRESS) == null
                        || parameters.get(Constants.PROPERTY_IP_ADDRESS).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_IP_ADDRESS),
                ts);
        objectProperties.add(property);

        property = new StringProperty(
                Constants.PROPERTY_PORT,
                Constants.PROPERTY_PORT,
                Constants.PROPERTY_PORT,
                parameters.get(Constants.PROPERTY_PORT) == null
                        || parameters.get(Constants.PROPERTY_PORT).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_PORT),
                ts);
        objectProperties.add(property);

        // --> SNMP Version 2c Properties
        property = new StringProperty(
                Constants.PROPERTY_SNMP_VERSION,
                Constants.PROPERTY_SNMP_VERSION,
                Constants.PROPERTY_SNMP_VERSION,
                parameters.get(Constants.PROPERTY_SNMP_VERSION) == null
                        || parameters.get(Constants.PROPERTY_SNMP_VERSION).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_SNMP_VERSION),
                ts);
        objectProperties.add(property);

        property = new StringProperty(
                Constants.PROPERTY_COMMUNITY,
                Constants.PROPERTY_COMMUNITY,
                Constants.PROPERTY_COMMUNITY,
                parameters.get(Constants.PROPERTY_COMMUNITY) == null
                        || parameters.get(Constants.PROPERTY_COMMUNITY).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_COMMUNITY),
                ts);
        objectProperties.add(property);

        // --> SNMP Version 3 Properties
        property = new StringProperty(
                Constants.PROPERTY_AUTH_PROTOCOL,
                Constants.PROPERTY_AUTH_PROTOCOL,
                Constants.PROPERTY_AUTH_PROTOCOL,
                parameters.get(Constants.PROPERTY_AUTH_PROTOCOL) == null
                        || parameters.get(Constants.PROPERTY_AUTH_PROTOCOL).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_AUTH_PROTOCOL),
                ts);
        objectProperties.add(property);

        property = new StringProperty(
                Constants.PROPERTY_AUTH_PASS,
                Constants.PROPERTY_AUTH_PASS,
                Constants.PROPERTY_AUTH_PASS,
                parameters.get(Constants.PROPERTY_AUTH_PASS) == null
                        || parameters.get(Constants.PROPERTY_AUTH_PASS).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_AUTH_PASS),
                ts);
        objectProperties.add(property);

        property = new StringProperty(
                Constants.PROPERTY_SECURITY_LEVEL,
                Constants.PROPERTY_SECURITY_LEVEL,
                Constants.PROPERTY_SECURITY_LEVEL,
                parameters.get(Constants.PROPERTY_SECURITY_LEVEL) == null
                        || parameters.get(Constants.PROPERTY_SECURITY_LEVEL).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_SECURITY_LEVEL),
                ts);
        objectProperties.add(property);

        property = new StringProperty(
                Constants.PROPERTY_SECURITY_NAME,
                Constants.PROPERTY_SECURITY_NAME,
                Constants.PROPERTY_SECURITY_NAME,
                parameters.get(Constants.PROPERTY_SECURITY_NAME) == null
                        || parameters.get(Constants.PROPERTY_SECURITY_NAME).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_SECURITY_NAME),
                ts);
        objectProperties.add(property);

        property = new StringProperty(
                Constants.PROPERTY_CONTEXT_NAME,
                Constants.PROPERTY_CONTEXT_NAME,
                Constants.PROPERTY_CONTEXT_NAME,
                parameters.get(Constants.PROPERTY_CONTEXT_NAME) == null
                        || parameters.get(Constants.PROPERTY_CONTEXT_NAME).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_CONTEXT_NAME),
                ts);
        objectProperties.add(property);

        property = new StringProperty(
                Constants.PROPERTY_PRIVACY_PROTOCOL,
                Constants.PROPERTY_PRIVACY_PROTOCOL,
                Constants.PROPERTY_PRIVACY_PROTOCOL,
                parameters.get(Constants.PROPERTY_PRIVACY_PROTOCOL) == null
                        || parameters.get(Constants.PROPERTY_PRIVACY_PROTOCOL).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_PRIVACY_PROTOCOL),
                ts);
        objectProperties.add(property);

        property = new StringProperty(
                Constants.PROPERTY_PRIVACY_PASS,
                Constants.PROPERTY_PRIVACY_PASS,
                Constants.PROPERTY_PRIVACY_PASS,
                parameters.get(Constants.PROPERTY_PRIVACY_PASS) == null
                        || parameters.get(Constants.PROPERTY_PRIVACY_PASS).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_PRIVACY_PASS),
                ts);
        objectProperties.add(property);

        // --> SSH Properties
        property = new StringProperty(
                Constants.PROPERTY_SSH_PORT,
                Constants.PROPERTY_SSH_PORT,
                Constants.PROPERTY_SSH_PORT,
                parameters.get(Constants.PROPERTY_SSH_PORT) == null
                        || parameters.get(Constants.PROPERTY_SSH_PORT).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_SSH_PORT),
                ts);
        objectProperties.add(property);

        property = new StringProperty(
                Constants.PROPERTY_SSH_USER,
                Constants.PROPERTY_SSH_USER,
                Constants.PROPERTY_SSH_USER,
                parameters.get(Constants.PROPERTY_SSH_USER) == null
                        || parameters.get(Constants.PROPERTY_SSH_USER).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_SSH_USER),
                ts);
        objectProperties.add(property);

        property = new StringProperty(
                Constants.PROPERTY_SSH_PASSWORD,
                Constants.PROPERTY_SSH_PASSWORD,
                Constants.PROPERTY_SSH_PASSWORD,
                parameters.get(Constants.PROPERTY_SSH_PASSWORD) == null
                        || parameters.get(Constants.PROPERTY_SSH_PASSWORD).isEmpty()
                        ? AbstractProperty.NULL_LABEL : parameters.get(Constants.PROPERTY_SSH_PASSWORD),
                ts);
        objectProperties.add(property);

        return objectProperties;
    }
}