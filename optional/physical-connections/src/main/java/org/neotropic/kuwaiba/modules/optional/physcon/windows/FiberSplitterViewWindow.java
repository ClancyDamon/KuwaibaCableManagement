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
package org.neotropic.kuwaiba.modules.optional.physcon.windows;

import org.neotropic.util.visual.window.ViewWindow;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.views.FiberSplitterView;

/**
 * Window to wrap a {@link FiberSplitterView}
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FiberSplitterViewWindow extends ViewWindow {
    public FiberSplitterViewWindow(BusinessObjectLight businessObject, 
        BusinessEntityManager bem, ApplicationEntityManager aem, MetadataEntityManager mem, TranslationService ts) throws InvalidArgumentException {
        
        super(String.format(ts.getTranslatedString("module.physcon.windows.title.fiber-splitter-view"), businessObject.getName()), ts);
        setContent(new FiberSplitterView(businessObject, bem, aem, mem, ts).getAsUiElement());
    }    
}
