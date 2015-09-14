package nl.fontys.sevenlo.iowarrior;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.fontys.sevenlo.hwio.BitAggregate;
import nl.fontys.sevenlo.hwio.IO;
import nl.fontys.sevenlo.hwio.SimpleBitAggregate;
import nl.fontys.sevenlo.utils.ResourceUtils;
import nl.fontys.sevenlo.widgets.IOGUIPanel;

/**
 * Test GUI for the IOWarrior.
 * Accepts property files on the command line.
 * These properties are "attached" to the iowarrior(s).
 * The io warriors are ordered in ascending order by serial number, 
 * so the order of the property file on the command line matters if more then one  
 * file is used.
 *
 * @author hom
 */
public class WarriorTestGUI {

    /**
     * 
     */
    /**
     * Startup.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        int im = 0xFFFF;
        String propFilename;
        Properties[] properties;
        System.out.println("args.length=" + args.length);
        if (0 < args.length) {
            properties = new Properties[args.length];
            for (int p = 0; p < args.length; p++) {
                // take first arg as prop file name
                propFilename = args[p];
                properties[p] = new Properties();
                // load properties file
                System.out.println("trying to load properties " + propFilename);
                properties[p] =
                        ResourceUtils.loadPropertiesFormFile(
                        properties[p], propFilename);
            }
        } else {
            properties = new Properties[1];
            properties[0] = new Properties();
        }

        IOWarriorConnector iowc = IOWarriorConnector.getInstance();
        BitAggregate irw = null;
        String[] labelText;
        int warriorCount = iowc.getWarriorCount();
        int p = 0;
        int initX = 100, initY = 100;
        if (0 != warriorCount) {
            labelText = new String[warriorCount];
            for (int h = 0; h < warriorCount; h++) {

                long handle = iowc.getHandle(h);
                    
                im = (int) ResourceUtils.parseHexProperty(properties[h],"inputMask","0xffff");
                irw = new IOWarrior(handle, im);

                labelText[h] = "Real IO Warrior connected serial "
                        + IOWarriorConnector.getInstance().getSerialNr(handle)
                        + " io mask=" + im;
                IOGUIPanel g = new IOGUIPanel(labelText[h],
                        irw, properties[h]);
                g.startTheShow();
                g.setLocation(initX, initY);
                initX += g.getWidth();

                if (p < properties.length - 1) {
                    p++;
                }
            }
        } else {
            IO rwSim = new ReaderWriterSim();
            irw = new SimpleBitAggregate(rwSim, im);
            labelText = new String[1];
            labelText[0] = "No IO warrior, io is faked, inputmask="
                    + Integer.toHexString(im);
            System.out.println("No warriors found using simulator");
            IOGUIPanel g = new IOGUIPanel(labelText[0], irw,
                    properties[0]);
            g.startTheShow();
        }
    }

    /**
     * IO Mockup if no Warrior is present.
     * @author hom
     */
    private static class ReaderWriterSim implements IO {

        private int value = 0;
        private volatile boolean newData = false;
        private final Object myLock = new Object();

        public ReaderWriterSim() {
        }

        @Override
        public int read() {
            System.out.println("sim read try ");
            synchronized (myLock) {
                try {
                    while (!newData) {
                        myLock.wait();
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(WarriorTestGUI.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("sim read " + value);
            int result = value;
            newData = false;
            return result;
        }

        private void write(int newValue) {
            synchronized (myLock) {
                value = newValue >> 12;
                newData = true;
                myLock.notifyAll();
            }
            System.out.println("sim written " + newValue);

        }

        @Override
        public void writeMasked(int mask, int newValue) {
            synchronized (myLock) {
                write((value & ~mask) | (newValue & mask));
            }
        }

        @Override
        public int lastWritten() {
            return value;
        }
    }
}
