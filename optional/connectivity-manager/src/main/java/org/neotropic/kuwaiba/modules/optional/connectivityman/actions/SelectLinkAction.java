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
import org.neotropic.util.visual.selectors.SelectorsUtils;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FilterDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Select an existing link
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class SelectLinkAction extends SelectContainerLinkAction {
    private final String header;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final List<BusinessObjectLight> businessObjects = new ArrayList();
    private ValueChangeListener<ComponentValueChangeEvent<ComboBox<BusinessObjectLight>, BusinessObjectLight>> listener;
    
    public SelectLinkAction(Connection connection, String header, ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        super(connection, header, bem, mem, ts);
        Objects.requireNonNull(header);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        this.header = header;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
    }

    @Override
    public ConfirmDialog getComponent() {
        ConfirmDialog wdw = new ConfirmDialog();
        wdw.setWidth("60%");
        wdw.setContentSizeFull();
        
        FlexLayout lytContent = new FlexLayout();
        lytContent.setWidthFull();
        lytContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        
        listener = valueChangeEvent -> {
            BusinessObjectLight value = valueChangeEvent.getValue();
            ComboBox<BusinessObjectLight> source = valueChangeEvent.getSource();
            int index = lytContent.indexOf(source.getParent().get());
            int size = lytContent.getComponentCount();
            
            if (value == null)
                lytContent.remove(lytContent.getComponentAt(size - 1));
            
            for (int i = size - 2; i > index; i--) {
                lytContent.remove(lytContent.getComponentAt(i));
                businessObjects.remove(i);
            }
            if (value != null) {
                businessObjects.add(value);
                try {
                    lytContent.add(addRow(value, null));
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
            } else
                businessObjects.remove(index);
        };
        try {
            for (int i = 0; i < businessObjects.size(); i++) {
                if (i == 0)
                    lytContent.add(addRow(null, businessObjects.get(i)));
                else
                    lytContent.add(addRow(businessObjects.get(i - 1), businessObjects.get(i)));
            }
            lytContent.add(addRow());
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
        
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), clickEvent -> {
            wdw.close();
        });
        Button btnSelectLink = new Button(ts.getTranslatedString("module.connectivity-manager.action.select-link"), clickEvent -> {
            BusinessObjectLight selectedLink = !businessObjects.isEmpty() ? businessObjects.get(businessObjects.size() - 1) : null;
            if (selectedLink != null) {
                try {
                    if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, selectedLink.getClassName())) {
                        HashMap<String, List<BusinessObjectLight>> endpoints = bem.getSpecialAttributes(selectedLink.getClassName(), selectedLink.getId(), PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA, PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB);
                        if (endpoints.isEmpty()) {
                            setSelectedLink(selectedLink);
                            wdw.close();
                        } else {
                            BusinessObjectLight source = getConnection() != null && getConnection().getSource() != null ? getConnection().getSource().getSelectedObject() : null;
                            BusinessObjectLight target = getConnection() != null && getConnection().getTarget() != null ? getConnection().getTarget().getSelectedObject() : null;
                            List<BusinessObjectLight> endpointsA = endpoints.get(PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA);
                            List<BusinessObjectLight> endpointsB = endpoints.get(PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB);
                            
                            if (endpointsA != null && endpointsB != null && source != null && target != null) {
                                if ((endpointsA.contains(source) && endpointsB.contains(target)) || (endpointsA.contains(target) && endpointsB.contains(source))) {
                                    setSelectedLink(selectedLink);
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
                    } else {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.information"), 
                            ts.getTranslatedString("module.connectivity-manager.action.no-link-selected"), 
                            AbstractNotification.NotificationType.INFO, 
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
                }
            }
        });
        FlexLayout lytFooter = new FlexLayout(btnCancel, btnSelectLink);
        lytFooter.setSizeFull();
        lytFooter.setFlexGrow(1, btnCancel, btnSelectLink);
        
        wdw.setHeader(header);
        wdw.setContent(lytContent);
        wdw.setFooter(lytFooter);
        return wdw;
    }
    
    private HorizontalLayout addRow() throws InventoryException {
        return addRow(null, null);
    }
    
    private HorizontalLayout addRow(BusinessObjectLight parent, BusinessObjectLight businessObject) throws InventoryException {
        HorizontalLayout lytRow = new HorizontalLayout();
        lytRow.setWidthFull();
        lytRow.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        lytRow.setMargin(false);
        lytRow.setPadding(false);
        
        DataProvider<BusinessObjectLight, String> dataProvider = getConnectionDataProvider();
        
        ComboBox<BusinessObjectLight> cmb = new ComboBox();
        cmb.setWidthFull();
        cmb.setPlaceholder(ts.getTranslatedString("module.connectivity-manager.action.placeholder.saarch-inventory-object"));
        cmb.setItemLabelGenerator(BusinessObjectLight::getName);
        cmb.setRenderer(new ObjectRenderer(mem, ts));
        cmb.setDataProvider(dataProvider);
        cmb.setClearButtonVisible(true);
        cmb.setValue(businessObject);
        cmb.addValueChangeListener(listener);
        lytRow.add(cmb);
        
        if (parent != null) {
            cmb.setWidth("70%");
            
            HashMap<String, Object> attrToFilter = new HashMap();
            attrToFilter.put(Constants.PROPERTY_ENABLED, true);
            
            ComboBox<FilterDefinition> cmbFilterDefinition = new ComboBox();
            cmbFilterDefinition.setClearButtonVisible(true);
            cmbFilterDefinition.setItemLabelGenerator(FilterDefinition::getName);
            cmbFilterDefinition.setItems(aem.getFilterDefinitionsForClass(parent.getClassName(), true, true, attrToFilter, -1, -1));
            cmbFilterDefinition.setPlaceholder(ts.getTranslatedString("module.connectivity-manager.cell-port-selector.filters"));
            cmbFilterDefinition.setRenderer(new ComponentRenderer<>(item -> {
                HorizontalLayout lytItem = new HorizontalLayout(
                    VaadinIcon.FILTER.create(), 
                    new Label(item.getName())
                );
                lytItem.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
                return lytItem;
            }));
            cmbFilterDefinition.addValueChangeListener(valueChangeEvent -> {
                FilterDefinition value = valueChangeEvent.getValue();
                if (value != null)
                    cmb.setDataProvider(SelectorsUtils.getFilterDataProvider(parent, value, ts));
                else
                    cmb.setDataProvider(dataProvider);
            });
            HorizontalLayout lytFilters = new HorizontalLayout(VaadinIcon.FILTER.create(), cmbFilterDefinition);
            lytFilters.setWidth("30%");
            lytFilters.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
            lytFilters.setMargin(false);
            lytFilters.setPadding(false);
            lytFilters.expand(cmbFilterDefinition);
            
            lytRow.add(lytFilters);
        }
        return lytRow;
    }
    
    private DataProvider<BusinessObjectLight, String> getConnectionDataProvider() {
        return DataProvider.fromFilteringCallbacks(
            query -> {
                String filter = query.getFilter().orElse(null);
                if (filter != null) {
                    if (businessObjects.isEmpty()) {
                        return bem.getSuggestedObjectsWithFilter(
                            filter, 
                            query.getOffset(), 
                            query.getLimit(), 
                            Constants.CLASS_VIEWABLEOBJECT, Constants.CLASS_GENERICPHYSICALCONNECTION
                        ).stream();
                    } else {
                        BusinessObjectLight connection = businessObjects.get(businessObjects.size() - 1);
                        return bem.getSuggestedChildrenWithFilter(
                            connection.getClassName(), 
                            connection.getId(), 
                            filter, 
                            false, 
                            query.getOffset(), 
                            query.getLimit(), 
                            Constants.CLASS_VIEWABLEOBJECT, Constants.CLASS_GENERICPHYSICALCONNECTION
                        ).stream();
                    }
                }
                return null;
            }, 
            query -> {
                String filter = query.getFilter().orElse(null);
                if (filter != null) {
                    if (businessObjects.isEmpty()) {
                        return bem.getSuggestedObjectsWithFilter(
                            filter, 
                            query.getOffset(), 
                            query.getLimit(), 
                            Constants.CLASS_VIEWABLEOBJECT, Constants.CLASS_GENERICPHYSICALCONNECTION
                        ).size();
                    } else {
                        BusinessObjectLight connection = businessObjects.get(businessObjects.size() - 1);
                        return bem.getSuggestedChildrenWithFilter(
                            connection.getClassName(), 
                            connection.getId(), 
                            filter, 
                            false, 
                            query.getOffset(), 
                            query.getLimit(), 
                            Constants.CLASS_VIEWABLEOBJECT, Constants.CLASS_GENERICPHYSICALCONNECTION
                        ).size();
                    }
                }
                return 0;
            });
    }
}