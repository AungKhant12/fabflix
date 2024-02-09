- # General
    - #### Team#: Fabflix Aqua
    
    - #### Names: Aung Khant, Andy Anh-Huy Tonthat
    
    - #### Project 5 Video Demo Link: https://youtu.be/dIYek3bjvo0

    - #### Instruction of deployment:

    - #### Collaborations and Work Distribution:

    - JDBC Connection Pooling - Andy
    - Master Slave Replication - Andy
    - Scaling with Cluster and Load Balancer - Andy
    - Measuring Performance - Aung


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
      - src/
        - ActorParser
        - AddMovieServlet
        - AddStarServlet
        - BrowseGenreServlet
        - BrowseTitleServlet
        - CartServlet
        - EmployeeLoginServlet
        - FetchGenresServlet
        - FetchMetadataServlet
        - FullTextSearchServlet
        - LoginServlet
        - MoviesServlet
        - PaymentServlet
        - SearchServlet
        - SingleMovieServlet
        - SingleStarServlet
        - StarsServlet
        - TitleSuggestionServlet
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
      - When the Tomcat server starts up, it creates multiple JDBC connections to the backend SQL.
      - When each servelet that gets hit, it requests a connection from Tomcat to the SQL backend.
      - 
    - #### Explain how Connection Pooling works with two backend SQL.
      - Each Tomcat server maintains its own pool of JDBC connections to the backend SQL. Whenever either server gets hit, it uses its own pool of connections.

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
      - WebContent/META-INF/Context.xml

    - #### How read/write requests were routed to Master/Slave SQL?
      - Setup Resource tag in Context.xml targeting Master in MySQL instance.

- # JMeter TS/TJ Time Logs
    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.
      - Python3 log_processing.py [path to log.txt] [path to log2.txt] 


- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis**                                                  |
|------------------------------------------------|----------------------------|----------------------------|-------------------------------------|---------------------------|---------------------------------------------------------------|
| Case 1: HTTP/1 thread                          | ![img/single-http-connectionPooling-1thread.png](path to image in img/) | 25ms                       | 339.8208ms                          | 4.7586ms                  | Single User means faster response time                        |
| Case 2: HTTP/10 threads                        | ![img/single-http-connectionPooling-10thread.png](path to image in img/) | 3248ms                     | 3253.6772ms                         | 10.1653ms                 | 10 users constantly hitting server means longer response time |
| Case 3: HTTPS/10 threads                       | ![img/single-https-connectionPooling-10thread.png](path to image in img/) | 3266ms                     | 3242.4599ms                         | 9.5293ms                  | 10 users with HTTPS is not significantly different from HTTP |
| Case 4: HTTP/10 threads/No connection pooling  | ![img/single-http-noconnectionPooling-10thread.png](path to image in img/) | 3495ms                     | 3279.1431 ms                        | 5.2828ms                  | No connection pooling results in longer querey time |

| **Scaled Version Test Plan**                   | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![img/scaled-http-connectionPooling-1thread-1.png](path to image in img/)   | 423ms                      | 356.0317ms                          | 5.1195ms                  | Scaled version with 1 user results in higher query time due to needing to go through load balancer |
| Case 2: HTTP/10 threads                        | ![img/scaled-http-connectionPooling-10thread.png](path to image in img/)   | 1864ms                     | 1813.7359ms                         | 7.6624ms                  | Scaled version with 10 users result in lower average query and search servlet times as the load balancer ensures not all requests are sent to only one machine |
| Case 3: HTTP/10 threads/No connection pooling  | ![img/scaled-http-noConnectionPooling-10thread.png](path to image in img/)   | 1695ms                     | 1069.4557ms                         | 10.1725ms                 | No connection pooling results in higher JDBC times |
