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
package org.neotropic.kuwaiba.modules.optional.physcon.actions.mirrors;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Sets a vertical layout as a drop target.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class VerticalLayoutDropTarget  extends VerticalLayout {
    private final BusinessEntityManager bem;
    private final TranslationService ts;
    private DropTarget<VerticalLayoutDropTarget> dropTarget;
        
    public VerticalLayoutDropTarget(BusinessEntityManager bem, TranslationService ts) {
        this.bem = bem;
        this.ts = ts;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        dropTarget = DropTarget.create(this);
        dropTarget.setDropEffect(DropEffect.MOVE);

        dropTarget.addDropListener(dropEvent -> {
            Component dragSource = dropEvent.getDragSourceComponent().get();
            if (dragSource instanceof PortComponent && dropEvent.getDropEffect() == DropEffect.MOVE) {
                try {
                    PortComponent portComponent = (PortComponent) dragSource;
                    if (!bem.hasSpecialRelationship(portComponent.getPort().getClassName(), portComponent.getPort().getId(), "mirror", 1)) { //NOI18N
                        dropEvent.getComponent().add(portComponent);
                    } else {
                        new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.information"),
                                ts.getTranslatedString("module.physcon.mirror-man.notification.info.the-port-is-mirrored"),
                                AbstractNotification.NotificationType.INFO,
                                ts
                        ).open();
                    }
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
            }
        });
    }
}