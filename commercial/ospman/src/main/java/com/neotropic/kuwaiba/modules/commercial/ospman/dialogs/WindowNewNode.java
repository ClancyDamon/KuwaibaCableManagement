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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.NavigationModule;
import org.neotropic.kuwaiba.modules.core.navigation.ObjectOptionsPanel;
import org.neotropic.util.visual.selectors.BusinessObjectSelector;
import org.neotropic.kuwaiba.modules.core.navigation.actions.CopyBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.DefaultDeleteBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.MoveBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.nodes.InventoryObjectNode;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.grids.IconNameCellGrid;
import org.neotropic.util.visual.icons.ClassNameIconGenerator;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to create new node
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowNewNode extends ConfirmDialog {
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final CoreActionsRegistry coreActionsRegistry;
    private final AdvancedActionsRegistry advancedActionsRegistry;
    private final ViewWidgetRegistry viewWidgetRegistry;
    private final ExplorerWidgetRegistry explorerWidgetRegistry;
    private BusinessObjectLight newNodeParent;
    private BusinessObjectLight tmpNewNodeParent;
    private final TemplateObjectLight newNodeTemplate;
    private String newNodeTemplateClassName;
    private BusinessObjectLight newNode;
    private final ResourceFactory resourceFactory;
    private final Consumer<BusinessObjectLight> consumerNewNode;
    private final Consumer<BusinessObjectLight> consumerCloseWdw;
    
    public WindowNewNode(BusinessObjectLight newNodeParent, TemplateObjectLight newNodeTemplate, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts, 
        ResourceFactory resourceFactory, 
        CoreActionsRegistry coreActionsRegistry, 
        AdvancedActionsRegistry advancedActionsRegistry, 
        ViewWidgetRegistry viewWidgetRegistry, 
        ExplorerWidgetRegistry explorerWidgetRegistry, 
        Consumer<BusinessObjectLight> consumerNewNode, 
        Consumer<BusinessObjectLight> consumerCloseWdw) {

        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.newNodeParent = newNodeParent;
        this.newNodeTemplate = newNodeTemplate;
        this.resourceFactory = resourceFactory;
        this.coreActionsRegistry = coreActionsRegistry;
        this.advancedActionsRegistry = advancedActionsRegistry;
        this.viewWidgetRegistry = viewWidgetRegistry;
        this.explorerWidgetRegistry = explorerWidgetRegistry;
        this.consumerNewNode = consumerNewNode;
        this.consumerCloseWdw = consumerCloseWdw;
        
        newNodeTemplateClassName = newNodeTemplate.getClassName();
        try {
            newNodeTemplateClassName = mem.getClass(newNodeTemplateClassName).toString();
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
        setWidth("60%");
        setHeight("60%");
        setDraggable(true);
        setCloseOnOutsideClick(false);
        setContentSizeFull();
        addOpenedChangeListener(openedChangeEvent -> {
            if (!openedChangeEvent.isOpened())
                consumerCloseWdw.accept(newNode);
        });
    }
    
    private void createNode() {
        TextField txtNewNodeParent = new TextField(ts.getTranslatedString("module.ospman.tools.new-node.new-node-parent"));
        txtNewNodeParent.setWidthFull();
        txtNewNodeParent.setClearButtonVisible(true);
        txtNewNodeParent.setRequired(true);
        txtNewNodeParent.setRequiredIndicatorVisible(true);
        if (newNodeParent != null)
            txtNewNodeParent.setValue(newNodeParent.toString());
                
        txtNewNodeParent.getElement().addEventListener("click", domEvent -> { //NOI18N
            BusinessObjectSelector newNodeParentSelector = new BusinessObjectSelector(
                null, null, 
                ts.getTranslatedString("module.ospman.tools.new-node.new-node-parent"), 
                aem, bem, mem, ts
            );
            newNodeParentSelector.addSelectedObjectChangeListener(selectedObjectChangeEvent -> {
                BusinessObjectLight selectedObject = selectedObjectChangeEvent.getSelectedObject();
                if (selectedObject != null) {
                    try {
                        List<ClassMetadataLight> possibleChildren = mem.getPossibleChildren(selectedObject.getClassName(), false);
                        for (ClassMetadataLight possibleChild : possibleChildren) {
                            if (mem.isSubclassOf(possibleChild.getName(), newNodeTemplate.getClassName())) {
                                tmpNewNodeParent = selectedObject;
                                return;
                            }
                        }
                        ClassMetadata newNodeTemplateClass = mem.getClass(newNodeTemplate.getClassName());
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.warning"), 
                            String.format(
                                ts.getTranslatedString("module.ospman.tools.new-node.new-node-parent.children.warning"), 
                                newNodeTemplateClass.toString()
                            ), 
                            AbstractNotification.NotificationType.WARNING, 
                            ts
                        ).open();
                        
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, 
                            ts
                        ).open();
                    }
                    tmpNewNodeParent = null;
                }
            });
            ConfirmDialog wdw = new ConfirmDialog();
            wdw.setWidth("60%");
            wdw.setCloseOnOutsideClick(false);
            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), clickEvent -> {
                tmpNewNodeParent = null;
                wdw.close();
            });
            Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"), clickEvent -> {
                newNodeParent = tmpNewNodeParent;
                if (newNodeParent != null)
                    txtNewNodeParent.setValue(newNodeParent.toString());
                else
                    txtNewNodeParent.setValue("");
                wdw.close();
            });
            HorizontalLayout lytFooter = new HorizontalLayout(btnCancel, btnOk);
            lytFooter.setWidthFull();
            lytFooter.setFlexGrow(1, btnCancel, btnOk);
            
            wdw.setHeader(ts.getTranslatedString("module.ospman.tools.new-node.new-node-parent"));
            wdw.setContent(newNodeParentSelector);
            wdw.setFooter(lytFooter);
            wdw.open();
        });
        
        TextField txtNewNodeName = new TextField(ts.getTranslatedString("module.ospman.tools.new-node.new-node-name"));
        txtNewNodeName.setWidthFull();
        txtNewNodeName.setClearButtonVisible(true);
        txtNewNodeName.setRequired(true);
        txtNewNodeName.setRequiredIndicatorVisible(true);
        txtNewNodeName.setValue(newNodeTemplate.getName());
        
        FlexLayout lytContent = new FlexLayout(
            txtNewNodeParent, 
            txtNewNodeName
        );
        lytContent.setSizeFull();
        lytContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        
        Button btnClose = new Button(
            ts.getTranslatedString("module.general.messages.close"), 
            clickEvent -> close()
        );
        Button btnNext = new Button(ts.getTranslatedString("module.general.messages.next"), clickEvent -> {
            if (newNodeParent == null) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.warning"), 
                    ts.getTranslatedString("module.ospman.tools.new-node.new-node-parent.warning"), 
                    AbstractNotification.NotificationType.WARNING, 
                    ts
                ).open();
                return;
            }
            if (txtNewNodeName.getValue() == null || txtNewNodeName.getValue().isEmpty()) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.warning"), 
                    ts.getTranslatedString("module.ospman.tools.new-node.new-node-name.warning"), 
                    AbstractNotification.NotificationType.WARNING, 
                    ts
                ).open();
                return;
            }
            HashMap<String, String> attributes = new HashMap();
            attributes.put(Constants.PROPERTY_NAME, txtNewNodeName.getValue());
            try {
                String newNodeId = bem.createObject(
                    newNodeTemplate.getClassName(), 
                    newNodeParent.getClassName(), 
                    newNodeParent.getId(), attributes, 
                    newNodeTemplate.getId()
                );
                bem.updateObject(
                    newNodeTemplate.getClassName(), 
                    newNodeId, 
                    attributes
                );
                newNode = bem.getObjectLight(
                    newNodeTemplate.getClassName(), 
                    newNodeId
                );
                consumerNewNode.accept(newNode);
                editNode();
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        });
        HorizontalLayout lytFooter = new HorizontalLayout(btnClose, btnNext);
        lytFooter.setSizeFull();
        lytFooter.setFlexGrow(1, btnClose, btnNext);
        
        setHeader(String.format(
            ts.getTranslatedString("module.ospman.tools.new-node.new-node.class"), 
            newNodeTemplateClassName
        ));
        setContent(lytContent);
        setFooter(lytFooter);
    }
    
    private void editNode() {
        ClassNameIconGenerator iconGenerator = new ClassNameIconGenerator(resourceFactory);
        TreeGrid<InventoryObjectNode> navigationTree = new TreeGrid();
        navigationTree.setWidth("60%");
        navigationTree.setHeightFull();
        
        navigationTree.addThemeVariants(
            GridVariant.LUMO_NO_BORDER,
            GridVariant.LUMO_NO_ROW_BORDERS,
            GridVariant.LUMO_COMPACT
        );
        navigationTree.setSelectionMode(Grid.SelectionMode.SINGLE);
        
        navigationTree.addComponentHierarchyColumn(item -> {
            String displayName = String.format("%s [%s]", 
                item.getObject().getName(), 
                item.getObject().getClassName()
            );
            try {
                displayName = String.format("%s [%s]",
                    item.getObject().getName(),
                    mem.getClass(item.getObject().getClassName()).toString()
                );
            } catch (MetadataObjectNotFoundException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
            return new IconNameCellGrid(displayName, item.getObject().getClassName(), iconGenerator);
        });
        navigationTree.addComponentColumn(item -> getItemColumn(item.getObject()))
            .setFlexGrow(0).setWidth("30px").setTextAlign(ColumnTextAlign.CENTER);
        
        HierarchicalDataProvider dataProvider = new AbstractBackEndHierarchicalDataProvider<InventoryObjectNode, Void>() {
            @Override
            protected Stream<InventoryObjectNode> fetchChildrenFromBackEnd(HierarchicalQuery<InventoryObjectNode, Void> query) {
                try {
                    InventoryObjectNode parent = query.getParent();
                    List<InventoryObjectNode> inventoryNodes = new ArrayList();
                    List<BusinessObjectLight> children;
                    if (parent != null)
                        children = bem.getObjectChildren(parent.getObject().getClassName(), parent.getObject().getId(), null, query.getOffset(), query.getLimit());
                    else
                        children = Arrays.asList(bem.getObjectLight(newNode.getClassName(), newNode.getId()));
                    children.forEach(child -> inventoryNodes.add(new InventoryObjectNode(child)));
                    return inventoryNodes.stream();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                    return Collections.EMPTY_LIST.stream();
                }
            }

            @Override
            public int getChildCount(HierarchicalQuery<InventoryObjectNode, Void> query) {
                try {
                    InventoryObjectNode parent = query.getParent();
                    if (parent != null)
                        return (int) bem.getObjectChildrenCount(parent.getObject().getClassName(), parent.getObject().getId(), null);
                    else
                        return 1;
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                    return 0;
                }
            }

            @Override
            public boolean hasChildren(InventoryObjectNode item) {
                try {
                    return bem.getObjectChildrenCount(item.getObject().getClassName(), item.getObject().getId(), null) > 0;
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                    return false;
                }
            }
        };
        navigationTree.setDataProvider(dataProvider);
        
        Scroller scroller = new Scroller();
        scroller.setWidth("40%");
        scroller.setHeightFull();
        
        navigationTree.addItemClickListener(itemClickEvent -> {
            InventoryObjectNode item = itemClickEvent.getItem();
            if (item != null)
                scroller.setContent(getItemContent(navigationTree, item));
        });
        ActionCompletedListener actionCompletedListener = 
            actionCompletedEvent -> navigationTree.getDataProvider().refreshAll();
        coreActionsRegistry.getActionsForModule(NavigationModule.MODULE_ID).stream().forEach(                
            action -> action.registerActionCompletedLister(actionCompletedListener)
        );
        addOpenedChangeListener(openedChangeEvent -> {
            boolean isOpened = openedChangeEvent.isOpened();
            if (!isOpened) {
                coreActionsRegistry.getActionsForModule(NavigationModule.MODULE_ID).stream().forEach(
                    action -> action.unregisterListener(actionCompletedListener)
                );
            }
        });
        HorizontalLayout lytContent = new HorizontalLayout(navigationTree, scroller);
        lytContent.setSizeFull();
        lytContent.setMargin(false);
        lytContent.setPadding(false);
        
        Button btnClose = new Button(
            ts.getTranslatedString("module.general.messages.close"), 
            clickEvent -> close()
        );
        HorizontalLayout lytFooter = new HorizontalLayout(btnClose);
        lytFooter.setSizeFull();
        lytFooter.setFlexGrow(1, btnClose);
        try {
            setHeader(
                String.format(ts.getTranslatedString("module.ospman.tools.new-node.new-node-navigation"),
                String.format("%s [%s]", newNode.getName(), mem.getClass(newNode.getClassName()).toString())
            ));
        } catch (MetadataObjectNotFoundException ex) {
            setHeader(
                String.format(ts.getTranslatedString("module.ospman.tools.new-node.new-node-navigation"), 
                newNode.toString()
            ));
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
        setContent(lytContent);
        setFooter(lytFooter);
    }
    
    private Component getItemColumn(BusinessObjectLight item) {
        MenuBar menuBar = new MenuBar();
        menuBar.setClassName("actions-menu-no-background");
        menuBar.setThemeName("short-icons");
        menuBar.setOpenOnHover(false);
        MenuItem actionsMenu = menuBar.addItem(VaadinIcon.ELLIPSIS_DOTS_H.create());
        SubMenu actionsSubMenu = actionsMenu.getSubMenu();
        MenuItem newObjMenuItem = actionsSubMenu.addItem(ts.getTranslatedString("module.navigation.actions.menu-item.new-business-object"));
        SubMenu newObjSubMenu = newObjMenuItem.getSubMenu();
        
        coreActionsRegistry.getActionsForModule(NavigationModule.MODULE_ID).stream().forEach(anAction -> {
            if (anAction != null) {
                MenuItem menuItem;
                if (anAction instanceof DefaultDeleteBusinessObjectVisualAction ||
                    anAction instanceof CopyBusinessObjectVisualAction ||
                    anAction instanceof MoveBusinessObjectVisualAction)
                    menuItem = actionsSubMenu.addItem(anAction.getModuleAction().getDisplayName());
                else
                    menuItem = newObjSubMenu.addItem(anAction.getModuleAction().getDisplayName());
                
                if (menuItem != null) {
                    menuItem.addClickListener(clickEvent -> {
                        ((Dialog) anAction.getVisualComponent(
                            new ModuleActionParameterSet(
                                new ModuleActionParameter("businessObject", item)))
                        ).open();
                    });
                }
            }
        });
        return menuBar;
    }
    
    private Component getItemContent(TreeGrid<InventoryObjectNode> navigationTree, InventoryObjectNode item) {
        try {
            ObjectOptionsPanel pnlObjOptions = new ObjectOptionsPanel(item.getObject(), 
                coreActionsRegistry, 
                advancedActionsRegistry, 
                viewWidgetRegistry, 
                explorerWidgetRegistry, 
                mem, aem, bem, ts);
            pnlObjOptions.setShowViews(false);
            pnlObjOptions.setShowExplorers(false);
            pnlObjOptions.setSelectionListener(event -> {
                switch (event.getActionCommand()) {
                    case ObjectOptionsPanel.EVENT_ACTION_SELECTION:
                        ((Dialog) ((AbstractVisualInventoryAction) event.getSource()).getVisualComponent(
                            new ModuleActionParameterSet(new ModuleActionParameter("businessObject", item.getObject())))
                        ).open();
                    break;
                }
            });
            pnlObjOptions.setPropertyListener(property -> {
                try {
                    HashMap<String, String> attributes = new HashMap();
                    attributes.put(property.getName(), String.valueOf(property.getValue()));
                    bem.updateObject(item.getObject().getClassName(), item.getObject().getId(), attributes);
                    if (Constants.PROPERTY_NAME.equals(property.getName())) {
                        navigationTree.getDataProvider().refreshAll();
                        consumerCloseWdw.accept(item.getObject());
                    }
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
            });
            return pnlObjOptions.build(UI.getCurrent().getSession().getAttribute(Session.class).getUser());
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
            return new Div();
        }
    }
    
    @Override
    public void open() {
        if (newNodeParent != null) {
            try {
                boolean isPossibleChild = false;
                
                List<ClassMetadataLight> possibleChildren = mem.getPossibleChildren(newNodeParent.getClassName(), false);
                for (ClassMetadataLight possibleChild : possibleChildren) {
                    if (mem.isSubclassOf(possibleChild.getName(), newNodeTemplate.getClassName())) {
                        isPossibleChild = true;
                        break;
                    }
                }
                if (!isPossibleChild) {
                    newNodeParent = null;
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.warning"), 
                        ts.getTranslatedString("module.ospman.tools.new-node.new-node-parent.default.warning"), 
                        AbstractNotification.NotificationType.WARNING, 
                        ts
                    ).open();
                }
            } catch (MetadataObjectNotFoundException ex) {
                newNodeParent = null;
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        }
        createNode();
        super.open();
    }
}
