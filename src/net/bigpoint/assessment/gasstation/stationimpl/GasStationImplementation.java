package net.bigpoint.assessment.gasstation.stationimpl;
/**
 * @author UdaySharma
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

public class GasStationImplementation implements GasStation {

	private double credit;
	private int sales;
	private int cancel_if_no_gas;
	private int cancel_if_expensive_gas;

	private HashMap<GasType, PriorityQueue<GasPump>> pumps;
	private HashMap<GasType, Double> cost;
	private AtomicInteger expensive_gas;
	private AtomicInteger no_gas;
	private AtomicInteger sales_num;
	private AtomicLong revnue;

	public GasStationImplementation(HashMap<GasType, Double> prices) {
		this.credit = 0.0;
		this.sales = 0;
		this.cancel_if_no_gas = 0;
		this.cancel_if_expensive_gas = 0;
		this.cost = prices;
		this.pumps = new HashMap<GasType, PriorityQueue<GasPump>>();

		for (GasType _gastype : GasType.values()) {
			pumps.put(_gastype, new PriorityQueue<GasPump>(1, new GasPumpCompare()));

			// To have a price for every GasType that this GasStation offers.
			// Another implementation could be that buyGas and getPrice throws a
			// GasNotOfferedException for a GasType that has no price instead.
			if (!this.cost.containsKey(_gastype))
				this.cost.put(_gastype, 1.0);
		}
	}

	@Override
	public synchronized void addGasPump(GasPump _gasPump) {
		pumps.get(_gasPump.getGasType()).add(_gasPump);
	}

	/**
	 * Collection of GasPump on Gas station
	 */
	@Override
	public synchronized Collection<GasPump> getGasPumps() {
	
		ArrayList<GasPump> cosumerqueue = new ArrayList<GasPump>();
		for (GasType _gastype : GasType.values()) {
			PriorityQueue<GasPump> queue = pumps.get(_gastype);
			for (GasPump _gaspump : queue) {
				cosumerqueue.add(new GasPump(_gaspump.getGasType(), _gaspump
						.getRemainingAmount()));
			}
		}
		return cosumerqueue;
	}
	@Override
	public synchronized double buyGas(GasType _gastype, double amountInLiters,
			double maxPricePerLiter) throws NotEnoughGasException,
			GasTooExpensiveException {
		expensive_gas = new AtomicInteger(0);
		sales_num = new AtomicInteger(0);
		revnue = new AtomicLong(0);
		no_gas = new AtomicInteger(0);
		double price = 0;

		/*
		 * check for gas availability in pump
		 * should be greater than 0
		 */
		if (amountInLiters <= 0 || maxPricePerLiter <= 0) {
			return 0;
		}
		
		/*
		 * If cost of the gas is maximum than the ongoing price 
		 * then cancel gas feeling due to the expensive
		 */
		if (cost.get(_gastype) > maxPricePerLiter) {
			cancel_if_expensive_gas = expensive_gas.addAndGet(1);
			throw new GasTooExpensiveException();
		}
		PriorityQueue<GasPump> pumptype = pumps.get(_gastype);

		/*
		 * calculate amount of gas remaining in the pump and amount price
		 * custore has to pay
		 */
		for (GasPump _gaspump : pumptype) {

			if (_gaspump.getGasType().equals(_gastype)) {

				synchronized (_gaspump) {

					if (_gaspump.getRemainingAmount() >= amountInLiters) {
						_gaspump.pumpGas(amountInLiters);
						price = amountInLiters * cost.get(_gastype);
					/*	System.out
								.println("[PUMP STATISTICS] amount remaining: "
										+ _gaspump.getRemainingAmount());*/
						credit = revnue
								.addAndGet(new Double(price).longValue());
						sales = sales_num.addAndGet(1);
						break;
					}
				}
			}
		}

		/*
		 * if price reach to 0 and amount of gas greater than the 0
		 * then cancel gas feeling due to the not enough gas
		 */
		if (price == 0 && amountInLiters > 0) {
			cancel_if_no_gas = no_gas.addAndGet(1);
			throw new NotEnoughGasException();
		}

		return price;
		
	}
	@Override
	public synchronized double getRevenue() {
		return credit;
	}
	@Override
	public synchronized int getNumberOfSales() {
		return sales;
	}
	@Override
	public synchronized int getNumberOfCancellationsNoGas() {
		return cancel_if_no_gas;
	}
	@Override
	public synchronized int getNumberOfCancellationsTooExpensive() {
		return cancel_if_expensive_gas;
	}
	@Override
	public synchronized double getPrice(GasType _gastype) {
		return cost.get(_gastype);
	}
	@Override
	public synchronized void setPrice(GasType _gastype, double price) {
		if (price > 0)
			cost.put(_gastype, price);
	}

}