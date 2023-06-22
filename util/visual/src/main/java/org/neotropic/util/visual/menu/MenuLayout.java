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
package org.neotropic.util.visual.menu;

import com.neotropic.flow.component.paper.dialog.PaperDialog;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import java.util.Objects;

/**
 * Layout to contain a set of {@link MenuButton}. This layout is useful to create top-level menus.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MenuLayout extends FlexLayout {
    private PaperDialog menuDialog;

    public void add(MenuButton menuButton) {
        Objects.requireNonNull(menuButton);
        super.add(menuButton);
        setClassName("sidebar_menu");
    }
    
    public PaperDialog getMenuDialog() {
        return menuDialog;
    }
    
    public void removeMenuDialog() {
        if (this.menuDialog != null) {
            this.menuDialog.close();
            remove(this.menuDialog);
            this.menuDialog = null;
        }
    }
    
    public void addMenuDialog(PaperDialog menuDialog) {
        removeMenuDialog();
        add(menuDialog);
        this.menuDialog = menuDialog;
    }
}
