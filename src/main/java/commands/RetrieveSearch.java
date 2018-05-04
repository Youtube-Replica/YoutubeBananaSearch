package commands;


import commands.Command;
import model.Search;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;

public class RetrieveSearch extends Command {
   public static String search_string = "";
   public void execute() {
       HashMap<String, Object> props = parameters;

       Channel channel = (Channel) props.get("channel");
       JSONParser parser = new JSONParser();
       search_string = "";
       try {
           System.out.println(props);
           JSONObject body = (JSONObject) parser.parse((String) props.get("body"));
           System.out.println(body.toString());
           JSONObject params = (JSONObject) parser.parse(body.get("parameters").toString());
           search_string = params.get("search_string").toString();
       } catch (ParseException e) {
           e.printStackTrace();
       }
       AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
       AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
       Envelope envelope = (Envelope) props.get("envelope");
       String response = Search.getSearch(search_string); //Gets channels searched for
       try {
           channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

}
