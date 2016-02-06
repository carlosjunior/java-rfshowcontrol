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

import java.util.Arrays;

import fr.jstessier.rf24.hardware.RF24Hardware;
import fr.jstessier.rfshowcontrol.RFShowControlRF24Adapter.Mode;

public class RFShowControlController {

	private final RFShowControlRF24Adapter rf24;

	private final byte[] channelValues;

	/**
	 * Constructor.
	 * 
	 * @param rf24Hardware
	 * @throws RFShowControlException
	 */
	public RFShowControlController(final RF24Hardware rf24Hardware) throws RFShowControlException {
		this(rf24Hardware, RFShowControlRF24Adapter.MAX_MUNBER_OF_CHANNEL);
	}

	/**
	 * Constructor.
	 * 
	 * @param rf24Hardware
	 * @param numberOfChannel
	 * @throws RFShowControlException
	 */
	public RFShowControlController(final RF24Hardware rf24Hardware, final int numberOfChannel) throws RFShowControlException {
		rf24 = new RFShowControlRF24Adapter(rf24Hardware, numberOfChannel);
		channelValues = new byte[numberOfChannel];
	}

	/**
	 * 
	 * @param rfChannel
	 * @param pipeAddress
	 * @param mode
	 * @return	The current RFShowControlController instance.
	 * @throws RFShowControlException	In case of communication error with RF Module.
	 */
	public RFShowControlController start(final byte rfChannel, final byte[] pipeAddress, 
			final Mode mode) throws RFShowControlException {
		rf24.configure(rfChannel, pipeAddress, mode);
		rf24.sendChannelValues(channelValues);
		return this;
	}

	/**
	 * Reset all channels to 0.
	 * 
	 * @return	The current RFShowControlController instance.
	 * @throws RFShowControlException	In case of communication error with RF Module.
	 */
	public RFShowControlController resetChannelValues() throws RFShowControlException {
		Arrays.fill(channelValues, (byte) 0);
		rf24.sendChannelValues(channelValues);
		return this;
	}

	/**
	 * Return the channel values.
	 * @return	The channel values.
	 */
	public byte[] getChannelValues() {
		return Arrays.copyOf(channelValues, channelValues.length);
	}

	/**
	 * 
	 * 
	 * @return	The current RFShowControlController instance.
	 * @throws RFShowControlException
	 */
	public RFShowControlController sendChannelValues() throws RFShowControlException {
		rf24.sendChannelValues(channelValues);
		return this;
	}

	/**
	 * 
	 * 
	 * @param newChannelValue
	 * @param channelNumber
	 * @return	The current RFShowControlController instance.
	 */
	public RFShowControlController updateChannelValue(byte newChannelValue, int channelNumber) {
		if (channelNumber < 1 || channelNumber > channelValues.length) {
			throw new IllegalArgumentException("channelNumber must be in range [1-" + channelValues.length + "]");
		}
		channelValues[channelNumber - 1] = newChannelValue;
		return this;
	}

	/**
	 * 
	 * 
	 * @param newChannelValues
	 * @param startChannelNumber
	 * @return	The current RFShowControlController instance.
	 */
	public RFShowControlController updateChannelValues(byte[] newChannelValues, int startChannelNumber) {
		if (newChannelValues == null || newChannelValues.length == 0) {
			return this;
		}
		else if (startChannelNumber < 1 || startChannelNumber > channelValues.length) {
			throw new IllegalArgumentException("startChannelNumber must be in range [1-" + channelValues.length + "]");
		}
		else if ((startChannelNumber - 1) > (channelValues.length - newChannelValues.length)) {
			throw new IllegalArgumentException("startChannelNumber + newChannelValues.length must not exceed " + channelValues.length);
		}
		System.arraycopy(newChannelValues, 0, channelValues, startChannelNumber - 1, newChannelValues.length);
		return this;
	}

	/**
	 * 
	 * 
	 * @param newChannelValues
	 * @return	The current RFShowControlController instance.
	 */
	public RFShowControlController updateChannelValues(byte[] newChannelValues) {
		if (newChannelValues.length != channelValues.length) {
			throw new IllegalArgumentException("newChannelValues length must be equal to " + channelValues.length);
		}
		System.arraycopy(newChannelValues, 0, channelValues, 0, channelValues.length);
		return this;
	}

	/**
	 * 
	 * 
	 * @param newChannelValues
	 * @return	The current RFShowControlController instance.
	 * @throws RFShowControlException
	 */
	public RFShowControlController updateAndSendChannelValues(byte[] newChannelValues) throws RFShowControlException {
		updateChannelValues(newChannelValues);
		sendChannelValues();
		return this;
	}

}
