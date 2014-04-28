package com.ahaines.machinelearning.api.dataset.wine;

public class WineAnalytes {

	private final double concentration4EP;
	private final double concentration4EG;
	
	public WineAnalytes(double concentration4EP, double concentration4EG){
		this.concentration4EP = concentration4EP;
		this.concentration4EG = concentration4EG;
	}

	public double getConcentration4EP() {
		return concentration4EP;
	}

	public double getConcentration4EG() {
		return concentration4EG;
	}
}
