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

import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.Heatmap;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapProvider;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapProvider.ZoomChangedEvent.ZoomChangedEventListener;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.UnitOfLength;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.ViewHeatmap;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AdvancedActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.CoreActionsRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ExplorerWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewWidgetRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryParameter;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryResult;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Window to execute a geographical query.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowGeographicalQuery extends ConfirmDialog {
    private static final String PARAM_LATITUDE = "latitude"; //NOI18
    private static final String PARAM_LONGITUDE = "longitude"; //NOI18
    private static final String PARAM_VIEW_NODES = "viewNodes"; //NOI18
    private static final String PARAM_RADIUS = "radius"; //NOI18N
    
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final PhysicalConnectionsService physicalConnectionsService;
    private final ScriptedQuery scriptedQuery;
    private final Double lat;
    private final Double lng;
    private final List<AbstractViewNode> viewNodes;
    private final UnitOfLength unitOfLength;
    private final Consumer<BusinessObjectLight> consumerLocateNode;
    private final CoreActionsRegistry coreActionsRegistry;
    private final AdvancedActionsRegistry advancedActionsRegistry;
    private final ViewWidgetRegistry viewWidgetRegistry;
    private final ExplorerWidgetRegistry explorerWidgetRegistry;
    private Grid<List> tblResult;
    private AbstractViewNode selectedNode;
    private ObjectOptionsWindow wdwObjectOptions;
    
    private final MapProvider mapProvider;
    private ViewHeatmap viewHeatmap;
    private Heatmap heatmap;
    
    public WindowGeographicalQuery(ScriptedQuery scriptedQuery, 
        Double lat, Double lng, List<AbstractViewNode> viewNodes, UnitOfLength unitOfLength,
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, 
        TranslationService ts, 
        PhysicalConnectionsService physicalConnectionsService,
        Consumer<BusinessObjectLight> consumerLocateNode,
        CoreActionsRegistry coreActionsRegistry,
        AdvancedActionsRegistry advancedActionsRegistry,
        ViewWidgetRegistry viewWidgetRegistry,
        ExplorerWidgetRegistry explorerWidgetRegistry,
        MapProvider mapProvider) {
        Objects.requireNonNull(scriptedQuery);
        Objects.requireNonNull(lat);
        Objects.requireNonNull(lng);
        Objects.requireNonNull(viewNodes);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(consumerLocateNode);
        Objects.requireNonNull(coreActionsRegistry);
        Objects.requireNonNull(advancedActionsRegistry);
        Objects.requireNonNull(viewWidgetRegistry);
        Objects.requireNonNull(explorerWidgetRegistry);
        Objects.requireNonNull(mapProvider);
        this.scriptedQuery = scriptedQuery;
        this.lat = lat;
        this.lng = lng;
        this.viewNodes = viewNodes;
        this.unitOfLength = unitOfLength;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.physicalConnectionsService = physicalConnectionsService;
        this.consumerLocateNode = consumerLocateNode;
        this.coreActionsRegistry = coreActionsRegistry;
        this.advancedActionsRegistry = advancedActionsRegistry;
        this.viewWidgetRegistry = viewWidgetRegistry;
        this.explorerWidgetRegistry = explorerWidgetRegistry;
        this.mapProvider = mapProvider;
    }
    
    public AbstractViewNode getSelectedNode() {
        return selectedNode;
    }
    
    public void setSelectedNode(AbstractViewNode selectedNode) {
        this.selectedNode = selectedNode;
    }
    
    @Override
    public void open() {
        getElement().getThemeList().add("wdw-osp-tool"); //NOI18N
        setWidth("30%");
        setHeight("80%");
        setContentSizeFull();
        setDraggable(true);
        setModal(false);
        setCloseOnOutsideClick(false);
        setResizable(true);
        try {
            List<ScriptedQueryParameter> parameters = aem.getScriptedQueryParameters(scriptedQuery.getId());
            FlexLayout lytContent = new FlexLayout();
            lytContent.setSizeFull();
            lytContent.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
            
            Div div = new Div();
            div.setSizeFull();
            
            Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), clickEvent -> close());
            
            Button btnLocateOnMap = new Button(
                ts.getTranslatedString("module.ospman.wdw.select-physical-node.locate-on-map"), 
                clickEvent -> {
                    if (tblResult != null) {
                        List row = tblResult.asSingleSelect().getValue();
                        if (row != null && !row.isEmpty()) {
                            BusinessObjectLight businessObject = (BusinessObjectLight) row.get(0);
                            consumerLocateNode.accept(businessObject);
                        }
                    }
                }
            );
            btnLocateOnMap.setEnabled(false);
            
            Button btnViewContent = new Button(
                ts.getTranslatedString("module.ospman.wdw.select-physical-node.view-content"),
                clickEvent -> {
                    if (tblResult != null) {
                        List row = tblResult.asSingleSelect().getValue();
                        if (row != null && !row.isEmpty()) {
                            BusinessObjectLight businessObject = (BusinessObjectLight) row.get(0);
                            new WindowViewContent(
                                businessObject, 
                                aem, bem, mem, ts, 
                                physicalConnectionsService
                            ).open();
                        }
                    }
                }
            );
            btnViewContent.setEnabled(false);
            Command cmdExecute = () -> {
                if (viewHeatmap != null)
                    mapProvider.removeHeatmap(viewHeatmap);
                viewHeatmap = null;
                heatmap = null;

                tblResult = null;
                div.removeAll();
                
                for (ScriptedQueryParameter param : parameters) {
                    if (param.isMandatory() && param.getValue() == null)
                        return;
                }
                try {
                    List<ScriptedQueryParameter> tmpParameters = new ArrayList();
                    parameters.forEach(param -> {
                        ScriptedQueryParameter tmpParam = new ScriptedQueryParameter(
                            param.getId(), 
                            param.getName(), 
                            param.getDescription(), 
                            param.getType(), 
                            param.isMandatory(), 
                            param.getDefaultValue()
                        );
                        tmpParam.setValue(param.getValue());
                        
                        tmpParameters.add(tmpParam);
                    });
                    tmpParameters.forEach(param -> {
                        if (PARAM_RADIUS.equals(param.getName()) && param.getValue() != null) {
                            Double value = param.getValue() != null ? (Double) param.getValue() : param.getDefaultValue() != null ? (Double) param.getDefaultValue() : 0;
                            param.setValue(UnitOfLength.toMeters(value, unitOfLength));
                        }
                    });
                    ScriptedQueryResult result = aem.executeScriptedQuery(
                        scriptedQuery.getId(),
                        tmpParameters.toArray(new ScriptedQueryParameter[0])
                    );
                    if (result != null) {
                        result.getRows().removeIf(row ->
                            getSelectedNode() != null ? getSelectedNode().getIdentifier().equals(row.get(0)) : false
                        );
                    }
                    if (result == null || !result.getRows().isEmpty()) {
                        HashMap<BusinessObjectLight, GeoCoordinate> points = new HashMap();
                        HashMap<BusinessObjectLight, Checkbox> checkboxes = new HashMap();
                        
                        FlexLayout lytResult = new FlexLayout();
                        lytResult.setSizeFull();
                        lytResult.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
                        
                        ListDataProvider<List> rows = new ListDataProvider(result.getRows());
                        
                        TextField txtFilter = new TextField(ts.getTranslatedString("module.ospman.wdw.filter-physical-node"));
                        txtFilter.setClearButtonVisible(true);
                        txtFilter.setSuffixComponent(VaadinIcon.FILTER.create());
                        txtFilter.setValueChangeMode(ValueChangeMode.EAGER);
                        txtFilter.addValueChangeListener(valueChangeEvent -> {
                            String filter = valueChangeEvent.getValue();
                            rows.setFilter(item -> {
                                if (filter != null && !filter.isEmpty()) {
                                    BusinessObjectLight businessObject = (BusinessObjectLight) item.get(0);
                                    try {
                                        return StringUtils.containsIgnoreCase(businessObject.getName(), filter) || 
                                               StringUtils.containsIgnoreCase(mem.getClass(businessObject.getClassName()).toString(), filter);
                                    } catch (MetadataObjectNotFoundException ex) {
                                        new SimpleNotification(
                                            ts.getTranslatedString("module.general.messages.error"), 
                                            ex.getLocalizedMessage(), 
                                            AbstractNotification.NotificationType.ERROR, 
                                            ts
                                        ).open();
                                    }
                                    return StringUtils.containsIgnoreCase(businessObject.getName(), filter) || 
                                           StringUtils.containsIgnoreCase(businessObject.getClassName(), filter);
                                }
                                return true;
                            });
                            rows.refreshAll();
                        });
                        Checkbox chkSelectAll = new Checkbox();
                        
                        Command cmdSetPoints = () -> {
                            List<GeoCoordinate> tmpPoints = new ArrayList();
                            points.values().forEach(point -> {
                                if (point != null)
                                    tmpPoints.add(point);
                            });
                            heatmap.setPoints(tmpPoints);
                        };
                        tblResult = new Grid();
                        tblResult.setSizeFull();
                        tblResult.addComponentColumn(row -> {
                            BusinessObjectLight businessObject = (BusinessObjectLight) row.get(0);
                            Checkbox chk = new Checkbox();
                            chk.addValueChangeListener(valueChangeEvent -> {
                                Boolean value = valueChangeEvent.getValue();
                                if (value) {
                                    if (row.size() == 4) {
                                        double lat = Double.valueOf(String.valueOf(row.get(2)));
                                        double lng = Double.valueOf(String.valueOf(row.get(3)));
                                        points.put(businessObject, new GeoCoordinate(lat, lng));
                                    }
                                    else
                                        points.put(businessObject, null);
                                }
                                else
                                    points.remove(businessObject);
                                if (valueChangeEvent.isFromClient()) {
                                    if (!value)
                                        chkSelectAll.setValue(false);
                                    cmdSetPoints.execute();
                                }
                            });
                            checkboxes.put(businessObject, chk);
                            return chk;
                        }).setWidth("10%");
                        tblResult.addComponentColumn(row -> {
                            BusinessObjectLight businessObject = (BusinessObjectLight) row.get(0);
                            Double distance = Double.valueOf(String.valueOf(row.get(1)));
                            
                            Label lblDistance = new Label(String.format(
                                ts.getTranslatedString("module.ospman.wdw.physical-node-distance"), 
                                UnitOfLength.convertMeters(distance, unitOfLength), 
                                UnitOfLength.getTranslatedString(unitOfLength, ts)
                            ));
                            lblDistance.setClassName("text-secondary");
                            
                            FlexLayout lytBusinessObject = new FlexLayout(
                                new FormattedObjectDisplayNameSpan(businessObject),
                                lblDistance
                            );
                            lytBusinessObject.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
                            
                            return lytBusinessObject;
                        }).setWidth("55%");
                        tblResult.addComponentColumn(row -> {
                            Button btnDetails = new Button(ts.getTranslatedString("module.ospman.wdw.geo-queries.row.details"), clickEvent -> {
                                BusinessObjectLight businessObject = (BusinessObjectLight) row.get(0);
                                if(wdwObjectOptions == null) {
                                    wdwObjectOptions = new ObjectOptionsWindow(
                                        coreActionsRegistry, advancedActionsRegistry, 
                                        viewWidgetRegistry, explorerWidgetRegistry, 
                                        aem, bem, mem, ts
                                    );
                                }
                                wdwObjectOptions.setBusinessObject(businessObject);
                                wdwObjectOptions.open();
                            });
                            btnDetails.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.open-node-options"));
                            return btnDetails;
                        }).setWidth("35%");
                        tblResult.asSingleSelect().addValueChangeListener(valueChangedEvent -> {
                            btnLocateOnMap.setEnabled(valueChangedEvent.getValue() != null);
                            btnViewContent.setEnabled(valueChangedEvent.getValue() != null);
                        });
                        tblResult.setDataProvider(rows);
                        
                        
                        chkSelectAll.getElement().setAttribute("title", ts.getTranslatedString("module.ospman.wdw.geo-queries.result.select-all")); //NOI18N
                        chkSelectAll.addValueChangeListener(valueChangeEvent -> {
                            if (valueChangeEvent.isFromClient()) {
                                boolean value = valueChangeEvent.getValue();
                                checkboxes.values().forEach(checkbox -> 
                                    checkbox.setValue(value)
                                );
                                if (value)
                                    cmdSetPoints.execute();
                                else
                                    heatmap.setPoints(Collections.EMPTY_LIST);
                            }
                        });
                        chkSelectAll.setWidth("10%");
                        
                        txtFilter.setWidth("90%");
                        
                        HorizontalLayout lytFilter = new HorizontalLayout(chkSelectAll, txtFilter);
                        lytFilter.setWidth("100%");
                        lytFilter.setMargin(false);
                        lytFilter.setPadding(false);
                        lytFilter.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
                        
                        lytResult.add(lytFilter, tblResult);
                        
                        div.add(lytResult);
                        
                        if (viewHeatmap == null) {
                            viewHeatmap = new ViewHeatmap();
                            heatmap = mapProvider.addHeatmap(viewHeatmap);
                        }
                    }
                    else
                        div.add(new Label(ts.getTranslatedString("module.ospman.wdw.select-physical-node.result-empty")));
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage(), 
                        AbstractNotification.NotificationType.ERROR, 
                        ts
                    ).open();
                }
            };
            Button btnExecute = new Button(
                ts.getTranslatedString("module.ospman.execute-geographical-queries"), 
                VaadinIcon.CARET_RIGHT.create(),
                clickEvent -> cmdExecute.execute()
            );
            btnExecute.setWidthFull();
            btnExecute.setEnabled(false);
            Command cmdEnabledBtnExecute = () -> {
                for (ScriptedQueryParameter param : parameters) {
                    if (param.isMandatory() && param.getValue() == null) {
                        btnExecute.setEnabled(false);
                        return;
                    }
                }
                btnExecute.setEnabled(true);
            };
            parameters.forEach(param -> {
                if (PARAM_LATITUDE.equals(param.getName()))
                    param.setValue(lat);
                else if (PARAM_LONGITUDE.equals(param.getName()))
                    param.setValue(lng);
                else if (PARAM_VIEW_NODES.equals(param.getName()))
                    param.setValue(viewNodes);
                else {
                    if (Constants.DATA_TYPE_STRING.equals(param.getType())) {
                        TextField txtParam = new TextField();
                        txtParam.addKeyPressListener(Key.ENTER, keyPressEvent -> cmdExecute.execute());
                        txtParam.setValueChangeMode(ValueChangeMode.EAGER);
                        txtParam.setClearButtonVisible(true);
                        if (param.getDescription() != null)
                            txtParam.setLabel(param.getDescription());
                        
                        txtParam.setRequired(param.isMandatory());
                        txtParam.setRequiredIndicatorVisible(param.isMandatory());
                        if (param.getDefaultValue() != null) {
                            param.setValue(param.getDefaultValue());
                            txtParam.setValue((String) param.getDefaultValue());
                        }
                        txtParam.setWidthFull();
                        txtParam.addValueChangeListener(valueChangeEvent ->  {
                            param.setValue(valueChangeEvent.getValue());
                            cmdEnabledBtnExecute.execute();
                        });
                        lytContent.add(txtParam);
                    } else if (Constants.DATA_TYPE_DOUBLE.equals(param.getType())) {
                        NumberField txtParam = new NumberField();
                        txtParam.addKeyPressListener(Key.ENTER, keyPressEvent -> cmdExecute.execute());
                        txtParam.setValueChangeMode(ValueChangeMode.EAGER);
                        txtParam.setClearButtonVisible(true);
                        if (param.getDescription() != null)
                            txtParam.setLabel(param.getDescription());
                        
                        txtParam.setRequiredIndicatorVisible(param.isMandatory());
                        if (param.getDefaultValue() != null) {
                            param.setValue(param.getDefaultValue());
                            txtParam.setValue((Double) param.getDefaultValue());
                        }
                        txtParam.setWidthFull();
                        txtParam.addValueChangeListener(valueChangeEvent -> {
                            param.setValue(valueChangeEvent.getValue());
                            cmdEnabledBtnExecute.execute();
                        });
                        lytContent.add(txtParam);
                    } else {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.warning"), 
                            String.format(ts.getTranslatedString("apis.persistence.aem.messages.scripted-query-parameter-type-not-supported"), param.getType(), param.getName()), 
                            AbstractNotification.NotificationType.WARNING, 
                            ts
                        ).open();
                    }
                }
            });
            lytContent.add(btnExecute, div);
            lytContent.expand(div);
            
            HorizontalLayout lytFooter = new HorizontalLayout(btnClose, btnLocateOnMap, btnViewContent);
            lytFooter.setFlexGrow(1, btnClose, btnLocateOnMap, btnViewContent);
            
            setHeader(scriptedQuery.getName());
            setContent(lytContent);
            setFooter(lytFooter);
            
            super.open();
            ZoomChangedEventListener zoomChangedEventListener = zoomChangedEvent-> {
                if (viewHeatmap != null) {
                    boolean dissipateOnZoom = mapProvider.getZoom() <= mapProvider.getMinZoomForLabels();
                    if (dissipateOnZoom != viewHeatmap.getDissipateOnZoom()) {
                        viewHeatmap.setDissipateOnZoom(dissipateOnZoom);
                        heatmap.setDissipateOnZoom(dissipateOnZoom);
                    }
                }
            };
            mapProvider.addZoomChangedEventListener(zoomChangedEventListener);
            addOpenedChangeListener(openedChangeEvent -> {
                if (!openedChangeEvent.isOpened()) {
                    if (viewHeatmap != null)
                        mapProvider.removeHeatmap(viewHeatmap);
                    mapProvider.removeZoomChangedEventListener(zoomChangedEventListener);
                }
            });
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
    }
}
