/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neotropic.kuwaiba.modules.core.queries.nodes;

import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueriesPool;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/**
 * Represents a root node in the scripts tree.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>}
 */
public class PoolNode extends AbstractNode<ScriptedQueriesPool>{

    public PoolNode(ScriptedQueriesPool object) {
        super(object);
    }
    
    @Override
    public AbstractAction[] getActions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
}

    @Override
    public void refresh(boolean recursive) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
