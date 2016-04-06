 This program implemnts  a graphic drawing program using Reagent, ClojureScript, and big ratom.
 
The program displays a drawing area, a palette to select a drawing mode, and undo button in a
web browser.
The drawing program supports drawing lines, circles and rectangles.
The palette allows the user to select a line, a circle of a rectangle. When the user selects a line the
program is in line mode. Then when the user clicks in the drawing area that location becomes
the starting point of a line. 
As the user moves the mouse a line is drawn from the starting point
to the mouse cursor. When user clicks the mouse a second time the line is finished. The location
of the second mouse click becomes the end of the line. After the second mouse click nothing
is drawn on the screen until the user clicks again. When drawing a circle, the first click determines
the location of the center of the circle. The second click determines the radius or size
of the circle. When drawing hey rectangle the first click determines the location of the corner of
the rectangle. The second mouse click determines location of the opposite corner of the rectangle.
The drawing area of the program retains all figures drawn on it until the user clears the
screen or undoes operations. Undo should undo drawing of figures and selecting the drawing
mode. A single undo will undo the drawing of one figure or one mode selection. For the drawing
area use the svg (Scalar Vector Graphics) tag of HTML.
