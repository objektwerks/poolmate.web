Work Order
----------
>**WIP** This is refactoring of the [work.order](https://github.com/objektwerks/work.order) project using Scalajs and Scala 3.

Roles
-----
>A role can invoke a set of actions.
1. **homeowner** — add, select and edit *work orders*
2. **service provider** — select and edit *work orders*
3. **app** - has super powers :)

Features [ Roles ]
------------------
>A feature maps to a set of roles.
1. **register user** — [ homeowner, service provider ]
2. **login user** — [ homeowner, service provider ]
3. **add work order** — [ homeowner ]
4. **edit work order** — [ homeowner, service provider ]
5. **list work orders** - [ homeowner, service provider ]
6. **edit user** — [ homeowner, service provider ]
7. **registration email notification*** - [ app ]
8. **new work order email notification** - [ app ]
9. **work order (updated and closed) email notifications** - [ app ]

Install
-------
1. brew install postgresql
2. brew install node
3. npm install ( in project root directory )
>See **package.json** for installable dependencies.

Build
-----
1. npm install ( only when package.json changes )
2. sbt clean compile fastLinkJS
>See **js/target/public** directory.

Test
----
1. sbt clean test fastLinkJS

Dev
---
1. sbt jvm/run ( new session, curl -v http://localhost:7272/now )
2. sbt ( new session )
3. ~ js/fastLinkJS
4. npx snowpack dev ( new session )
>Edits are reflected in the **fastLinkJS** and **snowpack** sessions.
>See **snowpack.config.json** and [Snowpack Config](https://www.snowpack.dev/reference/configuration) for configurable options.

Package Server
--------------
>See sbt-native-packager ( www.scala-sbt.org/sbt-native-packager/formats/universal.html )
1. sbt clean test fullLinkJS
2. sbt jvm/universal:packageZipTarball | sbt 'show graalvm-native-image:packageBin'
>**Optionally** execute Graalvm image: ./jvm/target/graalvm-native-image/scala.graalvm

Package Client
--------------
1. sbt clean test fullLinkJS
2. npx snowpack build
> See **build** directory.

Client
------
* Now
* Command => Event

Server
------
* Now: /now
* Api: /command

Config
------
>See these files:
1. jvm/src/main/resoures/server.conf
2. jvm/src/test/resources/test.server.conf

Cache
-----
>See jvm/Store.cache

Cors Handler
------------
* See poolmate.CorsHandler and poolmate.Server
* Also see https://github.com/Download/undertow-cors-filter

Logs
----
>See logs at /target/
1. jvm.log
2. test.jvm.log
3. test.shared.log

Mysql Schema
------------
1. work_order_db
2. user
3. work_order
>See **ddl.sql** for details.

Mysql Setup
-----------
>Built using Mysql 8.0.30
1. sudo mysql -u root
2. \. user.sql
8. \. ddl.sql

Mysql Connection Url
--------------------
* mysql://workorder:workorder@127.0.0.1:3306/work_order_db

Mysql Update
------------
1. mysql -u workorder -p
2. \. ddl.sql

Mysql Log
---------
>Apple M1, macOS, Big Sur - /opt/homebrew/var/mysql/computername.local.err

Mysql Connection Error
----------------------
>Nodejs occassionally produces this error with Mysql: connect ECONNREFUSED ::1:3306
>The solution is varied and fundamentally unknown.

Date Time
---------
>ISO standard: YYYY-MM-DDTHH:mm:ss.sssZ

Photos
------
>The following image file types are supported:
1. **jpeg**
2. **jpg**
3. **png**
>Only **1** image is allowed ***per*** work order. The app stores ***images*** in **WORK_ORDER_IMAGES_DIR** defined below.

Environment
-----------
>The following environment variables ***must*** be defined:
export WORK_ORDER_DATABASE_URL="mysql://workorder:workorder@127.0.0.1:3306/work_order_db"
export WORK_ORDER_DATABASE_USER="workorder"
export WORK_ORDER_DATABASE_PASSWORD="workorder"
export WORK_ORDER_DATABASE_POOL_INITIAL_SIZE=9
export WORK_ORDER_DATABASE_POOL_MAX_SIZE=32
export WORK_ORDER_DATABASE_POOL_CONNECTION_TIMEOUT_MILLIS=30000

export WORK_ORDER_EMAIL_HOST="youremailhost.com"
export WORK_ORDER_EMAIL_PORT=587
export WORK_ORDER_EMAIL_SENDER="youremailaddress@youremailhost.com"
export WORK_ORDER_EMAIL_PASSWORD="youremailpassword"

export WORK_ORDER_SERVICE_PROVIDER_EMAIL="testemailaddress1@youremailhost.com"
export WORK_ORDER_HOME_OWNER_EMAIL="testemailaddress2@youremailhost.com"

export WORK_ORDER_DIR=$HOME/.workorder
export WORK_ORDER_IMAGES_DIR=$WORK_ORDER_DIR/images
export WORK_ORDER_LOGS_DIR=$WORK_ORDER_DIR/logs

>All variables are for production less: WORK_ORDER_SERVICE_PROVIDER_EMAIL and WORK_ORDER_HOME_OWNER_EMAIL, which are
>for the integration test.

Resources
---------
1. Cask - https://com-lihaoyi.github.io/cask/index.html
2. uPickle - https://com-lihaoyi.github.io/upickle/
3. Requests - https://github.com/com-lihaoyi/requests-scala
4. ScalikeJdbc - http://scalikejdbc.org
5. H2 - https://h2database.com/html/main.html
6. Scala-Java-Time - https://github.com/cquiroz/scala-java-time
7. Scaffeine - https://github.com/blemale/scaffeine
8. Packager - https://www.scala-sbt.org/sbt-native-packager/formats/graalvm-native-image.html
9. Gaalvm - https://www.graalvm.org/docs/introduction/
10. Snowpack - https://www.snowpack.dev/


License
-------
> Copyright (c) [2022] [Objektwerks]

>Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    * http://www.apache.org/licenses/LICENSE-2.0

>Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations u\nder the License.