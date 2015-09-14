package nl.fontys.sevenlo.iowarrior;

import com.codemercs.iow.IowKit;
import nl.fontys.sevenlo.hwio.AbstractBitFactory;
import nl.fontys.sevenlo.hwio.BitAggregate;
import nl.fontys.sevenlo.hwio.Bit;
import nl.fontys.sevenlo.hwio.BitOps;
import nl.fontys.sevenlo.hwio.DefaultBitFactory;

/**
 * Bitwise operations on an IOWarrior.
 * This class supports input and ouput on the same device.
 * Only iowarrior pipe 0 is used. (Simple io).
 * This implementation supports IOWarrio40 only;
 * Which bits are input and which are output is determined
 * by the <i>inputMask</i>.
 * A 1 in the inputMask makes the bit at that position input,
 * a 0 makes the bit output. See the 1 and 0 as mnemonic for
 * input and output.
 * At construction time all inputbits are set high (1) (high impedance plus
 * pullup in the hardware), all outputbits are set to low (0).
 *
 * For debugging purposes, bit 0 of port 3 of the IOWarrior40
 * is toggled on each write.
 *
 * @author Pieter van den Hombergh (P.vandenHombergh at fontys.nl)
 */
public final class IOWarrior implements BitAggregate {

    //@GuardedBy("readLock")
    /** reference to handle from connector  */
    private final long handle;
    /** input to destinguish between in (1) and out (0). */
    private int inputMask;
    /** helps in serialized access to lastRead .*/
    private final Object readLock = new Object();
    /** helps in serialized access to shadow. */
    private final Object writeLock = new Object();
    //@GuardedBy("writeLock")
    /** the last value sent to the hardware output. */
    private volatile int shadow = 0;
    //@GuardedBy("writeLock")
    /** The last read (cached) value in the poll loop. */
    private volatile int lastRead = 0;
    /** blink byte 4. */
    //@GuardedBy("writeLock")
    private int blinker = 0;
    /** The connected bits */
    private final Bit[] bit;
    public static final int SUPPORTED_BITS = 32;

    /**
     * Create an  IoWarrior connection instance and uses a BitFactory
     * to create the IO Bits.
     * @param hnd handle to identify device to library
     * @param im inputMask
     * @param fact BitFactory
     */
    public IOWarrior(final long hnd, final int im,
            final AbstractBitFactory fact) {
        this.handle = hnd;
        this.inputMask = im;
        this.shadow = inputMask;
        bit = new Bit[SUPPORTED_BITS];
        for (int i = 0; i < bit.length; i++) {
            if ((inputMask & (1 << i)) != 0) {
                bit[i] = fact.createInputBit(i);
            } else {
                bit[i] = fact.createOutputBit(this, i);
            }
        }
        write(shadow);
    }

    /**
     * Create an  IoWarrior connection instance with default Bit instances.
     * @param hnd handle to idetify device to library
     * @param im inputMask
     */
    public IOWarrior(final long hnd, final int im) {
        this(hnd, im, new DefaultBitFactory());
    }

    /**
     * Read the iowarrior bits.
     *
     * This is a blocking read. The IOWarrior library will
     * only return if there were any changes in the
     * inputs. For event driven operation, this
     * should be called on a seperate input thread, such as returned
     * by {@link #createPollThread}.
     *
     * @return result which is 32 bits of new value.
     */
    public int read() {
        int[] rdata = IowKit.read(handle, 0, WARRIOR_BUF_SIZE);
        int result = 0;
        if (rdata.length > 0) {
            // discard highest byte
            for (int i = rdata.length - 2; i > 0; i--) {
                result <<= BYTE_BIT_SHIFT;
                result |= rdata[i] & LOWBYTE;
            }
        }
        // save for readFast
        synchronized (this.readLock) {
            lastRead = result;
            return lastRead;
        }
    }

    /**
     * Read the newest value from cache.
     * @return the latest value read by read().
     * @deprecated use lastRead() method instead.
     */
    @Deprecated
    public int cachedRead() {
        return lastRead();
    }

    /**
     * Writes the output with all input bits set high.
     * Writes only the lowest 24 bits as these are the only ones
     * connected to the output connector of the iowarrior.
     * The work is delegated to writeMasked with all bits to 1.
     * @param value to write.
     */
    public void write(int value) {
        writeMasked(~0, value);
    }

    /**
     * Specify the inputy mask to use.
     * @param ipm the input mask.
     */
    public void setInputMask(int ipm) {
        this.inputMask = ipm;
        writeMasked(ipm, ipm);
    }
    /**
     * The iowarrior has a command byte and 4 data bytes
     * for a total of 5.
     */
    private static final int WARRIOR_BUF_SIZE = 5;
    /** Isolate the low 8 bits of an int. */
    private static final int LOWBYTE = 255;
    /** The amount of bits to shift for a byte. */
    private static final int BYTE_BIT_SHIFT = 8;

    /**
     * WriteMasked as defined in the api of intwriter.
     * This version does all the work and is threadsafe.
     *
     * @param mask the bits that should be kept safe.
     * @param value the 'set' of  value bits.
     */
    public void writeMasked(int mask, int value) {
        int[] writeBuffer = null;
        int newShadow;
        // calculate stack local stuff from shadow and new value
        // using mask
        synchronized (this.writeLock) {
            newShadow = (shadow & ~mask) | (value & mask);
            if (newShadow != shadow) {
                shadow = newShadow;
                writeBuffer = new int[WARRIOR_BUF_SIZE];
                writeBuffer[0] = 0;
                writeBuffer[writeBuffer.length - 1] = blinker;
                blinker ^= 1; // toggle lowest bit of port 3.
            }
        }

        // writing to the hardware is done unsynchronized.
        // newShadow and writebuffer are local vars, thus thread safe.
        if (writeBuffer != null) {

            for (int i = 1; i < writeBuffer.length - 1; i++) {
                writeBuffer[i] = newShadow & LOWBYTE;
                newShadow >>= BYTE_BIT_SHIFT;
            }

            // assume that number of bytes written is correct.
            // so no test of result
            IowKit.write(handle, 0, writeBuffer);
        }
    }

    /**
     * Get a bit through ist number.
     * @param i the bit  number
     * @return the bit
     */
    public BitOps getBit(int i) {
        return bit[i];
    }

    /**
     * Retrun debug info.
     * @return a description of the warrior and ist state.
     */
    @Override
    public String toString() {
        return IOWarriorConnector.getInstance().getSerialNr(handle) +
                " im=" + inputMask + " read=" + lastRead + " shadow=" + shadow;
    }

    /**
     * Get the number of supported bits.
     * @return the size
     */
    public int size() {
        return SUPPORTED_BITS;
    }

    /**
     * Retrun the input mask.
     * @return the input mask
     */
    public int getInputMask() {
        return this.inputMask;
    }

    /**
     * Return the shadow valaue.
     * @return the shadow value.
     */
    public int lastWritten() {
        return shadow;
    }

    /**
     * Non blocking read of last received value.
     * Note that the value may be stale.
     * @return the last value.
     */
    public int lastRead() {
        synchronized (this.readLock) {
            return shadow;
        }
    }
    
}
