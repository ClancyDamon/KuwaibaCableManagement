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
package org.neotropic.kuwaiba.modules.optional.connectivityman.actions;

import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Builds a connectivity action.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class AbstractConnectivityActionBuilder {
    protected final ApplicationEntityManager aem;
    protected final BusinessEntityManager bem;
    protected final MetadataEntityManager mem;
    protected final TranslationService ts;
    private final String text;
    
    public AbstractConnectivityActionBuilder(String text, ApplicationEntityManager aem, 
        BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(aem);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        
        this.text = text;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
    }
    
    public String getText() {
        return text;
    }
    
    public abstract AbstractConnectivityAction getAction(Connection connection);
    /**
     * Builds a new mirror action.
     */
    public static class NewMirrorActionBuilder extends AbstractConnectivityActionBuilder {

        public NewMirrorActionBuilder(ApplicationEntityManager aem, 
            BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
            super(
                ts.getTranslatedString("module.connectivity-manager.action.new-mirror"), 
                aem, bem, mem, ts
            );
        }

        @Override
        public AbstractConnectivityAction getAction(Connection connection) {
            return new NewMirrorAction(connection, bem, mem, ts);
        }
    }
    
    public static class NewLinkActionBuilder extends AbstractConnectivityActionBuilder {

        public NewLinkActionBuilder(ApplicationEntityManager aem, 
            BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
            super(
                ts.getTranslatedString("module.connectivity-manager.action.new-link"), 
                aem, bem, mem, ts
            );
        }

        @Override
        public AbstractConnectivityAction getAction(Connection connection) {
            return new NewLinkAction(
                connection, 
                getText(), 
                bem, mem, ts
            );
        }
    }
    
    public static class NewLinkFromContainerTemplateActionBuilder extends AbstractConnectivityActionBuilder {

        public NewLinkFromContainerTemplateActionBuilder(ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
            super(
                ts.getTranslatedString("module.connectivity-manager.action.actions.6.label"), 
                aem, bem, mem, ts
            );
        }

        @Override
        public AbstractConnectivityAction getAction(Connection connection) {
            return new NewLinkFromContainerTemplateAction(connection, getText(), aem, bem, mem, ts);
        }
    }
    
    public static class SelectLinkActionBuilder extends AbstractConnectivityActionBuilder {

        public SelectLinkActionBuilder(ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
            super(
                ts.getTranslatedString("module.connectivity-manager.action.select-link"), 
                aem, bem, mem, ts
            );
        }

        @Override
        public AbstractConnectivityAction getAction(Connection connection) {
            return new SelectLinkAction(connection, getText(), aem, bem, mem, ts);
        }
    }
}
