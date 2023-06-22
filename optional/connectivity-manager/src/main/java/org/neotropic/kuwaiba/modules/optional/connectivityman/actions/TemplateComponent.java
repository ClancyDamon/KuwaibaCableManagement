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
package org.neotropic.kuwaiba.modules.optional.connectivityman.actions;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * A template as a component shows the template name and template class.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class TemplateComponent extends FlexLayout {
    
    public TemplateComponent(BusinessObjectLight parent, String containerName, List<TemplateObjectLight> templateElements, TemplateObjectLight template, MetadataEntityManager mem, TranslationService ts) {
        
        String templateClass = template.getClassName();
        try {
            templateClass = mem.getClass(template.getClassName()).toString();
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
        String text = String.format("%s > %s > ", parent.getName(), containerName);
        for (int i = 0; i < templateElements.size() - 1; i++)
            text += String.format("%s > ", templateElements.get(i).getName());
        
        Label lblParent = new Label(text);
        lblParent.setClassName("text-secondary");
        
        Label lblTemplate = new Label(String.format("%s [%s]", template.getName(), templateClass));
        add(lblTemplate);
        
        add(lblParent, lblTemplate);
        setSizeFull();
        setFlexDirection(FlexLayout.FlexDirection.COLUMN);
    }
}
