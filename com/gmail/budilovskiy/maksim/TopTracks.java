/**
 * class gets list of top tracks from last.fm number of tracks sets in
 * LAST_FM_LIMIT_OF_TRACKS value; method (tag, artist, etc.) sets in
 * LAST_FM_API_METHOD value;
 */
package com.gmail.budilovskiy.maksim;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
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

public class TopTracks {

    private static final String LAST_FM_API_KEY = "...";
    private static final String LAST_FM_LIMIT_OF_TRACKS = "100";

    private List<Track> topTracks = null;
    private String connectToUrl;

    public TopTracks(String searchString, String method) {

        StringBuffer buffer = buildConnectToURL(searchString, method);
        connectToUrl = buffer.toString();

        try {
            System.out.println(URLDecoder.decode(connectToUrl, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TopTracks.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            topTracks = getTopTracksFromLastFm(connectToUrl);
        } catch (IOException | XPathExpressionException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(TopTracks.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Unable to get data from last.fm");
        }

    }

    private StringBuffer buildConnectToURL(String searchString, String method) {
        try {
            searchString = URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TopTracks.class.getName()).log(Level.SEVERE, null, ex);
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("http://ws.audioscrobbler.com/2.0/");
        buffer.append("?method=");
        buffer.append(method);
        switch (method) {
            case "tag.gettoptracks":
                buffer.append("&tag=");
                buffer.append(searchString);
                break;
            case "artist.gettoptracks":
                buffer.append("&artist=");
                buffer.append(searchString);
                break;
        }
        buffer.append("&limit=");
        buffer.append(LAST_FM_LIMIT_OF_TRACKS);
        buffer.append("&api_key=");
        buffer.append(LAST_FM_API_KEY);
        return buffer;
    }

    /* getter */
    public List<Track> getTopTracks() {
        if (topTracks.isEmpty()) {
            return null;
        }
        return topTracks;
    }

    /**
     * get and parse XML from Last.fm topTracks (LAST_FM_LIMIT_OF_TRACKS) tracks
     * by tag and returns List of Tracks
     *
     * @param connectToUrl
     * @return list of top /LAST_FM_LIMIT_OF_TRACKS/ tracks from Last.fm
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    private List<Track> getTopTracksFromLastFm(String connectToUrl) throws IOException,
            ParserConfigurationException, SAXException,
            XPathExpressionException {

        topTracks = new ArrayList<>();

        URL url = new URL(connectToUrl);
        URLConnection connection = url.openConnection();

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        try (InputStream inputStream = connection.getInputStream()) {
            Document document = docBuilder.parse(inputStream, "UTF-8");
            document.getDocumentElement().normalize();

            XPathFactory xpf = XPathFactory.newInstance();
            XPath xPathEvaluator = xpf.newXPath();
            XPathExpression nameExpr = xPathEvaluator
                    .compile("lfm/toptracks/track/name");

            NodeList trackNameNodes = (NodeList) nameExpr.evaluate(document,
                    XPathConstants.NODESET);
            for (int i = 0; i < trackNameNodes.getLength(); i++) {
                Node trackNameNode = trackNameNodes.item(i);
                String trackName = trackNameNode.getTextContent();
                XPathExpression artistNameExpr = xPathEvaluator
                        .compile("following-sibling::artist/name");
                NodeList artistNameNodes = (NodeList) artistNameExpr.evaluate(
                        trackNameNode, XPathConstants.NODESET);
                for (int j = 0; j < artistNameNodes.getLength(); j++) {
                    String artistName = artistNameNodes.item(j)
                            .getTextContent();
                    topTracks.add(new Track(artistName, trackName));
                }
            }
        }
        return topTracks;
    }
}
