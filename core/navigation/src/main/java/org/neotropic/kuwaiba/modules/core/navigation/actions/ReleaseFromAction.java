/*
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.core.navigation.actions;

import com.vaadin.flow.component.UI;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Release a business object from another business object.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class ReleaseFromAction extends AbstractAction {
    /**
     * Parameter object class name.
     */
    public static String PARAM_OBJECT_CLASS = "objectClass"; //NOI18
    /**
     * Parameter by object id.
     */
    public static String PARAM_OBJECT_ID = "objectId"; //NOI18
    /**
     * Parameter by other target id.
     */
    public static String PARAM_OTHER_OBJECT_ID = "otherObjectId"; //NOI18N
    /**
     * Parameter by relationship name.
     */
    public static String PARAM_RELATIONSHIP_NAME = "relationshipName";
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * The ApplicationEntityManager instance
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    
    @PostConstruct
    protected void init() {
        this.id = "navigation.release-from";
        this.displayName = ts.getTranslatedString("module.navigation.actions.release-from.name");
        this.description = ts.getTranslatedString("module.navigation.actions.release-from.description");
        this.order = 1;
        
        setCallback(parameters -> {
            try {
                String objectClass = (String) parameters.get(PARAM_OBJECT_CLASS);
                String objectId = (String) parameters.get(PARAM_OBJECT_ID);
                String otherObjectId = (String) parameters.get(PARAM_OTHER_OBJECT_ID);
                String relationshipName = (String) parameters.get(PARAM_RELATIONSHIP_NAME);
                
                bem.releaseSpecialRelationship(
                        objectClass, 
                        objectId, 
                        otherObjectId, 
                        relationshipName
                );
                
                Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                aem.createObjectActivityLogEntry(
                        session.getUser().getUserName(),
                        objectClass,
                        objectId,
                        ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT,
                        Constants.PROPERTY_NAME,
                        relationshipName,
                        "",
                        String.format(ts.getTranslatedString("module.navigation.actions.release-from.relationship-log"),
                                objectId, objectClass, otherObjectId)
                );
                return new ActionResponse();
            } catch (InventoryException ex) {
                throw new ModuleActionException(ex.getMessage());
            }
        });
    }
    
    @Override
    public int getRequiredAccessLevel() {
        return Privilege.ACCESS_LEVEL_READ_WRITE;
    }

    @Override
    public boolean requiresConfirmation() {
        return false;
    }    
}