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
package org.neotropic.kuwaiba.modules.optional.physcon.actions.mirrors;

import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.EffectAllowed;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;

/**
 * Component to represent a port
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PortComponent extends HorizontalLayout {
    private final BusinessObjectLight port;
    private final DragSource<PortComponent> dragSource;
    private boolean selected = false;
    private final Icon icon;
    private final BusinessEntityManager bem;
    private final TranslationService ts;

    public PortComponent(BusinessObjectLight port, BusinessEntityManager bem, TranslationService ts) {
        Objects.requireNonNull(port);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(ts);
        
        dragSource = DragSource.create(this);
        dragSource.setEffectAllowed(EffectAllowed.MOVE);

        icon = VaadinIcon.CHECK_SQUARE_O.create();
        icon.setVisible(false);
        icon.setSize("16px");

        Div div = new Div();
        div.setWidth("16px");
        div.setHeight("16px");
        div.add(icon);
        
        FormattedObjectDisplayNameSpan spanObjectName = new FormattedObjectDisplayNameSpan(port, false, false, true, false);
        add(div, spanObjectName);
        setAlignItems(Alignment.CENTER);

        this.port = port;
        this.bem = bem;
        this.ts = ts;
    }

    public BusinessObjectLight getPort() {
        return port;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        icon.setVisible(selected);
    }

    public boolean isSelected() {
        return selected;
    }
}
