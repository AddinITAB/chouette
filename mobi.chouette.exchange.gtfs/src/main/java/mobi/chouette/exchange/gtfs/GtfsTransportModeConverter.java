package mobi.chouette.exchange.gtfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.extern.log4j.Log4j;
import mobi.chouette.common.TransportMode;
import mobi.chouette.exchange.AbstractTransportModeConverter;
import mobi.chouette.exchange.TransportModeConverter;
import mobi.chouette.exchange.TransportModeConverterFactory;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@Log4j
public class GtfsTransportModeConverter extends AbstractTransportModeConverter{
	
	private static final String transportModeUrlSrc = "gtfs.json";
	private static BiMap<Integer, TransportMode> mapCodeToTransportMode;
	@Getter
	protected static Map<TransportMode, TransportMode> mapTransportToPivotMode;
	@Getter
	protected static Map<TransportMode, TransportMode> mapPivotToTransportMode;
	
	private GtfsTransportModeConverter(){

	}

	private static GtfsTransportModeConverter INSTANCE = null;
	
	@Getter
	private static List<TransportMode> listTransportMode = null;

	public static synchronized GtfsTransportModeConverter getInstance(){
		
		if(INSTANCE == null){
			getTransportModeListFromJSONFile(getTransportModeUrl(transportModeUrlSrc));
			INSTANCE = new GtfsTransportModeConverter();
		}
		
		return INSTANCE;
	}
	
	
	
	public static void getTransportModeListFromJSONFile(String urlStr) {
		byte[] bytes = null;
		String text = null;
		try {
			mapCodeToTransportMode = HashBiMap.create();
			mapTransportToPivotMode = new HashMap<TransportMode, TransportMode>();
			mapPivotToTransportMode = new HashMap<TransportMode, TransportMode>();
			URL url = new URL(urlStr);
		    
		    JSONArray arrayModesTransport = null;
			InputStream is = url.openStream();
			
			bytes = IOUtils.toByteArray(is);
			text = new String(bytes, "UTF-8");
			if (text != null && text.trim().startsWith("[") && text.trim().endsWith("]")) {
				try {
					arrayModesTransport = new JSONArray(text);
					for (int i = 0; i < arrayModesTransport.length(); i++) {
						JSONObject transportMode = arrayModesTransport.getJSONObject(i);
						String code = transportMode.optString(CODE, null);
						String mode = transportMode.optString(MODE, null);
						String subMode = transportMode.optString(SUBMODE, null);
						String pivot = transportMode.optString(PIVOT, null);
						String pivotSub = transportMode.optString(PIVOT_SUBMODE, null);

						if (mode != null && subMode != null && pivot != null && pivotSub != null) {
							mapTransportToPivotMode.put(new TransportMode(mode, subMode), new TransportMode(pivot, pivotSub));
							mapPivotToTransportMode.put(new TransportMode(pivot, pivotSub), new TransportMode(mode, subMode));
						}
						
						if (code != null)
							mapCodeToTransportMode.put(new Integer(code), new TransportMode(mode, subMode));
							
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
		
		// Search specific mode matching with pivot transport mode
		for (Map.Entry<TransportMode, TransportMode> entry : mapPivotToTransportMode.entrySet())
		{
		    TransportMode entryT = entry.getKey();
		}
				
		// Search pivot mode matching with gtfs transport mode
		TransportMode tM = mapPivotToTransportMode.get(pivotMode);
		
		if (tM != null)
			return tM;
		
		// If not found set submode to unspecified and start again
		pivotMode.setSubMode("unspecified");
		tM = mapPivotToTransportMode.get(pivotMode);
		
		if (tM != null)
			return tM;	
		
		// If not found check on extended gtfs list
		ExtendedGtfsTransportModeConverter egtMC = ExtendedGtfsTransportModeConverter.getInstance();
		
		tM = egtMC.genericToSpecificMode(pivotMode);
		
		if (tM != null)
			return tM;
		
		// If there is no transport mode matching
		return null;

	}
	
	@Override
	public TransportMode specificToGenericMode(TransportMode specificMode) {
		return null;
	}

	public TransportMode fromCodeToPivotTransportMode(Integer code) {
		TransportMode specificTM = mapCodeToTransportMode.get(code);
		TransportMode pivotMode = mapTransportToPivotMode.get(specificTM);
		
		return pivotMode;
	}
	
	public Integer fromPivotTransportModeToCode(TransportMode transportPivotMode) {
		TransportMode tM = genericToSpecificMode(transportPivotMode);
		Integer code = null;
		
		if (tM != null) {
			BiMap<TransportMode, Integer> inverse = mapCodeToTransportMode.inverse();
			code = inverse.get(tM);
		
			if (code != null)
				return code;	
		}
		
		// If not found check on extended gtfs list
		ExtendedGtfsTransportModeConverter egtMC = ExtendedGtfsTransportModeConverter.getInstance();
		code = egtMC.fromPivotTransportModeToCode(transportPivotMode);
		
		if (code != null)
			return code;
		
		// If there is no transport mode matching
		return null;
	}
	
	public Integer fromSpecificTransportModeToCode(TransportMode tM) {
		Integer code = null;
		
		if (tM != null) {
			BiMap<TransportMode, Integer> inverse = mapCodeToTransportMode.inverse();
			code = inverse.get(tM);
		}
		
		return code;
	}
	
	public static class DefaultFactory extends TransportModeConverterFactory {

		@Override
		protected TransportModeConverter create() throws IOException {
			TransportModeConverter result = GtfsTransportModeConverter.getInstance();
			return result;
		}
	}

	static {
		TransportModeConverterFactory.factories.put("gtfs", new DefaultFactory());
	}
}
