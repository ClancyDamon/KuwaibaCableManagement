/*
 *  Copyright 2010-2022, Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.optional.reports.html;

import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.InventoryReport;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Useful methods to build HTML reports.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class HTMLReport extends InventoryReport {
    /**
     * Text of the embedded style sheet.
     */
    private String embeddedStyleSheet;
    /**
     * List of the URL of the external CSS linked from the report document. Note that the location has to be reachable from whenever the report will be rendered.
     */
    private List<String> linkedStyleSheets;
    /**
     * favicon URL.
     */
    private String favicon;
    /**
     * Text of the embedded Javascript section.
     */
    
    private List<String> embeddedJavascript;
    /**
     * List of the URL of the external js linked from the report document. Note that the location has to be reachable from whenever the report will be rendered.
     */
    private List<String> linkedJavascriptFiles;
    /**
     * Report components. They will be displayed one after another, so make sure you arrange them properly
     */
    private List<HTMLComponent> components;
  
    public HTMLReport(String title, String author, String version) {
        super(title, author, version);
        this.linkedJavascriptFiles = new ArrayList<>();
        this.embeddedJavascript = new ArrayList<>();
        this.components = new ArrayList<>();
        this.favicon = "";
    }

    public String asHTML() {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\">"); //NOI18N
        
        if (embeddedStyleSheet != null) {
            builder.append("<style type=\"text/css\">"); //NOI18N
            builder.append(embeddedStyleSheet);
            builder.append("</style>"); //NOI18N
        }
        
        if (linkedStyleSheets != null) {
            for (String linkedStyleSheet : linkedStyleSheets) {
                builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");  //NOI18N
                builder.append(linkedStyleSheet);
                builder.append("\">"); //NOI18N
            }
        }
        
        if (linkedJavascriptFiles != null) {
            for (String linkedJavascriptFile : linkedJavascriptFiles) {
                builder.append("<script src=\"");  //NOI18N
                builder.append(linkedJavascriptFile);
                builder.append("\"></script>"); //NOI18N
            }
        }
            
        if (embeddedJavascript != null) {
            builder.append("<script type=\"text/javascript\">"); //NOI18N
            
            for (String embedded : embeddedJavascript)
                builder.append(embedded);
            
            builder.append("</script>"); //NOI18N
        }
        
        builder.append("<title>"); //NOI18N
        builder.append(title);
        builder.append(" - Kuwaiba Open Network Inventory</title>"); //NOI18N
        
        if(!favicon.isEmpty()){
            builder.append("<link rel=\"shortcut icon\" href=\""); //NOI18N
            builder.append(favicon);
            builder.append("\"/>");
        }
            
        builder.append("</head><body>"); //NOI18N
        
        for (HTMLComponent component : components)
            builder.append(component.asHTML());
        
        builder.append("</body></html>"); //NOI18N
        return builder.toString();
    }
    
    @Override
    public byte[] asByteArray() {
        return asHTML().getBytes(StandardCharsets.UTF_8);
    }

    public String getEmbeddedStyleSheet() {
        return embeddedStyleSheet;
    }

    public void setEmbeddedStyleSheet(String embeddedStyleSheet) {
        this.embeddedStyleSheet = embeddedStyleSheet;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public List<String> getLinkedStyleSheets() {
        if (linkedStyleSheets == null)
            linkedStyleSheets = new ArrayList();
        return linkedStyleSheets;
    }

    public void setLinkedStyleSheets(List<String> linkedStyleSheets) {
        this.linkedStyleSheets = linkedStyleSheets;
    }
    
    public List<String> getEmbeddedJavascript() {
        return embeddedJavascript;
    }

    public void setEmbeddedJavascript(List<String> embeddedJavascript) {
        this.embeddedJavascript = embeddedJavascript;
    }
    
    public List<String> getLinkedJavascriptFiles() {
        return linkedJavascriptFiles;
    }

    public void setLinkedJavascriptFiles(List<String> linkedJavascriptFiles) {
        this.linkedJavascriptFiles = linkedJavascriptFiles;
    }

    public List<HTMLComponent> getComponents() {
        return components;
    }

    public void setComponents(List<HTMLComponent> components) {
        this.components = components;
    }
    
    public static String getDefaultStyleSheet() {
        return      "   @import url('https://fonts.googleapis.com/css?family=Open+Sans');\n"+    
                    "   body {\n" +
                    "            font-family: 'Open Sans', sans-serif;\n" +
                    "            font-size: small;\n" +
                    "            padding: 5px 10px 5px 10px;\n" +
                    "            background-color: #FFF;\n" +
                    "            color: #003C42;\n"+
                    "   }\n" +
                    "   table {\n" +
                    "            border: hidden;\n" +
                    "            width: 100%;\n" +
                    "          }\n" +
                    "   th {\n" +
                    "            background-color: #88AA00;\n" +
                    "            padding: 7px 7px 7px 7px;\n" +
                    "            color: white;\n" +
                    "            font-weight: normal;\n" +
                    "   }\n" +
                    "   td {\n" +
                    "            padding: 7px 7px 7px 7px;\n" +
                    "   }\n" +
                    "   div {\n" +
                    "            padding: 5px 5px 5px 5px;\n" +
                    "            float: left;\n             " +
                    "   }\n" +
                    "   div.warning {\n" +
                    "            background-color: #FFF3A2;\n" +
                    "            text-align: center;\n" +
                    "   }\n" +
                    "   div.error {\n" +
                    "            background-color: #FFD9C7;\n" +
                    "            text-align: center;\n" +
                    "   }\n" +
                    "   div.footer {\n" +
                    "            width: 100%;\n" +
                    "            text-align: center;\n" +
                    "            font-style: italic;\n" +
                    "            font-size: x-small;\n" +
                    "            color: #848484;\n" +
                    "   }\n" +
                    "   span.ok {\n" +
                    "            color: green;\n" +
                    "   }\n" +
                    "   span.warning {\n" +
                    "            color: orange;\n" +
                    "   }\n" +
                    "   span.error {\n" +
                    "            color: red;\n" +
                    "   }\n" +
                    "   td.generalInfoLabel {\n" +
                    "            background-color: #FF9167;\n" +
                    "            width: 20%;\n" +
                    "   }\n" +
                    "   td.generalInfoValue {\n" +
                    "            background-color: white;\n" +
                    "   }\n" +
                    "   tr.even {\n" +
                    "            background-color: #F5FFF8;\n" +
                    "   }\n" +
                    "   tr.odd {\n" +
                    "            background-color: #FFF;\n" +
                    "   }" +
                    "   hr { \n" +
                    "            display: block; \n"+
                    "            margin-top: 0.5em; \n"+
                    "            margin-bottom: 0.5em; \n"+
                    "            margin-left: auto; \n"+
                    "            margin-right: auto; \n"+
                    "            border-style: inset; \n"+
                    "            border-width: 1px; \n"+
                    "            color: #A5DF00; \n"+
                    "       }  \n";
    }
    
}
