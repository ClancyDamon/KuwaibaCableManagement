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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.selectors.BusinessObjectSelector;

/**
 * Action to create a new link using a container template.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class NewLinkFromContainerTemplateAction extends AbstractLinkConnectivityAction {
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    
    private final String header;
    private BusinessObjectLight endpointA;
    private BusinessObjectLight endpointB;
    private BusinessObjectLight tmpEndpointA;
    private BusinessObjectLight tmpEndpointB;
    private ClassMetadataLight templateClass;
    private String containerName;
    private TemplateObjectLight templateElement;
    private final List<TemplateObjectLight> templateElements = new ArrayList();
    private BiConsumer<TemplateObjectLight, TemplateObjectLight> consumerAddChild;
    
    public NewLinkFromContainerTemplateAction(Connection connection, String header, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, 
        TranslationService ts) {
        
        super(connection);
        Objects.requireNonNull(header);
        this.header = header;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
    }

    @Override
    public List<TemplateObjectLight> getSelectedObjects() {
        return templateElements;
    }

    @Override
    public String getName() {
        return containerName;
    }
    
    private FlexLayout getContent() {
        TextField txtEndpointA = new TextField(ts.getTranslatedString("module.connectivity-manager.action-6.select-container-endpoint-a"));
        txtEndpointA.setRequired(true);
        txtEndpointA.setRequiredIndicatorVisible(true);
        txtEndpointA.setClearButtonVisible(true);
        txtEndpointA.getElement().addEventListener("click", domEvent -> { //NOI18N
            BusinessObjectSelector endpointASelector = new BusinessObjectSelector(ts.getTranslatedString("module.connectivity-manager.action-6.select-container-endpoint-a"), aem, bem, mem, ts);
            endpointASelector.addSelectedObjectChangeListener(
                selectedObjectChangeEvent -> tmpEndpointA = selectedObjectChangeEvent.getSelectedObject()
            );
            ConfirmDialog wdw = new ConfirmDialog();
            wdw.setWidth("60%");
            wdw.setCloseOnOutsideClick(false);
            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), clickEvent -> wdw.close());
            Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"), clickEvent -> {
                if (tmpEndpointA != null) {
                    endpointA = tmpEndpointA;
                    txtEndpointA.setValue(endpointA.toString());
                    tmpEndpointA = null;
                } else {
                    endpointA = null;
                    txtEndpointA.clear();
                }
                wdw.close();
            });
            HorizontalLayout lytFooter = new HorizontalLayout(btnCancel, btnOk);
            lytFooter.setWidthFull();
            lytFooter.setFlexGrow(1, btnCancel, btnOk);
            
            wdw.setHeader(ts.getTranslatedString("module.connectivity-manager.action-6.select-container-endpoint-a"));
            wdw.setContent(endpointASelector);
            wdw.setFooter(lytFooter);
            wdw.open();
        });
        if (endpointA != null)
            txtEndpointA.setValue(endpointA.toString());
        
        TextField txtEndpointB = new TextField(ts.getTranslatedString("module.connectivity-manager.action-6.select-container-endpoint-b"));
        txtEndpointB.setRequired(true);
        txtEndpointB.setRequiredIndicatorVisible(true);
        txtEndpointB.setClearButtonVisible(true);
        txtEndpointB.getElement().addEventListener("click", domEvent -> { //NOI18N
            BusinessObjectSelector endpointBSelector = new BusinessObjectSelector(ts.getTranslatedString("module.connectivity-manager.action-6.select-container-endpoint-b"), aem, bem, mem, ts);
            endpointBSelector.addSelectedObjectChangeListener(
                selectedObjectChangeEvent -> tmpEndpointB = selectedObjectChangeEvent.getSelectedObject()
            );
            ConfirmDialog wdw = new ConfirmDialog();
            wdw.setWidth("60%");
            wdw.setCloseOnOutsideClick(false);
            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), clickEvent -> wdw.close());
            Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"), clickEvent -> {
                if (tmpEndpointB != null) {
                    endpointB = tmpEndpointB;
                    txtEndpointB.setValue(endpointB.toString());
                    tmpEndpointB = null;
                } else {
                    endpointB = null;
                    txtEndpointB.clear();
                }
                wdw.close();
            });
            HorizontalLayout lytFooter = new HorizontalLayout(btnCancel, btnOk);
            lytFooter.setWidthFull();
            lytFooter.setFlexGrow(1, btnCancel, btnOk);
            
            wdw.setHeader(ts.getTranslatedString("module.connectivity-manager.action-6.select-container-endpoint-b"));
            wdw.setContent(endpointBSelector);
            wdw.setFooter(lytFooter);
            wdw.open();
        });
        if (endpointB != null)
            txtEndpointB.setValue(endpointB.toString());
        
        HorizontalLayout lytEndpoints = new HorizontalLayout(txtEndpointA, txtEndpointB);
        lytEndpoints.setWidthFull();
        lytEndpoints.setFlexGrow(1, txtEndpointA, txtEndpointB);
        
        ComboBox<ClassMetadataLight> cmbClass = new ComboBox(ts.getTranslatedString("module.connectivity-manager.action-6.select-template-class"));
        cmbClass.setWidthFull();
        cmbClass.setRequired(true);
        cmbClass.setRequiredIndicatorVisible(true);
        cmbClass.setClearButtonVisible(true);
        cmbClass.setDataProvider(getClassesDataProvider());
        
        TextField txtContainerName = new TextField(ts.getTranslatedString("module.connectivity-manager.action-6.container-name"));
        txtContainerName.setRequired(true);
        txtContainerName.setRequiredIndicatorVisible(true);
        txtContainerName.setClearButtonVisible(true);
        if (containerName != null)
            txtContainerName.setValue(containerName);
        txtContainerName.addValueChangeListener(valueChangeEvent -> containerName = valueChangeEvent.getValue());
        
        ComboBox<TemplateObjectLight> cmbTemplateElement = new ComboBox(ts.getTranslatedString("module.connectivity-manager.action-6.select-container-template"));
        cmbTemplateElement.setRequired(true);
        cmbTemplateElement.setRequiredIndicatorVisible(true);
        cmbTemplateElement.setClearButtonVisible(true);
        cmbTemplateElement.setEnabled(false);
        
        HorizontalLayout lytTemplateElement = new HorizontalLayout(txtContainerName, cmbTemplateElement);
        lytTemplateElement.setWidthFull();
        lytTemplateElement.setFlexGrow(1, txtContainerName, cmbTemplateElement);
        lytTemplateElement.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        
        FlexLayout lytChildren = new FlexLayout();
        lytChildren.setWidthFull();
        lytChildren.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        
        List<ComboBox<TemplateObjectLight>> cmbChildren = new ArrayList();
        
        consumerAddChild = (parent, child) -> {
            ComboBox<TemplateObjectLight> cmbChild = new ComboBox(ts.getTranslatedString("module.connectivity-manager.action-6.select-template-object"));
            cmbChild.setWidthFull();
            cmbChild.setRequired(true);
            cmbChild.setRequiredIndicatorVisible(true);
            cmbChild.setClearButtonVisible(true);
            cmbChild.setDataProvider(getTemplateChildrenDataProvider(parent));
            cmbChild.setValue(child);
            cmbChild.addValueChangeListener(valueChangeEvent -> {
                TemplateObjectLight value = valueChangeEvent.getValue();
                
                int index = cmbChildren.indexOf(cmbChild);
                
                List<ComboBox> cmbChildrenToRemove = new ArrayList();
                for (int i = index + 1; i < cmbChildren.size(); i++) {
                    cmbChildrenToRemove.add(cmbChildren.get(i));
                    lytChildren.remove(cmbChildren.get(i));
                }
                cmbChildren.removeAll(cmbChildrenToRemove);
                if (index < templateElements.size())
                    templateElements.remove(index);
                
                if (value != null) {
                    templateElements.add(value);
                    consumerAddChild.accept(value, null);
                }
            });
            cmbChildren.add(cmbChild);
            lytChildren.add(cmbChild);
        };
        
        cmbTemplateElement.addValueChangeListener(valueChangeEvent -> {
            boolean isFromClient = valueChangeEvent.isFromClient();
            templateElement = valueChangeEvent.getValue();
            if (isFromClient) {
                templateElements.clear();
                lytChildren.removeAll();
            }
            if (templateElement != null) {
                if (isFromClient)
                    consumerAddChild.accept(templateElement, null);
                else {
                    for (int i = 0; i < templateElements.size(); i++) {
                        if (i == 0) {
                            consumerAddChild.accept(templateElement, templateElements.get(i));
                        } else if (i == templateElements.size() - 1) {
                            consumerAddChild.accept(templateElements.get(i - 1), templateElements.get(i));
                            consumerAddChild.accept(templateElements.get(i), null);
                        }
                        else
                            consumerAddChild.accept(templateElements.get(i - 1), templateElements.get(i));
                    }
                }
            } else {
                templateElements.clear();
                lytChildren.removeAll();
            }
        });
        cmbClass.addValueChangeListener(valueChangeEvent -> {
            boolean isFromClient = valueChangeEvent.isFromClient();
            templateClass = valueChangeEvent.getValue();
            
            if (templateClass != null)
                cmbTemplateElement.setDataProvider(getTemplateDataProvider());
            if (isFromClient)
                cmbTemplateElement.setValue(null);
            else
                cmbTemplateElement.setValue(templateElement);
            cmbTemplateElement.setEnabled(templateClass != null);
        });
        cmbClass.setValue(templateClass);
        
        FlexLayout content = new FlexLayout(lytEndpoints, cmbClass, lytTemplateElement, lytChildren);
        content.setWidthFull();
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        return content;
    }
    
    private DataProvider<ClassMetadataLight, String> getClassesDataProvider() {
        return DataProvider.fromFilteringCallbacks(
            query -> {
                String filter = query.getFilter().orElse("");
                try {
                    List<ClassMetadataLight> subclasses = mem.getSubClassesLight(
                        Constants.CLASS_GENERICPHYSICALCONTAINER, 
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
                        Constants.CLASS_GENERICPHYSICALCONTAINER, 
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
    
    private DataProvider<TemplateObjectLight, String> getTemplateDataProvider() {
        return DataProvider.fromFilteringCallbacks(
            query -> {
                String filter = query.getFilter().orElse("");
                try {
                    List<TemplateObjectLight> templates = aem.getTemplatesForClass(templateClass.getName());
                    return templates.stream()
                        .sorted(Comparator.comparing(TemplateObjectLight::getName))
                        .filter(template -> template.getName().toLowerCase().contains(filter.toLowerCase()))
                        .skip(query.getOffset())
                        .limit(query.getLimit());
                } catch (MetadataObjectNotFoundException ex) {
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
                    List<TemplateObjectLight> templates = aem.getTemplatesForClass(templateClass.getName());
                    return (int) templates.stream()
                        .filter(template -> template.getName().toLowerCase().contains(filter.toLowerCase()))
                        .skip(query.getOffset())
                        .limit(query.getLimit())
                        .count();
                } catch (MetadataObjectNotFoundException ex) {
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
    
    private DataProvider<TemplateObjectLight, String> getTemplateChildrenDataProvider(TemplateObjectLight parent) {
        return DataProvider.fromFilteringCallbacks(query -> {
                String filter = query.getFilter().orElse("");
                
                List<TemplateObjectLight> children = aem.getTemplateSpecialElementChildren(parent.getClassName(), parent.getId());
                return children.stream()
                    .sorted(Comparator.comparing(TemplateObjectLight::getName))
                    .filter(child -> child.getName().toLowerCase().contains(filter.toLowerCase()))
                    .skip(query.getOffset())
                    .limit(query.getLimit());
            }, 
            query -> {
                String filter = query.getFilter().orElse("");
                
                List<TemplateObjectLight> children = aem.getTemplateSpecialElementChildren(parent.getClassName(), parent.getId());
                return (int) children.stream()
                    .filter(child -> child.getName().toLowerCase().contains(filter.toLowerCase()))
                    .skip(query.getOffset())
                    .limit(query.getLimit())
                    .count();
            }
        );
    }
    
    @Override
    public Component getComponent() {
        ConfirmDialog wdw = new ConfirmDialog();
        wdw.setWidth("70%");
        wdw.setCloseOnOutsideClick(false);
        
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), clickEvent -> wdw.close());
        Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"), clickEvent -> {
            boolean close = true;
            if (endpointA == null) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.warning"), 
                    ts.getTranslatedString("module.connectivity-manager.action-6.no-endpoint-a"), 
                    AbstractNotification.NotificationType.WARNING, 
                    ts
                ).open();
                close = false;
            }
            if (endpointB == null) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.warning"), 
                    ts.getTranslatedString("module.connectivity-manager.action-6.no-endpoint-b"), 
                    AbstractNotification.NotificationType.WARNING, 
                    ts
                ).open();
                close = false;
            }
            if (containerName == null || containerName.isEmpty()) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.warning"), 
                    ts.getTranslatedString("module.connectivity-manager.action-6.no-container-name"), 
                    AbstractNotification.NotificationType.WARNING, 
                    ts
                ).open();
                close = false;
            }
            try {
                if (templateClass == null) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.warning"), 
                        ts.getTranslatedString("module.connectivity-manager.action-6.no-template-class"), 
                        AbstractNotification.NotificationType.WARNING, 
                        ts
                    ).open();
                    close = false;
                } else if (templateElement == null) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.warning"), 
                        ts.getTranslatedString("module.connectivity-manager.action-6.no-template"), 
                        AbstractNotification.NotificationType.WARNING, 
                        ts
                    ).open();
                    close = false;
                } else if (templateElements.isEmpty()) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.warning"), 
                        ts.getTranslatedString("module.connectivity-manager.action-6.empty-link"), 
                        AbstractNotification.NotificationType.WARNING, 
                        ts
                    ).open();
                    close = false;
                } else if (!mem.isSubclassOf(
                    Constants.CLASS_GENERICPHYSICALLINK, 
                    templateElements.get(templateElements.size() - 1).getClassName())) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.warning"), 
                        ts.getTranslatedString("module.connectivity-manager.action-6.no-link"), 
                        AbstractNotification.NotificationType.WARNING, 
                        ts
                    ).open();
                    close = false;
                }
                else
                    setSelectedLink(templateElements.get(templateElements.size() - 1));
            } catch (MetadataObjectNotFoundException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ts.getTranslatedString(ex.getLocalizedMessage()),
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
            if (close)
                wdw.close();
        });
        HorizontalLayout lytFooter= new HorizontalLayout(btnCancel, btnOk);
        lytFooter.setSizeFull();
        lytFooter.setFlexGrow(1, btnCancel, btnOk);
        
        wdw.setHeader(header);
        wdw.setContent(getContent());
        wdw.setFooter(lytFooter);
        return wdw;
    }

    @Override
    public boolean execute() {
        try {
            BusinessObjectLight linkEndpointA = getConnection().getSource() != null ? getConnection().getSource().getSelectedObject() : null;
            BusinessObjectLight linkEndpointB = getConnection().getTarget() != null ? getConnection().getTarget().getSelectedObject() : null;
            
            if (!bem.getSpecialAttributes(linkEndpointA.getClassName(), linkEndpointA.getId(), PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA, PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB).isEmpty() || 
                !bem.getSpecialAttributes(linkEndpointB.getClassName(), linkEndpointB.getId(), PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA, PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB).isEmpty() ||
                endpointA == null ||
                endpointB == null ||
                templateClass == null ||
                containerName == null || containerName.isEmpty() ||
                templateElement == null ||
                templateElements.isEmpty() ||
                !mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, templateElements.get(templateElements.size() - 1).getClassName())) {
                return false;
            }
            BusinessObjectLight commonParent = bem.getCommonParent(endpointA.getClassName(), endpointA.getId(), endpointB.getClassName(), endpointB.getId());
            
            HashMap<String, String> attributes = new HashMap();
            attributes.put(Constants.PROPERTY_NAME, containerName);
            
            HashMap<String, String> templateIds = bem.createSpecialObjectUsingTemplate(templateElement.getClassName(), 
                commonParent.getClassName(), commonParent.getId(), 
                attributes, templateElement.getId()
            );
            String containerClass = templateElement.getClassName();
            String containerId = templateIds.get(templateElement.getId());
            
            TemplateObjectLight linkTemplate = templateElements.get(templateElements.size() - 1);
            String linkClass = linkTemplate.getClassName();
            String linkId = templateIds.get(linkTemplate.getId());
            
            bem.createSpecialRelationship(
                endpointA.getClassName(), endpointA.getId(), 
                containerClass, containerId, 
                PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA, true
            );
            bem.createSpecialRelationship(
                endpointB.getClassName(), endpointB.getId(), 
                containerClass, containerId, 
                PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB, true
            );
            bem.createSpecialRelationship(
                linkEndpointA.getClassName(), linkEndpointA.getId(), 
                linkClass, linkId, 
                PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA, true
            );
            bem.createSpecialRelationship(
                linkEndpointB.getClassName(), linkEndpointB.getId(), 
                linkClass, linkId, 
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
        }
        return false;
    }
    
}
