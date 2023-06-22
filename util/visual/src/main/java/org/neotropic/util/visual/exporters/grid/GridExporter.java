/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.util.visual.exporters.grid;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.exporters.AbstractFormatter;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Manages data grid export to different formats
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class GridExporter {

    private Object dataSource;
    private final DataGridParser dataGridRetriever;
    private final List<AbstractFormatter> exporters;
    private final TranslationService ts;

    public Object getDataSource() {
        return dataSource;
    }

    public void setDataSource(Object dataSource) {
        this.dataSource = dataSource;
    }

    public GridExporter(TranslationService ts, Object dataSource, DataGridParser dataGridParser, AbstractFormatter ... supportedExporters) {
        super();
        this.dataSource = dataSource;   
        this.dataGridRetriever = dataGridParser;   
        this.ts = ts;
        this.exporters = Arrays.asList(supportedExporters);
    }
      
    public void open() {
        
        Anchor download = new Anchor();
        download.setId("anchorDownload");
        download.getElement().setAttribute("download", true);
        download.setClassName("hidden");
        Button btnAnchor = new Button(new Icon(VaadinIcon.DOWNLOAD_ALT), evt -> {
        });
        btnAnchor.getElement().getStyle().set("visibility", "hidden");
          download.getElement().setProperty("visibility", "hidden");
        download.add(btnAnchor);

        ComboBox<AbstractFormatter> cbxExporters = new ComboBox(ts.getTranslatedString("module.queries.export-to"));
        cbxExporters.setItems(exporters);
        cbxExporters.setItemLabelGenerator(item -> item.getDisplayName());
        cbxExporters.setWidthFull();
        cbxExporters.addValueChangeListener(listener -> {
            if (listener.getValue() != null) {
                final StreamRegistration regn = VaadinSession.getCurrent().getResourceRegistry().
                                    registerResource(createStreamResource("query_result" + listener.getValue().getExtension(), createReport(listener.getValue())));
                download.setHref(regn.getResourceUri().getPath());           
            }
        });
                
        ConfirmDialog dlgExport = new ConfirmDialog(ts, ts.getTranslatedString("module.queries.export"), ts.getTranslatedString("module.general.messages.ok"));
        dlgExport.getBtnConfirm().addClickListener( evt -> {
                    if (cbxExporters.getValue() != null) {
                        btnAnchor.clickInClient();
                        dlgExport.close();
                    }
                });
   
        
        dlgExport.setContent(new VerticalLayout(cbxExporters, download));
        dlgExport.setWidth("450px");
        dlgExport.open();
    }
    
    private StreamResource createStreamResource(String name, byte[] ba) {
        return new StreamResource(name, () -> new ByteArrayInputStream(ba));                                
    }  
    
    public byte[] createReport(AbstractFormatter exporter) {
        try {
            return exporter.format(dataGridRetriever.getData(dataSource));
        } catch (IOException ex) {
            Logger.getLogger(GridExporter.class.getName()).log(Level.SEVERE, null, ex);
             new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            return new byte [0];
        }
    }   
 
}
