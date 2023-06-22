/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.modules.core.ltman.actions;

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Delete a List Type item.
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class DeleteListTypeItemAction extends AbstractAction {

    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;

    /**
     * Reference to the application entity manager.
     */
    @Autowired
    private ApplicationEntityManager aem;

    @PostConstruct
    protected void init() {
        this.id = "listtypeman.delete-list-type-item";
        this.displayName = ts.getTranslatedString("module.ltman.actions.delete-list-type-item.name");
        this.description = ts.getTranslatedString("module.ltman.actions.delete-list-type-item.description");
        this.order = 1000;

        setCallback((parameters) -> {
            String className = (String) parameters.get("className");
            String oid = (String) parameters.get("oid");

            try {
                aem.deleteListTypeItem(className, oid, false);
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
