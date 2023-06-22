/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>
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
package com.neotropic.kuwaiba.modules.commercial.ipam.visual;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
/**
 * A custom span to draw the parents bread crumbs and keeps a record of 
 * the object parent to allow navigation when it is selected
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class CustomSpan extends Span{
    /**
     * it could be a folder or a subnet
     */
    private Object obj;

    public CustomSpan(Object obj, Component... components) {
        super(components);
        this.obj = obj;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
