/*
 *  Copyright 2010-2022 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.core.navigation.explorers;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
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
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractExplorerWidget;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * An explorer that allows the user to see the file attachments related to an inventory object.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Component
public class AttachmentExplorer extends AbstractExplorerWidget<VerticalLayout> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    
    @Override
    public String getName() {
        return ts.getTranslatedString("module.navigation.explorers.attachments.title");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.navigation.explorers.attachments.description");
    }
    
    @Override
    public String appliesTo() {
        return "InventoryObject";
    }

    @Override
    public VerticalLayout build(BusinessObjectLight selectedObject) {
        try {
            VerticalLayout lytMain = new VerticalLayout();
            List<FileObjectLight> attachments = bem.getFilesForObject(selectedObject.getClassName(), selectedObject.getId());
            
            if (attachments.isEmpty())
                lytMain.add(new Label(ts.getTranslatedString("module.navigation.explorers.attachments.no-attachments")));
            else {
                VerticalLayout lytObjectFiles = new VerticalLayout();
                attachments.forEach( aFileObject -> {
                    Div divLinkToFile = new Div(new Label(aFileObject.getName()));
                    divLinkToFile.addClassName("link-like-label");
                    divLinkToFile.addClickListener( event -> {
                        try {
                            FileObject anAttachment = bem.getFile(aFileObject.getFileOjectId(), selectedObject.getClassName(), selectedObject.getId());
                            final StreamResource resource = new StreamResource(selectedObject.getId() + "-" + aFileObject.getFileOjectId(),
                                                () -> new ByteArrayInputStream(anAttachment.getFile()));
                            final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
                            UI.getCurrent().getPage().open(registration.getResourceUri().toString());
                        } catch (InventoryException ex) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                                    AbstractNotification.NotificationType.ERROR, ts).open();
                        }
                    });   
                    lytObjectFiles.add(divLinkToFile);                                    
                });
                Tab tabPs = new Tab(ts.getTranslatedString("module.navigation.explorers.attachments.object-files"));
                Div page1 = new Div();
                page1.setSizeFull();
                page1.add(lytObjectFiles);

                Tab tabFiles = new Tab(ts.getTranslatedString("module.navigation.explorers.attachments.related-files"));
                Div page2 = new Div();
                page2.add(createFilesTab(selectedObject));
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
                lytMain.add(tabs, pages);
            }
            lytMain.setSizeFull();
            return lytMain;
        } catch (InventoryException ex) {
            return new VerticalLayout(new Label(String.format(ts.getTranslatedString("module.general.messages.component-cant-be-loaded"), 
                    ex.getLocalizedMessage())));
        }
   }
    
   private VerticalLayout createFilesTab(BusinessObjectLight selectedObject) {

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
   
   private StreamResource createStreamResource(String name, byte[] ba) {
        return new StreamResource(name, () -> new ByteArrayInputStream(ba));                                
    } 
}
