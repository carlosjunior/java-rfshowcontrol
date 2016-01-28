package fr.jstessier.rfshowcontrol;

import org.junit.Assert;
import org.junit.Test;

import fr.jstessier.rf24.exceptions.RF24Exception;
import fr.jstessier.rf24.hardware.RF24Hardware;

public class RFShowControlRF24AdapterTest {

	@Test
	public void splitDatasInRFShowControlPackets_20() throws RFShowControlException {

		final RFShowControlRF24Adapter adapter = new RFShowControlRF24Adapter(new MockHardware(), 
				RFShowControlRF24Adapter.MAX_MUNBER_OF_CHANNEL);

		byte[] datas = new byte[20];
		for (int index = 0; index < datas.length; index++) {
			datas[index] = (byte) (index % 255);
		}

		byte[][] splittedDatas = adapter.splitDatasInRFShowControlPackets(datas);

		Assert.assertEquals(1, splittedDatas.length);
		Assert.assertEquals(32, splittedDatas[0].length);
		for (int i = 0; i < 32; i++) {
			if (i < 20) {
				Assert.assertEquals((byte) (i % 255), splittedDatas[0][i]);
			}
			else if (i < 30) {
				Assert.assertEquals((byte) 0, splittedDatas[0][i]);
			}
			else if (i == 30) {
				Assert.assertEquals((byte) 0, splittedDatas[0][i]);
			}
			else if (i == 31) {
				Assert.assertEquals((byte) 0, splittedDatas[0][i]);
			}
		}
	}
	
	@Test
	public void splitDatasInRFShowControlPackets_35() throws RFShowControlException {

		RFShowControlRF24Adapter adapter = new RFShowControlRF24Adapter(new MockHardware(), 
				RFShowControlRF24Adapter.MAX_MUNBER_OF_CHANNEL);

		byte[] datas = new byte[35];
		for (int index = 0; index < datas.length; index++) {
			datas[index] = (byte) (index % 255);
		}

		byte[][] splittedDatas = adapter.splitDatasInRFShowControlPackets(datas);
		
		Assert.assertEquals(2, splittedDatas.length);
		Assert.assertEquals(32, splittedDatas[0].length);
		Assert.assertEquals(32, splittedDatas[1].length);
		for (int i = 0; i < 32; i++) {
			if (i < 30) {
				Assert.assertEquals((byte) (i % 255), splittedDatas[0][i]);
			}
			else if (i == 30) {
				Assert.assertEquals((byte) 0, splittedDatas[0][i]);
			}
			else if (i == 31) {
				Assert.assertEquals((byte) 0, splittedDatas[0][i]);
			}
		}
		for (int i = 0; i < 32; i++) {
			if (i < 5) {
				Assert.assertEquals((byte) (i % 255 + 30), splittedDatas[1][i]);
			}
			else if (i < 30) {
				Assert.assertEquals((byte) 0, splittedDatas[1][i]);
			}
			else if (i == 30) {
				Assert.assertEquals((byte) 1, splittedDatas[1][i]);
			}
			else if (i == 31) {
				Assert.assertEquals((byte) 0, splittedDatas[1][i]);
			}
		}
	}

	@Test
	public void splitDatasInRFShowControlPackets_60() throws RFShowControlException {

		RFShowControlRF24Adapter adapter = new RFShowControlRF24Adapter(new MockHardware(), 
				RFShowControlRF24Adapter.MAX_MUNBER_OF_CHANNEL);

		byte[] datas = new byte[60];
		for (int index = 0; index < datas.length; index++) {
			datas[index] = (byte) (index % 255);
		}
		
		byte[][] splittedDatas = adapter.splitDatasInRFShowControlPackets(datas);
		
		Assert.assertEquals(2, splittedDatas.length);
		Assert.assertEquals(32, splittedDatas[0].length);
		Assert.assertEquals(32, splittedDatas[1].length);
		for (int i = 0; i < 32; i++) {
			if (i < 30) {
				Assert.assertEquals((byte) (i % 255), splittedDatas[0][i]);
			}
			else if (i == 30) {
				Assert.assertEquals((byte) 0, splittedDatas[0][i]);
			}
			else if (i == 31) {
				Assert.assertEquals((byte) 0, splittedDatas[0][i]);
			}
		}
		for (int i = 0; i < 32; i++) {
			if (i < 30) {
				Assert.assertEquals((byte) (i % 255 + 30), splittedDatas[1][i]);
			}
			else if (i == 30) {
				Assert.assertEquals((byte) 1, splittedDatas[1][i]);
			}
			else if (i == 31) {
				Assert.assertEquals((byte) 0, splittedDatas[1][i]);
			}
		}
	}

	public static class MockHardware implements RF24Hardware {

		@Override
		public void setPinChipEnableHigh() {
			// NOP
		}

		@Override
		public void setPinChipEnableLow() {
			// NOP
		}

		@Override
		public byte[] spiWrite(byte... data) throws RF24Exception {
			return null;
		}
		
	}
	
}
