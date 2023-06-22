/*
 * Copyright 2023-2023. Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neotropic.kuwaiba.modules.commercial.sync.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.neotropic.kuwaiba.modules.commercial.sync.model.SyncDataSourceConfiguration;

/**
 * Represents a visual component of a data source
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@neotropic.co>}
 */
public class DataSourceComponent extends HorizontalLayout {

    public DataSourceComponent(SyncDataSourceConfiguration dataSource) {
        this.setSpacing(false);
        this.setPadding(false);
        this.setMargin(false);
        Div elementBody = new Div();
        StringBuilder body = new StringBuilder();
        body.append("<b>").append(dataSource.getCommonParameters().getDataSourcetype()).append("</b></br>");
        if (dataSource.getName() != null)
            body.append(dataSource.getName());
        elementBody.getElement().setProperty("innerHTML"
                , body.toString());
        add(elementBody);
    }

}
