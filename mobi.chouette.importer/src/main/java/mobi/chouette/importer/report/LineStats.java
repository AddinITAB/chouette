
package mobi.chouette.importer.report;


import javax.xml.bind.annotation.XmlAttribute;

import lombok.Data;

@Data
public class LineStats {

  @XmlAttribute(name = "route_count")
  private Long routeCount;
  
  @XmlAttribute(name = "connection_link_count")
  private Long connectionLinkCount;
  
  @XmlAttribute(name = "time_table_count")
  private Long timeTableCount;
  
  @XmlAttribute(name = "stop_area_count")
  private Long stopAreaCount;
  
  @XmlAttribute(name = "acces_point_count")
  private Long accesPointCount;
  
  @XmlAttribute(name = "vehicle_journey_count")
  private Long vehicleJourneyCount;
  
  @XmlAttribute(name = "journey_pattern_count")
  private Long journeyPatternCount;

}