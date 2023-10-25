# CATEGORY MANAGER

<h2>Table of contents</h2>
- <a href="#overview">Overview</a>
- <a href="#purpose">Purpose and Problem statement</a>
- <a href="#features">Capability and features</a>
- <a href="#design">Design architecture and LLD</a>
- <a href="#configurability">Configurability</a>
- <a href="#concepts">Brief on concepts, algorithms used in the library</a>
- <a href="#usage">Usage (Builder, Client, Operations)</a>
- <a href="#extension">Extending the library features</a>
- <a href="#example">Example Project Reference</a>
- <a href="#future-plans">Future release plans</a>
- <a href="#thank-you-note">Thank you note</a>
- <a href="#contact">Contact details</a>

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
With this library, you import your existing data from database directly into memory to construct in-memory data-structure,
further you can make CRUD operations on the data imported. 
<br>
You can also export the data that is in-memory to other database or table or get the export of category information as JSON. 
<br>
The Library gives the feature to generate the ancestor and descendant paths for any given category/key.
or export paths for all categories at once to get a dump in json format or into database table.
</p>
</div>

##### <hr>
<div id="design">
<h3>Design architecture and LLD</h3>
<br>
<img alt="Design Diagram" src="./assets/design.png"></img>
<br>
<p>
<li>
    Import, Export, RealtimeOperation, Data these are the interfaces which define the structure and features provided by 
    its service providers.
</li>
<li>
    ClientManagerClientBuilder is used to configure the Import, Export operations. These configuration involve db connection object, 
    db-import query, row mappers for imports and export-db-query and prepared statement mappers for exports respectively.
</li>
    CategoryManagerClient gives you access to operations objects to perform import, export, realtime operations on the data.
<li>
    Import operation, reads, builds in-memory data-structure for the data imported and make it global for other operations
    to operate on.
</li>
    Export operation has 2 default implementation. 1. RestExport 2. DbExport
    <br>
    With rest export you can retrieve information regarding any category that is in-memory, generate its ancestor and 
    descendant paths in the JSON format.
    <br>
    Db export basically does similar operation as Rest Export, and exports the generated data into Db tables as configured.
</p>
</div>

##### <hr>
<div id="configurability">
<h3>Configurability</h3>
<p>

</p>
</div>

##### <hr>
<div id="concepts">

</div>