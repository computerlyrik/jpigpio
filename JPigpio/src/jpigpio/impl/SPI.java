package jpigpio.impl;

import jpigpio.GPIO;
import jpigpio.JPigpio;
import jpigpio.PigpioException;
import jpigpio.Utils;

public class SPI {
	private int handle;
	private final JPigpio pigpio;
	private boolean debug;
	private GPIO ce;

    private final int cePin;

	public SPI(JPigpio pigpio, int channel, int baudRate, int flags) throws PigpioException {
		this.pigpio = pigpio;
		this.debug = false;
		handle = pigpio.spiOpen(channel, baudRate, flags);
        cePin = JPigpio.PI_SPI_CE0 + channel;

	} // End of constructor

    public void resetPinState() throws PigpioException {
        pigpio.gpioSetMode(cePin, JPigpio.PI_ALT0);
        pigpio.gpioSetMode(JPigpio.PI_SPI_MISO, JPigpio.PI_ALT0);
        pigpio.gpioSetMode(JPigpio.PI_SPI_MOSI, JPigpio.PI_ALT0);
        pigpio.gpioSetMode(JPigpio.PI_SPI_SCLK, JPigpio.PI_ALT0);
    }
	public void close() throws PigpioException {
		pigpio.spiClose(handle);
	} // End of close

	public void read(byte data[]) throws PigpioException {
        ceSetValue(JPigpio.PI_LOW);
		pigpio.spiRead(handle, data);
        ceSetValue(JPigpio.PI_HIGH);
		if (debug) {
			System.out.println("spiRead: " + Utils.dumpData(data));
		}
	} // End of read

	public void write(byte data[]) throws PigpioException {
		if (debug) {
			System.out.println("spiWrite: " + Utils.dumpData(data));
		}
        ceSetValue(JPigpio.PI_LOW);
		pigpio.spiWrite(handle, data);
        ceSetValue(JPigpio.PI_HIGH);
	} // End of write

	public void xfer(byte txData[], byte rxData[]) throws PigpioException {
		if (debug) {
			System.out.print("xfer: " + Utils.dumpData(txData));
		}
        ceSetValue(JPigpio.PI_LOW);
		pigpio.spiXfer(handle, txData, rxData);
        ceSetValue(JPigpio.PI_HIGH);
		if (debug) {
			System.out.println(" " + Utils.dumpData(rxData));
		}
	} // End of xfer

	public byte xfer(byte txData) throws PigpioException {
		byte txData1[] = {txData};
		byte rxData[] = new byte[1];
		xfer(txData1, rxData);
		return rxData[0];
	}

	    private void ceSetValue(boolean value) throws PigpioException {
	        if (ce != null) {
	            ce.setValue(value);
	        }
	    }
} // End of class
// End of file