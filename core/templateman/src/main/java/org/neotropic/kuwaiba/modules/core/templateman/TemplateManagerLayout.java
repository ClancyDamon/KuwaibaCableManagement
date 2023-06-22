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
package org.neotropic.kuwaiba.modules.core.templateman;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import org.neotropic.util.visual.general.ModuleLayout;

/**
 * Template layout, display main page in four resizable vertical segments. 
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@StyleSheet(value = "css/tepman.css")
@CssImport("./styles/configuration-manager.css")
@CssImport(value = "./styles/vaadin-menu-bar-buttons.css", themeFor = "vaadin-menu-bar")
@CssImport(value = "./styles/tepman-item-menubar-buttons.css", themeFor = "vaadin-menu-bar")
public class TemplateManagerLayout extends ModuleLayout { }
