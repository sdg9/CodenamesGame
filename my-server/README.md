Step 1: Build Fat Jar
Run gradle task `shadowJar`

Step 2: Deploy to heroku
heroku deploy:jar build/libs/my-server-1.0-all.jar --app codenames-ktor

Step 3: Make sure client is pointed to heroku instance
//    const val LOCAL_WEBSOCKET_DESKTOP = "ws://codenames-ktor.herokuapp.com/"
