package jpigpio;

import jpigpio.impl.SPI;

public class Application {

    private static JPigpio pigpio;
    private static SPI spi;

    public static void main(String[] args) throws PigpioException {

        pigpio = new Pigpio();

        pigpio.gpioInitialize();

        int chipSelectPin = JPigpio.PI_SPI_CE0;
        int soPin = JPigpio.PI_SPI_MISO;

        autoReset(chipSelectPin, soPin);
        System.out.println("After AutoReset CE: " + pigpio.gpioGetMode(chipSelectPin));
        System.out.println("After AutoReset SO: " + pigpio.gpioGetMode(soPin));

        spi = new SPI(pigpio, 0, JPigpio.PI_SPI_BAUD_8MHZ, 0);
        System.out.println("After SPI Setup CE: " + pigpio.gpioGetMode(chipSelectPin));
        System.out.println("After SPI Setup SO: " + pigpio.gpioGetMode(soPin));

        // manualReset(chipSelectPin, soPin, JPigpio.PI_SPI_MOSI, JPigpio.PI_SPI_SCLK);
        System.out.println("After ManualReset CE: " + pigpio.gpioGetMode(chipSelectPin));
        System.out.println("After ManuaReset SO: " + pigpio.gpioGetMode(soPin));

        byte[] txData, rxData;

        txData = new byte[] { (byte) (0xF0) };
        rxData = new byte[1];
        spi.xfer(txData, rxData);

        for (byte b : rxData) {
            System.out.println(Integer.toHexString(b));
        }

        txData = new byte[] { (byte) (0xF1) };
        rxData = new byte[1];
        spi.xfer(txData, rxData);

        for (byte b : rxData) {
            System.out.println(Integer.toHexString(b));
        }

        spi.close();
        pigpio.gpioTerminate();

    }

    private static void autoReset(int chipSelectPin, int soPin) throws PigpioException {

        pigpio.gpioSetMode(chipSelectPin, JPigpio.PI_OUTPUT);
        pigpio.gpioSetMode(soPin, JPigpio.PI_INPUT);

        pigpio.gpioWrite(chipSelectPin, JPigpio.PI_HIGH);
        pigpio.gpioWrite(chipSelectPin, JPigpio.PI_LOW);
        while (pigpio.gpioRead(soPin) != JPigpio.PI_LOW) {
            System.out.println("Waiting for so to go low");
            pigpio.gpioDelay(500);
        }

        System.out.println("Auto Reset complete");
    }

    private static void manualReset(int chipSelectPin, int soPin, int siPin, int sclkPin)
            throws PigpioException {

        /* 1 */
        pigpio.gpioSetMode(sclkPin, JPigpio.PI_OUTPUT);
        pigpio.gpioWrite(sclkPin, JPigpio.PI_HIGH);

        pigpio.gpioSetMode(siPin, JPigpio.PI_OUTPUT);
        pigpio.gpioWrite(siPin, JPigpio.PI_LOW);

        /* 2 */
        pigpio.gpioSetMode(chipSelectPin, JPigpio.PI_OUTPUT);

        pigpio.gpioWrite(chipSelectPin, JPigpio.PI_LOW);
        pigpio.gpioWrite(chipSelectPin, JPigpio.PI_HIGH);

        /* 3 */
        pigpio.gpioWrite(chipSelectPin, JPigpio.PI_LOW);
        pigpio.gpioWrite(chipSelectPin, JPigpio.PI_HIGH);
        pigpio.gpioDelay(42, JPigpio.PI_MICROSECONDS);

        /* 4 */
        pigpio.gpioSetMode(soPin, JPigpio.PI_INPUT);
        while (pigpio.gpioRead(soPin) != JPigpio.PI_LOW) {
            System.out.println("Waiting for so to go low");
            pigpio.gpioDelay(500);
        }

        /* 5 */
        spi.resetPinState();
        byte[] txData, rxData;

        txData = new byte[] { 0x30 };
        rxData = new byte[2];

        spi.xfer(txData, rxData);

        for (byte b : rxData) {
            System.out.println(Integer.toHexString(b));
        }

        /* 5 */
        pigpio.gpioSetMode(soPin, JPigpio.PI_INPUT);
        while (pigpio.gpioRead(soPin) != JPigpio.PI_LOW) {
            System.out.println("Waiting for so to go low");
            pigpio.gpioDelay(500);
        }
        spi.resetPinState();
        System.out.println("Manual Reset complete");
    }

}
