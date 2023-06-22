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
package org.neotropic.kuwaiba.modules.core.navigation.actions;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import elemental.json.Json;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.NavigationModule;
import static org.neotropic.kuwaiba.modules.core.navigation.actions.ManageAttachmentsAction.PARAM_CLASS_NAME;
import static org.neotropic.kuwaiba.modules.core.navigation.actions.ManageAttachmentsAction.PARAM_FILE;
import static org.neotropic.kuwaiba.modules.core.navigation.actions.ManageAttachmentsAction.PARAM_FILE_NAME;
import static org.neotropic.kuwaiba.modules.core.navigation.actions.ManageAttachmentsAction.PARAM_OBJECT_ID;
import static org.neotropic.kuwaiba.modules.core.navigation.actions.DetachFileAction.PARAM_FILE_OBJECT_ID;
import org.neotropic.kuwaiba.modules.core.navigation.explorers.AttachmentExplorer;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * UI of Manage Attachments
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class ManageAttachmentsVisualAction extends AbstractVisualInventoryAction {
    /**
     * Business Object Parameter.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Reference to module ManageAttachmentsAction action.
     */
    @Autowired
    private ManageAttachmentsAction attachFileAction;
     /**
     * Reference to module DetachFileAction action.
     */
    @Autowired
    private DetachFileAction detachFileAction;
    /**
     * References to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    
    @Value("${bem.max-attachment-size}")
    private int maxAttachmentSizeInMb;
    

    public ManageAttachmentsVisualAction() {
        super(NavigationModule.MODULE_ID);
    }
    
    @Override
    public int getRequiredSelectedObjects() {
        return 1;
    }

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight businessObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
        if (businessObject != null) {
            Grid<FileObjectLight> grdFiles = new Grid<>();
            grdFiles.setHeightByRows(true);
            ConfirmDialog wdw = new ConfirmDialog(ts, ts.getTranslatedString("module.navigation.actions.manage-attachments.name"), "", "");
            wdw.setModal(false);
            wdw.setResizable(true);
            wdw.setWidth("75%");
            wdw.setMinHeight("500px");
            Label lblNoFiles = new Label(ts.getTranslatedString("module.ltman.no-files-found-for-object"));
            MemoryBuffer bufferIcon = new MemoryBuffer();
            Upload uploadIcon = new Upload(bufferIcon);
            uploadIcon.setWidthFull();
            uploadIcon.setMaxFiles(1);
            uploadIcon.setDropLabel(new Label(ts.getTranslatedString("module.queries.dropmessage")));
            uploadIcon.addSucceededListener(event -> {
                try {
                    byte[] imageData = IOUtils.toByteArray(bufferIcon.getInputStream());

                    ActionResponse execute = attachFileAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>(PARAM_CLASS_NAME, businessObject.getClassName()),
                            new ModuleActionParameter<>(PARAM_OBJECT_ID, businessObject.getId()),
                            new ModuleActionParameter<>(PARAM_FILE_NAME, event.getFileName()),
                            new ModuleActionParameter<>(PARAM_FILE, imageData)));

                    loadFiles(businessObject, grdFiles, lblNoFiles);
                    uploadIcon.getElement().setPropertyJson("files", Json.createArray());
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.queries.file-attached"),
                            AbstractNotification.NotificationType.INFO, ts).open();

                } catch (IOException | ModuleActionException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            });
            uploadIcon.addFileRejectedListener(event -> {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), event.getErrorMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            });

            grdFiles.addColumn(FileObjectLight::getName);
            grdFiles.addComponentColumn(item -> {
                Button btnDeleteFile = new Button(new Icon(VaadinIcon.TRASH), evt -> {

                    ConfirmDialog dlgConfirm = new ConfirmDialog(ts, ts.getTranslatedString("module.general.labels.confirmation"),
                            ts.getTranslatedString("module.general.labels.confirm-delete"),
                            ts.getTranslatedString("module.general.messages.ok"));
                    dlgConfirm.setWidth("500px");
                    dlgConfirm.getBtnConfirm().addClickListener(listener -> {
                        try {
                            ActionResponse execute = detachFileAction.getCallback().execute(new ModuleActionParameterSet(
                                    new ModuleActionParameter<>(PARAM_CLASS_NAME, businessObject.getClassName()),
                                    new ModuleActionParameter<>(PARAM_OBJECT_ID, businessObject.getId()),
                                    new ModuleActionParameter<>(PARAM_FILE_OBJECT_ID, item.getFileOjectId())));
                            loadFiles(businessObject, grdFiles, lblNoFiles);
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.queries.file-deleted"),
                                    AbstractNotification.NotificationType.INFO, ts).open();
                            dlgConfirm.close();
                        } catch (ModuleActionException ex) {
                            Logger.getLogger(ManageAttachmentsVisualAction.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                    dlgConfirm.open();
                });
                btnDeleteFile.setClassName("icon-button");
                Anchor download = new Anchor();
                download.setId("anchorDownload");
                download.getElement().setAttribute("download", true);
                download.setClassName("hidden");
                download.getElement().setAttribute("visibility", "hidden");
                Button btnDownloadAnchor = new Button();
                btnDownloadAnchor.getElement().setAttribute("visibility", "hidden");
                Button btnDownload = new Button(new Icon(VaadinIcon.DOWNLOAD));
                btnDownload.setClassName("icon-button");
                btnDownload.addClickListener(evt -> {
                    try {
                        FileObject fo = bem.getFile(item.getFileOjectId(), businessObject.getClassName(), businessObject.getId());
                        final StreamRegistration regn = VaadinSession.getCurrent().getResourceRegistry().
                                registerResource(createStreamResource(item.getName(), fo.getFile()));
                        download.setHref(regn.getResourceUri().getPath());
                        btnDownloadAnchor.clickInClient();
                    } catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                        Logger.getLogger(ManageAttachmentsVisualAction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                download.add(btnDownloadAnchor);

                return new HorizontalLayout(btnDeleteFile, btnDownload, download);
            });
            loadFiles(businessObject, grdFiles, lblNoFiles);
            VerticalLayout lytFiles = new VerticalLayout(uploadIcon, lblNoFiles, grdFiles);

            Tab tabPs = new Tab(ts.getTranslatedString("module.navigation.explorers.attachments.object-files"));
            Div page1 = new Div();
            page1.setSizeFull();
            page1.add(lytFiles);

            Tab tabFiles = new Tab(ts.getTranslatedString("module.navigation.explorers.attachments.related-files"));
            Div page2 = new Div();
            page2.add(createListItemFilesTab(businessObject));
            page2.setVisible(false);

            Map<Tab, com.vaadin.flow.component.Component> tabsToPages = new HashMap<>();
            tabsToPages.put(tabPs, page1);
            tabsToPages.put(tabFiles, page2);
            Tabs tabs = new Tabs(tabPs, tabFiles);
            Div pages = new Div(page1, page2);
            pages.setWidthFull();
            Set<com.vaadin.flow.component.Component> pagesShown = Stream.of(page1)
                    .collect(Collectors.toSet());

            tabs.addSelectedChangeListener(event -> {
                pagesShown.forEach(page -> page.setVisible(false));
                pagesShown.clear();
                com.vaadin.flow.component.Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
                selectedPage.setVisible(true);
                pagesShown.add(selectedPage);
            });     
            
            wdw.getBtnConfirm().setVisible(false);
            wdw.getBtnCancel().setText(ts.getTranslatedString("module.general.messages.close"));
            wdw.setContent(tabs, pages);
            return wdw;
        }
        return null;
    }
    
    private VerticalLayout createListItemFilesTab(BusinessObjectLight selectedObject) {

        Map<BusinessObjectLight, List<FileObjectLight>> attachments = bem.getFilesFromRelatedListTypeItems(selectedObject.getId());
        VerticalLayout lytFiles = new VerticalLayout();
        lytFiles.setSpacing(false);
        if (attachments.size() > 0) {   
            Grid<BusinessObjectLight> grdFiles = new Grid<>();
            grdFiles.addComponentColumn(item -> {
               BoldLabel lblLTI = new BoldLabel(item.getName());
               Label lblLTIClass = new Label(item.getClassName());
               lblLTIClass.setClassName("text-secondary");
               VerticalLayout lytItem = new VerticalLayout(lblLTI, lblLTIClass);
               lytItem.setSpacing(false);
               return lytItem;
            });
            grdFiles.setItems(attachments.keySet());
            grdFiles.addComponentColumn(item -> {
               VerticalLayout lytColFiles = new VerticalLayout();
               lytColFiles.setSpacing(false);
                for (FileObjectLight file : attachments.get(item)) {
                    Label lblFile = new Label(file.getName());
                    lblFile.setWidthFull();
                    Anchor download = new Anchor();
                    download.setId("anchorDownload");
                    download.getElement().setAttribute("download", true);
                    download.setClassName("hidden");
                    download.getElement().setAttribute("visibility", "hidden");
                    Button btnDownloadAnchor = new Button();
                    btnDownloadAnchor.getElement().setAttribute("visibility", "hidden");
                    Button btnDownload = new Button(new Icon(VaadinIcon.DOWNLOAD));
                    btnDownload.addClickListener(evt -> {
                       try {
                           FileObject fo = aem.getFile(file.getFileOjectId(), item.getClassName(), item.getId());
                           final StreamRegistration regn = VaadinSession.getCurrent().getResourceRegistry().
                                   registerResource(createStreamResource(file.getName(), fo.getFile()));  
                           download.setHref(regn.getResourceUri().getPath());
                           btnDownloadAnchor.clickInClient();
                       } catch (BusinessObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
                           Logger.getLogger(AttachmentExplorer.class.getName()).log(Level.SEVERE, null, ex);
                       }
                    });
                    download.add(btnDownloadAnchor);
                    HorizontalLayout lytFile = new HorizontalLayout(lblFile, download, btnDownload);  
                    lytFile.setAlignItems(FlexComponent.Alignment.CENTER);
                    lytFile.setWidthFull();
                    lytColFiles.add(lytFile);
                } 
                return lytColFiles;
            });      
            lytFiles.add(grdFiles);
        } else 
            lytFiles.add(new Label(ts.getTranslatedString("module.navigation.explorers.attachments.no-related-attachments")));
        return lytFiles;
    }

    @Override
    public AbstractAction getModuleAction() {
        return attachFileAction;
    }
    
    private StreamResource createStreamResource(String name, byte[] ba) {
        return new StreamResource(name, () -> new ByteArrayInputStream(ba));                                
    }
    
    public void loadFiles(BusinessObjectLight bol, Grid grid, Label lblNoFiles) {
        try {
            List<FileObjectLight> files = bem.getFilesForObject(bol.getClassName(), bol.getId());
            grid.setItems(files);
            grid.getDataProvider().refreshAll();
            lblNoFiles.setVisible(files.isEmpty());
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(ManageAttachmentsVisualAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
