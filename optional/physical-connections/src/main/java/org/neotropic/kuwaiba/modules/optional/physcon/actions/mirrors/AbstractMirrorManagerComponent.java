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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.server.Command;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.button.ActionButton;

/**
 * UI to manage single mirror between ports.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @param <S> The type of free ports component.
 * @param <U> The type of assigned ports component.
 * @param <V> The type of window mirror free ports.
 */
public abstract class AbstractMirrorManagerComponent<
    S extends AbstractFreePortsComponent, 
    U extends AbstractAssignedPortsComponent, 
    V extends AbstractWindowMirrorFreePorts> extends HorizontalLayout {
    
    protected final BusinessEntityManager bem;
    protected final TranslationService ts;
    protected final BusinessObjectLight businessObject;
    
    protected VerticalLayoutDropTarget lytUnassignedSourcePorts;
    protected VerticalLayoutDropTarget lytUnassignedTargetPorts;
    protected Command cmdUpdateFreePorts;
    protected Command cmdUpdateMirrorManager;
    
    public AbstractMirrorManagerComponent(BusinessObjectLight businessObject, BusinessEntityManager bem, TranslationService ts) {
        Objects.requireNonNull(businessObject);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(ts);
        
        this.bem = bem;
        this.ts = ts;
        this.businessObject = businessObject;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        setSizeFull();
        // Unassigned Source Ports
        Label lblUnassignedSourcePorts = new Label(ts.getTranslatedString("module.physcon.mirror-man.lbl.text.unassigned-source-ports"));

        lytUnassignedSourcePorts = new VerticalLayoutDropTarget(bem, ts);
        lytUnassignedSourcePorts.setSizeFull();
        
        Scroller scrollerUnassignedSourcePorts = new Scroller();
        scrollerUnassignedSourcePorts.setSizeFull();
        scrollerUnassignedSourcePorts.setContent(lytUnassignedSourcePorts);

        VerticalLayout lytLeftTop = new VerticalLayout(
            lblUnassignedSourcePorts, scrollerUnassignedSourcePorts
        );
        lytLeftTop.setSpacing(false);
        lytLeftTop.setWidth("50%");
        lytLeftTop.setHeightFull();
        // Unassigned Target Ports
        Label lblUnassignedTargetPorts = new Label(ts.getTranslatedString("module.physcon.mirror-man.lbl.text.unassigned-target-ports"));

        lytUnassignedTargetPorts = new VerticalLayoutDropTarget(bem, ts);
        lytUnassignedTargetPorts.setSizeFull();
        
        Scroller scrollerUnassignedTargetPorts = new Scroller();
        scrollerUnassignedTargetPorts.setSizeFull();
        scrollerUnassignedTargetPorts.setContent(lytUnassignedTargetPorts);

        VerticalLayout lytRightTop = new VerticalLayout(
            lblUnassignedTargetPorts, scrollerUnassignedTargetPorts
        );
        lytRightTop.setSpacing(false);
        lytRightTop.setWidth("50%");
        lytRightTop.setHeightFull();
        // Layout Unassigned Ports
        HorizontalLayout lytUnassignedPortsContent = new HorizontalLayout(
            lytLeftTop, 
            lytRightTop
        );
        lytUnassignedPortsContent.setSizeFull();
        
        Label lblDndUnassignedPorts = new Label(ts.getTranslatedString("module.physcon.mirror-man.lbl.text.dnd-port"));
        lblDndUnassignedPorts.setClassName("text-secondary"); //NOI18N
        
        VerticalLayout lytUnassignedPorts = new VerticalLayout(lblDndUnassignedPorts, lytUnassignedPortsContent);        
        lytUnassignedPorts.setSpacing(false);
        lytUnassignedPorts.setWidthFull();
        lytUnassignedPorts.setHeight("95%");
        // Assigned Ports
        Scroller scrollerAssignedPorts = new Scroller();
        scrollerAssignedPorts.setSizeFull();
        
        VerticalLayout lytAssignedPorts = new VerticalLayout(scrollerAssignedPorts);
        lytAssignedPorts.setSpacing(false);
        lytAssignedPorts.setWidthFull();
        lytAssignedPorts.setHeight("95%");
        // Layout Mirror
        Tab tabUnassignedPorts = new Tab(ts.getTranslatedString("module.physcon.mirror-man.tab.mirror-ports"));
        Tab tabAssignedPorts = new Tab(ts.getTranslatedString("module.physcon.mirror-man.tab.existing-mirrors"));
        Tabs tabsMirroring = new Tabs(tabUnassignedPorts, tabAssignedPorts);
        tabsMirroring.setWidthFull();
        
        Div divMirroring = new Div();
        divMirroring.setSizeFull();
        divMirroring.add(lytUnassignedPorts);
        divMirroring.add(lytAssignedPorts);
        lytAssignedPorts.setVisible(false);
        
        tabsMirroring.addSelectedChangeListener(selectedChangeEvent -> {
            if (tabUnassignedPorts.equals(selectedChangeEvent.getSelectedTab())) {
                lytUnassignedPorts.setVisible(true);
                lytAssignedPorts.setVisible(false);
            } else if (tabAssignedPorts.equals(selectedChangeEvent.getSelectedTab())) {
                lytUnassignedPorts.setVisible(false);
                lytAssignedPorts.setVisible(true);
            }
        });
        
        VerticalLayout lytMirroring = new VerticalLayout(tabsMirroring, divMirroring);
        lytMirroring.setWidth("70%");
        lytMirroring.setHeight("95%");
        
        Label lblFreePorts = new Label(ts.getTranslatedString("module.physcon.mirror-man.lbl.text.free-ports"));
        lblFreePorts.setWidthFull();
        
        Scroller scrollerFreePorts = new Scroller();
        scrollerFreePorts.setSizeFull();

        ActionButton btnMirrorFreePorts = new ActionButton(
            ts.getTranslatedString("module.physcon.mirror-man.button.text.mirror-free-ports"), 
            ts.getTranslatedString("module.physcon.mirror-man.button.title.mirror-free-ports")
        );
        btnMirrorFreePorts.setWidthFull();
        
        VerticalLayout lytFreePorts = new VerticalLayout(lblFreePorts, scrollerFreePorts, btnMirrorFreePorts);
        lytFreePorts.setWidth("30%");
        lytFreePorts.setHeight("95%");
        
        cmdUpdateFreePorts = () -> scrollerFreePorts.setContent(getFreePortsComponent((U) scrollerAssignedPorts.getContent()));
        Command cmdUpdateAssignedPorts = () -> scrollerAssignedPorts.setContent(getAssignedPortsComponent());
        cmdUpdateAssignedPorts.execute();
        cmdUpdateFreePorts.execute();
        
        cmdUpdateMirrorManager = () -> {
            cmdUpdateAssignedPorts.execute();
            cmdUpdateFreePorts.execute();
        };
        btnMirrorFreePorts.addClickListener(clickEvent -> getMirrorFreePortsAction().open());
        add(lytFreePorts, lytMirroring);
    }
    /**
     * Gets the free ports component.
     * @param assignedPortsComponent The assigned ports component.
     * @return The free ports component.
     */
    public abstract S getFreePortsComponent(U assignedPortsComponent);
    /**
     * Gets the assigned ports component.
     * @return The assigned ports component.
     */
    public abstract U getAssignedPortsComponent();
    /**
     * Gets the mirror free ports action.
     * @return The mirror free ports action.
     */
    public abstract V getMirrorFreePortsAction();
}
