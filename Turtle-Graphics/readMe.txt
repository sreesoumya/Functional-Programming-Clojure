Turtle graphics program :

This is a  Clojure program to implement turtle graphics.It supports the turtle operations like
moving, turning, and raising/lowering a pen to draw, undo, step ,run and changing the color of the pen.
The turtle language consists of the following operations.
pen up -> When the pen is up and the turtle moves nothing is drawn.
pen down ->When the pen is down and the turtle moves a black line is drawn
move ->The move operation requires an amount to move. This operation moves the turtle
given amount in the current direction.
turn ->The turn operation requires an amount to turn. This operation turns the direction
of the turtle the given amount in degrees.(convered degress to radiants)
When running a Turtle program the result of the turtle’s pen is shown in a window.
A turtle program is basically a list of turtle commands.
This Turtle program runs in two modes: run and step. In the step mode the user can step the
program forward one operation by pressing the forward arrow key. The user can undo the last
operation by pressing the back arrow key. The user can undo back to the start of the turtle program.
Each time the program executes an operation it prints the operation in the display window.
A turtle program starts in the step mode. Pressing the “r” key changes to the run mode. In
the run mode the turtle program is executed without the user having to press the arrow keys.
When the turtle program comes to the end we switch back to step mode so the user can step
the program backwards.

Graphics
Used the Clojure library Quil to draw in a window. 
Quil does not handle menus, button and other standard GUI element. It just
handles drawing, keyboard and mouse events. 
Part of the program deals with maintaining state in Clojure.

Description:
Created a Clojure program that reads a turtle program from a file. 
The program executes the turtle program as described above and displays the graphics in a Quil window.
Have included the file in the directory containing the Clojure project and used relative path to read the file.
Each turn command is be followed by at least one move command.
Part of the requirement was that a move command must occur between every other move.
There were several issues to address. First is how to represent the turtle program. (Hint maps
are your friend.). Second is how to maintain state and keep track of where you are in the turtle
program. 
Also,Created a leiningen project for this program. 
