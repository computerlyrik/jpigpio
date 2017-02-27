package jpigpio;

import jpigpio.impl.SPI;

public class Application {

    public static void main(String[] args) throws PigpioException {

        JPigpio pigpio = new Pigpio();

        pigpio.gpioInitialize();

        int chipSelectPin = JPigpio.PI_SPI_CE0;
        int soPin = JPigpio.PI_SPI_MISO;

        pigpio.gpioSetMode(chipSelectPin, JPigpio.PI_OUTPUT);
        pigpio.gpioSetMode(soPin, JPigpio.PI_INPUT);

        pigpio.gpioWrite(chipSelectPin, JPigpio.PI_HIGH);
        pigpio.gpioWrite(chipSelectPin, JPigpio.PI_LOW);
        while (pigpio.gpioRead(soPin) != JPigpio.PI_LOW) {
            System.out.println("Waiting for so to go low");
            pigpio.gpioDelay(500);
        }

        System.out.print("Reset complete");
        SPI spi = new SPI(pigpio, 0, JPigpio.PI_SPI_BAUD_8MHZ, 0);

        System.out.print("After SPI Setup CE: "+pigpio.gpioGetMode(chipSelectPin));
        System.out.print("After SPI Setup SO: "+pigpio.gpioGetMode(soPin));

        byte[] txData = new byte[]{0x30};
        byte[] rxData = new byte[2];
        spi.xfer(txData, rxData);

        for (byte b : rxData) {
            System.out.println(Integer.toHexString(b));
        }

        pigpio.gpioTerminate();

    }

}
