# Spotify

This is a program that streams music. It consists of a server and a client.

## Server

The server is a thread and can be started by calling the `start()` method in the `Server` class.  
The server accepts an instance of a `Database` class, which in turn accepts the following four file names as strings:

- A file where the users' information is saved.
- A file where the playlists are saved.
- A path to the folder where the files in points 1 and 2 are saved.
- A folder where the songs are saved.

## Client

The client is a thread and can be started by calling the `start()` method in the `Client` class.

The client is a thread that can be started once the server is up and running. It communicates with the server through the following requests:

| Command             | Needed Parameters (<>)                   | What It Does                                                                                     | Example Request                          |
|:-:|:-:|:-:|:-:|
| register            | \<email> \<password>                     | Registers the user                                                                               | register example@example.com pass123        |
| login               | \<email> \<password>                     | Logs in the user                                                                                 | login example@example.com pass123           |
| disconnect          |                                          | Logs out the user                                                                                | disconnect                               |
| terminate           | \<terminate>      |                      | Terminates the Server
| search              | \<words>                                 | Searches for songs by one or more words                                                          | search queen                              |
| top                 | \<number>                                | Returns the top songs based on streams                                                           | top 10                                   |
| create-playlist     | \<name_of_the_playlist>                   | Creates a playlist                                                                               | create-playlist my_favorites             |
| add-song-to         | \<name_of_the_playlist> \<artist-song>   | Adds a song to a playlist, if the current user has a playlist with such name                      | add-song-to my_favorites Queen - Radio Ga Ga |
| show-playlist       | \<name_of_the_playlist>                   | Shows the songs in a given playlist                                                              | show-playlist my_favorites               |
| play                | \<artist-song>                            | Plays a song, if it exists in the database. The song must be inputed like this: \<Artist Name> - \<Song Name> | play Queen - Bohemian Rhapsody |
| stop                |                                          | Stops a song                                                                                     | stop                                     |                                      |

For addition information about the requests refer to the javadoc in the `Command` classes
##
The requests get handled through the `java.nio` API. The `play` command starts a `java.net` connection that streams a song to the client if the client connects to it.

When the client receives a song, it must stream it through a `java.net` connection. The server starts a daemon thread, and the client must create an instance of `SourceDataLine` and a thread of its own, and connect to the server's thread to read information (in bytes) and write it to the `SourceDataLine` instance. To stop the song, the client must call the `stop()` method of the `SourceDataLine`.

That's it! Enjoy streaming your favorite music with the Spotify Program.
