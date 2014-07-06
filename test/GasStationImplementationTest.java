import static org.junit.Assert.*;

import org.junit.Test;





import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;




import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import net.bigpoint.assessment.gasstation.stationimpl.GasStationImplementation;
/**
 * 
 * @author UdaySharma
 *
 */

public class GasStationImplementationTest {

	private GasStationImplementation _gasStation;
	private GasPump _gasPump;
	private GasPump _pump_amout_low;
	private HashMap<GasType,Double> costs;
	private static final double DIESEL_PRICE = 1.4;

	@Before
	public void setUp() throws Exception {
		costs = new HashMap<GasType,Double>();
		costs.put(GasType.DIESEL, DIESEL_PRICE);
		_gasStation = new GasStationImplementation(costs);
		_gasPump = new GasPump(GasType.DIESEL, 50);
		_pump_amout_low = new GasPump(GasType.SUPER, 30);
		_gasStation.addGasPump(_gasPump);
	}

	@Test
	public void testAddGasPump() {
		assertTrue(_gasStation.getGasPumps().size() == 1);
		_gasStation.addGasPump(new GasPump(GasType.REGULAR, 1));
		assertTrue(_gasStation.getGasPumps().size() == 2);
	}

	@Test
	public void testAddGasPumpToReturnedCollection() {
		Collection<GasPump> pumps = _gasStation.getGasPumps();
		pumps.add(_pump_amout_low);
		assertFalse(_gasStation.getGasPumps().contains(_pump_amout_low));
	}

	@Test
	public void testBuyGas() throws Exception {
		double amount = 10;
		assertEquals(amount*DIESEL_PRICE, _gasStation.buyGas(GasType.DIESEL, amount, 1.5), 0);
	}

	@Test(expected=GasTooExpensiveException.class)
	public void testBuyGasMaxPriceTooLow() throws NotEnoughGasException, GasTooExpensiveException {
		_gasStation.buyGas(GasType.DIESEL, 10, 1.3);
	}

	@Test(expected=NotEnoughGasException.class)
	public void testBuyGasNotEnoughGas() throws NotEnoughGasException, GasTooExpensiveException {
		_gasStation.buyGas(GasType.DIESEL, 60, 1.5);
	}

	@Test
	public void testBuyGasInvalidArguments() throws Exception {
		assertEquals(0, _gasStation.buyGas(GasType.DIESEL, -1.0, 1.5), 0);
		assertEquals(0, _gasStation.buyGas(GasType.DIESEL, 10, -1.0), 0);
		assertEquals(0, _gasStation.buyGas(GasType.DIESEL, -10, -1.0), 0);
	}

	@Test
	public void testGetRevenue() throws Exception {
		assertEquals(0, _gasStation.getRevenue(), 0);
		_gasStation.buyGas(GasType.DIESEL, -10, 1.5);
		assertEquals(0, _gasStation.getRevenue(), 0);
		_gasStation.buyGas(GasType.DIESEL, 10, -1.5);
		assertEquals(0, _gasStation.getRevenue(), 0);
		_gasStation.buyGas(GasType.DIESEL, 10, 1.5);
		assertEquals(10*costs.get(GasType.DIESEL), _gasStation.getRevenue(), 0);
	}

	@Test
	public void testGetNumberOfSales() throws Exception {
		assertEquals(0, _gasStation.getNumberOfSales());
		_gasStation.buyGas(GasType.DIESEL, -10, 1.5);
		assertEquals(0, _gasStation.getNumberOfSales());
		_gasStation.buyGas(GasType.DIESEL, 10, -1.5);
		assertEquals(0, _gasStation.getNumberOfSales());
		try {
			_gasStation.buyGas(GasType.DIESEL, 60, 1.5);
		} catch (NotEnoughGasException e) {
			//Exception expected.			
		}
		assertEquals(0, _gasStation.getNumberOfSales());
		try {
			_gasStation.buyGas(GasType.DIESEL, 10, 1.3);
		} catch (GasTooExpensiveException e) {
			//Exception expected.
		}
		assertEquals(0, _gasStation.getNumberOfSales());
		_gasStation.buyGas(GasType.DIESEL, 10, 1.5);
		assertEquals(1, _gasStation.getNumberOfSales());
	}

	@Test
	public void testGetNumberOfCancellationsNoGas() throws Exception {
		assertEquals(0, _gasStation.getNumberOfCancellationsNoGas());
		_gasStation.buyGas(GasType.DIESEL, -10, 1.5);
		assertEquals(0, _gasStation.getNumberOfCancellationsNoGas());
		_gasStation.buyGas(GasType.DIESEL, 10, -1.5);
		assertEquals(0, _gasStation.getNumberOfCancellationsNoGas());
		_gasStation.buyGas(GasType.DIESEL, 10, 1.5);
		assertEquals(0, _gasStation.getNumberOfCancellationsNoGas());
		try {
			_gasStation.buyGas(GasType.DIESEL, 10, 1.3);
		} catch (GasTooExpensiveException e) {
			//Exception expected.
		}
		assertEquals(0, _gasStation.getNumberOfCancellationsNoGas());
		try {
			_gasStation.buyGas(GasType.DIESEL, 60, 1.5);
		} catch (NotEnoughGasException e) {
			//Exception expected.
		}
		assertEquals(1, _gasStation.getNumberOfCancellationsNoGas());
	}

	@Test
	public void testGetNumberOfCancellationsTooExpensive() throws Exception {
		assertEquals(0, _gasStation.getNumberOfCancellationsTooExpensive());
		_gasStation.buyGas(GasType.DIESEL, -10, 1.5);
		assertEquals(0, _gasStation.getNumberOfCancellationsTooExpensive());
		_gasStation.buyGas(GasType.DIESEL, 10, -1.5);
		assertEquals(0, _gasStation.getNumberOfCancellationsTooExpensive());
		_gasStation.buyGas(GasType.DIESEL, 10, 1.5);
		assertEquals(0, _gasStation.getNumberOfCancellationsTooExpensive());
		try {
			_gasStation.buyGas(GasType.DIESEL, 60, 1.5);
		} catch (NotEnoughGasException e) {
			//Exception expected.
		}
		assertEquals(0, _gasStation.getNumberOfCancellationsTooExpensive());
		try {
			_gasStation.buyGas(GasType.DIESEL, 10, 1.3);
		} catch (GasTooExpensiveException e) {
			//Exception expected.
		}
		assertEquals(1, _gasStation.getNumberOfCancellationsTooExpensive());
	}

	@Test
	public void testGetSetPrice() {
		assertEquals(1.4, _gasStation.getPrice(GasType.DIESEL), 0);
		assertEquals(1.0, _gasStation.getPrice(GasType.REGULAR), 0);
		_gasStation.setPrice(GasType.REGULAR, -1.5);
		assertEquals(1.0, _gasStation.getPrice(GasType.REGULAR), 0);
		_gasStation.setPrice(GasType.REGULAR, 1.5);
		assertEquals(1.5, _gasStation.getPrice(GasType.REGULAR), 0);
	}
}