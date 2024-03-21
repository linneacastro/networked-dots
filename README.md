// NAMES: Vyacheslav Lukiyanchuk and Linnea P. Castro 
// DATE: 04 June 2023
// COURSE: CSE 223
// ASSIGNMENT #: PA6

// PA6 A Networked Dots Game Written in Java using Eclipse with WindowBuilder's Swing Package
// NetDot.java

// PROGRAM SYNOPSIS:
/*
This program contains four files (NetDot.java, SockThread.java, MyPanel.java, and Box.java) creating the code 
for a two-player networked NetDot game, written in Java, and delivered as a GUI using WindowBuilder's Swing Package. 
This game builds upon the Dots game created in PA5; utilizing the same methods established in the Box and MyPanel
Classes, with the addition of a SockThread class to allow the program to use threads to create a server connection
enabling communication via data sharing.  In this case, information shared by players includes: their names (to
allow for game boards to be updated with pertinent name and initial information), what coordinates a given player clicked
and subsequent line drawing or box ownership updates, and whether or not the Quit button has been pressed.

The introduction of a networking component to this game introduces a programming challenge - both game boards must be 
updated to reflect the same information, so how will we accomplish this?  The solution involves the creation of threads, 
as described above,and relies on click information being shared between players.  Click information being shared to 
methods within MyPanel will process identical information the same way, and thereby keep the two gameboards in sync in 
terms of score information, lines drawn, and boxes owned.  

Care was taken to create a simple system to initialize turns inside SockThread's run method so that turns would 
mirror the structure created in the original Dots game, allowing us to recycle as much code as possible and take 
advantage of using code that we knew was already doing its job and working. Turns are assigned in the run method 
depending on whether the player is the Client or the Server, Server always going first.  The mouse click listener 
on the gameboard only registers clicks that match the turn inside the gameArea's turn method, with the Server being 
0, and Client being 1.  
 
As in the original version, the game takes place inside a JFrame, containing two JPanels: a standard JPanel called 
ContentPane,and a custom MyPanel, extending JPanel, where actual gameplay occurs. The ContentPane JPanel contains a 
start/connect button, score label, turn label, and the actual game board MyPanel.  New additions in NetDot include radio 
buttons to specify whether the player is the Server or Client.  The client is given an additional text field which allows 
them to enter in a custom hostName used to connect to the Server's socket. 

This program demonstrates event driven code; the events of gameplay being driven by a user's clicks
on the start or reset buttons, and then primarily by mouse clicks on the game board.  Changes to the
game made through these mouse clicks are reflected in the GUI in the paint and repaint methods in 
the Graphics class.  These updates allow the game to have a dynamic interface that updates lines drawn,
the score, player turns, and ownership of boxes.  The networking aspect of this game allows the results of the 
event driven code be sent over to the other player in the form of coordinates that they are funneled to the 
otherPlayerTurn method to be updated on their game board. Through this method coordinates are processed through 
MyPanel's methods, allowing the other player's click and its consequences to be updated on the current player's board;
attributing any boxes completed and resulting score information to the correct player who made those moves initially. 

After two instances of the game are created, one player must select the Server radio button, and the other player
must select the Client radio button.  After inputting player names and pressing the start/connect button, a new
SockThread is created, allowing the Client to establish a connection with the Server. 
As in the original game, players take turns drawing lines between dots on the game board.  Lines are drawn with 
a single click, the line the player was intending to click inferred through calculations based on the coordinate 
and the shortest distance to a given box wall.  One turn warrants one click, with the exception of turns that 
complete a box.  If a player completed a box, their initial gets drawn inside that box and an extra turn is granted.  
When all boxes have been claimed, the player with the most boxes drawn is declared winner (or a tie if all is even).  

Code is modularized in the MyPanel Class, which parcels tasks out into methods representing elements that need
to be evaluated with every mouse click.   Some methods are singular in task, like the method that calculates 
Player 1's score.  Other methods, like the whatSideOfBox method is more broad in that it determines which side 
of the box is being clicked and also account for adjacent sides.  The methods, when called in appropriate
succession, give a way for game information to be relayed to the board (score, turn, lines, etc).  The methods
also give a basis for decision making, evaluating whether all boxes are owned a winner must be declared, for example.

More detailed methods on each class and enclosed methods can be found within each source file.

Skills practiced include: 
- Creating a network connection using sockets, Scanner, PrintWriter, and its associated flush() method. 
- Creating ways to take advantage of previously created and functioning code by creating an overlay of threads 
which are able to use these methods to create a networked game.
- Using arrays to store player name information.
- Capturing clicks with a Scanner and sending them via a PrintWriter, letting clicks that change a current player's
gameboard to also update an opposing player's gameboard, thereby keeping gameboards in sync, and players
playing the same game, essentially.
- Using radio buttons to change the behavior of the socket created, depending on whether Server or Client is selected.
 */
