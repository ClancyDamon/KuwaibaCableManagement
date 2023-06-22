/*
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.elementUi;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;

/**
 *
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class TextFieldElementUi extends TextField implements ElementUi {

    private String elementUiId;
    private String elementUiWidth;
    private String elementUiMinWidth;
    private String elementUiHeight;
    private String elementUiValue;
    private String elementUiEnabled;
    private String elementUiOnLoad;
    private String elementUiDataType;
    private String elementUiHidden;
    private String elementUiOnPropertyChange;
    private String elementUiMandatory;
    
    public TextFieldElementUi() { }

    @Override
    public String getElementUiId() {
        return elementUiId;
    }

    @Override
    public void setElementUiId(String elementUiId) {
        this.elementUiId = elementUiId;
    }

    @Override
    public String getElementUiWidth() {
        return elementUiWidth;
    }

    @Override
    public void setElementUiWidth(String elementUiWidth) {
        this.elementUiWidth = elementUiWidth;
    }

    @Override
    public String getElementUiMinWidth() {
        return elementUiMinWidth;
    }

    @Override
    public void setElementUiMinWidth(String elementUiMinWidth) {
        this.elementUiMinWidth = elementUiMinWidth;
    }
    
    @Override
    public String getElementUiHeight() {
        return elementUiHeight;
    }

    @Override
    public void setElementUiHeight(String elementUiHeigh) {
        this.elementUiHeight = elementUiHeigh;
    }    

    public String getElementUiValue() {
        return elementUiValue;
    }

    public void setElementUiValue(String elementUiValue) {
        this.elementUiValue = elementUiValue;
    }
    
    public String getElementUiEnabled() {
        return elementUiEnabled;
    }

    public void setElementUiEnabled(String elementUiEnabled) {
        this.elementUiEnabled = elementUiEnabled;
    }

    public String getElementUiOnLoad() {
        return elementUiOnLoad;
    }

    public void setElementUiOnLoad(String elementUiOnLoad) {
        this.elementUiOnLoad = elementUiOnLoad;
    }

    public String getElementUiDataType() {
        return elementUiDataType;
    }

    public void setElementUiDataType(String elementUiDataType) {
        this.elementUiDataType = elementUiDataType;
    }

    public String getElementUiHidden() {
        return elementUiHidden;
    }

    public void setElementUiHidden(String elementUiHidden) {
        this.elementUiHidden = elementUiHidden;
    }

    public String getElementUiOnPropertyChange() {
        return elementUiOnPropertyChange;
    }

    public void setElementUiOnPropertyChange(String elementUiOnPropertyChange) {
        this.elementUiOnPropertyChange = elementUiOnPropertyChange;
    }

    public String getElementUiMandatory() {
        return elementUiMandatory;
    }

    public void setElementUiMandatory(String elementUiMandatory) {
        this.elementUiMandatory = elementUiMandatory;
    }
    
    @Override
    public void addEventListener(Command command) {
        getElement().addEventListener("click", event -> {
            command.execute(); 
        });
    }
}