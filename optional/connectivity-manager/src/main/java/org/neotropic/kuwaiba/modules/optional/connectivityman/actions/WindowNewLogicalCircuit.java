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
package org.neotropic.kuwaiba.modules.optional.connectivityman.actions;

import org.neotropic.util.visual.selectors.ObjectCellComponent;
import org.neotropic.util.visual.selectors.PortSelector;
import com.neotropic.kuwaiba.modules.commercial.mpls.MplsService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.connectivityman.ConnectivityManagerService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsUtil;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to create a logical circuit.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowNewLogicalCircuit extends ConfirmDialog {
    /**
     * Reference to the Application Entity Manager.
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    private final MetadataEntityManager mem;
    /**
     * Reference to the Translation Service.
     */
    private final TranslationService ts;
    /**
     * Reference to the MPLS Service.
     */
    private final MplsService mplsService;
    /**
     * Reference to the Outside Plant Service.
     */
    private final ConnectivityManagerService connectivityManagerService;
    
    public WindowNewLogicalCircuit(
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, 
        TranslationService ts, MplsService mplsService, ConnectivityManagerService connectivityManagerService) {
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.mplsService = mplsService;
        this.connectivityManagerService = connectivityManagerService;
        setWidth("40%");
        setHeight("55%");
        setDraggable(true);
        setResizable(true);
        setCloseOnOutsideClick(true);
    }
    
    @Override
    public void open() {
        PortSelector selectorSourcePort = new PortSelector(
            ts.getTranslatedString("module.new-logical-circuit.wdw.new-logical-circuit.select-source-port"), 
            aem, bem, mem, ts
        );
        selectorSourcePort.setHasWarnings(false);
        
        PortSelector selectorTargetPort = new PortSelector(
            ts.getTranslatedString("module.new-logical-circuit.wdw.new-logical-circuit.select-target-port"), 
            aem, bem, mem, ts
        
        );
        selectorTargetPort.setHasWarnings(false);
        
        final String typeMplsCircuit = ts.getTranslatedString("module.new-logical-circuit.wdw.type.mpls-circuit");
        final String typeLastMileCircuit = ts.getTranslatedString("module.new-logical-circuit.wdw.type.last-mile-circuit");
        
        ComboBox<ClassMetadataLight> cmbLastMileCircuitType = new ComboBox();
        cmbLastMileCircuitType.setWidthFull();
        cmbLastMileCircuitType.setRequired(true);
        cmbLastMileCircuitType.setRequiredIndicatorVisible(true);
        cmbLastMileCircuitType.setClearButtonVisible(true);
        cmbLastMileCircuitType.setVisible(false);
        cmbLastMileCircuitType.setPlaceholder(ts.getTranslatedString("module.new-logical-circuit.wdw.last-mile-circuit.type"));
        cmbLastMileCircuitType.setDataProvider(getLastMileCircuitTypeDataProvider());
        
        ComboBox<String> cmbType = new ComboBox();
        cmbType.setWidthFull();
        cmbType.setRequired(true);
        cmbType.setRequiredIndicatorVisible(true);
        cmbType.setClearButtonVisible(true);
        cmbType.setPlaceholder(ts.getTranslatedString("module.new-logical-circuit.wdw.new-logical-circuit.select-type"));
        cmbType.setItems(typeMplsCircuit, typeLastMileCircuit);
        cmbType.addValueChangeListener(valueChangeEvent -> {
            if (typeLastMileCircuit.equals(valueChangeEvent.getValue()))
                cmbLastMileCircuitType.setVisible(true);
            else {
                cmbLastMileCircuitType.clear();
                cmbLastMileCircuitType.setVisible(false);
            }
        });
        
        TextField txtName = new TextField();
        txtName.setWidthFull();
        txtName.setRequired(true);
        txtName.setRequiredIndicatorVisible(true);
        txtName.setPlaceholder(ts.getTranslatedString("module.new-logical-circuit.wdw.new-logical-circuit.name"));
        
        Div divSource = new Div();
        divSource.setWidthFull();
        divSource.add(ts.getTranslatedString("module.new-logical-circuit.wdw.new-logical-circuit.select-source-port"));
        divSource.addClickListener(clickEvent -> {
            try {
                ConfirmDialog wdw = selectorSourcePort.getPortSelector();
                wdw.setDraggable(true);
                wdw.setModal(false);
                wdw.setCloseOnEsc(false);
                wdw.setCloseOnOutsideClick(false);
                wdw.setResizable(true);
                wdw.setWidth("60%");
                wdw.open();
                wdw.addOpenedChangeListener(openedChangeEvent -> {
                    if (!openedChangeEvent.isOpened()) {
                        divSource.removeAll();
                        txtName.clear();
                        if (selectorSourcePort.getSelectedObject() != null) {
                            ObjectCellComponent componentSourcePort = new ObjectCellComponent(
                                selectorSourcePort.getSelectedObject(), bem, ts, 
                                Arrays.asList(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, Constants.CLASS_GENERICDISTRIBUTIONFRAME)
                            );
                            divSource.add(componentSourcePort);
                            if (selectorSourcePort.getSelectedObject() != null && selectorTargetPort.getSelectedObject() != null)
                                txtName.setValue(String.format("%s - %s", selectorSourcePort.getSelectedObject().getName(), selectorTargetPort.getSelectedObject().getName()));
                        }
                        else
                            divSource.add(ts.getTranslatedString("module.new-logical-circuit.wdw.new-logical-circuit.select-source-port"));
                    }
                });
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        });
        Div divTarget = new Div();
        divTarget.setWidthFull();
        divTarget.add(ts.getTranslatedString("module.new-logical-circuit.wdw.new-logical-circuit.select-target-port"));
        divTarget.addClickListener(clickEvent -> {
            try {
                ConfirmDialog wdw = selectorTargetPort.getPortSelector();
                wdw.setDraggable(true);
                wdw.setModal(false);
                wdw.setCloseOnEsc(false);
                wdw.setCloseOnOutsideClick(false);
                wdw.setResizable(true);
                wdw.setWidth("60%");
                wdw.open();
                wdw.addOpenedChangeListener(openedChangeEvent -> {
                    if (!openedChangeEvent.isOpened()) {
                        divTarget.removeAll();
                        txtName.clear();
                        if (selectorTargetPort.getSelectedObject() != null) {
                            ObjectCellComponent componentTargetPort = new ObjectCellComponent(
                                selectorTargetPort.getSelectedObject(), bem, ts, 
                                Arrays.asList(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, Constants.CLASS_GENERICDISTRIBUTIONFRAME)
                            );
                            divTarget.add(componentTargetPort);
                            
                            txtName.setValue(PhysicalConnectionsUtil.getLinkName(
                                selectorSourcePort.getSelectedObject(), 
                                selectorTargetPort.getSelectedObject(),
                                bem, mem, ts
                            ));
                        }
                        else
                            divTarget.add(ts.getTranslatedString("module.new-logical-circuit.wdw.new-logical-circuit.select-target-port"));
                    }
                });
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        });
        
        VerticalLayout lytContent = new VerticalLayout(divSource, divTarget, cmbType, cmbLastMileCircuitType, txtName);
        
        Button btnCancel = new Button(
            ts.getTranslatedString("module.general.messages.cancel"), 
            clickEvent -> close()
        );
        Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"), clickEvent -> {
            if (selectorSourcePort.getSelectedObject() == null) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.information"), 
                    ts.getTranslatedString("module.new-logical-circuit.wdw.notification.info.select-source-port"), 
                    AbstractNotification.NotificationType.INFO, 
                    ts
                ).open();
                return;
            }
            if (selectorTargetPort.getSelectedObject() == null) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.information"), 
                    ts.getTranslatedString("module.new-logical-circuit.wdw.notification.info.select-target-port"), 
                    AbstractNotification.NotificationType.INFO, 
                    ts
                ).open();
                return;
            }
            if (cmbType.getValue() == null) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.information"), 
                    ts.getTranslatedString("module.new-logical-circuit.wdw.notification.info.select-logical-circuit-type"), 
                    AbstractNotification.NotificationType.INFO, ts
                ).open();
                return;
            }
            if (txtName.getValue() == null || txtName.getValue().isEmpty()) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.information"), 
                    ts.getTranslatedString("module.new-logical-circuit.wdw.notification.info.name-cannot-be-empty"), 
                    AbstractNotification.NotificationType.INFO, 
                    ts
                ).open();
                return;
            }
            if (typeMplsCircuit.equals(cmbType.getValue()))
                createMplsCircuit(txtName.getValue(), selectorSourcePort.getSelectedObject(), selectorTargetPort.getSelectedObject());
            else if (typeLastMileCircuit.equals(cmbType.getValue())) {
                if (cmbLastMileCircuitType.getValue() == null) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.information"), 
                        ts.getTranslatedString("module.new-logical-circuit.wdw.notification.info.select-last-mile-circuit-type"), 
                        AbstractNotification.NotificationType.INFO, 
                        ts
                    ).open();
                    return;
                }
                createLastMileCircuit(txtName.getValue(), cmbLastMileCircuitType.getValue(), selectorSourcePort.getSelectedObject(), selectorTargetPort.getSelectedObject());
            }
            close();
        });
        HorizontalLayout lytFooter = new HorizontalLayout(btnCancel, btnOk);
        lytFooter.setWidthFull();
        lytFooter.setFlexGrow(1, btnCancel, btnOk);
        
        setHeader(ts.getTranslatedString("module.new-logical-circuit.wdw.new-logical-circuit.header"));
        setContent(lytContent);
        setFooter(lytFooter);
        super.open();
    }
    
    private void createMplsCircuit(String name, BusinessObjectLight source, BusinessObjectLight target) {
        try {
            HashMap<String, String> attributes = new HashMap();
            attributes.put(Constants.PROPERTY_NAME, name);
            Session session = UI.getCurrent().getSession().getAttribute(Session.class);
            
            mplsService.createMPLSLink(
                source.getClassName(), source.getId(), 
                target.getClassName(), target.getId(), 
                attributes, session.getUser().getUserName()
            );
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.information"), 
                ts.getTranslatedString("module.new-logical-circuit.wdw.notification.info.logical-circuit-created"), 
                AbstractNotification.NotificationType.INFO, 
                ts
            ).open();
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
    }
    
    private void createLastMileCircuit(String name, ClassMetadataLight type, BusinessObjectLight source, BusinessObjectLight target) {
        try {
            HashMap<String, String> attributes = new HashMap();
            attributes.put(Constants.PROPERTY_NAME, name);
            Session session = UI.getCurrent().getSession().getAttribute(Session.class);
            
            connectivityManagerService.createLastMileLink(
                source.getClassName(), source.getId(), 
                target.getClassName(), target.getId(), 
                type.getName(), attributes, 
                session.getUser().getUserName()
            );
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.information"), 
                ts.getTranslatedString("module.new-logical-circuit.wdw.notification.info.logical-circuit-created"), 
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
    }
    
    private DataProvider<ClassMetadataLight, String> getLastMileCircuitTypeDataProvider() {
        return DataProvider.fromFilteringCallbacks(
            query -> {
                try {
                    List<ClassMetadataLight> classes = mem.getSubClassesLight(
                        Constants.CLASS_GENERICLASTMILECIRCUIT, false, false
                    );
                    return classes.stream()
                        .sorted(Comparator.comparing(ClassMetadataLight::toString))
                        .filter(c -> StringUtils.containsIgnoreCase(c.toString(), query.getFilter().orElse("")))
                        .skip(query.getOffset())
                        .limit(query.getLimit());
                    
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
                return null;
            }, 
            query -> {
                try {
                    List<ClassMetadataLight> classes = mem.getSubClassesLight(
                        Constants.CLASS_GENERICLASTMILECIRCUIT, false, false
                    );
                    return (int) classes.stream()
                        .filter(c -> StringUtils.containsIgnoreCase(c.toString(), query.getFilter().orElse("")))
                        .skip(query.getOffset())
                        .limit(query.getLimit())
                        .count();
                    
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
                return 0;
            }
        );
    }
}
