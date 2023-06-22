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
package org.neotropic.kuwaiba.modules.optional.physcon.actions.mirrors;

import org.neotropic.util.visual.dialog.ConfirmDialog;

/**
 * Window to mirror free ports.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class AbstractWindowMirrorFreePorts extends ConfirmDialog {
    public final String SUFFIX_IN = "-in";
    public final String SUFFIX_OUT = "-out";
    public final String SUFFIX_FRONT = "-front";
    public final String SUFFIX_BACK = "-back";
    public final String PREFIX_IN = "in-";
    public final String PREFIX_OUT = "out-";
    public final String PREFIX_FRONT = "front-";
    public final String PREFIX_BACK = "back-";
    
}
