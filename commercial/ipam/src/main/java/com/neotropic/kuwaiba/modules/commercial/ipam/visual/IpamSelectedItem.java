/*
 * Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ipam.visual;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Represents a selected item in the ipam
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class IpamSelectedItem extends VerticalLayout{
    private IpamExplorerRow lytHeader;
    private VerticalLayout lytDetails;

    public IpamSelectedItem() {
    }

    public IpamExplorerRow getLytHeader() {
        return lytHeader;
    }

    public void setLytHeader(IpamExplorerRow lytHeader) {
        this.lytHeader = lytHeader;
    }

    public VerticalLayout getLytDetails() {
        return lytDetails;
    }

    public void setLytDetails(VerticalLayout lytDetails) {
        this.lytDetails = lytDetails;
    }
}
