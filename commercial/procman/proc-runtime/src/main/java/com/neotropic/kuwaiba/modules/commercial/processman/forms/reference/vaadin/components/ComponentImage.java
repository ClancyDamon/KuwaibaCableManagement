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

import com.neotropic.kuwaiba.modules.commercial.processman.forms.components.uielement.AbstractUiElement;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.ElementImage;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.EventDescriptor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UI element to render the {@link ElementImage image} element
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentImage extends AbstractUiElement<ElementImage, Image> {
    private final String processEnginePath;

    public ComponentImage(ElementImage element, String processEnginePath) {
        super(element, new Image());
        Objects.requireNonNull(processEnginePath);
        this.processEnginePath = processEnginePath;
    }

    @Override
    protected void postConstruct() {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(String.format("%s/%s/%s", 
                processEnginePath, "form/img", getElement().getValue() //NOI18N
            )));
            StreamResource resource = new StreamResource(
                getElement().getValue(), 
                () -> new ByteArrayInputStream(bytes)
            );
            getUiElement().setAlt(getElement().getValue());
            getUiElement().setSrc(resource);
        } catch (IOException ex) {
            Logger.getLogger(ComponentImage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setId(String id) {
        getUiElement().setId(id);
    }

    @Override
    public void setWidth(String width) {
        getUiElement().setWidth(width);
    }

    @Override
    public void setHeight(String height) {
        getUiElement().setHeight(height);
    }

    @Override
    public void onElementEvent(EventDescriptor event) {}
}
