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
package org.neotropic.kuwaiba.modules.optional.physcon.actions;

import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsModule;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Action to get a window to manage port mirroring
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Component
public class ManagePortMirroringVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Reference to Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference of the module Action to manage port mirroring.
     */
    @Autowired
    private ManagePortMirroringAction managePortMirroringAction;
    /**
     * Reference of new business object visual action.
     */
    @Autowired
    private NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    /**
     * Reference of new business object from template visual action.
     */
    @Autowired
    private NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction;
    /**
     * Reference of new multiple business object visual action.
     */
    @Autowired
    private NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectVisualAction;

    public ManagePortMirroringVisualAction() {
        super(PhysicalConnectionsModule.MODULE_ID);
    }
    
    @Override
    public String appliesTo() {
        return "GenericPort";
    }

    @Override
    public WindowManagePortMirroring getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight businessObject = (BusinessObjectLight) parameters.get("businessObject");
        try {
            if (mem.isSubclassOf(Constants.CLASS_GENERICPORT, businessObject.getClassName())) {
                return new WindowManagePortMirroring(businessObject, bem ,mem, ts,
                        newBusinessObjectVisualAction,
                        newBusinessObjectFromTemplateVisualAction,
                        newMultipleBusinessObjectVisualAction, this);
            }
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
        return new WindowManagePortMirroring0(businessObject, bem, ts);
    }

    @Override
    public AbstractAction getModuleAction() {
        return managePortMirroringAction;
    }
    
    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
}
