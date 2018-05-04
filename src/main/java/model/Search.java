package model;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.util.MapBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Map;

public class Search {

    //Search for Videos by title and for Channel by name
    public static String getSearch(String s) {
        String host = System.getenv("ARANGO_DB_SERVICE_HOST");
        ArangoDB arangoDB = new ArangoDB.Builder().host(host, 8529).build();
        String dbName = "scalable";
        //First get by channel name
        String collectionName = "channel";
        JSONObject searchObjectTotal = new JSONObject();

        try {
            String query = "FOR doc IN channel\n" +
                   // "        FILTER doc.`Name` == @value\n" +
                    "        FILTER CONTAINS(doc.info.name, @value)" +
                    "        RETURN doc";
            Map<String, Object> bindVars = new MapBuilder().put("value", s).get();

            ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null,
                    BaseDocument.class);
            if(cursor.hasNext()) {
                BaseDocument cursor2 = null;
                JSONArray searchArray = new JSONArray();
                int id=0;
                for (; cursor.hasNext(); ) {
                    JSONObject searchObjectM = new JSONObject();
                    cursor2 = cursor.next();
                    BaseDocument myDocument2 = arangoDB.db(dbName).collection(collectionName).getDocument(cursor2.getKey(),
                            BaseDocument.class);
                    id= Integer.parseInt(cursor2.getKey());
                    searchObjectM.put("channel_id",id);
                    searchObjectM.put("info",myDocument2.getAttribute("info"));
                    searchObjectM.put("subscriptions",myDocument2.getAttribute("subscriptions"));
                    searchObjectM.put("watched_videos",myDocument2.getAttribute("watched_videos"));
                    searchObjectM.put("blocked_channels",myDocument2.getAttribute("blocked_channels"));
                    searchObjectM.put("notifications",myDocument2.getAttribute("notifications"));
                    searchArray.add(searchObjectM);
                }
                searchObjectTotal.put("channels",searchArray);
            }
//            else{
                JSONArray searchArray = new JSONArray();
                query = "FOR doc IN video\n" +
                       // "        FILTER doc.`title` like @value\n" +
                        "        FILTER CONTAINS(doc.title, @value)" +
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
                        BaseDocument myDocument2 = arangoDB.db(dbName).collection("video").getDocument(cursor2.getKey(),
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
                    }
                    searchObjectTotal.put("videos",searchArray);
                }
                else{
//                    searchObjectTotal.put("No Videos",s);
                }
//            }
        } catch (ArangoDBException e) {
            System.err.println("Failed to execute query. " + e.getMessage());
        }
        System.out.println("Search Object" + searchObjectTotal.toString());
        return searchObjectTotal.toString();

    }

}
