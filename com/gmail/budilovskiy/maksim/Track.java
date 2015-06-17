/**
 * class provide a single track bean
 * 
 */

package com.gmail.budilovskiy.maksim;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Track {

    private String trackArtist = null;
    private String trackTitle = null;
    private String trackURL = null;
    private static HttpURLConnection connection;

    public Track(String trackArtist, String trackTitle) {
        setTrackArtist(trackArtist);
        setTrackTitle(trackTitle);
    }

    public String getTrackArtist() {
        return trackArtist;
    }

    public final void setTrackArtist(String trackArtist) {
        this.trackArtist = trackArtist;
    }

    public final void setTrackTitle(String trackTitle) {
        this.trackTitle = trackTitle;
    }
    
    public String getTrackTitle() {
        return trackTitle;
    }

    public String getTrackURL() {
        return trackURL;
    }

    public void setTrackURL(String fullTrackName) {
        try {
            this.trackURL = getURL(fullTrackName);
            connection.disconnect();
            /* for debugging */
            System.out.println(new Date(System.currentTimeMillis()) + "\n" + this.toString() + "\nURL: " + trackURL);
        } catch (XPathExpressionException | IOException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(Track.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error while searching for a track URL: " + fullTrackName);
        }
    }

    @Override
    public String toString() {
        return trackArtist + " - " + trackTitle;
    }

    // getting Track URL from VK
    private static String getURL(String fullTrackName)
            throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        /*
        Create URL string
        */
        StringBuffer buff = new StringBuffer();
        buff.append("https://api.vk.com/method/");
        buff.append("audio.search.xml");
        buff.append("?q=");
        buff.append(URLEncoder.encode(fullTrackName, "UTF-8"));
        buff.append("&");
        buff.append("access_token=");
        buff.append("...");
        String connectToUrl = buff.toString();
        System.out.println(URLDecoder.decode(connectToUrl, "UTF-8"));

        /*
        Perform request to vk.com
        */
        URL url = new URL(connectToUrl);
        connection = (HttpURLConnection)url.openConnection();

        /*
        Get and parse XML from vk.com
        */
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document document;
        try (InputStream inputStream = connection.getInputStream()) {
            document = docBuilder.parse(inputStream, "UTF-8");
            document.getDocumentElement().normalize();
        }

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xPathEvaluator = xpf.newXPath();
        XPathExpression nameExpr = xPathEvaluator
                .compile("response/audio/url");
        NodeList urlNodes = (NodeList) nameExpr.evaluate(document,
                XPathConstants.NODESET);

        String URLOfFile;
        if (urlNodes.getLength() != 0) {
            Node trackNameNode = urlNodes.item(0);
            URLOfFile = trackNameNode.getTextContent().trim();
            int index = URLOfFile.indexOf("?extra");
            URLOfFile = URLOfFile.substring(0, index);
            return URLOfFile;
        } else {
            return null;
        }
    }
}
