package fr.lipn.sts.geo;

public class Location {
	String offset; //WordNet id
	String gnid; //geonames id
	double lat;
	double lon;
	
	public Location(String gnid, double lat, double lon){
		this.gnid=gnid;
		this.lat=lat;
		this.lon=lon;
	}
	
	public Location(String gnid, String lat, String lon){
		this.gnid=gnid;
		this.lat=Double.parseDouble(lat);
		this.lon=Double.parseDouble(lon);
	}
	
	public Location(String offset, String gnid, String lat, String lon){
		this.offset=offset;
		this.gnid=gnid;
		this.lat=Double.parseDouble(lat);
		this.lon=Double.parseDouble(lon);
	}
	/**
	 * calculate the geographic distance from another point (in Kms.)
	 * @param other
	 * @return
	 */
	public double getDistance(Location other) {
		return distance(this.lat, this.lon, other.lat, other.lon, 'K');
	}
	
	public double getLat(){
		return this.lat;
	}

	public double getLon(){
		return this.lon;
	}
	
	private static double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
      double theta = lon1 - lon2;
      double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
      dist = Math.acos(dist);
      dist = rad2deg(dist);
      dist = dist * 60 * 1.1515;
      if (unit == 'K') {
        dist = dist * 1.609344;
      } else if (unit == 'N') {
        dist = dist * 0.8684;
        }
      return (dist);
    }

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::  This function converts decimal degrees to radians             :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
	    return (deg * Math.PI / 180.0);
	}

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
      return (rad * 180.0 / Math.PI);
    }
    
    public int hashCode() {
        return gnid.hashCode();
    }
    
    public boolean equals(Object o) {
    	String oid=((Location)o).gnid;
    	return this.gnid.equals(oid);
    }
    
}
