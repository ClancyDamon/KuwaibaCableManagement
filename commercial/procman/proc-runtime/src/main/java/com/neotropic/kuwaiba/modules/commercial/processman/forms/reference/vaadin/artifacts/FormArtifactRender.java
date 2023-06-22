/*
 *  Copyright 2010-2023 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.commercial.processman.forms.reference.vaadin.artifacts;

import com.neotropic.kuwaiba.modules.commercial.processman.artifacts.form.FormInstanceCreator;
import com.neotropic.kuwaiba.modules.commercial.processman.artifacts.form.FormInstanceLoader;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.FormDefinitionLoader;
import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.FunctionRunnerException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ArtifactDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Renders a Form Artifact Definition and loads artifact data
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FormArtifactRender extends AbstractArtifactRender {
    private final String processEnginePath;
    private final ProcessInstance processInstance;
    private final ArtifactDefinition artifactDefinition;
    private final Artifact artifact;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final Consumer<FunctionRunnerException> consumerFuncRunnerEx;
    private final HashMap<String, Object> funcRunnerParams;
    
    private FormRender formRenderer;
    private FormInstanceCreator formInstanceCreator;
    
    public FormArtifactRender(String processEnginePath, ProcessInstance processInstance, 
        ArtifactDefinition artifactDefinition, Artifact artifact, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts,
        Consumer<FunctionRunnerException> consumerFuncRunnerEx, HashMap<String, Object> funcRunnerParams) {
        Objects.requireNonNull(processEnginePath);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        this.processEnginePath = processEnginePath;
        this.processInstance = processInstance;
        this.artifactDefinition = artifactDefinition;
        this.artifact = artifact;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.consumerFuncRunnerEx = consumerFuncRunnerEx;
        this.funcRunnerParams = funcRunnerParams;
    }
    
    @Override
    public Component render() {
        //TODO: validate preconditions script
        if (artifactDefinition == null)
            return new Label(ts.getTranslatedString("module.processman.artifact-renderer.form.no-artifact-definition"));
        if (artifactDefinition.getDefinition() == null)
            return new Label(ts.getTranslatedString("module.processman.artifact-renderer.form.no-artifact-definition"));
        if (artifact != null && artifact.getContent() != null && artifact.getContent().length > 0) {
            
            FormInstanceLoader fil = new FormInstanceLoader(processEnginePath, bem, mem, ts, consumerFuncRunnerEx, funcRunnerParams);
            FormDefinitionLoader formDefinitionLoader = fil.load(artifactDefinition.getDefinition(), artifact.getContent());
            
            formRenderer = new FormRender(formDefinitionLoader, processInstance, processEnginePath, ts);
            formRenderer.render(aem, bem, mem, ts);
            
            return formRenderer;
        }
        FormDefinitionLoader formDefinitionLoader = new FormDefinitionLoader(processEnginePath, artifactDefinition.getDefinition(), consumerFuncRunnerEx, funcRunnerParams);
        formDefinitionLoader.build();
        
        formRenderer = new FormRender(formDefinitionLoader, processInstance, processEnginePath, ts);
        formRenderer.render(aem, bem, mem, ts);
        
        return formRenderer;
    }
    
    @Override
    public byte[] getContent() {
        formInstanceCreator = new FormInstanceCreator(formRenderer.getFormStructure(), mem, ts);
        return formInstanceCreator.getStructure();
    }

    @Override
    public List<StringPair> getSharedInformation() {
        
        List<StringPair> pairs = super.getSharedInformation();

        for (String pair : formInstanceCreator.getSharedInformation().keySet()) {

            String key = pair;
            String value = formInstanceCreator.getSharedInformation().get(pair);

            pairs.add(new StringPair(key, value));
        }
        return pairs;
    }
}
