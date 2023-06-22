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
package com.neotropic.kuwaiba.modules.commercial.ospman.widgets;

import org.neotropic.util.visual.properties.BusinessObjectProperty;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.OspConstants;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.UnitOfLength;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.BooleanProperty;
import org.neotropic.util.visual.properties.DoubleProperty;
import org.neotropic.util.visual.properties.IntegerProperty;
import org.neotropic.util.visual.properties.ObjectProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.properties.StringProperty;

/**
 * Set of panels to work in the Outside Plant View.
 * The view properties panel is used to change the view properties.
 * The map properties panel is used to change the map properties.
 * The business object properties panel is used to change the business object properties.
 * The help panel is used to show the help.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OutsidePlantAccordion extends VerticalLayout {
    private final TranslationService ts;
    private final Accordion accordion;
    private final ViewPropertySheet propertySheetView;
    private final MapPropertySheet propertySheetMap;
    private final BusinessObjectPropertySheet propertySheetBusinessObject;
    private final NewNodePanelContent newNodePanelContent;
    
    public OutsidePlantAccordion(ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        this.ts = ts;
        
        setMargin(false);
        setPadding(false);
        setSpacing(false);
        
        propertySheetView = new ViewPropertySheet(aem, bem, mem, ts);
        propertySheetMap = new MapPropertySheet(ts);
        propertySheetBusinessObject = new BusinessObjectPropertySheet(aem, bem, mem, ts);
        newNodePanelContent = new NewNodePanelContent(aem, mem, ts);
        
        accordion = new Accordion();
        accordion.setSizeFull();
        addViewPropertiesPanel();
        addMapPropertiesPanel();
        addObjectPropertiesPanel();
        addNewNodePanel();
        addHelpPanel();
        add(accordion);
        getStyle().set("margin-top", "1px");
        getStyle().set("margin-left", "9px");
    }
    
    public ViewPropertySheet getViewPropertySheet() {
        return propertySheetView;
    }
    
    public MapPropertySheet getMapPropertySheet() {
        return propertySheetMap;
    }
    
    public BusinessObjectPropertySheet getBusinessObjectPropertySheet() {
        return propertySheetBusinessObject;
    }
    
    public NewNodePanelContent getNewNodePanelContent() {
        return newNodePanelContent;
    }
    
    private void addViewPropertiesPanel() {
        AccordionPanel panelViewProperties = new AccordionPanel(ts.getTranslatedString("module.ospman.view-properties"), propertySheetView);
        accordion.add(panelViewProperties);
    }
    
    private void addMapPropertiesPanel() {
        AccordionPanel panelMapProperties = new AccordionPanel(ts.getTranslatedString("module.ospman.map-properties"), propertySheetMap);
        accordion.add(panelMapProperties);
    }
    
    private void addObjectPropertiesPanel() {
        AccordionPanel panelObjectProperties = new AccordionPanel(ts.getTranslatedString("module.ospman.object-properties"), propertySheetBusinessObject);
        accordion.add(panelObjectProperties);
    }
    
    private void addHelpPanel() {
        AccordionPanel panelHelp = new AccordionPanel(ts.getTranslatedString("module.general.labels.help"), new VerticalLayout());
        accordion.add(panelHelp);
    }
    
    private void addNewNodePanel() {
        AccordionPanel panelNewNode = new AccordionPanel(ts.getTranslatedString("module.ospman.tools.new-node.new-node"), newNodePanelContent);
        accordion.add(panelNewNode);
    }
    
    public class ViewPropertySheet extends PropertySheet {
        private final StringProperty propertyName;
        private final StringProperty propertyDescription;
        private final BooleanProperty propertyEnableUpdateObjectPosition;
        private final BusinessObjectProperty propertyDefaultParent;
        
        private BusinessObjectLight defaultParent;
        
        public ViewPropertySheet(ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
            super(ts);
            propertyName = new StringProperty(
                Constants.PROPERTY_NAME, 
                ts.getTranslatedString("module.general.property.name"), 
                "", null, ts);
            
            propertyDescription = new StringProperty(
                Constants.PROPERTY_DESCRIPTION, 
                ts.getTranslatedString("module.general.property.description"), 
                "", null, ts);
            
            propertyEnableUpdateObjectPosition = new BooleanProperty(
                OspConstants.MAP_PROPERTY_SYNC_GEO_POSITION, 
                ts.getTranslatedString("module.general.property.sync-geo-position"), 
                "", null, ts);
            
            propertyDefaultParent = new BusinessObjectProperty(
                OspConstants.MAP_PROPERTY_DEFAULT_PARENT, 
                ts.getTranslatedString("module.general.property.default-parent"), 
                "", null, aem, bem, mem, ts
            );
        }
        
        public BusinessObjectLight getDefaultParent() {
            return defaultParent;
        }
        
        public void setDefaultParent(BusinessObjectLight defaultParent) {
            this.defaultParent = defaultParent;
        }
        
        public void setView(ViewObject view, boolean enableUpdateObjectPosition) {
            if (view != null) {
                propertyName.setValue(view.getName());
                propertyDescription.setValue(view.getDescription());
                propertyEnableUpdateObjectPosition.setValue(null);
                propertyEnableUpdateObjectPosition.setValue(enableUpdateObjectPosition);
                propertyDefaultParent.setValue(defaultParent);
                setItems(Arrays.asList(propertyName, propertyDescription, propertyEnableUpdateObjectPosition, propertyDefaultParent));
            } else {
                propertyName.setValue(null);
                propertyDescription.setValue(null);
                propertyEnableUpdateObjectPosition.setValue(null);
                propertyDefaultParent.setValue(defaultParent);
                clear();
            }
        }

        @Override
        public void clear() {
            super.clear();
            defaultParent = null;
        }
    }
    
    public class MapPropertySheet extends PropertySheet {
        private final DoubleProperty propertyCenterLatitude;
        private final DoubleProperty propertyCenterLongitude;
        private final IntegerProperty propertyZoom;
        private final ObjectProperty propertyMapTypeId;
        private final ObjectProperty propertyUnitOfLength;
        private final BooleanProperty propertyComputeEdgesLength;
        
        public MapPropertySheet(TranslationService ts) {
            super(ts);
            propertyCenterLatitude = new DoubleProperty(
                OspConstants.MAP_PROPERTY_CENTER_LATITUDE, 
                ts.getTranslatedString("module.ospman.property.center-latitude"), 
                "", null, ts);
            propertyCenterLatitude.setReadOnly(true);
            
            propertyCenterLongitude = new DoubleProperty(
                OspConstants.MAP_PROPERTY_CENTER_LONGITUDE, 
                ts.getTranslatedString("module.ospman.property.center-longitude"), 
                "", null, ts);
            propertyCenterLongitude.setReadOnly(true);
            
            propertyZoom = new IntegerProperty(
                OspConstants.MAP_PROPERTY_ZOOM,
                ts.getTranslatedString("module.ospman.property.zooom"), 
                "", null, ts);
            propertyZoom.setReadOnly(true);
            
            propertyMapTypeId = new ObjectProperty(
                OspConstants.MAP_PROPERTY_TYPE_ID,
                ts.getTranslatedString("module.ospman.property.map.type-id"), "", 
                null, 
                Collections.EMPTY_LIST,
                "", "", ts
            );
            
            propertyUnitOfLength = new ObjectProperty(
                OspConstants.MAP_PROPERTY_UNIT_OF_LENGTH,
                ts.getTranslatedString("module.ospman.property.unit-of-length"), "",
                null,
                Arrays.asList(UnitOfLength.getUnits().toArray()),
                "", "", ts
            );
            propertyUnitOfLength.setComponentRenderer(new ComponentRenderer<>( item -> {
                return new Label(item instanceof UnitOfLength ? UnitOfLength.getTranslatedString((UnitOfLength) item, ts) : "");
            }));
            
            propertyComputeEdgesLength = new BooleanProperty(
                OspConstants.MAP_PROPERTY_COMPUTE_EDGES_LENGTH, 
                ts.getTranslatedString("module.ospman.property.compute-edges-length"), 
                "", null, ts
            );
            
            setItems(Arrays.asList(
                propertyCenterLatitude, 
                propertyCenterLongitude, 
                propertyZoom, 
                propertyMapTypeId,
                propertyUnitOfLength, 
                propertyComputeEdgesLength
            ));
        }
        
        public void setPropertyCenterLatitude(double centerLatitude) {
            propertyCenterLatitude.setValue(centerLatitude);
            getDataProvider().refreshItem(propertyCenterLatitude);
        }
        
        public void setPropertyCenterLongitude(double centerLongitude) {
            propertyCenterLongitude.setValue(centerLongitude);
            getDataProvider().refreshItem(propertyCenterLongitude);
        }
        
        public void setPropertyZoom(double zoom) {
            propertyZoom.setValue((int) zoom);
            getDataProvider().refreshItem(propertyZoom);
        }
        
        public void setPropertyMapTypeId(String mapTypeId) {
            if (mapTypeId != null) {
                propertyMapTypeId.setValue(mapTypeId);
            } else {
                propertyMapTypeId.setValue(ts.getTranslatedString("module.propertysheet.property-not-supported"));
            }
            getDataProvider().refreshItem(propertyMapTypeId);
        }
        
        public void setPropertyUnitOfLength(UnitOfLength unitOfLength) {
            propertyUnitOfLength.setValue(unitOfLength);
            getDataProvider().refreshItem(propertyUnitOfLength);
        }
        
        public void setPropertyComputeEdgesLength(boolean computeEdgesLength) {
            propertyComputeEdgesLength.setValue(computeEdgesLength);
            getDataProvider().refreshItem(propertyComputeEdgesLength);
        }
        
        public void setPropertyMapTypeIds(List<String> mapTypeIds) {
            if (!mapTypeIds.isEmpty()) {
                propertyMapTypeId.setItems(mapTypeIds);
            } else {
                propertyMapTypeId.setItems(Arrays.asList(ts.getTranslatedString("module.propertysheet.property-not-supported")));
                propertyMapTypeId.setReadOnly(true);
            }
        }
    }
    
    public class BusinessObjectPropertySheet extends PropertySheet {
        private final ApplicationEntityManager aem;
        private final BusinessEntityManager bem;
        private final MetadataEntityManager mem;
        private BusinessObjectLight businessObject;
        
        public BusinessObjectPropertySheet(ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
            super(ts);
            this.aem = aem;
            this.bem = bem;
            this.mem = mem;
        }
        
        public BusinessObjectLight getBusinessObject() {
            return businessObject;
        }
        
        public void setBusinessObject(BusinessObjectLight businessObject) throws InventoryException {
            this.businessObject = businessObject;
            
            if (businessObject != null) {
                BusinessObject object = bem.getObject(businessObject.getClassName(), businessObject.getId());
                setItems(PropertyFactory.propertiesFromBusinessObject(object, ts, aem, mem));
            }
            else
                clear();
        }
    }
    
    public class NewNodePanelContent extends Scroller {
        private final ApplicationEntityManager aem;
        private final MetadataEntityManager mem;
        private final ComboBox<ClassMetadataLight> cmbNewNodeClass;
        private final Label lblHelp;
        private final Grid<TemplateObjectLight> tblNodeTemplate;
        
        public NewNodePanelContent(ApplicationEntityManager aem, MetadataEntityManager mem, TranslationService ts) {
            Objects.requireNonNull(aem);
            Objects.requireNonNull(mem);
            Objects.requireNonNull(ts);
            this.aem = aem;
            this.mem = mem;
            
            cmbNewNodeClass = new ComboBox(ts.getTranslatedString("module.ospman.tools.new-node.select-new-node-class"));
            cmbNewNodeClass.setWidthFull();
            cmbNewNodeClass.setClearButtonVisible(true);
            cmbNewNodeClass.setDataProvider(getNodeClassesDataProvider());
            
            lblHelp = new Label();
            lblHelp.setClassName("text-secondary"); //NOI18N
            
            tblNodeTemplate = new Grid();
            tblNodeTemplate.setWidthFull();
            tblNodeTemplate.setHeight("150px");
            tblNodeTemplate.setHeightByRows(false);
            
            tblNodeTemplate.addColumn(template -> {
                try {
                    ClassMetadata templateClass = mem.getClass(template.getClassName());
                    return String.format("%s [%s]", template.getName(), templateClass.toString());
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                    return template.toString();
                }
            });
            tblNodeTemplate.setSelectionMode(Grid.SelectionMode.NONE);
            tblNodeTemplate.setRowsDraggable(true);
            
            cmbNewNodeClass.addValueChangeListener(valueChangeEvent -> {
                ClassMetadataLight value = valueChangeEvent.getValue();
                if (value != null) {
                    lblHelp.setText(ts.getTranslatedString("module.ospman.tools.new-node.select-template"));
                    ListDataProvider<TemplateObjectLight> dataProvider = getTemplateDataProvider(value);
                    tblNodeTemplate.setDataProvider(dataProvider);
                    
                    tblNodeTemplate.setVisible(true);
                } else {
                    lblHelp.setText("");
                    tblNodeTemplate.setItems();
                    
                    tblNodeTemplate.setVisible(false);
                }
            });
            FlexLayout lytContent = new FlexLayout();
            lytContent.setSizeFull();
            lytContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
            lytContent.add(cmbNewNodeClass, lblHelp, tblNodeTemplate);
            setWidthFull();
            setContent(lytContent);
        }
        
        public void clear() {
            cmbNewNodeClass.setValue(null);
            lblHelp.setText("");
            tblNodeTemplate.setVisible(false);
        }
        
        private DataProvider<ClassMetadataLight, String> getNodeClassesDataProvider() {
            return DataProvider.fromFilteringCallbacks(
                query -> {
                    try {
                        List<ClassMetadataLight> subclasses = mem.getSubClassesLight(
                            Constants.CLASS_GENERICPHYSICALNODE, 
                            false, 
                            false
                        );
                        String filter = query.getFilter().orElse("");
                        return subclasses.stream()
                            .sorted(Comparator.comparing(ClassMetadataLight::getName))
                            .filter(subclass -> subclass.toString().toLowerCase().contains(filter.toLowerCase()))
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
                        List<ClassMetadataLight> subclasses = mem.getSubClassesLight(
                            Constants.CLASS_GENERICPHYSICALNODE, 
                            false, 
                            false
                        );
                        String filter = query.getFilter().orElse("");
                        return (int) subclasses.stream()
                            .filter(subclass -> subclass.toString().toLowerCase().contains(filter.toLowerCase()))
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
        
        public ListDataProvider<TemplateObjectLight> getTemplateDataProvider(ClassMetadataLight templateClass) {
            try {
                return new ListDataProvider(aem.getTemplatesForClass(templateClass.getName()));
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
            return null;
        }
    }
}
