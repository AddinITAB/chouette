package mobi.chouette.exchange.neptune;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import lombok.Getter;
import lombok.extern.log4j.Log4j;
import mobi.chouette.common.TransportMode;
import mobi.chouette.exchange.AbstractTransportModeConverter;
import mobi.chouette.exchange.TransportModeConverter;
import mobi.chouette.exchange.TransportModeConverterFactory;

@Log4j
public class NeptuneTransportModeConverter extends AbstractTransportModeConverter{
	private static final String transportModeUrlSrc = "https://raw.githubusercontent.com/afimb/chouette-projects-i18n/master/data/transport_mode/neptune.json";
	
	@Getter
	protected static Map<TransportMode, TransportMode> mapTransportToPivotMode;
	@Getter
	protected static Map<TransportMode, TransportMode> mapPivotToTransportMode;
	
	private NeptuneTransportModeConverter(){

	}

	private static NeptuneTransportModeConverter INSTANCE = null;
	public static synchronized NeptuneTransportModeConverter getInstance(){
		if(INSTANCE == null){
			log.warn("Parsing fichier neptune JSON");
			getTransportModeListFromJSONFile(transportModeUrlSrc);
			INSTANCE = new NeptuneTransportModeConverter();
			log.warn("Neptune mapTransportToPivotMode size : " + mapTransportToPivotMode.size());
			log.warn("Neptune mapPivotToTransportMode size : " + mapPivotToTransportMode.size());
		}
		
		return INSTANCE;
	}
	
	public static void getTransportModeListFromJSONFile(String urlStr) {
		byte[] bytes = null;
		String text = null;
		
		try {
			mapTransportToPivotMode = new HashMap<TransportMode, TransportMode>();
			mapPivotToTransportMode = new HashMap<TransportMode, TransportMode>();
			URL url = new URL(urlStr);
			InputStream is = url.openStream();
			
			bytes = IOUtils.toByteArray(is);
			text = new String(bytes, "UTF-8");
			if (text != null && text.trim().startsWith("[") && text.trim().endsWith("]")) {
				JSONArray arrayModesTransport;
				try {
					arrayModesTransport = new JSONArray(text);
					log.warn(arrayModesTransport.toString());
					for (int i = 0; i < arrayModesTransport.length(); i++) {
						JSONObject transportMode = arrayModesTransport.getJSONObject(i);
						String mode = transportMode.optString(MODE, null);
						String subMode = transportMode.optString(SUBMODE, null);
						String pivot = transportMode.optString(PIVOT, null);
						String pivotSub = transportMode.optString(PIVOT_SUBMODE, null);

						if (mode != null && subMode != null && pivot != null && pivotSub != null) {
							mapTransportToPivotMode.put(new TransportMode(mode, subMode), new TransportMode(pivot, pivotSub));
							mapPivotToTransportMode.put(new TransportMode(pivot, pivotSub), new TransportMode(mode, subMode));
						}
					}
				} catch (JSONException e) {
					log.warn("unparsable GTFSTransportMode JSon Object");
				}

			}

		} catch (IOException e) {
			log.warn("Cannot read json file from server");
		}
	}
	
	

	@Override
	public TransportMode genericToSpecificMode(TransportMode pivotMode) {
		// Search pivot mode matching with neptune transport mode
		TransportMode tM = mapPivotToTransportMode.get(pivotMode);
		
		if (tM != null)
			return tM;
		
		// If not found set submode to unspecified and start again
		pivotMode.setSubMode("unspecified");
		tM = mapPivotToTransportMode.get(pivotMode);
		
		return tM;	
	}

	@Override
	public TransportMode specificToGenericMode(TransportMode specificMode) {
//		log.warn("NeptuneTransportModeConverter specific mode : " + specificMode.getMode() + ":" + specificMode.getSubMode());
//		log.warn("NeptuneTransportModeConverter mapTransportToPivotMode size : " +  mapTransportToPivotMode.size());
//		TransportMode tM = null;
//		// Search specific mode matching with pivot transport mode
//		for (Map.Entry<TransportMode, TransportMode> entry : mapTransportToPivotMode.entrySet())
//		{
//		    TransportMode entryT = entry.getKey();
//		    log.warn(entryT.getMode() + ":" + entryT.getSubMode());
//		    if (specificMode.getMode().equals(entryT.getMode()) && specificMode.getSubMode().equals(entryT.getSubMode())) {
//				tM =  entry.getValue();
//				break;
//			}
//		}
		// Search specific mode matching with pivot transport mode
		TransportMode tM = mapTransportToPivotMode.get(specificMode);
				
		log.warn("NeptuneTransportModeConverter generic mode : " + tM.getMode() + ":" + tM.getSubMode());
		
		return tM;	
	}
	
	public static class DefaultFactory extends TransportModeConverterFactory {

		@Override
		protected TransportModeConverter create() throws IOException {
			TransportModeConverter result = new NeptuneTransportModeConverter();
			return result;
		}
	}

	static {
		TransportModeConverterFactory.factories.put("neptune", new DefaultFactory());
	}
}
