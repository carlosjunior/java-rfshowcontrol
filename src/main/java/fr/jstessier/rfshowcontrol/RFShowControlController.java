package fr.jstessier.rfshowcontrol;

public interface RFShowControlController {

	/**
	 *
	 * @param rfChannel
	 * @param pipeAddress
	 * @param mode
	 * @return	The current RFShowControlControllerImpl instance.
	 * @throws RFShowControlException	In case of communication error with RF Module.
	 */
	RFShowControlController start(byte rfChannel, byte[] pipeAddress,
								  RFShowControlRF24Adapter.Mode mode) throws RFShowControlException;

	/**
	 * Reset all channels to 0.
	 *
	 * @return	The current RFShowControlControllerImpl instance.
	 * @throws RFShowControlException	In case of communication error with RF Module.
	 */
	RFShowControlController resetChannelValues() throws RFShowControlException;

	/**
	 * Reset all channels to 0 and flush values.
	 *
	 * @return	The current RFShowControlControllerImpl instance.
	 * @throws RFShowControlException	In case of communication error with RF Module.
	 */
	RFShowControlController resetAndFlushChannelValues() throws RFShowControlException;

	/**
	 * Return the channel values.
	 * @return	The channel values.
	 */
	byte[] getChannelValues();

	/**
	 *
	 *
	 * @return	The current RFShowControlControllerImpl instance.
	 * @throws RFShowControlException
	 */
	RFShowControlController flushChannelValues() throws RFShowControlException;

	/**
	 *
	 *
	 * @param newChannelValue
	 * @param channelNumber
	 * @return	The current RFShowControlControllerImpl instance.
	 */
	RFShowControlController updateChannelValue(byte newChannelValue, int channelNumber);

	/**
	 *
	 *
	 * @param newChannelValues
	 * @param startChannelNumber
	 * @return	The current RFShowControlControllerImpl instance.
	 */
	RFShowControlController updateChannelValues(byte[] newChannelValues, int startChannelNumber);

	/**
	 *
	 *
	 * @param newChannelValues
	 * @return	The current RFShowControlControllerImpl instance.
	 */
	RFShowControlController updateChannelValues(byte[] newChannelValues);

	/**
	 *
	 *
	 * @param newChannelValues
	 * @return	The current RFShowControlControllerImpl instance.
	 * @throws RFShowControlException
	 */
	RFShowControlController updateAndFlushChannelValues(byte[] newChannelValues) throws RFShowControlException;

}
