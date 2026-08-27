package com.lc.offgrid.pojo.external;

/**
 * Where each download under resources/external/download came from, and where it is kept.
 */
public class ExternalUpdater
{
	private static final String	REST_STOP_PATH	= "external/download/bc_reststop.json";
	private static final String	REST_STOP_URL	= "https://openmaps.gov.bc.ca/geo/pub/WHSE_IMAGERY_AND_BASE_MAPS.MOT_REST_AREAS_SP/ows?service=WFS&version=2.0.0&request=GetFeature&typeName=pub:WHSE_IMAGERY_AND_BASE_MAPS.MOT_REST_AREAS_SP&outputFormat=application/json&srsName=EPSG:4326";

	private static final String	EXIT_PATH		= "external/download/bc_exits.json";
	private static final String	EXIT_URL		= "https://overpass-api.de/api/interpreter?data=%5Bout%3Ajson%5D%5Btimeout%3A180%5D%3B%0Anode%5B%22highway%22%3D%22motorway_junction%22%5D%2848.2%2C-139.1%2C60.1%2C-114.0%29%3B%0Aout%20body%3B%0A";

	private static final String	OFFRAMP_PATH	= "external/download/bc_offramp.json";
	private static final String	OFFRAMP_URL		= "https://openmaps.gov.bc.ca/geo/pub/WHSE_BASEMAPPING.DRA_DGTL_ROAD_ATLAS_MPAR_SP/ows?service=WFS&version=2.0.0&request=GetFeature&typeName=pub:WHSE_BASEMAPPING.DRA_DGTL_ROAD_ATLAS_MPAR_SP&outputFormat=application/json&srsName=EPSG:4326&CQL_FILTER=HIGHWAY_EXIT_NUMBER%20IS%20NOT%20NULL%20AND%20ROAD_NAME_FULL%20LIKE%20%27%25Offramp%25%27";

	private static final String	AMENITY_PATH	= "external/download/bc_exits_amenities.json";
	private static final String	AMENITY_URL		= "https://overpass-api.de/api/interpreter?data=%5Bout%3Ajson%5D%5Btimeout%3A600%5D%3B%0Anode%5B%22highway%22%3D%22motorway_junction%22%5D%2848.2%2C-139.1%2C60.1%2C-114.0%29-%3E.j%3B%0A%28%0Anwr%28around.j%3A1000%29%5B%22amenity%22~%22%5E%28fuel%7Crestaurant%7Cfast_food%7Ccafe%7Ctoilets%7Cdrinking_water%29%24%22%5D%3B%0Anwr%28around.j%3A1000%29%5B%22shop%22~%22%5E%28convenience%7Csupermarket%7Cdepartment_store%29%24%22%5D%3B%0Anwr%28around.j%3A1000%29%5B%22tourism%22~%22%5E%28hotel%7Cmotel%7Ccamp_site%29%24%22%5D%3B%0A%29%3B%0Aout%20center%20tags%3B%0A";

	public static String getRestStopPath()
	{
		return REST_STOP_PATH;
	}

	public static String getRestStopUrl()
	{
		return REST_STOP_URL;
	}

	public static String getExitPath()
	{
		return EXIT_PATH;
	}

	public static String getExitUrl()
	{
		return EXIT_URL;
	}

	public static String getOfframpPath()
	{
		return OFFRAMP_PATH;
	}

	public static String getOfframpUrl()
	{
		return OFFRAMP_URL;
	}

	public static String getAmenityPath()
	{
		return AMENITY_PATH;
	}

	public static String getAmenityUrl()
	{
		return AMENITY_URL;
	}
}
