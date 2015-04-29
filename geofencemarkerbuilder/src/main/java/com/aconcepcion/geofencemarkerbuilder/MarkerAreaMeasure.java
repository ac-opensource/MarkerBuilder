package com.aconcepcion.geofencemarkerbuilder;

/**
 * MapAreaMeasure
 *
 * @author A-Ar Andrew Concepcion
 */
public class MarkerAreaMeasure {
    
	public static enum Unit {pixels, meters}
	
	public double value;
	public Unit unit;
	
	public MarkerAreaMeasure(double value, Unit unit) {
		this.value = value;
		this.unit = unit;
	}
}