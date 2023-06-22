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
package org.neotropic.kuwaiba.modules.commercial.contractman.actions;

import com.vaadin.flow.component.UI;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.contractman.ContractManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a new Contract.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewContractAction extends AbstractAction {
    /**
     * Reference to the Contract Manager Service
     */
    @Autowired 
    private ContractManagerService cms;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;  
    /**
     * Parameter pool
     */
    public static String PARAMETER_POOL = "pool";
    
    @PostConstruct
    protected void init() {
        this.id = "contractman.new-contract";
        this.displayName = ts.getTranslatedString("module.contractman.actions.contract.new-contract.name");
        this.description = ts.getTranslatedString("module.contractman.actions.contract.new-contract.description");
        this.order = 1000;
        
        setCallback((parameters) -> {
            Session session = UI.getCurrent().getSession().getAttribute(Session.class);
            InventoryObjectPool pool = (InventoryObjectPool) parameters.get(PARAMETER_POOL);
            String className = (String) parameters.get(Constants.PROPERTY_CLASSNAME);
            String name = (String) parameters.get(Constants.PROPERTY_NAME);
            String desc = (String) parameters.get(Constants.PROPERTY_DESCRIPTION);
            HashMap<String, String> attributes = new HashMap();
            attributes.put(Constants.PROPERTY_NAME, name);
            attributes.put(Constants.PROPERTY_DESCRIPTION, desc); 
            try {
                cms.createContract(pool.getId(), className, attributes, session.getUser().getUserName());
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException |
                    MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                throw new ModuleActionException(ex.getMessage());
            }
            return new ActionResponse();     
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