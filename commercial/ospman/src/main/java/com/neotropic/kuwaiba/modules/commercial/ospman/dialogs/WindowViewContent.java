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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FilterDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.modules.optional.physcon.windows.FiberSplitterViewWindow;
import org.neotropic.kuwaiba.modules.optional.physcon.windows.PhysicalPathViewWindow;
import org.neotropic.kuwaiba.modules.optional.physcon.windows.PhysicalTreeViewWindow;
import org.neotropic.kuwaiba.modules.optional.physcon.windows.SpliceBoxViewWindow;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Given an object for example a manhole, and list all the devices inside 
 * to show the views available to the device like splice box view or fiber splitter view 
 * and when select it show the ports inside and list the views available to ports 
 * as physical view and physical tree view.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowViewContent extends ConfirmDialog {
    private static final String CLASS_GENERIC_COMM_ELEMENT = "GenericCommunicationsElement";
    private static final String CLASS_GENERIC_BOX= "GenericBox";
    
    private final BusinessObjectLight object;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final PhysicalConnectionsService physicalConnectionsService;
    
    public WindowViewContent(BusinessObjectLight object, ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts, PhysicalConnectionsService physicalConnectionsService) {
        Objects.requireNonNull(object);
        
        this.object = object;
        this.bem = bem;
        this.aem = aem;
        this.mem = mem;
        this.ts = ts;
        this.physicalConnectionsService = physicalConnectionsService;
        
        setHeight("80%");
        setWidth("80%");
        setContentSizeFull();
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setDraggable(true);
        setModal(false);
        setResizable(true);
    }
    
    @Override
    public void open() {
        try {
            final ClassMetadata classGenericCommElement = mem.getClass(CLASS_GENERIC_COMM_ELEMENT);
            final ClassMetadata classGenericBox = mem.getClass(CLASS_GENERIC_BOX);

            HashMap<String, Object> attrToFilter = new HashMap();
            attrToFilter.put(Constants.PROPERTY_ENABLED, true);
            
            List<FilterDefinition> filterDefinitions = aem.getFilterDefinitionsForClass(object.getClassName(), true, true, attrToFilter, -1, -1);
            Collections.sort(filterDefinitions, Comparator.comparing(FilterDefinition::getName));
            
            FilterDefinition defaultFilter = new FilterDefinition(-1, 
                ts.getTranslatedString("module.ospman.wdw-view-content.filter.all-devices"), 
                true
            );
            filterDefinitions.add(0, defaultFilter);
            
            if (!filterDefinitions.isEmpty()) {
                ComboBox<FilterDefinition> cmbFilters = new ComboBox(ts.getTranslatedString("module.ospman.wdw-view-content.filter"));
                cmbFilters.setWidthFull();
                cmbFilters.setPlaceholder(ts.getTranslatedString("module.ospman.wdw-view-content.select-filter"));
                cmbFilters.setClearButtonVisible(true);
                cmbFilters.setItems(filterDefinitions);
                cmbFilters.setRenderer(new ComponentRenderer<>(filterDefinition -> {
                    Icon iconFilter = VaadinIcon.FILTER.create();
                    iconFilter.setSize("14px");

                    HorizontalLayout lytFilter = new HorizontalLayout(
                        iconFilter,
                        new Label(filterDefinition.getName())
                    );
                    lytFilter.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
                    return lytFilter;
                }));
                Scroller scrollerLeft = new Scroller();
                scrollerLeft.setSizeFull();

                FlexLayout lytLeft = new FlexLayout(cmbFilters, scrollerLeft);
                lytLeft.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
                lytLeft.setWidth("40%");
                lytLeft.setHeightFull();

                FlexLayout lytRight = new FlexLayout();
                lytRight.setWidth("60%");
                lytRight.setHeightFull();

                HorizontalLayout lytLists = new HorizontalLayout(lytLeft, lytRight);
                lytLists.setSizeFull();

                FlexLayout lytContent = new FlexLayout(new Label(ts.getTranslatedString("module.ospman.view-node.tool.view-content.help")), lytLists);
                lytContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
                lytContent.setSizeFull();

                Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), clickEvent -> close());
                btnClose.setSizeFull();

                cmbFilters.addValueChangeListener(valueChangeEvent -> {
                    FilterDefinition value = valueChangeEvent.getValue();
                    if (value != null) {
                        try {
                            List<BusinessObjectLight> objects;
                            
                            if (value.getId() == -1) {
                                objects = new ArrayList();
                                objects.addAll(bem.getChildrenOfClassLightRecursive(object.getId(), object.getClassName(), CLASS_GENERIC_COMM_ELEMENT, null, -1, -1));
                                objects.addAll(bem.getChildrenOfClassLightRecursive(object.getId(), object.getClassName(), CLASS_GENERIC_BOX, null, -1, -1));
                                Collections.sort(objects, Comparator.comparing(BusinessObjectLight::getName));
                            }
                            else
                                objects = value.getFilter().run(object.getId(), object.getClassName(), null, -1, -1);
                            
                            if (!objects.isEmpty()) {
                                Collections.sort(objects, Comparator.comparing(BusinessObjectLight::getName));
                                List<ClassMetadata> classes = new ArrayList();
                                HashMap<ClassMetadata, List<BusinessObjectLight>> items = new HashMap();
                                for (BusinessObjectLight item : objects) {
                                    ClassMetadata itemClass = mem.getClass(item.getClassName());
                                    if (!items.containsKey(itemClass)) {
                                        classes.add(itemClass);
                                        items.put(itemClass, new ArrayList());
                                    }
                                    items.get(itemClass).add(item);
                                }
                                Collections.sort(classes, Comparator.comparing(ClassMetadata::toString));
                                updateContent(classes, items, scrollerLeft, lytRight);
                            } else {
                                scrollerLeft.setContent(new Label(String.format(
                                    ts.getTranslatedString("module.ospman.wdw-view-content.filter.not-found"), 
                                    value.getClassToBeApplied()
                                )));
                                lytRight.removeAll();
                            }
                        } catch (InventoryException ex) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                ex.getLocalizedMessage(), 
                                AbstractNotification.NotificationType.ERROR, ts
                            ).open();
                        }
                    }
                    else
                        updateContent(new ArrayList(), new HashMap(), scrollerLeft, lytRight);
                });
                setHeader(String.format(ts.getTranslatedString("module.ospman.view-node.title.view-content"), object.getName()));
                setContent(lytContent);
                setFooter(btnClose);
                super.open();
            } else {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.information"), 
                    String.format(
                        ts.getTranslatedString("module.ospman.wdw-view-content.no-filters"), 
                        classGenericBox.toString(), 
                        classGenericCommElement.toString()
                    ), 
                    AbstractNotification.NotificationType.INFO, 
                    ts
                ).open();
            }
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
    }
    
    private void updateContent(List<ClassMetadata> classes, HashMap<ClassMetadata, List<BusinessObjectLight>> configItemClasses, Scroller scrollerLeft, FlexLayout lytRight) {
        lytRight.removeAll();
        
        FlexLayout lytConfigItems = new FlexLayout();
        lytConfigItems.setWidthFull();
        lytConfigItems.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        
        List<ExtendedDetails> detailsComponents = new ArrayList();
        
        for (ClassMetadata configItemClass : classes) {
            configItemClasses.get(configItemClass).forEach(configItem -> {
                ExtendedDetails details = new ExtendedDetails();
                details.addThemeName("details-list");
                detailsComponents.add(details);
                details.getElement().getStyle().set("box-shadow", "none"); //NOI18N

                Label lblConfigItemName = new Label(configItem.getName());
                Label lblConfigItemClassName = new Label(configItemClass.toString());
                lblConfigItemClassName.setClassName("text-secondary");
                
                FlexLayout lytConfigItem = new FlexLayout(lblConfigItemName, lblConfigItemClassName);
                lytConfigItem.setWidthFull();
                lytConfigItem.setFlexDirection(FlexLayout.FlexDirection.COLUMN);

                details.setSummary(lytConfigItem);
                details.addOpenedChangeListener(openedChangeEvent -> {
                    if (!((ExtendedDetails) openedChangeEvent.getSource()).isFromClient()) {
                        ((ExtendedDetails) openedChangeEvent.getSource()).setFromClient(true);
                        return;
                    }
                    detailsComponents.forEach(detailsComponent -> {
                        if (!detailsComponent.equals(openedChangeEvent.getSource())) {
                            if (detailsComponent.isOpened()) {
                                detailsComponent.setFromClient(false);
                                detailsComponent.setOpened(false);
                            }
                        }
                    });
                    try {
                        boolean isOpened = openedChangeEvent.isOpened();                                
                        boolean isFromClient = openedChangeEvent.isFromClient();

                        if (isFromClient) {
                            List<BusinessObjectLight> boards = bem.getChildrenOfClassLightRecursive(configItem.getId(), configItem.getClassName(), Constants.CLASS_GENERICBOARD, null, -1, -1);
                            Collections.sort(boards, Comparator.comparing(BusinessObjectLight::getName));

                            if (isOpened || details.getContent().count() == 1) {
                                FlexLayout lytConfigItemViews = getConfigItemViews(configItem);
                                lytRight.removeAll();
                                lytRight.add(lytConfigItemViews);
                            }
                            if (isOpened && details.getContent().count() == 0) {
                                if (!boards.isEmpty()) {
                                    ListBox<BusinessObjectLight> lstBoards = new ListBox();
                                    lstBoards.setWidthFull();
                                    lstBoards.setItems(boards);

                                    lstBoards.setRenderer(new ComponentRenderer<>(board -> {
                                        Label lblBoardName = new Label(board.getName());
                                        Label lblBoardClass = new Label();
                                        lblBoardClass.setClassName("text-secondary");
                                        try {
                                            ClassMetadata boardClass = mem.getClass(board.getClassName());
                                            lblBoardClass.setText(boardClass.toString());

                                        } catch (MetadataObjectNotFoundException ex) {
                                            lblBoardClass.setText(board.getClassName());
                                            new SimpleNotification(
                                                ts.getTranslatedString("module.general.messages.error"), 
                                                ex.getLocalizedMessage(), 
                                                AbstractNotification.NotificationType.ERROR, ts
                                            ).open();
                                        }
                                        FlexLayout lytBoard = new FlexLayout(lblBoardName, lblBoardClass);
                                        lytBoard.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
                                        
                                        try {
                                            List<BusinessObjectLight> parents = bem.getParentsUntilFirstOfClass(
                                                board.getClassName(), board.getId(), 
                                                CLASS_GENERIC_BOX, CLASS_GENERIC_COMM_ELEMENT
                                            );
                                            if (!parents.isEmpty()) {
                                                String lblText = "";
                                                for (int i = 0; i < parents.size(); i++)
                                                    lblText += ":" + parents.get(i).getName() + " ";

                                                Label lblParents = new Label(lblText);
                                                lblParents.setClassName("text-secondary");

                                                lytBoard.add(lblParents);
                                            }
                                        } catch (InventoryException ex) {
                                            new SimpleNotification(
                                                ts.getTranslatedString("module.general.messages.error"), 
                                                ex.getLocalizedMessage(), 
                                                AbstractNotification.NotificationType.ERROR, 
                                                ts
                                            ).open();
                                        }
                                        return lytBoard;
                                    }));
                                    lstBoards.addValueChangeListener(valueChangeEvent -> {
                                        BusinessObjectLight value = valueChangeEvent.getValue();
                                        if (value != null) {
                                            try {
                                                FlexLayout lytConfigItemViews = getConfigItemViews(value);
                                                lytRight.removeAll();
                                                lytRight.add(lytConfigItemViews);

                                            } catch (InventoryException ex) {
                                                new SimpleNotification(
                                                    ts.getTranslatedString("module.general.messages.error"), 
                                                    ex.getLocalizedMessage(), 
                                                    AbstractNotification.NotificationType.ERROR, ts
                                                ).open();
                                            }
                                        }
                                    });
                                    Scroller s = new Scroller();
                                    s.setWidthFull();
                                    s.setContent(lstBoards);
                                    details.setContent(s);
                                }
                                else
                                    details.setContent(new Div(new Label(ts.getTranslatedString("module.ospman.view-node.tool.view-content.no-boards"))));
                            }
                            if (details.getContent().count() == 1) {
                                details.getContent().forEach(component -> {
                                    if (component instanceof Scroller && ((Scroller) component).getContent() instanceof ListBox)
                                        ((ListBox) ((Scroller) component).getContent()).clear();
                                });
                            }
                        }
                        else
                            ((ExtendedDetails) openedChangeEvent.getSource()).setFromClient(true);
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts
                        ).open();
                    }
                });

                lytConfigItems.add(details);
            });
        }
        scrollerLeft.setContent(lytConfigItems);
    }
    
    private FlexLayout getConfigItemViews(BusinessObjectLight configItem) throws InventoryException {
        List<BusinessObjectLight> ports;
        if (mem.isSubclassOf(Constants.CLASS_GENERICBOARD, configItem.getClassName()))
            ports = bem.getChildrenOfClassLightRecursive(configItem.getId(), configItem.getClassName(), Constants.CLASS_GENERICPORT, null, -1, -1);
        else
            ports = bem.getChildrenOfClassLight(configItem.getId(), configItem.getClassName(), Constants.CLASS_GENERICPORT, -1);
        Collections.sort(ports, Comparator.comparing(BusinessObjectLight::getName));
        
        FlexLayout lytConfigItemViews = new FlexLayout();
        lytConfigItemViews.setSizeFull();
        lytConfigItemViews.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        
        if (mem.isSubclassOf(Constants.CLASS_SPLICE_BOX, configItem.getClassName())) {
            lytConfigItemViews.add(new Button(ts.getTranslatedString("module.ospman.views.splice-box"), clickEvent -> {
                try {
                    new SpliceBoxViewWindow(configItem, bem, aem, mem, ts).open();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
            }));
        } else if (mem.isSubclassOf(Constants.CLASS_FIBER_SPLITTER, configItem.getClassName())) {
            lytConfigItemViews.add(new Button(ts.getTranslatedString("module.ospman.views.splitter"), clickEvent -> {
                try {
                    new FiberSplitterViewWindow(configItem, bem, aem, mem, ts).open();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
            }));
        }
        if (!ports.isEmpty()) {
            TextField txtSearchPort = new TextField();
            txtSearchPort.setWidthFull();
            txtSearchPort.setSuffixComponent(VaadinIcon.SEARCH.create());
            txtSearchPort.setClearButtonVisible(true);
            txtSearchPort.setPlaceholder(ts.getTranslatedString("module.ospman.view-node.tool.view-content.search-port"));
            lytConfigItemViews.add(txtSearchPort);
            
            Scroller scroller = new Scroller();
            scroller.setSizeFull();
            scroller.setContent(getPorts(ports));
            lytConfigItemViews.add(scroller);
            
            txtSearchPort.addValueChangeListener(valueChangeEvent -> {
                String value = valueChangeEvent.getValue();
                if (value != null && !value.isEmpty()) {
                    List<BusinessObjectLight> items = new ArrayList();
                    ports.forEach(port -> {
                        String portClassName = port.getClassName();
                        try {
                            portClassName = mem.getClass(port.getClassName()).toString();
                        } catch (MetadataObjectNotFoundException ex) {
                            new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.error"),
                                    ex.getLocalizedMessage(), 
                                    AbstractNotification.NotificationType.ERROR, ts
                            ).open();
                        }
                        if (portClassName.toLowerCase().contains(value.toLowerCase()) || port.getName().toLowerCase().contains(value.toLowerCase()))
                            items.add(port);
                    });
                    scroller.setContent(getPorts(items));
                }
                else
                    scroller.setContent(getPorts(ports));
            });
        } else
            lytConfigItemViews.add(new Label(ts.getTranslatedString("module.ospman.view-node.tool.view-content.no-ports")));
        return lytConfigItemViews;
    }
    
    public VerticalLayout getPorts(List<BusinessObjectLight> ports) {
        VerticalLayout lytPorts = new VerticalLayout();
        lytPorts.setWidthFull();

        ports.forEach(port -> {
            FlexLayout lytPortObject = new FlexLayout(new FormattedObjectDisplayNameSpan(port));
            lytPortObject.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
            try {
                List<BusinessObjectLight> parents = bem.getParentsUntilFirstOfClass(
                    port.getClassName(), port.getId(), 
                    CLASS_GENERIC_BOX, CLASS_GENERIC_COMM_ELEMENT
                );
                if (!parents.isEmpty()) {
                    String lblText = "";
                    for (BusinessObjectLight parent : parents)
                        lblText += ":" + parent.getName() + " ";
                    Label lblParents = new Label(lblText);
                    lblParents.setClassName("text-secondary");
                    lytPortObject.add(lblParents);
                }
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
            Button btnPhysicalPathView = new Button(ts.getTranslatedString("module.ospman.views.physical-path"), event -> {
                try {
                    new PhysicalPathViewWindow(port, bem, aem, mem, ts, physicalConnectionsService).open();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
            });
            Button btnPḧysicalTreeView = new Button(ts.getTranslatedString("module.ospman.views.physical-tree"), event -> {
                try {
                    new PhysicalTreeViewWindow(port, bem, aem, mem, ts, physicalConnectionsService).open();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts
                    ).open();
                }
            });
            HorizontalLayout lytPort = new HorizontalLayout(
                lytPortObject, 
                btnPhysicalPathView, 
                btnPḧysicalTreeView
            );
            lytPort.setWidthFull();
            lytPort.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
            lytPort.expand(lytPortObject);

            lytPorts.add(lytPort);
        });
        return lytPorts;
    }
    private class ExtendedDetails extends Details {
        private boolean fromClient = true;
        
        public ExtendedDetails() {
        }
        
        public boolean isFromClient() {
            return fromClient;
        }
        
        public void setFromClient(boolean fromClient) {
            this.fromClient = fromClient;
        }
    }
}
