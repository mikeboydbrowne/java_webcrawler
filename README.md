##Overview##
`edu/upenn/cis455` holds the webcrawler, XPath engine, and crawler interface.

`test/edu/upenn/cis455` holds the tests for these programs. 

###Implementation###
Within `edu/upenn/cis455` there are four directories: `crawler`, `servlet`, `storage`, and `xpathengine`.

- `crawler` contains an implementation of a web crawler that crawls the web given a start url
- `servlet` contains a servlet-based interface for the web crawler
    - `XPathServlet.java` is the servlet used to run a web interface for the XPath Engine contained in `xpathengine` 
- `storage` contains a wrapper class for a local instance of BerkeleyDB
    - `CrawlerEntity.java` is the entity object for pieces of data accumulated by the crawler (i.e. urls, HTML/XML documents, etc.).
    - `DBWrapper.java` is the class that instantiates the local database files, gives the user access to the database handles, and shuts the databases down.
    - `UserEntity.java` is the entity object for pieces of data that come from the webapp.
- `xpathengine` Is the Rerank and oracle are used to generate a set of the best translations.
    - `XPathEngine.java` contains an interface for an XPath validator and evaluator implemented in `XPathEngineImpl.java`
    - `XPathEngineFactory.java` a factory class to generate instantiations of the `XPathEngine` class
    - `XPathEngineImpl.java` an implementation of the methods described in `XPathEngine.java`

###Testing###
Within `test/edu/upenn/cis` there are several classes used to test the classes contained in `edu/upenn/cis455`

- `RunAllTests.java` runs all test files implemented
