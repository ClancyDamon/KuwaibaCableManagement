/**
 * Scripted query to search nearby houses without ONT,
 * inside of a radius in meters with center in the given latitude and longitude.
 * 
 * Pool Name: ospman.geo
 * Name: Houses without ONT
 * Parameters:
 *  latitude:Double, 
 *  longitude:Double, 
 *  radius:Double (meters)
 *  viewNodes:List<AbstractViewNode>
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryResult
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject

def latitude = scriptParameters.get("latitude")
def longitude = scriptParameters.get("longitude")
def radius = scriptParameters.get("radius")
def viewNodes = scriptParameters.get("viewNodes")

def query = """
  MATCH (house:inventoryObjects)-[:INSTANCE_OF]->(class:classes {name: 'House'})
  WHERE NOT (house)<-[:CHILD_OF*]-(:inventoryObjects)-[:INSTANCE_OF]->(:classes {name: 'OpticalNetworkTerminal'})
  RETURN house AS house, class.name AS class
"""

def queryResult = connectionHandler.execute(query)
def nodes = new ArrayList()
while (queryResult.hasNext()) {
  def next = queryResult.next()
  def node = next.get("house")
  def businessObject = new BusinessObject(next.get("class"), node.getProperty("_uuid"), node.getProperty("name"))
  businessObject.setAttributes(['longitude': node.getProperty("longitude", null), 'latitude': node.getProperty("latitude", null)])
  nodes.add(businessObject)
}
def viewNodesMap = new HashMap()
viewNodes.each { viewNodesMap.put(it.getIdentifier().getId(), [ 'latitude': it.getProperties()['lat'], 'longitude': it.getProperties()['lon'] ]) }

def parameterViewNodes = new ArrayList()

nodes.each {
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
def parameters = ['viewNodes': parameterViewNodes, 'longitude': longitude, 'latitude': latitude, 'radius': radius]
queryResult = connectionHandler.execute(query, parameters).columnAs("result")

def result = new ScriptedQueryResult(4)

while (queryResult.hasNext()) {
  def next = queryResult.next()
  next.each {
    result.addRow([ new BusinessObject(it['class'], it['_uuid'], it['name']), it['distance'], it['latitude'], it['longitude'] ])
  }
}
result