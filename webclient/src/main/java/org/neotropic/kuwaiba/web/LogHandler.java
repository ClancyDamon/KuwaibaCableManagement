/*
 *  Copyright 2010-2021 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Custom logger that appends the prefix <code>[KUWAIBA]</code> tag to Kuwaiba-generated messages.
 * @author Charles Edward Bedon Cortazar {@literal charles.bedon@kuwaiba.org } 
 */
public class LogHandler extends Handler {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    @Override
    public void publish(LogRecord logRecord) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatter.format(new Date(logRecord.getMillis())))
          .append(" ")
          .append(logRecord.getLevel())
          .append("  [")
          .append(logRecord.getSourceClassName())
          .append("]  ")      
          .append(logRecord.getSourceMethodName())
          .append("    : [KUWAIBA] ");
        if (logRecord.getThrown() != null) {
            StringWriter theStringWriter = new StringWriter();
            PrintWriter thePrintWriter = new PrintWriter(theStringWriter);
            logRecord.getThrown().printStackTrace(thePrintWriter);
            sb.append(theStringWriter.toString());
        } else
            sb.append(String.format(logRecord.getMessage(), logRecord.getParameters()));
        
        System.out.println(sb);
    }

    @Override
    public void flush() { }

    @Override
    public void close() throws SecurityException { }
    
}
