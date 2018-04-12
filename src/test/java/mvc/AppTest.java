package mvc;

import javax.servlet.ServletException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import me.zqliang.mvc.servlet.DispatcherServlet;

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
        System.out.println("123");
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
    	DispatcherServlet ds = new DispatcherServlet();
    	try {
			ds.init(null);
		} catch (ServletException e) {
			e.printStackTrace();
		}
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
