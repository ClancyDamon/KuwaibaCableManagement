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
package org.neotropic.kuwaiba.modules.optional.serviceman.actions;

import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractRelationshipManagementAction;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.serviceman.ServiceManagerModule;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handles the relationships between network resources and services. The relationship name is "uses".
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class NetworkResourcesRelationshipManagementAction extends AbstractRelationshipManagementAction {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;

    public NetworkResourcesRelationshipManagementAction() {
        super(ServiceManagerModule.MODULE_ID);
    }
    
    @Override
    public String getTargetObjectClass() {
        return "GenericService";
    }

    @Override
    public String getRelationshipName() {
        return "uses";
    }

    @Override
    public String getIncomingRelationshipDisplayName() {
        return ts.getTranslatedString("module.serviceman.actions.manage-relationships.incoming");
    }

    @Override
    public String getOutgoingRelationshipDisplayName() {
        return ts.getTranslatedString("module.serviceman.actions.manage-relationships.outgoing");
    }

    @Override
    public RelationshipCardinality getCardinality() {
        return RelationshipCardinality.ONE_TO_MANY;
    }
    
    @Override
    public String getName() {
        return ts.getTranslatedString("module.serviceman.actions.manage-relationships.name");
    }

    @Override
    public int getRequiredSelectedObjects() {
        return SELECTION_ANY_OBJECTS;
    }

    @Override
    public void createRelationship(List<BusinessObjectLight> sourceObjects, List<BusinessObjectLight> targetObjects) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractAction getModuleAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ConfirmDialog getVisualComponent(ModuleActionParameterSet parameters) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void releaseRelationship(List<BusinessObjectLight> sourceObjects, List<BusinessObjectLight> targetObjects) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
