import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class RouterClass {

    //node: present node of the router
    //Timer: After 15secs router rebroadcasts the routing table

    public RouterInfoClass node;
    public int count;
    public static long TIMER = 15000;


    public RouterClass(RouterInfoClass node) {
        this.node = node;
        this.count = 1;
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("Invalid Format!");
            System.out.println("Please Enter in the following format: java Sender <filename or filepath>");
            return;
        }

        String filename = args[0];
        //args[0] contains the file name which is extracted here
        String presentNode = filename.split(".txt")[0];
        RouterInfoClass currentNode = new RouterInfoClass(presentNode);
        //Router Initialization
        RouterClass routerClass = new RouterClass(currentNode);
        //Timer Starts
        long startTime = System.currentTimeMillis();

        try {
            int currentPort = 8000 + (int) presentNode.charAt(0);

            DatagramSocket datagramSocket = new DatagramSocket(currentPort);

            InetAddress inetAddress = InetAddress.getByName("localhost");

            //Update table if values changed in the routing table
            while(true){
                if(routerClass.node.updateInfo){

                    try{
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));

                        String readLine = bufferedReader.readLine();

                        //To store neighbouring nodeds routing costs
                        HashMap<String, Double> neighbors = new HashMap<String, Double>();

                        while((readLine = bufferedReader.readLine()) != null) {
                            StringTokenizer stringTokenizer = new StringTokenizer(readLine);
                            if(stringTokenizer.countTokens() != 2){
                                System.err.println("Invalid data!");
                                bufferedReader.close();
                                return;
                            }
                            String nextNode = stringTokenizer.nextToken().trim();
                            Double cost = Double.parseDouble(stringTokenizer.nextToken().trim());

                            neighbors.put(nextNode, cost);
                        }
                        bufferedReader.close();

                        Set<String> newNode = new HashSet<String>();
                        newNode.addAll(neighbors.keySet());
                        newNode.addAll(routerClass.node.nextNode.keySet());
                        //If a new neighbouring node is found, update in the routing table
                        for(String s : newNode){
                            Double weight = neighbors.get(s);
                            if(weight == null){
                                weight = Double.POSITIVE_INFINITY;
                            }
                            routerClass.node.newNode(s, weight, s, routerClass.count == 1);
                        }

                    } catch(FileNotFoundException e){
                        System.err.println("File Not Found!");
                        return;
                    } catch(NumberFormatException e){
                        System.err.println("Invalid Data!");
                        return;
                    } catch(IOException e){
                        System.err.println("Invalid Data!");
                        return;
                    }
                    System.out.println("Transmitting data to neighbours, Count: " + routerClass.count++);
                    System.out.println(routerClass.node);
                    System.out.println();

                    for(String neighbour : routerClass.node.nextNode.keySet()){
                        if(routerClass.node.nextNode.get(neighbour) == Double.POSITIVE_INFINITY){
                            continue;
                        }
                        String tableString = currentNode.convertTableData(neighbour);

                        byte[] data = tableString.getBytes();
                        int port = 8000 + (int) neighbour.charAt(0);

                        DatagramPacket dataGramPacket = new DatagramPacket(data, data.length, inetAddress, port);
                        datagramSocket.send(dataGramPacket);
                    }
                    routerClass.node.update();
                }
                //Timer for rebroadcasting the table
                try{
                    long TIMER = RouterClass.TIMER - (System.currentTimeMillis() - startTime);
                    if (TIMER < 0) {
                        throw new SocketTimeoutException();
                    }
                    byte[] receivedPacket = new byte[1024];
                    DatagramPacket receiveDatagramPacket = new DatagramPacket(receivedPacket, receivedPacket.length);

                    datagramSocket.setSoTimeout((int) TIMER);
                    datagramSocket.receive(receiveDatagramPacket);
                    byte[] receiveData = receiveDatagramPacket.getData();
                    RouterInfoClass receivedNode = new RouterInfoClass(receiveData);

                    for(String node : receivedNode.routingInfo.keySet()){
                        DefaultClass defaultClass = routerClass.node.routingInfo.get(node);
                        Double distance = Double.POSITIVE_INFINITY;
                        String nextHop = null;
                        if(defaultClass != null){
                            distance = defaultClass.count;
                            nextHop = defaultClass.nextHop;
                        }
                        //calculate shortest path using Bellman-Ford Algorithm
                        Double newDistance = receivedNode.routingInfo.get(node).count + routerClass.node.nextNode.get(receivedNode.routerName);
                        if(distance > newDistance){
                            routerClass.node.addNewEntry(node, newDistance, receivedNode.routerName);
                        } else if(nextHop != null && nextHop.equals(receivedNode.routerName) && !distance.equals(newDistance)){
                            routerClass.node.addNewEntry(node, newDistance, receivedNode.routerName);
                        }
                    }
                } catch(SocketTimeoutException e){
                    routerClass.node.updateInfo = true;
                    //if timeout occurs restart timer and update data in the routing table
                    startTime = System.currentTimeMillis();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }  catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }
}