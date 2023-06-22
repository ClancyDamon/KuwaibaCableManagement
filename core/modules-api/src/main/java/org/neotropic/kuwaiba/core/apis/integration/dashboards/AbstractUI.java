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

package org.neotropic.kuwaiba.core.apis.integration.dashboards;

/**
 * All UIs in Kuwaiba must implement this interface in order to be displayed. This interface 
 * forces the implementors to init the content in the method {@link #initContent() } instead of 
 * in the <code>onAttach</code> method. This is done because the privileges are checked in the {@link  RouterLayout } associated 
 * to the UI, but the UI's <code>onAttach</code> method is called before the RouterLayout <code>onAttach</code>. In this way, 
 * the content is only added once the session has been properly validated.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public interface AbstractUI {
    /**
     * The code to initialize the content should be place here, <b>not</b> in the <code>onAttach</code> method, or worse, 
     * in the class constructor.
     */
    public void initContent();
}
