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

import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.layout.GridLayout;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Dialog to create a new container connection.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowNewContainer extends ConfirmDialog {
    /**
     * Source Location
     */
    public final BusinessObjectLight source;
    /**
     * Target Location
     */
    public final BusinessObjectLight target;
    /**
     * Reference to the Application Entity Manager
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    public final BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    public final MetadataEntityManager mem;
    /**
     * Reference to the Translation Service
     */
    public final TranslationService ts;
    /**
     * Callback to execute to cancel new container.
     */
    private final Runnable callbackCancel;
    
    private final PhysicalConnectionsService physicalConnectionService;
    
    private final Consumer<BusinessObjectLight> containerConsumer;
    
    public WindowNewContainer(BusinessObjectLight source, BusinessObjectLight target, 
        TranslationService ts, ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, 
        PhysicalConnectionsService physicalConnectionService, Consumer<BusinessObjectLight> containerConsumer, Runnable callbackCancel) {
        
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(physicalConnectionService);
        Objects.requireNonNull(containerConsumer);
        Objects.requireNonNull(callbackCancel);
        
        this.source = source;
        this.target = target;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.callbackCancel = callbackCancel;
        this.physicalConnectionService = physicalConnectionService;
        this.containerConsumer = containerConsumer;
    }

    @Override
    public void open() {
        try {
            BusinessObjectLight parent = bem.getCommonParent(
                source.getClassName(), source.getId(), 
                target.getClassName(), target.getId()
            );
            if (parent == null || Constants.DUMMY_ROOT.equals(parent.getName()))
                throw new OperationNotPermittedException(ts.getTranslatedString("module.physcon.messages.no-common-parent"));
            init();
            super.open();
        } catch (InventoryException ex) {
            callbackCancel.run();
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"),
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
    }
    
    public void init() throws InventoryException {
        VerticalLayout lytContent = new VerticalLayout();
        lytContent.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        
        GridLayout lytEndpoints = new GridLayout();
        lytEndpoints.setGridTemplateColumns(5);
        
        String endpointAName = source.getName() != null ? source.getName() : 
            ts.getTranslatedString("module.propertysheet.labels.null-value-property");
        lytEndpoints.add(new Label(ts.getTranslatedString("module.ospman.containers.endpointa")), 1, 1);
        lytEndpoints.add(new BoldLabel(endpointAName), 2, 1);
        
        lytEndpoints.add(new Label("/"), 3, 1);
        
        String endpointBName = target.getName() != null ? target.getName() : 
            ts.getTranslatedString("module.propertysheet.labels.null-value-property");
        lytEndpoints.add(new Label(ts.getTranslatedString("module.ospman.containers.endpointb")), 4, 1);
        lytEndpoints.add(new BoldLabel(endpointBName), 5, 1);
        lytContent.add(lytEndpoints);
        
        Tab tabCreateContainer = new Tab(ts.getTranslatedString("module.ospman.containers.create-container"));
        Tab tabSelectContainer = new Tab(ts.getTranslatedString("module.ospman.containers.existing-container"));
        Tabs tabs = new Tabs(tabCreateContainer, tabSelectContainer);
        tabs.setSelectedTab(tabCreateContainer);
        lytContent.add(tabs);
        
        Div tabContent = new Div();
        
        TextField txtName = new TextField();
        txtName.setWidthFull();
        txtName.setRequired(true);
        txtName.setClearButtonVisible(true);

        ComboBox<ClassMetadataLight> cmbClass = new ComboBox();
        cmbClass.setWidthFull();
        cmbClass.setClearButtonVisible(true);
        cmbClass.setRequired(true);
        cmbClass.setPlaceholder(ts.getTranslatedString("module.ospman.placeholder.select-container-class"));
        cmbClass.setItems(mem.getSubClassesLight(Constants.CLASS_GENERICPHYSICALCONTAINER, false, false));

        ComboBox<TemplateObjectLight> cmbTemplate = new ComboBox();
        cmbTemplate.setWidthFull();
        cmbTemplate.setClearButtonVisible(true);
        cmbTemplate.setItemLabelGenerator(TemplateObjectLight::getName);
        cmbTemplate.setPlaceholder(ts.getTranslatedString("module.ospman.placeholder.select-container-template"));
        cmbClass.addValueChangeListener(event -> {
            cmbTemplate.clear();
            ClassMetadataLight value = event.getValue();
            if (value != null) {
                try {
                    cmbTemplate.setItems(aem.getTemplatesForClass(value.getName()));
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
            }
        });
        List<BusinessObjectLight> endpoints = new ArrayList();

        List<BusinessObjectLight> sourceEndpointsA = bem.getSpecialAttribute(source.getClassName(), source.getId(), OspConstants.SPECIAL_ATTR_ENDPOINT_A);
        List<BusinessObjectLight> sourceEndpointsB = bem.getSpecialAttribute(source.getClassName(), source.getId(), OspConstants.SPECIAL_ATTR_ENDPOINT_B);
        List<BusinessObjectLight> targetEndpointsA = bem.getSpecialAttribute(target.getClassName(), target.getId(), OspConstants.SPECIAL_ATTR_ENDPOINT_A);
        List<BusinessObjectLight> targetEndpointsB = bem.getSpecialAttribute(target.getClassName(), target.getId(), OspConstants.SPECIAL_ATTR_ENDPOINT_B);

        List<BusinessObjectLight> sourceEndpoints = new ArrayList();
        sourceEndpoints.addAll(sourceEndpointsA);
        sourceEndpoints.addAll(sourceEndpointsB);

        List<BusinessObjectLight> targetEndpoints = new ArrayList();
        targetEndpoints.addAll(targetEndpointsA);
        targetEndpoints.addAll(targetEndpointsB);

        sourceEndpoints.forEach(sourceEndpoint -> {
            targetEndpoints.forEach(targetEndpoint -> {
                if (sourceEndpoint.equals(targetEndpoint))
                    endpoints.add(targetEndpoint);
            });
        });
        Collections.sort(endpoints, Comparator.comparing(BusinessObjectLight::getName));
        
        ComboBox<BusinessObjectLight> cmbContainer = new ComboBox();
        cmbContainer.setWidthFull();
        cmbContainer.setItemLabelGenerator(BusinessObjectLight::getName);
        cmbContainer.setRenderer(new ComponentRenderer<>(item -> {
            FlexLayout lytItem = new FlexLayout();
            lytItem.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
            lytItem.add(new Label(item.getName()));
            Label lblClass = new Label();
            lblClass.setClassName("text-secondary"); //NOI18N
            try {
                lytItem.add(mem.getClass(item.getClassName()).toString());
            } catch (MetadataObjectNotFoundException ex) {
                lytItem.add(item.getClassName());
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
            lytItem.add(lblClass);
            return lytItem;
        }));
        cmbContainer.setClearButtonVisible(true);
        cmbContainer.setItems(endpoints);
        cmbContainer.addValueChangeListener(valueChangeEvent -> {
            txtName.clear();
            cmbClass.clear();
            cmbTemplate.clear();
        });
        String width = "300px";
        txtName.setWidth(width);
        cmbClass.setWidth(width);
        cmbTemplate.setWidth(width);
        cmbContainer.setWidth(width);
        GridLayout lytCreateContainerContent = getCrateContainerContent(txtName, cmbClass, cmbTemplate);
        GridLayout lytSelectContainerContent = getSelectContainerContent(cmbContainer);
        lytSelectContainerContent.setVisible(false);
        
        tabContent.add(lytCreateContainerContent);
        tabContent.add(lytSelectContainerContent);
        lytContent.add(tabContent);
        
        tabs.addSelectedChangeListener(event -> {
            cmbContainer.clear();
            lytCreateContainerContent.setVisible(false);
            lytSelectContainerContent.setVisible(false);
            
            if (event.getSelectedTab() == tabCreateContainer) {
                lytCreateContainerContent.setVisible(true);
            }
            if (event.getSelectedTab() == tabSelectContainer) {
                lytSelectContainerContent.setVisible(true);
            }
        });
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), event -> {
            close();
            callbackCancel.run();
        });
        Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"), event -> {
            if (cmbContainer.getValue() != null) {
                containerConsumer.accept(cmbContainer.getValue());
                close();
                return;
            }
            try {
                String containerName = txtName.getValue() != null && !txtName.getValue().isEmpty() ? txtName.getValue() : null;
                String containerClass = cmbClass.getValue() != null ? cmbClass.getValue().getName() : null;
                String templateId = cmbTemplate.getValue() != null ? cmbTemplate.getValue().getId() : null;
                if (txtName.getValue() == null) {
                    txtName.focus();
                    return;
                } else if (txtName.getValue().isEmpty()) {
                    txtName.focus();
                    return;
                }
                if (cmbClass.getValue() == null) {
                    cmbClass.focus();
                    return;
                }
                if (containerName != null && containerClass != null) {
                    Session session = UI.getCurrent().getSession().getAttribute(Session.class);
                    String containerId = physicalConnectionService.createPhysicalConnection(
                            source.getClassName(), source.getId(),
                            target.getClassName(), target.getId(),
                            containerName, containerClass, templateId,
                            session.getUser().getUserName());

                    containerConsumer.accept(bem.getObjectLight(containerClass, containerId));
                }
                else
                    callbackCancel.run();
                close();

            } catch (IllegalStateException | InventoryException ex) {
                callbackCancel.run();
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getMessage(), 
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
                close();
            }
        });
        btnOk.setClassName("primary-button"); //NOI18N
        HorizontalLayout lytFooter = new HorizontalLayout();
        lytFooter.addAndExpand(btnCancel, btnOk);
        
        setHeader(ts.getTranslatedString("module.ospman.containers.new"));
        setContent(lytContent);
        setFooter(lytFooter);
        
        setDraggable(true);
        setCloseOnOutsideClick(false);
        addThemeVariants(EnhancedDialogVariant.SIZE_SMALL);
    }
    
    public GridLayout getCrateContainerContent(TextField txtName, ComboBox cmbClass, ComboBox cmbTemplate) {
        GridLayout lytContent = new GridLayout();
        lytContent.setGridTemplateColumns(3);
        
        Label lblName = new Label(ts.getTranslatedString("module.general.labels.name"));                
        lytContent.add(lblName, 1, 1);
        lytContent.add(txtName, 2, 1, 3, 1);
        lytContent.setAlignSelf(GridLayout.Alignment.CENTER, lblName);
        lytContent.setAlignSelf(GridLayout.Alignment.CENTER, txtName);
        
        Label lblClass = new Label(ts.getTranslatedString("module.ospman.new-connection-class"));
        lytContent.add(lblClass, 1, 2);
        lytContent.add(cmbClass, 2, 2, 3, 2);
        lytContent.setAlignSelf(GridLayout.Alignment.CENTER, lblClass);
        lytContent.setAlignSelf(GridLayout.Alignment.CENTER, cmbClass);
        
        Label lblTemplate = new Label(ts.getTranslatedString("module.ospman.new-connection-template"));
        lytContent.add(lblTemplate, 1, 3);
        lytContent.add(cmbTemplate, 2, 3, 3, 3);
        lytContent.setAlignSelf(GridLayout.Alignment.CENTER, lblTemplate);
        lytContent.setAlignSelf(GridLayout.Alignment.CENTER, cmbTemplate);
        return lytContent;
    }
    
    public GridLayout getSelectContainerContent(ComboBox cmbContainer) {
        GridLayout lytContent = new GridLayout();
        lytContent.setGridTemplateColumns(2);
        Label lblContainer = new Label(ts.getTranslatedString("module.ospman.containers.container"));
        lytContent.add(lblContainer, 1, 1);
        lytContent.add(cmbContainer, 2, 1, 3, 1);
        lytContent.setAlignSelf(GridLayout.Alignment.CENTER, lblContainer);
        lytContent.setAlignSelf(GridLayout.Alignment.CENTER, cmbContainer);
        return lytContent;
    }
}
