import java.util.HashMap;


//RoutingInfoClass is used to store information about neighbouring nodes

public class RouterInfoClass {
    public String routerName;
    public HashMap<String, Double> nextNode;
    public HashMap<String, DefaultClass> routingInfo;
    public Boolean updateInfo;

  //Constructor to initialize RouterInfoClass
    public RouterInfoClass(String routerName) {
        this.routerName = routerName;
        this.updateInfo = true;
        this.nextNode = new HashMap<>();
        this.routingInfo = new HashMap<>();
    }

    public RouterInfoClass(byte[] receiveData){

        String updateInfo = new String(receiveData);

        String[] data = updateInfo.split(",");

        this.updateInfo = true;

        this.routerName = data[0].trim();

        this.routingInfo = new HashMap<>();

        DefaultClass newTable = new DefaultClass();
        newTable.count = 0.0;
        this.routingInfo.put(this.routerName, newTable);

        for(int i = 1; i < data.length -1; i++){
            String[] newEntry = data[i].split(":");
            Double distance = Double.parseDouble(newEntry[1].trim());
            DefaultClass defaultClass = new DefaultClass();
            defaultClass.count = distance;

            this.routingInfo.put(newEntry[0].trim(), defaultClass);
        }
    }
    public void update(){
        this.updateInfo = false;
    }
    //method to add a new node to the routing table
    public void newNode(String next, double count, String newCount, Boolean present){
        this.updateInfo = true;
        double oldCount = Double.POSITIVE_INFINITY;

        if(this.nextNode.get(next) != null){
            oldCount = this.nextNode.get(next);
        }
        this.nextNode.put(next, count);

        if(present || oldCount != count){
            this.addNewEntry(next, count, newCount);
        }
    }
    //add new entry if there was change in previous values
    public void addNewEntry(String node, double count, String newCount){
        this.updateInfo = true;

        DefaultClass defaultClass = this.routingInfo.get(node);
        if(defaultClass != null){
            defaultClass.count = count;
            defaultClass.nextHop = newCount;
        } else{
            this.routingInfo.put(node, new DefaultClass(count, newCount));
        }

    }
    //convertTableData is used convert data for broadcasting to the neighbours
    public String convertTableData(String avoid){

        String data = "";

        for(String node : this.routingInfo.keySet()){

            if(node.equals(this.routerName)){
                continue;
            }

            DefaultClass defaultClass = this.routingInfo.get(node);
            if(!defaultClass.nextHop.equals(avoid)){
                data += node + ": " + defaultClass.count + ", ";
            }
        }

        if(data.equals("")){
            return this.routerName;
        }

        return this.routerName + "," + data.substring(0, data.length() - 1);
    }

    //toString() displays the routing information of the nodes
    @Override
    public String toString() {

        String output = "";
        for(String node : routingInfo.keySet()){
            if(node.equals(this.routerName)){
                continue;
            }

            DefaultClass defaultClass = this.routingInfo.get(node);
            output += "shortest path " + this.routerName + "-" + node + ": the nextHop hop is " + defaultClass.nextHop + " and the cost is " + defaultClass.count + "\n";
            // output += this.routerName + "-" + node + ": " + defaultClass.nextHop + " and " + defaultClass.count + "\n";

        }
        return output;
    }


}