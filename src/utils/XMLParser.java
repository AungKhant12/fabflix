import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.x.protobuf.MysqlxPrepare;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class XMLParser {
    // Parsers
    private MoviesParser moviesParser;
    private StarsParser  starsParser;
    private ActorsParser actorsParser;

    // Data from parsers
    private Map<String, Integer> genreMap;
    private Map<String, Star>  starMap;
    private Map<String, Movie> movieMap;

    // Data for Threads
    private Map<String, Star>  starMap1 = new HashMap<String, Star>();
    private Map<String, Star>  starMap2 = new HashMap<String, Star>();;
    private Map<String, Star>  starMap3 = new HashMap<String, Star>();;
    private Map<String, Star>  starMap4 = new HashMap<String, Star>();;
    private Map<String, Star>  starMap5 = new HashMap<String, Star>();

    private Map<String, Movie> movieMap1 = new HashMap<String, Movie>();
    private Map<String, Movie> movieMap2 = new HashMap<String, Movie>();;
    private Map<String, Movie> movieMap3 = new HashMap<String, Movie>();
    private Map<String, Movie> movieMap4 = new HashMap<String, Movie>();;
    private Map<String, Movie> movieMap5 = new HashMap<String, Movie>();;

    public void addToStarMap(Map<String, Star> newMap) {
        for (String starName : newMap.keySet()) {
            starMap.put(starName, newMap.get(starName));
        }
    }

    public void addToMovieMap(Map<String, Movie> newMap) {
        for (String movieId : newMap.keySet()) {
            movieMap.put(movieId, newMap.get(movieId));
        }
    }

    public void splitStarMap() {
        int i = 0;

        for (String starName : starMap.keySet()) {
            if (i % 5 == 0) { starMap1.put(starName, starMap.get(starName)); }
            else if (i % 5 == 1) { starMap2.put(starName, starMap.get(starName)); }
            else if (i % 5 == 2) { starMap3.put(starName, starMap.get(starName)); }
            else if (i % 5 == 3) { starMap4.put(starName, starMap.get(starName)); }
            else if (i % 5 == 4) { starMap5.put(starName, starMap.get(starName)); }

            i++;
        }
    }

    public void splitMovieMap() {
        int i = 0;

        for (String movieId : movieMap.keySet()) {
            if (i % 5 == 0) { movieMap1.put(movieId, movieMap.get(movieId)); }
            else if (i % 5 == 1) { movieMap2.put(movieId, movieMap.get(movieId)); }
            else if (i % 5 == 2) { movieMap3.put(movieId, movieMap.get(movieId)); }
            else if (i % 5 == 3) { movieMap4.put(movieId, movieMap.get(movieId)); }
            else if (i % 5 == 4) { movieMap5.put(movieId, movieMap.get(movieId)); }

            i++;
        }
    }

    // IDs for inserting into tables
    private int newestGenreId;
    private int newestStarId;
    private int newestMovieId;

    // Data for analysis
    int numStars = 0;
    int numGenres = 0;
    int numMovies = 0;
    int numStarsInMovies = 0;
    int numGenresInMovies = 0;
    int numInconsistentMovies = 0;
    int numDuplicateMovies = 0;
    int numMoviesMissing = 0;
    int numMoviesWithoutStars = 0;
    int numDuplicateStars = 0;
    int numStarsNotFound = 0;


    //---[ Main Method ]-------------------------------------------------------
    public static void main(String[] args)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
    {
        final String FUNC = "main";

        // Connect to MySQL
        String username = "mytestuser";
        String password = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, username, password);
        connection.setAutoCommit(false);

        // Parse XML files
        XMLParser xmlParser = new XMLParser();

        // Calc stats & insert into tables
        xmlParser.setNewestGenreId(connection);
        xmlParser.setNewestStarId(connection);
        xmlParser.setNewestMovieId(connection);
        xmlParser.log(FUNC, "Finished setting new IDs for star, movie, & genre insertions");

        xmlParser.splitStarMap();
        xmlParser.splitMovieMap();
        xmlParser.log(FUNC, "Finished splitting maps for 3 threads");

        xmlParser.insertGenres(connection);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        StarWorker starWorker1 = new StarWorker(connection, (HashMap<String, Star>)xmlParser.starMap1, xmlParser);
        StarWorker starWorker2 = new StarWorker(connection, (HashMap<String, Star>)xmlParser.starMap2, xmlParser);
        StarWorker starWorker3 = new StarWorker(connection, (HashMap<String, Star>)xmlParser.starMap3, xmlParser);
        StarWorker starWorker4 = new StarWorker(connection, (HashMap<String, Star>)xmlParser.starMap4, xmlParser);
        StarWorker starWorker5 = new StarWorker(connection, (HashMap<String, Star>)xmlParser.starMap5, xmlParser);

        MovieWorker movieWorker1 = new MovieWorker(connection, (HashMap<String, Movie>)xmlParser.movieMap1, xmlParser);
        MovieWorker movieWorker2 = new MovieWorker(connection, (HashMap<String, Movie>)xmlParser.movieMap2, xmlParser);
        MovieWorker movieWorker3 = new MovieWorker(connection, (HashMap<String, Movie>)xmlParser.movieMap3, xmlParser);
        MovieWorker movieWorker4 = new MovieWorker(connection, (HashMap<String, Movie>)xmlParser.movieMap4, xmlParser);
        MovieWorker movieWorker5 = new MovieWorker(connection, (HashMap<String, Movie>)xmlParser.movieMap5, xmlParser);

        executor.execute(starWorker1);
        executor.execute(starWorker2);
        executor.execute(starWorker3);
        executor.execute(starWorker4);
        executor.execute(starWorker5);
        executor.execute(movieWorker1);
        executor.execute(movieWorker2);
        executor.execute(movieWorker3);
        executor.execute(movieWorker4);
        executor.execute(movieWorker5);

        executor.shutdown();
        while (!executor.isTerminated()) {}

//        xmlParser.insertMovies(connection);
//        xmlParser.insertStars(connection);
        xmlParser.log(FUNC, "Finished inserting into stars, movies, & genres tables");

        executor = Executors.newFixedThreadPool(10);

        SIMWorker simWorker1 = new SIMWorker(connection, (HashMap<String, Star>)xmlParser.starMap1, (HashMap<String, Movie>)xmlParser.movieMap, xmlParser);
        SIMWorker simWorker2 = new SIMWorker(connection, (HashMap<String, Star>)xmlParser.starMap2, (HashMap<String, Movie>)xmlParser.movieMap, xmlParser);
        SIMWorker simWorker3 = new SIMWorker(connection, (HashMap<String, Star>)xmlParser.starMap3, (HashMap<String, Movie>)xmlParser.movieMap, xmlParser);
        SIMWorker simWorker4 = new SIMWorker(connection, (HashMap<String, Star>)xmlParser.starMap4, (HashMap<String, Movie>)xmlParser.movieMap, xmlParser);
        SIMWorker simWorker5 = new SIMWorker(connection, (HashMap<String, Star>)xmlParser.starMap5, (HashMap<String, Movie>)xmlParser.movieMap, xmlParser);

        GIMWorker gimWorker1 = new GIMWorker(connection, (HashMap<String, Integer>)xmlParser.genreMap, (HashMap<String, Movie>)xmlParser.movieMap1, xmlParser);
        GIMWorker gimWorker2 = new GIMWorker(connection, (HashMap<String, Integer>)xmlParser.genreMap, (HashMap<String, Movie>)xmlParser.movieMap2, xmlParser);
        GIMWorker gimWorker3 = new GIMWorker(connection, (HashMap<String, Integer>)xmlParser.genreMap, (HashMap<String, Movie>)xmlParser.movieMap3, xmlParser);
        GIMWorker gimWorker4 = new GIMWorker(connection, (HashMap<String, Integer>)xmlParser.genreMap, (HashMap<String, Movie>)xmlParser.movieMap4, xmlParser);
        GIMWorker gimWorker5 = new GIMWorker(connection, (HashMap<String, Integer>)xmlParser.genreMap, (HashMap<String, Movie>)xmlParser.movieMap5, xmlParser);

        executor.execute(simWorker1);
        executor.execute(simWorker2);
        executor.execute(simWorker3);
        executor.execute(simWorker4);
        executor.execute(simWorker5);

        executor.execute(gimWorker1);
        executor.execute(gimWorker2);
        executor.execute(gimWorker3);
        executor.execute(gimWorker4);
        executor.execute(gimWorker5);

        executor.shutdown();
        while (!executor.isTerminated()) {}

//        xmlParser.insertGIM(connection);
//        xmlParser.insertSIM(connection);
        xmlParser.log(FUNC, "Finished inserting into gim & sim tables");

        xmlParser.getMoviesWithoutStars();

        xmlParser.showStats();
        // TODO: only uncomment after testing
        connection.commit();
        connection.close();
    }
    //---[ Main Method ]-------------------------------------------------------

    //---[ Constructor ]-------------------------------------------------------
    public XMLParser() {
        final String FUNC = "Constructor";

        actorsParser = new ActorsParser();
        starsParser  = new StarsParser();
        moviesParser = new MoviesParser();

        actorsParser.parseDocument();
        starsParser.setStarMap(actorsParser.getStarMap());
        starsParser.parseDocument();

        moviesParser.parseDocument();
        log(FUNC, "Finished parsing XML files");

        setMaps();
        fillStats();
    }
    //---[ Constructor ]-------------------------------------------------------


    //---[ Mutators ]----------------------------------------------------------
    private void setMaps() {
        genreMap = new HashMap<String, Integer>();
        starMap = starsParser.getStarMap();
        movieMap = moviesParser.getMovieMap();
    }
    private void fillStats() {
        numStars = 0;
        numGenres = 0;
        numMovies = 0;

        numStarsInMovies = 0;

        numInconsistentMovies = getMoviesMissingFields();
        numStarsNotFound = starsParser.getStarsNotInActors();
    }
    //---[ Mutators ]----------------------------------------------------------




    //---[ Helper Methods ]----------------------------------------------------
    public void setNewestGenreId(Connection connection) {
        Set<String> genreSet = moviesParser.getGenreSet();

        // Gets last id in genre table
        String query = "select max(id) maxId from genres;";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            newestGenreId = resultSet.getInt("maxId") + 1;
        }
        catch (Exception e) {}
    }
    public void setNewestStarId(Connection connection) {
        String FUNC = "setNewestStarId";
        String query = "select max(id) maxId from stars;";
        String temp = "";

        // Gets last id in stars table
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            temp = resultSet.getString("maxId");
            newestStarId = Integer.parseInt(temp.substring(2)) + 1;
        }
        catch (Exception e) {}
    }
    public void setNewestMovieId(Connection connection) {
        String FUNC = "setNewestMovieId";
        String query = "select max(id) maxId from movies;";
        String temp = "";

        // Gets last id in movies table
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            temp = resultSet.getString("maxId");
            newestMovieId = Integer.parseInt(temp.substring(2)) + 1;
        }
        catch (Exception e) {}
    }

    public int calcNumStarsInMovies() {
        numStarsInMovies = 0;

        for (Star s : starMap.values()) {
            for (String movieId : s.getMovieIdSet()) {
                if (movieMap.containsKey(movieId)) {
                    numStarsInMovies++;
                }
            }
        }

        return numStarsInMovies;
    }

    public int getMoviesWithoutStars() {
        List<String> movieIdWithoutStars = new ArrayList<String>();
        int numMoviesMiss = 0;
        int numMoviesNoStars = 0;

        for (Star star : starMap.values()) {
            for (String movieId : star.getMovieIdSet()) {
                if (movieMap.containsKey(movieId)) {
                    Movie temp = movieMap.get(movieId);
                    temp.foundStar();
                }
                else {
                    numMoviesMiss++;
                }
            }
        }

        for (Movie movie : movieMap.values()) {
            if (!movie.getHasStars()) {
                numMoviesNoStars++;
            }
        }

        System.out.println("Movies not found: " + numMoviesMiss);
        numMoviesMissing = numMoviesMiss;
        numMoviesWithoutStars = numMoviesNoStars;
        System.out.println("Movies w/o stars: " + numMoviesNoStars);

        return movieIdWithoutStars.size();
    }

    public int insertGenres(Connection connection) throws SQLException {
        String FUNC = "getNumExistingStars";
        Set<String> genreSet = moviesParser.getGenreSet();

        String genresQuery = "SELECT id, name FROM genres;";
        String genreInsertQuery = "INSERT INTO genres (id, name) VALUES (?, ?);";

        // gets existing genres
        HashSet<String> genres = new HashSet<String>();
        PreparedStatement statement = connection.prepareStatement(genresQuery);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            String genreName = resultSet.getString("name");
            int genreId = resultSet.getInt("id");
            genres.add(genreName);
            genreMap.put(genreName, genreId);
        }

        statement = connection.prepareStatement(genreInsertQuery);

        for (String newGenre : genreSet) {
            if (!genres.contains(newGenre)) {

                statement.setInt(1, newestGenreId);
                statement.setString(2, newGenre);

                genreMap.put(newGenre, newestGenreId);
                statement.addBatch();

                newestGenreId++;
                numGenres++;
            }
        }
        statement.executeBatch();

        resultSet.close();
        statement.close();

        return numGenres;
    }
    public int insertMovies(Connection connection) throws SQLException {
        final String FUNC = "insertMovies";
        String movieInsertQuery = "INSERT INTO movies (id, title, year, director) VALUES (? ,?, ?, ?);";
        String ratingInsertQuery = "INSERT INTO ratings (movieId, rating, numVotes) VALUES (?, ?, ?)";
        HashSet<String> existingMovies = new HashSet<String>();

        HashMap<String, Movie> newMap = new HashMap<String, Movie>();

        // get existing titles of movies
        String getMoviesQuery = "SELECT title FROM movies;";
        PreparedStatement statement = connection.prepareStatement(getMoviesQuery);
        ResultSet resultSet = statement.executeQuery();

        PreparedStatement ratingStatement = connection.prepareStatement(ratingInsertQuery);

        while (resultSet.next()) {
            existingMovies.add(resultSet.getString("title"));
        }

        statement = connection.prepareStatement(movieInsertQuery);

        for (Movie m : movieMap.values()) {
            if (!isValidFilm(m)) { continue; }
            else if (existingMovies.contains(m.getMovieTitle())) { numDuplicateMovies++; continue; }

            String newMovieId = "tt" + leftPad(Integer.toString(newestMovieId));
            m.setMovieIdDB(newMovieId);
            newMap.put(m.getMovieId(), m);

            statement.setString(1, newMovieId);
            statement.setString(2, m.getMovieTitle());
            statement.setInt(3, m.getMovieYear());
            statement.setString(4, m.getMovieDirector());

            ratingStatement.setString(1, newMovieId);
            ratingStatement.setFloat(2, 0.0f);
            ratingStatement.setInt(3, 0);

             statement.addBatch();
             ratingStatement.addBatch();

            newestMovieId++;
            numMovies++;
        }

        statement.executeBatch();
        ratingStatement.executeBatch();

        resultSet.close();
        statement.close();

        movieMap = newMap;

        return numMovies;
    }
    public void insertGIM(Connection connection) throws SQLException {
        final String FUNC = "insertGIM";

        String gimInsertQuery = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?);";
        PreparedStatement statement = connection.prepareStatement(gimInsertQuery);

        // insert movies & genres
        for (Movie m : movieMap.values()) {
            if (isValidFilm(m) && !m.getMovieIdDB().isBlank()) {
                List<String> genres = m.getGenres();

                for (String genre : genres) {
                    int genreId = genreMap.get(genre);

                    statement.setInt(1, genreId);
                    statement.setString(2, m.getMovieIdDB());

                    statement.addBatch();

                    numGenresInMovies++;
                }
            }
        }
        try {
            statement.executeBatch();
        }
        catch (BatchUpdateException bue) { System.out.println("problem in GIM. fix later"); }
        statement.close();
    }
    public void insertStars(Connection connection) throws SQLException {
        String insertStarsQuery = "INSERT INTO stars (id, name, birthYear) VALUES (? ,?, ?);";
        HashSet<String> existingStars = new HashSet<String>();

        HashMap<String, Star> newMap = new HashMap<String, Star>();

        // get existing stars' names
        String getStarsQuery = "SELECT name FROM stars;";
        PreparedStatement statement = connection.prepareStatement(getStarsQuery);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            existingStars.add(resultSet.getString("name"));
        }

        // add new stars
        statement = connection.prepareStatement(insertStarsQuery);
        for (Star s : starMap.values()) {
            if (existingStars.contains(s.getName())) { numDuplicateStars++; continue; }

            String newStarId = "nm" + leftPad(Integer.toString(newestStarId));
            s.setDBID(newStarId);
            newMap.put(s.getName(), s);

            statement.setString(1, newStarId);
            statement.setString(2, s.getName());

            if (s.getBirthYear() == -1) { statement.setNull(3, Types.INTEGER); }
            else { statement.setInt(3, s.getBirthYear()); }

             statement.addBatch();

            newestStarId++;
            numStars++;
        }
        statement.executeBatch();
        statement.close();

        starMap = newMap;
    }
    public void insertSIM(Connection connection) throws SQLException {

        String simInsertQuery = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?);";
        PreparedStatement statement = connection.prepareStatement(simInsertQuery);

        for (Star s : starMap.values()) {
            for (String movieId : s.getMovieIdSet()) {
                if (movieMap.containsKey(movieId)) {
                    Movie tempMovie = movieMap.get(movieId);

                    String starId    = s.getDBID();
                    String movieDBId = tempMovie.getMovieIdDB();

                    statement.setString(1, starId);
                    statement.setString(2, movieDBId);

                    statement.addBatch();

                    numStarsInMovies++;
                }
            }
        }
        try {
            statement.executeBatch();
        }
        catch (BatchUpdateException bue) {  }

        statement.close();
    }

    public int getMoviesMissingFields() {
        final String FUNC = "showMoviesMissingFields";
        int missing = 0;

        for (Movie m : movieMap.values()) {
            if (m.getMovieTitle().isBlank() || m.getMovieYear() == 0 || m.getMovieDirector().isBlank() || m.hasNoGenres()) {
                missing++;
            }
        }

        return missing;
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

    // used when inserting ID into movies table
    public String leftPad(String source) {
        int len = 7;

        while (source.length() < len) {
            source = "0" + source;
        }

        return source;
    }
    //---[ Helper Methods ]----------------------------------------------------


    //---[ Printing ]----------------------------------------------------------
    public void showStats() {
        final String FUNC = "showStats";

        log(FUNC, "Num stars: " + numStars);
        log(FUNC, "Num genres: " + numGenres);
        log(FUNC, "Num movies: " + numMovies);

        log(FUNC, "Num stars in movies: " + numStarsInMovies);
        log(FUNC, "Num genres in movies: " + numGenresInMovies);

        log(FUNC, "Num movies w/ missing fields: " + numInconsistentMovies);
        log(FUNC, "Num movies already in DB: " + numDuplicateMovies);
        log(FUNC, "Num stars already in DB: " + numDuplicateStars);
        log(FUNC, "Num stars not in actors file: " + numStarsNotFound);
    }
    //---[ Printing ]----------------------------------------------------------


    //---[ Logging ]-----------------------------------------------------------
    public void log(String func, String msg) {
        final String MODULE = "XMLParser";
        boolean logsEnabled = true;

        if (logsEnabled)
            System.out.println(String.format("%s - %s: %s", MODULE, func, msg));
    }
    //---[ Logging ]-----------------------------------------------------------



    //---[ StarWorker Class ]
    static class StarWorker implements Runnable {
        Connection connection;
        Map<String, Star> starMap;
        XMLParser xmlParser;

        public StarWorker(Connection c, HashMap<String, Star> sm, XMLParser xmlp) {
            this.connection = c;
            this.starMap = sm;

            this.xmlParser = xmlp;
        }

        @Override
        public void run() {
            try {
                String insertStarsQuery = "INSERT INTO stars (id, name, birthYear) VALUES (? ,?, ?);";
                HashMap<String, String> existingStars = new HashMap<String, String>();

                HashMap<String, Star> newMap = new HashMap<String, Star>();

                // get existing stars' names
                String getStarsQuery = "SELECT id, name FROM stars;";
                PreparedStatement statement = connection.prepareStatement(getStarsQuery);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    String dbId = resultSet.getString("id");
                    String dbName = resultSet.getString("name");

                    existingStars.put(dbName, dbId);
                }

                // add new stars
                statement = connection.prepareStatement(insertStarsQuery);
                for (Star s : starMap.values()) {
                    if (existingStars.containsKey(s.getName())) {
                        xmlParser.numDuplicateStars++;
                        s.setDBID(existingStars.get(s.getName()));
                        newMap.put(s.getName(), s);
                        continue;
                    }

                    String newStarId = "nm" + leftPad(Integer.toString(xmlParser.newestStarId));
                    s.setDBID(newStarId);
                    newMap.put(s.getName(), s);

                    statement.setString(1, newStarId);
                    statement.setString(2, s.getName());

                    if (s.getBirthYear() == -1) { statement.setNull(3, Types.INTEGER); }
                    else { statement.setInt(3, s.getBirthYear()); }

                    statement.addBatch();

                    xmlParser.newestStarId++;
                    xmlParser.numStars++;
                }
                statement.executeBatch();
                statement.close();

                xmlParser.addToStarMap(newMap);
                //starMap = newMap;
            }
            catch (SQLException se) {}
        }

        // used when inserting ID into movies table
        public String leftPad(String source) {
            int len = 7;

            while (source.length() < len) {
                source = "0" + source;
            }

            return source;
        }
    }

    //---[ MovieWorker Class ]
    static class MovieWorker implements Runnable {
        Connection connection;
        HashMap<String, Movie> movieMap;
        XMLParser xmlParser;

        MovieWorker(Connection c, HashMap<String, Movie> mm, XMLParser xmlp) {
            this.connection = c;
            this.movieMap = mm;

            this.xmlParser = xmlp;
        }

        @Override
        public void run() {
            try {
                final String FUNC = "insertMovies";
                String movieInsertQuery = "INSERT INTO movies (id, title, year, director) VALUES (? ,?, ?, ?);";
                String ratingInsertQuery = "INSERT INTO ratings (movieId, rating, numVotes) VALUES (?, ?, ?)";
                HashSet<String> existingMovies = new HashSet<String>();

                HashMap<String, Movie> newMap = new HashMap<String, Movie>();

                // get existing titles of movies
                String getMoviesQuery = "SELECT title FROM movies;";
                PreparedStatement statement = connection.prepareStatement(getMoviesQuery);
                ResultSet resultSet = statement.executeQuery();

                PreparedStatement ratingStatement = connection.prepareStatement(ratingInsertQuery);

                while (resultSet.next()) {
                    existingMovies.add(resultSet.getString("title"));
                }

                statement.close();
                statement = connection.prepareStatement(movieInsertQuery);

                for (Movie m : movieMap.values()) {
                    if (!xmlParser.isValidFilm(m)) { continue; }
                    else if (existingMovies.contains(m.getMovieTitle())) { xmlParser.numDuplicateMovies++; continue; }

                    String newMovieId = "tt" + xmlParser.leftPad(Integer.toString(xmlParser.newestMovieId));
                    m.setMovieIdDB(newMovieId);
                    newMap.put(m.getMovieId(), m);

                    statement.setString(1, newMovieId);
                    statement.setString(2, m.getMovieTitle());
                    statement.setInt(3, m.getMovieYear());
                    statement.setString(4, m.getMovieDirector());

                    ratingStatement.setString(1, newMovieId);
                    ratingStatement.setFloat(2, 0.0f);
                    ratingStatement.setInt(3, 0);

                    statement.addBatch();
                    ratingStatement.addBatch();

                    xmlParser.newestMovieId++;
                    xmlParser.numMovies++;
                }

                statement.executeBatch();
                ratingStatement.executeBatch();

                resultSet.close();
                statement.close();

                xmlParser.addToMovieMap(newMap);
                // movieMap = newMap;
            }
            catch (SQLException se) {}
        }
    }

    //---[ SIMWorker Class ]
    static class SIMWorker implements Runnable {
        Connection connection;
        Map<String, Star> starMap;
        Map<String, Movie> movieMap;
        XMLParser xmlParser;

        public SIMWorker(Connection conn, Map<String, Star> sm, Map<String, Movie> mm, XMLParser xmlp) {
            this.connection = conn;
            this.starMap = sm;
            this.movieMap = mm;
            this.xmlParser = xmlp;
        }

        @Override
        public void run() {
            try {

                String simInsertQuery = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?);";
                PreparedStatement statement = connection.prepareStatement(simInsertQuery);

                for (Star s : starMap.values()) {
                    for (String movieId : s.getMovieIdSet()) {
                        if (movieMap.containsKey(movieId)) {
                            Movie tempMovie = movieMap.get(movieId);

                            String starId    = s.getDBID();
                            String movieDBId = tempMovie.getMovieIdDB();

                            statement.setString(1, starId);
                            statement.setString(2, movieDBId);

                            statement.addBatch();

                            xmlParser.numStarsInMovies++;
                        }
                    }
                }
                try {
                    statement.executeBatch();
                }
                catch (BatchUpdateException bue) {  }

                statement.close();
            }
            catch (SQLException se) {}
        }
    }

    //---[ GIMWorker Class ]
    static class GIMWorker implements Runnable {
        Connection connection;
        Map<String, Integer> genreMap;
        Map<String, Movie> movieMap;
        XMLParser xmlParser;

        public GIMWorker(Connection conn, Map<String, Integer> gm, Map<String, Movie> mm, XMLParser xmlp) {
            this.connection = conn;
            this.genreMap = gm;
            this.movieMap = mm;
            this.xmlParser = xmlp;
        }

        @Override
        public void run() {
            try {
                String gimInsertQuery = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?);";
                PreparedStatement statement = connection.prepareStatement(gimInsertQuery);

                // insert movies & genres
                for (Movie m : movieMap.values()) {
                    if (xmlParser.isValidFilm(m) && !m.getMovieIdDB().isBlank()) {
                        List<String> genres = m.getGenres();

                        for (String genre : genres) {
                            int genreId = genreMap.get(genre);

                            statement.setInt(1, genreId);
                            statement.setString(2, m.getMovieIdDB());

                            statement.addBatch();

                            xmlParser.numGenresInMovies++;
                        }
                    }
                }
                try {
                    statement.executeBatch();
                }
                catch (BatchUpdateException bue) { System.out.println("problem in GIM. fix later"); }
                statement.close();
            }
            catch (SQLException se) {}
        }
    }
}
