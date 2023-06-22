# Consider using a specific version of Alpine Linux instead of the latest version to ensure consistent behavior across deployments
FROM alpine:latest
LABEL org.opencontainers.image.authors="contact@neotropic.co"

# Install dependencies
RUN apk add --no-cache openjdk11 npm maven subversion unzip

# Create user "kuwaiba" so it can run all processes. Shell access will be disabled by default
RUN adduser -D -s /sbin/nologin kuwaiba

# Create base directories
# /data will store the logs, database, the process definitions, background files and attachments
RUN mkdir -p /data/logs
RUN mkdir -p /data/db
RUN mkdir -p /data/files/attachments
RUN mkdir -p /data/img/backgrounds
# /opt/programs will contain all programs and their dependencies
RUN mkdir -p /opt/programs/kuwaiba/server
RUN mkdir -p /opt/programs/kuwaiba/components

# The working directory
WORKDIR /opt/programs

# Download Kuwaiba server and components
RUN svn checkout svn://svn.code.sf.net/p/kuwaiba/code/server/trunk/ kuwaiba/server/
RUN svn checkout svn://svn.code.sf.net/p/kuwaiba/code/components/ace-editor-flow/ace-editor-flow-root/ace-editor-flow/ kuwaiba/components/ace-editor-flow
RUN svn checkout svn://svn.code.sf.net/p/kuwaiba/code/components/gantt-chart-flow/trunk/gantt-chart-flow-root/gantt-chart-flow/ kuwaiba/components/gantt-chart-flow
RUN svn checkout svn://svn.code.sf.net/p/kuwaiba/code/components/gcharts-flow/trunk/gcharts-flow-root/gcharts-flow/ kuwaiba/components/gcharts-flow
RUN svn checkout svn://svn.code.sf.net/p/kuwaiba/code/components/gmaps-flow/trunk/gmaps-flow-root/gmaps-flow/ kuwaiba/components/gmaps-flow
RUN svn checkout svn://svn.code.sf.net/p/kuwaiba/code/components/mxgraph-flow/mx-graph-flow-root/mx-graph-flow/ kuwaiba/components/mx-graph-flow
RUN svn checkout svn://svn.code.sf.net/p/kuwaiba/code/components/olmap-flow/trunk/olmap-flow-root/olmap-flow/ kuwaiba/components/olmap-flow
RUN svn checkout svn://svn.code.sf.net/p/kuwaiba/code/components/paper-autocomplete-flow/paper-autocomplete-flow/ kuwaiba/components/paper-autocomplete-flow
RUN svn checkout svn://svn.code.sf.net/p/kuwaiba/code/components/paper-dialog-flow/paper-dialog-flow/ kuwaiba/components/paper-dialog-flow
RUN svn checkout svn://svn.code.sf.net/p/kuwaiba/code/components/paper-toggle-button-flow/paper-toggle-button-flow/ kuwaiba/components/paper-toggle-button-flow

# Compilation and packaging of each of the components
# "Ace Editor" is a standalone code editor
RUN mvn -f kuwaiba/components/ace-editor-flow/ clean install
# "Gantt Chart" bar chart that shows time along a horizontal axis and activities placed as bars along the time line
RUN mvn -f kuwaiba/components/gantt-chart-flow/ clean install
# "GCharts" provides a perfect way to visualize data on your website
RUN mvn -f kuwaiba/components/gcharts-flow/ clean install
# "GMaps" provides a Google Maps component 
RUN mvn -f kuwaiba/components/gmaps-flow/ clean install
# "Paper Slider" allows user to select a value from a range of values by moving the slider thumb
RUN mvn -f kuwaiba/components/mx-graph-flow/ clean install
# "Open Layers Map" makes it easy to put a dynamic map
RUN mvn -f kuwaiba/components/olmap-flow/ clean install
# "Paper Autocomplete" field to offer a type-ahead functionality
RUN mvn -f kuwaiba/components/paper-autocomplete-flow/ clean install
# "Paper Dialog" is a dialog with Material Design styling and optional animations when it is opened or closed 
RUN mvn -f kuwaiba/components/paper-dialog-flow/ clean install
# "Paper Toggle Button" provides a ON/OFF switch that user can toggle the state by tapping or by dragging the switch
RUN mvn -f kuwaiba/components/paper-toggle-button-flow/ clean install

# Compilation and packaging of Kuwaiba server
RUN mvn -f kuwaiba/server/ clean install
RUN mvn --also-make --projects webclient -f kuwaiba/server/ -Pproduction install

# Move the Kuwaiba server jar to the working directory
RUN mv kuwaiba/server/webclient/target/kuwaiba_server_2.1-beta.jar  ./

# Extract sample database. Use 01_empty_kuwaiba.db.zip if you want a completely clean database
RUN unzip kuwaiba/server/dbs/03_data_sample_kuwaiba.db.zip
RUN mv kuwaiba.db /data/db/

# Move process definitions
RUN mv kuwaiba/server/samples/procman/ /data/processEngine/

# Clean up
RUN rm -rf /opt/programs/kuwaiba

# Remove dependencies
RUN apk del npm maven subversion unzip

# Set permissions for user kuwaiba
RUN chown -R kuwaiba:kuwaiba /data
RUN chown -R kuwaiba:kuwaiba /opt/programs

# Expose the ports. 8080 will give you access to the application (web interface), while
# 8081 will give you access to the SOAP-based web service interface
EXPOSE 8080 8081

# Switch to user kuwaiba
USER kuwaiba
# Set the command to start Kuwaiba and redirect the output to a log file
CMD ["sh", "-c", "java -jar /opt/programs/kuwaiba_server_2.1-beta.jar > /data/logs/kuwaiba_$(date +%Y%m%d_%H%M%S).log 2>&1"]