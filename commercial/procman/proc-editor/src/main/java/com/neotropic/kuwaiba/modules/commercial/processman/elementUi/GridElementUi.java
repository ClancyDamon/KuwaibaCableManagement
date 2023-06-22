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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.server.Command;

/**
 *
 * @author Mauricio Ruiz Beltrán {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class GridElementUi extends Grid implements ElementUi {
    
    private String elementUiId;
    private String elementUiWidth;
    private String elementUiHeight;
    private String elementUiMinWidth;
    private String elementUiOnLoad;
    private String elementUiShared;
    private String elementUiColumns;
    private String elementUiRows;
    private String elementUiAlignment;
    private String elementUiSave;
    private String elementUiDataType;

    public GridElementUi() { }
    
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

    public String getElementUiOnLoad() {
        return elementUiOnLoad;
    }

    public void setElementUiOnLoad(String elementUiOnLoad) {
        this.elementUiOnLoad = elementUiOnLoad;
    }

    public String getElementUiShared() {
        return elementUiShared;
    }

    public void setElementUiShared(String elementUiShared) {
        this.elementUiShared = elementUiShared;
    }

    public String getElementUiColumns() {
        return elementUiColumns;
    }

    public void setElementUiColumns(String elementUiColumns) {
        this.elementUiColumns = elementUiColumns;
    }

    public String getElementUiRows() {
        return elementUiRows;
    }

    public void setElementUiRows(String elementUiRows) {
        this.elementUiRows = elementUiRows;
    }

    public String getElementUiAlignment() {
        return elementUiAlignment;
    }

    public void setElementUiAlignment(String elementUiAlignment) {
        this.elementUiAlignment = elementUiAlignment;
    }

    public String getElementUiSave() {
        return elementUiSave;
    }

    public void setElementUiSave(String elementUiSave) {
        this.elementUiSave = elementUiSave;
    }

    public String getElementUiDataType() {
        return elementUiDataType;
    }

    public void setElementUiDataType(String elementUiDataType) {
        this.elementUiDataType = elementUiDataType;
    }
    
    @Override
    public void addEventListener(Command command) {
        getElement().addEventListener("click", event -> {
            command.execute(); 
        });
    }
}