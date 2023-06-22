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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsUtil;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Action to create a new link connected to the connection source and target.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class NewLinkAction extends AbstractLinkConnectivityAction {
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final String header;
    private BusinessObjectLight newLinkParent;
    private BusinessObjectLight newLinkEndpointA;
    private BusinessObjectLight newLinkEndpointB;
    private String newLinkName;
    private ClassMetadataLight newLinkClass;
    
    public NewLinkAction(Connection connection, String header, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        super(connection);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(header);
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.header = header;
    }
    
    public String getNewLinkName() {
        return newLinkName;
    }
    
    public ClassMetadataLight getNewLinkClass() {
        return newLinkClass;
    }
    
    private String businessObjectAsString(BusinessObjectLight businessObject) {
        Objects.requireNonNull(businessObject);
        try {
            return String.format("%s [%s]", businessObject.getName(), mem.getClass(businessObject.getClassName()).toString());
        } catch (MetadataObjectNotFoundException ex) {
            return businessObject.toString();
        }
    }
    
    private FlexLayout getNewLinkRow() {
        newLinkEndpointA = getConnection().getSource() != null ? getConnection().getSource().getSelectedObject() : null;
        newLinkEndpointB = getConnection().getTarget() != null ? getConnection().getTarget().getSelectedObject() : null;
        if (newLinkEndpointA != null && newLinkEndpointB != null) {
            try {
                newLinkParent = bem.getCommonParent(
                    newLinkEndpointA.getClassName(), newLinkEndpointA.getId(), 
                    newLinkEndpointB.getClassName(), newLinkEndpointB.getId()
                );
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        }
        TextField txtParent = new TextField(ts.getTranslatedString("module.connectivity-manager.action-3.placeholder.new-link-parent"));
        txtParent.setEnabled(false);
        txtParent.setWidthFull();
        txtParent.setClearButtonVisible(true);
        txtParent.setRequired(true);
        txtParent.setRequiredIndicatorVisible(true);
        if (newLinkParent != null)
            txtParent.setValue(businessObjectAsString(newLinkParent));
        
        TextField txtEndpointA = new TextField(ts.getTranslatedString("module.connectivity-manager.action-3.placeholder.new-link-endpoint-a"));
        txtEndpointA.setEnabled(false);
        txtEndpointA.setRequired(true);
        txtEndpointA.setRequiredIndicatorVisible(true);
        if (newLinkEndpointA != null)
            txtEndpointA.setValue(businessObjectAsString(newLinkEndpointA));
        
        TextField txtEndpointB = new TextField(ts.getTranslatedString("module.connectivity-manager.action-3.placeholder.new-link-endpoint-b"));
        txtEndpointB.setEnabled(false);
        txtEndpointB.setRequired(true);
        txtEndpointB.setRequiredIndicatorVisible(true);
        if (newLinkEndpointB != null)
            txtEndpointB.setValue(businessObjectAsString(newLinkEndpointB));
        
        TextField txtName = new TextField(ts.getTranslatedString("module.connectivity-manager.action-3.label.new-link-name"));
        txtName.setClearButtonVisible(true);
        txtName.setRequired(true);
        txtName.setRequiredIndicatorVisible(true);
        
        newLinkName = PhysicalConnectionsUtil.getLinkName(newLinkEndpointA, newLinkEndpointB, bem, mem, ts);
        txtName.setValue(newLinkName);
        txtName.addValueChangeListener(valueChangeEvent -> newLinkName = valueChangeEvent.getValue());
        
        ComboBox<ClassMetadataLight> cmbClass = new ComboBox();
        cmbClass.setPlaceholder(ts.getTranslatedString("module.connectivity-manager.action-3.placeholder.new-link-class"));
        cmbClass.setClearButtonVisible(true);        
        cmbClass.setRequired(true);
        cmbClass.setRequiredIndicatorVisible(true);
        cmbClass.setDataProvider(getLinkClassesDataProvider());
        cmbClass.setValue(newLinkClass);
        cmbClass.addValueChangeListener(valueChangeEvent -> newLinkClass = valueChangeEvent.getValue());
        
        HorizontalLayout lytEndpoints = new HorizontalLayout(txtEndpointA, txtEndpointB);
        lytEndpoints.setWidthFull();
        lytEndpoints.setMargin(false);
        lytEndpoints.setPadding(false);
        lytEndpoints.setFlexGrow(1, txtEndpointA, txtEndpointB);
        lytEndpoints.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        
        HorizontalLayout lytContainer = new HorizontalLayout(txtName, cmbClass);
        lytContainer.setWidthFull();
        lytContainer.setMargin(false);
        lytContainer.setPadding(false);
        lytContainer.setFlexGrow(1, txtName, cmbClass);
        lytContainer.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        
        FlexLayout newLinkRow = new FlexLayout(txtParent, lytEndpoints, lytContainer);
        newLinkRow.setWidthFull();
        newLinkRow.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        return newLinkRow;
    }
    
    @Override
    public Component getComponent() {
        ConfirmDialog wdw = new ConfirmDialog() {
            @Override
            public void open() {
                try {
                    boolean open = true;
                    BusinessObjectLight source = getConnection().getSource() != null ? getConnection().getSource().getSelectedObject() : null;
                    BusinessObjectLight target = getConnection().getTarget() != null ? getConnection().getTarget().getSelectedObject() : null;
                    if (source != null && !bem.getSpecialAttributes(source.getClassName(), source.getId(), PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA, PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB).isEmpty()) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.warning"), 
                            ts.getTranslatedString("module.connectivity-manager.action-3.connected-source"), 
                            AbstractNotification.NotificationType.WARNING, 
                            ts
                        ).open();
                        open = false;
                    }
                    if (target != null && !bem.getSpecialAttributes(target.getClassName(), target.getId(), PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA, PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB).isEmpty()) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.warning"), 
                            ts.getTranslatedString("module.connectivity-manager.action-3.connected-target"), 
                            AbstractNotification.NotificationType.WARNING, 
                            ts
                        ).open();
                        open = false;
                    }
                    if (source != null && target != null) {
                        BusinessObjectLight parent = bem.getCommonParent(
                            source.getClassName(), source.getId(), 
                            target.getClassName(), target.getId()
                        );
                        if (parent == null) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.warning"), 
                                ts.getTranslatedString("module.connectivity-manager.action-3.no-common-parent"), 
                                AbstractNotification.NotificationType.WARNING, 
                                ts
                            ).open();
                            open = false;
                        }
                    }
                    if (open)
                        super.open();
                    /**
                     * If one or both link endpoints are null 
                     * or are connected 
                     * or are null 
                     * or no has common parent 
                     * the window is not open.
                     */
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
            }
        };
        wdw.setCloseOnOutsideClick(false);
        wdw.setWidth("70%");
        
        VerticalLayout lytContent = new VerticalLayout(getNewLinkRow());
        lytContent.setWidthFull();
        lytContent.setMargin(false);
        lytContent.setPadding(false);
        
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), clickEvent -> wdw.close());
        Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"), clickEvent -> {
            boolean close = true;
            if (newLinkName == null || newLinkName.isEmpty()) {
                close = false;
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.warning"), 
                    ts.getTranslatedString("module.connectivity-manager.action-3.new-link-name-empty"), 
                    AbstractNotification.NotificationType.WARNING, 
                    ts
                ).open();
            }
            if (newLinkClass == null) {
                close = false;
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.warning"), 
                    ts.getTranslatedString("module.connectivity-manager.action-3.new-link-class-empty"), 
                    AbstractNotification.NotificationType.WARNING, 
                    ts
                ).open();
            }
            if (close)
                wdw.close();
        });
        FlexLayout lytFooter = new FlexLayout(btnCancel, btnOk);
        lytFooter.setSizeFull();
        lytFooter.setFlexGrow(1, btnCancel, btnOk);
        
        wdw.setHeader(header);
        wdw.setContent(lytContent);
        wdw.setFooter(lytFooter);
        return wdw;
    }
    
    private DataProvider<ClassMetadataLight, String> getLinkClassesDataProvider() {
        return DataProvider.fromFilteringCallbacks(
            query -> {
                String filter = query.getFilter().orElse("");
                try {
                    List<ClassMetadataLight> subclasses = mem.getSubClassesLight(
                        Constants.CLASS_GENERICPHYSICALLINK, 
                        false, 
                        false
                    );
                    return subclasses.stream()
                        .sorted(Comparator.comparing(ClassMetadataLight::getName))
                        .filter(subclass -> subclass.getName().toLowerCase().contains(filter.toLowerCase()))
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
                String filter = query.getFilter().orElse("");
                try {
                    List<ClassMetadataLight> subclasses = mem.getSubClassesLight(
                        Constants.CLASS_GENERICPHYSICALLINK, 
                        false, 
                        false
                    );
                    return (int) subclasses.stream()
                        .filter(subclass -> subclass.getName().toLowerCase().contains(filter.toLowerCase()))
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
    @Override
    public boolean execute() {
        try {
            //Updating the link endpoints and parent by those selected in the connection
            newLinkEndpointA = getConnection().getSource() != null ? getConnection().getSource().getSelectedObject() : null;
            newLinkEndpointB = getConnection().getTarget() != null ? getConnection().getTarget().getSelectedObject() : null;
                        
            if (newLinkEndpointA == null)
                return false;
            if (newLinkEndpointB == null)
                return false;
            if (!bem.getSpecialAttributes(newLinkEndpointA.getClassName(), newLinkEndpointA.getId(), PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA, PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB).isEmpty())
                return false;
            if (!bem.getSpecialAttributes(newLinkEndpointB.getClassName(), newLinkEndpointB.getId(), PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA, PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB).isEmpty())
                return false;
            if (newLinkName == null || newLinkName.isEmpty())
                return false;
            if (newLinkClass == null)
                return false;
            
            newLinkParent = bem.getCommonParent(
                newLinkEndpointA.getClassName(), newLinkEndpointA.getId(), 
                newLinkEndpointB.getClassName(), newLinkEndpointB.getId()
            );
            if (newLinkParent == null)
                return false;
            
            HashMap<String, String> newLinkAttributes = new HashMap();
            newLinkAttributes.put(Constants.PROPERTY_NAME, newLinkName);
            
            String newLinkId = bem.createSpecialObject(newLinkClass.getName(), 
                newLinkParent.getClassName(), newLinkParent.getId(), 
                newLinkAttributes, null
            );
            bem.createSpecialRelationship(
                newLinkEndpointA.getClassName(), newLinkEndpointA.getId(),
                newLinkClass.getName(), newLinkId, 
                PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA, true
            );
            bem.createSpecialRelationship(
                newLinkEndpointB.getClassName(), newLinkEndpointB.getId(),
                newLinkClass.getName(), newLinkId, 
                PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB, true
            );
            return true;
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
            return false;
        }
    }
}