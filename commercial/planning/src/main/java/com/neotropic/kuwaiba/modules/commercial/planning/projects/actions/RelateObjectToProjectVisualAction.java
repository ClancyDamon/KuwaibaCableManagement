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

import com.neotropic.kuwaiba.modules.commercial.planning.projects.ProjectsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAdvancedAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.selectors.BusinessObjectSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of relate object action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class RelateObjectToProjectVisualAction extends AbstractVisualAdvancedAction {
    /**
     * Parameter business object.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject";
    /**
     * Parameter project.
     */
    public static String PARAM_PROJECT = "project";
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * References to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private RelateObjectToProjectAction relateObjectToProjectAction;
    /**
     * Window to associate object with a project
     */
    private ConfirmDialog wdwRelate;
    /**
     * Target project;
     */
    private BusinessObjectLight targetProject;

    public RelateObjectToProjectVisualAction() {
        super(ProjectsModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey(PARAM_BUSINESS_OBJECT)) {
            BusinessObjectLight selectedObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
            BusinessObjectSelector lytContent = new BusinessObjectSelector(
                    ts.getTranslatedString("module.projects.actions.relate-object-to-project.placeholder"),
                    false, aem, bem, mem, ts, Constants.CLASS_GENERICPROJECT);

            wdwRelate = new ConfirmDialog(ts,
                    String.format(ts.getTranslatedString("module.projects.actions.relate-object-to-project.title"), selectedObject.toString()),
                    ts.getTranslatedString("module.general.messages.relate"));
            wdwRelate.getBtnConfirm().setEnabled(false);
            wdwRelate.setDraggable(true);
            wdwRelate.setResizable(true);
            wdwRelate.setWidth("60%");
            
            wdwRelate.getBtnConfirm().addClickListener(e -> {
                if (targetProject != null) {
                    try {
                        if (mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, targetProject.getClassName())) {
                            relateObjectToProjectAction.getCallback().execute(new ModuleActionParameterSet(
                                    new ModuleActionParameter<>(PARAM_BUSINESS_OBJECT, selectedObject),
                                    new ModuleActionParameter<>(PARAM_PROJECT, targetProject)));
                            
                            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                                    ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                    String.format(ts.getTranslatedString("module.projects.actions.relate-object-to-project.success"), selectedObject.toString()),
                                    RelateObjectToProjectAction.class));
                            
                            wdwRelate.close();
                        }
                    } catch (ModuleActionException | MetadataObjectNotFoundException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), RelateObjectToProjectAction.class));
                    }
                }
            });
            
            lytContent.addSelectedObjectChangeListener(event -> {
                try {
                    targetProject = event.getSelectedObject();
                    wdwRelate.getBtnConfirm().setEnabled(targetProject != null && mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, targetProject.getClassName()));
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            });
            // Add content
            wdwRelate.add(lytContent);
            return wdwRelate;
        } else
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), PARAM_BUSINESS_OBJECT)));
    }

    @Override
    public AbstractAction getModuleAction() {
        return relateObjectToProjectAction;
    }
    
    @Override
    public String appliesTo() {
        return Constants.CLASS_INVENTORYOBJECT;
    }
    
    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }
}