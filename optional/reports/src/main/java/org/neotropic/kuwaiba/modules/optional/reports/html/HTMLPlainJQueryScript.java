/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neotropic.kuwaiba.modules.optional.reports.html;

/**
 *
 * @author adrian
 */
public class HTMLPlainJQueryScript extends HTMLComponent{

    private String script;
            
    public HTMLPlainJQueryScript(String script) {
        this.script = script;
    }
    
    @Override
    public String asHTML() {
        StringBuilder builder = new StringBuilder();
        builder.append(script);
        return builder.toString();
    }
    
}
