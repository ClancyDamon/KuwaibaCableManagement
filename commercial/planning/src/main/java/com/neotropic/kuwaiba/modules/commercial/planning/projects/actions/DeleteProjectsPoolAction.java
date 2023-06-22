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
package com.neotropic.kuwaiba.modules.commercial.planning.projects.actions;

import com.neotropic.kuwaiba.modules.commercial.planning.projects.ProjectsService;
import com.vaadin.flow.component.UI;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Delete a Projects Pool.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteProjectsPoolAction extends AbstractAction {
    /**
     * Reference to the Project Service
     */
    @Autowired
    ProjectsService ps;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;

    @PostConstruct
    protected void init() {
        this.id = "projects.delete-pool";
        this.displayName = ts.getTranslatedString("module.projects.actions.pool.delete-pool.name");
        this.description = ts.getTranslatedString("module.projects.actions.pool.delete-pool.description");
        this.order = 1000;
        
        setCallback((parameters) -> {
            Session session = UI.getCurrent().getSession().getAttribute(Session.class);
            InventoryObjectPool pool = (InventoryObjectPool) parameters.get("pool");
            ActionResponse actionReesponse = new ActionResponse();
            try {    
                ps.deleteProjectPool(pool.getId()
                        , pool.getClassName()
                        , session.getUser().getUserName()
                );
            } catch (ApplicationObjectNotFoundException | OperationNotPermittedException 
                    | MetadataObjectNotFoundException | InvalidArgumentException ex) {
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
        return false;
    }
}