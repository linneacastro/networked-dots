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

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JFormattedTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.Scanner;

import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;


public class NetDot extends JFrame {
	
	// VARIABLES
	int turn = 3; // Player1 is 0, player 2 is 1.  2 means game started but no connection has been made yet. 
	              // 3 means game has not yet started, ie. no one has pressed the Start button yet. 

	private JPanel contentPane;	
	private MyPanel gameArea;
	private JLabel scoreLabel; 
	
	private JTextField hostTextField;
	private Object serverRadioButton;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	
	JFormattedTextField player1NameTextField;
	JFormattedTextField player2NameTextField;
	
	JLabel winStatusLabel;
	
	SockThread sock;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NetDot frame = new NetDot();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	
	}
	

	/**
	 * Create the frame.
	 */
	public NetDot() {
		
		setTitle("NetDot - Player 1 - SERVER");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 596, 506);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		// WINSTATUS LABEL - shows turn and win
		winStatusLabel = new JLabel(" ");
		winStatusLabel.setVerticalAlignment(SwingConstants.TOP);
		winStatusLabel.setBounds(453, 310, 111, 51);
		contentPane.add(winStatusLabel);
		
		// CREATE GAMEAREA
		gameArea = new MyPanel(); // gameArea is a new MyPanel, which extends JPanel
		gameArea.setBounds(20, 40, 415, 415);
	
		// MOUSE EVENT REGISTERING CLICKS ON GAMEAREA
		// This will update current player's clicks on their own gameboard.  See otherPlayerTurn method below
		// for the method which will send click to other player's gameboard for updating.
		gameArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if(turn == gameArea.turn()) // Only play on your turn.
					                        // turn in NetDot.java is synchronized with turn method inside MyPanel.java/gameArea
					                        // to allow for recycling of original code and to ensure that the networked version's
					                        // way of tracking turns directly mirrors original logic so game updates are in sync.
				{
						
					int currentX=e.getX(); // Get coordinates of click
					int currentY=e.getY();
					
					// Check if point is within boundaries of gameboard
					if(currentX < 0 || currentX > 399 || currentY < 0 || currentY > 399) {
						return;
					}

					else { // Point is within boundary of game board, do the following:
					
						gameArea.savePoint(currentX, currentY); // Save x and y coordinates
						int currentRow=e.getY()/50; // Divide by 50 to determine row and column
						int currentCol=e.getX()/50;
						gameArea.saveRowCol(currentRow, currentCol); // Save that row and column
					
						
						if(gameArea.whatSideOfBox()) { // If line is already true, this will return true, display message, otherwise
							                           // this line will make line true in this box and adjacent, claiming ownership if appropriate
							scoreLabel.setText("This spot is already taken");
							return;
						}
					
						gameArea.turn(); // Turn method, will change turn if box wasn't just completed
					
						gameArea.repaint(); // Call repaint
					
						winStatusLabel.setText("turn Player" + (gameArea.turn()+1) + ": " + gameArea.getTurn()); // Update turn label and score
						scoreLabel.setText(gameArea.player1Name + ": " + gameArea.P1ScoreAsInt() + "    " + gameArea.player2Name + ": " + gameArea.P2ScoreAsInt());
						
						// Send this click to the other player via established socket.  'C' designates that this will be transmitted and received
						// as a coordinate, with legitimate corresponding x and y coordinates as obtained above.
						sock.sendInfo("C " + e.getX() + " " + e.getY());
					
					
					if(!gameArea.isGameInPlay()) { // If all boxes are taken, this will return false and a winner will be announced
						scoreLabel.setText(gameArea.whoIsTheWinner());
					}
					
					} // End of else block
					
				}
				else{ // If a user clicks on the game board and it isn't their turn
					winStatusLabel.setText("Not Your Turn");
				}
					
			}
		});
		
		gameArea.setLayout(null);
		gameArea.createGameBoard(); // call createGameBoard method to create 8x8 array and fill each with a Box
		contentPane.add(gameArea);
		
		
		// PLAYER 1 NAME FIELD
		player1NameTextField = new JFormattedTextField();
		player1NameTextField.setBounds(453, 58, 100, 26);
		player1NameTextField.setText("Server"); // Default Player 1 name is Server
		contentPane.add(player1NameTextField);
		
		// PLAYER 2 NAME FIELD
		player2NameTextField = new JFormattedTextField();
		player2NameTextField.setBounds(453, 252, 100, 26);
		player2NameTextField.setText("Client"); // Default Player 2 name is Client
		contentPane.add(player2NameTextField);
		
		player2NameTextField.setEditable(false); // Default radio button selected is Server, so Player 2's text field will only be editable if user selects Client radio button

		
		// SCORE JLABEL
		scoreLabel = new JLabel(gameArea.player1Name + ": " + gameArea.player1Score + "    " + gameArea.player2Name + ": " + gameArea.player2Score);
		scoreLabel.setBounds(20, 6, 340, 16);
		contentPane.add(scoreLabel);
		
		
		// CLIENT RADIO BUTTON (declared here  for use in server radio button action event)
		JRadioButton clientRadioButton = new JRadioButton("Client");
		buttonGroup.add(clientRadioButton);
		
		JButton connectButton = new JButton("Start");
		
		
		// SERVER RADIO BUTTON
		JRadioButton serverRadioButton = new JRadioButton("Server");
		buttonGroup.add(serverRadioButton);
		serverRadioButton.setSelected(true);
		serverRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( turn == 3) { // No one has pressed start yet.  These are default game settings.
					// If server radio button selected (I am Player 1), Player 2 name box grayed out, Player 1 name is editable
					// hostTextField disappears, client radio button unselected
					player2NameTextField.setEditable(false);
					player1NameTextField.setEditable(true);
					hostTextField.setVisible(false);
					clientRadioButton.setSelected(false);
					setTitle("NetDot - Player 1 - SERVER");
					connectButton.setText("Start");
				}
			}
		});
		serverRadioButton.setBounds(449, 23, 141, 23);
		contentPane.add(serverRadioButton);
		
		
		// CLIENT RADIO BUTTON
		//JRadioButton clientRadioButton = new JRadioButton("Client");
		clientRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(turn == 3) { // No one has pressed start yet, but the player has selected the Client radio button
					// If server radio button selected (I am Player 2), Player 1 name box grayed out, Player 2 name is editable,
					// hostTextField appears or stays, and server radio button is unselected
					player1NameTextField.setEditable(false);
					player2NameTextField.setEditable(true);
					hostTextField.setVisible(true);
					serverRadioButton.setSelected(false);
					setTitle("NetDot - Player 2 - CLIENT");
					connectButton.setText("Connect");
				}
			}
		});
		clientRadioButton.setBounds(449, 217, 141, 23);
		contentPane.add(clientRadioButton);
		
		
		// HOST TEXT FIELD
		hostTextField = new JTextField();
		hostTextField.setText("localhost"); // Default is localhost, but user can edit
		hostTextField.setBounds(453, 285, 100, 26);
		contentPane.add(hostTextField);
		hostTextField.setColumns(10);
		hostTextField.setVisible(false); // Not visible unless client radio button is selected
		
		// Create a new SockThread (extends Thread)
		sock = new SockThread();
        sock.saveParent(this); // NetDot saved as parent inside saveParent method within SockThread.java
		
		// CONNECT/START JBUTTON
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				// Close the GUI if connect button has been replaced with "Quit" and is clicked
				if(connectButton.getText() == "Quit") {
					sock.sendInfo("Q 1 1"); // Use SockThread's sendInfo method to notify other player
					                        // that current player pressed "Quit", GUI window will exit
					System.exit(0); // Exit GUI
				}
				
				// Prepare for game to start
				else {
					turn = 2; // Switch to game was started, connection not yet complete, waiting for both players to connect successfully
					
					// Begin locking in text fields to set variables in place
					if(player1NameTextField.getText().isEmpty()) player1NameTextField.setText("Server"); // Default names if blank when Start/Connect clicked
					if(player2NameTextField.getText().isEmpty()) player2NameTextField.setText("Client");
					
					player1NameTextField.setEditable(false); // Gray out name field when Start button is clicked
					gameArea.player1Name=player1NameTextField.getText(); // Pass custom player 1 name to player1Name variable
					gameArea.player1Initial=gameArea.player1Name.charAt(0); // Store 1st initial of name in player1Initial variable
					
					player2NameTextField.setEditable(false);
					gameArea.player2Name=player2NameTextField.getText();
					gameArea.player2Initial=gameArea.player2Name.charAt(0);
					
					hostTextField.setEditable(false);
					
					sock.hostName=hostTextField.getText(); // Send the hostName to sock
					sock.iAmTheServer=serverRadioButton.isSelected(); // Capture whether user is Client or Server
					sock.iAmTheClient=clientRadioButton.isSelected(); // Pass booleans to SockThread, sock
					
					// Call savePlayerNames method within SockThread.java to save your own name within array, together with default name for other player
					// The default other player name is a placeholder which will be replaced with the other player's actual name  after the thread's
					// start/run method is called.
					sock.savePlayerNames(player1NameTextField.getText(), player2NameTextField.getText());
					
					sock.start(); // Activates the run method inside SockThread.java
					              // The part of run() that gets run depends on which of the above booleans is true (whether Server or Client radio button is selected)
					
					connectButton.setText("Quit"); // Change Connect/Start button to say Quit

				}
			}
		});
		connectButton.setBounds(447, 132, 117, 29);
		contentPane.add(connectButton);
		
		
	}
	
	// BEGIN GAME METHOD - this method is called within the SockThread.java file's run method, after a connection has been made and names exchanged,
	// but before setting up the while loop to create a pathway for sharing clicks and quit information. 
	public void beginGame() {
		
		// Server and Client must have different first initials, otherwise they will receive a message to quit and restart.
		if(player2NameTextField.getText().charAt(0) == player1NameTextField.getText().charAt(0)) // Prompt if players have matching initials
		{
			scoreLabel.setText("Players must have different 1st initials. Quit + restart.");
			return;
		}
		
        // Names have been updated by run method inside SockThread, so make sure that new information is updated and reflected
		// on the gameboard. 
		player1NameTextField.setEditable(false); // Gray out name field 
		gameArea.player1Name=player1NameTextField.getText(); // Pass custom player 1 name to player1Name variable
		gameArea.player1Initial=gameArea.player1Name.charAt(0); // Store 1st initial of name in player1Initial variable
		
		player2NameTextField.setEditable(false);
		gameArea.player2Name=player2NameTextField.getText();
		gameArea.player2Initial=gameArea.player2Name.charAt(0);
		
		// Save player initials into an array to be used by MyPanel.java / gameArea
		gameArea.saveInitials(player1NameTextField.getText().charAt(0), player2NameTextField.getText().charAt(0));
		
		gameArea.isGameInPlay=true; // Change isGameInPlay boolean to true
		
		scoreLabel.setText(gameArea.player1Name + ": " + gameArea.P1ScoreAsInt() + "    " + gameArea.player2Name + ": " + gameArea.P2ScoreAsInt());
	    
		winStatusLabel.setText("turn: " + gameArea.getTurn());
		
	    gameArea.repaint(); // Call repaint
		
	}
	
	
	// OTHER PLAYER TURN METHOD - This method is called within the run method of SockThread.java to share information about a current
	// player's clicks with the other player, in order to keep the game boards of both players in sync.  When a coordinate is received via
	// the scanner, the updates have already been made on the GUI belonging to the player who made those moves, this method is for sending 
	// those updates to the other player and making sure their GUI is also updated with the same information, thereby keeping both GUIs
	// reflecting the same lines, box ownership, scores, etc.  This method mirrors all actions that take place as a result of the mouse 
	// click event on the gameboard above.
	public void otherPlayerTurn( int x, int y)
	{
		int currentX=x; // Get coordinates of click
		int currentY=y;
		
		// Check if point is within boundaries of gameboard
		if(currentX < 0 || currentX > 399 || currentY < 0 || currentY > 399) {
			return;
		}

		else { // Point is within boundary of game board, do the following
		
			gameArea.savePoint(currentX, currentY); // Save x and y coordinates
			int currentRow=y/50; // Divide by 50 to determine row and column
			int currentCol=x/50;
			gameArea.saveRowCol(currentRow, currentCol); // Save that row and column
		
			if(gameArea.whatSideOfBox()) { // If line is already true, this will return true, display message, otherwise
				                           // this line will make line true in this box and adjacent, claiming ownership if appropriate
				scoreLabel.setText("This spot is already taken");
				return;
			}
			
			gameArea.turn(); // Turn method, will change turn if box wasn't just completed
		
			gameArea.repaint(); // Call repaint
		
			winStatusLabel.setText("turn Player" + (gameArea.turn()+1) + ": " + gameArea.getTurn()); // Update turn label and score
			scoreLabel.setText(gameArea.player1Name + ": " + gameArea.P1ScoreAsInt() + "    " + gameArea.player2Name + ": " + gameArea.P2ScoreAsInt());
		
		if(!gameArea.isGameInPlay()) { // If all boxes are taken, this will return false and a winner will be announced
			scoreLabel.setText(gameArea.whoIsTheWinner());
		}
		
		} // End of else block
	} // End of otherPlayerTurn method
	
}
