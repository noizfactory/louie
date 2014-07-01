/*
 * RequestContext.java
 * 
 * Copyright (c) 2013 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.request;

import com.rhythm.pb.data.RequestContext;

/**
 *
 * @author cjohnson
 */
public class RequestContextManager {
  final private static RequestContextManager SINGLETON = new RequestContextManager();
  
  ThreadLocal<RequestContext> request;
  
  private RequestContextManager() {
      request = new ThreadLocal<RequestContext>();
  }
  
  public static RequestContext getRequest() {
      return SINGLETON.request.get();
  }
    
  public static void setRequest(RequestContext req) {
      SINGLETON.request.set(req);
  }
  
  public static void clearRequest() {
      SINGLETON.request.remove();
  }
  
}