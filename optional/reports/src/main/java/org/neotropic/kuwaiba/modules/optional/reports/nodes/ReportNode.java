/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neotropic.kuwaiba.modules.optional.reports.nodes;

import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadataLight;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/**
 * Represents a node in the reports tree.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>}
 */
public class ReportNode extends AbstractNode<ReportMetadataLight>{

    public ReportNode(ReportMetadataLight object) {
        super(object);
        //iconUrl = "img/report.png";
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
