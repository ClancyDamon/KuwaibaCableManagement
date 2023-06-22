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

import com.neotropic.flow.component.paper.dialog.PaperDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Shows a list of all the Outside Plant Views available
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>} 
 */
public class DialogOspViews extends PaperDialog {
    private final Component positionTarget;
    private final ApplicationEntityManager aem;
    private final TranslationService ts;
    private final Consumer<ViewObject> consumerSelectedView;
    
    public DialogOspViews(Component positionTarget, ApplicationEntityManager aem, TranslationService ts, Consumer<ViewObject> consumerSelectedView) {
        Objects.requireNonNull(positionTarget);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(consumerSelectedView);
        this.positionTarget = positionTarget;
        this.aem = aem;
        this.ts = ts;
        this.consumerSelectedView = consumerSelectedView;
    }

    @Override
    public void open() {
        try {
            List<ViewObjectLight> views = aem.getOSPViews();
            if (!views.isEmpty()) {
                positionTarget(positionTarget);
                setNoOverlap(true);
                setHorizontalAlign(PaperDialog.HorizontalAlign.LEFT);
                setVerticalAlign(PaperDialog.VerticalAlign.TOP);
                setMargin(false);
                setWidth("20%");
                setHeight("30%");

                ListBox<ViewObjectLight> lstViewObjects = new ListBox();
                lstViewObjects.setRenderer(new ComponentRenderer<>(viewObject -> 
                    viewObject.getName() != null ? new Label(viewObject.getName()) : new Label())
                );
                lstViewObjects.addValueChangeListener(valueChangeEvent -> {
                    valueChangeEvent.unregisterListener();
                    if (valueChangeEvent.getValue() != null) {
                        try {
                            consumerSelectedView.accept(aem.getOSPView(valueChangeEvent.getValue().getId()));
                        } catch (ApplicationObjectNotFoundException ex) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                ex.getLocalizedMessage(), 
                                AbstractNotification.NotificationType.ERROR, ts
                            ).open();
                        }
                    }
                });
                lstViewObjects.setItems(views);
                Scroller lytViewObjects = new Scroller();
                lytViewObjects.setSizeFull();
                lytViewObjects.setContent(lstViewObjects);
                lytViewObjects.getStyle().set("margin", "0px"); //NOI18N
                lytViewObjects.getStyle().set("padding", "0px"); //NOI18N

                add(lytViewObjects);
                dialogConfirm(lytViewObjects);
                
                super.open();
            } else {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.information"), 
                    ts.getTranslatedString("module.ospman.tools.open-view"), 
                    AbstractNotification.NotificationType.INFO, 
                    ts
                ).open();
            }
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
    }
}
