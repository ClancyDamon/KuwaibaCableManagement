/**
 * Finds ONT houses where the ONT firmware are equal inside of a radius in meters 
 * with center in the given latitude and longitude.
 * 
 * Pool Name: ospman.geo
 * Name: ospman.geo.ontHouses
 * Parameters: 
 *  classes: List<String>
 *  latitude:Double, 
 *  longitude:Double, 
 *  radius:Double (meters)
 *  viewNodes:List<AbstractViewNode>
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryResult
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject

def firmware = scriptParameters.get("firmware")
def latitude = scriptParameters.get("latitude")
def longitude = scriptParameters.get("longitude")
def radius = scriptParameters.get("radius")
def viewNodes = scriptParameters.get("viewNodes")

def query = """
  MATCH (class:classes{name:'House'})<-[:INSTANCE_OF]-(house:inventoryObjects)<-[:CHILD_OF*]-(ont:inventoryObjects)-[:INSTANCE_OF]->(:classes{name:'OpticalNetworkTerminal'})
  WHERE ont.softwareVersion = \$firmware
  RETURN DISTINCT house AS house, class.name AS class
"""
def parameters = ['firmware': firmware]
def queryResult = connectionHandler.execute(query, parameters)
def houses = new ArrayList()
while (queryResult.hasNext()) {
  def next = queryResult.next()
  def house = next.get("house")
  def businessObject = new BusinessObject(next.get("class"), house.getProperty("_uuid"), house.getProperty("name"))
  businessObject.setAttributes(['longitude': house.getProperty("longitude", null), 'latitude': house.getProperty("latitude", null)])
  houses.add(businessObject)
}
def viewNodesMap = new HashMap()
viewNodes.each { viewNodesMap.put(it.getIdentifier().getId(), [ 'latitude': it.getProperties()['lat'], 'longitude': it.getProperties()['lon'] ]) }

def parameterViewNodes = new ArrayList()

houses.each {
  if (viewNodesMap[it.getId()] != null) {
    def viewNode = ['_uuid': it.getId(), 'class': it.getClassName(), 'name': it.getName(), 'longitude': viewNodesMap[it.getId()]['longitude'], 'latitude': viewNodesMap[it.getId()]['latitude']]
    parameterViewNodes.add(viewNode)
  } else if (it.getAttributes()['longitude'] != null && it.getAttributes()['latitude'] != null) {
    def viewNode = ['_uuid': it.getId(), 'class': it.getClassName(), 'name': it.getName(), 'longitude': it.getAttributes()['longitude'], 'latitude': it.getAttributes()['latitude']]
    parameterViewNodes.add(viewNode)
  }
}

query = """
  RETURN [
      viewNode IN \$viewNodes
      WHERE distance(point({longitude: viewNode.longitude, latitude: viewNode.latitude}), point({longitude: \$longitude, latitude: \$latitude})) <= \$radius
      | {
        class: viewNode.class,
        _uuid: viewNode._uuid,
        name: viewNode.name,
        distance: distance(point({longitude: viewNode.longitude, latitude: viewNode.latitude}), point({longitude: \$longitude, latitude: \$latitude})),
        latitude: viewNode.latitude,
        longitude: viewNode.longitude
        }
    ] AS result
"""
parameters = ['viewNodes': parameterViewNodes, 'longitude': longitude, 'latitude': latitude, 'radius': radius]
queryResult = connectionHandler.execute(query, parameters).columnAs("result")

def result = new ScriptedQueryResult(4)

while (queryResult.hasNext()) {
  def next = queryResult.next()
  next.each {
    result.addRow([ new BusinessObject(it['class'], it['_uuid'], it['name']), it['distance'], it['latitude'], it['longitude'] ])
  }
}
result