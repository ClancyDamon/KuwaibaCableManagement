/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.modules.core.userman;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.collections.api.map.ImmutableMap;

/**
 *
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */

public class PrivilegeList {
    public static final String PRIVILEGE_NAVIGATION_TREE = "navigation-tree";
    public static final String PRIVILEGE_OBJECT_PROPERTIES = "object-properties";
    public static final String PRIVILEGE_SPECIAL_EXPLORERS = "special-explorers";
    public static final String PRIVILEGE_ATTACHMENTS = "attachments";
    public static final String PRIVILEGE_PHYSICAL_VIEW = "physical-view";
    public static final String PRIVILEGE_TOPOLOGY_DESIGNER = "topology-designer";
    public static final String PRIVILEGE_DATA_MODEL_MANAGER = "data-model-manager";
    public static final String PRIVILEGE_LIST_TYPE_MANAGER = "list-type-manager";
    public static final String PRIVILEGE_CONTAINMENT_MANAGER = "containment-manager";
    public static final String PRIVILEGE_FAVORITES = "favorites";
    public static final String PRIVILEGE_SDH_MODULE = "sdh-module";
    public static final String PRIVILEGE_MPLS_MODULE = "mpls-module";
    public static final String PRIVILEGE_PROJECTS = "projects";
    public static final String PRIVILEGE_CONTRACT_MANAGER = "contract-manager";
    public static final String PRIVILEGE_IP_ADDRESS_MANAGER = "ip-address-manager";
    public static final String PRIVILEGE_SERVICE_MANAGER = "service-manager";
    public static final String PRIVILEGE_USER_MANAGER = "user-manager";
    public static final String PRIVILEGE_SOFTWARE_ASSETS_MANAGER = "software-assets-manager";
    public static final String PRIVILEGE_REPORTS = "reports";
    public static final String PRIVILEGE_TEMPLATES = "templates";
    public static final String PRIVILEGE_POOLS = "pools";
    public static final String PRIVILEGE_BULK_IMPORT = "bulk-import";
    public static final String PRIVILEGE_AUDIT_TRAIL = "audit-trail";
    public static final String PRIVILEGE_QUERY_MANAGER = "query-manager";
    public static final String PRIVILEGE_TASK_MANAGER = "task-manager";
    public static final String PRIVILEGE_SYNC = "sync";
    public static final String PRIVILEGE_CONTACTS = "contacts";
    public static final String PRIVILEGE_CONFIG_VARIABLES = "config-variables";
    public static final String PRIVILEGE_VALIDATORS = "validators";
    public static final String PRIVILEGE_WAREHOUSES = "warehouses";
    public static final String PRIVILEGE_PROXIES = "proxies";
    
    /**
     * For now, these privileges will be hard-coded, however in the near future, every module will provide its own set of tokens
     */
    public static Map<String, String> privileges = new HashMap<String, String>() {{
             put(PRIVILEGE_NAVIGATION_TREE, "Navigation Tree");
            put(PRIVILEGE_OBJECT_PROPERTIES, "Object Properties");
            put(PRIVILEGE_SPECIAL_EXPLORERS, "Special Explorers");
            put(PRIVILEGE_PHYSICAL_VIEW, "Physical View");
            put(PRIVILEGE_TOPOLOGY_DESIGNER, "Topology Designer");
            put(PRIVILEGE_DATA_MODEL_MANAGER, "Data Model Manager");
            put(PRIVILEGE_LIST_TYPE_MANAGER, "List Type Manager");
            put(PRIVILEGE_CONTAINMENT_MANAGER, "Containment Manager");
            put(PRIVILEGE_FAVORITES, "Favorites");
            put(PRIVILEGE_PROJECTS, "Projects");
            put(PRIVILEGE_SDH_MODULE, "SDH Module");
            put(PRIVILEGE_MPLS_MODULE, "MPLS Module");
            put(PRIVILEGE_CONTRACT_MANAGER, "Contract Manager");
            put(PRIVILEGE_IP_ADDRESS_MANAGER, "IP Address Manager");
            put(PRIVILEGE_SERVICE_MANAGER, "Service Manager");
            put(PRIVILEGE_USER_MANAGER, "User Manager");
            put(PRIVILEGE_SOFTWARE_ASSETS_MANAGER, "Software Assets Manager");
            put(PRIVILEGE_REPORTS, "Reports");
            put(PRIVILEGE_TEMPLATES, "Templates");
            put(PRIVILEGE_POOLS, "Pools");
            put(PRIVILEGE_BULK_IMPORT, "Bulk Import");
            put(PRIVILEGE_AUDIT_TRAIL, "Audit Trail");
            put(PRIVILEGE_QUERY_MANAGER, "Query Manager");
            put(PRIVILEGE_TASK_MANAGER, "Task Manager");
            put(PRIVILEGE_FAVORITES, "Favorites");
            put(PRIVILEGE_SYNC, "Inventory Synchronization");
    }};

  
   
}

