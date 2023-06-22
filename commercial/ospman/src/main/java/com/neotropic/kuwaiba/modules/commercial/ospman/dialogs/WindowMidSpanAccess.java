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

import org.neotropic.kuwaiba.modules.core.navigation.dialogs.WindowNewObject;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.physcon.actions.ManagePortMirroringVisualAction;
import org.neotropic.util.visual.button.ActionButton;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to manage mid-span access and splice of links
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowMidSpanAccess extends ConfirmDialog implements ActionCompletedListener {
    private Div divLocation;
    private BusinessObjectLight cableObject;
    private BusinessObjectLight deviceObject;
    
    private BusinessObjectLight selectedNode;
    private final BusinessObjectLight node;
    private final List<BusinessObjectLight> nodes;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final PhysicalConnectionsService physicalConnectionsService;
    private final NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    private final NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction;
    private final NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction;
    private final ManagePortMirroringVisualAction managePortMirroringVisualAction;
    
    private boolean spliceFiber = true;
    private boolean cutFiber = false;
    private boolean showLeftoverFiber = false;
    private OspLocationView ospLocationView;
    private final List<Button> toolButtons = new ArrayList();
    private Button btnSpliceFiber;
    private Button btnCutFiber;
    private Button btnShowLeftoverFiber;
    private Button btnExchange;
    private Button btnAccordionMenu;
    private boolean exchange = false;
    private boolean accordionMenu = true;
    
    public WindowMidSpanAccess(BusinessObjectLight node, List<BusinessObjectLight> nodes,
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts, 
        NewBusinessObjectVisualAction newBusinessObjectVisualAction, 
        NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction,
        NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction,
        ManagePortMirroringVisualAction managePortMirroringVisualAction, 
        PhysicalConnectionsService physicalConnectionsService) {
        
        Objects.requireNonNull(node);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(newBusinessObjectVisualAction);
        Objects.requireNonNull(managePortMirroringVisualAction);
        Objects.requireNonNull(physicalConnectionsService);
        
        this.selectedNode = node;
        this.node = node;
        this.nodes = nodes;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.physicalConnectionsService = physicalConnectionsService;
        this.managePortMirroringVisualAction = managePortMirroringVisualAction;
        this.newBusinessObjectVisualAction = newBusinessObjectVisualAction;
        this.newBusinessObjectFromTemplateVisualAction = newBusinessObjectFromTemplateVisualAction;
        this.newMultipleBusinessObjectsVisualAction = newMultipleBusinessObjectsVisualAction;
        
        setCloseOnOutsideClick(false);
        setMinWidth("90%");
        setMinHeight("90%");
        setContentSizeFull();
        setDraggable(true);
    }

    @Override
    public void open() {
        try {
            if (bem.hasSpecialAttribute(selectedNode.getClassName(), selectedNode.getId(), OspConstants.SPECIAL_ATTR_ENDPOINT_A) ||
                bem.hasSpecialAttribute(selectedNode.getClassName(), selectedNode.getId(), OspConstants.SPECIAL_ATTR_ENDPOINT_B)) {
                
                FlexLayout lytSelector = new FlexLayout();
                lytSelector.setWidth("300px");
                lytSelector.setHeightFull();
                lytSelector.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
                
                Button btnSearchCable = new Button(ts.getTranslatedString("module.ospman.mid-span-access.select-cable"));
                btnSearchCable.setWidthFull();
                Button btnSearchDevice = new Button(ts.getTranslatedString("module.ospman.mid-span-access.select-device"));
                btnSearchDevice.setWidthFull();
                
                if (nodes != null && !nodes.isEmpty()) {
                    ComboBox<BusinessObjectLight> cmbNode = new ComboBox();
                    cmbNode.setLabel(ts.getTranslatedString("module.ospman.mid-span-access.location"));
                    cmbNode.setPlaceholder(ts.getTranslatedString("module.ospman.mid-span-access.select-node"));
                    cmbNode.setItemLabelGenerator(BusinessObjectLight::getName);
                    cmbNode.setClearButtonVisible(true);
                    cmbNode.setItems(nodes);
                    cmbNode.setValue(selectedNode);
                    
                    cmbNode.addValueChangeListener(valueChangeEvent -> {
                        selectedNode = valueChangeEvent.getValue();
                        setHeader(String.format(
                            ts.getTranslatedString("module.ospman.mid-span-access.title"), 
                            selectedNode != null ? selectedNode.getName() : ""
                        ));
                        cableObject = null;
                        deviceObject = null;
                        btnSearchCable.setText(ts.getTranslatedString("module.ospman.mid-span-access.select-cable"));
                        btnSearchDevice.setText(ts.getTranslatedString("module.ospman.mid-span-access.select-device"));
                        updateOspLocationView();
                    });
                    lytSelector.add(cmbNode);
                } else {
                    Label lblLocation = new Label(String.format("%s *", ts.getTranslatedString("module.ospman.mid-span-access.location")));
                    lblLocation.setClassName("text-secondary"); //NOI18N
                    Label lblSearchLocation = new Label(selectedNode.getName());
                    lblSearchLocation.setWidthFull();
                    FlexLayout lytLocation = new FlexLayout(lblLocation, lblSearchLocation);
                    lytLocation.setWidthFull();
                    lytLocation.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
                    HorizontalLayout lytSearchLocation = new HorizontalLayout(lytLocation);
                    lytSearchLocation.setWidthFull();
                    
                    lytSelector.add(lytSearchLocation);
                }
                Label lblCable = new Label(String.format("%s *", ts.getTranslatedString("module.ospman.mid-span-access.cable")));
                lblCable.setClassName("text-secondary"); //NOI18N
                FlexLayout lytCable = new FlexLayout(lblCable, btnSearchCable);
                lytCable.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
                lytCable.setWidthFull();
                HorizontalLayout lytSearchCable = new HorizontalLayout(lytCable);
                lytSearchCable.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
                lytSearchCable.setWidthFull();
                lytSearchCable.addClickListener(clickEvent -> {
                    if (selectedNode != null) {
                        new WindowContainerSelector(selectedNode, bem, mem, ts, selectedContainer -> {
                            if (selectedContainer != null) {
                                cableObject = selectedContainer;
                                if (cableObject != null)
                                    btnSearchCable.setText(cableObject.getName());
                                else
                                    btnSearchCable.setText(ts.getTranslatedString("module.ospman.mid-span-access.select-cable"));
                                updateOspLocationView();
                            }
                        }).open();
                    }
                });
                
                Label lblDevice = new Label(String.format("%s *", ts.getTranslatedString("module.ospman.mid-span-access.device")));
                lblDevice.setClassName("text-secondary"); //NOI18N
                FlexLayout lytDevice = new FlexLayout(lblDevice, btnSearchDevice);
                lytDevice.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
                lytDevice.setWidthFull();
                
                ActionButton btnPlus = new ActionButton(
                    VaadinIcon.PLUS_SQUARE_O.create(), 
                    ts.getTranslatedString("module.ospman.mid-span-access.button.new-device")
                );
                HorizontalLayout lytSearchDevice = new HorizontalLayout(lytDevice, btnPlus);
                lytSearchDevice.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
                lytSearchDevice.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
                lytSearchDevice.setAlignSelf(FlexComponent.Alignment.CENTER, btnPlus);
                lytSearchDevice.setWidthFull();
                lytDevice.addClickListener(clickEvent -> {
                    if (selectedNode != null) {
                        DeviceSelectorWindow wdwDeviceSelector = new DeviceSelectorWindow(selectedNode, mem, ts, device -> {
                            if (device != null) {
                                deviceObject = device;
                                btnSearchDevice.setText(device.getName());
                                updateOspLocationView();
                            }
                        }, bem);
                        wdwDeviceSelector.open();
                    }
                });
                btnPlus.addClickListener(clickEvent -> 
                    new WindowNewObject(selectedNode, 
                        newBusinessObjectVisualAction, 
                        newBusinessObjectFromTemplateVisualAction, 
                        newMultipleBusinessObjectsVisualAction,
                        ts
                    ).open()
                );
                Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), event -> close());
                
                VerticalLayout lytRow4 = new VerticalLayout();
                lytRow4.setSizeFull();
                
                lytSelector.add(lytSearchCable, lytSearchDevice, lytRow4);
                
                divLocation = new Div();
                divLocation.setWidth("98%");
                divLocation.setHeight("95%");
                divLocation.getStyle().set("border-style", "solid"); //NOI18N
                divLocation.getStyle().set("border-width", "1px"); //NOI18N
                divLocation.getStyle().set("border-color", "var(--paper-grey-900)"); //NOI18N
                
                ComponentEventListener<ClickEvent<Button>> clickEventListener = clickEvent -> selectButtons();
                
                btnAccordionMenu = new Button(VaadinIcon.CHEVRON_LEFT_SMALL.create(), clickEvent -> {
                    accordionMenu = !accordionMenu;
                    lytSelector.setVisible(accordionMenu);
                    if (accordionMenu)
                        clickEvent.getSource().setIcon(VaadinIcon.CHEVRON_LEFT_SMALL.create());
                    else
                        clickEvent.getSource().setIcon(VaadinIcon.CHEVRON_RIGHT_SMALL.create());
                });
                btnAccordionMenu.getElement().setAttribute("title", 
                    ts.getTranslatedString("module.ospman.mid-span-access.device-selector")
                );
                btnSpliceFiber = new Button(VaadinIcon.PLUG.create(), clickEvent -> {
                    spliceFiber = true;
                    cutFiber = false;
                    if (ospLocationView != null) {
                        ospLocationView.setSpliceFiber(spliceFiber);
                        ospLocationView.setCutFiber(cutFiber);
                    }
                });
                btnSpliceFiber.getElement().setAttribute("title", 
                    ts.getTranslatedString("module.ospman.location.tool.splice-fiber.title") //NOI18N
                );
                
                btnCutFiber = new Button(VaadinIcon.SCISSORS.create(), clickEvent -> {
                    cutFiber = true;
                    spliceFiber = false;
                    if (ospLocationView != null) {
                        ospLocationView.setCutFiber(cutFiber);
                        ospLocationView.setSpliceFiber(spliceFiber);
                    }
                    toolButtons.forEach(button -> button.removeClassName("nav-button")); //NOI18N
                    clickEvent.getSource().addClassName("nav-button"); //NOI18N
                });
                btnCutFiber.getElement().setAttribute("title", 
                    ts.getTranslatedString("module.ospman.location.tool.cut-fiber.title") //NOI18N
                );
                
                btnShowLeftoverFiber = new Button(VaadinIcon.EYE.create(), clickEvent -> {
                    showLeftoverFiber = !showLeftoverFiber;
                    if (ospLocationView != null)
                        ospLocationView.setShowLeftoverFiber(showLeftoverFiber);
                    updateOspLocationView();
                });
                btnShowLeftoverFiber.getElement().setAttribute("title", 
                    ts.getTranslatedString("module.ospman.location.tool.show-hide-leftover-fiber") //NOI18N
                );
                
                btnExchange = new Button(VaadinIcon.EXCHANGE.create(), clickEvent -> {
                    exchange = !exchange;
                    updateOspLocationView();
                });
                btnExchange.getElement().setAttribute("title", 
                    ts.getTranslatedString("module.ospman.location.tool.change-device-position")); //NOI18N
                
                Button btnRefresh = new Button(VaadinIcon.REFRESH.create(), clickEvent -> updateOspLocationView());
                btnRefresh.getElement().setAttribute("title", 
                    ts.getTranslatedString("module.ospman.location.tool.refresh-location-view.title") //NOI18N
                );
                
                toolButtons.addAll(Arrays.asList(btnAccordionMenu, btnSpliceFiber, btnCutFiber, btnShowLeftoverFiber, btnExchange, btnRefresh));
                toolButtons.forEach(button -> {
                    button.getStyle().set("margin", "0px 1px 5px 0px"); //NOI18N
                    button.setEnabled(false);
                    button.setClassName("icon-button"); //NOI18N
                    button.addClickListener(clickEventListener);                    
                });                
                FlexLayout lytTools = new FlexLayout(toolButtons.toArray(new Component[0]));
                
                FlexLayout lytContent = new FlexLayout(lytTools, divLocation);
                lytContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
                lytContent.setSizeFull();
                
                HorizontalLayout lytMain = new HorizontalLayout(lytSelector, lytContent);
                lytMain.setSizeFull();
                
                setHeader(String.format(ts.getTranslatedString("module.ospman.mid-span-access.title"), selectedNode.getName()));
                setContent(lytMain);
                setFooter(btnClose);
                this.newBusinessObjectVisualAction.registerActionCompletedLister(this);
                this.managePortMirroringVisualAction.registerActionCompletedLister(this);
                super.open();
            } else {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"),
                    ts.getTranslatedString("module.ospman.mid-span-access.there-are-no-cables"), 
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"),
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
    }

    @Override
    public void close() {
        this.newBusinessObjectVisualAction.unregisterListener(this);
        this.managePortMirroringVisualAction.unregisterListener(this);
        super.close();
    }
    
    public void selectButtons() {
        toolButtons.forEach(button -> button.removeClassName("nav-button")); //NOI18N
        if (spliceFiber)
            btnSpliceFiber.addClassName("nav-button"); //NOI18N
        if (cutFiber)
            btnCutFiber.addClassName("nav-button"); //NOI18N
        if (showLeftoverFiber)
            btnShowLeftoverFiber.addClassName("nav-button"); //NOI18N
        if (exchange)
            btnExchange.addClassName("nav-button"); //NOI18N
        if (accordionMenu)
            btnAccordionMenu.addClassName("nav-button"); //NOI18N
    }
    
    private void updateOspLocationView() {
        divLocation.removeAll();
        
        if (cableObject != null && deviceObject != null && selectedNode != null) {
            try {
                toolButtons.forEach(button -> button.setEnabled(true));
                selectButtons();
                ospLocationView = new OspLocationView(
                    selectedNode, cableObject, deviceObject, 
                    aem, bem, mem, ts, 
                    managePortMirroringVisualAction, physicalConnectionsService, 
                    showLeftoverFiber, exchange
                );
                ospLocationView.setSpliceFiber(spliceFiber);
                ospLocationView.setCutFiber(cutFiber);
                ospLocationView.setShowLeftoverFiber(showLeftoverFiber);
                divLocation.add(ospLocationView);
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"),
                    ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
        }
    }
        
    @Override
    public void actionCompleted(ActionCompletedEvent event) {
        if (ManagePortMirroringVisualAction.class.equals(event.getSource())) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.information"),
                ts.getTranslatedString("module.ospman.mid-span-access.location.refreshing"),
                AbstractNotification.NotificationType.INFO,
                ts
            ).open();
            updateOspLocationView();
        } else if (NewBusinessObjectVisualAction.class.equals(event.getSource())) {
            if (event.getStatus() == ActionCompletedEvent.STATUS_SUCCESS)
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), event.getMessage(), 
                    AbstractNotification.NotificationType.INFO, ts).open();
            else
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), event.getMessage(), 
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
}