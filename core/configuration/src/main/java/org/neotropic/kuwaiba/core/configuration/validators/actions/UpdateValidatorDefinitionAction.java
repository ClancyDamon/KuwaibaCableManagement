/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neotropic.kuwaiba.core.configuration.validators.actions;

import com.vaadin.flow.component.UI;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Update validator definition.
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Component
public class UpdateValidatorDefinitionAction extends AbstractAction {   

    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    
    @PostConstruct
    protected void init(){       
        this.id = "configman.update-validator-definition";
        this.displayName = ts.getTranslatedString("module.configman.validators.actions.update-validator.name");
        this.description = ts.getTranslatedString("module.configman.validators.actions.update-validator.description");
        this.order = 1000;
        
        setCallback((parameters) -> {
            Session session = UI.getCurrent().getSession().getAttribute(Session.class);
            long validatorDefinitionId = (long) parameters.get(Constants.PROPERTY_ID);
            String validatorName = (String) parameters.get(Constants.PROPERTY_NAME);
            String validatorDescription = (String) parameters.get(Constants.PROPERTY_DESCRIPTION);
            String classToBeApplied = (String) parameters.get(Constants.PROPERTY_CLASSNAME);
            String script = (String) parameters.get(Constants.PROPERTY_SCRIPT);
            Boolean isEnable = (Boolean) parameters.get(Constants.PROPERTY_ENABLED);
            
            try {                
                aem.updateValidatorDefinition(validatorDefinitionId
                        , validatorName
                        , validatorDescription
                        , classToBeApplied
                        , script
                        , isEnable
                        , session.getUser().getUserName());
            } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException  ex) {
                throw new ModuleActionException(ex.getLocalizedMessage());
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
