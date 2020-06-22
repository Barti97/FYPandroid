package com.example.fyp.service;

import android.util.Log;

import org.apache.tomcat.util.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

//import org.apache.commons.io.input.BoundedInputStream;

public class HttpRequest {

    public static String executePost(String targetURL, String urlParameters) {
        return executePost(targetURL, urlParameters, null, null);
    }

    public static String executePost(String targetURL, String urlParameters, String username, String password) {

        Log.d("urlParameters", urlParameters);
        HttpURLConnection connection = null;

        try {
            // Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8");

            if (username != null && password != null) {
//                Log.d("username", username);
//                Log.d("password", password);
                String auth = createBasicAuth(username, password);
//                Log.d("auth", auth);
                connection.setRequestProperty("Authorization", auth);
            }


            connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            // Get Response
            Log.d("code", Integer.toString(connection.getResponseCode()));
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String executeGet(String urlToRead) {
        HttpURLConnection conn = null;
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Accept", "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8");
//            InputStream bounded = new BoundedInputStream(is, MAX_BYTE_COUNT);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(bounded));
//            String line = reader.readLine();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            String line;
            int c;
            while((c = rd.read()) != -1) {
                result.append((char)c);
            }
//            while ((line = rd.readLine()) != null) {
//                result.append(line);
////                Log.i("line", line);
//            }
            rd.close();
//            Log.d("RES SIZE", Integer.toString(result.length()));
            return result.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String createBasicAuth(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodeStringIntoBytes = auth.getBytes(StandardCharsets.UTF_8);
        byte[] encodedAuth = Base64.encodeBase64(encodeStringIntoBytes);
        String authHeader = "Basic " + new String(encodedAuth);
        return authHeader;
    }
}