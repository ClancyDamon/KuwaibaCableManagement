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
package com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.components;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;

/**
 * Uploading files and adds an anchor to the uploaded file.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class UploadAnchor extends VerticalLayout {
    private MemoryBuffer memoryBuffer;
    private Div div;
    
    public UploadAnchor() {
        memoryBuffer = new MemoryBuffer();
        Upload upload = new Upload(memoryBuffer);
        div = new Div();
        upload.addSucceededListener(succeededEvent -> {
            String fileName = succeededEvent.getFileName();
            div.removeAll();
            Anchor anchor = new Anchor(
                new StreamResource(
                    fileName, 
                    () -> memoryBuffer.getInputStream()
                ), 
                fileName
            );
            anchor.setTarget("_blank"); //NOI18N
            div.add(anchor);
        });
        upload.addFileRejectedListener(fileRejectedEvent -> {
            div.removeAll();
            div.add(new Paragraph(fileRejectedEvent.getErrorMessage()));
        });
        upload.getElement().addEventListener("file-remove", //NOI18N
            fileRemoveEvent -> div.removeAll()
        );
        add(upload, div);
    }
    
    public MemoryBuffer getMemoryBuffer() {
        return memoryBuffer;
    }
    
    public Div getDiv() {
        return div;
    }
}