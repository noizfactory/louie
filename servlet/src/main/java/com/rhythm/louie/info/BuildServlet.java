/*
 * BuildServlet.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */
package com.rhythm.louie.info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rhythm.louie.server.BuildProperties;

/**
 *
 * @author cjohnson
 */
@WebServlet(urlPatterns = {"/build"})
public class BuildServlet extends HttpServlet {
      
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<BuildProperties> serviceBuilds = new ArrayList<>();
        List<BuildProperties> louieBuilds = new ArrayList<>();
        List<BuildProperties> otherBuilds = new ArrayList<>();
        
        for (BuildProperties build : BuildProperties.getAllBuildProperties()) {
            if (!build.isServiceBuild()) {
                otherBuilds.add(build);
            } else if (build.getVersion()!=null && build.getBuildVersion().startsWith("louie-")) {
                louieBuilds.add(build);
            } else {
                serviceBuilds.add(build);
            }
        }
        
        Map<String,Object> properties = new HashMap<>();
        properties.put("louieBuilds", louieBuilds);
        properties.put("builds",serviceBuilds);
        properties.put("otherBuilds",otherBuilds);

        InfoUtils.writeTemplateResponse(request, response,"build.vm", properties);
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Build Information";
    }// </editor-fold>
}
