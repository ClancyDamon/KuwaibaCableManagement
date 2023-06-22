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

import org.neotropic.util.visual.window.ObjectSelectorWindow;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.dialogs.WindowNewObject;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to manage port mirroring
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowManagePortMirroring extends ConfirmDialog {
    private enum MirrorType {
        SINGLE_MIRROR {
            @Override
            public String toString() {
                return "mirror"; //NOI18N
            }
        },
        MULTIPLE_MIRROR {
            @Override
            public String toString() {
                return "mirrorMultiple"; //NOI18N
            }
        }
    }
    /**
     * Reference to the business entity manager.
     */
    private BusinessEntityManager bem;
    /**
     * Reference to the metadata entity manager.
     */
    private MetadataEntityManager mem;
    /**
     * Reference to the translation service.
     */
    private TranslationService ts;
    
    private BusinessObjectLight initPortParent;
    private BusinessObjectLight portParent;
    private BusinessObjectLight port;
    private BusinessObjectLight otherPortParent;
    private BusinessObjectLight otherPort;
    private NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    private NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction;
    private NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction;
    private ManagePortMirroringVisualAction managePortMirroringVisualAction;
    
    private ActionCompletedListener actionCompletedListener;
    
    private boolean open = true;
    private String errorMessage;
    
    private Div divPortSelector;
    private Div divMirrorManager;
    /**
     * Field values.
     */
    private MirrorType mirrorTypeValue = MirrorType.SINGLE_MIRROR;
    private String searchPortValue;
    /**
     * Selected ports
     */
    private BusinessObjectLight selectedPort;
    private BusinessObjectLight selectedOtherPort;
    
    private boolean clickOtherButton = false;
    private String filterPort = null;
    private MirrorType filterMirrorType = null;
    private String filterOtherPort = null;
    
    public WindowManagePortMirroring() {}
        
    public WindowManagePortMirroring(BusinessObjectLight businessObject, 
        BusinessEntityManager bem, MetadataEntityManager mem, 
        TranslationService ts, 
        NewBusinessObjectVisualAction newBusinessObjectVisualAction, 
        NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction,
        NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction,
        ManagePortMirroringVisualAction managePortMirroringVisualAction) {
        
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(newBusinessObjectVisualAction);
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.newBusinessObjectVisualAction = newBusinessObjectVisualAction;
        this.newBusinessObjectFromTemplateVisualAction = newBusinessObjectFromTemplateVisualAction;
        this.newMultipleBusinessObjectsVisualAction = newMultipleBusinessObjectsVisualAction;
        this.managePortMirroringVisualAction = managePortMirroringVisualAction;
        
        try {
            if (businessObject != null) {
                if (mem.isSubclassOf(Constants.CLASS_GENERICPORT, businessObject.getClassName())) {
                    port = businessObject;
                    portParent = bem.getParent(port.getClassName(), port.getId());
                    
                    otherPort = port;
                    otherPortParent = portParent;
                    
                } else {
                    portParent = businessObject;
                    otherPortParent = portParent;
                    initPortParent = portParent;
                }
                setHeader(String.format(ts.getTranslatedString("module.physcon.windows.manage-port-mirroring.business-object.title"), businessObject.getName()));
            }
            else
                setHeader(ts.getTranslatedString("module.physcon.windows.manage-port-mirroring.title"));
            
            HorizontalLayout lytContent = new HorizontalLayout();
            lytContent.setMargin(false);
            lytContent.setPadding(false);
            lytContent.setSizeFull();
            
            divPortSelector = new Div();
            divPortSelector.setWidth("30%");
            divPortSelector.setHeightFull();           
            
            divMirrorManager = new Div();
            divMirrorManager.setWidth("70%");
            divMirrorManager.setHeightFull();
            
            updateMirrors();
            
            lytContent.add(divPortSelector, divMirrorManager);
            
            setContent(lytContent);
            Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), clickEvent -> close());
            btnClose.setSizeFull();
            setFooter(btnClose);
            
            setContentSizeFull();
            setWidth("90%");
            setHeight("85%");
            setDraggable(true);
            
        } catch (InventoryException ex) {
            errorMessage = ex.getLocalizedMessage();
            
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
            open = false;
        }
    }
    
    @Override
    public void open() {
        if (open)
            super.open();
        else {
            if (managePortMirroringVisualAction != null) {
                managePortMirroringVisualAction.fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                        ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR, 
                        errorMessage,
                        ManagePortMirroringVisualAction.class
                ));
            }
        }
    }

    @Override
    public void close() {
        super.close();
        if (managePortMirroringVisualAction != null) {
            if (errorMessage != null) {
                managePortMirroringVisualAction.fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                    ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR, 
                    errorMessage, 
                    ManagePortMirroringVisualAction.class)
                );
            } else {
                managePortMirroringVisualAction.fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                    ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS, 
                    null, 
                    ManagePortMirroringVisualAction.class)
                );
            }
        }
    }
    private void updateMirrors() throws InventoryException {
        selectedPort = null;
        selectedOtherPort = null;
        
        divPortSelector.removeAll();
        VerticalLayout lytPortSelector = getPortSelector();
        lytPortSelector.setSizeFull();
        divPortSelector.add(lytPortSelector);
        
        divMirrorManager.removeAll();
        VerticalLayout lytMirrorManager = getMirrorManager(portParent, port);
        lytMirrorManager.setSizeFull();
        divMirrorManager.add(lytMirrorManager);
    }
    
    private String toString(MirrorType mirrorType) {
        if (MirrorType.SINGLE_MIRROR.equals(mirrorType))
            return ts.getTranslatedString("module.physcon.mirror.setting.type.single-mirror");
        else if (MirrorType.MULTIPLE_MIRROR.equals(mirrorType))
            return ts.getTranslatedString("module.physcon.mirror.setting.type.multiple-mirror");
        return null;
    }
    
    private VerticalLayout getPortSelector() throws InventoryException {
        VerticalLayout lytPortSelector = new VerticalLayout();
        lytPortSelector.setMargin(false);
        lytPortSelector.setPadding(false);
        lytPortSelector.setSpacing(false);
        
        Div divDeviceSelector = new Div();
        divDeviceSelector.setSizeFull();
        
        Button btnDevice = new Button(ts.getTranslatedString("module.physcon.mirror.setting.device"));
        Button btnOtherDevice = new Button(ts.getTranslatedString("module.physcon.mirror.setting.other-device"));
        
        btnDevice.addClickListener(clickEvent -> {
            try {
                clickOtherButton = false;
                btnDevice.setClassName("navigation-button-selected"); //NOI18N
                btnOtherDevice.setClassName("navigation-button"); //NOI18N
                divDeviceSelector.removeAll();
                VerticalLayout lytDeviceSelector = getDeviceSelector(portParent, port, false);
                lytDeviceSelector.setSizeFull();
                divDeviceSelector.add(lytDeviceSelector);
                
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getMessage(), 
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
        });
        btnOtherDevice.addClickListener(clickEvent -> {
            try {
                clickOtherButton = true;
                btnDevice.setClassName("navigation-button"); //NOI18N
                btnOtherDevice.setClassName("navigation-button-selected"); //NOI18N
                divDeviceSelector.removeAll();
                VerticalLayout lytOhterDeviceSelector = getDeviceSelector(otherPortParent, otherPort, true);
                lytOhterDeviceSelector.setSizeFull();
                divDeviceSelector.add(lytOhterDeviceSelector);
                
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getMessage(), 
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
        });
        FlexLayout lytButtons = new FlexLayout(btnDevice, btnOtherDevice);
        lytButtons.setWidthFull();
        lytButtons.setFlexDirection(FlexLayout.FlexDirection.ROW);
        lytButtons.setFlexGrow(1, btnDevice, btnOtherDevice);
        
        if (!clickOtherButton)
            btnDevice.click();
        else
            btnOtherDevice.click();
        lytPortSelector.add(lytButtons, divDeviceSelector);
        lytPortSelector.expand(divDeviceSelector);
        return lytPortSelector;
    }
    
    private VerticalLayout getDeviceSelector(BusinessObjectLight portParentObject, BusinessObjectLight portObject, boolean other) throws InventoryException {
        VerticalLayout lytDeviceSelector = new VerticalLayout();
        lytDeviceSelector.setMargin(false);
        lytDeviceSelector.setPadding(false);
        lytDeviceSelector.setSpacing(false);
        
        TextField txtPortParent = new TextField(!other ? 
            ts.getTranslatedString("module.physcon.mirror-settings.ports-parent") : 
            ts.getTranslatedString("module.physcon.mirror-settings.other-ports-parent")
        );
        txtPortParent.setEnabled(false);
        txtPortParent.setPlaceholder(!other ? 
            ts.getTranslatedString("module.physcon.mirror.setting.port-parent") : 
            ts.getTranslatedString("module.physcon.mirror.setting.other-port-parent")
        );
        
        Button btnSearchPortParent = new Button(VaadinIcon.SEARCH.create(), clickEvent -> {
            ObjectSelectorWindow wdwObjectSelector = new ObjectSelectorWindow(initPortParent, mem, ts, selectedDevice -> {
                try {
                    if (!other)
                        portParent = selectedDevice;
                    else
                        otherPortParent = selectedDevice;
                    updateMirrors();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
            }) {
                
                @Override
                public List<BusinessObjectLight> getItems(BusinessObjectLight selectedObject) throws InventoryException {
                    List<BusinessObjectLight> items = bem.getChildrenOfClassLightRecursive(
                        selectedObject.getId(), selectedObject.getClassName(), 
                        Constants.CLASS_INVENTORYOBJECT, null, -1, -1
                    );
                    Collections.sort(items, Comparator.comparing(BusinessObjectLight::getName));
                    return items;
                }
            };
            wdwObjectSelector.open();
        });
        btnSearchPortParent.getElement().setProperty("title", !other ? //NOI18N
            ts.getTranslatedString("module.physcon.mirror-settings.search-port-parent") : 
            ts.getTranslatedString("module.physcon.mirror-settings.search-other-port-parent")
        ); 
        ComboBox<MirrorType> cmbType = new ComboBox(ts.getTranslatedString("module.physcon.mirror.setting.type"));
        cmbType.setRequired(true);
        cmbType.setWidthFull();
        cmbType.setItems(MirrorType.SINGLE_MIRROR, MirrorType.MULTIPLE_MIRROR);
        cmbType.setValue(mirrorTypeValue);
        cmbType.setItemLabelGenerator(item -> toString(item));
        cmbType.addValueChangeListener(valueChangeEvent -> mirrorTypeValue = valueChangeEvent.getValue());
        lytDeviceSelector.add(cmbType);     
        lytDeviceSelector.expand(cmbType);
        
        HorizontalLayout lytPortParent = new HorizontalLayout(txtPortParent, btnSearchPortParent);
        lytPortParent.setWidthFull();
        lytPortParent.expand(txtPortParent);
        lytPortParent.setMargin(false);
        lytPortParent.setPadding(false);
        lytPortParent.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytDeviceSelector.add(lytPortParent);            
        
        TextField txtSearchPort = new TextField();        
        txtSearchPort.setClearButtonVisible(true);
        txtSearchPort.setPlaceholder(!other ? 
            ts.getTranslatedString("module.physcon.mirror-settings.search-port") : 
            ts.getTranslatedString("module.physcon.mirror-settings.search-other-port")
        );
        txtSearchPort.setSuffixComponent(VaadinIcon.SEARCH.create());
        txtSearchPort.setValueChangeMode(ValueChangeMode.EAGER);
        
        if (portParentObject != null) {
            txtPortParent.setValue(portParentObject.getName());
            if (portObject != null)
                btnSearchPortParent.setEnabled(false);
            MultiSelectListBox<BusinessObjectLight> lstPorts = new MultiSelectListBox();
            lstPorts.setSizeFull();
            
            List<BusinessObjectLight> ports = bem.getChildrenOfClassLight(
                portParentObject.getId(), 
                portParentObject.getClassName(),
                Constants.CLASS_GENERICPORT, -1);
            Collections.sort(ports, Comparator.comparing(BusinessObjectLight::getName));
            
            lstPorts.setItems(ports);
            lstPorts.setRenderer(new ComponentRenderer<>(item -> {
                    HorizontalLayout lyt = new HorizontalLayout();
                    lyt.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
                    lyt.setMargin(false);
                    lyt.setPadding(false);

                    if (item.equals(portObject))
                        lyt.add(VaadinIcon.PIN.create());
                    else {
                        Div div = new Div();
                        div.setWidth("var(--iron-icon-width");
                        div.setHeight("var(--iron-icon-height)");
                        lyt.add(div);
                    }
                    lyt.add(new FormattedObjectDisplayNameSpan(item, false, false, true, false));
                    return lyt;
            }));
            lstPorts.setItemEnabledProvider(item -> {
                try {
                    if (item.equals(portObject))
                        return false;
                    
                    return bem.getSpecialAttributes(
                        item.getClassName(), item.getId(),
                        MirrorType.SINGLE_MIRROR.toString(), MirrorType.MULTIPLE_MIRROR.toString()
                    ).isEmpty();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
                return false;
            });
            Scroller scroller = new Scroller();
            scroller.setSizeFull();
            scroller.setContent(lstPorts);
            
            lstPorts.addValueChangeListener(valueChangeEvent -> {
                Set<BusinessObjectLight> value = valueChangeEvent.getValue();
                if (!value.isEmpty()) {
                    if (!other)
                        selectedPort = value.toArray(new BusinessObjectLight[0])[0];
                    else
                        selectedOtherPort = value.toArray(new BusinessObjectLight[0])[0];
                }
                if (selectedPort != null && selectedOtherPort != null) {
                    try {
                        createMirror(selectedPort, mirrorTypeValue, selectedOtherPort);
                        updateMirrors();
                    } catch (InventoryException ex) {
                        lstPorts.clear();
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                    
                } else if (portObject != null && !value.isEmpty()) {
                    try {
                        BusinessObjectLight objOtherPort = value.iterator().next();
                        createMirror(portObject, mirrorTypeValue, objOtherPort);
                        updateMirrors();
                    } catch (InventoryException ex) {
                        lstPorts.clear();
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                } else if (value.size() == 2) {
                    try {
                        BusinessObjectLight objPort = value.toArray(new BusinessObjectLight[0])[0];
                        BusinessObjectLight objOtherPort = value.toArray(new BusinessObjectLight[0])[1];
                        createMirror(objPort, mirrorTypeValue, objOtherPort);
                        updateMirrors();
                    } catch (InventoryException ex) {
                        lstPorts.clear();
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                }
            });
            lytDeviceSelector.add(scroller);
                        
            txtSearchPort.addValueChangeListener(valueChangeEvent -> {
                searchPortValue = valueChangeEvent.getValue();
                if (searchPortValue  != null && !searchPortValue .isEmpty()) {
                    List<BusinessObjectLight> items = ports.stream()
                        .filter( aPort -> 
                            aPort.getName() != null && 
                            !aPort.getName().isEmpty() && 
                            aPort.getName().toLowerCase().contains(searchPortValue.toLowerCase())
                        ).collect(Collectors.toList());
                                
                    lstPorts.setItems(items);
                }
                else
                    lstPorts.setItems(ports);
            });
            if (searchPortValue != null && !searchPortValue.isEmpty())
                txtSearchPort.setValue(searchPortValue);
        }
        Button btnNewPort = new Button(
            ts.getTranslatedString("module.physcon.mirror-settings.new-port"), 
            VaadinIcon.PLUS.create(), 
            clickEvent -> {
                if (portParentObject != null) {
                    WindowNewObject wdwNewObject = new WindowNewObject(portParentObject, 
                        newBusinessObjectVisualAction, 
                        newBusinessObjectFromTemplateVisualAction, 
                        newMultipleBusinessObjectsVisualAction, ts
                    );
                    actionCompletedListener = actionCompleteEvent -> {
                        try {
                            updateMirrors();
                        } catch (InventoryException ex) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                ex.getMessage(), 
                                AbstractNotification.NotificationType.ERROR, ts
                            ).open();
                        }
                    };
                    wdwNewObject.setActionCompletedListener(actionCompletedListener);
                    wdwNewObject.open();
                    newBusinessObjectVisualAction.registerActionCompletedLister(actionCompletedListener);
                }
            }
        );
        btnNewPort.addClassName("nav-button");
        
        HorizontalLayout lytPorts = new HorizontalLayout();
        lytPorts.setWidthFull();
        lytPorts.setMargin(false);
        lytPorts.setPadding(false);
        lytPorts.add(txtSearchPort, btnNewPort);
        lytPorts.expand(txtSearchPort);
        
        lytDeviceSelector.add(lytPorts);
        return lytDeviceSelector;
    }
    
    private void createMirror(BusinessObjectLight port, MirrorType mirrorType, BusinessObjectLight otherPort) throws InventoryException {
        if (MirrorType.SINGLE_MIRROR.equals(mirrorType)) {
            if (!bem.getSpecialAttributes(port.getClassName(), port.getId(), MirrorType.SINGLE_MIRROR.toString(), MirrorType.MULTIPLE_MIRROR.toString()).isEmpty())
                throw new InventoryException(String.format(ts.getTranslatedString("module.physcon.mirror-manager.related-port"), port.getName()));
            if (!bem.getSpecialAttributes(otherPort.getClassName(), otherPort.getId(), MirrorType.SINGLE_MIRROR.toString(), MirrorType.MULTIPLE_MIRROR.toString()).isEmpty())
                throw new InventoryException(String.format(ts.getTranslatedString("module.physcon.mirror-manager.related-port"), otherPort.getName()));
        } else if (MirrorType.MULTIPLE_MIRROR.equals(mirrorType)) {
            if (!bem.getSpecialAttributes(port.getClassName(), port.getId(), MirrorType.SINGLE_MIRROR.toString()).isEmpty())
                throw new InventoryException(String.format(ts.getTranslatedString("module.physcon.mirror-manager.related-port"), port.getName()));
            if (!bem.getSpecialAttributes(otherPort.getClassName(), otherPort.getId(), MirrorType.SINGLE_MIRROR.toString()).isEmpty())
                throw new InventoryException(String.format(ts.getTranslatedString("module.physcon.mirror-manager.related-port"), otherPort.getName()));
        }
        bem.createSpecialRelationship(
            port.getClassName(), port.getId(),
            otherPort.getClassName(), otherPort.getId(),
            mirrorType.toString(), MirrorType.SINGLE_MIRROR.equals(mirrorType)
        );
    }
    
    private VerticalLayout getMirrorManager(BusinessObjectLight portParentObject, BusinessObjectLight portObject) throws InventoryException {
        VerticalLayout lytMirrorManager = new VerticalLayout();
        lytMirrorManager.add(new Label(ts.getTranslatedString("module.physcon.mirror-manager.title")));
        if (portObject != null)
            lytMirrorManager.add(new Label(ts.getTranslatedString("module.physcon.mirror-manager.port.help")));
        else if (portParentObject != null)
            lytMirrorManager.add(new Label(ts.getTranslatedString("module.physcon.mirror-manager.device.help")));
        
        Grid<Mirror> tblMirrors = new Grid();
        tblMirrors.setSelectionMode(Grid.SelectionMode.MULTI);
        tblMirrors.setWidthFull();
        tblMirrors.setHeight("80%");
        
        List<Mirror> items = new ArrayList();
        if (portObject != null) {
            BusinessObjectLight portParentObj = bem.getParent(portObject.getClassName(), portObject.getId());
            
            HashMap<String, List<BusinessObjectLight>> mirrors = bem.getSpecialAttributes(
                portObject.getClassName(), portObject.getId(), 
                MirrorType.SINGLE_MIRROR.toString(), MirrorType.MULTIPLE_MIRROR.toString()
            );
            mirrors.forEach((stringMirrorType, otherBusinessObjects) -> {
                Collections.sort(otherBusinessObjects, Comparator.comparing(BusinessObjectLight::getName));
                otherBusinessObjects.forEach(otherBusinessObject -> {
                    try {
                        BusinessObjectLight otherPortParentObj = bem.getParent(
                            otherBusinessObject.getClassName(), otherBusinessObject.getId()
                        );
                        items.add(new Mirror(
                            portObject, portParentObj, 
                            stringMirrorType, 
                            otherBusinessObject, otherPortParentObj
                        ));
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                });
            });
        } else if (portParentObject != null) {
            List<BusinessObjectLight> ports = bem.getChildrenOfClassLight(
                portParentObject.getId(), portParentObject.getClassName(),
                Constants.CLASS_GENERICPORT, -1
            );
            Collections.sort(ports, Comparator.comparing(BusinessObjectLight::getName));
            for (BusinessObjectLight portChild : ports) {
                BusinessObjectLight portParentObj = bem.getParent(portChild.getClassName(), portChild.getId());
                
                HashMap<String, List<BusinessObjectLight>> mirrors = bem.getSpecialAttributes(
                    portChild.getClassName(), portChild.getId(), 
                    MirrorType.SINGLE_MIRROR.toString(), MirrorType.MULTIPLE_MIRROR.toString()
                );
                mirrors.forEach((stringMirrorType, otherBusinessObjects) -> {
                    Collections.sort(otherBusinessObjects, Comparator.comparing(BusinessObjectLight::getName));
                    otherBusinessObjects.forEach(otherBusinessObject -> {
                        try {
                            BusinessObjectLight otherPortParentObj = bem.getParent(
                                otherBusinessObject.getClassName(), otherBusinessObject.getId()
                            );
                            Mirror mirror = new Mirror(
                                portChild, portParentObj, 
                                stringMirrorType, 
                                otherBusinessObject, otherPortParentObj
                            );
                            if (!items.contains(mirror))
                                items.add(mirror);
                        } catch (InventoryException ex) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                ex.getMessage(), 
                                AbstractNotification.NotificationType.ERROR, ts
                            ).open();
                        }
                    });
                });
            }
        }
        tblMirrors.setItems(getMirrors(items));
        
        Grid.Column<Mirror> columnPort = tblMirrors.addComponentColumn(item -> {
            HorizontalLayout lytPort = new HorizontalLayout();
            lytPort.setPadding(false);
            lytPort.setMargin(false);
            lytPort.add(
                new FormattedObjectDisplayNameSpan(item.getBusinessObjectParent(), false, false, false, false),
                new Label(">"),
                new FormattedObjectDisplayNameSpan(item.getBusinessObject(), false, false, false, false)
            );
            return lytPort;
        }).setHeader(ts.getTranslatedString("module.physcon.mirror-manager.port"));
        
        Grid.Column<Mirror> columnMirror = tblMirrors.addColumn(item -> toString(item.getMirrorType()))
            .setHeader(ts.getTranslatedString("module.physcon.mirror-manager.mirror-type"));
        
        Grid.Column<Mirror> columnOtherPort = tblMirrors.addComponentColumn(item -> {
            HorizontalLayout lyt = new HorizontalLayout();
            lyt.setPadding(false);
            lyt.setMargin(false);
            lyt.add(
                new FormattedObjectDisplayNameSpan(item.getOtherBusinessObjectParent(), false, false, false, false), 
                new Label(">"), 
                new FormattedObjectDisplayNameSpan(item.getOtherBusinessObject(), false, false, false, false)
            );
            return lyt;
        }).setHeader(ts.getTranslatedString("module.physcon.mirror-manager.other-port"));
        
        Grid.Column<Mirror> columnActions =  tblMirrors.addComponentColumn(item -> {
            HorizontalLayout lytPortTools = new HorizontalLayout();
            lytPortTools.add(new Button(VaadinIcon.CLOSE.create(), clickEvent -> {
                try {
                    bem.releaseSpecialRelationship(
                        item.getBusinessObject().getClassName(), item.getBusinessObject().getId(), 
                        item.getOtherBusinessObject().getId(), 
                        item.getMirrorType().toString()
                    );
                    updateMirrors();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
            }));
            return lytPortTools;
        });        
        TextField txtFilterPort = new TextField();
        if (filterPort != null)
            txtFilterPort.setValue(filterPort);
        txtFilterPort.setClearButtonVisible(true);
        txtFilterPort.setPlaceholder(ts.getTranslatedString("module.physcon.mirror-manager.filter.port"));
        txtFilterPort.setSizeFull();
        txtFilterPort.setValueChangeMode(ValueChangeMode.EAGER);
        txtFilterPort.addValueChangeListener(valueChangeEvent -> {
            filterPort = valueChangeEvent.getValue();
            tblMirrors.setItems(getMirrors(items));
        });
        ComboBox<MirrorType> cmbMirrorType = new ComboBox();
        cmbMirrorType.setClearButtonVisible(true);
        cmbMirrorType.setPlaceholder(ts.getTranslatedString("module.physcon.mirror-manager.filter.mirror"));        
        cmbMirrorType.setItems(MirrorType.SINGLE_MIRROR, MirrorType.MULTIPLE_MIRROR);
        if (filterMirrorType != null)
            cmbMirrorType.setValue(filterMirrorType);
        cmbMirrorType.setItemLabelGenerator(item -> toString(item));
        cmbMirrorType.setSizeFull();
        cmbMirrorType.addValueChangeListener(valueChangeEvent -> {
            filterMirrorType = valueChangeEvent.getValue();
            tblMirrors.setItems(getMirrors(items));
        });
        
        TextField txtFilterOtherPort = new TextField();
        if (filterOtherPort != null)
            txtFilterOtherPort.setValue(filterOtherPort);
        txtFilterOtherPort.setClearButtonVisible(true);
        txtFilterOtherPort.setPlaceholder(ts.getTranslatedString("module.physcon.mirror-manager.filter.other-port"));
        txtFilterOtherPort.setSizeFull();
        txtFilterOtherPort.setValueChangeMode(ValueChangeMode.EAGER);
        txtFilterOtherPort.addValueChangeListener(valueChangeEvent -> {
            filterOtherPort = valueChangeEvent.getValue();
            tblMirrors.setItems(getMirrors(items));
        });
        Button btnDeleteSelected = new Button(
            ts.getTranslatedString("module.physcon.mirror-manager.button.delete-selected-mirrors"), 
            VaadinIcon.CLOSE.create(),
            clickEvent -> {
                ConfirmDialog confirmDialog = new ConfirmDialog(ts, 
                    ts.getTranslatedString("module.general.labels.confirmation"), 
                    new Label(ts.getTranslatedString("module.physcon.mirror-manager.button.delete-selected-mirrors.confirmation")), 
                    ts.getTranslatedString("module.general.messages.ok"), 
                    () -> {
                        Set<Mirror> selectedMirrors = tblMirrors.asMultiSelect().getSelectedItems();
                        if (selectedMirrors != null) {
                            selectedMirrors.forEach(selectedMirror -> {
                                try {
                                    bem.releaseSpecialRelationship(
                                        selectedMirror.getBusinessObject().getClassName(), selectedMirror.getBusinessObject().getId(), 
                                        selectedMirror.getOtherBusinessObject().getId(), 
                                        selectedMirror.getMirrorType().toString()
                                    );                                    
                                } catch (InventoryException ex) {
                                    new SimpleNotification(
                                        ts.getTranslatedString("module.general.messages.error"), 
                                        ex.getMessage(), 
                                        AbstractNotification.NotificationType.ERROR, ts
                                    ).open();
                                }
                            });
                            try {
                                updateMirrors();
                            } catch (InventoryException ex) {
                                new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.error"), 
                                    ex.getMessage(), 
                                    AbstractNotification.NotificationType.ERROR, ts
                                ).open();
                            }
                        }
                    }
                );
                confirmDialog.setDraggable(true );
                confirmDialog.open();
            }
        );
        btnDeleteSelected.setEnabled(false);
        tblMirrors.asMultiSelect().addValueChangeListener(valueChangeEvent -> {
            if (valueChangeEvent.getValue() != null && !valueChangeEvent.getValue().isEmpty())
                btnDeleteSelected.setEnabled(true);
            else
                btnDeleteSelected.setEnabled(false);
        });
        HeaderRow rowFilter = tblMirrors.appendHeaderRow();
        rowFilter.getCell(columnPort).setComponent(txtFilterPort);
        rowFilter.getCell(columnMirror).setComponent(cmbMirrorType);
        rowFilter.getCell(columnOtherPort).setComponent(txtFilterOtherPort);
        
        tblMirrors.appendFooterRow();
        FooterRow footerRow1 = tblMirrors.appendFooterRow();
        footerRow1.join(columnOtherPort, columnActions).setComponent(btnDeleteSelected);
        
        tblMirrors.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        
        lytMirrorManager.add(tblMirrors);
        return lytMirrorManager;
    }
    
    private List<Mirror> getMirrors(List<Mirror> mirrors) {
        List<Mirror> filterMirrors = new ArrayList();
        
        List<Mirror> filterPorts = new ArrayList();
        List<Mirror> filterMirrorTypes = new ArrayList();
        List<Mirror> filterOtherPorts = new ArrayList();
        
        mirrors.forEach(mirror -> {
            String strPort = String.format("%s > %s", 
                mirror.getBusinessObject().getName(), 
                mirror.getBusinessObjectParent().getName()
            );
            if (filterPort != null && !filterPort.isEmpty() && strPort.toLowerCase().contains(filterPort.toLowerCase()))
                filterPorts.add(mirror);
        });
        mirrors.forEach(mirror -> {
            if (filterMirrorType != null && mirror.getMirrorType().equals(filterMirrorType))
                filterMirrorTypes.add(mirror);
        });
        mirrors.forEach(mirror -> {
            String strOtherPort = String.format("%s > %s", 
                mirror.getOtherBusinessObject().getName(),
                mirror.getOtherBusinessObjectParent().getName()
            );
            if (filterOtherPort != null &&  !filterOtherPort.isEmpty() && strOtherPort.toLowerCase().contains(filterOtherPort.toLowerCase()))
                filterOtherPorts.add(mirror);
        });
        filterPorts.forEach(mirror -> {
            if (!filterMirrors.contains(mirror))
                filterMirrors.add(mirror);
        });
        filterMirrorTypes.forEach(mirror -> {
            if (!filterMirrors.contains(mirror))
                filterMirrors.add(mirror);
        });
        filterOtherPorts.forEach(mirror -> {
            if (!filterMirrors.contains(mirror))
                filterMirrors.add(mirror);
        });
        if (filterMirrors.isEmpty()) {
            if ((filterPort == null || filterPort.isEmpty()) && (filterMirrorType == null) && (filterOtherPort == null || filterOtherPort.isEmpty())) {
                Collections.sort(mirrors, Comparator.comparing(Mirror::getBusinessObjectName));
                return mirrors;
            }
        }
        Collections.sort(filterMirrors, Comparator.comparing(Mirror::getBusinessObjectName));
        return filterMirrors;
    }
    /**
     * Class that represent a mirror relationship between two objects.
     */
    private class Mirror {
        private final BusinessObjectLight businessObject;
        private final BusinessObjectLight businessObjectParent;
        private final BusinessObjectLight otherBusinessObject;
        private final BusinessObjectLight otherBusinessObjectParent;
        private MirrorType mirrorType;
        
        public Mirror(
            BusinessObjectLight businessObject, BusinessObjectLight businessObjectParent, 
            String mirrorType, 
            BusinessObjectLight otherBusinessObject, BusinessObjectLight otherBusinessObjectParent) {
            
            this.businessObject = businessObject;
            this.businessObjectParent = businessObjectParent;
            if (MirrorType.SINGLE_MIRROR.toString().equals(mirrorType))
                this.mirrorType = MirrorType.SINGLE_MIRROR;
            else if (MirrorType.MULTIPLE_MIRROR.toString().equals(mirrorType))
                this.mirrorType = MirrorType.MULTIPLE_MIRROR;
            this.otherBusinessObject = otherBusinessObject;
            this.otherBusinessObjectParent = otherBusinessObjectParent;
        }
        
        public String getBusinessObjectName() {
            return businessObject != null ? businessObject.getName() : null;
        }
        
        public BusinessObjectLight getBusinessObject() {
            return businessObject;
        }
        
        public BusinessObjectLight getBusinessObjectParent() {
            return businessObjectParent;
        }
        
        public BusinessObjectLight getOtherBusinessObject() {
            return otherBusinessObject;
        }
        
        public BusinessObjectLight getOtherBusinessObjectParent() {
            return otherBusinessObjectParent;
        }
        
        public MirrorType getMirrorType() {
            return mirrorType;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Mirror) {
                Mirror otherMirror = (Mirror) obj;
                return ((this.getBusinessObject().equals(otherMirror.getBusinessObject()) && this.getOtherBusinessObject().equals(otherMirror.getOtherBusinessObject())) || 
                    (this.getBusinessObject().equals(otherMirror.getOtherBusinessObject()) && this.getOtherBusinessObject().equals(otherMirror.getBusinessObject()))) && 
                    this.getMirrorType().equals(otherMirror.getMirrorType());
            }
            return false;
        }
    }
}