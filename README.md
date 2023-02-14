# Spotify

This is a Program that Streams music. It consists of a server and a client.

## Server

The `Server` is a Thread and can be started by calling the `start()` method of a Thread created with an instance of the `Server` class.  
The `Server` accepts an instance of a `Database` interface, which in turn accepts the following four file names as strings:

- A file where the users' information is saved.
- A file where the playlists are saved.
- A path to the folder where the files in points 1 and 2 are saved.
- A folder where the songs are saved.  

Default file names are present in the `Database` interface. For additional Infomation refer to the `Database` interface and its Implementations.  

The `Server` handles the requests through the `java.nio` API.

## Client

The `Client` is a thread and can be started by calling the `start()` method of a Thread created with an instance of the `Client` class.

The `Client` is a thread that can be started once the server is up and running. It communicates with the server through the following requests:

## Requests
|Command|Needed Parameters|What It Does|Example Request|
|:-:|:-:|:-:|:-:|
| `register` | \<email> \<password> | Registers the user | "register example@example pass123" |
| `login` | \<email> \<password> | Logs in the user | "login example@example pass123" |
| `disconnect` | | Logs out the user | "disconnect" |
| `terminate` | \<terminate> | Terminates the Server | "terminate" |
| `search` | \<words> | Searches for songs by one or more words included in the Title or the Artist's name | "search queen bohemian"  |
| `top` | \<number> | Returns the top songs based on streams | "top 10" |
| `create-playlist` | \<name_of_the_playlist> | Creates a playlist for the current User | "create-playlist my_favorites" |
| `add-song-to` | \<name_of_the_playlist> \<artist-song> | Adds a song to a playlist, if the current user has a playlist with such name | "add-song-to my_favorites Queen - Bohemian Rapsody" |
| `show-playlist` | \<name_of_the_playlist> | Shows the songs in a given playlist | "show-playlist my_favorites" |
| `play` | \<artist-song> | Plays a song, if it exists in the database. The song must be inputed like this: \<Artist Name> - \<Song Name> | "play Queen - Bohemian Rhapsody" |
| stop | | Stops a song | "stop" |

For addition information about the requests refer to the javadoc in the `Command` classes.  

## Responses
|Example Response|Description|
|:-:|:-:|
| "ok PCM_SIGNED 48000.0 16 1 2 48000.0 false 7000" | Response for the `play` command. If it doesn't start, `ok` means the song doesn't exist. |
| String | All other commands return a String that can be printed |
 
The `play` command starts a `java.net` connection that streams a song to the client if the client connects to it.

### Streaming a Song
The `Server` starts a daemon thread that is going to read the Song file as bytes and write them to the Socket that has connected to it.  
  
The `Client` must create an instance of `SourceDataLine` from the received Response, a thread that connects to the `Server`'s thread to read the song (as bytes) and write it to the `SourceDataLine` instance.  
  
To stop the song, the client must call the `stop()` method of the `SourceDataLine`. The Thread from the `Server`'s side will stop automatically without throwing exceptions.

That's it! Enjoy streaming your favorite music with the Spotify Program.
