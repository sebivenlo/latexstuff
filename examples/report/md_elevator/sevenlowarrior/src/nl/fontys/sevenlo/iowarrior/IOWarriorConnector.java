package nl.fontys.sevenlo.iowarrior;

import com.codemercs.iow.IowKit;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Provides a central connection point to the IO Warriors.
 *
 * This class tries to open a connection to the iowarrior subsystem.
 * On succes it created a handle for each iowarrior found, which can
 * be used to attach to for IO operations. The handles can be used as
 * a parameter (e.g. in the constructor) of classes that provide
 * access to the IOWarriors functionallity.
 *
 * The Connector is a Singleton.
 *
 * @author Pieter van den Hombergh (P.vandenHombergh at fontys.nl)
 */
public final class IOWarriorConnector {
    /**
     * The time to wait on a write.
     */
    public static final long WRITE_TIMEOUT = 1000L;
    /**
     * The handles to the connected warriors.
     */
    private Long[] handles = new Long[0];

    /**
     * Private ctor for connector.
     */
    private IOWarriorConnector() {
        if ( IowKit.openDevice() != 0L ) {
            handles = new Long[(int) IowKit.getNumDevs()];
            for ( int i = 0, j = 1; i < handles.length; i++, j++ ) {
                handles[i] = IowKit.getDeviceHandle( j );
                IowKit.setWriteTimeout( handles[i], WRITE_TIMEOUT );
            }

            // make sure that order is dependent on serial number, not on
            // connection time order.
            Arrays.sort( handles, new HandleComparator() );

            // make sure device is closed at end to release device for next run.
            Thread exitHook = new Thread( new Runnable() {


                public void run() {
                    IowKit.closeDevice( 0L );

                }
            } );
            Runtime.getRuntime().addShutdownHook( exitHook );
        }

        listDevices();
    }

    /**
     * Singleton helper. See Java concurrency in practice,
     * initialisation idioms. The Holder class is loaded on first
     * invocation. The classloader and the java memory model
     * ensure threadsafety. See Effective Java.
     */
    private static final class Holder {
        /** The instance we save. */
        private static IOWarriorConnector instance = new IOWarriorConnector();
        /** utility class should not have an accessible  constructor. */
        private Holder( ) { }

        /**
         * Return the one Elvis.
         * @return the instance.
         */
        public static IOWarriorConnector getInstance() {
            return instance;
        }
    }

    /**
     * The singleton getter.
     * @return the only instance of the connector.
     */
    public static IOWarriorConnector getInstance() {
        return Holder.instance;
    }

    /**
     * Get the handle for the ith warrior.
     * The IOWarriors are sorted in order of product id,serial number.
     * The method will throw an
     * ArrayIndexOutOfBoundsException if no IOWarriors are found or
     * if the method is called with i &gt;= getWarriorCount;
     * @param i the handle index requested
     * @return the handle to an IOWarrior
     * @throws ArrayIndexOutOfBoundsException when no iowarriors are
     * available.
     */
    public long getHandle( final int i ) {
        return handles[i];
    }

    /**
     * List the devices.
     */
    public void listDevices() {
        System.out.println( this );
    }

    /**
     * Get the number of IOWarriors connected to the system.
     * @return the number of warriors
     */
    public int getWarriorCount() {
        return handles.length;
    }

    /**
     * Get the handle of the first IOWarrior found.
     * The method returns the handle to IOWarrior with the
     * lowest serial number attached to the system before this singleton's
     * constructor was called. The method will throw an
     * ArrayIndexOutOfBoundsException if no IOWarriors are found.
     * @return a handle to an IOWarrior
     */
    public long getHandle() {
        return getHandle( 0 );
    }

    /**
     * Helper to keep handles in product id, serial number order.
     * The class definition states that it compares Long. These
     * are compared using the prodict id and serial numbers of the
     * IOWarriors.
     */
    private static class HandleComparator implements Comparator < Long > {

        public int compare( Long a, Long b ) {
            int result;

            Long apid = IowKit.getProductId( a );
            Long bpid = IowKit.getProductId( b );
            result = apid.compareTo( bpid );
            if ( result != 0 ) {
                return result;
            }
            String as = IowKit.getSerialNumber( a );
            String bs = IowKit.getSerialNumber( b );
            result = as.compareTo( bs );
            return result;
        }
    }

    /**
     * Get the serial number of the warrior.
     * Used in comparison.
     * @param warrior index in array
     * @return serial number, assumed to be unique per model.
     */
    public String getSerialNr( final int warrior ) {
        return IowKit.getSerialNumber( handles[warrior] );

    }
    /**
     * Get the serial number of the warrior.
     * Used in comparison.
     * @param warriorHandle handle for warrior
     * @return serial number, assumed to be unique per model.
     */
    public String getSerialNr( final long warriorHandle ) {
        return IowKit.getSerialNumber( warriorHandle );

    }
    /**
     * Return a string representation of the iowarriors found.
     * @return string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "Found " + handles.length + " IOWarriors\n" );
        for ( int i = 0, j = 1; i < handles.length; i++, j++ ) {
            sb.append(j + " - " );
            sb.append( "Product = "
                    + Long.toHexString(IowKit.getProductId( handles[i] ) ) );
            sb.append( " Serial = "
                    + IowKit.getSerialNumber( handles[i] ) );
            sb.append( " Revision = "
                    + Long.toHexString( IowKit.getRevision( handles[i] ) ) );
            sb.append( " Handle = "
                    + Long.toHexString( handles[i] ) );
        }
        return sb.toString();
    }
}
