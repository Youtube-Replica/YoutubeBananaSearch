package Model;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.util.MapBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class Search {

    //Search for Videos by title and for Channel by name
    public static String getSearch(String s) {
        ArangoDB arangoDB = new ArangoDB.Builder().build();
        String dbName = "subscriptions";
        //First get by channel name
        String collectionName = "Channels";
        JSONObject searchObjectTotal = new JSONObject();

        System.out.println("String we got:" + s);
        try {
            String query = "FOR doc IN Channels\n" +
                   // "        FILTER doc.`Name` == @value\n" +
                    "        FILTER CONTAINS(doc.`Name`, @value)" +
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
                    searchObjectM.put("ID", id);
                    searchObjectM.put("Name", myDocument2.getAttribute("Name"));
                    searchObjectM.put("Category", myDocument2.getAttribute("Category"));
                    searchObjectM.put("Profile Picture", myDocument2.getAttribute("ProfilePicture"));
                    searchArray.add(searchObjectM);
                }
                searchObjectTotal.put("Channel "+id,searchArray);
            }
//            else{
                JSONArray searchArray = new JSONArray();
                query = "FOR doc IN Videos\n" +
                       // "        FILTER doc.`title` like @value\n" +
                        "        FILTER CONTAINS(doc.`title`, @value)" +
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
                        BaseDocument myDocument2 = arangoDB.db(dbName).collection("Videos").getDocument(cursor2.getKey(),
                                BaseDocument.class);
                        id= Integer.parseInt(cursor2.getKey());
                        searchObjectM.put("ID", id);
                        searchObjectM.put("ChannelID", Integer.parseInt(""+myDocument2.getAttribute("channel_id")));
                        String channel_id = ""+myDocument2.getAttribute("channel_id");
                        BaseDocument myDocument3 = arangoDB.db(dbName).collection(collectionName).getDocument(channel_id,
                                BaseDocument.class);
                        searchObjectM.put("Channel Name ", myDocument3.getAttribute(""));
                        searchObjectM.put("Likes", Integer.parseInt(""+myDocument2.getAttribute("likes")));
                        searchObjectM.put("Dislikes", Integer.parseInt(""+myDocument2.getAttribute("dislikes")));
                        searchObjectM.put("Views", Integer.parseInt(""+myDocument2.getAttribute("views")));
                        searchObjectM.put("Thumbnail", myDocument2.getAttribute("thumbnail"));
                        searchObjectM.put("Title", myDocument2.getAttribute("title"));
                        searchObjectM.put("Category", myDocument2.getAttribute("category"));
                        searchObjectM.put("Duration", Integer.parseInt(""+myDocument2.getAttribute("duration")));
                        searchObjectM.put("Date Created", myDocument2.getAttribute("date_created"));
                        searchArray.add(searchObjectM);
                    }
                    searchObjectTotal.put("Video "+id,searchArray);
                }
                else{
                    searchObjectTotal.put("Not found ",s);
                }
//            }
        } catch (ArangoDBException e) {
            System.err.println("Failed to execute query. " + e.getMessage());
        }
        System.out.println("Search Object" + searchObjectTotal.toString());
        return searchObjectTotal.toString();


    }

}
