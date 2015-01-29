package mobi.chouette.exchange.neptune.parser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.log4j.Log4j;
import mobi.chouette.common.Context;
import mobi.chouette.exchange.neptune.importer.Constant;
import mobi.chouette.importer.Parser;
import mobi.chouette.importer.ParserFactory;
import mobi.chouette.importer.ParserUtils;
import mobi.chouette.importer.XPPUtil;
import mobi.chouette.model.Line;
import mobi.chouette.model.Route;
import mobi.chouette.model.type.TransportModeNameEnum;
import mobi.chouette.model.type.UserNeedEnum;
import mobi.chouette.model.util.ObjectFactory;
import mobi.chouette.model.util.Referential;

import org.xmlpull.v1.XmlPullParser;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

@Log4j
public class LineParser implements Parser, Constant {
	private static final String CHILD_TAG = "Line";

	@Override
	public void parse(Context context) throws Exception {

		XmlPullParser xpp = (XmlPullParser) context.get(XPP);
		Referential referential = (Referential) context.get(REFERENTIAL);

		xpp.require(XmlPullParser.START_TAG, null, CHILD_TAG);
		context.put(COLUMN_NUMBER, xpp.getColumnNumber());
		context.put(LINE_NUMBER, xpp.getLineNumber());

		Line line = null;
		while (xpp.nextTag() == XmlPullParser.START_TAG) {

			if (xpp.getName().equals("objectId")) {
				String objectId = ParserUtils.getText(xpp.nextText());
				line = ObjectFactory.getLine(referential, objectId);
			} else if (xpp.getName().equals("objectVersion")) {
				Integer version = ParserUtils.getInt(xpp.nextText());
				line.setObjectVersion(version);
			} else if (xpp.getName().equals("creationTime")) {
				Date creationTime = ParserUtils.getSQLDateTime(xpp.nextText());
				line.setCreationTime(creationTime);
			} else if (xpp.getName().equals("creatorId")) {
				line.setCreatorId(ParserUtils.getText(xpp.nextText()));
			} else if (xpp.getName().equals("name")) {
				line.setName(ParserUtils.getText(xpp.nextText()));
			} else if (xpp.getName().equals("number")) {
				line.setNumber(ParserUtils.getText(xpp.nextText()));
			} else if (xpp.getName().equals("publishedName")) {
				line.setPublishedName(ParserUtils.getText(xpp.nextText()));
			} else if (xpp.getName().equals("transportModeName")) {
				TransportModeNameEnum value = ParserUtils.getEnum(
						TransportModeNameEnum.class, xpp.nextText());
				line.setTransportModeName(value);
			} else if (xpp.getName().equals("LineEnd")) {
				String objectId = ParserUtils.getText(xpp.nextText());
				// TODO [DSU] LineEnd
			} else if (xpp.getName().equals("routeId")) {
				String objectId = ParserUtils.getText(xpp.nextText());
				Route route = ObjectFactory.getRoute(referential, objectId);
				route.setLine(line);
			} else if (xpp.getName().equals("ptNetworkIdShortcut")) {
				final String objectId = ParserUtils.getText(xpp.nextText());
				// TODO [DSU] ptNetworkIdShortcut
			} else if (xpp.getName().equals("registration")) {
				while (xpp.nextTag() == XmlPullParser.START_TAG) {
					if (xpp.getName().equals("registrationNumber")) {
						line.setRegistrationNumber(ParserUtils.getText(xpp
								.nextText()));
					} else {
						XPPUtil.skipSubTree(log, xpp);
					}
				}
			} else if (xpp.getName().equals("comment")) {
				line.setComment(ParserUtils.getText(xpp.nextText()));
			} else if (xpp.getName().equals("LineExtension")) {

				while (xpp.nextTag() == XmlPullParser.START_TAG) {
					if (xpp.getName().equals("mobilityRestrictedSuitability")) {
						line.setRegistrationNumber(ParserUtils.getText(xpp
								.nextText()));
					} else if (xpp.getName().equals(
							"accessibilitySuitabilityDetails")) {
						List<UserNeedEnum> userNeeds = new ArrayList<UserNeedEnum>();
						while (xpp.nextTag() == XmlPullParser.START_TAG) {
							if (xpp.getName().equals("MobilityNeed")
									|| xpp.getName()
											.equals("PsychosensoryNeed")
									|| xpp.getName().equals("MedicalNeed")) {
								UserNeedEnum userNeed = ParserUtils.getEnum(
										UserNeedEnum.class, xpp.nextText());
								if (userNeed != null) {
									userNeeds.add(userNeed);
								}
							} else {
								XPPUtil.skipSubTree(log, xpp);
							}
						}
						line.setUserNeeds(userNeeds);
					} else {
						XPPUtil.skipSubTree(log, xpp);
					}
				}

			} else {
				XPPUtil.skipSubTree(log, xpp);
			}
		}
	}

	private void todo(Referential referential, final Line line) {

		Map<String, Line> removed = Maps.filterEntries(referential.getLines(),
				new Predicate<Map.Entry<String, Line>>() {
					@Override
					public boolean apply(Entry<String, Line> input) {
						boolean result = false;
						Line item = input.getValue();

						if (!item.equals(line)) {
							item.setPTNetwork(null);
							result = true;
						}
						return result;
					}
				});

		for (String key : removed.keySet()) {
			referential.getLines().remove(key);
		}
	}

	static {
		ParserFactory.register(LineParser.class.getName(), new ParserFactory() {
			private LineParser instance = new LineParser();

			@Override
			protected Parser create() {
				return instance;
			}
		});
	}
}