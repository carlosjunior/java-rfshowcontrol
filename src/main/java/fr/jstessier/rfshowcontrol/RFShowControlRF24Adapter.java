package fr.jstessier.rfshowcontrol;

/*
 * Copyright (C) 2015 J.S. TESSIER
 * 
 * This file is part of rfshowcontrol.
 * 
 * java-rf24 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * rfshowcontrol is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with rfshowcontrol. If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */

import fr.jstessier.rf24.RF24;
import fr.jstessier.rf24.Registers;
import fr.jstessier.rf24.enums.DataPipe;
import fr.jstessier.rf24.enums.DataRates;
import fr.jstessier.rf24.enums.OutputPower;
import fr.jstessier.rf24.enums.WritePayloadType;
import fr.jstessier.rf24.exceptions.RF24Exception;
import fr.jstessier.rf24.hardware.RF24Hardware;

/**
 * Adapter for driving RFShowControl protocol with a RF24 module. 
 */
public class RFShowControlRF24Adapter {

	/** Max number of RFShowControl channel. */
	public static final int MAX_MUNBER_OF_CHANNEL = 512;

	/** Number of RFShowControl channel per RF24 packet. */
	private static final int NUMBER_OF_CHANNEL_PER_PACKET = 30;

	/** RF24 packet size. */
	private static final int PACKET_SIZE = 32;

	/** The RF24 module driver. */
	private final RF24 rf24;

	/** Number of channel [1-512]. */
	private final int numberOfChannel;

	/** TX or RX mode. */
	private Mode mode;

	/** The adapter is configured ? */
	private boolean configured;

	/**
	 * Constructor.
	 * 
	 * @param rf24Hardware		Interface with hardware for communication with RF module (SPI and GPIO).
	 * @param numberOfChannel	Number of active channel [1-512].
	 * @throws RFShowControlException	In case of communication error with RF Module.
	 */
	public RFShowControlRF24Adapter(final RF24Hardware rf24Hardware, final int numberOfChannel) throws RFShowControlException {
		if (numberOfChannel < 1 || numberOfChannel > RFShowControlRF24Adapter.MAX_MUNBER_OF_CHANNEL) {
			throw new IllegalArgumentException("numberOfChannel must be in range [1-" 
					+ RFShowControlRF24Adapter.MAX_MUNBER_OF_CHANNEL + "]");
		}

		this.numberOfChannel = numberOfChannel;
		try {
			rf24 = new RF24(rf24Hardware);
		} catch (RF24Exception e) {
			throw new RFShowControlException("An error occured during RF24 instanciation", e);
		}
	}

	/**
	 * Initialize the module RF24 with configuration and place it in standby mode 1.
	 * 
	 * @param rfChannel		The radio frequency channel (7 bit -> [0 - 127]).
	 * @param pipeAddress	The address of the pipe.
	 * @param mode			Transmitter or receiver mode.
	 * @return	The current RFShowControlRF24Controller instance.
	 * @throws RF24Exception	In case of communication error with RF Module.
	 */
	public RFShowControlRF24Adapter configure(final byte rfChannel, final byte[] pipeAddress, 
			final Mode mode) throws RFShowControlException {

		if (mode == null) {
			throw new IllegalArgumentException("mode is mandatory");
		}

		try {
			// Power down the module
			rf24.powerDown();
	
			// Reset CONFIG to default value.
			rf24.writeRegisterValue(Registers.CONFIG, Registers.CONFIG.getResetValue());
	
			// Enable 16-bits CRC
			rf24.enableCRC2bytes();
	
			// Set frequency channel
			rf24.setFrequencyChannel(rfChannel);
	
			// Set data rates to a slowest and most reliable speed
			rf24.setDataRatesAndOutputPower(DataRates.DR_250_KBPS, OutputPower.RF_0_DBM);
	
			// Enables the W_TX_PAYLOAD_NOACK command
			rf24.enableWritePayloadNoAckCommand();
	
			// Reset current status and flush buffers
			// Notice reset and flush is the last thing we do
			rf24.resetAllInterrupts();
			rf24.flushRx();
			rf24.flushTx();
	
			// Power up the module
			rf24.powerUp();
	
			this.mode = mode;
			if (Mode.TX.equals(mode)) {
				rf24.openWritingPipe(pipeAddress);
			}
			else {
				rf24.openReadingPipe(DataPipe.P1, pipeAddress);
			}
	
			configured = true;
		} catch (RF24Exception e) {
			throw new RFShowControlException("An error occured during RF24 configuration", e);
		}

		return this;
	}

	/**
	 * Return the number of active channel.
	 * @return	The number of active channel.
	 */
	public int getNumberOfChannel() {
		return numberOfChannel;
	}

	/**
	 * Return the mode.
	 * @return	The mode.
	 */
	public Mode getMode() {
		return mode;
	}

	/**
	 * Send datas.
	 * 
	 * @param datas		Datas to send (max length = 512 bytes)
	 * @throws RFShowControlException
	 */
	public RFShowControlRF24Adapter sendChannelValues(final byte[] datas) throws RFShowControlException {
		if (!configured) {
			throw new IllegalStateException("RFShowControlRF24Adapter is not configured");
		}
		else if (datas == null || datas.length == 0) {
			return this;
		}
		else if (datas.length > numberOfChannel) {
			throw new IllegalArgumentException("datas length must not exceed " + numberOfChannel);
		}

		final byte[][] packets = splitDatasInRFShowControlPackets(datas);
		for (int index = 0; index < packets.length; index++) {
			try {
				rf24.sendPayload(WritePayloadType.W_TX_PAYLOAD_NO_ACK, packets[index]);
			} catch (RF24Exception e) {
				throw new RFShowControlException("An error occured during RF24 communication", e);
			}
		}
		return this;
	}

	/**
	 * Split datas in RFShowControl packets.
	 * 
	 * @param datas	Datas to split in RFShowControl packets
	 * @return	The RFShowControl packets.
	 */
	protected byte[][] splitDatasInRFShowControlPackets(final byte[] datas) {
		// compute number of packet to send
		int numberOfPacket = datas.length / NUMBER_OF_CHANNEL_PER_PACKET;
		if (datas.length % NUMBER_OF_CHANNEL_PER_PACKET > 0) {
			numberOfPacket++;
		}
		// split datas in RFShowControl packets
		final byte[][] splitedDatas = new byte[numberOfPacket][];
		for (byte index = 0; index < numberOfPacket; index++) {
			splitedDatas[index] = createRFShowControlPacket(datas, 
					index * NUMBER_OF_CHANNEL_PER_PACKET, 
					(index + 1) * NUMBER_OF_CHANNEL_PER_PACKET, 
					index);
			
		}
		return splitedDatas;
	}

	/**
	 * create a RFShowControl packet from datas.
	 * 
	 * @param datas			Datas to split in RFShowControl packet.
	 * @param from			Begin index of data to copy in packet.
	 * @param to			End index of data to copy in packet.
	 * @param packetIndex	The packet index / offset.
	 * @return	The RFShowControl packet.
	 */
	protected byte[] createRFShowControlPacket(final byte[] datas, final int from, final int to, final byte packetIndex) {
        // Copy datas into packet payload
		final byte[] packetPayload = new byte[PACKET_SIZE];
        final int length = Math.min(datas.length - from, PACKET_SIZE);
        System.arraycopy(datas, from, packetPayload, 0, length);
        for (int index = length; index < PACKET_SIZE - 2; index++) {
        	packetPayload[index] = (byte) 0;
        }
        // Add packet OFFSET
        packetPayload[PACKET_SIZE - 2] = packetIndex;
        // Add packet TBD
        packetPayload[PACKET_SIZE - 1] = 0;
        return packetPayload;
    }

	/**
	 * Controller mode (TX or RX).
	 */
	public enum Mode {

		/** Transmitter. */
		TX, 

		/** Receiver. */
		RX;

	}

}
