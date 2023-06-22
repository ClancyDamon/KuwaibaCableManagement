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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ProcessInstanceEditorWindow extends ConfirmDialog {
    private final ProcessInstance processInstance;
    private final ApplicationEntityManager aem;
    private final TranslationService ts;
    private final Command cmdProcessInstanceUpdated;
    
    public ProcessInstanceEditorWindow(ProcessInstance processInstance, ApplicationEntityManager aem, TranslationService ts, Command cmdProcessInstanceUpdated) {
        this.processInstance = processInstance;
        this.aem = aem;
        this.ts = ts;
        this.cmdProcessInstanceUpdated = cmdProcessInstanceUpdated;
    }

    @Override
    public void open() {
        FormLayout lytContent = new FormLayout();
        TextField txtName = new TextField(ts.getTranslatedString("module.processman.process-instance.update.name"));
        TextField txtDescription = new TextField(ts.getTranslatedString("module.processman.process-instance.update.description"));
        if (processInstance.getName() != null)
            txtName.setValue(processInstance.getName());
        if (processInstance.getDescription() != null)
            txtDescription.setValue(processInstance.getDescription());

        lytContent.add(txtName, txtDescription);
        
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), clickEvent -> close());
        Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"), clickEvent -> {
            aem.updateProcessInstance(processInstance.getId(), txtName.getValue(), txtDescription.getValue());
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.information"), 
                ts.getTranslatedString("module.processman.process-instance.update.updated"), 
                AbstractNotification.NotificationType.INFO, 
                ts
            ).open();
            close();
            cmdProcessInstanceUpdated.execute();
        });
        HorizontalLayout lytFooter = new HorizontalLayout(btnCancel, btnOk);
        lytFooter.setFlexGrow(1, btnCancel, btnOk);
        
        setHeader(ts.getTranslatedString("module.processman.process-instance.update.update-process-instance"));
        setContent(lytContent);
        setFooter(lytFooter);
        super.open();
    }
    
}
