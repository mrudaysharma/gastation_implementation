package net.bigpoint.assessment.gasstation.stationimpl;

import java.util.Comparator;

import net.bigpoint.assessment.gasstation.GasPump;

public class GasPumpCompare implements Comparator<GasPump> {

	public int compare(GasPump a, GasPump b) {
		return (int)(a.getRemainingAmount() - b.getRemainingAmount());
	}

}