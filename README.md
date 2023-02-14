# Spotify
#### This is a Project made for passing the MJT Course @ FMI. Link to the Project [here](https://github.com/fmi/java-course/blob/master/course-projects/spotify.md).
##
This is a Program that Streams Music. It consists of a `Server` and a `Client`.

## Server

The `Server` is a Thread and can be started by calling the `start()` Method of a Thread created with an Instance of the `Server` Class.  
  
The `Server` accepts an Instance of a `Database` Interface, which in turn accepts the following four File Names as Strings:

- A File where the Users' Information is saved.
- A File where the Playlists are saved.
- A Path to the Folder where the Files in Points 1 and 2 are saved.
- A Folder where the Songs are saved.  

Default File Names are present in the `Database` Interface. For additional Infomation refer to the `Database` Interface's and its Implementations' javadocs.  

The `Server` handles the Requests through the `java.nio` API.

## Client

The `Client` is a Thread and can be started by calling the `start()` method of a Thread created with an Instance of the `Client` Class.

The `Client` is a Thread that can be started once the `Server` is up and running. It communicates with the `Server` through the following Requests:

## Requests
|Command|Needed Parameters|What It Does|Example Request|
|:-:|:-:|:-:|:-:|
| `register` | \<email> \<password> | Registers a new User | "register example@example pass123" |
| `login` | \<email> \<password> | Logs in the User | "login example@example pass123" |
| `disconnect` | | Logs out the User | "disconnect" |
| `terminate` | \<terminate> | Terminates the Server | "terminate" |
| `search` | \<words> | Searches for Songs by one or more Words included in the Title or the Artist's Name | "search queen bohemian"  |
| `top` | \<number> | Returns the top Songs based on Streams | "top 10" |
| `create-playlist` | \<name_of_the_playlist> | Creates a Playlist for the current User | "create-playlist my_favorites" |
| `add-song-to` | \<name_of_the_playlist> \<artist-song> | Adds a Song to a Playlist, if the current User has a Playlist with such Name | "add-song-to my_favorites Queen - Bohemian Rapsody" |
| `show-playlist` | \<name_of_the_playlist> | Shows the Songs in a given Playlist | "show-playlist my_favorites" |
| `play` | \<artist-song> | Plays a Song, if it exists in the Database. The Song must be inputed like this: \<Artist Name> - \<Song Name> | "play Queen - Bohemian Rhapsody" |
| stop | | Stops a song | "stop" |  

For additional Information about the Requests refer to the javadoc in the `Command` Classes.  

## Responses
|Example Response|Description|
|:-:|:-:|
| "ok PCM_SIGNED 48000.0 16 1 2 48000.0 false 7000" | Response for the `play` Command. If it doesn't start with `ok`, that means the Song doesn't exist. |
| String | All other Commands return a String that can be printed | 

## Songs
All Songs must be saved in `.wav` Format with the Name of the Artist followed by a `-` and the Song Name. Example: "Queen - Bohemian Rapsody.wav".  

For additional Information about the Songs refer to the javadoc of the `Song` Class and the javadoc of the `Database` Class' Implementation, more specifically how the Songs are read from the specified Folder.

## Starting the Program
Start the `Server` by calling the `start()` Method of a Thread created with an Instance of the `Server` Class.  

The `Database` Object, which implements `AutoCloseable`, passed in the Server's Constructor will read all Information from the specified in it's Constructor Files and Folders. If any of the Files do not exist, they will be created once the Program ends.  

Start a `Client` by calling the `start()` Method of a Thread created with an Instance of the `Client` Class.  

An unregistered `Client` must `register`. A Registered `Client` can only `login`. A logged in `Client` can do everything except `register` and `login`.

A `Client` can log out by sending a `disconnect` Request. 

A `Client` can stop the `Server` by sending a `terminate` Request. This will call the `close()` Method of the `Database` and all current Information will be saved in the corresponding Files. The Information can be loaded by starting the Program again with these Files passed in the `Database`'s Constructor.

## Streaming a Song
The `play` Command starts a `java.net` Connection that streams a Song to the `Client` if the `Client` connects to it.  

The `Server` starts a Daemon Thread that is going to read the Song file as Bytes and write them to the Socket that has connected to it.  
  
The `Client` must create an Instance of the `SourceDataLine` Class from the java Sound API from the received Response, a Thread that connects to the `Server`'s thread through a `java.net` Connection to read the song (as bytes) and write them to the `SourceDataLine` Object.  
  
To stop the Song, the `Client` must call the `stop()` Method of the `SourceDataLine` Object. The Thread from the `Server`'s side will stop automatically without throwing Exceptions.

Enjoy streaming your favorite Music with my simple Spotify Program.
