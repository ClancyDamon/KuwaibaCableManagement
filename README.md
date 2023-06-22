# Introduction
***  

Kuwaiba is an open source, enterprise-grade network inventory system.

This software is provided “AS IS”, with no warranty at all. Install it and use it at your own risk.

This software is dual-licensed EPLv1 and GPLv3, choose the one that fits your purposes, but keep in mind that we ship third-party libraries whose licenses are only compatible with GPL. See THIRDPARTY and LICENSE.EPL/LICENSE.GPL3 files for details.

Visit https://www.kuwaiba.org or subscribe to our YouTube channel (https://www.youtube.com/user/neotropicco) for more tutorials and guides on how to build, install and use the application.

Get superb enterprise support from Neotropic SAS! (https://www.neotropic.co)

# Kick Start Guide
***  
**1. Get the code**
`svn co https://svn.code.sf.net/p/kuwaiba/code/server/trunk`

**2. Get and separately build the dependencies (mostly add-ons) not available on the central Maven repository**  

* [Ace Editor Component for Vaadin Flow](https://sourceforge.net/p/kuwaiba/code/HEAD/tree/components/ace-editor-flow)
* [Google Maps Component for Vaadin Flow](https://sourceforge.net/p/kuwaiba/code/HEAD/tree/components/gmaps-flow)
* [Mx Graph Component for Vaadin Flow](https://sourceforge.net/p/kuwaiba/code/HEAD/tree/components/mxgraph-flow)
* [Google Charts Component for Vaadin Flow](https://sourceforge.net/p/kuwaiba/code/HEAD/tree/components/gcharts-flow)
* [Paper Dialog for Vaadin Flow](https://sourceforge.net/p/kuwaiba/code/HEAD/tree/components/paper-dialog-flow)
* [Paper Toggle Button for Vaadin Flow](https://sourceforge.net/p/kuwaiba/code/HEAD/tree/components/paper-toggle-button-flow)
* [IBM Gantt Component for Vaadin Flow](https://sourceforge.net/p/kuwaiba/code/HEAD/tree/components/gantt-chart-flow)
* [Apache FreeMarker] (https://freemarker.apache.org/)
* [OpenLayers for Vaadin Flow](https://sourceforge.net/p/kuwaiba/code/HEAD/tree/components/olmap-flow/)

**2. Set the application properties in the Web Client Module**  

`db.path=/data/db/kuwaiba.db`  

**3. Build Web Client Module**  

# License
***
