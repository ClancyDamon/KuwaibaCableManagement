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
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * New link as cell component.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class NewLinkCellComponent extends FlexLayout {
    
    public NewLinkCellComponent(String newLinkName, ClassMetadataLight newLinkClass, 
        BusinessObjectLight commonParent, MetadataEntityManager mem, TranslationService ts) {
        
        Objects.requireNonNull(newLinkName);
        Objects.requireNonNull(newLinkClass);
        Objects.requireNonNull(commonParent);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        
        String newLinkClassText = newLinkClass.toString();
        try {
            newLinkClassText = mem.getClass(newLinkClass.getName()).toString();
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
        Label lblParent = new Label(String.format("%s >", commonParent.getName()));
        lblParent.setClassName("text-secondary"); //NOI18N      
        
        Label lblNewLink = new Label(String.format("%s [%s]", newLinkName, newLinkClassText));
        
        add(lblParent, lblNewLink);
        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
    }
}