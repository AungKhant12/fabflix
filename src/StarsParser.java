import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import java.util.Set;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class StarsParser extends DefaultHandler {
    Map<String, Star> starMap;

    Set<String> missingStarSet = new HashSet<String>();
    Set<String> moviesWithStars = new HashSet<String>();

    private String newVal;
    private String tempName;
    private String tempMovieId;
    private Star   newStar;

    int starsNotInActors = 0;
    int numMovieDataAdded = 0;

    // Constructor
    public StarsParser() {
        starMap = new HashMap<String, Star>();
    }

    public Map<String, Star> getStarMap() { return starMap; }

    public int getNumMovieDataAdded() { return numMovieDataAdded; }
    public int getStarsNotInActors() { return starsNotInActors; }

    public Set<String> getMoviesWithStars() { return moviesWithStars; }

    // Mutators
    public void setStarMap(Map<String, Star> starMap) {
        this.starMap = starMap;
    }

    public static void main(String[] args) {
        StarsParser parser = new StarsParser();

        parser.parseDocument();
        parser.printData();
    }

    //---[ Main Methods ]------------------------------------------------------
    public void parseDocument() {
        final String FUNC = "parseDocument";
        //log(FUNC,"Entered Method");

        SAXParserFactory spf = SAXParserFactory.newInstance();  // gets factory

        try {
            SAXParser sp = spf.newSAXParser(); // gets new parser instance
            //log(FUNC,"Created new parser instance");

            final String DOC_NAME = "casts124.xml";
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

            log(FUNC, "NFE exception. Got through " + Integer.toString(starMap.size()) + " movies");
        }
    }
    public void printData() {
        final String FUNC = "printData";
        log(FUNC, "Entered method");

        for (String starName : starMap.keySet()) {
            System.out.println(starMap.get(starName).toString());
        }

        log(FUNC, "Number of movies assigned to stars: " + numMovieDataAdded);
    }
    //---[ Main Methods ]------------------------------------------------------


    //---[ SAX Parser Methods ]------------------------------------------------
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        newVal = "";
        if (qName.equalsIgnoreCase("m")) {
            tempName = "";
            tempMovieId = "";
            newStar = new Star();
        }
    }
    public void characters(char[] ch, int start, int length) throws SAXException {
        newVal = new String(ch, start, length);
    }
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // furnish Star instance
        switch (qName.toLowerCase()) {
            case "f":
                tempMovieId = newVal;
                moviesWithStars.add(tempMovieId);
                break;
            case "a":
                tempName = newVal;
                break;
            // finish furnishing star instance
            case "m":
                addStarIfNeeded();
        }
    }
    //---[ SAX Parser Methods ]------------------------------------------------


    //---[ Helper Methods ]----------------------------------------------------
    public void addStarIfNeeded() {
        // check if star name already seen
        if (starMap.containsKey(tempName)) {
            Star seenStar = starMap.get(tempName);
            seenStar.addMovieId(tempMovieId);
            starMap.put(tempName, seenStar);

            numMovieDataAdded++;
        }
        else if (notSAStar(tempName)) {
            missingStarSet.add(tempName);
            starsNotInActors++;
        }
    }

    public boolean notSAStar(String starName) {
        return !(
            starName.equals("s a") || starName.equals("s.a.") || starName.equals("sa")
        );
    }
    //---[ Helper Methods ]----------------------------------------------------


    //---[ Logging ]-----------------------------------------------------------
    public void log(String func, String msg) {
        final String MODULE = "StarsParser";
        System.out.println(String.format("%s - %s: %s", MODULE, func, msg));
    }
    //---[ Logging ]-----------------------------------------------------------
}
