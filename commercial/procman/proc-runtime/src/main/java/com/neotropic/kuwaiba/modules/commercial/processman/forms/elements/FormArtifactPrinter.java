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
package com.neotropic.kuwaiba.modules.commercial.processman.forms.elements;

import com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.components.ComponentUpload;
import java.io.File;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ArtifactDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight; 
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleSequence;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Class to manage printable form artifacts.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FormArtifactPrinter {
    private final String processEnginePath;
    private final ArtifactDefinition artifactDefinition;
    private final FormStructure formStructure;
    private final TranslationService ts;
    
    public FormArtifactPrinter(String processEnginePath, ArtifactDefinition artifactDefinition, FormStructure formStructure, TranslationService ts) {
        Objects.requireNonNull(processEnginePath);
        Objects.requireNonNull(artifactDefinition);
        Objects.requireNonNull(formStructure);
        
        this.processEnginePath = processEnginePath;
        this.artifactDefinition = artifactDefinition;
        this.formStructure = formStructure;
        this.ts = ts;
    }
    
    public byte[] getBytes() {
        try {
            HashMap<String, Object> root = new HashMap();
            
            List<AbstractElement> elements = formStructure.getElements();

            for (AbstractElement element : elements) {

                if (element instanceof ElementGrid) {
                    SimpleSequence lst = new SimpleSequence((ObjectWrapper) null);

                    ElementGrid elementGrid = (ElementGrid) element;
                    String id = elementGrid.getId();

                    int columnsSize = elementGrid.getColums() != null ? elementGrid.getColums().size() : 0;

                    if (elementGrid.getRows() != null) {
                        List<List<Object>> rows = elementGrid.getRows();
                        for (int i = 0; i < rows.size(); i++) {
                            List row = rows.get(i);

                            for (int j = 0; j < columnsSize; j++) {
                                String value = null;

                                if (j < row.size()) {
                                    if (row.get(j) instanceof BusinessObjectLight)
                                        value = ((BusinessObjectLight) row.get(j)).getName();
                                    else
                                        value = row.get(j).toString();
                                }
                                root.put(id + i + j, value);
                                lst.add(value);
                            }
                        }
                    }
                    if (elementGrid.getRows() == null || 
                        (elementGrid.getRows() != null && elementGrid.getRows().isEmpty())) {

                        for (int j = 0; j < columnsSize; j += 1)
                            root.put(id + "0" + j, "");
                    }
                    root.put(id, lst);
                }
                else if (element instanceof AbstractElementField) {
                    AbstractElementField elementField = (AbstractElementField) element;

                    if (elementField.getId() != null) {
                        if (elementField instanceof ElementUpload) {
                            ElementUpload elementUpload = (ElementUpload) elementField;
                            if (elementUpload.getElementEventListener() instanceof ComponentUpload) {
                                ComponentUpload componentUpload = (ComponentUpload) elementUpload.getElementEventListener();
                                root.put(element.getId(), componentUpload.getUploadUrl());
                            }
                        } else {
                            String id = element.getId();

                            String value = "";

                            if (elementField.getValue() != null) {
                                if (elementField.getValue() instanceof BusinessObjectLight) {

                                    value = ((BusinessObjectLight) elementField.getValue()).getName();
                                }
                                else {

                                    value = elementField.getValue().toString();
                                }
                            }
                            root.put(id, value);
                        }
                    }
                }
            }
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
            cfg.setDirectoryForTemplateLoading(new File(processEnginePath + "/form/templates/"));
            cfg.setDefaultEncoding("UTF-8");
            
            Template temp = cfg.getTemplate(artifactDefinition.getPrintableTemplate());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Writer out = new OutputStreamWriter(baos);
            temp.process(root, out);
            return baos.toByteArray();
        } catch (IOException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        } catch (TemplateException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage(), 
                AbstractNotification.NotificationType.ERROR, 
                ts
            ).open();
        }
        return null;
    }
}
