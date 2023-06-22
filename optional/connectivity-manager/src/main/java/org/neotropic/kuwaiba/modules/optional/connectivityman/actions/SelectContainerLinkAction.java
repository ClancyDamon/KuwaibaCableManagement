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

import org.neotropic.util.visual.selectors.ObjectRenderer;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import static org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA;
import static org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Select an existing link in an existing container.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class SelectContainerLinkAction extends AbstractLinkConnectivityAction {
    private final List<BusinessObjectLight> selectedContainers = new ArrayList();    
    private ValueChangeListener<ComponentValueChangeEvent<ComboBox<BusinessObjectLight>, BusinessObjectLight>> listener;
    
    private final String header;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    
    public SelectContainerLinkAction(Connection connection, String header, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        super(connection);
        Objects.requireNonNull(header);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        
        this.header = header;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
    }
    
    @Override
    public ConfirmDialog getComponent() {
        ConfirmDialog wdw = new ConfirmDialog();
        wdw.setWidth("60%");
        wdw.setContentSizeFull();

        Label lblContainer = new Label(ts.getTranslatedString("module.connectivity-manager.action.label.container"));
        Label lblLink = new Label(ts.getTranslatedString("module.connectivity-manager.action.label.link"));
        ComboBox<BusinessObjectLight> cmbLink = new ComboBox();
        cmbLink.setWidthFull();
        cmbLink.setPlaceholder(ts.getTranslatedString("module.connectivity-manager.action.placeholder.search-link"));
        cmbLink.setItemLabelGenerator(BusinessObjectLight::getName);
        cmbLink.setRenderer(new ObjectRenderer(mem, ts));
        cmbLink.setDataProvider(getLinkDataProvider());
        cmbLink.setClearButtonVisible(true);
        cmbLink.setValue((BusinessObjectLight) getSelectedLink());
        cmbLink.addValueChangeListener(valueChangeEvent -> setSelectedLink(valueChangeEvent.getValue()));
        
        FlexLayout lytContainer = new FlexLayout();
        lytContainer.setWidthFull();
        lytContainer.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        
        Consumer<BusinessObjectLight> consumerAddCmbContainer = container -> {
            ComboBox<BusinessObjectLight> cmbContainer = new ComboBox();
            cmbContainer.setWidthFull();
            cmbContainer.setPlaceholder(ts.getTranslatedString("module.connectivity-manager.action.placeholder.search-container"));
            cmbContainer.setItemLabelGenerator(BusinessObjectLight::getName);
            cmbContainer.setRenderer(new ObjectRenderer(mem, ts));
            cmbContainer.setDataProvider(getContainerDataProvider());
            cmbContainer.setClearButtonVisible(true);
            cmbContainer.setValue(container);
            cmbContainer.addValueChangeListener(listener);
            lytContainer.add(cmbContainer);
            lytContainer.setOrder((int) (cmbContainer.getChildren().count() - 1), cmbContainer);
        };
        listener = valueChangeEvent -> {
            BusinessObjectLight value = valueChangeEvent.getValue();
            ComboBox<BusinessObjectLight> cmbContainer = valueChangeEvent.getSource();
            
            int index = -1;
            for (int i = 0; i < lytContainer.getComponentCount(); i++) {
                Component component = lytContainer.getComponentAt(i);
                if (component.equals(cmbContainer)) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                List<ComboBox<BusinessObjectLight>> remove = new ArrayList();
                for (int i = index + 1; i < lytContainer.getChildren().count(); i++) {
                    selectedContainers.remove(((ComboBox<BusinessObjectLight>) lytContainer.getComponentAt(i)).getValue());
                    remove.add((ComboBox<BusinessObjectLight>) lytContainer.getComponentAt(i));
                }
                if (index == 0)
                    selectedContainers.clear();
                remove.forEach(item -> lytContainer.remove(item));
                
                if (value != null) {
                    if (index < selectedContainers.size() && selectedContainers.get(index) != null)
                        selectedContainers.set(index, value);
                    else
                        selectedContainers.add(value);
                    consumerAddCmbContainer.accept(null);
                }
                else {
                    if (index < selectedContainers.size() && selectedContainers.get(index) != null)
                        selectedContainers.remove(index);
                }
            }
            cmbLink.getDataProvider().refreshAll();
            cmbLink.setValue(null);
        };
        selectedContainers.forEach(selectedContainer -> consumerAddCmbContainer.accept(selectedContainer));
        consumerAddCmbContainer.accept(null);
        
        FlexLayout lytContainerContent = new FlexLayout(lblContainer, lytContainer);
        lytContainerContent.setWidthFull();
        lytContainerContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        
        FlexLayout lytLinkContent = new FlexLayout(lblLink, cmbLink);
        lytLinkContent.setWidthFull();
        lytLinkContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        
        VerticalLayout lytContent = new VerticalLayout(lytContainerContent, lytLinkContent);
        lytContent.setWidthFull();
        lytContent.setPadding(false);
        lytContent.setMargin(false);

        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), clickEvent -> {
            wdw.close();
        });
        Button btnSelectLink = new Button(
            ts.getTranslatedString("module.connectivity-manager.action.select-link"), 
            clickEvent -> {
                BusinessObjectLight selectedLink = (BusinessObjectLight) getSelectedLink();
                if (selectedLink != null) {
                    try {
                        if (bem.getSpecialAttributes(selectedLink.getClassName(), selectedLink.getId(), PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA, PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB).isEmpty()) {                            
                            wdw.close();
                            return;
                        } else {
                            BusinessObjectLight source = getConnection() != null && getConnection().getSource() != null ? getConnection().getSource().getSelectedObject() : null;
                            BusinessObjectLight target = getConnection() != null && getConnection().getTarget() != null ? getConnection().getTarget().getSelectedObject() : null;
                            List<BusinessObjectLight> endpointsA = bem.getSpecialAttribute(selectedLink.getClassName(), selectedLink.getId(), PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA);
                            List<BusinessObjectLight> endpointsB = bem.getSpecialAttribute(selectedLink.getClassName(), selectedLink.getId(), PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB);
                            if (endpointsA != null && endpointsB != null && source != null && target != null) {
                                if ((endpointsA.contains(source) && endpointsB.contains(target)) || (endpointsA.contains(target) && endpointsB.contains(source))) {
                                    wdw.close();
                                    return;
                                }
                            }
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.warning"), 
                                ts.getTranslatedString("module.connectivity-manager.action.connected-link"), 
                                AbstractNotification.NotificationType.WARNING, 
                                ts
                            ).open();
                        }
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, 
                            ts
                        ).open();
                        setSelectedLink(null);
                        wdw.close();
                    }
                } else {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.information"), 
                        ts.getTranslatedString("module.connectivity-manager.action.no-link-selected"), 
                        AbstractNotification.NotificationType.INFO, 
                        ts
                    ).open();
                }
            }
        );
        FlexLayout lytFooter = new FlexLayout(btnCancel, btnSelectLink);
        lytFooter.setSizeFull();
        lytFooter.setFlexGrow(1, btnCancel, btnSelectLink);

        wdw.setHeader(header);
        wdw.setContent(lytContent);
        wdw.setFooter(lytFooter);
        return wdw;
    }
    
    private DataProvider<BusinessObjectLight, String> getContainerDataProvider() {
        return DataProvider.fromFilteringCallbacks(
            query -> {
                String filter = query.getFilter().orElse(null);
                if (filter != null) {
                    if(selectedContainers.isEmpty()) {
                        List<BusinessObjectLight> containers = bem.getSuggestedObjectsWithFilter(
                            filter, Constants.CLASS_GENERICPHYSICALCONTAINER, -1
                        );
                        return containers.stream()
                            .sorted(Comparator.comparing(BusinessObjectLight::getName))
                            .filter(container -> container.getName().toLowerCase().contains(filter.toLowerCase()))
                            .skip(query.getOffset())
                            .limit(query.getLimit());
                        
                    } else {
                        BusinessObjectLight selectedContainer = selectedContainers.get(selectedContainers.size() - 1);
                        try {
                            List<BusinessObjectLight> containers = bem.getChildrenOfClassLight(
                                selectedContainer.getId(), selectedContainer.getClassName(), 
                                Constants.CLASS_GENERICPHYSICALCONTAINER, -1
                            );
                            return containers.stream()
                                .sorted(Comparator.comparing(BusinessObjectLight::getName))
                                .filter(container -> container.getName().toLowerCase().contains(filter.toLowerCase()))
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
                    }
                }
                return null;
            }, 
            query -> {
                String filter = query.getFilter().orElse(null);
                if (filter != null) {
                    if(selectedContainers.isEmpty()) {
                        List<BusinessObjectLight> containers = bem.getSuggestedObjectsWithFilter(
                            filter, Constants.CLASS_GENERICPHYSICALCONTAINER, -1
                        );
                        return (int) containers.stream()
                            .filter(container -> container.getName().toLowerCase().contains(filter.toLowerCase()))
                            .skip(query.getOffset())
                            .limit(query.getLimit())
                            .count();
                        
                    } else {
                        BusinessObjectLight selectedContainer = selectedContainers.get(selectedContainers.size() - 1);
                        try {
                            List<BusinessObjectLight> containers = bem.getChildrenOfClassLight(
                                selectedContainer.getId(), selectedContainer.getClassName(), 
                                Constants.CLASS_GENERICPHYSICALCONTAINER, -1
                            );
                            return (int) containers.stream()
                                .filter(container -> container.getName().toLowerCase().contains(filter.toLowerCase()))
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
                    }
                }
                return 0;
            }
        );
    }
    
    private DataProvider<BusinessObjectLight, String> getLinkDataProvider() {
        return DataProvider.fromFilteringCallbacks(
            query -> {
                String filter = query.getFilter().orElse(null);
                if (filter != null) {
                    if (!selectedContainers.isEmpty()) {
                        BusinessObjectLight selectedContainer = selectedContainers.get(selectedContainers.size() - 1);
                        try {
                            List<BusinessObjectLight> links = bem.getChildrenOfClassLight(
                                selectedContainer.getId(), 
                                selectedContainer.getClassName(), 
                                Constants.CLASS_GENERICPHYSICALLINK, 
                                -1
                            );
                            return links.stream()
                                .sorted(Comparator.comparing(BusinessObjectLight::getName))
                                .filter(link -> link.getName().toLowerCase().contains(filter.toLowerCase()))
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
                    }
                }
                return null;
            }, 
            query -> {
                String filter = query.getFilter().orElse(null);
                if (filter != null) {
                    if (!selectedContainers.isEmpty()) {
                        BusinessObjectLight selectedContainer = selectedContainers.get(selectedContainers.size() - 1);
                        try {
                            List<BusinessObjectLight> links = bem.getChildrenOfClassLight(
                                selectedContainer.getId(), 
                                selectedContainer.getClassName(), 
                                Constants.CLASS_GENERICPHYSICALLINK, 
                                -1
                            );
                            return (int) links.stream()
                                .filter(link -> link.getName().toLowerCase().contains(filter.toLowerCase()))
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
                    }
                }
                return 0;
            }
        );
    }
    
    @Override
    public boolean execute() {
        try {
            Connection connection = getConnection();
            if (connection != null && connection.getSource() != null && connection.getTarget() != null) {
                BusinessObjectLight source = connection.getSource().getSelectedObject();
                BusinessObjectLight target = connection.getTarget().getSelectedObject();
                BusinessObjectLight link = (BusinessObjectLight) getSelectedLink();
                
                if (source != null && target != null && link != null && 
                    mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, source.getClassName()) && 
                    mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, link.getClassName()) && 
                    mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, target.getClassName()) && 
                    bem.getSpecialAttributes(source.getClassName(), source.getId(), RELATIONSHIP_ENDPOINTA, RELATIONSHIP_ENDPOINTB).isEmpty() && 
                    bem.getSpecialAttributes(link.getClassName(), link.getId(), RELATIONSHIP_ENDPOINTA, RELATIONSHIP_ENDPOINTB).isEmpty() && 
                    bem.getSpecialAttributes(target.getClassName(), target.getId(), RELATIONSHIP_ENDPOINTA, RELATIONSHIP_ENDPOINTB).isEmpty()) {

                    bem.createSpecialRelationship(source.getClassName(), source.getId(), link.getClassName(), link.getId(), RELATIONSHIP_ENDPOINTA, true);
                    bem.createSpecialRelationship(target.getClassName(), target.getId(), link.getClassName(), link.getId(), RELATIONSHIP_ENDPOINTB, true);
                    return true;
                }
            }
            return true;
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
        return false;
    }
}
