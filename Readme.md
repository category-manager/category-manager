# CATEGORY MANAGER

<h2>Table of contents</h2>
<li><a href="#overview">Overview</a></li>
<li> <a href="#usage">Usage (Setup, Builder, Client, Operations)</a></li>
<li> <a href="#purpose">Purpose and Problem statement</a></li>
<li> <a href="#features">Capability and features</a></li>
<li> <a href="#design">Design architecture and LLD</a></li>
<li> <a href="#configurability">Configurability</a></li>
<li> <a href="#concepts">Brief on concepts, algorithms used in the library</a></li>
<li> <a href="#extension">Extending the library features</a></li>
<li> <a href="#example">Example Project Reference</a></li>
<li> <a href="#future-plans">Future release plans</a></li>
<li> <a href="#thank-you-note">Thank you note</a></li>
<li> <a href="#contact">Contact details</a></li>

##### <hr>
<div id="overview">
<h3> Overview </h3>
<p style="color: steelblue; font-weight: regular;">
    Category Manager is a simple, easy to use , configurable, extendable and efficient solution to store, manage categorical / hierarchical 
    information of your business in memory of your application. And also export the same across systems through rest API's to maintain 
    data consistency across systems and databases.
</p>
</div>

##### <hr>
<div id="usage">
<h3> Usage </h3>
<h4>Setup</h4>

First export USERNAME=YOU_GITHUB_USERNAME and TOKEN=GITHUB_PRIVATE_TOKEN with package read privilages

Add the below dependency in your maven POM file

    <dependency>
      <groupId>com.github.sudarshan</groupId>
      <artifactId>category-manager</artifactId>
      <version>0.1.0-BETA</version>
    </dependency>

If you are using Gradle, add this to your dependency

        implementation "com.github.sudarshan:category-manager:${categoryManagerVersion}"

<h4>Builder config </h4>

        CategoryManagerClient
            .getBuilder()
            .configureImport(connection, IMPORT_SQL, getImportRowMapper())
            .configureDbExport(
                connection,
                EXPORT_ALL_CATEGORY_SQL,
                EXPORT_CATEGORY_ALL_PATH_SQL,
                EXPORT_CATEGORY_SQL,
                EXPORT_CATEGORY_PATH_SQL,
                getExportCategoryPsMapper(),
                getExportCategoryPathPsMapper()
            );

<h4>Export Configs </h4>

    private static BiFunction<PreparedStatement, ICategoryPathExportData, PreparedStatement> getExportCategoryPathPsMapper() {
        return (ps, data) -> {
            try {
                Connection connection = ps.getConnection();
                DefaultCategoryPathExportData d = (DefaultCategoryPathExportData)data;
                String categoryId = d.getCategoryId();
                List<String> ancestorPaths = d.getAncestorPaths();
                ps.setString(1, categoryId);
                ps.setArray(2, connection.createArrayOf("text", ancestorPaths.toArray()));
                ps.setTimestamp(3, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                ps.setTimestamp(4, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));

            } catch(SQLException sqlException) {
                log.error(sqlException.getMessage());
            }
            return ps;
        } ;
    }

    private static BiFunction<PreparedStatement, ICategoryExportData, PreparedStatement> getExportCategoryPsMapper() {
        return (ps, data) -> {
            try {
                var d = (DefaultCategoryExportData)data;
                String categoryId = d.getCategoryId();
                Node node = d.getNode();
                Connection connection = ps.getConnection();
                ps.setString(1, categoryId);
                ps.setString(2, node.getData().toString());
                ps.setArray(3, connection.createArrayOf("text",node.getParents().toArray()));
                ps.setArray(4, connection.createArrayOf("text",node.getChildren().toArray()));
                ps.setTimestamp(5, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                ps.setTimestamp(6, new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));

            } catch (SQLException sqlException) {
                log.error(sqlException.getMessage());
            }
            return ps;
        } ;
    }
<h4>Import Configs </h4>
    
    private Function<ResultSet, Node> getImportRowMapper() {
        return (rs) -> {
            Node node = new Node();
            try {
                String id = rs.getString("id");
                String name = rs.getString("name");
                var mapper = new ObjectMapper();
                var objNode = mapper.createObjectNode();
                objNode.put("name", name);
                JsonNode data = objNode;
                String[] parentCategoryIds = (String[]) rs.getArray("parent_category_ids").getArray();
                if(Objects.isNull(parentCategoryIds)) {
                    parentCategoryIds = new String[0];
                    log.info("found null as parent for {}",id);
                }
                node.set_id(id);
                node.setChildren(new HashSet<>());
                node.setParents(new HashSet<>(Arrays.asList(parentCategoryIds)));
                node.setData(data);
                return node;
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
            return node;
        };
    }

</div>

##### <hr>
<div id="purpose">
<h3> Purpose and Problem statement </h3>
<p>
Applications of e-commerce, healthcare, educational institutes or any general system have categorical information.
It's very valuable to store, retrieve and manage this information time efficiently and accurately. And have consistent data across multiple
teams in case of large system with the least delay, Be able to export data for review.<br><br>
And also to view the information in the relative upper and lower hierarchy, trace the data back to its origin or its leaf nodes.
Category manager provides solution to achieve all these.
</p>
</div>

##### <hr>
<div id="features">
<h3> Capability and features </h3>
<p>
<b> You can ,</b>
<li>
    Import your existing hierarchical data from Db. Or start from scratch - build the hierarchy structure using Rest API wrapper around this library.
    I've already provided you with a <a href="https://github.com/category-manager/category-manager-ui" target="_blank"> descent UI tool</a>
    with <a href="https://github.com/category-manager/product-details"> springboot backend </a> to get started .
</li>
<li>
    Perform realtime CRUD operation on the data.
</li>
<li>
    Export node information, Export Paths.
<br>
    Export could be a Json to serve API request or into any database table. 
</li>
</p>
</div>

##### <hr>
<div id="design">
<h3>Design / architecture </h3>
<br>
<img alt="Design Diagram" src="./assets/cm-design.png"></img>
<br>
Design / architecture
<br>
<p>
<li>
<b> Import, Export, RealtimeOperation, Data </b> these are the interfaces which define the structure and features provided by 
    its service providers.
</li>
<li>
    <b> ClientManagerClientBuilder </b> is used to configure the Import, Export operations. These configuration involve <i>db connection object, 
    db-import query, row mappers for imports and export-db-query and prepared statement mappers for exports respectively.</i>
</li>
<li>
    <b> CategoryManagerClient </b> gives you access to operations objects to perform import, export, realtime operations on the data.
</li>
</p>
<br>
<img alt="Design Diagram" src="./assets/cm-er.png"></img>
<br>
Core spi
<br>
<p>
<li>
<b> Import operation </b>, reads, builds in-memory data-structure for the data imported and make it global for other operations
    to operate on.
</li>
<li>
<b> Export operation  has 2 default implementation. 1. RestExport 2. DbExport. </b>
With rest export you can retrieve information regarding any category that is in-memory, generate its ancestor and 
descendant paths in the JSON format.
</li>
<li>
<b> Db export </b> basically does similar operation as Rest Export, and exports the generated data into Db tables as configured.
</li>
<li>
<b>Realtime operation </b> lets you do add, update, delete the nodes at highest abstraction level, but the underneath involves the concept of NodePresence,
the way we classify the data, relinking the sub-tree/graph on each operation which will be discussed further in the concepts section of this page. 
</li>
</p>
<br>
<img alt="Design Diagram" src="./assets/cm-topology.png"></img>
Project files structure
<br>

</div>

##### <hr>
<div id="configurability">
<h3>Configurability</h3>
<p>
    &nbsp As discussed above, these interfaces are merely a specification, you can implement your own classes suiting your 
    requirements if any.
    Or you simply extend the existing DefaultImplementation and override subset of the methods.
<br>    
    &nbsp As of now you cannot register your implementation into clientManager , meaning you would have to implement clientManager
    if you decide to go with your implementation for any operations.
<br>
In future release you will be able to register your implementation of any operation into the operations registry which will be provided.  
</p>
</div>

##### <hr>
<div id="concepts">

</div>