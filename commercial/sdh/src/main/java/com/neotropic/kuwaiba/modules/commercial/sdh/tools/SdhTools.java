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
package com.neotropic.kuwaiba.modules.commercial.sdh.tools;

import com.neotropic.kuwaiba.modules.commercial.sdh.SdhView;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.mxgraph.MxGraphCanvas;
import org.neotropic.util.visual.mxgraph.exporters.MxGraphJpgExporter;

/**
 * Component with a set of tools available to work in an SDH view
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class SdhTools extends HorizontalLayout {
    
    private TranslationService ts;
    private ViewObjectLight theView;
    private PaperDialogAddObject sdhAddObject;
    private PaperDialogSearchObject sdhSearchObject;
    private Button btnAddObject;  
    private Button btnDetectConnections;  
    private Button btnSaveView;  
    private Button btnRemoveFromDatabase;  
    private Button btnRemoveFromView;  
    private MxGraphCanvas mxGraphCanvas;
    private Dialog dlgAddobject;
    private Button btnOpenView;
    private Button btnNewView;
    private Button btnRemoveView;
    private Button btnExportAsJPG;
    
    public Button getBtnOpenView() {
        return btnOpenView;
    }

    public void setBtnOpenView(Button btnOpenView) {
        this.btnOpenView = btnOpenView;
    }

    public Button getBtnNewView() {
        return btnNewView;
    }

    public void setBtnNewView(Button btnNewView) {
        this.btnNewView = btnNewView;
    }

    public Button getBtnRemoveView() {
        return btnRemoveView;
    }

    public void setBtnRemoveView(Button btnRemoveView) {
        this.btnRemoveView = btnRemoveView;
    }
    
    public ViewObjectLight getView() {
        return theView;
    }

    public void setView(ViewObjectLight theView) {
        this.theView = theView;
    }
    
    public SdhTools(SdhView sdhView, BusinessEntityManager bem, TranslationService ts) {
        
        this.ts = ts;
        this.mxGraphCanvas = sdhView.getMxgraphCanvas();
        
        btnOpenView = new Button(new Icon(VaadinIcon.FOLDER_OPEN_O));
        SdhTools.setButtonTitle(btnOpenView, ts.getTranslatedString("module.sdh.open-sdh-view"));
        btnOpenView.setClassName("icon-button");
        
        btnNewView = new Button(new Icon(VaadinIcon.FILE_ADD));
        SdhTools.setButtonTitle(btnNewView, ts.getTranslatedString("module.sdh.new-sdh-view"));
        btnNewView.setClassName("icon-button");
        
        btnRemoveView = new Button(new Icon(VaadinIcon.CLOSE_CIRCLE_O));
        btnRemoveView.setClassName("icon-button");
        btnRemoveView.setEnabled(false);
        SdhTools.setButtonTitle(btnRemoveView, ts.getTranslatedString("module.sdh.remove-view"));
           
        btnAddObject= new Button(new Icon(VaadinIcon.INSERT),
                e -> {
              openDlgAddobject();
        }); 
        setButtonTitle(btnAddObject, ts.getTranslatedString("module.sdh.add-objects-links"));
            
        btnDetectConnections = new Button(new Icon(VaadinIcon.CLUSTER),
                e -> {
              fireEvent(new DetectConnectionsEvent(this, false));
        }); 
        setButtonTitle(btnDetectConnections, ts.getTranslatedString("module.mpls.detect-connections"));
        
        sdhAddObject = new PaperDialogAddObject(ts, bem, mxGraphCanvas);
        sdhAddObject.addNewObjectListener(evt -> {
            fireEvent(new NewObjectEvent(this, false, (BusinessObjectLight) evt.getObject()));
            dlgAddobject.close();
        });
        initDlgAddobject();
        
        sdhSearchObject = new PaperDialogSearchObject(ts, bem, mxGraphCanvas);
        sdhSearchObject.addSelectObjectListener(evt -> {
            fireEvent(new SelectObjectEvent(this, false, (BusinessObjectLight) evt.getObject()));
        });
     
        btnSaveView = new Button(new Icon(VaadinIcon.DOWNLOAD), evt -> {
            fireEvent(new SaveViewEvent(this, false));
        });
        btnSaveView.getElement().setProperty("title", ts.getTranslatedString("module.sdh.save-view"));
        setButtonTitle(btnSaveView, ts.getTranslatedString("module.sdh.save-view"));        
        
        btnRemoveFromDatabase = new Button( new Icon(VaadinIcon.TRASH),
                e -> {
                    fireEvent(new DeleteObjectPermanentlyEvent(this, false));
        });
        btnRemoveFromDatabase.setEnabled(false);
        setButtonTitle(btnRemoveFromDatabase, ts.getTranslatedString("module.sdh.delete-from-database-view"));        
              
        btnRemoveFromView = new Button(new Icon(VaadinIcon.FILE_REMOVE),
                e -> {
                    fireEvent(new DeleteObjectEvent(this, false));
        }); 
        btnRemoveFromView.setEnabled(false);
        setButtonTitle(btnRemoveFromView, ts.getTranslatedString("module.sdh.delete-from-view"));   
        
        Anchor download = new Anchor();
        download.setId("anchorDownload");
        download.getElement().setAttribute("download", true);
        download.setClassName("hidden");
        download.getElement().setAttribute("visibility", "hidden");
        Button btnDownloadAnchor = new Button();
        btnDownloadAnchor.getElement().setAttribute("visibility", "hidden");
        btnExportAsJPG = new Button(new Icon(VaadinIcon.FILE_PICTURE), evt -> {
            if (mxGraphCanvas != null) {
                byte [] data = sdhView.getAsImage(new MxGraphJpgExporter());
                String name = theView.getName().trim().toLowerCase().replaceAll("\\s+", "_");
                final StreamRegistration regn = VaadinSession.getCurrent().getResourceRegistry().
                               registerResource(createStreamResource(name + "_" +
                                       LocalDate.now().toString()  +".jpg", data));
                download.setHref(regn.getResourceUri().getPath());
                btnDownloadAnchor.clickInClient();
            }
        });
        btnExportAsJPG.setClassName("icon-button");
        setButtonTitle(btnExportAsJPG, ts.getTranslatedString("module.general.label.download-as-jpg"));
        btnExportAsJPG.setEnabled(false);
        download.add(btnDownloadAnchor);
         
        this.setAlignItems(Alignment.CENTER);
        add(sdhSearchObject,  btnOpenView, btnNewView, btnRemoveView, btnAddObject, btnRemoveFromDatabase, btnRemoveFromView, btnExportAsJPG, download);
        this.setSpacing(false);
    }
    
    public Registration addNewObjectListener(ComponentEventListener<NewObjectEvent> listener) {
        return addListener(NewObjectEvent.class, listener);
    }
    
    public Registration addSelectObjectListener(ComponentEventListener<SelectObjectEvent> listener) {
        return addListener(SelectObjectEvent.class, listener);
    }
    
    public Registration addNewConnectionListener(ComponentEventListener<NewConnectionEvent> listener) {
        return addListener(NewConnectionEvent.class, listener);
    }
    
    public Registration addSaveViewListener(ComponentEventListener<SaveViewEvent> listener) {
        return addListener(SaveViewEvent.class, listener);
    }
    
    public Registration addDeleteObjectListener(ComponentEventListener<DeleteObjectEvent> listener) {
        return addListener(DeleteObjectEvent.class, listener);
    }
    
    public Registration addDeleteObjectPermanentlyObjectListener(ComponentEventListener<DeleteObjectPermanentlyEvent> listener) {
        return addListener(DeleteObjectPermanentlyEvent.class, listener);
    }
    
    public Registration AddExistingConnectionListener(ComponentEventListener<AddExistingConnectionEvent> listener) {
        return addListener(AddExistingConnectionEvent.class, listener);
    }
    
    public Registration AddDetectConnectionsListener(ComponentEventListener<DetectConnectionsEvent> listener) {
        return addListener(DetectConnectionsEvent.class, listener);
    }

    private void initDlgAddobject() {
        dlgAddobject = new Dialog();
        dlgAddobject.setWidth("450px");
        dlgAddobject.setDraggable(true);
          Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), evt -> {
            dlgAddobject.close();
        });
        VerticalLayout lytContent = new VerticalLayout(sdhAddObject, btnCancel);
        dlgAddobject.add(lytContent);
    }

    private void openDlgAddobject() {
        dlgAddobject.open();
    }
   
    public class NewObjectEvent extends ComponentEvent<SdhTools> {
        private final BusinessObjectLight object;
        public NewObjectEvent(SdhTools source, boolean fromClient, BusinessObjectLight object) {
            super(source, fromClient);
            this.object = object;
        }
        public BusinessObjectLight getObject() {
            return object;
        }
    }
    
    public class SelectObjectEvent extends ComponentEvent<SdhTools> {
        private final BusinessObjectLight object;
        public SelectObjectEvent(SdhTools source, boolean fromClient, BusinessObjectLight object) {
            super(source, fromClient);
            this.object = object;
        }
        public BusinessObjectLight getObject() {
            return object;
        }
    }
    
    public class NewConnectionEvent extends ComponentEvent<SdhTools> {
        public NewConnectionEvent(SdhTools source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    public class AddExistingConnectionEvent extends ComponentEvent<SdhTools> {
        public AddExistingConnectionEvent(SdhTools source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    public class SaveViewEvent extends ComponentEvent<SdhTools> {
        public SaveViewEvent(SdhTools source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    public class DeleteObjectEvent extends ComponentEvent<SdhTools> {
        public DeleteObjectEvent(SdhTools source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    public class DeleteObjectPermanentlyEvent extends ComponentEvent<SdhTools> {
        public DeleteObjectPermanentlyEvent(SdhTools source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    public class DetectConnectionsEvent extends ComponentEvent<SdhTools> {
        public DetectConnectionsEvent(SdhTools source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    /**
     * Function that enables/disables main functionality buttons 
     * @param enable true to enable the buttons, false otherwise
     */
    public void setGeneralToolsEnabled(boolean enable) {     
        btnSaveView.setEnabled(enable);
        btnDetectConnections.setEnabled(enable);
        sdhSearchObject.setEnabled(enable);
        btnAddObject.setEnabled(enable);
        btnRemoveView.setEnabled(enable);
        btnExportAsJPG.setEnabled(enable);
    }
    
    /**
     * Function that enables/disables the buttons that depends of object selection events 
     * @param enable true to enable the buttons, false otherwise
     */
    public void setSelectionToolsEnabled(boolean enable) {
        btnRemoveFromDatabase.setEnabled(enable);
        btnRemoveFromView.setEnabled(enable);
    }
    
    /**
     * Set the title/tool tip for the given button
     * @param button the button to be set
     * @param title the title to be added
     */
    public static void setButtonTitle(Button button, String title) {
        button.getElement().setProperty("title", title);     
        button.setClassName("icon-button");
    }
    
    private StreamResource createStreamResource(String name, byte[] ba) {
        return new StreamResource(name, () -> new ByteArrayInputStream(ba));                                
    }
    
}
