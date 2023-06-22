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
package org.neotropic.util.visual.selectors;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.general.FormattedObjectDisplayNameSpan;
import org.parboiled.common.StringUtils;

/**
 * An object as a component shows the name and class of the object parent and the object.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ObjectComponent extends FlexLayout {
    
    public ObjectComponent(BusinessObjectLight object, MetadataEntityManager mem, TranslationService ts) {
        FormattedObjectDisplayNameSpan spanName = new FormattedObjectDisplayNameSpan(object, false, false, false, false);
        Label lblClass = new Label(
            !StringUtils.isEmpty(object.getClassDisplayName()) ? object.getClassDisplayName() : object.getClassName()
        );
        lblClass.setClassName("text-secondary"); //NOI18N
        add(spanName, lblClass);
        setSizeFull();
        setFlexDirection(FlexLayout.FlexDirection.COLUMN);
    }
}
