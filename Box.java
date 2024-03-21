// NAMES: Vyacheslav Lukiyanchuk and Linnea P. Castro 
// DATE: 04 June 2023
// COURSE: CSE 223
// ASSIGNMENT #: PA6

// PA6 A Networked Dots Game Written in Java using Eclipse with WindowBuilder's Swing Package
// Box.java

// CLASS SYNOPSIS:
/*
The Box class is foundational for describing characteristics of the boxes which will
eventually become the array of boxes the gameboard is comprised of.  "Boxes" become the 
currency that the methods in MyPanel iterate through, count up, and compare with. 

The Box class consists of a main constructor, getters, setters, and an additional method
that lets the MyPanel class determine if a box has all 4 booleans set to true, which precedes
putting current player's initials inside that box. 

The Box class remains unchanged from its form in the original Dots game, with the exception of 
minor comment editing.
*/


public class Box {
 
	    // VARIABLES IN BOX CLASS
		char whoOwnsThisBox; // Box ownership, initialized to single space, to be replaced w player initial
		boolean top;
		boolean bottom;
		boolean right;
		boolean left;
		
		
		// CONSTUCTOR METHOD - This method created a single box and describes all of its characteristics.  All
		// booleans are initialized to false and ownership is a single blank space character.
		public Box() {          
		    whoOwnsThisBox=' '; // Initialize ownership to blank space; nothing will show up when repaint is called
		    
			top=false; // Box edges are booleans initialized to false, all unowned to start
			bottom=false;
			right=false;
			left=false;
		}
		
		
		// GETTERS - The Getters allow status of booleans to be accessed, so the MyPanel class can determine
		// if a line has already been drawn/is true, for example.  Information about ownership can also be
		// retrieved and is used by methods in MyPanel to count up Player scores. 
		public boolean top() {
			return(top);
		}
		
		public boolean bottom() {
			return(bottom);
		}
		
		public boolean right() {
			return(right);
		}
		
		public boolean left() {
			return(left);
		}
		
		public char whoOwnsThisBox() {
			return(whoOwnsThisBox);
		}
		
		// SETTERS - Allow ownership to be claimed, as well as individual box sides to be changed from false to true
		// when claimed within the game.
		public void setTop(boolean top) {
			this.top = top;
		}
		
		public void setBottom(boolean bottom) {
			this.bottom = bottom;
		}
		
		public void setRight(boolean right) {
			this.right = right;
		}
		public void setLeft(boolean left) {
			this.left = left;
		}
		public void setWhoOwnsThisBox(char whoOwnsThisBox) {
			this.whoOwnsThisBox = whoOwnsThisBox;
		}
		
		// IS BOX COMPLETE? ("BOXED") METHOD - This method makes parent code in MyPanel cleaner, cutting down on
		// boolean comparision and replacing this with a single boolean value, true if and only if all 4 edges are true.
		public boolean boxed()
		{
			return top && bottom && right && left;
		}
	
}

