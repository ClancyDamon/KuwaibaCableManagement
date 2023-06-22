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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.Scroller;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.PropertySheet;

/**
 * Window to show an object property sheet
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowObjectProperties extends ConfirmDialog {
    private final BusinessObjectLight objectLight;
    private PropertySheet propertySheet;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final Consumer<BusinessObjectLight> consumerObjectChange;
    
    public WindowObjectProperties(BusinessObjectLight object, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts,
        Consumer<BusinessObjectLight> consumerObjectChange) {
        Objects.requireNonNull(object);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        
        this.objectLight = object;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.consumerObjectChange = consumerObjectChange;
        
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setDraggable(true);
        setModal(false);
        setResizable(true);
        setContentSizeFull();
        
        setWidth("25%");
        setHeight("75%");
    }

    @Override
    public void open() {
        try {
            BusinessObject object = bem.getObject(objectLight.getClassName(), objectLight.getId());
            propertySheet = new PropertySheet(ts);
            propertySheet.setItems(PropertyFactory.propertiesFromBusinessObject(object, ts, aem, mem));
            propertySheet.addPropertyValueChangedListener(property -> {
                try {
                    HashMap<String, String> attributes = new HashMap();
                    attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                    bem.updateObject(object.getClassName(), object.getId(), attributes);
                    propertySheet.getDataProvider().refreshItem(property);
                    
                    if (Constants.PROPERTY_NAME.equals(property.getName()) && consumerObjectChange != null) {
                        objectLight.setName(PropertyValueConverter.getAsStringToPersist(property));
                        consumerObjectChange.accept(objectLight);
                    }
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
            });
            Scroller scroller = new Scroller();
            scroller.setSizeFull();
            scroller.setContent(propertySheet);
            
            Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), clickEvent -> close());
            btnClose.setWidthFull();
            
            setHeader(ts.getTranslatedString("module.propertysheet.labels.header"));
            setContent(scroller);
            setFooter(btnClose);
            super.open();
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
    }
    
}
