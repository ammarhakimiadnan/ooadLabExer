# Drawing Studio Pro

**Overview**
Drawing Studio Pro is a Java Swing application that provides a dual-canvas interface for creating and manipulating images. The left canvas allows for precise image manipulation with rotation, scaling, and flipping capabilities, while the right canvas offers freehand drawing tools and basic image placement.

**Installation Instructions**
Running from Source
1. Clone or download the repository to your local machine.
2. Ensure you have JDK installed (version 8 or later recommended).
3. Open a terminal/command prompt in the project directory.
4. Compile the project:
    `javac Main.java`
5. Run the application:
    `java Main`

**How to Use the Application**
**Interface Overview**
The application features:
1. Left Canvas: For precise image manipulation
2. Right Canvas: For freehand drawing and image placement
3. Toolbar: Contains all control buttons at the bottom

**Left Canvas Features**
1. Adding Images:
    - Click the animal or flower icons to add predefined images
    - Use the "Load Image" button to add custom images

2. Manipulating Images:
    - Click and drag to move images
    - Use corner handles to scale images
    - Use the rotation handle (top-center) to rotate images
    - Click edge handles to flip images horizontally or vertically

3. Canvas Controls:
    - Rotate: Rotate the entire canvas 90°
    - Delete: Remove the selected image
    - Compose: Open a preview window with save options
    - New Canvas: Create a canvas with custom dimensions

**Right Canvas Features**
1. Drawing Tools:
    - Left-click and drag to draw
    - Right-click to change pen color
    - Use the slider to adjust pen size
    - Toggle the eraser button to erase drawings

2. Image Controls:
    - Double-click an image to select it
    - Drag selected images to reposition them

3. Canvas Controls:
    - Clear: Remove all drawings and images
    - Load Image: Add an image to the canvas
    - Save: Save the current canvas state

**Saving Your Work**
Both canvases can be saved independently:
1. Click the respective save button
2. Choose a location and filename
3. Select PNG or JPG format
4. Click "Save"