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
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import com.neotropic.kuwaiba.modules.commercial.ospman.widgets.OutsidePlantView;
import com.vaadin.flow.component.html.Span;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Dialog to confirm the delete of an outside plant view
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowDeleteOspView extends ConfirmDialog {
    
    public WindowDeleteOspView(long ospViewId, TranslationService ts, ApplicationEntityManager aem, OutsidePlantView outsidePlantView) {
        super(ts, 
            ts.getTranslatedString("module.general.labels.confirmation"),                         
            new Span(ts.getTranslatedString("module.ospman.delete-view")), 
            ts.getTranslatedString("module.general.messages.ok"), () -> {
                try {
                    aem.deleteOSPView(ospViewId);
                    outsidePlantView.newOspView();
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.success"),
                        ts.getTranslatedString("module.ospman.view-deleted"), 
                        AbstractNotification.NotificationType.INFO, ts
                    ).open();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"),
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
            }
        );
        setDraggable(true);
    }
}
