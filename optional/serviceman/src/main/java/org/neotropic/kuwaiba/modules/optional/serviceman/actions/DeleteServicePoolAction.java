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
package org.neotropic.kuwaiba.modules.optional.serviceman.actions;

import com.vaadin.flow.component.UI;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.serviceman.ServiceManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Deletes a service pool.
 * @author Mauricio Ruiz Beltran {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteServicePoolAction extends AbstractAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Service Manager Service
     */
    @Autowired
    private ServiceManagerService sms;
    /**
     * Parameter for service pool
     */
    private static final String PARAMETER_SERVICE_POOL = "servicePool";
    
    @PostConstruct
    protected void init() {
        this.id = "serviceman.delete-service-pool";
        this.displayName = ts.getTranslatedString("module.serviceman.actions.delete-service-pool.name");
        this.description = ts.getTranslatedString("module.serviceman.actions.delete-service-pool.description");
        this.order = 10;
    
        setCallback((parameters) -> {
            Session session = UI.getCurrent().getSession().getAttribute(Session.class);
            InventoryObjectPool servicePool = (InventoryObjectPool) parameters.get(PARAMETER_SERVICE_POOL);
            ActionResponse actionReesponse = new ActionResponse();
            try {
                sms.deleteServicePool(servicePool.getId(), servicePool.getClassName(), session.getUser().getUserName());
            } catch (ApplicationObjectNotFoundException | OperationNotPermittedException ex) {
                actionReesponse.put("exception", ex);
            }
            return actionReesponse;
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