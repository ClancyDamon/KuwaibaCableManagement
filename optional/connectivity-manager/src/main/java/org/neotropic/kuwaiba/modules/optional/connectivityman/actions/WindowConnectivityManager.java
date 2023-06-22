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

import org.neotropic.util.visual.selectors.ObjectCellComponent;
import org.neotropic.util.visual.selectors.ObjectRenderer;
import org.neotropic.util.visual.selectors.PortSelector;
import com.vaadin.componentfactory.selectiongrid.SelectionGrid;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.NavigationModule;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectFromTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewMultipleBusinessObjectsVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.dialogs.WindowNewObject;
import org.neotropic.kuwaiba.modules.core.templateman.TemplateManagerModule;
import org.neotropic.kuwaiba.modules.optional.connectivityman.ConnectivityManagerService;
import org.neotropic.kuwaiba.modules.optional.connectivityman.actions.AbstractConnectivityActionBuilder.NewLinkFromContainerTemplateActionBuilder;
import org.neotropic.kuwaiba.modules.optional.connectivityman.actions.AbstractConnectivityActionBuilder.NewLinkActionBuilder;
import org.neotropic.kuwaiba.modules.optional.connectivityman.actions.AbstractConnectivityActionBuilder.NewMirrorActionBuilder;
import org.neotropic.kuwaiba.modules.optional.connectivityman.actions.AbstractConnectivityActionBuilder.SelectLinkActionBuilder;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.window.ObjectSelectorWindow;

/**
 * Window to manage connections.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowConnectivityManager extends ConfirmDialog {
    /**
     * Reference to the Application Entity Manager.
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    private final MetadataEntityManager mem;
    /**
     * Reference to the Translation Service.
     */
    private final TranslationService ts;
    /**
     * Reference to the new business object visual action.
     */
    private final NewBusinessObjectVisualAction newBusinessObjectVisualAction;
    /**
     * Reference to the new business object from template visual action.
     */
    private final NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction;
    /**
     * Reference to the new multiple business object visual action.
     */
    private final NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction;
    /**
     * Reference to the Physical Connections Service.
     */
    private final PhysicalConnectionsService physicalConnectionsService;
    /**
     * Reference to the template manager module.
     */
    private final TemplateManagerModule templateManagerModule;
    /**
     * Reference to Navigation Module.
     */
    private final NavigationModule navigationModule;
    
    private final ConnectivityManagerService connectivityManagerService;
    
    private final List<AbstractConnectivityActionBuilder> actions = new ArrayList();
    
    public WindowConnectivityManager(ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts, 
        NewBusinessObjectVisualAction newBusinessObjectVisualAction,
        NewBusinessObjectFromTemplateVisualAction newBusinessObjectFromTemplateVisualAction,
        NewMultipleBusinessObjectsVisualAction newMultipleBusinessObjectsVisualAction, 
        PhysicalConnectionsService physicalConnectionsService, 
        TemplateManagerModule templateManagerModule, NavigationModule navigationModule, 
        ConnectivityManagerService connectivityManagerService) {
        
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(newBusinessObjectVisualAction);
        Objects.requireNonNull(newBusinessObjectFromTemplateVisualAction);
        Objects.requireNonNull(newMultipleBusinessObjectsVisualAction);
        Objects.requireNonNull(physicalConnectionsService);
        Objects.requireNonNull(templateManagerModule);
        Objects.requireNonNull(navigationModule);
        Objects.requireNonNull(connectivityManagerService);
        
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.newBusinessObjectVisualAction = newBusinessObjectVisualAction;
        this.newBusinessObjectFromTemplateVisualAction = newBusinessObjectFromTemplateVisualAction;
        this.newMultipleBusinessObjectsVisualAction = newMultipleBusinessObjectsVisualAction;
        this.physicalConnectionsService = physicalConnectionsService;
        this.templateManagerModule = templateManagerModule;
        this.navigationModule = navigationModule;
        this.connectivityManagerService = connectivityManagerService;
        
        setContentSizeFull();
        setModal(false);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setWidth("90%");
        setHeight("90%");
        setDraggable(true);
        setResizable(true);
        
        actions.add(new SelectLinkActionBuilder(aem, bem, mem, ts));
        actions.add(new NewLinkActionBuilder(aem, bem, mem, ts));
        actions.add(new NewMirrorActionBuilder(aem, bem, mem, ts));
        actions.add(new NewLinkFromContainerTemplateActionBuilder(aem, bem, mem, ts));
    }

    @Override
    public void open() {
        setHeader(ts.getTranslatedString("module.connectivity-manager.action.name"));
        List<Connection> connections = new ArrayList();
        
        Tab tabCircuit = new Tab(ts.getTranslatedString("module.connectivity-manager.action.tab.circuit"));
        Tab tabLogicalCxn = new Tab(ts.getTranslatedString("module.connectivity-manager.action.tab.logical-connection"));
        tabLogicalCxn.setEnabled(false);
        
        Scroller scroller = new Scroller();
        scroller.setSizeFull();        
        
        Tabs tabs = new Tabs(tabCircuit, tabLogicalCxn);
        tabs.setFlexGrowForEnclosedTabs(1);
        tabs.addSelectedChangeListener(selectedChangeEvent -> {
            Tab selectedTab = tabs.getSelectedTab();
            if (tabCircuit.equals(selectedTab))
                circuitContent(connections, scroller,tabs, tabCircuit, tabLogicalCxn);
            else if (tabLogicalCxn.equals(selectedTab))
                logicalConnectionContent(connections, scroller);
        });
        circuitContent(connections, scroller,tabs, tabCircuit, tabLogicalCxn);
        
        VerticalLayout lytContent = new VerticalLayout(tabs, scroller);
        lytContent.setSizeFull();
        lytContent.setMargin(false);
        lytContent.setPadding(false);
        lytContent.expand(scroller);
        
        setContent(lytContent);
        super.open();
    }
    
    private void circuitContent(List<Connection> connections, Scroller scroller, Tabs tabs, Tab tabCircuit, Tab tabLogicalCxn) {
        Connection dummyConnection = new Connection();
        connections.add(dummyConnection);
        
        ListDataProvider<Connection> connectionDataProvider = new ListDataProvider(connections);
        
        SelectionGrid<Connection> tblConnections = new SelectionGrid(10);
        tblConnections.setSizeFull();
        tblConnections.setDataProvider(connectionDataProvider);
        
        Column<Connection> colSource = tblConnections.addComponentColumn(connection -> {
            if (connection.getSource() == null)
                connection.setSource(new PortSelector(ts.getTranslatedString("module.connectivity-manager.action.column.select-source"), aem, bem, mem, ts));
            
            Command cmdClick = () -> {
                try {
                    ConfirmDialog wdw = connection.getSource().getPortSelector();
                    wdw.setDraggable(true);
                    wdw.setModal(false);
                    wdw.setCloseOnEsc(false);
                    wdw.setCloseOnOutsideClick(false);
                    wdw.setResizable(true);
                    wdw.setWidth("60%");
                    wdw.addOpenedChangeListener(openendChangeEvent -> {
                        if (!openendChangeEvent.isOpened() && connection.getSource().getSelectedObject() != null) {
                            connectionDataProvider.refreshItem(connection);
                            tblConnections.focusOnCell(connection, tblConnections.getColumnByKey("col-target"));
                        }
                    });
                    wdw.open();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
            };
            BusinessObjectLight source = connection.getSource().getSelectedObject();
            if (source != null) {
                ObjectCellComponent sourceComponent = new ObjectCellComponent(source, bem, ts, Arrays.asList(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, Constants.CLASS_GENERICDISTRIBUTIONFRAME));
                sourceComponent.addClickListener(clickEvent -> cmdClick.execute());
                return sourceComponent;
            } else {
                Button btnSource = new Button(ts.getTranslatedString("module.connectivity-manager.action.column.select-source"));
                btnSource.setSizeFull();
                btnSource.addClickListener(clickEvent -> cmdClick.execute());
                return btnSource;
            }
        })
        .setHeader(ts.getTranslatedString("module.connectivity-manager.action.column.source"))
        .setKey("col-source"); //NOI18N
        
        Column<Connection> colTarget = tblConnections.addComponentColumn(connection -> {
            if (connection.getTarget() == null)
                connection.setTarget(new PortSelector(ts.getTranslatedString("module.connectivity-manager.action.column.select-target"), aem, bem, mem, ts));
            
            Command cmdClick = () -> {
                try {
                    ConfirmDialog wdw = connection.getTarget().getPortSelector();
                    wdw.setDraggable(true);
                    wdw.setModal(false);
                    wdw.setCloseOnEsc(false);
                    wdw.setCloseOnOutsideClick(false);
                    wdw.setResizable(true);
                    wdw.setWidth("60%");
                    wdw.addOpenedChangeListener(openendChangeEvent -> {
                        if (!openendChangeEvent.isOpened() && connection.getTarget().getSelectedObject() != null) {
                            connectionDataProvider.refreshItem(connection);
                            tblConnections.focusOnCell(connection, tblConnections.getColumnByKey("col-action"));
                        }
                    });
                    wdw.open();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
            };
            BusinessObjectLight target = connection.getTarget().getSelectedObject();
            if (target != null) {
                ObjectCellComponent targetComponent = new ObjectCellComponent(target, bem, ts, Arrays.asList(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, Constants.CLASS_GENERICDISTRIBUTIONFRAME));
                targetComponent.addClickListener(clickEvent -> cmdClick.execute());
                return targetComponent;
            } else {
                Button btnTarget = new Button(ts.getTranslatedString("module.connectivity-manager.action.column.select-target"));
                btnTarget.setSizeFull();
                btnTarget.addClickListener(clickEvent -> cmdClick.execute());
                return btnTarget;
            }
        })
        .setHeader(ts.getTranslatedString("module.connectivity-manager.action.column.target"))
        .setKey("col-target"); //NOI18N
        
        Column<Connection> colAction = tblConnections.addComponentColumn(connection -> {
            BusinessObjectLight source = connection.getSource() != null ? connection.getSource().getSelectedObject() : null;
            BusinessObjectLight target = connection.getTarget() != null ? connection.getTarget().getSelectedObject() : null;
                
            ComboBox<AbstractConnectivityActionBuilder> cmbActions = new ComboBox();
            cmbActions.setSizeFull();
            cmbActions.setClearButtonVisible(true);
            cmbActions.setPlaceholder(ts.getTranslatedString("module.connectivity-manager.action.placeholder.select-action"));
            cmbActions.setItemLabelGenerator(AbstractConnectivityActionBuilder::getText);
            cmbActions.setItems(actions);
            cmbActions.setValue(connection.getType());
            if (source == null || target == null)
                cmbActions.setEnabled(false);
            
            cmbActions.addValueChangeListener(valueChangeEvent -> {
                AbstractConnectivityActionBuilder value = valueChangeEvent.getValue();
                
                if (source == null) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.warning"), 
                        ts.getTranslatedString("module.connectivity-manager.row.warning.select-source-port"), 
                        AbstractNotification.NotificationType.WARNING, 
                        ts
                    ).open();
                }
                if (target == null) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.warning"), 
                        ts.getTranslatedString("module.connectivity-manager.row.warning.select-target-port"), 
                        AbstractNotification.NotificationType.WARNING, 
                        ts
                    ).open();
                }
                if (source == null || target == null)
                    value = null;
                
                if (value != null) {
                    connection.setType(value);
                    
                    AbstractConnectivityAction connectivityAction = null;
                    
                    if (connection.getType() != null)
                        connectivityAction = connection.getType().getAction(connection);

                    if (connectivityAction instanceof AbstractConnectivityVisualAction) {
                        Component component = ((AbstractConnectivityVisualAction) connectivityAction).getComponent();
                        if (component instanceof ConfirmDialog) {
                            ConfirmDialog confirmDialog = (ConfirmDialog) component;
                            confirmDialog.open();
                            confirmDialog.addOpenedChangeListener(openedChangeEvent -> {
                                boolean isOpened = openedChangeEvent.isOpened();
                                if (!isOpened) {
                                    connectionDataProvider.refreshItem(connection);
                                    addConnection(tblConnections, connections, connectionDataProvider, connection);
                                }
                            });
                        }
                    }
                    connection.setAction(connectivityAction);
                }
                else
                    connection.setType(null);
                connectionDataProvider.refreshItem(connection);
                addConnection(tblConnections, connections, connectionDataProvider, connection);
            });
            return cmbActions;
        })
        .setHeader(ts.getTranslatedString("module.connectivity-manager.action.column.action"))
        .setKey("col-action"); //NOI18N
        
        Column<Connection> colLink = tblConnections.addComponentColumn(connection -> {
            if (connection.getAction() instanceof AbstractLinkConnectivityAction) {
                if (connection.getAction() instanceof NewLinkAction) {
                    NewLinkAction newLinkAction = (NewLinkAction) connection.getAction();
                    
                    BusinessObjectLight source = connection.getSource() != null ? connection.getSource().getSelectedObject() : null;
                    BusinessObjectLight target = connection.getTarget() != null ? connection.getTarget().getSelectedObject() : null;
                    
                    if (source != null && target != null && 
                        newLinkAction.getNewLinkName() != null && !newLinkAction.getNewLinkName().isEmpty() && 
                        newLinkAction.getNewLinkClass() != null) {
                        try {
                            BusinessObjectLight commonParent = bem.getCommonParent(
                                source.getClassName(), source.getId(), 
                                target.getClassName(), target.getId()
                            );
                            NewLinkCellComponent newLinkCellComponent = new NewLinkCellComponent(
                                newLinkAction.getNewLinkName(), newLinkAction.getNewLinkClass(), 
                                commonParent, mem, ts
                            );
                            newLinkCellComponent.addClickListener(clickEvent -> {
                                ConfirmDialog wdw = (ConfirmDialog) newLinkAction.getComponent();
                                wdw.open();
                                wdw.addOpenedChangeListener(openedChangeEvent -> {
                                    boolean isOpened = openedChangeEvent.isOpened();
                                    if (!isOpened) {
                                        connectionDataProvider.refreshItem(connection);
                                        addConnection(tblConnections, connections, connectionDataProvider, connection);
                                    }
                                });
                            });
                            return newLinkCellComponent;
                        } catch (InventoryException ex) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                ex.getLocalizedMessage(), 
                                AbstractNotification.NotificationType.ERROR, 
                                ts
                            ).open();
                        }
                    }
                } if (((AbstractLinkConnectivityAction) connection.getAction()).getSelectedLink() instanceof BusinessObjectLight) {
                    ObjectCellComponent objectComponent = new ObjectCellComponent((BusinessObjectLight) ((AbstractLinkConnectivityAction) connection.getAction()).getSelectedLink(), bem, ts, Arrays.asList(Constants.CLASS_VIEWABLEOBJECT));
                    objectComponent.addClickListener(clickEvent -> {
                        ConfirmDialog wdw = (ConfirmDialog) ((AbstractLinkConnectivityAction) connection.getAction()).getComponent();
                        wdw.open();
                        wdw.addOpenedChangeListener(openedChangeEvent -> {
                            boolean isOpened = openedChangeEvent.isOpened();
                            if (!isOpened) {
                                connectionDataProvider.refreshItem(connection);
                                addConnection(tblConnections, connections, connectionDataProvider, connection);
                            }
                        });
                    });
                    return objectComponent;
                } else if (((AbstractLinkConnectivityAction) connection.getAction()).getSelectedLink() instanceof TemplateObjectLight) {
                    BusinessObjectLight source = connection.getSource() != null ? connection.getSource().getSelectedObject() : null;
                    BusinessObjectLight target = connection.getTarget() != null ? connection.getTarget().getSelectedObject() : null;
                    
                    if (source != null && target != null) {
                        try {
                            BusinessObjectLight commonParent = bem.getCommonParent(
                                source.getClassName(), source.getId(), 
                                target.getClassName(), target.getId()
                            );
                            TemplateComponent templateComponent = new TemplateComponent(commonParent, 
                                ((AbstractLinkConnectivityAction) connection.getAction()).getName(),
                                (List<TemplateObjectLight>) ((AbstractLinkConnectivityAction) connection.getAction()).getSelectedObjects(), 
                                (TemplateObjectLight) ((AbstractLinkConnectivityAction) connection.getAction()).getSelectedLink(), 
                                mem, 
                                ts
                            );
                            templateComponent.addClickListener(clickEvent -> {
                                ConfirmDialog wdw = (ConfirmDialog) ((AbstractLinkConnectivityAction) connection.getAction()).getComponent();
                                wdw.open();
                                wdw.addOpenedChangeListener(openedChangeEvent -> {
                                    boolean isOpened = openedChangeEvent.isOpened();
                                    if (!isOpened) {
                                        connectionDataProvider.refreshItem(connection);
                                        addConnection(tblConnections, connections, connectionDataProvider, connection);
                                    }
                                });
                            });
                            return templateComponent;
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
            }
            return new Div();
        })
        .setHeader(ts.getTranslatedString("module.connectivity-manager.action.column.link"))                
        .setKey("col-link");
        
        Column<Connection> colActions = tblConnections.addComponentColumn(connection -> {
            FlexLayout lytActions = new FlexLayout();
            lytActions.setSizeFull();
            
            if (connection.getId() != null) {
                Button btnDelete = new Button(VaadinIcon.TRASH.create(), clickEvent -> {
                    connections.remove(connection);
                    connectionDataProvider.refreshAll();
                });
                lytActions.add(btnDelete);
            }
            return lytActions;
        }).setFlexGrow(0);
        tblConnections.setDetailsVisibleOnClick(false);
        tblConnections.setDetailsVisible(dummyConnection, !tblConnections.isDetailsVisible(dummyConnection));
        tblConnections.focusOnCell(dummyConnection, colSource);
        
        HeaderRow headerRow = tblConnections.prependHeaderRow();
        HeaderCell headerCell = headerRow.join(colSource, colTarget, colAction, colLink, colActions);
        
        Anchor anchorNewObject = new Anchor();
        anchorNewObject.setText(ts.getTranslatedString("module.connectivity-manager.action.new-object"));
        anchorNewObject.getElement().addEventListener("click", domEvent -> { //NOI18N
            SelectorWindow wdwSelector = new SelectorWindow(null, mem, ts, selectedObject -> {
                WindowNewObject wdwNewObject = new WindowNewObject(selectedObject, 
                    newBusinessObjectVisualAction, 
                    newBusinessObjectFromTemplateVisualAction, 
                    newMultipleBusinessObjectsVisualAction, ts);
                wdwNewObject.open();
            });
            wdwSelector.open();
        });
        
        Anchor anchorTemplateManagerModule = new Anchor();
        anchorTemplateManagerModule.setText(templateManagerModule.getName());
        anchorTemplateManagerModule.setHref(templateManagerModule.getId());
        anchorTemplateManagerModule.setTarget("_blank"); //NOI18N
        
        Anchor anchorNavigationModule = new Anchor();
        anchorNavigationModule.setText(navigationModule.getName());
        anchorNavigationModule.setHref(navigationModule.getId());
        anchorNavigationModule.setTarget("_blank"); //NOI18N
        
        HorizontalLayout lytActions = new HorizontalLayout(anchorNewObject, anchorTemplateManagerModule, anchorNavigationModule);
        lytActions.setWidthFull();
        lytActions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        headerCell.setComponent(lytActions);
        
        Button btnClose = new Button(
            ts.getTranslatedString("module.general.messages.close"), 
            clickEvent -> close()
        );
        btnClose.setWidthFull();
        
        Button btnCreateConnections = new Button(
            ts.getTranslatedString("module.connectivity-manager.action.text.create-circuit"), 
            clickEvent -> {
                if (connections.size() == 2) {
                    if (connections.get(0).getAction() != null && connections.get(0).getAction().execute()) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.information"),
                            ts.getTranslatedString("module.connectivity-manager.action.circuit-created"), 
                            AbstractNotification.NotificationType.INFO, 
                            ts
                        ).open();
                        tabCircuit.setEnabled(false);
                        tabLogicalCxn.setEnabled(true);
                        tabs.setSelectedTab(tabLogicalCxn);
                    }
                } else if (connections.size() >= 3) {
                    for (int i = 1; i < connections.size() - 1; i++) {
                        PortSelector iTargetSelector = connections.get(i - 1).getTarget();
                        PortSelector jSourceSelector = connections.get(i).getSource();
                        if (iTargetSelector != null && jSourceSelector != null) {
                            BusinessObjectLight iTarget = iTargetSelector.getSelectedObject();
                            BusinessObjectLight jSource = jSourceSelector.getSelectedObject();
                            if (iTarget != null && jSource != null && iTarget.equals(jSource)) {
                            } else {
                                new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.error"), 
                                    ts.getTranslatedString("module.connectivity-manager.action.no-continuous"), 
                                    AbstractNotification.NotificationType.ERROR, 
                                    ts
                                ).open();
                                return;
                            }
                        }
                    }
                    for (int i = 0; i < connections.size() - 1; i++) {
                        Connection connection = connections.get(i);
                        if (connection.getAction() != null) {
                            if (!connection.getAction().execute())
                                return;
                        }
                    }
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.information"),
                        ts.getTranslatedString("module.connectivity-manager.action.circuit-created"), 
                        AbstractNotification.NotificationType.INFO, 
                        ts
                    ).open();
                    tabCircuit.setEnabled(false);
                    tabLogicalCxn.setEnabled(true);
                    tabs.setSelectedTab(tabLogicalCxn);
                } else {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.warning"),
                        ts.getTranslatedString("module.connectivity-manager.action.text.circuit-empty"), 
                        AbstractNotification.NotificationType.WARNING, 
                        ts
                    ).open();
                }
            }
        );
        btnCreateConnections.setSizeFull();
        
        FlexLayout lytFooter = new FlexLayout(btnClose, btnCreateConnections);
        lytFooter.setSizeFull();
        lytFooter.setFlexGrow(1, btnClose, btnCreateConnections);
        
        scroller.setContent(tblConnections);
        setFooter(lytFooter);
    }
    
    private void logicalConnectionContent(List<Connection> connections, Scroller scroller) {
        PortSelector selectorSource = connections.get(0).getSource();
        PortSelector selectorTarget = connections.get(connections.size() - 2).getTarget();
        
        TextField txtLogicalCxnName = new TextField();
        txtLogicalCxnName.setClearButtonVisible(true);
        txtLogicalCxnName.setValue(String.format("%s - %s", selectorSource.getSelectedObject().getName(), selectorTarget.getSelectedObject().getName()));
        txtLogicalCxnName.setRequired(true);
        txtLogicalCxnName.setLabel(ts.getTranslatedString("module.connectivity-manager.action.label.logical-cxn-name"));
        
        Label lblEndpointA = new Label(ts.getTranslatedString("module.connectivity-manager.action.text.endpoint-a"));
        lblEndpointA.setWidthFull();
        ObjectCellComponent componentEndpointA = new ObjectCellComponent(selectorSource.getSelectedObject(), bem, ts, Arrays.asList(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, Constants.CLASS_GENERICDISTRIBUTIONFRAME));
        Div divEndpointA = new Div(componentEndpointA);
        divEndpointA.setWidthFull();
        divEndpointA.addClickListener(clickEvent -> {
            try {
                ConfirmDialog wdw = selectorSource.getPortSelector();
                wdw.setDraggable(true);
                wdw.setModal(false);
                wdw.setCloseOnEsc(false);
                wdw.setCloseOnOutsideClick(false);
                wdw.setResizable(true);
                wdw.setWidth("60%");
                wdw.open();
                wdw.addOpenedChangeListener(openedChangeEvent -> {
                    boolean isOpened = openedChangeEvent.isOpened();
                    if (!isOpened) {
                        divEndpointA.removeAll();
                        if (selectorSource.getSelectedObject() != null) {
                            ObjectCellComponent newComponentEndpointA = new ObjectCellComponent(selectorSource.getSelectedObject(), bem, ts, Arrays.asList(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, Constants.CLASS_GENERICDISTRIBUTIONFRAME));
                            divEndpointA.add(newComponentEndpointA);
                            if (selectorSource.getSelectedObject() != null && selectorTarget.getSelectedObject() != null)
                                txtLogicalCxnName.setValue(String.format("%s - %s", selectorSource.getSelectedObject().getName(), selectorTarget.getSelectedObject().getName()));
                        } else {
                            txtLogicalCxnName.clear();
                            divEndpointA.add(ts.getTranslatedString("module.connectivity-manager.action.text.select-endpoint-a"));
                        }
                    }
                });
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        });
        FlexLayout lytEndpointA = new FlexLayout(
            lblEndpointA,
            divEndpointA
        );
        lytEndpointA.setWidthFull();
        lytEndpointA.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        lytEndpointA.setFlexGrow(1, lblEndpointA, divEndpointA);
        
        Label lblEndpointB = new Label(ts.getTranslatedString("module.connectivity-manager.action.text.endpoint-b"));
        lblEndpointB.setWidthFull();
        ObjectCellComponent componentEndpointB = new ObjectCellComponent(selectorTarget.getSelectedObject(), bem, ts, Arrays.asList(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, Constants.CLASS_GENERICDISTRIBUTIONFRAME));
        Div divEndpointB = new Div(componentEndpointB);
        divEndpointB.setWidthFull();
        divEndpointB.addClickListener(clickEvent -> {
            try {
                ConfirmDialog wdw = selectorTarget.getPortSelector();
                wdw.setDraggable(true);
                wdw.setModal(false);
                wdw.setCloseOnEsc(false);
                wdw.setCloseOnOutsideClick(false);
                wdw.setResizable(true);
                wdw.setWidth("60%");
                wdw.open();
                wdw.addOpenedChangeListener(openedChangeEvent -> {
                    boolean isOpened = openedChangeEvent.isOpened();
                    if (!isOpened) {
                        divEndpointB.removeAll();
                        if (selectorTarget.getSelectedObject() != null) {
                            ObjectCellComponent newComponentEndpointB = new ObjectCellComponent(selectorTarget.getSelectedObject(), bem, ts, Arrays.asList(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, Constants.CLASS_GENERICDISTRIBUTIONFRAME));
                            divEndpointB.add(newComponentEndpointB);
                            if (selectorSource.getSelectedObject() != null && selectorTarget.getSelectedObject() != null)
                                txtLogicalCxnName.setValue(String.format("%s - %s", selectorSource.getSelectedObject().getName(), selectorTarget.getSelectedObject().getName()));
                        } else {
                            txtLogicalCxnName.clear();
                            divEndpointB.add(ts.getTranslatedString("module.connectivity-manager.action.text.select-endpoint-b"));
                        }
                    }
                });
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage(), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        });
        FlexLayout lytEndpointB = new FlexLayout(
            lblEndpointB,
            divEndpointB
        );                
        lytEndpointB.setWidthFull();
        lytEndpointB.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        lytEndpointB.setFlexGrow(1, lblEndpointB, divEndpointB);
        
        ComboBox<ClassMetadataLight> cmbLogicalCxnClass = new ComboBox();
        cmbLogicalCxnClass.setItemLabelGenerator(c -> c.toString());
        cmbLogicalCxnClass.setClearButtonVisible(true);
        cmbLogicalCxnClass.setRequired(true);
        cmbLogicalCxnClass.setPlaceholder(ts.getTranslatedString("moudle.connectivity-manager.action.placeholder.search-logical-cxn-class"));
        cmbLogicalCxnClass.setLabel(ts.getTranslatedString("module.connectivity-manager.action.label.logical-cxn-class"));
        cmbLogicalCxnClass.setDataProvider(getClassesDataProvider());
        
        HorizontalLayout lytLogicalCxn = new HorizontalLayout(txtLogicalCxnName, cmbLogicalCxnClass);
        lytLogicalCxn.setWidthFull();
        lytLogicalCxn.setMargin(false);
        lytLogicalCxn.setPadding(false);
        lytLogicalCxn.setFlexGrow(1, txtLogicalCxnName, cmbLogicalCxnClass);
        
        ComboBox<BusinessObjectLight> cmbService = new ComboBox();
        cmbService.setItemLabelGenerator(service -> service.getName());
        cmbService.setClearButtonVisible(true);
        cmbService.setRenderer(new ObjectRenderer(mem, ts));
        cmbService.setPlaceholder(ts.getTranslatedString("module.connectivity-manager.action.placeholder.search-service"));
        cmbService.setLabel(ts.getTranslatedString("module.connectivity-manager.action.label.service"));
        cmbService.setDataProvider(getServicesDataProvider());
        
        HorizontalLayout lytService = new HorizontalLayout(cmbService);
        lytService.setWidthFull();
        lytService.setMargin(false);
        lytService.setPadding(false);
        lytService.setFlexGrow(1, cmbService);
        
        VerticalLayout lytContent = new VerticalLayout(lytEndpointA, lytEndpointB, lytLogicalCxn, lytService);
        lytContent.setSizeFull();
        lytContent.setMargin(false);
        lytContent.setPadding(false);
        scroller.setContent(lytContent);
        
        Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), clickEvent -> close());
        Button btnLogicalCxn = new Button(ts.getTranslatedString("module.connectivity-manager.action.text.create-logical-connection"), clickEvent -> {
            Session session = UI.getCurrent().getSession().getAttribute(Session.class);
            String logicalCxnName = txtLogicalCxnName.getValue();
            ClassMetadataLight logicalCxnClass = cmbLogicalCxnClass.getValue();
            BusinessObjectLight service = cmbService.getValue();
            
            if (session != null && logicalCxnName != null && !logicalCxnName.isEmpty() && logicalCxnClass != null && 
                selectorSource.getSelectedObject() != null && selectorTarget.getSelectedObject() != null) {
                HashMap<String, String> properties = new HashMap();
                properties.put(Constants.PROPERTY_NAME, logicalCxnName);
                try {
                    String linkId = connectivityManagerService.createLastMileLink(
                        selectorSource.getSelectedObject().getClassName(), selectorSource.getSelectedObject().getId(), 
                        selectorTarget.getSelectedObject().getClassName(), selectorTarget.getSelectedObject().getId(), 
                        logicalCxnClass.getName(), properties, 
                        session.getUser().getUserName()
                    );
                    if (service != null) {
                        bem.createSpecialRelationship(service.getClassName(), service.getId(), logicalCxnClass.getName(), linkId, "uses", true); //NOI18N
                        aem.createObjectActivityLogEntry(session.getUser().getUserName(), service.getClassName(), service.getId(), 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                            "uses", "", linkId, ""); //NOI18N
                    }
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.information"),
                        ts.getTranslatedString("module.connectivity-manager.action.logical-cxn-created"), 
                        AbstractNotification.NotificationType.INFO, 
                        ts
                    ).open();
                    close();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.warning"),
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.WARNING, 
                        ts
                    ).open();
                }
            }
        });
        
        FlexLayout lytFooter = new FlexLayout(btnClose, btnLogicalCxn);
        lytFooter.setSizeFull();
        lytFooter.setFlexGrow(1, btnClose, btnLogicalCxn);
        setFooter(lytFooter);
    }
    
    private DataProvider<ClassMetadataLight, String> getClassesDataProvider() {
        return DataProvider.fromFilteringCallbacks(
            query -> {
                try {
                    List<ClassMetadataLight> classes = mem.getSubClassesLight(Constants.CLASS_GENERICLASTMILECIRCUIT, false, true);
                    return classes.stream()
                        .sorted(Comparator.comparing(ClassMetadataLight::toString))
                        .filter(c -> StringUtils.containsIgnoreCase(c.toString(), query.getFilter().orElse("")))
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
                    List<ClassMetadataLight> classes = mem.getSubClassesLight(Constants.CLASS_GENERICLASTMILECIRCUIT, false, true);
                    return (int) classes.stream()
                        .filter(c -> StringUtils.containsIgnoreCase(c.toString(), query.getFilter().orElse("")))
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
    
    private DataProvider<BusinessObjectLight, String> getServicesDataProvider() {
        return DataProvider.fromFilteringCallbacks(
            query -> {
                String filter = query.getFilter().orElse(null);
                if (filter != null) {
                    List<BusinessObjectLight> services = bem.getSuggestedObjectsWithFilter(filter, Constants.CLASS_GENERICSERVICE, -1);
                    return services.stream()
                        .sorted(Comparator.comparing(BusinessObjectLight::getName))
                        .skip(query.getOffset())
                        .limit(query.getLimit());
                }
                return null;
            }, 
            query -> {
                String filter = query.getFilter().orElse(null);
                if (filter != null) {
                    List<BusinessObjectLight> services = bem.getSuggestedObjectsWithFilter(filter, Constants.CLASS_GENERICSERVICE, -1);
                    return (int) services.stream()
                        .skip(query.getOffset())
                        .limit(query.getLimit())
                        .count();
                }
                return 0;
            }
        );
    }
    
    private void addConnection(SelectionGrid<Connection> tblConnections, List<Connection> connections, ListDataProvider<Connection> connectionDataProvider, Connection connection) {
        
        if (connection.getSource() != null && connection.getSource().getSelectedObject() != null && 
            connection.getTarget() != null && connection.getTarget().getSelectedObject() != null && 
            connection.getAction() != null && 
            connection.getId() == null && 
            (
             connection.getAction() instanceof NewMirrorAction || 
             (
                connection.getAction() instanceof NewLinkAction && 
                ((NewLinkAction) connection.getAction()).getNewLinkName() != null && 
                !((NewLinkAction) connection.getAction()).getNewLinkName().isEmpty() && 
                ((NewLinkAction) connection.getAction()).getNewLinkClass()!= null
             ) ||
             (connection.getAction() instanceof AbstractLinkConnectivityAction && ((AbstractLinkConnectivityAction) connection.getAction()).getSelectedLink() != null))
            ) {
            
            connection.setId(connections.size());
            
            Connection newConnection = new Connection();
            PortSelector objectSelector = new PortSelector(ts.getTranslatedString("module.connectivity-manager.action.column.select-source"), aem, bem, mem, ts);
            objectSelector.setSelectedObject(connection.getTarget().getSelectedObject());
            objectSelector.setSelectedObjects(connection.getTarget().getSelectedObjects());
            newConnection.setSource(objectSelector);
            
            connections.add(newConnection);
            connectionDataProvider.refreshAll();
            
            tblConnections.focusOnCell(newConnection, tblConnections.getColumnByKey("col-target")); //NOI18N
        }
    }
    
    private class SelectorWindow extends ObjectSelectorWindow {

        public SelectorWindow(
            BusinessObjectLight object, 
            MetadataEntityManager mem, 
            TranslationService ts, 
            Consumer<BusinessObjectLight> consumerSelectedObject) {
            
            super(object, mem, ts, consumerSelectedObject);
        }

        @Override
        public void open() {
            super.open();
            setHeader(ts.getTranslatedString("module.connectivity-manager.action.select-new-object-parent"));
        }
        
        @Override
        public List<BusinessObjectLight> getItems(BusinessObjectLight selectedObject) throws InventoryException {
            if (selectedObject == null)
                return bem.getSuggestedObjectsWithFilter("", Constants.CLASS_VIEWABLEOBJECT, -1);
            else {
                return bem.getChildrenOfClassLight(selectedObject.getId(), 
                    selectedObject.getClassName(), 
                    Constants.CLASS_INVENTORYOBJECT, -1
                );
            }
        }
    }
}
