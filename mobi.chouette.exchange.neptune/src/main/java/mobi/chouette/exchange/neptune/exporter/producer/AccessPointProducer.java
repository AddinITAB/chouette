package mobi.chouette.exchange.neptune.exporter.producer;

import java.math.BigDecimal;

import lombok.extern.log4j.Log4j;
import mobi.chouette.common.Constant;
import mobi.chouette.common.Context;
import mobi.chouette.exchange.neptune.NeptuneChouetteIdGenerator;
import mobi.chouette.exchange.neptune.exporter.NeptuneExportParameters;
import mobi.chouette.model.AccessPoint;
import mobi.chouette.model.type.AccessPointTypeEnum;
import mobi.chouette.model.type.LongLatTypeEnum;

import org.trident.schema.trident.AddressType;
import org.trident.schema.trident.LongLatTypeType;
import org.trident.schema.trident.PTAccessPointType;
import org.trident.schema.trident.ProjectedPointType;

@Log4j
public class AccessPointProducer extends
      AbstractJaxbNeptuneProducer<PTAccessPointType, AccessPoint> implements Constant
{
   // @Override
   public PTAccessPointType produce(Context context, AccessPoint accessPoint, boolean addExtension)
   {
	   
	   NeptuneExportParameters parameters = (NeptuneExportParameters) context.get(CONFIGURATION);
	   NeptuneChouetteIdGenerator neptuneChouetteIdGenerator = (NeptuneChouetteIdGenerator) context.get(CHOUETTEID_GENERATOR);
		
      PTAccessPointType jaxbAccessPoint = tridentFactory
            .createPTAccessPointType();

      //
      populateFromModel(context, jaxbAccessPoint, accessPoint);

      jaxbAccessPoint.setComment(getNotEmptyString(accessPoint.getComment()));
      jaxbAccessPoint.setName(accessPoint.getName());

      // type
      if (accessPoint.getType() != null)
      {
         AccessPointTypeEnum type = accessPoint.getType();
         jaxbAccessPoint.setType(type.name());
      }

      // opening/closingTime
      jaxbAccessPoint.setOpeningTime(toCalendar(accessPoint.getOpeningTime()));
      jaxbAccessPoint.setClosingTime(toCalendar(accessPoint.getClosingTime()));

      if (accessPoint.hasAddress())
      {
         AddressType castorAddress = tridentFactory.createAddressType();
         castorAddress.setCountryCode(getNotEmptyString(accessPoint
               .getCountryCode()));
         castorAddress.setStreetName(getNotEmptyString(accessPoint
               .getStreetName()));
         jaxbAccessPoint.setAddress(castorAddress);
      }

      jaxbAccessPoint.setContainedIn(neptuneChouetteIdGenerator.toSpecificFormatId(accessPoint.getContainedIn().getChouetteId(), parameters.getDefaultCodespace(), accessPoint.getContainedIn()));

      if (accessPoint.hasCoordinates())
      {
         LongLatTypeEnum longLatType = accessPoint.getLongLatType();
         try
         {
            jaxbAccessPoint.setLongLatType(LongLatTypeType
                  .fromValue(longLatType.name()));
            jaxbAccessPoint.setLatitude(accessPoint.getLatitude());
            jaxbAccessPoint.setLongitude(accessPoint.getLongitude());
         } catch (IllegalArgumentException e)
         {
            // TODO generate report
         }
      }
      else
      {
			log.error("missing coordinates for AccessPoint "+accessPoint.toString()+" "+accessPoint.getName());
    	  // longitude/latitude mmandatory
    	  jaxbAccessPoint.setLatitude(BigDecimal.ZERO);
    	  jaxbAccessPoint.setLongitude(BigDecimal.ZERO);
    	  jaxbAccessPoint.setLongLatType(LongLatTypeType.WGS_84);
      }

      if (accessPoint.hasProjection())
      {
         ProjectedPointType jaxbProjectedPoint = tridentFactory
               .createProjectedPointType();
         jaxbProjectedPoint.setProjectionType(accessPoint.getProjectionType());
         jaxbProjectedPoint.setX(accessPoint.getX());
         jaxbProjectedPoint.setY(accessPoint.getY());
         jaxbAccessPoint.setProjectedPoint(jaxbProjectedPoint);
      }

      return jaxbAccessPoint;
   }

}
