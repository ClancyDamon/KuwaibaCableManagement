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

import com.neotropic.kuwaiba.modules.commercial.processman.forms.elements.FunctionRunnerException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.renderer.TextRenderer;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ArtifactDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessInstance;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ConditionalArtifactRender extends AbstractArtifactRender {
    private final String processEnginePath;
    private final ProcessInstance processInstance;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    private final Consumer<FunctionRunnerException> consumerFuncRunnerEx;
    private final HashMap<String, Object> funcRunnerParams;
    
    private final ActivityDefinition activityDefintion;
    private final ArtifactDefinition artifactDefinition;
    private final Artifact artifact;
    private final ArtifactDefinition infoArtifactDefinition;
    private RadioButtonGroup<Boolean> btn;
    
    public ConditionalArtifactRender(ActivityDefinition activityDefinition, ArtifactDefinition artifactDefinition, Artifact artifact, 
        ArtifactDefinition informationArtifactDefinition, 
        String processEnginePath, ProcessInstance processInstance, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts,
        Consumer<FunctionRunnerException> consumerFuncRunnerEx, HashMap<String, Object> funcRunnerParams) {
        Objects.requireNonNull(processEnginePath);
        Objects.requireNonNull(processInstance);
        Objects.requireNonNull(bem);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        this.activityDefintion = activityDefinition;
        this.artifactDefinition = artifactDefinition;
        this.artifact = artifact;
        this.infoArtifactDefinition = informationArtifactDefinition;
        this.processEnginePath = processEnginePath;
        this.processInstance = processInstance;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.consumerFuncRunnerEx = consumerFuncRunnerEx;
        this.funcRunnerParams = funcRunnerParams;
    }

    @Override
    public Component render() {
        //TODO: preconditions
        btn = new RadioButtonGroup();
        btn.setItems(true, false);
        btn.setRenderer(new TextRenderer<>(item -> 
            item ? ts.getTranslatedString("module.general.messages.yes") : ts.getTranslatedString("module.general.messages.no")
        ));
        btn.setLabel(artifactDefinition != null ? new String(artifactDefinition.getDefinition()) : ts.getTranslatedString("module.propertysheet.labels.null-value-property"));
        if (artifact != null) {
            if (!activityDefintion.getId().equals(processInstance.getCurrentActivityId())) {
                btn.setEnabled(false);
                btn.setValue(getValue());
            }
        }
        VerticalLayout lytConditional = new VerticalLayout(btn);
        lytConditional.setSizeFull();
        lytConditional.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, btn);
        if (infoArtifactDefinition != null) {
            AbstractArtifactRender artifactRenderer = null;
            switch (infoArtifactDefinition.getType()) {
                case ArtifactDefinition.TYPE_FORM:
                    artifactRenderer = new FormArtifactRender(processEnginePath, processInstance, infoArtifactDefinition, null, aem, bem, mem, ts, consumerFuncRunnerEx, funcRunnerParams);
                break;
            }
            if (artifactRenderer != null) {
                SplitLayout lyt = new SplitLayout();
                lyt.setSizeFull();
                lyt.setOrientation(SplitLayout.Orientation.VERTICAL);
                lyt.addToPrimary(lytConditional);
                lyt.addToSecondary(artifactRenderer.render());
                lyt.setSplitterPosition(20);
                return lyt;
            }
        }
        return lytConditional;
    }
    
    @Override
    public byte[] getContent() {
        String strContent = "<artifact type=\"conditional\"><value>" + (btn.getValue() != null ? btn.getValue() : false) + "</value></artifact>";
        return strContent.getBytes();
    }
    
    private boolean getValue() {
        if (artifact != null) {
            try {
                byte[] content = artifact.getContent();
                
                XMLInputFactory xif = XMLInputFactory.newInstance();
                ByteArrayInputStream bais = new ByteArrayInputStream(content);
                XMLStreamReader reader = xif.createXMLStreamReader(bais);
                
                QName tagValue = new QName("value"); //NOI18N

                while (reader.hasNext()) {

                    int event = reader.next();

                    if (event == XMLStreamConstants.START_ELEMENT) {

                        if (reader.getName().equals(tagValue))
                            return Boolean.valueOf(reader.getElementText());
                    }
                }
            } catch (XMLStreamException ex) {
                return false;
            }
        }
        return false;
    }
}
