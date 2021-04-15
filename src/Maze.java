import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import javalib.worldcanvas.WorldCanvas;

import java.awt.Color;
import javalib.worldimages.*;

/*
HashMap<String, String> representatives;
List<Edge> edgesInTree;
List<Edge> worklist = all edges in graph, sorted by edge weights;
 
initialize every node's representative to itself
While(there's more than one tree)
  Pick the next cheapest edge of the graph: suppose it connects X and Y.
  If find(representatives, X) equals find(representatives, Y):
    discard this edge  // they're already connected
  Else:
    Record this edge in edgesInTree
    union(representatives,
          find(representatives, X),
          find(representatives, Y))
Return the edgesInTree
*/

// represents a node that is connected by a list of edges
class Node {

  // We are aware there probably should be more fields in Node
  // but for now, we are just focused on creating the Maze itself
  ArrayList<Edge> outedges;

  Node(ArrayList<Edge> outedges) {
    this.outedges = outedges;
  }

  // empty constructor if the constructed Node has no edges
  Node() {
    this.outedges = new ArrayList<Edge>();
  }

  // EFFECT:connects this Node to the given Node through an edge
  // of the given weight
  // returns the edge that was created to connect the two nodes
  public Edge connect(Node other, int weight) {
    Edge temp = new Edge(this, other, weight);
    this.outedges.add(temp);
    other.outedges.add(temp);
    return temp;
  }

  // returns true if this node is connected to the given node
  // through any of its outedges
  public boolean isConnected(Node other) {
    for (Edge e : this.outedges) {
      if (e.connectsThisToThat(this, other)) {
        return true;
      }
    }
    return false;
  }

  // removes the given edge from this.outedges
  public void remove(Edge e) {
    this.outedges.remove(e);
  }
}

// represents an edge that connects the two nodes
class Edge {
  Node from, to;
  int weight; // how expensive is the edge;

  public Edge(Node from, Node to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  // returns true if this edge connects the two given Nodes
  public boolean connectsThisToThat(Node n1, Node n2) {
    return (this.from.equals(n1) && this.to.equals(n2))
        || (this.from.equals(n2) && this.to.equals(n1));
  }

  // removes this edge from both from and to
  public void removeThisEdge() {
    this.from.remove(this);
    this.to.remove(this);
  }
}

class KruskalMaze {
  HashMap<Node, Node> representatives;
  // all edges in graph, sorted by edge weights
  ArrayList<Edge> worklist;
  // all edges that are not used in the algorithm
  ArrayList<Edge> leftOver;

  KruskalMaze(HashMap<Node, Node> representatives, ArrayList<Edge> worklist) {
    this.representatives = representatives;
    this.worklist = worklist;
    this.leftOver = new ArrayList<Edge>();
  }

  // Kruskal's Algorithm utilized for constructing the maze
  ArrayList<Edge> algorithm() {
    HashUtils<Node> util = new HashUtils<Node>();
    ArrayList<Edge> resultEdges = new ArrayList<Edge>();
    // we know algo is done when edges in tree equals the number of nodes - 1;
    while (resultEdges.size() != this.representatives.size() - 1) {
      Edge e = this.worklist.get(0);
      // checks to see if this edge creates a loop
      if (util.find(this.representatives, e.from).equals(util.find(this.representatives, e.to))) {
        this.leftOver.add(this.worklist.remove(0));
      }
      else {
        resultEdges.add(e);
        util.union(this.representatives, util.find(this.representatives, e.from),
            util.find(this.representatives, e.to));
        this.worklist.remove(0);
      }
    }
    this.leftOver.addAll(this.worklist);
    return resultEdges;
  }

  // removes all of this.leftover from its connected parts
  void removeAllLeftOver() {
    for (Edge e : this.leftOver) {
      e.removeThisEdge();
    }
  }
}

//this class is generalized even though it is only used in one specific case
//utilities for Hashmaps for Nodes
class HashUtils<T> {
  // returns the highest representative of the given node
  T find(HashMap<T, T> reps, T node) {
    // if this node equals the given node, return it
    if (node.equals(reps.get(node))) {
      return node;
    }
    else {
      // if not, use the same hashmap and the node that this node points to
      return this.find(reps, reps.get(node));
    }
  }

  // sets rep1's representative to rep2
  void union(HashMap<T, T> reps, T rep1, T rep2) {
    reps.replace(rep1, rep2);
  }

}

//class that represents the Maze

class MazeGame extends World {
  ArrayList<ArrayList<Node>> mazeBoard;
  // one value that controls the size of the board
  int squareSize;

  public MazeGame(int x, int y, int sqSize, Random rand) {
    this.mazeBoard = this.makeConnectedBoard(x, y, rand);
    this.squareSize = sqSize;
  }

  public MazeGame(int x, int y, int sqSize) {
    this(x, y, sqSize, new Random());
  }

  // creates the board with x length and y height
  // where each cell is connected to each neighboring cell through an
  // edge with a random length anywhere from 0 to 99
  // also creates a Hashmap for the Nodes to be used Kruskal's algo
  // Then, it takes out all of the edges that are not a part of Kruskal's
  // connections
  public ArrayList<ArrayList<Node>> makeConnectedBoard(int x, int y, Random rand) {
    // hashmap that represents what each node's representative is
    HashMap<Node, Node> boardHash = new HashMap<Node, Node>();
    // all of the edges in the board
    ArrayList<Edge> allEdges = new ArrayList<Edge>();
    ArrayList<ArrayList<Node>> resultBoard = new ArrayList<ArrayList<Node>>();
    for (int i = 0; i < y; i += 1) {
      ArrayList<Node> rowI = new ArrayList<Node>();
      for (int j = 0; j < x; j += 1) {
        Node temp = new Node();
        rowI.add(temp);
        // ensures that this node is added to the hashmap and maps to itself
        boardHash.put(temp, temp);
        // kruskalEdges starts with all of the edges in the list
        if (i > 0) {
          allEdges.add(temp.connect(resultBoard.get(i - 1).get(j), rand.nextInt(100)));
        }
        if (j > 0) {
          allEdges.add(temp.connect(rowI.get(j - 1), rand.nextInt(100)));
        }
      }
      resultBoard.add(rowI);
    }

    this.sortEdges(allEdges);
    KruskalMaze kru = new KruskalMaze(boardHash, allEdges);
    // creates a path through all of the cells, also filling the leftover field with
    // all the unused connections between the neighbor nodes
    kru.algorithm();
    // removes all of the leftoverpaths that are not used in kruskal's algo
    kru.removeAllLeftOver();

    return resultBoard;
  }

  // sorts the edges to be put into kruskal's algorithm
  public void sortEdges(ArrayList<Edge> input) {
    input.sort(new CompareByWeight());
  }

  // draws the maze

  public WorldImage drawMaze() {
    WorldImage bkg = new EmptyImage();
    for (int i = 0; i < this.mazeBoard.size(); i += 1) {
      WorldImage row = new EmptyImage();
      for (int j = 0; j < this.mazeBoard.get(i).size(); j += 1) {
        WorldImage cell = new RectangleImage(this.squareSize, this.squareSize, OutlineMode.SOLID,
            Color.gray);
        if (i == 0 && j == 0) {
          cell = new RectangleImage(this.squareSize, this.squareSize, OutlineMode.SOLID,
              Color.green);
        }
        else if (i == this.mazeBoard.size() - 1 && j == this.mazeBoard.get(i).size() - 1) {
          cell = new RectangleImage(this.squareSize, this.squareSize, OutlineMode.SOLID,
              Color.magenta);
        }

        // if the cell to the left is not connected, create a cell with a wall to the
        // left
        if (j > 0 && !this.mazeBoard.get(i).get(j).isConnected(this.mazeBoard.get(i).get(j - 1))) {
          cell = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE,
              new RectangleImage(1, this.squareSize, OutlineMode.SOLID, Color.black), 0, 0, cell);
        }
        // if the cell above this cell is not connected, create a cell with a wall above
        if (i > 0 && !this.mazeBoard.get(i).get(j).isConnected(this.mazeBoard.get(i - 1).get(j))) {
          cell = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP,
              new RectangleImage(this.squareSize, 1, OutlineMode.SOLID, Color.black), 0, 0, cell);
        }
        row = new BesideImage(row, cell);
      }
      bkg = new AboveImage(bkg, row);
    }
    // return new OverlayImage(bkg, new
    // RectangleImage(this.mazeBoard.get(0).size()*this.squareSize+ 5,
    // this.mazeBoard.size() * this.squareSize + 5, OutlineMode.SOLID,
    // Color.black));
    return bkg;
  }

  // makes the scene
  public WorldScene makeScene() {
    WorldScene bkg = this.getEmptyScene();
    bkg.placeImageXY(this.drawMaze(), this.getMazeWidth() / 2, this.getMazeHeight() / 2);
    return bkg;
  }

  // returns the width of the screen for the current maze game
  public int getMazeWidth() {
    return this.mazeBoard.get(0).size() * this.squareSize;
  }

//returns the height of the screen for the current maze game
  public int getMazeHeight() {
    return this.mazeBoard.size() * this.squareSize;
  }

}

class CompareByWeight implements Comparator<Edge> {

  @Override
  public int compare(Edge o1, Edge o2) {
    return o1.weight - o2.weight;
  }
}

//represents examples for the maze;
class ExamplesMaze {
  Node node1, node2, node3, node4, node5, node6, n1, n2, n3, node7, node8;
  Edge edge1, edge2, edge3, edge4, edge5, edge6, edge7, edge8, e1, e2, e3, edge9;
  ArrayList<Edge> edges1, edges2;
  HashMap<Node, Node> reps, reps2;
  ArrayList<Edge> worklist1, worklist2;
  KruskalMaze kruskal1, kruskal2;
  MazeGame g1, g2, g3, g4;

// creates the initial conditions
  void initConditions() {
    // constructs the same tree/map as the one in lecture notes
    node1 = new Node();
    node2 = new Node();
    node3 = new Node();
    node4 = new Node();
    node5 = new Node();
    node6 = new Node();
    node7 = new Node();
    node8 = new Node();
    n1 = new Node();
    n2 = new Node();
    n3 = new Node();
    edge1 = this.node1.connect(node2, 10);
    edge2 = this.node2.connect(node3, 20);
    edge3 = this.node3.connect(node4, 30);
    edge4 = this.node4.connect(node6, 40);
    edge5 = this.node2.connect(node6, 50);
    edge6 = this.node3.connect(node5, 60);
    edge7 = this.node2.connect(node5, 70);
    edge8 = this.node1.connect(node5, 80);
    edge9 = this.node7.connect(node8, 10);
    e1 = this.n1.connect(n3, 15);
    e2 = this.n1.connect(n2, 25);
    e3 = this.n2.connect(n3, 35);

    edges1 = new ArrayList<Edge>(Arrays.asList(this.edge2, this.edge3, this.edge1));
    edges2 = new ArrayList<Edge>(Arrays.asList(this.edge8, this.edge5, this.edge7, this.edge6));

    g1 = new MazeGame(100, 60, 10);
    g2 = new MazeGame(20, 20, 10);
    g3 = new MazeGame(50, 50, 20);
    g4 = new MazeGame(2, 2, 20);

    reps = new HashMap<Node, Node>();
    reps2 = new HashMap<Node, Node>();
    // Put all the data into the hashtable
    reps.put(node1, node1);
    reps.put(node2, node2);
    reps.put(node3, node3);
    reps.put(node4, node4);
    reps.put(node5, node5);
    reps.put(node6, node6);

    reps2.put(n1, n1);
    reps2.put(n2, n2);
    reps2.put(n3, n3);

    worklist1 = new ArrayList<Edge>(
        Arrays.asList(edge1, edge2, edge3, edge4, edge5, edge6, edge7, edge8));
    worklist2 = new ArrayList<Edge>(Arrays.asList(e1, e2, e3));
    kruskal1 = new KruskalMaze(this.reps, this.worklist1);
    kruskal2 = new KruskalMaze(this.reps2, this.worklist2);

  }

// tests the InitConditions method
  void testInitConditions(Tester t) {
    this.initConditions();
    // Get the data
    t.checkExpect(reps.get(node1), node1);
    t.checkExpect(reps.get(node2), node2);
    // Check that some data is present
    t.checkExpect(reps.containsKey(node1), true);
    t.checkExpect(reps.containsKey(node4), true);
    // Data that isn't present will return null
    t.checkExpect(reps.get(n1), null);
  }

// tests the connect method in the Node class
  void testConnect(Tester t) {
    this.initConditions();
    t.checkExpect(this.node1.outedges.size(), 2);
    t.checkExpect(this.node2.outedges.size(), 4);
    t.checkExpect(this.node1.connect(node2, 10), new Edge(node1, node2, 10));
    t.checkExpect(this.node1.outedges.size(), 3);
    t.checkExpect(this.node2.outedges.size(), 5);
    t.checkExpect(this.node1.outedges.get(2), new Edge(node1, node2, 10));
    t.checkExpect(this.node2.outedges.get(4), new Edge(node1, node2, 10));
  }

// tests the isConnected method in the Node class
  void testIsConnected(Tester t) {
    this.initConditions();
    t.checkExpect(this.node1.isConnected(node6), false);
    t.checkExpect(this.node7.isConnected(node8), true);
    t.checkExpect(this.node2.isConnected(node3), true);
    t.checkExpect(this.n1.isConnected(n2), true);

  }

//tests the remove method in the Node class
  void testRemove(Tester t) {
    this.initConditions();
    t.checkExpect(this.node1.outedges.size(), 2);
    this.node1.remove(edge8);
    t.checkExpect(this.node1.outedges.size(), 1);
    t.checkExpect(this.node2.isConnected(node3), true);
    t.checkExpect(this.n1.isConnected(n2), true);

  }

//tests the connectsThisToThat method in the Edge class
  void testConnectsThisToThat(Tester t) {
    this.initConditions();
    t.checkExpect(this.edge1.connectsThisToThat(n2, n2), false);
    t.checkExpect(this.edge2.connectsThisToThat(node2, node3), true);
    t.checkExpect(this.edge3.connectsThisToThat(node2, node4), false);
    t.checkExpect(this.edge4.connectsThisToThat(node4, node6), true);
  }

//tests the removeThisEdge method in the Edge class 
  void testRemoveThisEdge(Tester t) {
    this.initConditions();
    t.checkExpect(this.node1.outedges.size(), 2);
    this.edge1.removeThisEdge();
    t.checkExpect(this.node1.outedges.size(), 1);
    this.edge8.removeThisEdge();
  }

// tests the find method in the HashUtils class
  void testFind(Tester t) {
    this.initConditions();
    t.checkExpect(new HashUtils<Node>().find(reps, node1), node1);
    this.kruskal1.representatives.replace(node2, node1);
    t.checkExpect(new HashUtils<Node>().find(reps, node2), node1);
  }

// tests the union method in the HashUtils class
  void testUnion(Tester t) {
    this.initConditions();
    t.checkExpect(this.reps.get(node1), node1);
    t.checkExpect(this.reps2.get(n1), n1);
    new HashUtils<Node>().union(reps, node1, node2);
    new HashUtils<Node>().union(reps2, n1, n3);
    t.checkExpect(this.reps.get(node1), node2);
    t.checkExpect(this.reps2.get(n1), n3);
  }

// tests the algorithm method in the KruskalMaze class
  void testAlgo(Tester t) {
    this.initConditions();
    t.checkExpect(this.kruskal2.algorithm(), new ArrayList<Edge>(Arrays.asList(e1, e2)));
    t.checkExpect(this.kruskal1.algorithm(),
        new ArrayList<Edge>(Arrays.asList(edge1, edge2, edge3, edge4, edge6)));
  }

//tests the removeLeftOver method in the KruskalMaze class
  void testRemoveLeftOver(Tester t) {
    this.initConditions();
    t.checkExpect(this.node2.outedges.get(0), this.edge1);
    t.checkExpect(this.node2.outedges.get(1), this.edge2);
    t.checkExpect(this.node2.outedges.get(2), this.edge5);
    t.checkExpect(this.node2.outedges.get(3), this.edge7);
    t.checkExpect(this.node6.outedges.size(), 2);

    this.kruskal1.algorithm();
    this.kruskal1.removeAllLeftOver();

    t.checkExpect(this.node2.outedges.size(), 2);
    t.checkExpect(this.node2.outedges.get(0), this.edge1);
    t.checkExpect(this.node2.outedges.get(1), this.edge2);
    t.checkExpect(this.node6.outedges.size(), 1);
    t.checkExpect(this.node6.outedges.get(0), this.edge4);
  }

// tests the compare method in the CompareByWeight class
  void testCompare(Tester t) {
    this.initConditions();
    t.checkExpect(new CompareByWeight().compare(edge1, edge2), -10);
    t.checkExpect(new CompareByWeight().compare(edge4, edge6), -20);
    t.checkExpect(new CompareByWeight().compare(e1, e3), -20);
  }

// tests the sortEdges method in the CompareByWeight class
  void testSortEdges(Tester t) {
    this.initConditions();
    this.g1.sortEdges(edges1);
    t.checkExpect(this.edges1.get(0), this.edge1);
    t.checkExpect(this.edges1.get(1), this.edge2);
    t.checkExpect(this.edges1.get(2), this.edge3);
    this.g2.sortEdges(edges2);
    t.checkExpect(this.edges2.get(0), this.edge5);
    t.checkExpect(this.edges2.get(1), this.edge6);
    t.checkExpect(this.edges2.get(2), this.edge7);
    t.checkExpect(this.edges2.get(3), this.edge8);
  }

// tests the drawMaze method in the MazeGame class
  void testDrawMaze(Tester t) {
    this.initConditions();
    t.checkExpect(this.g1.mazeBoard.size(), 60);
    t.checkExpect(this.g1.mazeBoard.get(0).size(), 100);

    WorldImage row2Emp = new EmptyImage();
    WorldImage bkg = new EmptyImage();
    WorldImage cell1 = new RectangleImage(20, 20, OutlineMode.SOLID, Color.green);
    WorldImage cell2 = new RectangleImage(20, 20, OutlineMode.SOLID, Color.gray);
    WorldImage cell3 = new RectangleImage(20, 20, OutlineMode.SOLID, Color.gray);
    WorldImage cell4 = new RectangleImage(20, 20, OutlineMode.SOLID, Color.magenta);

    if (!this.g4.mazeBoard.get(0).get(1).isConnected(this.g4.mazeBoard.get(0).get(0))) {
      cell2 = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE,
          new RectangleImage(1, 20, OutlineMode.SOLID, Color.black), 0, 0, cell2);
    }
    WorldImage row1 = new EmptyImage();
    row1 = new BesideImage(new BesideImage(row1, cell1), cell2);

    if (!this.g4.mazeBoard.get(1).get(0).isConnected(this.g4.mazeBoard.get(0).get(0))) {
      cell3 = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP,
          new RectangleImage(20, 1, OutlineMode.SOLID, Color.black), 0, 0, cell3);
    }
    if (!this.g4.mazeBoard.get(1).get(1).isConnected(this.g4.mazeBoard.get(1).get(0))) {
      cell4 = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE,
          new RectangleImage(1, 20, OutlineMode.SOLID, Color.black), 0, 0, cell4);
    }
    if (!this.g4.mazeBoard.get(1).get(1).isConnected(this.g4.mazeBoard.get(0).get(1))) {
      cell4 = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP,
          new RectangleImage(20, 1, OutlineMode.SOLID, Color.black), 0, 0, cell4);
    }

    WorldImage row2 = new BesideImage(new BesideImage(row2Emp, cell3), cell4);
    WorldImage world = new AboveImage(new AboveImage(bkg, row1), row2);
    t.checkExpect(this.g4.drawMaze(), world);
  }

  // tests the MakeConnectedBoard
  void testMakeConnectedBoard(Tester t) {
    this.initConditions();
    // needs to test to make sure that each node connects to each other node
    // the use of g1 is unrelated to the connectedBoard method
    ArrayList<ArrayList<Node>> test1 = this.g1.makeConnectedBoard(4, 3, new Random(0));
    t.checkExpect(test1.size(), 3);
    t.checkExpect(test1.get(0).size(), 4);
    //testing that the total number of edges is equal to the number of nodes - 1
    int count1 = 0;
    for(ArrayList<Node> arrNodes : test1) {
      for(Node n : arrNodes) {
        count1 += n.outedges.size();
      }
    }
    //must divide count by two since each edge will be counted twice, once at every node it connects
    t.checkExpect(count1/2, 11);
    
    ArrayList<ArrayList<Node>> test2 = this.g1.makeConnectedBoard(100, 60, new Random(0));
    t.checkExpect(test2.size(), 60);
    t.checkExpect(test2.get(0).size(), 100);
    //testing that the total number of edges is equal to the number of nodes - 1
    int count2 = 0;
    for(ArrayList<Node> arrNodes : test2) {
      for(Node n : arrNodes) {
        count2 += n.outedges.size();
      }
    }
    //must divide count by two since each edge will be counted twice, once at every node it connects
    t.checkExpect(count2/2, 5999);
  }

  boolean testScene(Tester t) {
    this.initConditions();
    WorldCanvas c = new WorldCanvas(this.g2.getMazeWidth(), this.g2.getMazeHeight());
    return c.drawScene(this.g2.makeScene()) && c.show();
  }

}
