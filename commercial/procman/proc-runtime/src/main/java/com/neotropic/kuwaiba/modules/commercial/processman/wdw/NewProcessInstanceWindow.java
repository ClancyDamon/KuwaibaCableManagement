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
package com.neotropic.kuwaiba.modules.commercial.processman.wdw;

import com.neotropic.kuwaiba.modules.commercial.processman.ProcessDiagramUi;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouteConfiguration;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to create a process instance
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class NewProcessInstanceWindow extends ConfirmDialog {
    private final ProcessDefinition processDefinition;
    private final ApplicationEntityManager aem;
    private final TranslationService ts;
    
    public NewProcessInstanceWindow(ProcessDefinition processDefinition, ApplicationEntityManager aem, TranslationService ts) {
        this.processDefinition = processDefinition;
        this.aem = aem;
        this.ts = ts;
    }

    @Override
    public void open() {
        FormLayout lytContent = new FormLayout();
        TextField txtName = new TextField();
        TextField txtDescription = new TextField();
        lytContent.addFormItem(txtName, 
            ts.getTranslatedString("module.processman.new-process-instance.window.content.name")
        );
        lytContent.addFormItem(txtDescription, 
            ts.getTranslatedString("module.processman.new-process-instance.window.content.description")
        );
        Button btnCancel = new Button(
            ts.getTranslatedString("module.general.messages.cancel"),
            clickEvent -> close()
        );
        Button btnOk = new Button(
            ts.getTranslatedString("module.general.messages.ok"), 
            clickEvent -> {
                try {
                    String processInstanceId = aem.createProcessInstance(
                        processDefinition.getId(), 
                        txtName.getValue(),
                        txtDescription.getValue()
                    );
                    ProcessInstance processInstance = aem.getProcessInstance(processInstanceId);
                    
                    getUI().ifPresent(ui -> {
                        ui.getPage().open(
                            String.format("%s?%s=%s", RouteConfiguration.forSessionScope().getUrl(ProcessDiagramUi.class), Constants.PROPERTY_ID, processInstance.getId()), 
                            "_blank" //NOI18N
                        );
                    });
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ts.getTranslatedString("module.processman.notification.text.user-not-authorized"), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
                close();
            }
        );
        HorizontalLayout lytFooter = new HorizontalLayout(btnCancel, btnOk);
        lytFooter.setSizeFull();
        lytFooter.setFlexGrow(1, btnCancel, btnOk);
        setHeader(ts.getTranslatedString("module.processman.window.header.new-process-instance"));
        setContent(lytContent);
        setFooter(lytFooter);
        super.open();
    }
}
