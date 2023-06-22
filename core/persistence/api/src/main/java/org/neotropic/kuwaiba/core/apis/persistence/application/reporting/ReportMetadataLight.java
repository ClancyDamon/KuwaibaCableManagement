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

package org.neotropic.kuwaiba.core.apis.persistence.application.reporting;

import org.neotropic.kuwaiba.core.apis.persistence.util.MimeTypes;

/**
 * A simplified representation of a {@link Report}.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ReportMetadataLight implements Comparable<ReportMetadataLight> {
    /**
     * The output is a CSV text file.
     */
    public static final int TYPE_CSV = 1;
    /**
     * The output is an HTML text file.
     */
    public static final int TYPE_HTML = 2;
    /**
     * The output is a PDF file.
     */
    public static final int TYPE_PDF = 3;
    /**
     * The output is a XLSX spreadsheet.
     */
    public static final int TYPE_XLSX = 4;
    /**
     * Other type of report but CSV, XLSX, HTML or PDF.
     */
    public static final int TYPE_OTHER = 10;
    /**
     * Report id.
     */
    private long id;
    /**
     * Report name.
     */
    private String name;
    /**
     * Report description.
     */
    private String description;
    /**
     * Is the report enabled?
     */
    private Boolean enabled;
    /**
     * The type of report. 
     */
    private int type;
    
    public ReportMetadataLight(long id, String name, String description, boolean enabled, int type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    public static String getTypeAsString(Integer type) {
        switch(type) {
            case TYPE_CSV:
                return "CSV";
            case TYPE_HTML:
                return "HTML";
            case TYPE_PDF:
                 return "PDF";
            case TYPE_XLSX:
                 return "XLSX";
            default:
                 return "Other";
        }
    }

    @Override
    public int compareTo(ReportMetadataLight o) {
        return getName().compareTo(o.getName());
    }
    
    public static String getMimeTypeForReport(int reportType) {
        switch (reportType) {
            case TYPE_CSV:
                return MimeTypes.MIME_TYPE_CSV;
            case TYPE_HTML:
                return MimeTypes.MIME_TYPE_HTML;
            case TYPE_PDF:
                return MimeTypes.MIME_TYPE_PDF;
            case TYPE_XLSX:
                return MimeTypes.MIME_TYPE_XLSX;
            default:
                return MimeTypes.MIME_TYPE_OTHER;
        }
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
