package model;

import Client.Client;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.util.MapBuilder;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Map;

public class Search {

    static ArangoDB arangoDB;
    static Search instance = new Search();
    static String dbName = "subscriptions";

    private Search(){
        arangoDB = new ArangoDB.Builder().build();
    }

    public static Search getInstance(){
        return Search.instance;
    }

    public void setDB(int i){
        arangoDB = new ArangoDB.Builder().maxConnections(i).build();
    }

    //Search for Videos by title and for Channel by name
    public static String getSearch(String s) {
        //First get by channel name
        JSONObject searchObjectTotal = new JSONObject();

            String query = "FOR doc IN Channels\n" +
                   // "        FILTER doc.`Name` == @value\n" +
                    "        FILTER CONTAINS(doc.info.category, @value)" +
                    "        RETURN doc";
            Map<String, Object> bindVars = new MapBuilder().put("value", s).get();

            ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null,
                    BaseDocument.class);
            if(cursor.hasNext()) {
                BaseDocument cursor2 = null;
                JSONArray searchArray = new JSONArray();
                int id = 0;
                for (; cursor.hasNext(); ) {
                    JSONObject searchObjectM = new JSONObject();
                    cursor2 = cursor.next();
                    try {
                        BaseDocument myDocument2 = arangoDB.db(dbName).collection("Channels").getDocument(cursor2.getKey(),
                                BaseDocument.class);
                        id = Integer.parseInt(cursor2.getKey());
                        searchObjectM.put("channel_id", id);
                        searchObjectM.put("info", myDocument2.getAttribute("info"));
                        searchObjectM.put("subscriptions", myDocument2.getAttribute("subscriptions"));
                        searchObjectM.put("watched_videos", myDocument2.getAttribute("watched_videos"));
                        searchObjectM.put("blocked_channels", myDocument2.getAttribute("blocked_channels"));
                        searchObjectM.put("notifications", myDocument2.getAttribute("notifications"));
                        searchArray.add(searchObjectM);
                    } catch (ArangoDBException e) {
                        Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> Failed to get document: myKey; " + e.getMessage(), CharsetUtil.UTF_8));
                        System.err.println("Failed to get document: myKey; " + e.getMessage());
                    }
                }
                searchObjectTotal.put("channels", searchArray);
            }

                JSONArray searchArray = new JSONArray();
                query = "FOR doc IN Videos\n" +
                       // "        FILTER doc.`title` like @value\n" +
                        "        FILTER CONTAINS(doc.category, @value)" +
                        "        RETURN doc";
                bindVars = new MapBuilder().put("value", s).get();

                cursor = arangoDB.db(dbName).query(query, bindVars, null,
                        BaseDocument.class);

                if(cursor.hasNext()) {
                    BaseDocument cursor2=null;
                    int id=0;
                    for (; cursor.hasNext(); ) {
                        cursor2 = cursor.next();
                        System.out.println(cursor2.getKey());
                        JSONObject searchObjectM = new JSONObject();
                        try{
                        BaseDocument myDocument2 = arangoDB.db(dbName).collection("Videos").getDocument(cursor2.getKey(),
                                BaseDocument.class);
                        id= Integer.parseInt(cursor2.getKey());

                        searchObjectM.put("VideoID",id);
                        searchObjectM.put("ChannelID",myDocument2.getAttribute("channel_id"));
                        searchObjectM.put("Likes",myDocument2.getAttribute("likes"));
                        searchObjectM.put("Dislikes",myDocument2.getAttribute("dislikes"));
                        searchObjectM.put("Views",myDocument2.getAttribute("views"));
                        searchObjectM.put("Title",myDocument2.getAttribute("title"));
                        searchObjectM.put("Category",myDocument2.getAttribute("category"));
                        searchObjectM.put("Duration",myDocument2.getAttribute("duration"));
                        searchObjectM.put("Description",myDocument2.getAttribute("description"));
                        searchObjectM.put("Qualities",myDocument2.getAttribute("qualities"));
                        searchObjectM.put("Private",myDocument2.getAttribute("private"));
                        searchObjectM.put("url",myDocument2.getAttribute("url"));
                        searchObjectM.put("Date_Created",myDocument2.getAttribute("date_created"));
                        searchObjectM.put("Date_Modified",myDocument2.getAttribute("date_modified"));


                        searchArray.add(searchObjectM);
                    } catch (ArangoDBException e) {
                        Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> Failed to get document: myKey; " + e.getMessage(), CharsetUtil.UTF_8));
                        System.err.println("Failed to get document: myKey; " + e.getMessage());
                     }
                    }
                    searchObjectTotal.put("videos",searchArray);
                }
                else{
                    Client.serverChannel.writeAndFlush(Unpooled.copiedBuffer("Error> No entry by that name " + s, CharsetUtil.UTF_8));
                    searchObjectTotal.put("No Entry by the name ",s);
                }

        System.out.println("Search Object" + searchObjectTotal.toString());
        return searchObjectTotal.toString();
    }

}
