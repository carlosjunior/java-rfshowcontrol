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

import fr.jstessier.rf24.hardware.RF24Hardware;
import fr.jstessier.rfshowcontrol.RFShowControlRF24Adapter.Mode;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class RFShowControlControllerImpl implements RFShowControlController {

    private static final int SEND_CHANNEL_VALUES_PERIOD_MS = 500;

	private final RFShowControlRF24Adapter rf24;

	private final byte[] channelTempValues;

	private byte[] channelActiveValues;

    private final Object synchroTempValues = new Object();

    private final Object synchroSendValues = new Object();

    private final Timer timer = new Timer(true);

	/**
	 * Constructor.
	 *
	 * @param rf24Hardware
	 * @throws RFShowControlException
	 */
	public RFShowControlControllerImpl(final RF24Hardware rf24Hardware) throws RFShowControlException {
		this(rf24Hardware, RFShowControlRF24Adapter.MAX_MUNBER_OF_CHANNEL);
	}

	/**
	 * Constructor.
	 *
	 * @param rf24Hardware
	 * @param numberOfChannel
	 * @throws RFShowControlException
	 */
	public RFShowControlControllerImpl(final RF24Hardware rf24Hardware, final int numberOfChannel) throws RFShowControlException {
		rf24 = new RFShowControlRF24Adapter(rf24Hardware, numberOfChannel);
		channelTempValues = new byte[numberOfChannel];
		channelActiveValues = new byte[numberOfChannel];
	}

	@Override
	public RFShowControlController start(final byte rfChannel, final byte[] pipeAddress,
                                         final Mode mode) throws RFShowControlException {
		rf24.configure(rfChannel, pipeAddress, mode);
        synchronized (synchroTempValues) {
            Arrays.fill(channelTempValues, (byte) 0);
            flushChannelValues();
        }
        // Send channelValue every 500ms
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendChannelValues();
                } catch (RFShowControlException e) {
                    e.printStackTrace();
                }
            }
        }, 0, SEND_CHANNEL_VALUES_PERIOD_MS);
		return this;
	}


    @Override
    public RFShowControlController resetChannelValues() throws RFShowControlException {
        synchronized (synchroTempValues) {
            Arrays.fill(channelTempValues, (byte) 0);
        }
        return this;
    }

	@Override
	public RFShowControlController resetAndFlushChannelValues() throws RFShowControlException {
        synchronized (synchroTempValues) {
            Arrays.fill(channelTempValues, (byte) 0);
            flushChannelValues();
        }
		return this;
	}

	@Override
	public byte[] getChannelValues() {
        synchronized (synchroTempValues) {
            return Arrays.copyOf(channelTempValues, channelTempValues.length);
        }
	}

	private RFShowControlController sendChannelValues() throws RFShowControlException {
        synchronized (synchroSendValues) {
            rf24.sendChannelValues(channelActiveValues);
        }
		return this;
	}

    @Override
    public RFShowControlController flushChannelValues() throws RFShowControlException {
        synchronized (synchroTempValues) {
            channelActiveValues = Arrays.copyOf(channelTempValues, channelTempValues.length);
        }
        sendChannelValues();
        return this;
    }

	@Override
	public RFShowControlController updateChannelValue(byte newChannelValue, int channelNumber) {
        synchronized (synchroTempValues) {
            if (channelNumber < 1 || channelNumber > channelTempValues.length) {
                throw new IllegalArgumentException("channelNumber must be in range [1-" + channelTempValues.length + "]");
            }
            channelTempValues[channelNumber - 1] = newChannelValue;
        }
		return this;
	}

	@Override
	public RFShowControlController updateChannelValues(byte[] newChannelValues, int startChannelNumber) {
        synchronized (synchroTempValues) {
            if (newChannelValues == null || newChannelValues.length == 0) {
                return this;
            }
            else if (startChannelNumber < 1 || startChannelNumber > channelTempValues.length) {
                throw new IllegalArgumentException("startChannelNumber must be in range [1-" + channelTempValues.length + "]");
            }
            else if ((startChannelNumber - 1) > (channelTempValues.length - newChannelValues.length)) {
                throw new IllegalArgumentException("startChannelNumber + newChannelValues.length must not exceed " + channelTempValues.length);
            }
            System.arraycopy(newChannelValues, 0, channelTempValues, startChannelNumber - 1, newChannelValues.length);
        }
		return this;
	}

	@Override
	public RFShowControlController updateChannelValues(byte[] newChannelValues) {
        synchronized (synchroTempValues) {
            if (newChannelValues.length != channelTempValues.length) {
                throw new IllegalArgumentException("newChannelValues length must be equal to " + channelTempValues.length);
            }
            System.arraycopy(newChannelValues, 0, channelTempValues, 0, newChannelValues.length);
        }
		return this;
	}

	@Override
	public RFShowControlController updateAndFlushChannelValues(byte[] newChannelValues) throws RFShowControlException {
        synchronized (synchroTempValues) {
            updateChannelValues(newChannelValues);
            flushChannelValues();
        }
		return this;
	}

}
