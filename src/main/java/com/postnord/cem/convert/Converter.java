package com.postnord.cem.convert;

import java.util.Map;

import com.postnord.cem.util.Coord;
import com.postnord.cem.util.ServicePointUtil2;

public class Converter {

	public void setPostnummerGeoPos(Map<String, Object> map) {
		Object postcode = map.get("Till_Postnummer");
		if (postcode != null) {
			setPostalCode(map, (String) postcode);
		}
	}

	private void setPostalCode(Map<String, Object> map, String postcode) {
		Coord coord = ServicePointUtil2.getCoord("SWE", postcode);
		if (coord != null) {
			map.put("postnummer_easting", coord.getLon());
			map.put("postnummer_northing", coord.getLat());
			map.put("postnummer_geopoint", coord.toString());
			map.put("postnummer_geopos", true);
		}
	}

}
