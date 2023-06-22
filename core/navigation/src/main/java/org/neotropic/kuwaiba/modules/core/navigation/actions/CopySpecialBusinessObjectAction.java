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

import com.vaadin.flow.component.icon.VaadinIcon;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
 * Copy as special a business object to another business object.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Component
public class CopySpecialBusinessObjectAction extends AbstractAction {
    /**
     * Copy as special a business object action parameter target class.
     */
    public static String PARAM_TARGET_CLASS = "targetClass"; //NOI18N
    /**
     * Copy as special a business object action parameter target id.
     */
    public static String PARAM_TARGET_ID = "targetId"; //NOI18N
    /**
     * Copy as special a business object action parameter class.
     */
    public static String PARAM_OBJECT_CLASS = "class"; //NOI18N
    /**
     * Copy as special a business object action parameter id.
     */
    public static String PARAM_OBJECT_ID = "id"; //NOI18N
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
        this.id = "navigation.copy-special-business-object"; //NOI18N
        this.displayName = ts.getTranslatedString("module.navigation.actions.copy-special-business-object.name");
        this.description = ts.getTranslatedString("module.navigation.actions.copy-special-business-object.description");
        this.icon = VaadinIcon.COPY.create();
        setCallback(parameters -> {
            try {
                String targetClass = (String) parameters.get(PARAM_TARGET_CLASS);
                String targetId = (String) parameters.get(PARAM_TARGET_ID);
                String objectClass = (String) parameters.get(PARAM_OBJECT_CLASS);
                String objectId = (String) parameters.get(PARAM_OBJECT_ID);

                HashMap<String, List<String>> objects  = new HashMap();
                objects.put(objectClass, Arrays.asList(objectId));
            
                bem.copySpecialObjects(targetClass, targetId, objects, true);
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