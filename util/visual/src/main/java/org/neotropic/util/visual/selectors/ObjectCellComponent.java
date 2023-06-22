/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.util.visual.selectors;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * An object as a cell component shows the name and class of the object parent and the object.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ObjectCellComponent extends FlexLayout {
    
    public ObjectCellComponent(BusinessObjectLight object, BusinessEntityManager bem, TranslationService ts, List<String> parentClassNames) {
        Objects.requireNonNull(object);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(ts);
        
        try {
            List<BusinessObjectLight> parents = bem.getParentsUntilFirstOfClass(object.getClassName(), object.getId(), parentClassNames.toArray(new String[0]));
            Collections.reverse(parents);
            if (!parents.isEmpty()) {
                String text = "";
                for (int i = 0; i < parents.size(); i++)
                    text += parents.get(i).getName() + " > ";
                Label lblParents = new Label(text);
                lblParents.setClassName("text-secondary"); //NOI18N
                add(lblParents);
            }
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
        FormattedObjectDisplayNameSpan spanObjectName = new FormattedObjectDisplayNameSpan(object, false, false, true, false);
        add(spanObjectName);
        
        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
    }
}
