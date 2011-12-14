package com.emergya.aplicaciones.datatransfer;

import java.util.Collection;
import java.util.LinkedList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.emergya.aplicaciones.classTest.TestClass1;
import com.emergya.aplicaciones.classTest.TestClass2;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	private static final String SEPARADOR = "/";
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
    public void testApp(){
        //assertTrue( true );
    	
    	//exactCopy
    	try{
    		TestClass1 tc1 = new TestClass1();
    		
    		tc1.setId(new Long(1));
    		tc1.setLogin("Login");
    		tc1.setPassword("Password");
    		tc1.setNumber(new Integer(2));
    		
    		TestClass2 tc2 = new TestClass2();
    		tc2 = (TestClass2)DataTransfer.exactCopy(tc1, tc2);
    		
    		//System.out.println(tc2.getId() + SEPARADOR + tc2.getLogin() + SEPARADOR + tc2.getPassword() + SEPARADOR + tc2.getNumber());
    		assertTrue( true );
    	}catch(DataTransferException e){
    		e.printStackTrace();
    	}
    	
    	
    	
    	
    	//partialCopy
    	try{
    		TestClass1 tc1 = new TestClass1();
    		
    		tc1.setId(new Long(2));
    		tc1.setLogin("Login2");
    		tc1.setPassword("Password2");
    		tc1.setNumber(new Integer(22));
    		
    		TestClass2 tc2 = new TestClass2();
    		tc2 = (TestClass2)DataTransfer.partialCopy(tc1, tc2);
    		
    		//System.out.println(tc2.getId() + SEPARADOR + tc2.getLogin() + SEPARADOR + tc2.getPassword() + SEPARADOR + tc2.getNumber());
    		assertTrue( true );
    	}catch(DataTransferException e){
    		e.printStackTrace();
    	}
    	
    	
    	
    	
    	
    	//customCopy
    	try{
    		TestClass1 tc1 = new TestClass1();
    		
    		tc1.setId(new Long(3));
    		tc1.setLogin("Login3");
    		tc1.setPassword("Password3");
    		tc1.setNumber(new Integer(22));
    		
    		TestClass2 tc2 = new TestClass2();
    		Collection<String> attributes = new LinkedList<String>();
    		attributes.add("id");
    		attributes.add("login");
    		
    		tc2 = (TestClass2)DataTransfer.customCopy(tc1, tc2, attributes);
    		//System.out.println(tc2.getId() + SEPARADOR + tc2.getLogin() + SEPARADOR + tc2.getPassword() + SEPARADOR + tc2.getNumber());
    		
    		tc2 = new TestClass2();
    		attributes = new LinkedList<String>();
    		attributes.add("password");
    		attributes.add("number");
    		tc2 = (TestClass2)DataTransfer.customCopy(tc1, tc2, attributes);
    		//System.out.println(tc2.getId() + SEPARADOR + tc2.getLogin() + SEPARADOR + tc2.getPassword() + SEPARADOR + tc2.getNumber());
    		
    		assertTrue( true );
    		
    	}catch(DataTransferException e){
    		e.printStackTrace();
    	}
    
    	
    	
    	
    	//completCopy
    	try{
    		TestClass1 tc1 = new TestClass1();
    		
    		tc1.setId(new Long(1));
    		tc1.setLogin("Login");
    		tc1.setPassword("Password");
    		tc1.setNumber(new Integer(2));
    		
    		TestClass2 tc2 = new TestClass2();
    		tc2 = (TestClass2)DataTransfer.completCopy(tc1, tc2);
    		
    		//System.out.println(tc2.getId() + SEPARADOR + tc2.getLogin() + SEPARADOR + tc2.getPassword() + SEPARADOR + tc2.getNumber());
    		assertTrue( true );
    	}catch(DataTransferException e){
    		e.printStackTrace();
    	}
    	
    }
    
    
    
    
}
