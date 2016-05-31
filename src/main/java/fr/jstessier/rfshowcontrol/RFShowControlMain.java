package fr.jstessier.rfshowcontrol;

import fr.jstessier.rf24.exceptions.RF24Exception;
import fr.jstessier.rf24.hardware.RF24Hardware;
import fr.jstessier.rf24.hardware.RF24HardwarePi4j;
import fr.jstessier.rfshowcontrol.RFShowControlRF24Adapter.Mode;

public class RFShowControlMain {

	private static final byte[] RED = new byte[] {(byte) 255, (byte) 0, (byte) 0};
	private static final byte[] GREEN = new byte[] {(byte) 0, (byte) 255, (byte) 0};
	private static final byte[] BLUE = new byte[] {(byte) 0, (byte) 0, (byte) 255};
	private static final byte[] WHITE = new byte[] {(byte) 255, (byte) 255, (byte) 255};

	private static final byte[][] LED1 = new byte[][] { WHITE, RED, GREEN, BLUE };
	private static final byte[][] LED2 = new byte[][] { RED, GREEN, BLUE, WHITE };

	public static void main(String[] args) throws RFShowControlException {

		RF24Hardware rf24Hardware;
		try {
			rf24Hardware = new RF24HardwarePi4j((byte) 0, (byte) 3);
		} catch (RF24Exception e) {
			throw new RFShowControlException("An error occured during RF24 Hardware instanciation", e);
		}

		final RFShowControlController controller = new RFShowControlControllerImpl(rf24Hardware, 6);
		controller.start(
				(byte) 76, 
				new byte[] { (byte) 0xD2, (byte) 0xF2, (byte) 0xF2, (byte) 0xF2, (byte) 0xF2 },
				Mode.TX);
		
		int index = 0;
		while (true) {
			byte[] led1 = LED1[index];
			byte[] led2 = LED2[index];
			controller.updateChannelValues(led1, 1);
			controller.updateChannelValues(led2, 4);
			try {
				controller.sendChannelValues();
			} catch (RFShowControlException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			index++;
			index = index % 4;
		}

	}

}
