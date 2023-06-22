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
package com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.artifacts;

import com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.components.UploadAnchor;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ArtifactDefinition;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class AttachmentArtifactRender extends AbstractArtifactRender {
    private final ApplicationEntityManager aem;
    private final TranslationService ts;
    private final ArtifactDefinition artifactDefinition;
    private final Artifact artifact;
    
    private UploadAnchor uploadAnchor;
    
    public AttachmentArtifactRender(ArtifactDefinition artifactDefinition, Artifact artifact, ApplicationEntityManager aem, TranslationService ts) {
        Objects.requireNonNull(aem);
        Objects.requireNonNull(ts);
        this.aem = aem;
        this.ts = ts;
        this.artifactDefinition = artifactDefinition;
        this.artifact = artifact;
    }
    
    @Override
    public Component render() {
        VerticalLayout lyt = new VerticalLayout();
        try {
            Label lbl = new Label(artifactDefinition != null ? new String(artifactDefinition.getDefinition()) : null);
            
            uploadAnchor = new UploadAnchor();
            
            String fileName = getValue();
            if (fileName != null && !fileName.isEmpty()) {
                String processPath = String.valueOf(aem.getConfiguration().get("processesPath")); //NOI18N
                
                byte[] bytes = Files.readAllBytes(Paths.get(String.format("%s/%s", processPath, fileName)));
                Anchor anchor = new Anchor(
                    new StreamResource(
                        fileName, 
                        () -> new ByteArrayInputStream(bytes)
                    ), 
                    fileName
                );
                anchor.setTarget("_blank"); //NOI18N
                uploadAnchor.getDiv().add(anchor);
            }
            lyt.add(lbl, uploadAnchor);
        } catch (IOException ex) {
            Logger.getLogger(AttachmentArtifactRender.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lyt;
    }
    
    @Override
    public byte[] getContent() {
        String fileName = getValue();
        
        if (uploadAnchor.getMemoryBuffer().getFileData() != null) {
            OutputStream outputStream = null;
            try {
                String processPath = String.valueOf(aem.getConfiguration().get("processesPath")); //NOI18N
                MemoryBuffer memoryBuffer = uploadAnchor.getMemoryBuffer();
                fileName = memoryBuffer.getFileName();
                File file = new File(String.format("%s/%s", processPath, fileName));
                outputStream = new FileOutputStream(file);
                IOUtils.copy(memoryBuffer.getInputStream(), outputStream);
                IOUtils.closeQuietly(outputStream);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(AttachmentArtifactRender.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(AttachmentArtifactRender.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(AttachmentArtifactRender.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        String strContent = "<artifact type=\"attachment\"><value>" + (fileName != null ? fileName : "") + "</value></artifact>";
        return strContent.getBytes();
    }
    
    public String getValue() {
        if (artifact != null) {
            try {
                byte[] content = artifact.getContent();

                XMLInputFactory xif = XMLInputFactory.newInstance();
                ByteArrayInputStream bais = new ByteArrayInputStream(content);
                XMLStreamReader reader = xif.createXMLStreamReader(bais);

                QName tagValue = new QName("value"); //NOI18N

                while (reader.hasNext()) {

                    int event = reader.next();

                    if (event == XMLStreamConstants.START_ELEMENT) {

                        if (reader.getName().equals(tagValue))
                            return reader.getElementText();
                    }
                }

            } catch (Exception ex) {
                //TODO: notification Attachment could not be found
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ts.getTranslatedString(""), 
                    AbstractNotification.NotificationType.ERROR, 
                    ts
                ).open();
            }
        }
        return null;
    }
}
