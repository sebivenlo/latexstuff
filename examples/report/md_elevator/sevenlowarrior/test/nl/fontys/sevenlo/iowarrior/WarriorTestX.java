/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.fontys.sevenlo.iowarrior;

import com.codemercs.iow.IowKit;
import nl.fontys.sevenlo.hwio.SimplePoller;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Only run this test with IOWarrior connected with cross cable.
 * @author hom
 */
public class WarriorTestX {

    public WarriorTestX() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        IowKit.closeDevice(0L);

    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void hello() {
        IOWarriorConnector c = IOWarriorConnector.getInstance();
        assertTrue("make sure we have warriors ", 0 < c.getWarriorCount());

    }

    @Test
    public void bitRace() {
        IOWarriorConnector c = IOWarriorConnector.getInstance();
        long iowh = c.getHandle();
        int inputMask = 0xfff;
        IOWarrior iow = new IOWarrior(iowh, inputMask); /// 12 low bits input
        System.out.println(iow);
        SimplePoller pol = new SimplePoller(iow, inputMask);
        for (int i = 0; i < 12; i++) {
            System.out.println("Test bit " + i);
            iow.getBit(12 + i).set(true);
            pol.pollOnce();
            assertTrue(iow.getBit(i).isSet());
            iow.getBit(12 + i).set(false);
            pol.pollOnce();
            assertFalse(iow.getBit(i).isSet());
        }
        inputMask = 0xfff000;
        iow.setInputMask(inputMask);
        for (int i = 0; i < 12; i++) {
            System.out.println("Test bit " + i);
            iow.getBit(i).set(true);
            pol.pollOnce();
            assertTrue(iow.getBit(12 + i).isSet());
            iow.getBit(i).set(false);
            pol.pollOnce();
            assertFalse(iow.getBit(12 + i).isSet());
        }

    }
}
