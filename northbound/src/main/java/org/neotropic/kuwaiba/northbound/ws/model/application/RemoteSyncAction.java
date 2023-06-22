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

package org.neotropic.kuwaiba.northbound.ws.model.application;


/**
 * An instance of this class define an action to be performed upon a sync finding
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class RemoteSyncAction {
    /*
     * The finding should be executed
     */
    private final static int ACTION_EXECUTE = 1;
    /**
     * The finding should be skipped
     */
    private final static int ACTION_SKIP = 0;
    
    private RemoteSyncFinding finding;
    
    private int type;

    public RemoteSyncAction() { }
    
    public RemoteSyncAction(RemoteSyncFinding finding, int type) {
        this.finding = finding;
        this.type = type;
    }
   
    public RemoteSyncFinding getFinding() {
        return finding;
    }

    public void setFinding(RemoteSyncFinding finding) {
        this.finding = finding;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
