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
package org.neotropic.kuwaiba.core.configuration.proxies.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.configuration.proxies.ProxyManagerModule;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.selectors.BusinessObjectSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of relate project action.
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class AssociateProjectVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private AssociateProjectAction relateProjectAction;
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
     * Close action command
     */
    private Command commandClose;
    /**
     * Window to relate project with a proxy
     */
    private ConfirmDialog wdwRelate;
    /**
     * Target project;
     */
    private BusinessObjectLight targetProject;

    public AssociateProjectVisualAction() {
        super(ProxyManagerModule.MODULE_ID);
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        if (parameters.containsKey("proxy")) {
            BusinessObjectLight selectedProxy = (BusinessObjectLight) parameters.get("proxy");
            commandClose = (Command) parameters.get("commandClose");

            BusinessObjectSelector lytContent = new BusinessObjectSelector(
                    ts.getTranslatedString("module.projects.actions.relate-object-to-project.placeholder"),
                    false, aem, bem, mem, ts, Constants.CLASS_GENERICPROJECT);

            wdwRelate = new ConfirmDialog(ts,
                    String.format(ts.getTranslatedString("module.projects.actions.relate-object-to-project.title"), selectedProxy.toString()),
                    ts.getTranslatedString("module.general.messages.relate"));
            wdwRelate.getBtnConfirm().setEnabled(false);
            wdwRelate.setDraggable(true);
            wdwRelate.setResizable(true);
            wdwRelate.setMinWidth("60%");

            wdwRelate.getBtnConfirm().addClickListener(e -> {
                if (targetProject != null) {
                    try {
                        if (mem.isSubclassOf(Constants.CLASS_GENERICPROJECT, targetProject.getClassName())) {
                            relateProjectAction.getCallback().execute(new ModuleActionParameterSet(
                                    new ModuleActionParameter<>("proxy", selectedProxy),
                                    new ModuleActionParameter<>("project", targetProject)));

                            //refresh related grid
                            commandClose.execute();
                            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                    String.format(ts.getTranslatedString("module.configman.proxies.actions.associate-project.success"), selectedProxy.getName()),
                                    AssociateProjectAction.class));
                            wdwRelate.close();
                        }
                    } catch (ModuleActionException | MetadataObjectNotFoundException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), ReleaseProjectAction.class));
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
            return new Dialog(new Label(String.format(ts.getTranslatedString("module.general.messages.parameter-not-found"), "proxy")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return relateProjectAction;
    }
}