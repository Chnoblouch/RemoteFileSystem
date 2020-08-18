# An Incredibly Unsafe and Slow Remote File System that Really Shouldn't Be Used
## (Shortened: AIUASRFSTRSBU)

This is a Java application that lets you access files on other computers.\
To start a server, run `java -jar RemoteFileSystem.jar server`.\
To start a client, run `java -jar RemoteFileSystem.jar client insert-ip-adress`.\
The server port is 3782 and data is transferred over TCP.

"AIUASRFSTRSBU" makes it possible to create, delete, rename, move, copy, upload and download files over the LAN. You can use it to open, edit and save text files, display .jpg, .png and .gif files, play .wav files and start and stop .jar files.

However, all jokes aside, this application really is not something you should use, there are way better tools out there to do this kind of stuff. 