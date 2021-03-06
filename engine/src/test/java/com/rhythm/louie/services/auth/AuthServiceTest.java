/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rhythm.louie.services.auth;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rhythm.pb.RequestProtos.IdentityPB;
import com.rhythm.pb.RequestProtos.SessionBPB;
import com.rhythm.pb.RequestProtos.SessionKey;

import com.rhythm.louie.connection.Identity;
import com.rhythm.louie.connection.LouieConnection;
import com.rhythm.louie.connection.LouieConnectionFactory;
import com.rhythm.louie.services.info.InfoClient;
import com.rhythm.louie.services.info.InfoClientFactory;

import static org.junit.Assert.*;

/**
 *
 * @author cjohnson
 */
public class AuthServiceTest {
    
    private SessionKey currentKey;
    private final SessionKey bogusKey = SessionKey.newBuilder().setKey("123").build();
    
    private final AuthClient client;
    private final LouieConnection connection;
    
    public AuthServiceTest() {
        connection = LouieConnectionFactory.getLocalConnection(
                Identity.createJUnitIdentity());
        client = AuthClientFactory.getClient(connection);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    
    /**
     * Test of createSession method, of class AuthService.
     */
    @Test
    public void testCreateSession() throws Exception {
        System.out.println("createSession - auth");
        SessionKey key = client.createSession(connection.getIdentity());
        
        assertNotNull(key);
        
        System.out.println(key);
        
        currentKey = key;
    }
    
    /**
     * Test of getSession method, of class AuthService.
     */
    @Test
    public void testGetSession() throws Exception {
        if (currentKey==null) {
            testCreateSession();
        }
        
        System.out.println("getSession");
        
        //SessionBPB session = client.getSession(currentKey);
        
        SessionKey searchKey = SessionKey.newBuilder()
                .setKey("vrc0fquusb4c6nnchi157n91k2").build();
        
        SessionBPB session = client.getSession(searchKey);
        assertNotNull(session);
        System.out.println(session);
        
        session = client.getSession(bogusKey);
        assertNull(session);
        System.out.println(session);
    }
    
    /**
     * Test of findSessions method, of class AuthService.
     */
    @Test
    public void testFindSessions() throws Exception {
        System.out.println("findSessions");
        
        SessionKey searchKey = SessionKey.newBuilder()
                .setKey("vr").build();
        
        List<SessionBPB> session = client.findSessions(searchKey);
        assertNotNull(session);
        
        for (SessionBPB s : session) {
            System.out.println(s);
        }
    }

    /**
     * Test of getSessionIdentity method, of class AuthService.
     */
    @Test
    public void testGetSessionIdentity() throws Exception {
        if (currentKey==null) {
            testCreateSession();
        }
        
        System.out.println("getSessionIdentity");
        
        IdentityPB identity = client.getSessionIdentity(currentKey);
        assertNotNull(identity);
        System.out.println(identity);
        
        identity = client.getSessionIdentity(bogusKey);
        assertNull(identity);
        System.out.println(identity);
    }

    /**
     * Test of isValidSession method, of class AuthService.
     */
    @Test
    public void testIsValidSession() throws Exception {
        if (currentKey==null) {
            testCreateSession();
        }
        
        System.out.println("isValidSession");
        
        Boolean result =  client.isValidSession(currentKey);
        System.out.println(result);
        assertTrue(result);
        
        result = client.isValidSession(bogusKey);
        System.out.println(result);
        assertFalse(result);
    }
    

    /**
     * Test of createSession method, directly on the connection.
     */
    @Test
    public void testConnectionCreateSession() throws Exception {
        System.out.println("createSession - LouieConnection");
        
        LouieConnection testConn = LouieConnectionFactory.getLocalConnection(
                Identity.createJUnitIdentity());
        SessionKey key = testConn.getSessionKey();
        
        assertNotNull(key);
        
        System.out.println(key);
    } 
    
     /**
     * Test of to be sure the connection recreates a new session.
     */
    @Test
    public void testNonExistentSession() throws Exception {
        System.out.println("Non Existing Session");
        
        LouieConnection testConn =  LouieConnectionFactory.getLocalConnection(
                Identity.createJUnitIdentity(),"NOTAKEY");
        InfoClient louieClient = InfoClientFactory.getClient(testConn);
        
        System.out.println(connection.getSessionKey());
        
        List<String> result = louieClient.getAllServiceNames();

        assertFalse(result.isEmpty());
        assertFalse("NOTAKEY".equals(connection.getSessionKey().getKey()));
        
        System.out.println(connection.getSessionKey());
    }
    
     /**
     * Test of to be sure the connection recreates a new session.
     */
    @Test
    public void testSessionStats() throws Exception {
        System.out.println("Session Stats");
        
        LouieConnection testConn =  LouieConnectionFactory.getLocalConnection(
                Identity.createJUnitIdentity(),"NOTAKEY");
        
        InfoClient louieClient = InfoClientFactory.getClient(testConn);
        
        List<String> result = louieClient.getAllServiceNames();
        
        assertFalse(result.isEmpty());
        
        result = louieClient.getAllServiceNames();
        
        assertFalse(result.isEmpty());
        
        SessionBPB session = client.getSession(testConn.getSessionKey());
        System.out.println("Stats: "+session);
        
        assertNotNull(session);
        assertTrue(session.getStats().getRequestCount()==2);
    }
}
