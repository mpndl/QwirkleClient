package za.nmu.wrpv.qwirkle;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class XMLHandler {
    public static void loadFromXML(Run run, Activity context) {
        if (fileExists(context,"games.xml")) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(context.openFileInput("games.xml"));

                XPath xPath = XPathFactory.newInstance().newXPath();
                XPathExpression xPathExpression = xPath.compile("//game");
                NodeList nodeList = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);
                    int id = Integer.parseInt(element.getElementsByTagName("gameID").item(0).getTextContent());
                    Date date = new Date(Date.parse(element.getElementsByTagName("date").item(0).getTextContent()));
                    Player player = loadPlayer((Element) element.getElementsByTagName("player").item(0));
                    List<PlayerMessage> messages = loadMessages((Element) element.getElementsByTagName("messages").item(0));
                    List<Player> players = loadPlayers((Element) element.getElementsByTagName("players").item(0));
                    Game game = new Game();
                    game.gameID = id;
                    game.date = date;
                    game.messages = messages;
                    game.player = player;
                    game.players = players;

                    Map<String, Object> data = new HashMap<>();
                    data.put("game", game);
                    run.run(data);
                }
            } catch (XPathExpressionException | SAXException | ParserConfigurationException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<PlayerMessage> loadMessages(Element msgsElm) {
        List<PlayerMessage> messages = new ArrayList<>();

        NodeList nodeList = msgsElm.getElementsByTagName("message");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element curMsgElm = (Element) nodeList.item(i);

            String msg = curMsgElm.getElementsByTagName("imessage").item(0).getTextContent();
            Element playerElm = (Element) curMsgElm.getElementsByTagName("player").item(0);

            PlayerMessage message = new PlayerMessage();
            message.message = msg;
            message.player = loadPlayer(playerElm);
            message.time = curMsgElm.getElementsByTagName("time").item(0).getTextContent();
            messages.add(message);
        }

        Collections.reverse(messages);
        return messages;
    }

    private static List<Player> loadPlayers(Element playersElm) {
        List<Player> players = new ArrayList<>();
        NodeList nodeList = playersElm.getElementsByTagName("player");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element curPlayerElm = (Element) nodeList.item(i);
            Player player = loadPlayer(curPlayerElm);
            players.add(player);
        }
        return players;
    }

    private static Player loadPlayer(Element playerElm) {
        Player player = new Player();

        player.color = playerElm.getAttribute("color");
        player.name = Player.Name.valueOf(playerElm.getTextContent().strip());
        player.points = Integer.parseInt(playerElm.getAttribute("points"));

        return player;
    }

    public static void appendToXML(Game game, Activity context) throws TransformerException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

            if (fileExists(context, "games.xml")) {
                document = builder.parse(context.openFileInput("games.xml"));
            }
            else {
                document = builder.newDocument();
            }

            Element root = document.getDocumentElement();

            if (root == null) {
                root = document.createElement("games");
                document.appendChild(root);
            }

            Element element = createGameElement(document, game);
            root.appendChild(element);

            writeToXML(document, context.openFileOutput("games.xml", MODE_PRIVATE));
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    public static Element createTextElement(Document doc, String name, String text) {
        Text textNode = doc.createTextNode(text);
        Element element = doc.createElement(name);
        element.appendChild(textNode);
        return element;
    }

    public static Element createPlayerElement(Document doc, Player player) {
        Element nameText = createTextElement(doc, "name", player.name.toString());

        Element playerElm = doc.createElement("player");

        playerElm.setAttribute("color", player.color);
        playerElm.setAttribute("points", String.valueOf(player.points));

        playerElm.appendChild(nameText);

        return playerElm;
    }

    public static Element createMessageElement(Document doc, PlayerMessage message) {
        Element messageText = createTextElement(doc, "imessage", message.message);
        Element playerText = createPlayerElement(doc, message.player);
        Element timeText = createTextElement(doc, "time", message.time);

        Element messageElement = doc.createElement("message");

        messageElement.appendChild(messageText);
        messageElement.appendChild(playerText);
        messageElement.appendChild(timeText);
        return messageElement;
    }

    public static Element createGameElement(Document doc, Game game) {
        Element gameElement = doc.createElement("game");

        Element messagesElement = doc.createElement("messages");
        for (PlayerMessage message: game.messages) {
            Element messageElement = createMessageElement(doc, message);
            messagesElement.appendChild(messageElement);
        }

        Element playersElm = doc.createElement("players");
        for (Player player: game.players) {
            Element playerElm = createPlayerElement(doc, player);
            playersElm.appendChild(playerElm);
        }

        Element idText = createTextElement(doc, "gameID", String.valueOf(game.gameID));
        Element dateText = createTextElement(doc, "date", game.date.toString());
        Element playerText = createPlayerElement(doc, game.player);

        gameElement.appendChild(idText);
        gameElement.appendChild(dateText);
        gameElement.appendChild(playerText);
        gameElement.appendChild(messagesElement);
        gameElement.appendChild(playersElm);

        return gameElement;
    }

    public static void writeToXML(Document doc, FileOutputStream fos) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc), new StreamResult(fos));
    }



    public static boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        return file != null && file.exists();
    }

}
