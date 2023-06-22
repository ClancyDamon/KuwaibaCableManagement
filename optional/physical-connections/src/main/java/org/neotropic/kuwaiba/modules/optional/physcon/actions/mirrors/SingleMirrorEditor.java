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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Component to edit single mirrors.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class SingleMirrorEditor extends AbstractMirrorEditor {
    private final BusinessEntityManager bem;
    private final TranslationService ts;
    
    public SingleMirrorEditor(PortComponent source, PortComponent target, 
        VerticalLayout lytParent, Command cmdUpdateFreePorts, 
        BusinessEntityManager bem, TranslationService ts) {
        
        super(source, target, lytParent, cmdUpdateFreePorts);
        this.bem = bem;
        this.ts = ts;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        setWidthFull();
        ActionButton btnDelete = new ActionButton(
            VaadinIcon.CLOSE.create(), 
            ts.getTranslatedString("module.physcon.mirror-man.button.title.release-mirror")
        );
        btnDelete.addClickListener(clickEvent -> {
            try {
                BusinessObjectLight sourcePort = source.getPort();
                bem.releaseSpecialRelationship(sourcePort.getClassName(), sourcePort.getId(), "-1", "mirror"); //NOI18N

                lytParent.remove(this);
                cmdUpdateFreePorts.execute();

                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.information"), 
                    ts.getTranslatedString("module.physcon.mirror-man.notification.info.mirror-has-been-deleted"), 
                    AbstractNotification.NotificationType.INFO, 
                    ts
                ).open();
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        });
        HorizontalLayout lyt = new HorizontalLayout();
        lyt.add(source, target);

        add(lyt, btnDelete);
        expand(lyt);
    }
}