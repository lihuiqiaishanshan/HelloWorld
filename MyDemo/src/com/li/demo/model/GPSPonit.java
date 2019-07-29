package com.li.demo.model;

public class GPSPonit {

	private double mLat; // 纬度

	private double mLon; // 经度

	public double getmLat() {

		return mLat;

	}

	public void setmLat(double mLat) {

		this.mLat = mLat;

	}

	public double getmLon() {

		return mLon;

	}

	public void setmLon(double mLon) {

		this.mLon = mLon;

	}

	public GPSPonit(double mLat, double mLon) {

		this.mLat = mLat;

		this.mLon = mLon;

	}

	public GPSPonit() {

	}

	@Override
	public String toString() {

		return "Ponit [mLat=" + mLat + ", mLon=" + mLon + "]";

	}

}
