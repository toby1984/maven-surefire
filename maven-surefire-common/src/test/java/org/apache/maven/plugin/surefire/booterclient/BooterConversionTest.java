package org.apache.maven.plugin.surefire.booterclient;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.maven.plugin.surefire.booterclient.BooterSerializer;
import org.apache.maven.surefire.booter.BooterDeserializer;

public class BooterConversionTest
    extends TestCase
{
    Method convert, constructParamObjects;

    public void setUp()
        throws Exception
    {
        convert = BooterSerializer.class.getDeclaredMethod( "convert", new Class[]{ Object.class } );
        convert.setAccessible( true );
        constructParamObjects = BooterDeserializer.class.getDeclaredMethod( "constructParamObjects",
                                                                          new Class[]{ String.class, Class.class } );
        constructParamObjects.setAccessible( true );
    }

    public void testString()
        throws Exception
    {
        doTest( "Hello world!" );
    }

    public void testFile()
        throws Exception
    {
        doTest( new File( "." ) );
    }

    public void testFileArray()
        throws Exception
    {
        doTestArray( new File[]{ new File( "." ) } );
    }

    public void testArrayList()
        throws Exception
    {
        doTest( new ArrayList() );
    }

    public void testBoolean()
        throws Exception
    {
        doTest( Boolean.TRUE );
        doTest( Boolean.FALSE );
    }

    public void testInteger()
        throws Exception
    {
        doTest( new Integer( 0 ) );
    }

    public void testProperties()
        throws Exception
    {
        Properties p = new Properties();
        p.setProperty( "foo", "bar" );
        doTest( p );
    }

    public void testPropertiesEmpty()
        throws Exception
    {
        Properties p = new Properties();
        doTest( p );
    }

    public void testPropertiesWithComma()
        throws Exception
    {
        Properties p = new Properties();
        p.setProperty( "foo, comma", "bar" );

        doTest( p );
    }

    public void doTest( Object o )
        throws Exception
    {
        String serialized = serialize( o );
        Object[] output = deserialize( serialized, o.getClass());
        Assert.assertEquals( "Wrong number of output elements: " + Arrays.asList( output ), 1, output.length );
        Assert.assertEquals( o, output[0] );
    }

    public void doTestArray( Object[] o )
        throws Exception
    {
        String serialized = serialize( o );
        Object[] output = deserialize( serialized, o.getClass() );
        Assert.assertEquals( "Wrong number of output elements: " + Arrays.asList( output ), 1, output.length );
        assertArrayEquals( "Deserialized array didn't match", o, (Object[]) output[0] );
    }

    private void assertArrayEquals( String message, Object[] expected, Object[] actual )
    {
        Assert.assertEquals( message + "; wrong number of elements", expected.length, actual.length );
        for ( int i = 0; i < expected.length; i++ )
        {
            Assert.assertEquals( message + "; element " + i + " differs", expected[i], actual[i] );
        }
    }

    private Object[] deserialize( String paramProperty, Class typeProperty )
        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        return (Object[]) constructParamObjects.invoke( null, new Object[]{ paramProperty, typeProperty } );
    }

    private String serialize( Object o )
        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        return (String) convert.invoke( null, new Object[]{ o } );
    }
}
