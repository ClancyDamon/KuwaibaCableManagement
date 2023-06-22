/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.util.visual.widgets;

import com.vaadin.flow.component.html.Label;

/**
 * A simple widget that displays a custom message and stretches horizontally.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class EmptyWideWidget extends AbstractDashboardWidget {

    public EmptyWideWidget(String title) {
        super(); 
        setTitle(title);
        setWidthFull();
    }

    @Override
    public void createContent() {
        this.contentComponent = new Label(title);
        this.contentComponent.setId("widgets-empty");
    }

}
