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

package org.neotropic.kuwaiba.web.ui;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import org.neotropic.kuwaiba.core.apis.integration.dashboards.AbstractUI;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The About Us page.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route(value = "about", layout = MainLayout.class)
public class AboutUI extends VerticalLayout implements HasDynamicTitle, AbstractUI {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the module registry. The global register of all active modules.
     */
    @Autowired
    private ModuleRegistry moduleRegistry;
    public AboutUI() {
        setSizeFull();
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.about.title");
    }

    @Override
    public void initContent() {
        add(new H4(ts.getTranslatedString("module.about.labels.about-title")), 
            new Html(ts.getTranslatedString("module.about.labels.about-text")),
            new H4(ts.getTranslatedString("module.about.labels.licensing-title")),
            new Html(ts.getTranslatedString("module.about.labels.licensing-text")),
            new H4(ts.getTranslatedString("module.about.labels.third-party-title")),
            new Html(ts.getTranslatedString("module.about.labels.third-party-text")),
            new H4(ts.getTranslatedString("module.about.labels.commercial-support-title")),
            new Html(ts.getTranslatedString("module.about.labels.commercial-support-text")),
            new H4(ts.getTranslatedString("module.about.labels.active-modules")));
        this.moduleRegistry.getModules().values().forEach( aModule -> add(new Html("<p><span style=\"font-weight: bold\">" + 
                aModule.getName() + "</span> " + aModule.getVersion() + " (" + aModule.getVendor() + ")" + "<br>" + 
                aModule.getDescription() + "</p>")));
    }
}
