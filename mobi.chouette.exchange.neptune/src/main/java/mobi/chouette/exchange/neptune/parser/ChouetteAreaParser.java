package mobi.chouette.exchange.neptune.parser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.log4j.Log4j;
import mobi.chouette.common.Context;
import mobi.chouette.common.XPPUtil;
import mobi.chouette.exchange.importer.Parser;
import mobi.chouette.exchange.importer.ParserFactory;
import mobi.chouette.exchange.importer.ParserUtils;
import mobi.chouette.exchange.neptune.Constant;
import mobi.chouette.exchange.neptune.JsonExtension;
import mobi.chouette.exchange.neptune.NeptuneChouetteIdGenerator;
import mobi.chouette.exchange.neptune.NeptuneChouetteIdObjectUtil;
import mobi.chouette.exchange.neptune.importer.NeptuneImportParameters;
import mobi.chouette.exchange.neptune.model.AreaCentroid;
import mobi.chouette.exchange.neptune.model.NeptuneObjectFactory;
import mobi.chouette.exchange.neptune.validation.AreaCentroidValidator;
import mobi.chouette.exchange.neptune.validation.RoutingConstraintValidator;
import mobi.chouette.exchange.neptune.validation.StopAreaValidator;
import mobi.chouette.exchange.validation.ValidatorFactory;
import mobi.chouette.model.RoutingConstraint;
import mobi.chouette.model.StopArea;
import mobi.chouette.model.StopPoint;
import mobi.chouette.model.type.ChouetteAreaEnum;
import mobi.chouette.model.type.LongLatTypeEnum;
import mobi.chouette.model.type.UserNeedEnum;
import mobi.chouette.model.util.Referential;

import org.xmlpull.v1.XmlPullParser;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@Log4j
public class ChouetteAreaParser implements Parser, Constant, JsonExtension {
	private static final String CHILD_TAG = "ChouetteArea";

	@Override
	public void parse(Context context) throws Exception {

		XmlPullParser xpp = (XmlPullParser) context.get(PARSER);
		// Referential referential = (Referential) context.get(REFERENTIAL);

		xpp.require(XmlPullParser.START_TAG, null, CHILD_TAG);
		context.put(COLUMN_NUMBER, xpp.getColumnNumber());
		context.put(LINE_NUMBER, xpp.getLineNumber());

		BiMap<String, String> map = HashBiMap.create();
		context.put(STOPAREA_AREACENTROID_MAP, map);

		while (xpp.nextTag() == XmlPullParser.START_TAG) {

			if (xpp.getName().equals("StopArea")) {
				parseStopArea(context, map);
			} else if (xpp.getName().equals("AreaCentroid")) {
				parseAreaCentroid(context, map);
			} else {
				XPPUtil.skipSubTree(log, xpp);
			}
		}
	}

	private void parseStopArea(Context context, BiMap<String, String> map) throws Exception {
		XmlPullParser xpp = (XmlPullParser) context.get(PARSER);
		Referential referential = (Referential) context.get(REFERENTIAL);

		xpp.require(XmlPullParser.START_TAG, null, "StopArea");
		int columnNumber = xpp.getColumnNumber();
		int lineNumber = xpp.getLineNumber();

		NeptuneImportParameters parameters = (NeptuneImportParameters) context.get(CONFIGURATION);
		NeptuneChouetteIdGenerator neptuneChouetteIdGenerator = (NeptuneChouetteIdGenerator) context
				.get(CHOUETTEID_GENERATOR);

		StopAreaValidator validator = (StopAreaValidator) ValidatorFactory.create(StopAreaValidator.class.getName(),
				context);
		RoutingConstraintValidator rcValidator = (RoutingConstraintValidator) ValidatorFactory.create(
				RoutingConstraintValidator.class.getName(), context);

		StopArea stopArea = null;
		RoutingConstraint routingConstraint = null;
		List<String> contains = new ArrayList<String>();

		String objectId = null;
		while (xpp.nextTag() == XmlPullParser.START_TAG) {
			if (xpp.getName().equals("objectId")) {
				objectId = ParserUtils.getText(xpp.nextText());
				stopArea = NeptuneChouetteIdObjectUtil.getStopArea(referential, neptuneChouetteIdGenerator
						.toChouetteId(objectId, parameters.getDefaultCodespace(), StopArea.class));
				stopArea.setFilled(true);
			} else if (xpp.getName().equals("objectVersion")) {
				Integer version = ParserUtils.getInt(xpp.nextText());
				stopArea.setObjectVersion(version);
			} else if (xpp.getName().equals("creationTime")) {
				Date creationTime = ParserUtils.getSQLDateTime(xpp.nextText());
				stopArea.setCreationTime(creationTime);
			} else if (xpp.getName().equals("creatorId")) {
				stopArea.setCreatorId(ParserUtils.getText(xpp.nextText()));
			} else if (xpp.getName().equals("name")) {
				stopArea.setName(ParserUtils.getText(xpp.nextText()));
			} else if (xpp.getName().equals("comment")) {
				stopArea.setComment(ParserUtils.getText(xpp.nextText()));
			} else if (xpp.getName().equals("StopAreaExtension")) {
				String areaType = null;
				while (xpp.nextTag() == XmlPullParser.START_TAG) {
					if (xpp.getName().equals("areaType")) {
						areaType = ParserUtils.getText(xpp.nextText());
						if (areaType.equalsIgnoreCase("ITL")) {
							// Arret Netex : Pass stopArea of type ITL into
							// routingConstraint object
							routingConstraint = NeptuneChouetteIdObjectUtil.getRoutingConstraint(referential,
									neptuneChouetteIdGenerator.toChouetteId(objectId, parameters.getDefaultCodespace(),
											RoutingConstraint.class));
							routingConstraint.setName(stopArea.getName());
							routingConstraint.setComment(stopArea.getComment());
							referential.getStopAreas().remove(
									neptuneChouetteIdGenerator.toChouetteId(objectId, parameters.getDefaultCodespace(),
											StopArea.class));
							for (String childId : contains) {
								StopArea child = NeptuneChouetteIdObjectUtil.getStopArea(
										referential,
										neptuneChouetteIdGenerator.toChouetteId(childId,
												parameters.getDefaultCodespace(), StopArea.class));
								// Arret NETEX : Let Routing Constraint handle
								// this
								routingConstraint.addRoutingConstraintStopArea(child);
							}

						} else {
							stopArea.setAreaType(ParserUtils.getEnum(ChouetteAreaEnum.class, areaType));

							if (stopArea.getAreaType() != ChouetteAreaEnum.BoardingPosition
									&& stopArea.getAreaType() != ChouetteAreaEnum.Quay) {
								for (String childId : contains) {
									StopArea child = NeptuneChouetteIdObjectUtil.getStopArea(
											referential,
											neptuneChouetteIdGenerator.toChouetteId(childId,
													parameters.getDefaultCodespace(), StopArea.class));
									child.setParent(stopArea);
								}
							} else {
								log.warn("Stop Area chouette Id : " + stopArea.getChouetteId().getCodeSpace() + ":" + stopArea.getChouetteId().getTechnicalId());
								for (String childId : contains) {
									StopPoint stopPoint = NeptuneChouetteIdObjectUtil.getStopPoint(
											referential,
											neptuneChouetteIdGenerator.toChouetteId(childId,
													parameters.getDefaultCodespace(), StopPoint.class));
									stopPoint.setContainedInStopArea(stopArea);
								}

							}
						}

					} else if (xpp.getName().equals("nearestTopicName")) {
						stopArea.setNearestTopicName(ParserUtils.getText(xpp.nextText()));
					} else if (xpp.getName().equals("fareCode")) {
						stopArea.setFareCode(ParserUtils.getInt(xpp.nextText()));
					} else if (xpp.getName().equals("registration")) {
						while (xpp.nextTag() == XmlPullParser.START_TAG) {
							if (xpp.getName().equals("registrationNumber")) {
								stopArea.setRegistrationNumber(ParserUtils.getText(xpp.nextText()));
							} else {
								XPPUtil.skipSubTree(log, xpp);
							}
						}
					} else if (xpp.getName().equals("mobilityRestrictedSuitability")) {
						stopArea.setMobilityRestrictedSuitable(ParserUtils.getBoolean(xpp.nextText()));
					} else if (xpp.getName().equals("accessibilitySuitabilityDetails")) {
						List<UserNeedEnum> userNeeds = new ArrayList<UserNeedEnum>();
						while (xpp.nextTag() == XmlPullParser.START_TAG) {
							if (xpp.getName().equals("MobilityNeed") || xpp.getName().equals("PsychosensoryNeed")
									|| xpp.getName().equals("MedicalNeed") || xpp.getName().equals("EncumbranceNeed")) {
								UserNeedEnum userNeed = ParserUtils.getEnum(UserNeedEnum.class, xpp.nextText());
								if (userNeed != null) {
									userNeeds.add(userNeed);
								}

							} else {
								XPPUtil.skipSubTree(log, xpp);
							}
						}
						stopArea.setUserNeeds(userNeeds);
					} else if (xpp.getName().equals("stairsAvailability")) {
						stopArea.setStairsAvailable(ParserUtils.getBoolean(xpp.nextText()));
					} else if (xpp.getName().equals("liftAvailability")) {
						stopArea.setLiftAvailable(ParserUtils.getBoolean(xpp.nextText()));
					} else {
						XPPUtil.skipSubTree(log, xpp);
					}
				}
			} else if (xpp.getName().equals("contains")) {
				String containsId = ParserUtils.getText(xpp.nextText());
				contains.add(containsId);
				validator.addContains(context, objectId, containsId);

			} else if (xpp.getName().equals("centroidOfArea")) {
				String value = ParserUtils.getText(xpp.nextText());
				validator.addAreaCentroidId(context, objectId, value);
				map.put(objectId, value);

			} else {
				XPPUtil.skipSubTree(log, xpp);
			}
		}
		// Arret Netex : If StopArea is ITL type, we use
		// RoutingConstraintValidator
		if (routingConstraint != null)
			rcValidator.addStopAreaLocation(context, routingConstraint, lineNumber, columnNumber);
		else
			validator.addLocation(context, stopArea, lineNumber, columnNumber);
	}

	private void parseAreaCentroid(Context context, BiMap<String, String> map) throws Exception {
		XmlPullParser xpp = (XmlPullParser) context.get(PARSER);
		Referential referential = (Referential) context.get(REFERENTIAL);
		NeptuneObjectFactory factory = (NeptuneObjectFactory) context.get(NEPTUNE_OBJECT_FACTORY);

		xpp.require(XmlPullParser.START_TAG, null, "AreaCentroid");
		int columnNumber = xpp.getColumnNumber();
		int lineNumber = xpp.getLineNumber();

		NeptuneImportParameters parameters = (NeptuneImportParameters) context.get(CONFIGURATION);
		NeptuneChouetteIdGenerator neptuneChouetteIdGenerator = (NeptuneChouetteIdGenerator) context
				.get(CHOUETTEID_GENERATOR);

		AreaCentroidValidator validator = (AreaCentroidValidator) ValidatorFactory.create(
				AreaCentroidValidator.class.getName(), context);

		BiMap<String, String> inverse = map.inverse();
		StopArea stopArea = null;
		AreaCentroid areaCentroid = null;
		String objectId = null;

		while (xpp.nextTag() == XmlPullParser.START_TAG) {
			if (xpp.getName().equals("objectId")) {
				objectId = ParserUtils.getText(xpp.nextText());
				areaCentroid = factory.getAreaCentroid(neptuneChouetteIdGenerator.toChouetteId(objectId,
						parameters.getDefaultCodespace(), AreaCentroid.class));
				String areaId = inverse.get(objectId);
				if (areaId != null) {
					stopArea = NeptuneChouetteIdObjectUtil.getStopArea(referential, neptuneChouetteIdGenerator
							.toChouetteId(areaId, parameters.getDefaultCodespace(), StopArea.class));
					areaCentroid.setContainedIn(stopArea);
				}

			} else if (xpp.getName().equals("name")) {
				areaCentroid.setName(ParserUtils.getText(xpp.nextText()));
				if (stopArea != null && stopArea.getName() == null)
					stopArea.setName(areaCentroid.getName());
			} else if (xpp.getName().equals("comment")) {
				areaCentroid.setComment(ParserUtils.getText(xpp.nextText()));
				if (stopArea != null && stopArea.getComment() == null)
					stopArea.setComment(areaCentroid.getComment());
			} else if (xpp.getName().equals("longLatType")) {
				LongLatTypeEnum longLatType = ParserUtils.getEnum(LongLatTypeEnum.class, xpp.nextText());
				if (stopArea != null) {
					stopArea.setLongLatType(longLatType);
					validator.addLongLatType(context, objectId, stopArea.getLongLatType());
				}
			} else if (xpp.getName().equals("latitude")) {
				BigDecimal latitude = ParserUtils.getBigDecimal(xpp.nextText());
				if (stopArea != null)
					stopArea.setLatitude(latitude);
			} else if (xpp.getName().equals("longitude")) {
				BigDecimal longitude = ParserUtils.getBigDecimal(xpp.nextText());
				if (stopArea != null)
					stopArea.setLongitude(longitude);
			} else if (xpp.getName().equals("containedIn")) {
				String containedIn = ParserUtils.getText(xpp.nextText());
				if (areaCentroid.getContainedIn() == null) {
					stopArea = NeptuneChouetteIdObjectUtil.getStopArea(referential, neptuneChouetteIdGenerator
							.toChouetteId(containedIn, parameters.getDefaultCodespace(), StopArea.class));
					areaCentroid.setContainedIn(stopArea);
				}
				validator.addContainedIn(context, objectId, containedIn);
			} else if (xpp.getName().equals("address")) {

				while (xpp.nextTag() == XmlPullParser.START_TAG) {
					if (xpp.getName().equals("countryCode")) {
						String countryCode = ParserUtils.getText(xpp.nextText());
						if (stopArea != null)
							stopArea.setCountryCode(countryCode);
					} else if (xpp.getName().equals("streetName")) {
						String streetName = ParserUtils.getText(xpp.nextText());
						if (stopArea != null)
							stopArea.setStreetName(streetName);
					} else {
						XPPUtil.skipSubTree(log, xpp);
					}
				}
			} else if (xpp.getName().equals("projectedPoint")) {

				while (xpp.nextTag() == XmlPullParser.START_TAG) {
					if (xpp.getName().equals("X")) {
						BigDecimal value = ParserUtils.getBigDecimal(xpp.nextText());
						if (stopArea != null)
							stopArea.setX(value);
					} else if (xpp.getName().equals("Y")) {
						BigDecimal value = ParserUtils.getBigDecimal(xpp.nextText());
						if (stopArea != null)
							stopArea.setY(value);
					} else if (xpp.getName().equals("projectionType")) {
						String value = ParserUtils.getText(xpp.nextText());
						if (stopArea != null)
							stopArea.setProjectionType(value);
					} else {
						XPPUtil.skipSubTree(log, xpp);
					}
				}
			} else {
				XPPUtil.skipSubTree(log, xpp);
			}
		}
		validator.addLocation(context, areaCentroid, lineNumber, columnNumber);
	}

	static {
		ParserFactory.register(ChouetteAreaParser.class.getName(), new ParserFactory() {
			private ChouetteAreaParser instance = new ChouetteAreaParser();

			@Override
			protected Parser create() {
				return instance;
			}
		});
	}
}
