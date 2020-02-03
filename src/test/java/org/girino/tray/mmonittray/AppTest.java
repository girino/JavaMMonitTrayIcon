/*******************************************************************************
 * Copyright (c) 2019 by Girino Vey.
 * 
 * Permission to use this software, modify and distribute it, or parts of it, is 
 * granted to everyone who wishes provided that the above copyright notice 
 * is kept or the conditions of the full version of this license are met.
 * 
 * See Full license at: https://girino.org/license/
 ******************************************************************************/
package org.girino.tray.mmonittray;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
