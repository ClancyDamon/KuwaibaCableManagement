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

package org.neotropic.kuwaiba.modules.optional.reports;

import com.neotropic.flow.component.aceeditor.AceEditor;
import com.neotropic.flow.component.aceeditor.AceMode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.reports.actions.DeleteParameterVisualAction;
import org.neotropic.kuwaiba.modules.optional.reports.actions.DeleteReportVisualAction;
import org.neotropic.kuwaiba.modules.optional.reports.actions.NewClassReportVisualAction;
import org.neotropic.kuwaiba.modules.optional.reports.actions.NewInventoryReportVisualAction;
import org.neotropic.kuwaiba.modules.optional.reports.actions.NewParameterVisualAction;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for Reporting. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
@Route(value = "reports", layout = ReportsLayout.class)
public class ReportsUI extends VerticalLayout implements HasDynamicTitle, AbstractUI {
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * factory to build resources from data source
     */  
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * the visual action to create a new class
     */
    @Autowired
    private NewClassReportVisualAction newClassReportVisualAction;
    /**
     * the visual action to delete a class
     */
    @Autowired
    private DeleteReportVisualAction deleteReportVisualAction;
    /**
     * the visual action to create a new class
     */
    @Autowired
    private NewInventoryReportVisualAction newInventoryReportVisualAction;
    /**
     * the visual action to create a new attribute
     */
    @Autowired
    private NewParameterVisualAction newParameterVisualAction;
   /**
     * the visual action to delete  attribute
     */
    @Autowired
    private DeleteParameterVisualAction deleteParameterVisualAction;
     /**
     * factory to build resources from data source
     */ 
    private Grid<ReportMetadataLight> tblClassLevelReports;
     /**
     * factory to build resources from data source
     */ 
    private Grid<ReportMetadataLight> tblInventoryReports;               
    /**
     * combo filter for inventory tree
     */   
    private ComboBox<ClassMetadataLight> cbxFilterClassReports;
     /**
     * combo filter for list type tree
     */   
    private ComboBox<ReportMetadataLight> cbxFilterInventoryReports;
    /**
     * The grid with the list task parameters
    */
    private final Grid<StringPair> tblParameters;
     /**
     * Object to save the select task parameter
     */
    private StringPair currentParameter;
    /**
     * Field to edit the report name
     */
    private TextField txtName;
     /**
     * Field to edit the report description
     */
    private TextArea txtDescription;
    /**
     * check to set enable/disable the report
     */
    private Checkbox chckEnabled;
    /**
     * AceEditor instance to edit the script
     */
    private AceEditor editorScript;
    /**
     * layout enclosing the parameter report actions and data grid
     */
    private VerticalLayout lytParameters;
    /**
     * Button instance to execute the save report action
     */
    private Button btnSaveReport;
    /**
     * Button instance to execute the run report action
     */
    private Button btnRunReport;
    /**
     * Button instance to execute the delete report action
     */
    private Button btnDeleteReport;
    /**
    * Layout enclosing the report content
    */
    private VerticalLayout lytReportContent;
    /**
     * boolean to know the type of the selected report
     */
    private boolean isClassLevelCurrentReport;
    /*
     label to show the report name
    */
    private H4 lblReportNameTitle;
    /*
     label to show the class level report related class
    */
    private Label lblReportClass;
    /**
     * listener to new class action
     */
    private ActionCompletedListener listenerNewClassReportAction;
    /**
     * listener to new class action
     */
    private  ActionCompletedListener listenerNewInventoryReportAction;
     /**
     * listener to new class action
     */
    private ActionCompletedListener listenerNewParameterAction;
     /**
     * listener to new class action
     */
    private ActionCompletedListener listenerDeleteParameterAction;
      /**
     * listener to delete report action
     */
    private ActionCompletedListener listenerDeleteReportAction;
    /**
     * Reference to the selected report
     */
    private ReportMetadataLight selectedReport;
    
    private ClassMetadataLight selectedClass;

    public ReportsUI() {
        super();
        setSizeFull();
        tblParameters = new Grid<>();
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
         this.newClassReportVisualAction.unregisterListener(listenerNewClassReportAction);
         this.newInventoryReportVisualAction.unregisterListener(listenerNewInventoryReportAction);
         this.deleteParameterVisualAction.unregisterListener(listenerDeleteParameterAction);
         this.newParameterVisualAction.unregisterListener(listenerNewParameterAction);
         this.deleteReportVisualAction.unregisterListener(listenerDeleteReportAction);
    }
    
    public void showActionCompledMessages(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();                                          
            } catch (Exception ex) {
                Logger.getLogger(ReportsUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
 
    }

    /**
     * Initialize the class report tree
     */
    private void initializeClassReportsTree() {
        try {
            List<ClassMetadataLight> inventoryObjectClasses = mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, true, true);

            List<ReportMetadataLight> classReports = bem.getClassLevelReports(Constants.CLASS_INVENTORYOBJECT, true, true);
            tblClassLevelReports = new Grid<>();
            tblClassLevelReports.setItems(classReports);
            tblClassLevelReports.addColumn(ReportMetadataLight::getName)
                    .setKey(ts.getTranslatedString("module.general.labels.name"));
            
            
            cbxFilterClassReports = new ComboBox<>(ts.getTranslatedString("module.reporting.labels.choose-class"));
            cbxFilterClassReports.setWidthFull();
            cbxFilterClassReports.setItems(inventoryObjectClasses);
            cbxFilterClassReports.setClearButtonVisible(true);
            cbxFilterClassReports.setItemLabelGenerator(ClassMetadataLight::getName);

            cbxFilterClassReports.addValueChangeListener(ev -> {
              try { 
                selectedClass = ev.getValue();
                if (ev.getValue() == null)
                    tblClassLevelReports.setItems(bem.getClassLevelReports(Constants.CLASS_INVENTORYOBJECT, true, true));
                else
                    tblClassLevelReports.setItems(bem.getClassLevelReports(ev.getValue().getName(), false, true));
                tblClassLevelReports.getDataProvider().refreshAll();
              } catch (MetadataObjectNotFoundException ex) {
                  new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                    AbstractNotification.NotificationType.ERROR, ts).open();
                  Logger.getLogger(ReportsUI.class.getName()).log(Level.SEVERE, null, ex);
              }
            });

        tblClassLevelReports.addItemClickListener(item ->  {      
            selectedReport = (ReportMetadataLight) item.getItem();
            lytReportContent.setVisible(true);
            isClassLevelCurrentReport = true;
            updateReportContent(isClassLevelCurrentReport);
                
        });
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                    AbstractNotification.NotificationType.ERROR, ts).open();
            Logger.getLogger(ReportsUI.class.getName()).log(Level.SEVERE, null, ex);
        }     
    }
    /**
     * Initialize the parameters grid
     */
    private void buildTaskParametersGrid() { 
        tblParameters.addColumn(StringPair::getKey)
                .setHeader(ts.getTranslatedString("module.reporting.parameters.name"));
        tblParameters.addComponentColumn(evt -> {
          Button btnDeleteParameter = new Button(new Icon(VaadinIcon.TRASH),
                (event) -> {
                    this.deleteParameterVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("parameter", evt.getKey()),
                    new ModuleActionParameter("report", selectedReport))).open();
                });
          return btnDeleteParameter;
        }).setHeader("");
        tblParameters.addItemClickListener(event -> {
            currentParameter = event.getItem();
        });     
    }
       
    private void initializeGridInventoryReports() {
        
        try {
            tblInventoryReports = new Grid();
            List<ReportMetadataLight> inventoryReports = bem.getInventoryLevelReports(true);
            tblInventoryReports.setItems(inventoryReports);
            tblInventoryReports.addThemeVariants(GridVariant.LUMO_COMPACT);
            tblInventoryReports.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
            tblInventoryReports.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
            tblInventoryReports.addThemeVariants(GridVariant.LUMO_NO_BORDER);
            tblInventoryReports.addColumn(ReportMetadataLight::getName)
                    .setHeader(ts.getTranslatedString("module.general.labels.name"))
                    .setKey(ts.getTranslatedString("module.general.labels.name"));
            
            tblInventoryReports.addItemClickListener((ItemClickEvent<ReportMetadataLight> ev) -> {
                try {
                    selectedReport = ev.getItem();
                    isClassLevelCurrentReport = false;
                    lytReportContent.setVisible(true);
                    updateReportContent(isClassLevelCurrentReport);
                } catch (Exception ex) {
                    
                }
            });
                    
        cbxFilterInventoryReports = new ComboBox<>(ts.getTranslatedString("module.general.labels.filter"));
        cbxFilterInventoryReports.setWidthFull();
        cbxFilterInventoryReports.setItems(inventoryReports);
        cbxFilterInventoryReports.setClearButtonVisible(true);
        cbxFilterInventoryReports.setItemLabelGenerator(ReportMetadataLight::getName);
        cbxFilterInventoryReports.addValueChangeListener(ev -> {
            if (ev.getValue() == null)
                tblInventoryReports.setItems(inventoryReports);
            else
                tblInventoryReports.setItems(Arrays.asList(ev.getValue()));
            });
        } catch (ApplicationObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                    AbstractNotification.NotificationType.ERROR, ts).open();
            Logger.getLogger(ReportsUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Updates the grid that shows the inventory reports.
     */
    private void updateInventoryReports() {
        try {
            List<ReportMetadataLight> inventoryReports = bem.getInventoryLevelReports(true);
            tblInventoryReports.setItems(inventoryReports);
            tblInventoryReports.getDataProvider().refreshAll();
            cbxFilterInventoryReports.setItems(inventoryReports);
        } catch (ApplicationObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                    AbstractNotification.NotificationType.ERROR, ts).open();
            Logger.getLogger(ReportsUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Update the grid that shows the class Level reports.
     */
    private void updateClassLevelReports() {
        try {
            String filterClass = Constants.CLASS_INVENTORYOBJECT;
            if (selectedClass != null)
                filterClass = selectedClass.getName();
            List<ReportMetadataLight> classReports = bem.getClassLevelReports(filterClass, true, true);       
            tblClassLevelReports.setItems(classReports);
            tblClassLevelReports.getDataProvider().refreshAll();
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                    AbstractNotification.NotificationType.ERROR, ts).open();
            Logger.getLogger(ReportsUI.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    /**
     * Update the report content with the current selected report
     * @param classLevelReport boolean to know if the current report is a  class level report
     */
    private void updateReportContent(boolean classLevelReport) {
         if (selectedReport != null) {
             try {       
                ReportMetadata rep = bem.getReport(selectedReport.getId());

                selectedReport.setName(rep.getName());
                selectedReport.setDescription(rep.getDescription());
                selectedReport.setEnabled(rep.isEnabled());
                lblReportNameTitle.setText(selectedReport.getName());
                txtName.setValue(selectedReport.getName());
                txtDescription.setValue(selectedReport.getDescription());
                chckEnabled.setValue(selectedReport.isEnabled());
                editorScript.setValue(rep.getScript());
                if (classLevelReport) {
                    lytParameters.setVisible(false);
                    btnRunReport.setVisible(false);
                    lblReportClass.setVisible(true);
                } else {
                    lytParameters.setVisible(true);
                    tblParameters.setItems(rep.getParameters());
                    tblParameters.getDataProvider().refreshAll();
                    btnRunReport.setVisible(true);
                    lblReportClass.setVisible(false);
                }
             } catch (ApplicationObjectNotFoundException ex) {
                 new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                    AbstractNotification.NotificationType.ERROR, ts).open();
                 Logger.getLogger(ReportsUI.class.getName()).log(Level.SEVERE, null, ex);
             }
         } 
    }
    /**
     * Save the selected report with the current data
     */
    private void saveCurrentReport() {
        if (selectedReport != null) {
            try {
                selectedReport.setName(txtName.getValue());
                selectedReport.setDescription(txtDescription.getValue());
                selectedReport.setEnabled(chckEnabled.getValue());
                lblReportNameTitle.setText(txtName.getValue());
                bem.updateReport(selectedReport.getId(), txtName.getValue(), txtDescription.getValue(), 
                        chckEnabled.getValue(), ReportMetadataLight.TYPE_HTML, editorScript.getValue());
                if (isClassLevelCurrentReport)
                    tblClassLevelReports.getDataProvider().refreshAll();
                else 
                    tblInventoryReports.getDataProvider().refreshItem(selectedReport);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.information"), ts.getTranslatedString("module.reporting.report-saved"), 
                    AbstractNotification.NotificationType.INFO, ts).open();
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                    AbstractNotification.NotificationType.ERROR, ts).open();
                Logger.getLogger(ReportsUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void resetView() {
        txtName.setValue("");
        txtDescription.setValue("");
        txtName.setValue("");
    }

    private void runReport() {
        if (selectedReport != null) {
            if (!isClassLevelCurrentReport) {
                try {
                    ReportMetadata rep = bem.getReport(selectedReport.getId());
                    if (!rep.getParameters().isEmpty())
                        createDlgExecInventoryReport(rep.getParameters());
                    else
                        executeInventoryReport(rep.getParameters());
                } catch (ApplicationObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                    AbstractNotification.NotificationType.ERROR, ts).open();
                    Logger.getLogger(ReportsUI.class.getName()).log(Level.SEVERE, null, ex);
                }       
            } 
        } 
    }
    /**
     * Create a new dialog to show the parameters when any inventory report is executed
     * @param parameters the list of parameters
     */
    private void createDlgExecInventoryReport(List<StringPair> parameters) {
        
        VerticalLayout lytContent = new VerticalLayout();
        VerticalLayout lytParams = new VerticalLayout();
        List<TextField> lstFields = new ArrayList<>();
        parameters.stream().map(param -> {
            TextField txtParam = new TextField(param.getKey());
            txtParam.setId(param.getKey());
            return txtParam;
        }).map(txtParam -> {
            lytParams.add(txtParam);
            return txtParam;
        }).forEachOrdered(txtParam -> {
            lstFields.add(txtParam);
        });
        Dialog dlgParameters = new Dialog();
        dlgParameters.setWidth("400px");
        Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"), evt -> {
            for (int i = 0; i < lstFields.size(); i++) {
                parameters.get(i).setValue(lstFields.get(i).getValue());
            }
            executeInventoryReport(parameters);
            dlgParameters.close();
        });
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), evt -> {
            dlgParameters.close();
        });
        HorizontalLayout lytActions = new HorizontalLayout(btnOk, btnCancel);
        lytContent.add(lytParams, lytActions);
        dlgParameters.add(lytContent);
        dlgParameters.open();
    }

    private void executeInventoryReport(List<StringPair> parameters) {
        try {
            byte[] reportBody = bem.executeInventoryLevelReport(selectedReport.getId(),
                    parameters);
            
            final StreamResource resource = new StreamResource("Report",
                () -> new ByteArrayInputStream(reportBody));
            resource.setContentType(ReportMetadataLight.getMimeTypeForReport(selectedReport.getType()));         
            final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
            UI.getCurrent().getPage().open(registration.getResourceUri().toString());
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                    AbstractNotification.NotificationType.INFO, ts).open();
            Logger.getLogger(ReportsUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.reporting.title");
    }

    @Override
    public void initContent() {
        setSizeFull();
        listenerNewClassReportAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {          
                updateClassLevelReports();
                showActionCompledMessages(ev);      
        }; 
        this.newClassReportVisualAction.registerActionCompletedLister(listenerNewClassReportAction);
        
        listenerNewInventoryReportAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {          
            updateInventoryReports();
            showActionCompledMessages(ev);
        };
        this.newInventoryReportVisualAction.registerActionCompletedLister(listenerNewInventoryReportAction);
        
        listenerDeleteReportAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {          
            resetView();
            selectedReport = null;
            lytReportContent.setVisible(false);
            if (isClassLevelCurrentReport)
                updateClassLevelReports();
            else 
                updateInventoryReports();
            showActionCompledMessages(ev);
        }; 
        this.deleteReportVisualAction.registerActionCompletedLister(listenerDeleteReportAction);
        
        listenerNewParameterAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {          
            updateReportContent(false);
            showActionCompledMessages(ev);
        }; 
        this.newParameterVisualAction.registerActionCompletedLister(listenerNewParameterAction);
        
        listenerDeleteParameterAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {          
            updateReportContent(false);
            showActionCompledMessages(ev);
        }; 
        this.deleteParameterVisualAction.registerActionCompletedLister(listenerDeleteParameterAction);
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(25); 
               
        initializeClassReportsTree();
        initializeGridInventoryReports();
        buildTaskParametersGrid();
        
        Button btnNewClassReport = new Button(this.newClassReportVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                    this.newClassReportVisualAction.getVisualComponent(new ModuleActionParameterSet(new ModuleActionParameter("class", selectedClass))).open();
                });
        HorizontalLayout lylActions = new HorizontalLayout(btnNewClassReport);
        VerticalLayout lytClassReportsTree = new VerticalLayout(lylActions, cbxFilterClassReports, tblClassLevelReports);
        lytClassReportsTree.setPadding(false);
        lytClassReportsTree.setSpacing(false);

        Button btnNewInventoryReport = new Button(this.newInventoryReportVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                     this.newInventoryReportVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
                });
        HorizontalLayout lylInventoryActions = new HorizontalLayout(btnNewInventoryReport);
        VerticalLayout lytInventoryReports = new VerticalLayout(lylInventoryActions, cbxFilterInventoryReports, tblInventoryReports);
        lytInventoryReports.setPadding(false);
        lytInventoryReports.setSpacing(false);
        
        Tab tab1 = new Tab(ts.getTranslatedString("module.reporting.class-level-reports"));
        Div page1 = new Div();
        page1.setSizeFull();
        page1.add(lytClassReportsTree);

        Tab tab2 = new Tab(ts.getTranslatedString("module.reporting.Inventory-level-reports"));
        Div page2 = new Div();
        page2.add(lytInventoryReports);
        page2.setVisible(false);
        
        Map<Tab, Component> tabsToPages = new HashMap<>();
        tabsToPages.put(tab1, page1);
        tabsToPages.put(tab2, page2);
        Tabs tabs = new Tabs(tab1, tab2);
        Div pages = new Div(page1, page2);
        pages.setWidthFull();
        Set<Component> pagesShown = Stream.of(page1)
                .collect(Collectors.toSet());

        tabs.addSelectedChangeListener(event -> {
            pagesShown.forEach(page -> page.setVisible(false));
            pagesShown.clear();
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
            pagesShown.add(selectedPage);
        });
  
        lblReportNameTitle = new H4(); 
        lblReportNameTitle.getElement().getStyle().set("margin-top", "0");
        lblReportClass = new Label(); 
        txtName = new TextField(ts.getTranslatedString("module.general.labels.name"), ts.getTranslatedString("module.general.labels.name"));
        txtName.setWidthFull();
        txtDescription = new TextArea(ts.getTranslatedString("module.general.labels.description"), ts.getTranslatedString("module.general.labels.description"));
        txtDescription.setHeight("100px");
        txtDescription.setWidthFull();
        chckEnabled = new Checkbox(ts.getTranslatedString("module.reporting.enabled"));
        Button btnAddParameter = new Button(this.newParameterVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                     this.newParameterVisualAction.getVisualComponent(new ModuleActionParameterSet(
                      new ModuleActionParameter("report", selectedReport))).open();
                });      
        lytParameters = new VerticalLayout(btnAddParameter, tblParameters);      
        VerticalLayout lytReportInfo = new VerticalLayout(txtName, txtDescription, chckEnabled);
        lytReportInfo.setWidth("500px");
        HorizontalLayout lytDlgReportInfoContent = new HorizontalLayout(lytReportInfo, lytParameters);
        Dialog dlgReportInfo = new Dialog(lytDlgReportInfoContent);
        Button btnSaveReportInfo = new Button(ts.getTranslatedString("module.general.messages.save"), new Icon(VaadinIcon.DOWNLOAD),  evt -> {
            saveCurrentReport();
            dlgReportInfo.close();
        });
        Button btnCancelReportInfo = new Button(ts.getTranslatedString("module.general.messages.cancel"), (e) -> {
            dlgReportInfo.close();
        });
        dlgReportInfo.add(new HorizontalLayout(btnSaveReportInfo, btnCancelReportInfo));
         
        Button btnEditReport = new Button(ts.getTranslatedString("module.reporting.edit-properties"), new Icon(VaadinIcon.EDIT),  evt -> {
            dlgReportInfo.open();
        });
        btnSaveReport = new Button(ts.getTranslatedString("module.general.messages.save"), new Icon(VaadinIcon.DOWNLOAD),  evt -> {
            saveCurrentReport();
        });
        btnDeleteReport = new Button(this.deleteReportVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.TRASH),
                (event) -> {
                     this.deleteReportVisualAction.getVisualComponent(new ModuleActionParameterSet(
                      new ModuleActionParameter("report", selectedReport))).open();
        });
        btnRunReport = new Button(ts.getTranslatedString("module.reporting.save-and-execute"), new Icon(VaadinIcon.PLAY), evt -> {
            saveCurrentReport();
            runReport();
        });
        HorizontalLayout lytReportActions = new HorizontalLayout();
        HorizontalLayout lytReportTitle = new HorizontalLayout(lblReportNameTitle, lblReportClass);
        lytReportTitle.setPadding(false);
        lytReportTitle.setMargin(false);
        lytReportTitle.setAlignItems(Alignment.BASELINE);
        lytReportActions.addAndExpand(lytReportTitle);
        lytReportActions.add(btnEditReport, btnSaveReport, btnDeleteReport, btnRunReport);
        lytReportActions.setAlignItems(Alignment.BASELINE);
        lytReportActions.setWidthFull();
        editorScript = new AceEditor();
        editorScript.setMode(AceMode.groovy);
        editorScript.setHeight("100%");
        lytReportContent = new VerticalLayout(lytReportActions, editorScript);       
        lytReportContent.setVisible(false);
        lytReportContent.setMargin(false);
        lytReportContent.setSpacing(false);
        HorizontalLayout lytSecContent = new HorizontalLayout();
        lytSecContent.add(lytReportContent);
        lytSecContent.setPadding(false);
        lytSecContent.setMargin(false);
        lytSecContent.setSpacing(false);
        VerticalLayout lytTrees = new VerticalLayout(tabs, pages);
        lytTrees.setPadding(false);
        lytTrees.setSizeFull();
        splitLayout.addToPrimary(lytTrees);
        splitLayout.setSplitterPosition(28);
        splitLayout.addToSecondary(lytSecContent);
                    
        add(splitLayout);
    }
}
