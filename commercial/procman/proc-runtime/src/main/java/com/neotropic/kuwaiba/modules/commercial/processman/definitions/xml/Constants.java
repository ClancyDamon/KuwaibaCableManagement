/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.processman.definitions.xml;

/**
 * Contains some values that are used in a form definition, like tags, 
 * attributes, functions, styles
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class Constants {
    
    public class Tag {
        public static final String ROOT = "root"; //NOI18N
        public static final String FORM = "form"; //NOI18N
        public static final String GRID_LAYOUT = "gridLayout"; //NOI18N
        public static final String I18N = "i18n"; //NOI18N
        public static final String MESSAGES = "messages"; //NOI18N
        public static final String MESSAGE = "message"; //NOI18N
        public static final String LABEL = "label"; //NOI18N
        public static final String TEXT_FIELD = "textField"; //NOI18N
        public static final String VERTICAL_LAYOUT = "verticalLayout"; //NOI18N
        public static final String TEXT_AREA = "textArea"; //NOI18N
        public static final String DATE_FIELD = "dateField"; //NOI18N
        public static final String COMBO_BOX = "comboBox"; //NOI18N
        public static final String GRID = "grid"; //NOI18N
        public static final String COLUMN = "column"; //NOI18N
        public static final String BUTTON = "button"; //NOI18N
        public static final String SUBFORM = "subform"; //NOI18N
        public static final String HORIZONTAL_LAYOUT = "horizontalLayout"; //NOI18N
        public static final String IMAGE = "image"; //NOI18N
        public static final String SCRIPT = "script"; //NOI18N
        public static final String FUNCTION = "function"; //NOI18N
        public static final String PANEL = "panel"; //NOI18N
        public static final String TREE = "tree"; //NOI18N
        public static final String LIST_SELECT_FILTER = "listSelectFilter"; //NOI18N
        public static final String FORM_INSTANCE = "formInstance"; //NOI18N
        public static final String UPLOAD = "upload"; //NOI18N
        public static final String ROWS = "rows"; //NOI18N
        public static final String ROW = "row"; //NOI18N
        public static final String DATA = "data"; //NOI18N
        public static final String MINI_APPLICATION = "miniApplication"; //NOI18N
        public static final String CHECK_BOX = "checkBox"; //NOI18N
    }
    
    public class Attribute {
        public static final String TITLE = "title"; //NOI18N
        public static final String KEY = "key"; //NOI18N
        public static final String VALUE = "value"; //NOI18N
        public static final String LANG = "lang"; //NOI18N
        public static final String ROWS = "rows"; //NOI18N
        public static final String COLUMNS = "columns"; //NOI18N   
        public static final String CAPTION = "caption"; //NOI18N
        public static final String ID = "id"; //NOI18N
        public static final String STYLE_NAME = "styleName"; //NOI18N
        public static final String AREA="area"; //NOI18N
        public static final String ENABLED="enabled"; //NOI18N
        public static final String PRECONDITIONS="preconditions"; //NOI18N
        public static final String NAME = "name"; //NOI18N
        public static final String PROPERTY_CHANGE_LISTENER = "propertychangelistener"; //NOI18N
        public static final String PARAMETER_NAMES = "parameternames"; //NOI18N
        public static final String TYPE = "type"; //NOI18N
        public static final String QUERY_NAME = "queryname"; //NOI18N
        public static final String MESSAGE = "message"; //NOI18N
        public static final String MANDATORY = "mandatory"; //NOI18N
        public static final String DATA_TYPE = "datatype"; //NOI18N
        public static final String VERSION = "version"; //NOI18N
        public static final String OBJECT_ID="objectId"; //NOI18N
        public static final String OBJECT_NAME = "objectname"; //NOI18N
        public static final String CLASS_ID = "classid"; //NOI18N
        public static final String CLASS_NAME = "classname"; //NOI18N
        public static final String FORM_ID = "formid"; //NOI18N
        public static final String SHARED = "shared"; //NOI18N
        public static final String SRC = "src"; //NOI18N
        public static final String MODE = "mode"; //NOI18N
        public static final String PACKAGE = "package"; //NOI18N
        public static final String DESCRIPTION = "description"; //NOI18N
        public static final String PATH = "path"; //NOI18N       
        public static final String SELECTION_MODE = "selectionMode"; //NOI18N
        public static final String USE_PAGINATION = "usePagination"; //NOI18N
        public static final String SORT = "sort"; //NOI18N
                
        public class DataType {
            public static final String REMOTE_OBJECT = "RemoteObject"; //NOI18N
            public static final String REMOTE_OBJECT_LIGTH = "RemoteObjectLight"; //NOI18N
            public static final String CLASS_INFO = "ClassInfo"; //NOI18N
            public static final String CLASS_INFO_LIGTH = "ClassInfoLight"; //NOI18N
            public static final String INTEGER = "Integer"; //NOI18N
            public static final String STRING = "String"; //NOI18N
            public static final String EMAIL = "Email"; //NOI18N, Data type to use default validators
            public static final String DATE = "Date"; //NOI18N
            public static final String ATTACHMENT = "Attachment"; //NOI18N
            public static final String BOOLEAN = "Boolean"; //NOI18N
        }
        
        public class Mode {
            public static final String DETACHED = "detached"; //NOI18N
            public static final String EMBEDDED = "embedded"; //NOI18N
        }
        
        public class StyleName {
            public static final String BOLD = "bold"; //NOI18N
            public static final String BUTTON_PLUS_ICON_ONLY = "buttonPlusCircleIconOnly"; //NOI18N
            public static final String BUTTON_CLOSE_ICON = "buttonCloseIcon"; //NOI18N
            public static final String BUTTON_PENCIL_ICON = "buttonPencilIcon"; //NOI18N
            public static final String BUTTON_PLUS_ICON = "buttonPlusIcon"; //NOI18N
            public static final String BUTTON_COGS_ICON = "buttonCogsIcon"; //NOI18N
            public static final String BUTTON_PRIMARY = "buttonPrimary"; //NOI18N
            public static final String BUTTON_DANGER = "buttonDanger"; //NOI18N
        }
    }
    
    public class EventAttribute {
        public static final String ONCLICK = "onclick"; //NOI18N
        public static final String ONNOTIFY = "onnotify"; //NOI18N
        public static final String ONPROPERTYCHANGE = "onpropertychange"; //NOI18N
        public static final String ONLOAD = "onload"; //NOI18N
        public static final String ONLAZYLOAD = "onlazyload"; //NOI18N
        public static final String ONUPLOADSUCCEEDED = "onuploadsucceeded"; //NOI18N
    }
    
    public class Function {
        public static final String OPEN = "open"; //NOI18N
        public static final String CLOSE = "close"; //NOI18N
        public static final String CLEAN = "clean"; //NOI18N
        public static final String VALIDATE = "validate"; //NOI18N
        public static final String I18N = "i18n"; //NOI18N
        public static final String ADD_GRID_ROW = "addgridrow"; //NOI18N
        public static final String ADD_GRID_ROW_FROM_SCRIPT = "addgridrowfromscript"; //NOI18N        
        public static final String ADD_GRID_ROWS = "addgridrows"; //NOI18N
        public static final String EDIT_GRID_ROW = "editgridrow"; //NOI18N
        public static final String DELETE_GRID_ROW = "deletegridrow"; //NOI18N
        public static final String SAVE="save"; //NOI18N
        public static final String PROPERTY_CHANGE = "propertyChange"; //NOI18N
        
        public class Type {
            public static final String FUNCTION = "function"; //NOI18N
            public static final String QUERY = "query"; //NOI18N
            public static final String VALIDATOR = "validator"; //NOI18N
        }
    }
    
    public class Property {
        public static final String VALUE = "value"; //NOI18N
        public static final String ENABLED = "enabled"; //NOI18N
        public static final String DUMMY_PROPERTY = "dummyproperty"; //NOI18N
        public static final String ITEMS = "items"; //NOI18N
        public static final String ROWS = "rows"; //NOI18N
        public static final String HEIGHT = "height"; //NOI18N
        public static final String WIDTH = "width"; //NOI18N
        public static final String HIDDEN = "hidden"; //NOI18N
        public static final String REPAINT = "repaint"; //NOI18N
        public static final String SELECTED_ROW = "selectedRow"; //NOI18N
        public static final String CAPTION = "caption"; //NOI18N
        public static final String INPUT_PARAMETERS = "inputParameters"; //NOI18N
        public static final String OUTPUT_PARAMETERS = "outputParameters"; //NOI18N
        public static final String SAVE = "save"; //NOI18N
        public static final String ALIGNMENT = "alignment"; //NOI18N
        public static final String SELECTED_ROWS = "selectedRows"; //NOI18N
                        
        public class Alignment {
            public static final String TOP_RIGHT = "topRight"; //NOI18N
            public static final String TOP_LEFT = "topLeft"; //NOI18N
            public static final String TOP_CENTER = "topCenter"; //NOI18N
            public static final String MIDDLE_RIGHT = "middleRight"; //NOI18N
            public static final String MIDDLE_LEFT = "middleLeft"; //NOI18N
            public static final String MIDDLE_CENTER = "middleCenter"; //NOI18N
            public static final String BOTTOM_RIGHT = "bottomRight"; //NOI18N
            public static final String BOTTOM_LEFT = "bottomLeft"; //NOI18N
            public static final String BOTTOM_CENTER = "bottomCenter"; //NOI18N
        }
    }
}
