// NAMES: Vyacheslav Lukiyanchuk and Linnea P. Castro 
// DATE: 04 June 2023
// COURSE: CSE 223
// ASSIGNMENT #: PA6

// PA6 A Networked Dots Game Written in Java using Eclipse with WindowBuilder's Swing Package
// SockThread.java

// CLASS SYNOPSIS:
/*
The SockThread Class extends the java Thread class.  It contains five methods.  A basic constructor method outlines the 
way NetDot will create a new SockThread, and a saveParent class will allow a SockThread object to reference its parent program 
(NetDot.java in this case), as the run method will be calling NetDot methods.  

The savePlayerNames method serves as a precursor to the name swapping which happens after a connection is established in the run
method.  When a user presses the start/connect button, names exactly as they appear in text fields are passed into the playernames
array.  This means that the current player will most likely have customized their name, but the opposing player's name will be the 
default server or client placeholder.  The array will be updated in the first communication instance between Server and Client after
a communication socket has been created.  

The beginning of the run method forks off into two separate instruction sets, managed by if statements, executed depending on whether
the player has selected that they are the Client or the Server via GUI radiobuttons.  If the player has selected that they are the Server,
their run method will create the ServerSocket, initialize a Scanner and PrintWriter, receive the client's name, update the playernames
array, and then send their name of the Client via the sendInfo method.  If the player has selected that they are the Client, they will
be establishing a connection to the Server, creating a Scanner and PrintWriter, sending their name to the Server, receiving the Server's name
and then updating their playernames array.  Note that the Server sends the first message in this case.  

The run method is vital for establishing player turn protocol and is designed to mirror the instructions that manage turns within the MyPanel 
Class.  The Server is set to be turn=0 and the Client turn=1.  The run method calls NetDot's beginGame method, whereby all player name and initial
information is locked into the game board GUI.  Turn information is passed back to the main program, NetDot, before entering the main while
loop where the thread will spend the bulk of its gametime inside.  

The while loop uses the following format, while (sc.hasNextLine()), using the scanner to read in an incoming line of text containing
information about what the opposing player did on their turn.  They either will have pressed the quit button causing both player's screens 
to exit, or the player will have completed a legitimate click.  Information of a legitimate click will trigger a call to NetDot's otherPlayerTurn
method, which will essentially allow the current players gameboard to update with the changes made by the other player.  This can happen because 
both players are calling the same methods within the MyPanel method.  As long as they are playing the the same rules and turns are in sync,
we can expect the gameboard behavior to display the same information because they were given the same information to process. 

The final method in the SockThread Class is sendInfo.  This is used internally by the run method to exchange names, streamlining the PrintWriter
and flush process, but it also is used in the main program following a player's click so that the data changed by that click can be sent
to the opposing player for the appropriate update. 
*/

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.Scanner;

public class SockThread extends Thread{ // Extends java Thread class
	
	// VARIABLES
	boolean iAmTheServer;
	boolean iAmTheClient;
	
	NetDot parent; // Used for referring back to variables inside NetDot
	
	String hostName; // Allows custom hostName to be passed in by Client in GUI
	
	PrintWriter pw; // PrintWriter variable established for sending messages, used in run method and sendInfo method
	
	String[] playernames = new String[2]; // Array for saving playernames from NetDot's textfields
	                                      // for player name exchange in savePlayerNames method
	
	
	// CONSTRUCTOR METHOD - A very basic constructor method, to be used by main program NetDot.java when creating a new
	// SockThread to initialize a new communication socket that establishes communication between the Client and Server.
	// Called in NetDot.java following both players clicking connect (first Server, then Client).
	public SockThread() {
		
	}
	
	
	// SAVE PARENT METHOD - This method allows a NetDot object to be saved as the "parent" Class.  This is used together with
	// the NetDot parent variable declared above, to enable referencing back to variables inside NetDot.
	public void saveParent(NetDot parent)
	{
		this.parent = parent;
	}
	
	
	// SAVE PLAYER NAMES METHOD - This method is used as a precursor to exchanging player names.  After the connect/start button is pressed
	// within the main program, but before the run/start method is called, name text field entries will passed into the SockThread class
	// to be stored in opposing spots of the playernames array.  The premise is that the player's own name will be sent in and then that will 
	// be passed to the other player to replace the placeholder name that was originally passed in by the main program. 
	// After the communication socket is established, the opposing players name within the array will be updated to reflect 
	// their actual name (this happens inside run/start).  This exchange will ultimately allow both player names, as entered in their GUI name 
	// text field to be displayed on the opposing player's GUI, in keeping with the goal of having both player's game GUIs be kept in lockstep, 
	// in terms of not only clicks, but also scores, and name information.
	public void savePlayerNames(String player1, String player2)
	{
		playernames[0] = player1;
		playernames[1] = player2;
	}
	
	
	// RUN/START METHOD - Following the Server/Player 1 clicking the Connect button, followed by Client/Player 2 doing the same,
	// this method is used to create a ServerSocket whereby both players can connect and communicate with each other.
	// This method and the ServerSocket created allows game clicks, names, and score information to be shared and updated,
	// turning the original Dots game into a networked game, which can be played by two players on separate machines, over a network.
	// Different pieces of the run method are executed depending on whether the player pressing Connect is the Client or Server, with the 
	// Server actually creating the ServerSocket.  The while loop at the bottom of the run method is shared by both client and server. 
	// The while loop essentially listens for feedback from the gameboard that either the quit button has been pressed or a coordinate has 
	// been sent in.  
	public void run() {
		
		// VARIABLE DECLARATION (PrintWriter, pw, declared outside of run method, see above)
		int turn = 0; // Initialize turn to 0
		
		ServerSocket serverSock;
		Socket sock;
		Scanner sc;
		String data;
		
		// IF IAMTHESERVER IS TRUE, DO THE FOLLOWING:
		if (iAmTheServer) { 
			
			// Create ServerSocket called sock
			try {
				serverSock = new ServerSocket(1234); // 1234 is the Port Number
			}
			catch (IOException e1) {
				e1.printStackTrace();return;
			}
			
			// Listen for connection requests
			Socket mainSocket;
			try {
				mainSocket=serverSock.accept();
			}
			catch (IOException e2) {
				e2.printStackTrace();return;
			}

			// Connection has been made, read from sock		
			try {
				sc=new Scanner(mainSocket.getInputStream());
			}
			catch (IOException e3) {
				e3.printStackTrace();return;
			}
			
			// Make PrintWriter for sending messages to Client
			try {
				pw=new PrintWriter(mainSocket.getOutputStream());
			}
			catch (IOException e4) {
				e4.printStackTrace();return;
			}
			
			// Swap names (first receive from Client, then send my name)
			sc.hasNextLine(); // Receive and record name for player 2 by reading from this scanner
			data=sc.nextLine();
			parent.player2NameTextField.setText(data); // What the client sends over is their name, set this as player2Name in corresponding
			                                           // GUI text field, in NetDot (parent)
			this.sendInfo(playernames[0]); // Now send my name to the Client
			
			turn = 0; 
			
		}
		
		// IF NOT SERVER, THEN CLIENT, DO THIS:
		else {
			
			// Create a socket and connect to specified host and port number
			try {
				sock = new Socket(hostName, 1234); // Connect to hostName passed in as specified via port 1234
			}
			catch (UnknownHostException e5) {
				e5.printStackTrace();return;
			}
			catch (IOException e5) {
				e5.printStackTrace();return;
			}
			
			System.out.println("Connection Established with Server"); // Confirmation of connection established
			
			// Create Scanner to receive data
			try {
				sc=new Scanner(sock.getInputStream());
			}
			catch (IOException e5) {
				e5.printStackTrace();return;
			}
			
			// Create PrintWriter to send data
			try {
				pw=new PrintWriter(sock.getOutputStream());
			}
			catch (IOException e5) {
				e5.printStackTrace();return;
			}
			
			// Player name swap
			this.sendInfo(playernames[1]); // Send my name and store at [1] in playernames array (Player 1 is in spot [0])
			sc.hasNextLine(); // As long as scanner has a nextline, the next line will be Player 1's name
			data=sc.nextLine();
			parent.player1NameTextField.setText(data); // Set Player 1's textfield in parent NetDot Gui to the line I just received
			
			turn = 1;
		}
			
			// Set up board game now that server connection has been made and names swapped.  It is within this method (beginGame)
		    // inside NetDot.java that the repaint method is ultimately called to update all new player name information.
			parent.beginGame();
			
			parent.turn = turn; // Turn=0 for Server and 1 for Client, that value is passed back to NetDot as turn.
			
			// Names have been swapped, gameplay has begun, now while loop is "listening" for input from gameboard
			while (sc.hasNextLine()) { // Using scanner to receive data
				data=sc.nextLine();
				if (data.charAt(0) == 'Q') System.exit(0); // If either user pressed Quit, exit.  Otherwise, parse incoming string:
				
				String[] dataArray = data.split(" "); // Line received will be separated into fields as denoted by a single space
	    
				// 'C' means a coordinate was received, [0] was the 'C' coordinate designation, [1] is x-coordinate, [2] is y-coordinate.
				// This uses the otherPlayerTurn method inside NetDot.java to pass in coordinate information so that the board changes made
				// during the current player's turn are sent to the other player's game board to be updated, thereby keeping game boards and
				// information in sync.
				if (dataArray[0].charAt(0) == 'C') parent.otherPlayerTurn( Integer.parseInt(dataArray[1]), Integer.parseInt(dataArray[2]));
			}
			
	}
	
	// SEND INFO METHOD - This method is used to streamline sending data to opposing player.  A String is entered as method's argument (command), 
	// and then sent using the PrintWriter, pw via println followed by flush().  This method is used inside SockThread.java's run
	// method as players conduct their first data exchange by sending their own name to the other player. This method is also used inside
	// NetDot main program to send information about current player's moves to the opposing player in the form of a Q (if they pressed the Quit button),
	// or a C followed by coordinate if they completed a legitimate click.  When the while loop inside the run method receives this information,
	// it will either exit the game if Quit was pressed, or it will call back to the otherPlayerTurn method inside the main program to update what
	// happened on the other player's turn to keep the gameboards updated and in sync.
	public void sendInfo(String commmand) {
		pw.println(commmand);
		pw.flush();
	}
}
