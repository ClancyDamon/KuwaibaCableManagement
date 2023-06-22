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

package org.neotropic.kuwaiba.modules.optional.physcon.widgets;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractObjectRelatedViewWidget;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.physcon.views.FiberSplitterView;
import org.neotropic.kuwaiba.modules.optional.physcon.views.PhysicalPathView;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A tooled component tat wraps a {@link FiberSplitterView}.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class PhysicalPathViewWidget extends AbstractObjectRelatedViewWidget<VerticalLayout> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the service that provides means to manipulate connections.
     */
    @Autowired
    private PhysicalConnectionsService physicalConnectionsService;
    
    private HashMap<Checkbox, BusinessObjectLight> mapRelateToService;
    
    @Override
    public String appliesTo() {
        return "GenericPort";
    }

    @Override
    public String getName() {
        return ts.getTranslatedString("module.visualization.physical-path-view-name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.visualization.physical-path-view-description");
    }

    @Override
    public String getVersion() {
        return "1.1";
    }
    
    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>"; //NOI18N
    }
    
    @Override
    public String getTitle() {
        return ts.getTranslatedString("module.navigation.widgets.object-dashboard.physical-path-view-name");
    }
    
    @Override
    public VerticalLayout build(BusinessObjectLight businessObject) throws InventoryException {
        mapRelateToService = new HashMap<>();
        PhysicalPathView physicalPathView =  new PhysicalPathView(businessObject, bem, aem, mem, ts, physicalConnectionsService);
        VerticalLayout pLayout = physicalPathView.getAsUiElement();
        pLayout.setWidth("70vw");
        
        Button btnOpenDlgRelateToService = new Button(ts.getTranslatedString("module.visualization.physical-path-view-relate-to-service"), new Icon(VaadinIcon.OPTIONS), evt -> {
            openDlgRelateToService(physicalPathView.getMapPathCircuit());
        });
        
        VerticalLayout lytContent = new VerticalLayout(btnOpenDlgRelateToService, pLayout);
        lytContent.setMargin(false);
        lytContent.setPadding(false);
        lytContent.setHeightFull();
        return lytContent;      
    }

    private void openDlgRelateToService(LinkedHashMap<BusinessObjectLight, BusinessObjectLight> mapPathCircuit) {
       LinkedHashMap<BusinessObjectLight, BusinessObjectLight> mapPath = mapPathCircuit;
        Grid<BusinessObjectLight> gridRelateToService = new Grid<>();
        gridRelateToService.setHeight("350px");
        gridRelateToService.setWidthFull();
        gridRelateToService.setItems(mapPath.keySet());
        gridRelateToService.addComponentColumn(item -> {
            
            Label lblPathItem = new Label(item.toString());
            Checkbox chkRelateItemToService = new Checkbox("");
            mapRelateToService.put(chkRelateItemToService, item);
            HorizontalLayout lytItemPath = new HorizontalLayout(lblPathItem, chkRelateItemToService);
            lytItemPath.setFlexGrow(1, lblPathItem);
            lytItemPath.setAlignItems(FlexComponent.Alignment.CENTER);
            lytItemPath.setMargin(false);
            lytItemPath.setPadding(false);
            lytItemPath.setWidth("250px");
            
            BusinessObjectLight parentDevice = mapPath.get(item);        
            HorizontalLayout lytItemDevice = new HorizontalLayout();
            lytItemDevice.setMargin(false);
            lytItemDevice.setAlignItems(FlexComponent.Alignment.CENTER);
            lytItemDevice.setPadding(false);
            if (parentDevice != null) {            
                Checkbox chkRelateItemDeviceToService = new Checkbox("");
                Label lblParentItem = new Label(parentDevice.toString());
                lytItemDevice.add(lblParentItem, chkRelateItemDeviceToService);
                mapRelateToService.put(chkRelateItemDeviceToService, parentDevice);
                lytItemDevice.setFlexGrow(1, lblParentItem);
            }
            HorizontalLayout lytItem = new HorizontalLayout(lytItemPath, lytItemDevice);
            lytItem.setFlexGrow(1, lytItemDevice);
            lytItem.setAlignItems(FlexComponent.Alignment.CENTER);
            return lytItem;
        }).setHeader(ts.getTranslatedString("module.visualization.physical-path-view-relate-to-service"));
       
        List<BusinessObjectLight> services = bem.getSuggestedObjectsWithFilter("",  Constants.CLASS_GENERICSERVICE, -1);
        ComboBox<BusinessObjectLight> cbxServices = new ComboBox<>(ts.getTranslatedString("module.visualization.physical-path-view-service"));
        cbxServices.setItems(services);
        cbxServices.setItemLabelGenerator(BusinessObjectLight::getName);
        cbxServices.setAllowCustomValue(false);
        cbxServices.setClearButtonVisible(true);
        cbxServices.setRequired(true);
        cbxServices.setWidth("300px");
        
        VerticalLayout lytRelateToService = new VerticalLayout(cbxServices, gridRelateToService);
        lytRelateToService.setWidth("600px");
        lytRelateToService.setPadding(false);
        EnhancedDialog dlgRelateToService = new EnhancedDialog();
        Button btnRelateSelection = new Button(ts.getTranslatedString("module.visualization.physical-path-view-relate-selection-service"), 
                new Icon(VaadinIcon.COMPRESS_SQUARE), evt -> {
            List<BusinessObjectLight> objectsToRelate = new ArrayList<>();
            for (Checkbox chk : mapRelateToService.keySet())
                if (chk.getValue())
                    objectsToRelate.add(mapRelateToService.get(chk));
            
            if (objectsToRelate.isEmpty()) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.visualization.physical-path-view-no-selection-to-relate"), 
                            AbstractNotification.NotificationType.WARNING, ts).open();
                return;            
            }
                       
            if (cbxServices.getValue() != null) {
                for (BusinessObjectLight ol : objectsToRelate) {
                    try {
                        bem.createSpecialRelationship(cbxServices.getValue().getClassName(), cbxServices.getValue().getId(),
                                ol.getClassName(), ol.getId(), "uses", true); //NOI18N
                    } catch (BusinessObjectNotFoundException | OperationNotPermittedException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                        Logger.getLogger(PhysicalPathViewWidget.class.getName()).log(Level.SEVERE, null, ex);
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                                AbstractNotification.NotificationType.ERROR, ts).open();
                        return;
                    }
                }
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.visualization.physical-path-view-selection-related-successfully"),
                        AbstractNotification.NotificationType.INFO, ts).open();
                dlgRelateToService.close();
            } else
                new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.visualization.physical-path-view-select-the-service"),
                        AbstractNotification.NotificationType.WARNING, ts).open();
        });
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), evt -> {
            dlgRelateToService.close();
        });
        dlgRelateToService.setContent(lytRelateToService);
        dlgRelateToService.setFooter(new HorizontalLayout(btnCancel, btnRelateSelection));
        dlgRelateToService.setHeader(new BoldLabel(ts.getTranslatedString("module.visualization.physical-path-view-relate-to-service")));
        dlgRelateToService.open();     
        
    }
    
}
