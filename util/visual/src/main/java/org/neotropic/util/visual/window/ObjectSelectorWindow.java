/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.util.visual.window;

import com.vaadin.componentfactory.theme.EnhancedDialogVariant;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.parboiled.common.StringUtils;

/**
 * Window to select a object.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class ObjectSelectorWindow extends ConfirmDialog {
    private final BusinessObjectLight object;
    /**
     * Reference to the Metadata Entity Manager.
     */
    private final MetadataEntityManager mem;
    /**
     * Reference to the Translation Service.
     */
    private final TranslationService ts;
    /**
     * Callback to execute when select device.
     */
    private final Consumer<BusinessObjectLight> consumerSelectedObject;
    
    private boolean buttonOkEnabled = true;
    private Button btnOk;
    
    public ObjectSelectorWindow(MetadataEntityManager mem, TranslationService ts, Consumer<BusinessObjectLight> consumerSelectedObject) {
        this(null, mem, ts, consumerSelectedObject);
    }
    
    public ObjectSelectorWindow(BusinessObjectLight object, 
        MetadataEntityManager mem, TranslationService ts, 
        Consumer<BusinessObjectLight> consumerSelectedObject) {
        
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(consumerSelectedObject);
        
        this.object = object;
        this.mem = mem;
        this.ts = ts;
        this.consumerSelectedObject = consumerSelectedObject;
        
        addThemeVariants(EnhancedDialogVariant.SIZE_SMALL);
        setContentSizeFull();
        setDraggable(true);
        setModal(false);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(true);
        setResizable(true);
    }
    
    protected BusinessObjectLight getObject() {
        return object;
    }
    
    protected TranslationService getTranslationService() {
        return ts;
    }
    
    @Override
    public void open() {
        try {
            btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"));
            btnOk.setEnabled(false);
            
            ComboBox<BusinessObjectLight> cmbObject = new ComboBox();
            cmbObject.setPlaceholder(ts.getTranslatedString("module.visual-utilities.object-selector.search"));
            cmbObject.setItemLabelGenerator(BusinessObjectLight::getName);
            cmbObject.setRenderer(new ComponentRenderer<>(item -> {
                FormattedObjectDisplayNameSpan span = new FormattedObjectDisplayNameSpan(item, false, false, false, false);
                Label lblClass = new Label(!StringUtils.isEmpty(item.getClassDisplayName()) ? item.getClassDisplayName() : item.getClassName());
                lblClass.setClassName("text-secondary"); //NOI18N
                
                FlexLayout lytItem = new FlexLayout(span, lblClass);
                lytItem.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
                return lytItem;
            }));
            cmbObject.setWidthFull();
            cmbObject.setItems(getItems(object));
            
            List<BusinessObjectLight> values = new ArrayList();
            List<Component> valueComponents = new ArrayList();
            
            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), clickEvent -> close());
            Button btnClearAllFilters = new Button(ts.getTranslatedString("module.visual-utilities.object-selector.clear-all-filters"));
            
            HorizontalLayout lytButtons = new HorizontalLayout();
            lytButtons.setMargin(false);
            lytButtons.setPadding(false);
            lytButtons.add(btnCancel, btnClearAllFilters, btnOk);
            lytButtons.setFlexGrow(1, btnCancel, btnClearAllFilters, btnOk);
            
            FormLayout lytValues = new FormLayout();
            lytValues.setSizeFull();
            
            Function<BusinessObjectLight, Component> valueComponent = value -> {
                FormattedObjectDisplayNameSpan span = new FormattedObjectDisplayNameSpan(value, false, false, false, false);
                Label lblClass = new Label(!StringUtils.isEmpty(value.getClassDisplayName()) ? value.getClassDisplayName() : value.getClassName());
                lblClass.setClassName("text-secondary"); //NOI18N
                
                FlexLayout lytObject = new FlexLayout(span, lblClass);
                lytObject.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
                lytObject.setSizeFull();
                
                Icon iconClose = VaadinIcon.CLOSE_SMALL.create();
                iconClose.addClickListener(clickEvent -> {
                    try {
                        if (values.contains(value)) {
                            int index = values.indexOf(value);

                            for (int i = values.size() - 1; i >= index; i--) {
                                values.remove(i);
                                lytValues.remove(valueComponents.remove(i));
                            }
                            if (index - 1 >= 0)
                                cmbObject.setItems(getItems(values.get(index - 1)));
                            else
                                cmbObject.setItems(getItems(object));
                        }
                        if (values.isEmpty())
                            btnOk.setEnabled(false);
                        
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                });
                HorizontalLayout lytValue = new HorizontalLayout(lytObject, iconClose);
                lytValue.setSizeFull();
                lytValue.setMargin(false);
                lytValue.setPadding(false);
                
                return lytValue;
            };
            cmbObject.addValueChangeListener(valueChangeEvent -> {
                BusinessObjectLight value = valueChangeEvent.getValue();
                if (value != null) {
                    try {
                        values.add(value);
                        
                        Component component = valueComponent.apply(value);
                        valueComponents.add(component);
                        
                        lytValues.add(component);
                        
                        cmbObject.setItems(getItems(value));
                        btnOk.setEnabled(buttonOkEnabled);
                        
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                }
            });
            btnClearAllFilters.addClickListener(clickEvent -> {
                try {
                    values.clear();
                    valueComponents.clear();
                    lytValues.removeAll();
                    cmbObject.setItems(getItems(object));
                    btnOk.setEnabled(false);
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
            });
            btnOk.addClickListener(clickEvent -> {
                close();
                if (!values.isEmpty())
                    consumerSelectedObject.accept(values.get(values.size() - 1));
            });
            // Layout Section
            FlexLayout lytContent = new FlexLayout(cmbObject, lytValues);
            lytContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
            lytContent.setSizeFull();
            if (object != null)
                setHeader(String.format(ts.getTranslatedString("module.visual-utilities.object-selector.title.object"), object.getName()));
            else
                setHeader(ts.getTranslatedString("module.visual-utilities.object-selector.title"));
            setContent(lytContent);
            setFooter(lytButtons);
            
            super.open();
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
    }
    
    public void setButtonOkEnabled(boolean enabled) {
        buttonOkEnabled = enabled;
        if (btnOk != null)
            btnOk.setEnabled(enabled);
    }
    /**
     * List of items to set in the selector element.
     * @param selectedObject The last object selected.
     * @return List of items
     * @throws InventoryException
     */
    public abstract List<BusinessObjectLight> getItems(BusinessObjectLight selectedObject) throws InventoryException;
}