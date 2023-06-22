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
package org.neotropic.kuwaiba.modules.optional.physcon.actions;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.mirrors.MultipleMirrorManagerComponent;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.mirrors.SingleMirrorManagerComponent;

/**
 * Window to manage port mirroring
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowManagePortMirroring0 extends WindowManagePortMirroring {
    private final BusinessObjectLight businessObject;
    private final BusinessEntityManager bem;
    private final TranslationService ts;
    
    public WindowManagePortMirroring0(BusinessObjectLight businessObject, BusinessEntityManager bem, TranslationService ts) {
        Objects.requireNonNull(businessObject);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(ts);
        
        this.businessObject = businessObject;
        this.bem = bem;
        this.ts = ts;
        
        Button btnClose = new Button(
            ts.getTranslatedString("module.general.messages.close"), 
            clickEvent -> close()
        );
        btnClose.setSizeFull();
        setHeader(String.format(ts.getTranslatedString("module.physcon.windows.manage-port-mirroring.business-object.title"), businessObject.getName()));
        setFooter(btnClose);
        
        setContentSizeFull();
        setWidth("90%");
        setHeight("85%");
        setDraggable(true);
        setResizable(true);
    }
    
    @Override
    public void open() {
        Tab tabSingleMirror = new Tab(ts.getTranslatedString("module.physcon.mirror-man.tab.single-mirror"));
        Tab tabMultipleMirror = new Tab(ts.getTranslatedString("module.physcon.mirror-man.tab.multiple-mirror"));
        Tabs tabsMirrorType = new Tabs(tabSingleMirror, tabMultipleMirror);
        tabsMirrorType.setWidthFull();
        
        Div divMirrorManager = new Div();
        divMirrorManager.setSizeFull();
        divMirrorManager.add(new SingleMirrorManagerComponent(businessObject, bem, ts));
        
        tabsMirrorType.addSelectedChangeListener(selectedChangeEvent -> {
            divMirrorManager.removeAll();
            if (tabSingleMirror.equals(selectedChangeEvent.getSelectedTab()))
                divMirrorManager.add(new SingleMirrorManagerComponent(businessObject, bem, ts));
            else if (tabMultipleMirror.equals(selectedChangeEvent.getSelectedTab()))
                divMirrorManager.add(new MultipleMirrorManagerComponent(businessObject, bem, ts));
        });
        VerticalLayout lytContent = new VerticalLayout(tabsMirrorType, divMirrorManager);
        lytContent.setSizeFull();
        lytContent.setSpacing(false);
        lytContent.setPadding(false);
        lytContent.setMargin(false);
        
        setContent(lytContent);
        super.open();
    }
}
