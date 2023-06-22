/*
 * Copyright 2010-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.softman.actions;

import com.vaadin.flow.component.UI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.softman.SoftwareManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static org.neotropic.kuwaiba.modules.commercial.softman.SoftwareManagerService.RELATIONSHIP_LICENSEHAS;

/**
 * An action to relate objects (initially <code>GenericCommunicationElement</code> instances)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class RelateToLicenseAction extends AbstractAction {
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Software Manager Service
    */
    @Autowired
    private SoftwareManagerService sms;
    /**
     * The ApplicationEntityManager instance
     */
    @Autowired
    private ApplicationEntityManager aem;

    @PostConstruct
    protected void init() {
        this.id = "softman.relate-license";
        this.displayName = ts.getTranslatedString("module.softman.actions.relate-to-license.name");
        this.description = ts.getTranslatedString("module.softman.actions.relate-to-license.description");
        
        setCallback((parameters) -> {
            Session session = UI.getCurrent().getSession().getAttribute(Session.class);  
            BusinessObjectLight object = (BusinessObjectLight) parameters.get("businessObject");
            BusinessObjectLight license = (BusinessObjectLight) parameters.get("license");
            
            try {
                sms.relateObjectToLicense(license.getClassName(), license.getId(), object.getClassName(), object.getId());
                aem.createObjectActivityLogEntry(session.getUser().getUserName(), object.getClassName(), object.getId(),
                        ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, Constants.PROPERTY_NAME, "", RELATIONSHIP_LICENSEHAS,
                        String.format("module.softman.actions.relate-object-to-license.relationship-log", object.getId(), object.getClassName(), license.getId(), license.getClassName()));
                
            } catch (MetadataObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException | ApplicationObjectNotFoundException ex) {
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