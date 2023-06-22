/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.modules.core.queries.filters;

import com.vaadin.flow.component.Component;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Abstract class for create query filters
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public abstract class AbstractFilter {
    
    protected TranslationService ts;
    protected Criteria criteria;
    protected String value;

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
  
    public enum Criteria {
        EQUAL("Equals to",0),
        LESS_THAN("Less than",1),
        EQUAL_OR_LESS_THAN("Equals or less than",2),
        GREATER_THAN("Greater than",3),
        EQUAL_OR_GREATER_THAN("Equals or greater than",4),
        BETWEEN("Between",5),
        CONTAINS("Contains",6),
        EXACT_MATCH("Exact Match",0);
        private final String label;
        private final int id;

        Criteria(String label, int id){
            this.label = label;
            this.id = id;
        }

        public String label(){return label;}
        public int id(){return id;}

        public static Criteria fromId(int i){
            switch (i){
                default:
                case 0:
                    return EQUAL;
                case 1:
                    return LESS_THAN;
                case 2:
                    return EQUAL_OR_LESS_THAN;
                case 3:
                    return GREATER_THAN;
                case 4:
                    return EQUAL_OR_GREATER_THAN;
                case 5:
                    return BETWEEN;
                case 6:
                    return CONTAINS;
            }
        }
    }

    public AbstractFilter(TranslationService ts) {
        this.ts = ts;
    }
       
    public abstract Component getComponent();
    public abstract String getValueAsString();
    public abstract boolean isValid();
}
