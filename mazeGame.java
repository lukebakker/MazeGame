
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.*;

//Assignment 10
//Luke Bakker
//lbakker1858
//Sophie Wigmore
//wigmores

//represents a Node
class Node {
    public static int NODE_SIZE = 50;
    int x;
    int y;
    ArrayList<Edge> connections;

    Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.connections = new ArrayList<Edge>();
    }

    // draws the node
    WorldScene drawNode(WorldScene scene) {
        Color c = Color.lightGray;
        if (this.x == MazeWorld.WORLD_X - 1
                && this.y == MazeWorld.WORLD_Y - 1) {
            c = Color.PINK;
        }
        else if (this.x == 0 && this.y == 0) {
            c = Color.green;
        }
        WorldImage node = new RectangleImage(NODE_SIZE, NODE_SIZE,
                OutlineMode.SOLID, c);
        scene.placeImageXY(node, (this.x * NODE_SIZE) + (NODE_SIZE / 2),
                this.y * NODE_SIZE + (NODE_SIZE / 2));

        // draws every edge in this node
        for (Edge e : connections) {
            e.drawEdge(scene);
        }

        return scene;

    }

    // determines if this node is the same as that node
    public boolean sameNode(Node that) {
        return this.x == that.x && this.y == that.y;

    }
}

// represents an Edge
class Edge {
    public static int EDGE_SIZE = Node.NODE_SIZE;
    int weight;
    Node start;
    Node end;
    boolean toBeRemoved;

    Edge(Node start, Node end) {
        this.start = start;
        this.end = end;
        Random rand = new Random();
        this.weight = rand.nextInt(100);
        toBeRemoved = false;
    }

    Edge(int weight, Node start, Node end) {
        this.weight = weight;
        this.start = start;
        this.end = end;
    }

    // draws the edges
    WorldScene drawEdge(WorldScene scene) {
        if (!this.toBeRemoved) {
            // draws right edge
            if (this.start.x < this.end.x) {
                WorldImage edge = new RectangleImage(1, EDGE_SIZE,
                        OutlineMode.SOLID, Color.BLACK);
                scene.placeImageXY(edge, (this.start.x * EDGE_SIZE + EDGE_SIZE),
                        this.start.y * EDGE_SIZE + (EDGE_SIZE / 2));
            }
            // draws the bottom edge
            if (this.start.y < this.end.y) {
                WorldImage edge = new RectangleImage(EDGE_SIZE, 1,
                        OutlineMode.SOLID, Color.BLACK);
                scene.placeImageXY(edge,
                        this.start.x * EDGE_SIZE + (EDGE_SIZE / 2),
                        this.start.y * EDGE_SIZE + EDGE_SIZE);
            }
        }
        return scene;
    }

}

// represents the game world
class MazeWorld extends World {
    public static int WORLD_X = 10;
    public static int WORLD_Y = 10;
    ArrayList<ArrayList<Node>> allNodes;
    ArrayList<Edge> edges;

    // constructor for MazeWorld:
    MazeWorld() {
        // builds nodes
        allNodes = new ArrayList<ArrayList<Node>>();
        buildNodeList();
        // builds connections between the nodes
        buildConnections();
        // builds edges w/ randomly weighted edges
        this.edges = new ArrayList<Edge>();
        buildEdgeList();
        // sorts the edges by weight
        sortEdges();
        // creates the spanning tree for the maze
        kruskalAlgorithm();
        // EFFECT: removes the spanning edges from maze
        edgeRemove();
    }

    // draws the world
    public WorldScene makeScene() {
        WorldScene w = new WorldScene(WORLD_X * Node.NODE_SIZE,
                WORLD_Y * Node.NODE_SIZE);
        for (int i = 0; i < WORLD_X; i += 1) {
            for (int j = 0; j < WORLD_Y; j += 1) {
                allNodes.get(i).get(j).drawNode(w);
            }
        }
        return w;
    }

    // EFFECT: builds list of nodes nodes
    void buildNodeList() {
        for (int i = 0; i < WORLD_X; i += 1) {
            ArrayList<Node> temp = new ArrayList<Node>();
            for (int j = 0; j < WORLD_Y; j += 1) {
                temp.add(new Node(i, j));
            }
            this.allNodes.add(temp);
        }
    }

    // EFFECT: updates the connections between nodes
    void buildConnections() {
        for (int i = 0; i < WORLD_X; i += 1) {
            for (int j = 0; j < WORLD_Y; j += 1) {
                // adds connection on right to list of connections
                if (i < WORLD_X - 1) {
                    allNodes.get(i).get(j).connections
                            .add(new Edge(allNodes.get(i).get(j),
                                    allNodes.get(i + 1).get(j)));
                }
                // adds connection on bottom to list of connections
                if (j < WORLD_Y - 1) {
                    allNodes.get(i).get(j).connections
                            .add(new Edge(allNodes.get(i).get(j),
                                    allNodes.get(i).get(j + 1)));
                }
            }
        }
    }

    // returns the spanning tree for the edges of the maze
    ArrayList<Edge> kruskalAlgorithm() {
        HashMap<Node, Node> representatives = new HashMap<Node, Node>();
        ArrayList<Edge> worklist = this.edges;
        ArrayList<Edge> edgesInTree = new ArrayList<Edge>();

        // initializes every node in worklist to be its own representative
        for (ArrayList<Node> nodeList : this.allNodes) {
            for (Node n : nodeList) {
                representatives.put(n, n);
            }
        }
        int numConnections = 0;
        int worklistIdx = 0;
        while (numConnections < WORLD_X * WORLD_Y - 1) {
            Edge currentCheap = worklist.get(worklistIdx);
            if (this.find(representatives, currentCheap.start)
                    .sameNode(this.find(representatives, currentCheap.end))) {
                worklistIdx = worklistIdx + 1;
            }
            else {
                edgesInTree.add(worklist.get(worklistIdx));
                union(representatives,
                        this.find(representatives, currentCheap.start),
                        find(representatives, currentCheap.end));
                worklistIdx = worklistIdx + 1;
                numConnections = numConnections + 1;
            }
        }
        return edgesInTree;
    }

    // finds the representative of the current node
    Node find(HashMap<Node, Node> representatives, Node node) {
        // the node is its own representative
        if (node.sameNode(representatives.get(node))) {
            return node;
        }
        // the node maps to another representative
        else {
            return find(representatives, representatives.get(node));
        }
    }

    // EFFECT: "unions" or joins two disjoint groups together using their
    // representatives
    void union(HashMap<Node, Node> representatives, Node n1, Node n2) {
        representatives.put(n1, n2);
    }

    // EFFECT: builds the list of edges between nodes
    void buildEdgeList() {
        for (ArrayList<Node> nodeList : allNodes) {
            for (Node node : nodeList) {
                for (Edge edge : node.connections) {
                    this.edges.add(edge);
                }
            }

        }
    }

    // EFFECT: sorts the edges by weight
    void sortEdges() {
        sortEdgeHelp(0, this.edges.size());
    }

    // EFFECT: sorts the edges in the range of indices according to weight
    void sortEdgeHelp(int low, int high) {
        if (low >= high) {
            return;
        }
        Edge pivot = this.edges.get(low);
        int pivotIdx = partition(low, high, pivot);
        sortEdgeHelp(low, pivotIdx);
        sortEdgeHelp(pivotIdx + 1, high);
    }

    // returns the index where the pivot ends up in the sorted source
    // EFFECT: modifies the main edge list and the temp in the given range
    // so all values to left of pivot are less than or equal to the pivot
    // and all values to the right of the pivot are greater than it
    int partition(int low, int high, Edge pivot) {
        int curLo = low;
        int curHi = high - 1;
        while (curLo < curHi) {
            while (curLo < high
                    && this.edges.get(curLo).weight <= pivot.weight) {
                curLo = curLo + 1;
            }

            while (curHi > low && this.edges.get(curHi).weight > pivot.weight) {
                curHi = curHi - 1;
            }
            if (curLo < curHi) {
                Edge tempLo = this.edges.get(curLo);
                this.edges.set(curLo, this.edges.get(curHi));
                this.edges.set(curHi, tempLo);
            }
        }
        Edge tempHi = this.edges.get(curHi);
        this.edges.set(curHi, pivot);
        this.edges.set(low, tempHi);
        return curHi;
    }

    // EFFECT: updates each edge in spanning tree to be removed from maze
    void edgeRemove() {
        for (Edge e : this.kruskalAlgorithm()) {
            e.toBeRemoved = true;
        }
    }
}

class ExamplesMaze {
    MazeWorld m;

    // initializes data
    void initData() {
        this.m = new MazeWorld();
    }

    void testRun(Tester t) {
        initData();
        m.bigBang((m.WORLD_X) * Node.NODE_SIZE, (m.WORLD_Y) * Node.NODE_SIZE);
    }

    // tests buildConnections method
    void testBuildConnections(Tester t) {
        initData();
        t.checkExpect(m.allNodes.get(0).get(0).connections.size() == 2, true);
        t.checkExpect(m.allNodes.get(MazeWorld.WORLD_X - 1).get(0).connections
                .size() == 1, true);
        t.checkExpect(
                m.allNodes.get(MazeWorld.WORLD_X - 1)
                        .get(MazeWorld.WORLD_Y - 1).connections.size() == 0,
                true);
        t.checkExpect(m.allNodes.get(3).get(3).connections.size() == 2, true);
        t.checkExpect(m.allNodes.get(0).get(MazeWorld.WORLD_Y - 1).connections
                .size() == 1, true);
    }

    // tests buildEdgeList
    void testBuildEdgeList(Tester t) {
        initData();
        t.checkExpect(m.edges.size() > 0, true);
        t.checkExpect(m.edges.size(),
                (MazeWorld.WORLD_X * (MazeWorld.WORLD_Y - 1))
                        + (MazeWorld.WORLD_Y * (MazeWorld.WORLD_X - 1)));

    }

    // tests sortEdges
    void testSortEdges(Tester t) {
        initData();
        for (int i = 0; i < 179; i += 1) {
            t.checkExpect(m.edges.get(i).weight <= m.edges.get(i + 1).weight,
                    true);
        }
    }

    // tests kruskal
    void testKruskal(Tester t) {
        initData();
        t.checkExpect(m.kruskalAlgorithm().size(), 99);

    }

    // tests removal of edges
    void testEdgeRemoval(Tester t) {
        initData();
        for (int i = 0; i < 99; i += 1) {
            t.checkExpect(m.kruskalAlgorithm().get(i).toBeRemoved, true);
        }
    }

}
