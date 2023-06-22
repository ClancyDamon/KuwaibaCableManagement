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
 * Detaches a file from an inventory object and deletes it from the file system.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class DetachFileAction extends AbstractAction {
    /**
     * The id of the attachment to be removed.
     */
    public static String PARAM_FILE_OBJECT_ID = "fileObjectId"; //NOI18N
    /**
     * The class name of the object the attachment is related to.
     */
    public static String PARAM_CLASS_NAME = "className"; //NOI18N
    /**
     * The id of the object the attachment is related to.
     */
    public static String PARAM_OBJECT_ID = "objectId"; //NOI18N
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    
    @PostConstruct
    protected void init() {
        this.id = "navigation.detach-file";
        this.displayName = ts.getTranslatedString("module.navigation.actions.detach-file.name");
        this.description = ts.getTranslatedString("module.navigation.actions.detach-file.description");
        this.icon = new Icon(VaadinIcon.FILE);
        this.order = 4;
        
        setCallback(parameters -> {
            try {
                long fileObjectId = (long) parameters.get(PARAM_FILE_OBJECT_ID);
                String className = (String) parameters.get(PARAM_CLASS_NAME);
                String objectId = (String) parameters.get(PARAM_OBJECT_ID);

                bem.detachFileFromObject(fileObjectId, className, objectId);
                
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
