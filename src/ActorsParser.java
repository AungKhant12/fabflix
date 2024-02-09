import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class ActorsParser extends DefaultHandler {
    Map<String, Star> starMap;
    List<String> dupActorList = new ArrayList<String>();

    private String newVal;
    private String tempName;
    private String tempYear;
    private Star   newStar;

    //---[ Constructors ]------------------------------------------------------
    public ActorsParser() { starMap  = new HashMap<String, Star>(); }
    //---[ Constructors ]------------------------------------------------------


    //---[ Accessors ]---------------------------------------------------------
    public Map<String, Star> getStarMap() { return starMap; }
    public int getNumNewActors() { return starMap.size(); }
    //---[ Accessors ]---------------------------------------------------------

    //---[ Parsing Methods ]---------------------------------------------------
    public void parseDocument() {
        final String FUNC = "parseDocument";
        //log(FUNC,"Entered Method");

        final String DOC_NAME = "actors63.xml";

        SAXParserFactory spf = SAXParserFactory.newInstance();  // gets factory

        try {
            SAXParser sp = spf.newSAXParser(); // gets new parser instance
            //log(FUNC,"Created new parser instance");

            sp.parse(DOC_NAME, this);      // parses file, registers class for callbacks
            //log(FUNC, "Successfully parsed " + DOC_NAME);
        }
        catch (SAXException se) {
            se.printStackTrace();
        }
        catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        catch (IOException ie) {
            ie.printStackTrace();
        }
        catch (NumberFormatException nfe) {
            nfe.printStackTrace();

            log(FUNC, "NFE exception. Got through " + Integer.toString(starMap.size()) + " actors");
        }
    }
    private void printData() {
        final String FUNC = "printData";
        //log(FUNC, "Entered method");

        for (String actorName : starMap.keySet()) {
            System.out.println(starMap.get(actorName).toString());
        }
    }
    //---[ Parsing Methods ]---------------------------------------------------

    //---[ SAX Parser Methods ]------------------------------------------------
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        final String FUNC = "startElement";

        newVal = "";
        if (qName.equalsIgnoreCase("actor")) {
            tempName = "";
            tempYear = "";
            newStar = new Star();
        }
    }
    public void characters(char[] ch, int start, int length) throws SAXException {
        newVal = new String(ch, start, length);
    }
    public void endElement(String uri, String localName, String qName) throws SAXException {
        final String FUNC = "endElement";

        switch (qName.toLowerCase()) {
        // furnish Star instance
        case "stagename":
            tempName = newVal.strip();
            break;
        case "dob":
            tempYear = newVal.strip();
            break;
        // finish furnishing star instance
        case "actor":
            if (!starMap.containsKey(tempName)) {
                newStar.setName(tempName);

                try { newStar.setBirthYear(tempYear); }
                catch (Exception e) {}

                starMap.put(tempName, newStar);
            }
            else {
                dupActorList.add(tempName);
            }
        }
    }
    //---[ SAX Parser Methods ]------------------------------------------------


    //---[ Parser Testing ]----------------------------------------------------
    public static void main(String[] args) {
        ActorsParser parser = new ActorsParser();

        parser.parseDocument();
        parser.printData();
    }
    //---[ Parser Testing ]----------------------------------------------------


    //---[ Logging Methods ]---------------------------------------------------
    public void log(String func, String msg) {
        final String MODULE = "ActorsParser";
        System.out.println(String.format("%s - %s: %s", MODULE, func, msg));
    }
    //---[ Logging Methods ]---------------------------------------------------
}
