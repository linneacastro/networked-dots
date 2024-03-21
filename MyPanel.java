// NAMES: Vyacheslav Lukiyanchuk and Linnea P. Castro 
// DATE: 04 June 2023
// COURSE: CSE 223
// ASSIGNMENT #: PA6

// PA6 A Networked Dots Game Written in Java using Eclipse with WindowBuilder's Swing Package
// MyPanel.java

// CLASS SYNOPSIS:
/*
The MyPanel Class includes all methods that can be called on by a main program to access
information.  These methods, when used in combination with the setters and getters in the
Box class, allow the program to have a complete picture of the status of the gameboard,
both graphically, and in terms of access to information.  

In the networked version of Dots, NetDot, the methods are unchanged, but remain central to 
keeping both players' gameboards in sync.  The main program's methods to update the current players board 
depending on click, and then send that information via thread to be updated on the other player's 
gameboard, depend on the fact that given the same information, and provided turns stay in sync,
the gameboards on both sides will display the same information, as they are operating by the same set of 
rules/methods.

These methods are used, most often within the mouse click action event in the NetDot.java file
to receive information and update the board following a user's click.  For example, the 
savePoint methods saves a click coordinate, and the saveRowCol method converts that point
into a row and column which coordinated with a specific box.  The saved variables are then 
used in the whatSideOfBox method to calculate what row and column the user's click fell into, 
what side of the box that click was proximate to, and if not already true, to set that boolean to true. 
The whatSideOfBox method is particularly important as well because it takes care of setting
adjacent boxes to true for all edges which aren't outer edges of the game board.  

The methods in this Class rely heavily on information gleaned from the setters and getters
in the Box class, and depend on for loops constructed with meticulous accuracy to iterate through box lines,
and ownership in their entirety, in order to correctly relay that information to the program when 
repaint is called in the main NetDot JFrame.  

All methods below remain unchanged from the original version of Dots, with the exception of four methods
which were deleted due to redundancy, and not used in the original Dots program nor in the NetDots version.

More detailed information on individual classes can be found below.
 */

import java.awt.Graphics;

import javax.swing.JPanel;

public class MyPanel extends JPanel {

	// CONSTRUCTOR - This method allows Dots main class to construct a MyPanel.
	public MyPanel() {
	}
	
	// VARIABLES
	int turn = 0; // Player1 is 0, player 2 is 1.
	boolean turnflag = false; // Flag for changing turns.  If true then next players turn
	                          // otherwise current player goes again.
	
	char[] playerinitials = new char [2]; // Arrays storing player initials and names
	String[] playernames = new String[2];
	
	int boxesInRow=8;
	int boxesInCol=8;
	
	int boxWidthInPixels=50;
	int boxHeightInPixels=50;
	
	int totalRows=boxesInRow;
	int totalCols=boxesInCol;
	
	boolean isGameInPlay=false; // Will change to true once start button is pressed
	
	int boxesCompletedSoFar=0; // Variables used to evaluate previous and current state of gameboard, used to determine win or if game is still in play
	int boxesNeededToWin=totalRows*totalCols;
	int boxesCompletedBeforeTurn;
	int boxesCompletedAfterTurn;
	
	String player1Name="1 Name"; // Default name and initial values
	char player1Initial='1';
	int player1Score=0;
	
	String player2Name="2 Name";
	char player2Initial='2';
	int player2Score=0;
	
	int currentX;
	int currentY;
	int currentRow;
	int currentCol;
	
	Box[][] arrayOfBoxes; // Used in createGameBoard method directly below
	
	
	// CREATE GAME BOARD METHOD - This method creates the game board by creating an array of boxes, and populating
	// each box with all the characteristics of a box, as described in the Box class.  This method includes nested
	// for loops, the outer loop iterating through all rows, and the inner loop iterating through columns
	public void createGameBoard() {
		arrayOfBoxes = new Box[boxesInRow][boxesInCol];
		for(int j=0; j<totalRows; j++) {
			for(int i=0; i<totalCols; i++) {
				arrayOfBoxes[j][i] = new Box();
			}
		}
	}
	
	
	// SAVE PLAYER INITIALS INTO ARRAY - This method saves the player initials in a playerinitials character array.
	// Player 1's initial will be in the 0 index spot, Player 2's initial will be in the 1 index spot. 
	public void saveInitials(char player1initial, char player2initial)
	{
		playerinitials[0] = player1initial;
		playerinitials[1] = player2initial;
	}
	
	
	// TURN CHECKING METHOD - This method uses the boolean variable turnflag.  After turn is called,
	//a new turn is calculated.  Answer will always be either a 1 or 0. 
	public int turn() {
		if (turnflag) { // If turnflag is true...next player's turn, if false, current player goes again
			turn = (turn+1)%2; // set turn to (current turn+1)%2, answer will be 1 or 2, directly corresponding with playerinitial array indices.
			turnflag = false;
		}
		
		return turn;
	}
	
	
	// GET TURN METHOD - Turn will be set based on results of the turn method above, which will set the turn
	//variable.  
	public char getTurn() {
		return playerinitials[turn]; // Player1 is 0, player 2 is 1.  Turn value calculated in turn method above.
	}
		
		
	// WHO OWNS THIS BOX METHOD - This method takes in a row and column, and outputs the ownership character associated
	// with that specific box.
	public char returnOwnership(int row, int col) {
		char ownership;
		ownership=arrayOfBoxes[row][col].whoOwnsThisBox();
		return(ownership);
	}
	
		
	// CHECK FOR COMPLETED BOX AND CLAIM BOX METHOD - Using the boxed method inside the Box class, if a box has
	// all 4 walls set to true, the player whose turn it is currently, will have their initial placed inside
	// the box, claiming ownership.  Turnflag is set to false to keep it on current player's turn, 
	// meaning a new turn will not be calculated.
	public void claimBox() {
		if(arrayOfBoxes[currentRow][currentCol].boxed()) {
			arrayOfBoxes[currentRow][currentCol].setWhoOwnsThisBox(playerinitials[turn]); // Claim ownership of box with placement of initials corresponding with current turn of player.
			turnflag = false;  // Box was just completed, current player gets to go again
		}
	}
	
	
	// CALCULATE PLAYER 1 SCORE AS INT - This method calculates Player 1's score and is used by the score label in the 
	// main program to display player 1's cumulative score.  
		public int P1ScoreAsInt() {
			player1Score = 0;  // Scores must be reset to 0 before recounting each time, otherwise score will compound/be cumulative and not be accurate.
			for(int j=0; j<totalRows; j++) {
				for(int i=0; i<totalCols; i++) {
					if(arrayOfBoxes[j][i].whoOwnsThisBox()==playerinitials[0]) { // Comparing ownership of box to Player 1's initials
						player1Score++; // Incrementing if they match.
					}
				}
			}
			return(player1Score); // Returning Player 1's score as an int
		}
		
	
	// CALCULATE PLAYER 2 SCORE AS INT - This method calculates Player 2's score and is used by the score label in the 
	// main program to display player 2's cumulative score. 
	public int P2ScoreAsInt() {
		player2Score = 0; // Scores must be reset to 0 before recounting, otherwise score will compound/be cumulative and not be accurate.
		for(int j=0; j<totalRows; j++) {
			for(int i=0; i<totalCols; i++) {
				if(arrayOfBoxes[j][i].whoOwnsThisBox()==playerinitials[1]) {
					player2Score++; 
				}
			}
		}
		return(player2Score); // Player 2's score is returned as an integer to the main program.
	}
		
		
	// IS THE GAME IN PLAY? / HAVE ALL BOXES BEEN CLAIMED? METHOD - This method checks to see if all boxes have
	// been completed, comparing that with total possible boxes (rows*cols) as calculated in variables above.
	// Used by main program to see if it is time to declare a winner.
	public boolean isGameInPlay() {
		boxesCompletedSoFar = 0; // Initialize to 0
		for(int j=0; j<totalRows; j++) {
			for(int i=0; i<totalCols; i++) {
				if (arrayOfBoxes[j][i].whoOwnsThisBox() != ' ') { // If Box is something other than ' ' single space (it has an initial in it)
					boxesCompletedSoFar++; // Increment boxesCompletedSoFar
				}
			}
		}
		if(boxesCompletedSoFar==boxesNeededToWin) { // All boxes have been claimed with an initial
			isGameInPlay=false; // Change boolean to false
			return(isGameInPlay); // Return isGameInPlay status to main program
		}
		else { // Game is still in play, no winner yet
			isGameInPlay=true; // Game is still in play
			return(isGameInPlay); // Return isGameInPlay status to main program
		}
	}
		
		
	// CALCULATE WINNER METHOD - This method is called once all boxes have been claimed, ie. until the
	// above isGameInPlay method returns false.  It tallies up ownership of all boxes.  
	// The player with the most boxes claimed wins.  If number of boxes claimed is equal, a tie is 
	// called.
	public String whoIsTheWinner() {
		String winner="Tie Game!";
		if(boxesCompletedSoFar==boxesNeededToWin){
			if (P1ScoreAsInt()>P2ScoreAsInt()) {
				winner=player1Name + " is the Winner";
			}
			if (P1ScoreAsInt()<P2ScoreAsInt()) {
				winner=player2Name+ " is the Winner";
			}
			else{
				winner="Tie Game!";
			}
		}
		return (winner);
	}
	
	
		
	// SAVE X AND Y POINT FROM MOUSE CLICK - This method saves an x and y coordinate from the main program.  The point will get passed in
	// as long as x and y values are between 0 and 399 inclusive (as specified in Dots.java).
	public void savePoint(int x, int y) {
		currentX=x;
		currentY=y;
	}
		
		
	// SAVE ROW AND COL FROM MOUSE CLICK METHOD - This method allows a coordinate that has been converted by the main program
	// into a row and column to be saved as currentRow and currentCol to be used in succession with other methods,
	// such as the one below, whatSideOfBox.
	public void saveRowCol(int row, int col) {
		currentRow=row;
		currentCol=col;
	}
				

	// WHAT SIDE OF BOX AM I CLICKING? *AND* CLAIMING ADJACENT BOX METHOD - This method is quite an important one, as
	// it determines which side of the box the user's click is closest to and also claims adjacent boxes,
	// for all sides that are not outer edges of the box.  This method returns a boolean to the main program.
	// It is used to determine if a message needs to be displayed to the user saying that the line they are clicking
	// has already been taken.
	public boolean whatSideOfBox() {
		int distToTop;
		int distToBottom;
		int distToLeft;
		int distToRight;
							
		distToTop=currentY-currentRow*boxHeightInPixels; // Calculate distance from click coordinate to each side of box
		distToBottom=((currentRow*boxHeightInPixels)+boxHeightInPixels)-currentY;
		distToRight=((currentCol*boxWidthInPixels)+boxWidthInPixels)-currentX;
		distToLeft=currentX-currentCol*boxWidthInPixels;
					
		turnflag = true;
		
		// TOP SIDE
		if(distToTop < distToBottom && distToTop < distToRight && distToTop < distToLeft) {
			if(arrayOfBoxes[currentRow][currentCol].top) { // If this is already true
				//illegal move
				//parent.warningLabel.setText("Line already claimed");
				turnflag = false; // Keep it on same player's turn
				return true;
			}
			else {
				arrayOfBoxes[currentRow][currentCol].setTop(true); // Not already true, set to true
				//only claim box if valid turn
				claimBox();
				if(currentRow > 0) { // Check adjacent row
					arrayOfBoxes[currentRow-1][currentCol].setBottom(true); // If currentRow > 0, change row above it's bottom line to true 
					// Claim adjacent box if all sides of it are true
					if(arrayOfBoxes[currentRow-1][currentCol].boxed()) { // Check to see if that adjacent boxes sides are all true
						arrayOfBoxes[currentRow-1][currentCol].setWhoOwnsThisBox(playerinitials[turn]); // Claim that adjacent box
						turnflag = false; // Adjacent box completed, keep it on current player's turn
					}
				}
			}
		}
		
		//BOTTOM SIDE
		else if(distToBottom < distToTop && distToBottom < distToRight && distToBottom < distToLeft) {
			if(arrayOfBoxes[currentRow][currentCol].bottom) {
				//illegal move
				turnflag = false;
				return true;
			}
			else {
				arrayOfBoxes[currentRow][currentCol].setBottom(true);
				//only claim box if valid turn
				claimBox();
				if(currentRow < totalRows-1) { // Check adjacent row
					arrayOfBoxes[currentRow+1][currentCol].setTop(true); // If we're not at the bottom most row, go to spot below that box, and set it's top boolean to true
					// Claim adjacent box if all sides are true
					if(arrayOfBoxes[currentRow+1][currentCol].boxed()) {
						arrayOfBoxes[currentRow+1][currentCol].setWhoOwnsThisBox(playerinitials[turn]);
						turnflag = false;
					}
				}
			}
		}
		
		// RIGHT SIDE
		else if(distToRight < distToTop && distToRight < distToBottom && distToRight < distToLeft) {
			if(arrayOfBoxes[currentRow][currentCol].right) {
				//illegal move
				turnflag = false;
				return true;
			}
			else {
				arrayOfBoxes[currentRow][currentCol].setRight(true);
				//only claim box if valid turn
				claimBox();
				if(currentCol < totalCols-1) { // Check adjacent column
					arrayOfBoxes[currentRow][currentCol+1].setLeft(true); // If we're not at the right most column, move to the right on and set the left bool on that box to true
					// Claim adjacent box if all 4 sides are true
					if(arrayOfBoxes[currentRow][currentCol+1].boxed()) {
						arrayOfBoxes[currentRow][currentCol+1].setWhoOwnsThisBox(playerinitials[turn]);
						turnflag = false;
					}
				}
			}
		}
		
		// LEFT SIDE
		else {
			// This is a more generalized catchall than an if statement with specific inequalities.
			// If all other inequalities fail, this will be executed.
			if(arrayOfBoxes[currentRow][currentCol].left) {
				//illegal move
				turnflag = false;
				return true;
			}
			else {
				arrayOfBoxes[currentRow][currentCol].setLeft(true);
				//only claim box if valid turn
				claimBox();
				if(currentCol > 0) { // Check adjacent column
						arrayOfBoxes[currentRow][currentCol-1].setRight(true); // If we're not at the left most column, move to the left one column and set the right bool on that box to true
					// Claim adjacent box if all 4 sides are true
					if(arrayOfBoxes[currentRow][currentCol-1].boxed()) {
						arrayOfBoxes[currentRow][currentCol-1].setWhoOwnsThisBox(playerinitials[turn]);
						turnflag = false;
				    }
									
				}
			}
		}
		
		return false;
	}
		
	
	//PAINT METHOD - The paint method draws the dots, all true lines, and initials within each box, if claimed. 
	// The paint method is called through repaint at the end of each mouse click on the gameboard after the game has been started.
	// Repaint is also called after starting or resetting the game.  
	public void paint(Graphics g) {
		super.paint(g); 
		
		// DRAW THE DOTS
		for(int j=0; j<=totalRows; j++) { // This for loop will increment rows
			for(int i=0; i<=totalCols; i++) { // This for loop will increment columns 
				if(arrayOfBoxes==null) return; // Do not paint if this array has not yet been initialized, crucial line for keeping Design tab visible
				g.drawOval(i*50, j*50, 3, 3); // Make 50x50 squares by drawing 3x3 pixel dots in each corner
			}
		}
			
		// DRAW THE BOXES
		for(int j=0; j<totalRows; j++) {
			for(int i=0; i<totalCols; i++) {
					
				if(arrayOfBoxes[j][i].top()==true) {
					// Draw top line
					g.drawLine(i*boxWidthInPixels, j*boxHeightInPixels, (i*boxWidthInPixels)+boxWidthInPixels, j*boxHeightInPixels);
				}
				if(arrayOfBoxes[j][i].bottom()==true) {
					// Draw bottom line
					g.drawLine(i*boxWidthInPixels, (j*boxHeightInPixels)+boxHeightInPixels, (i*boxWidthInPixels)+boxWidthInPixels, (j*boxHeightInPixels)+boxHeightInPixels);
				}
				if(arrayOfBoxes[j][i].left()==true) {
					// Draw left line
					g.drawLine(i*boxWidthInPixels, j*boxHeightInPixels, i*boxWidthInPixels, (j*boxHeightInPixels)+boxHeightInPixels);
				}
				if(arrayOfBoxes[j][i].right()==true) {
					// Draw right line
					g.drawLine((i*boxWidthInPixels)+boxWidthInPixels, j*boxHeightInPixels, (i*boxWidthInPixels)+boxWidthInPixels, (j*boxHeightInPixels)+boxHeightInPixels);
				}
			}
		}
			
		// DRAW INITIALS
		for(int j=0; j<totalRows; j++) {
			for(int i=0; i<totalCols; i++) {
				//String populateBox;
				//populateBox=String.valueOf(arrayOfBoxes[j][i].whoOwnsThisBox);
				g.drawString(arrayOfBoxes[j][i].whoOwnsThisBox + "", i*boxWidthInPixels+25, j*boxHeightInPixels+30);			
			}				
		}
			
		
		} // End of paint method
	
		
}

