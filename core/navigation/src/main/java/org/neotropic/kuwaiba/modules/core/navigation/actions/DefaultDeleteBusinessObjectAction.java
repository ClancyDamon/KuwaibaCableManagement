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
package org.neotropic.kuwaiba.modules.core.navigation.actions;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default action to delete a business object. It tries to delete the said object and its 
 * children and special children, assuming none of these have other relationships attached 
 * to them. Otherwise an error is returned. This is a fallback action if no specific delete 
 * action is specified in your module. Purpose-specific delete actions should inherit from {@link AbstractDeleteAction}.
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class DefaultDeleteBusinessObjectAction extends AbstractAction {
    /**
     * New business object action parameter id.
     */
    public static String PARAM_OBJECT_OID = "id"; //NOI18N
    /**
     * New business object action parameter template id.
     */
    public static String PARAM_OBJECT_CLASS_NAME = "class"; //NOI18N
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    
    @PostConstruct
    protected void init() {
        this.id = "navigation.delete-business-object";
        this.displayName = ts.getTranslatedString("module.navigation.actions.delete-business-object.name");
        this.description = ts.getTranslatedString("module.navigation.actions.delete-business-object.description");
        this.icon = new Icon(VaadinIcon.TRASH);
        this.order = 2;
        
        setCallback(parameters -> {
            try {
                String className = (String) parameters.get(PARAM_OBJECT_CLASS_NAME);
                String objectId = (String) parameters.get(PARAM_OBJECT_OID);
            
                bem.deleteObject(className, objectId, false);
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
        return true;
    }
}
