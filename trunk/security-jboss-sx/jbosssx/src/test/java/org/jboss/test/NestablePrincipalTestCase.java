/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.test;

import java.security.Principal;
import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.security.AnybodyPrincipal;
import org.jboss.security.NestablePrincipal;
import org.jboss.security.NobodyPrincipal;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;

/** Tests of the NestablePrincipal class.

@see org.jboss.security.NestablePrincipal

@author Scott.Stark@jboss.org
@version $Revision$
*/
public class NestablePrincipalTestCase extends TestCase
{
    static Principal[] principals = {
        new SimplePrincipal("user1"),
        new SimplePrincipal("user2"),
        new SimplePrincipal("user2"),
        new SimplePrincipal("user3")
    };
    static NestablePrincipal principal = new NestablePrincipal("CallerPrincipal");

    public NestablePrincipalTestCase(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new NestablePrincipalTestCase("testGetName"));
        suite.addTest(new NestablePrincipalTestCase("testEquals"));
        suite.addTest(new NestablePrincipalTestCase("testAddMember"));
        suite.addTest(new NestablePrincipalTestCase("testRemoveMember"));
        suite.addTest(new NestablePrincipalTestCase("testAnybody"));
        suite.addTest(new NestablePrincipalTestCase("testNobody"));

        return suite;
    }

    public void testGetName()
    {
        System.out.println("testGetName");
        assertTrue(principal.getName().equals("CallerPrincipal"));
    }

    public void testEquals()
    {
        System.out.println("testEquals");
        SimpleGroup CallerPrincipal = new SimpleGroup("CallerPrincipal");
        assertTrue(principal.equals(CallerPrincipal));
    }

    /** Test of removeMember method, of class org.jboss.security.NestablePrincipal. */
    public void testRemoveMember()
    {
        System.out.println("testRemoveMember");
        for(int p = principals.length -1; p >= 0; p --)
        {
            assertTrue("Remove "+principals[p], principal.removeMember(principals[p]));
            testMembers();
        }
    }

    /** Test of addMember method, of class org.jboss.security.NestablePrincipal. */
    public void testAddMember()
    {
        System.out.println("testAddMember");
        
        for(int p = 0; p < principals.length; p ++)
        {
            Principal user = principals[p];
            principal.addMember(user);
            assertTrue("AddMember "+user, principal.isMember(user));
            testMembers();
        }
    }

    public void testAnybody()
    {
        System.out.println("testAnybody");
        principal.addMember(principals[0]);
        assertTrue("AnybodyPrincipal.isMember", principal.isMember(AnybodyPrincipal.ANYBODY_PRINCIPAL));
    }

    public void testNobody()
    {
        System.out.println("testNobody");
        SimplePrincipal nobody = new SimplePrincipal("<NOBODY>");
        principal.addMember(nobody);
        assertTrue("AnybodyPrincipal.isMember", principal.isMember(NobodyPrincipal.NOBODY_PRINCIPAL) == false);
    }

    /** Test of members method, of class org.jboss.security.NestablePrincipal. */
    private void testMembers()
    {       
        Enumeration<Principal> members = principal.members();
        while( members.hasMoreElements() )
        {
            Principal user = (Principal) members.nextElement();
            assertTrue("Members "+user, principal.isMember(user));
        }
    }

    public static void main(java.lang.String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

}
