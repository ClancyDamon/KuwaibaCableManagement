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
package org.neotropic.kuwaiba.modules.optional.taskman.actions;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.Task;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Delete a task parameter
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteTaskParameterAction extends AbstractAction {
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
        this.id = "taskman.new-task-parameter";
        this.displayName = ts.getTranslatedString("module.taskman.task.actions.delete-task-parameter.name");
        this.description = ts.getTranslatedString("module.taskman.task.actions.delete-task-parameter.description");
        this.order = 1000;

        setCallback((parameters) -> {
            Task task = (Task) parameters.get("task");
            String key = (String) parameters.get("key");
            StringPair removeParameter = new StringPair(key, null);
            List<StringPair> listParameter = new ArrayList();
            listParameter.add(removeParameter);
            try {
                aem.updateTaskParameters(task.getId(), listParameter);
            } catch (ApplicationObjectNotFoundException ex) {
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