
# sm-assets-frontend

A Scala/Play service to serve assets-frontend data locally.
This replaces the original python based assets-frontend server, removing the dependency on python and the complicates that came with it (i.e. running SimpleHttpServer vs http.server depending on version).

Unlike the original assets-frontend which would download every version on the off-chance it was needed, sm-assets-frontend downloads them on-demand and serves the content direct from the zip file.


## Configuration

`workdir` defaults to the $WORKSPACE environment variable used by service-manager
`artifactory.url` base url for artifactory
`artifactory.path` name of the assets-frontend folder

## API
GET `/assets/:version/:path/:to/:file` downloads requested version of assets-frontend and serves static content

GET `/admin/installed` admin page listing installed versions
DELETE `/admin/installed` removes all installed versions


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").