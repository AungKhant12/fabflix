import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

public class MoviesParser extends DefaultHandler {
//    List<Movie> movieList;
    Map<String, Movie> movieMap;
    Map<String, Star> starMap;
    Set<String> genreSet;

    Map<String, String> genreMap;

    private String newVal;
    private String directorName;
    private Movie  newMovie;
    private boolean directorNameTagOpenSeen = false;

    // Tracking inconsistent data
    private int dirCount = 0;
    private int newMovieCount = 0;

    private int moviesWithoutStarsCount = 0;
    private int invalidFilmCount = 0;
    private int numStarsNotFound = 0;

    //---[ Constructors ]------------------------------------------------------
    public MoviesParser() {
//        movieList = new ArrayList<Movie>();
        movieMap = new HashMap<String, Movie>();
        starMap = new HashMap<String, Star>();
        genreSet = new HashSet<String>();
        genreMap = new HashMap<String, String>();

        setGenreMap();
    }
    //---[ Constructors ]------------------------------------------------------


    //---[ Accessors ]---------------------------------------------------------
    public int getNewMovieCount() { return newMovieCount; }
    public int getMovieCount() { return movieMap.size(); }
    public int getMoviesWithoutStarsCount() { return moviesWithoutStarsCount; }
    public int getGenreCount() { return genreSet.size(); }
    public Set<String> getGenreSet() { return genreSet; }
    public int getInvalidFilmCount() { return invalidFilmCount; }
    public int getNumStarsNotFound() { return numStarsNotFound; }

    public Map<String, Movie> getMovieMap() { return movieMap; }
    //---[ Accessors ]---------------------------------------------------------

    public void setGenreMap() {
        genreMap.put("advt", "Adventure");
        genreMap.put("anti-dram", "Drama");
        genreMap.put("bio", "Biography");
        genreMap.put("biop", "Biography");
        genreMap.put("cnr", "Cops and Robbers");
        genreMap.put("comd", "Comedy");
        genreMap.put("docu", "Documentary");
        genreMap.put("dram", "Drama");
        genreMap.put("dramd", "Drama");
        genreMap.put("dram>", "Drama");
        genreMap.put("horr", "Horror");
        genreMap.put("musc", "Musical");
        genreMap.put("myst", "Mystery");
        genreMap.put("porb", "Pornography");
        genreMap.put("porn", "Pornography");
        genreMap.put("road", "Drama");
        genreMap.put("romt", "Romance");
        genreMap.put("susp", "Thriller");
        genreMap.put("s.f.", "Sci-Fi");
        genreMap.put("west", "Western");
        genreMap.put("actn", "Action");
        genreMap.put("fant", "Fantasy");
        genreMap.put("scfi", "Sci-Fi");
        genreMap.put("cart", "Cartoon");
        genreMap.put("hist", "Historical");
        genreMap.put("epic", "Epic");
        genreMap.put("cnrb", "Cops and Robbers");
        genreMap.put("surr", "Surreal");
        genreMap.put("crim", "Crime");
        genreMap.put("cond", "Crime");
        genreMap.put("noir", "Noire");
        genreMap.put("biog", "Biography");
        genreMap.put("disa", "Disaster");
        genreMap.put("west1", "Western");
        genreMap.put("adctx", "Adventure");
        genreMap.put("txx", "");
        genreMap.put("camp", "Camp");
        genreMap.put("scif", "Sci-Fi");
        genreMap.put("surl", "Surreal");
        genreMap.put("surreal", "Surreal");
        genreMap.put("romtadvt", "Adventure");
        genreMap.put("ctxxx", "");
        genreMap.put("stage", "Stage");
        genreMap.put("musical", "Musical");
        genreMap.put("muusc", "Music");
        genreMap.put("avant", "Avant-garde");
        genreMap.put("garde", "Avant-garde");
        genreMap.put("cnrbb", "");
        genreMap.put("draam", "Drama");
        genreMap.put("ram", "Romance");
        genreMap.put("ctcxx", "");
        genreMap.put("dramn", "Drama");
        genreMap.put("psych", "Psychological");
        genreMap.put("ctxx", "");
        genreMap.put("comdx", "Comedy");
        genreMap.put("homo", "");
        genreMap.put("muscl", "Musical");
        genreMap.put("cmr", "");
        genreMap.put("duco", "");
        genreMap.put("h", "Drama");
        genreMap.put("fanth*", "Fantasy");
        genreMap.put("rfp;", "");
        genreMap.put("h*", "");
        genreMap.put("natu", "Nature");
        genreMap.put("tv", "TV");
        genreMap.put("scat", "");
        genreMap.put("drama", "Drama");
        genreMap.put("biopp", "Biography");
        genreMap.put("mystp", "Mystery");
        genreMap.put("", "");
        genreMap.put("h**", "Thriller");
        genreMap.put("avga", "Avant-garde");
        genreMap.put("h0", "");
        genreMap.put("weird", "Weird");
        genreMap.put("cult", "Cult");
        genreMap.put("ront", "");
        genreMap.put("sctn", "Scientific");
        genreMap.put("dist", "");
        genreMap.put("hor", "Horror");
        genreMap.put("sports", "Sports");
        genreMap.put("psyc", "Psychological");
        genreMap.put("adct", "");
        genreMap.put("viol", "Violence");
        genreMap.put("ca", "");
        genreMap.put("biopx", "Biography");
        genreMap.put("axtn", "");
        genreMap.put("sxfi", "Sci-Fi");
        genreMap.put("biob", "Biography");
        genreMap.put("kinky", "Kinky");
        genreMap.put("tvmini", "TV");
        genreMap.put("ducu", "Documentary");
        genreMap.put("romtx", "Romance");
        genreMap.put("sati", "Satire");
        genreMap.put("dicu", "");
        genreMap.put("faml", "Family");
        genreMap.put("undr", "Adventure");
        genreMap.put("expm", "Experimental");
        genreMap.put("art", "Art");
        genreMap.put("video", "Video");
        genreMap.put("allegory", "Allegory");
        genreMap.put("romt.", "Romance");
        genreMap.put("dram.actn", "Action");
        genreMap.put("verite", "Drama");
        genreMap.put("act", "Action");
    }

    //---[ Parsing Methods ]---------------------------------------------------
    public void parseDocument() {
        final String FUNC = "parseDocument";
        //log(FUNC,"Entered Method");

        SAXParserFactory spf = SAXParserFactory.newInstance();  // gets factory
        // log(FUNC, "Created parser factory");

        try {
            SAXParser sp = spf.newSAXParser(); // gets new parser instance
            // log(FUNC,"Created new parser instance");

            final String DOC_NAME = "mains243.xml";
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

            log(FUNC, "NFE exception. Got through " + Integer.toString(movieMap.size()) + " movies");
        }
    }
    private void printData() {
        final String FUNC = "printData";
        log(FUNC, "Entered method");

//        Iterator<Movie> iterator = movieList.iterator();
//        while (iterator.hasNext()) {
//            System.out.println(iterator.next().toString());
//        }

        for (String id : movieMap.keySet()) {
            System.out.println(movieMap.get(id).toString());
        }

        log(FUNC, "Director count: " + dirCount);
        log(FUNC, "Movie harvest count: " + movieMap.size());
    }
    //---[ Parsing Methods ]---------------------------------------------------


    //---[ SAX Parser Methods ]------------------------------------------------
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        final String FUNC = "startElement";
        //log(FUNC, "Entered method");

        newVal = "";
        boolean newDirectorFound = qName.equals("director");
        if (newDirectorFound) {
            directorNameTagOpenSeen = true;
        }
        boolean directorFound = qName.equals("dirname") || qName.equals("dirn");
        if (directorFound) {
            if (directorNameTagOpenSeen) {
                // log(FUNC, "New director found");
                directorName = "";
                dirCount++;
            }
        }
        else if (qName.equalsIgnoreCase("film")) {
            // log(FUNC, "New Movie found");
            newMovie = new Movie();
        }

        //log(FUNC, "Exiting method\n");
    }
    public void characters(char[] ch, int start, int length) throws SAXException {
        final String FUNC = "characters";
        //log(FUNC, "Entered method");
        //log(FUNC, String.format("start: %d, length: %d", start, length));

        newVal = new String(ch, start, length);

        //log(FUNC, "Value harvested: " + newVal);
    }
    public void endElement(String uri, String localName, String qName) throws SAXException {
        final String FUNC = "endElement";

        switch (qName.toLowerCase()) {
            // found new director
            case "director":
                directorNameTagOpenSeen = false;
                break;
            case "dirname":
            case "dirn":
                if (directorName.isEmpty()) { directorName = newVal; }
                break;
            // furnish movie instance
            case "fid":
                newMovie.setMovieId(newVal);
                break;
            case "t":
                newMovie.setMovieTitle(newVal);
                break;
            case "year":
                try {
                    newMovie.setMovieYear(Integer.parseInt(newVal));
                }
                catch (Exception e) {}
                break;
            case "cat":
                newVal = newVal.toLowerCase().strip();

                String[] parsedGenres = newVal.split(" ");
                for (String newGenre : parsedGenres) {
                    if (genreMap.containsKey(newGenre)) {
                        newVal = genreMap.get(newGenre);

                        if (!newVal.isEmpty())
                            genreSet.add(newVal);
                    }
                    else {
                        System.out.println("genreMap does not contain: " + newGenre);
                    }

                    newMovie.addGenre(newVal);
                }

                break;
            // finish furnishing movie instance
            case "film":
                newMovie.setMovieDirector(directorName);

                movieMap.put(newMovie.getMovieId(), newMovie);

                newMovieCount++;
        }
    }
    //---[ SAX Parser Methods ]------------------------------------------------


    //---[ Helper Methods ]----------------------------------------------------
    public String cleanYear(String year) {
        String newYear = "";
        char glyph;

        for (int i = 0; i < year.length(); i++) {
            glyph = year.charAt(i);

            if (glyph < '0' || glyph > '9') { glyph = '0'; }

            newYear += glyph;
        }

        return newYear;
    }

    public boolean isValidFilm(Movie movie) {
        if (movie.getMovieTitle().isBlank()) {
            return false;
        }
        else if (movie.getMovieDirector().isBlank()) {
            return false;
        }
        else if (movie.getMovieYear() == 0) {
            return false;
        }

        return true;
    }
    //---[ Helper Methods ]----------------------------------------------------


    //---[ Parser Testing ]----------------------------------------------------
    public static void main(String[] args) {
        MoviesParser mp = new MoviesParser();

        mp.parseDocument();
        mp.printData();
    }
    //---[ Parser Testing ]----------------------------------------------------


    //---[ Logging Methods ]---------------------------------------------------
    public void log(String func, String msg) {
        final String MODULE = "MovieParser";
        System.out.println(String.format("%s - %s: %s", MODULE, func, msg));
    }
    //---[ Logging Methods ]---------------------------------------------------
}
