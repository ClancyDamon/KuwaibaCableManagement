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
package org.neotropic.util.visual.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import java.util.Objects;

/**
 * Grid Layout Component use the CSS Grid Layout to align elements into columns
 * and rows.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("grid-layout")
public class GridLayout extends Component 
    implements HasOrderedComponents, HasStyle, HasSize {
    
    public enum Alignment {
        START("start"), //NOI18N
        END("end"), //NOI18N
        CENTER("center"), //NOI18N
        STRECH("stretch"); //NOI18N
        
        private final String value;
        
        private Alignment(String value) {
            this.value = value;
        }
        
        @Override        
        public String toString() {
            return value;
        }
    }
    
    public GridLayout() {
        getStyle().set("display", "grid"); //NOI18N
        getStyle().set("grid-gap", "5px"); //NOI18N
    }
    /**
     * Sets the grid template columns.
     * @param columns Number of columns in the grid.
     */
    public void setGridTemplateColumns(int columns) {
        if (columns > 0) {
            String value = "";
            for (int i = 0; i < columns; i++)
                value += " auto"; //NOI18N
            getStyle().set("grid-template-columns", value); //NOI18N
        }
    }
    
    public void setJustifySelf(Alignment alignment, Component component) {
        Objects.requireNonNull(component);
        if (indexOf(component) != -1)
            component.getElement().getStyle().set("justify-self", alignment.toString()); //NOI18N
    }
    
    public void setAlignSelf(Alignment alignment, Component component) {
        Objects.requireNonNull(component);
        if (indexOf(component) != -1)
            component.getElement().getStyle().set("align-self", alignment.toString()); //NOI18N
    }
    /**
     * Adds a grid item.
     * @param component Grid item.
     * @param columnStart Specifies where to start the component.
     * @param rowStart Specifies where to start the component.
     */
    public void add(Component component, int columnStart, int rowStart) {
        Objects.requireNonNull(component);
        if (columnStart >= 1 && rowStart >= 1) {
            component.getElement().getStyle().set("grid-column-start", String.valueOf(columnStart)); //NOI18N
            component.getElement().getStyle().set("grid-row-start", String.valueOf(rowStart)); //NOI18N
            add(component);
        }
    }
    /**
     * Adds a grid item.
     * @param component Grid item.
     * @param columnStart Specifies where to start the component.
     * @param rowStart Specifies where to start the component.
     * @param columnEnd Specifies where to end the component.
     * @param rowEnd Specifies where to end the component.
     */
    public void add(Component component, int columnStart, int rowStart, int columnEnd, int rowEnd) {
        add(component, columnStart, rowStart);
        component.getElement().getStyle().set("grid-column-end", String.valueOf(columnEnd)); //NOI18N
        component.getElement().getStyle().set("grid-row-end", String.valueOf(rowEnd)); //NOI18N
    }
}
