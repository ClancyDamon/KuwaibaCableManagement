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
package org.neotropic.util.visual.properties;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.selectors.BusinessObjectSelector;

/**
 * Property to select a business object.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class BusinessObjectProperty extends AbstractProperty<BusinessObjectLight> {
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;

    public BusinessObjectProperty(String name, String displayName, String description, BusinessObjectLight value, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        super(name, displayName, description, value, ts);
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
    }
    
    @Override
    public BusinessObjectLight getDefaultValue() {
        return null;
    }

    @Override
    public AbstractField getAdvancedEditor() {
        return new BusinessObjectField(new BusinessObjectSelector(getDisplayName(), aem, bem, mem, ts));
    }

    @Override
    public boolean supportsAdvancedEditor() {
        return true;
    }

    @Override
    public boolean supportsInplaceEditor() {
        return false;
    }

    @Override
    public AbstractField getInplaceEditor() {
        return null;
    }

    @Override
    public String getAsString() {
        return getValue() != null ? getValue().toString() : AbstractProperty.NULL_LABEL;
    }
    /**
     * Component to wrap the business object selector component.
     */
    @Tag("business-object-field")
    private class BusinessObjectField extends AbstractField implements HasComponents, HasSize {
        
        public BusinessObjectField(BusinessObjectSelector selector) {
            super(null);
            Objects.requireNonNull(selector);
            setSizeFull();
            selector.setSizeFull();
            selector.addSelectedObjectChangeListener(event -> setValue(event.getSelectedObject()));
            add(selector);
        }
        
        @Override
        protected void setPresentationValue(Object t) {
        }
    }
}
