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

package org.neotropic.kuwaiba.core.apis.persistence.util;

/**
 * Common MIME types used chiefly when generating reports or presenting attachments for download.
 * or
 *  @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class MimeTypes {
    /**
     * MIME type for CSV text files.
     */
    public static final String MIME_TYPE_CSV = "text/csv";
    /**
     * MIME type for HTML text documents.
     */
    public static final String MIME_TYPE_HTML = "text/html";
    /**
     * MIME type for PDF binary files.
     */
    public static final String MIME_TYPE_PDF = "application/pdf";
    /**
     * MIME type for PDF binary spreadsheets.
     */
    public static final String MIME_TYPE_XLSX = "application/vnd.ms-excel";
    /**
     * MIME type for other type of binary formats.
     */
    public static final String MIME_TYPE_OTHER = "application/octet-stream";
}
