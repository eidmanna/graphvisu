** GraphVisu **

Clone to github of https://sourceforge.net/projects/graphvisu/ by Julien Gaffuri

The purpose of this program is:
- to visualise graphs
- to generate dependencies graphs of a set of XSD files.

INSTALLATION REQUIREMENTS
Java 1.6 or later. To check if java is installed and the version, launch the command "java -version". If not installed, the installation program can be found on http://www.java.com/

QUICK START
1. Write in the file "in.txt" the list of xsd files URLs to parse (one URL per line).
2. Build the xsd files graph by double clicking on "buildgraph.bat" (or "buildgraph.sh" for linux users).
3. Visualise the graph by double clicking on "viewgraph.bat" (or "viewgraph.sh" for linux users). Pan with left click, move a node with right click, zoom in and out with mouse wheel, visualise a node characteristic by flying over it.


INSTRUCTIONS

1. The first step is to build the graph using the buildgraph.jar program. This program takes a list of xsd files URLs and build recursively the dependency graph taken these XSD files as 'seeds'. For a given XSD file, the dependending files are the ones specified in the "include" and "import" XML elements.
Usage: "java -jar buildgraph.jar inPath outPath logPath"
with
"inPath": Path of the input file listing all URLs to include in the graph. One URL per line. Empty lines are allowed. URLs to be excluded should start with a "-". Lines starting with "//" are ignored. Default is "in.txt".
"outPath": Path of the output file representing the XSD files graph. Default is "out.txt".
"logPath": Path of an output log file. Default is "log.txt".

2. The graph is visualised using the viewgraph.jar program. Pan with left click, zoom in and out with mouse wheel, visualise a node characteristic by flying over it. Nodes are displayed as black dots and links as black lines with an arrow in the middle showing the dependency direction. Nodes representing XSD files without any dependency (that is with only incoming links) are displayed in green. Nodes representing XSD files with only dependencies (that is with only outgoing links) are displayed in red. The the graph's 'strongly connected components' are shown with a translucent gray halo. Graphic constraints are used to improve the graph visualisation. For example, the most generic files sould be forced to go at the bottom and the most specific at the top. For local exploration, it is possible to move some nodes with the mouse right button.
Usage: "java -jar viewgraph.jar graphFilePath"
with
"graphFilePath": Path of the output file representing the graph to visualise. Default is "out.txt".

NB:
Both programs are independant. The graph visualisation program may be used for any other kind of directed graph.

