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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.internal.Pair;
import java.util.List;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;

/**
 * Tools available on draw containers.
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowDrawContainerTools extends ConfirmDialog {

    public WindowDrawContainerTools(List controlPoints, Runnable deleteControlPoint, Runnable cancel, TranslationService ts) {
        setDraggable(true);
        setHeader(ts.getTranslatedString("module.ospman.tools.container.draw"));

        Pair<Integer, String> toolDeleteLast = new Pair(1, ts.getTranslatedString("module.ospman.tools.container.control-point.delete"));
        Pair<Integer, String> toolCancelDraw = new Pair(2, ts.getTranslatedString("module.ospman.tools.container.draw.cancel"));

        ListBox<Pair> lstTools = new ListBox();
        if (controlPoints.size() - 1 > 1) {
            lstTools.setItems(toolDeleteLast, toolCancelDraw);
        } else {
            lstTools.setItems(toolCancelDraw);
        }
        lstTools.setRenderer(new ComponentRenderer<>(tool -> new Label(tool.getSecond().toString())));
        lstTools.addValueChangeListener(valueChangeEvent -> {
            Pair<Integer, String> item = valueChangeEvent.getValue();
            if (item != null) {
                switch (item.getFirst()) {
                    case 1:
                        if (controlPoints.size() - 1 > 1) {
                            controlPoints.remove((controlPoints.size() - 1) - 1);

                            if (controlPoints.size() - 1 == 1) {
                                lstTools.setItems(toolCancelDraw);
                            }
                            deleteControlPoint.run();
                        }
                        lstTools.clear();
                        break;
                    case 2:
                        cancel.run();
                        close();
                        break;
                }
            }
        });
        Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), clickEvent -> close());
        btnClose.setSizeFull();

        setContentSizeFull();
        setContent(lstTools);
        setFooter(btnClose);
        setModal(true);
    }
}
