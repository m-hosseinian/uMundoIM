package de.tudarmstadt.tk.umundoim.activity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.util.Log;

import de.tudarmstadt.tk.umundoim.R;
import de.tudarmstadt.tk.umundoim.constant.Constants;
import de.tudarmstadt.tk.umundoim.datasrtucture.IMSubscription;

import org.umundo.core.Discovery;
import org.umundo.core.Discovery.DiscoveryType;
import org.umundo.core.Greeter;
import org.umundo.core.Node;
import org.umundo.core.Publisher;
import org.umundo.core.Receiver;
import org.umundo.core.Subscriber;
import org.umundo.core.Message;
import org.umundo.core.SubscriberStub;


public class ChatMainActivity extends ActionBarActivity {

    private static String TAG = "ChatMainActivity";
    private static String controlChannel = "CONTROL";
    private static String CONTROLTAG = "ControlActivity";

    private ScrollView chatScrollView;
    private String trend;
    private Discovery disc;
    private Node chatNode;
    private Subscriber controlSub;
    private Publisher controlPub;
    private IMSubscription subscription;

    private HashMap<String, String> participants = new HashMap<>();


    private ArrayAdapter<String> trendsDropDownAdapter;
    private TextView chatTextView;
    private TextView messageEditText;
    private Button sendButton;
    private Spinner subSelection;

    //TODO bug1: the creator of the new trend can not unsubscribe
    // TODO bug2: trends do not sync with the new user who joins
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            MulticastLock mcLock = wifi.createMulticastLock("mylock");
            mcLock.acquire();
        } else {
            Log.v("android-umundo", "Cannot get WifiManager");
        }

        System.loadLibrary("umundoNativeJava_d");

        disc = new Discovery(DiscoveryType.MDNS);
        chatNode = new Node();
        disc.add(chatNode);

        Constants.trendsDropDownList.add("coreChat");

        setContentView(R.layout.activity_chat_main);
        chatTextView = (TextView) findViewById(R.id.textViewChat);
        messageEditText = (EditText) findViewById(R.id.textViewMessage);
        sendButton = (Button) findViewById(R.id.buttonSend);
        chatScrollView = (ScrollView) findViewById(R.id.scrollViewChat);

        subSelection = (Spinner) findViewById(R.id.subscriptions_adapter);
        trendsDropDownAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Constants.trendsDropDownList);


        chatTextView.setText(chatTextView.getText().toString() +
                Constants.userName + "!" +
                "\n");

        subSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                trend = subSelection.getSelectedItem().toString();
                Log.i(TAG, "current trend: " + trend);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /**
         * Create and manage control channel for commands
         */
        controlPub = new Publisher(controlChannel);
        controlSub = new Subscriber(controlChannel);
        controlSub.setReceiver(new ControlReceiver());
        chatNode.addPublisher(controlPub);
        chatNode.addSubscriber(controlSub);

        /**
         * Broadcast new user present in order to receive updated trend lists
         */
        Message controlMsg = new Message();
        controlMsg.putMeta("newUser",Constants.userName);
        controlPub.waitForSubscribers(1);
        controlPub.send(controlMsg);

        initButtons();
    }

    void initButtons() {

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (messageEditText.getText() != null && messageEditText.getText().length() != 0) {
                            chatTextView.setText(chatTextView.getText().toString() +
                                    Constants.userName + "@" + trend + ": " + messageEditText.getText() +
                                    "\n");
                            Message msg = new Message();
                            msg.putMeta("userName", Constants.userName);
                            msg.putMeta("trend", trend);
                            msg.putMeta("chatMsg", messageEditText.getText().toString());

                            Constants.subscriptionMap.get(trend).getPublisher().send(msg);
                            //chatPub.send(msg);
                            messageEditText.setText("");
                        } else {
                            Log.w(TAG, "empty message.");
                        }
                        chatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    public class ChatReceiver extends Receiver {

        private Message lmsg;
        @Override
        public void receive(Message msg) {
            lmsg = msg;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (lmsg.getMeta().containsKey("participant")) {
                        participants.put(lmsg.getMeta("subscriber"), lmsg.getMeta("participant"));
                        Log.i(TAG, lmsg.getMeta("participant") + "@" + lmsg.getMeta("trend") + " joined the chat");
                        chatTextView.setText(chatTextView.getText().toString() +
                                lmsg.getMeta("participant") + "@" + lmsg.getMeta("trend") + " joined the chat" +
                                "\n");
                    } else {
                        Log.i(TAG, lmsg.getMeta("userName") + "@" + lmsg.getMeta("trend") + ": "
                                + lmsg.getMeta("chatMsg"));
                        chatTextView.setText(chatTextView.getText().toString() +
                                lmsg.getMeta("userName") + "@" + lmsg.getMeta("trend") + ": "
                                + lmsg.getMeta("chatMsg") +
                                "\n");
                    }
                    chatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });

        }
    }

    public class ControlReceiver extends  Receiver {

        private Message controlMessage;

        @Override
        public void  receive(Message msg) {
            controlMessage = msg;
            HashMap<String,String> metas = msg.getMeta();
            Log.i(CONTROLTAG,"We got a new control message;will decide based on its metas");
            if ( metas.containsKey("newTrend")){
                Constants.subscriptionStatus.put( controlMessage.getMeta("newTrend") , false );
                Log.i(CONTROLTAG, "new Trend was added by other users as: "+ controlMessage.getMeta("newTrend"));

            }
            /**
             * We respond to the new user announcing herself into the network by giving her all our subscribed channels
             */
            else if (metas.containsKey("newUser")) {
                Log.i(CONTROLTAG, "New User detected ... sending her all the new trends");
                Iterator<Map.Entry<String,Boolean>> iterator = Constants.subscriptionStatus.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String,Boolean> pair = iterator.next();
                    if ( pair.getValue()) {
                        Message message = new Message();
                        message.putMeta("updatedTrends",msg.getMeta("newUser"));
                        message.putMeta("channel",pair.getKey() );
                        controlPub.send(message);
                        Log.i(CONTROLTAG, "New trend:  "+ pair.getValue() + " was sent to the new user");
                    }
                }
            }
            /**
             * We have received the updated trends from other users;if it is signed for us then we use it
             */
            else if (metas.containsKey("updatedTrends")) {
                Log.i(CONTROLTAG, "We received updated trends, lets check to see if they are ours to store?");
                if (Constants.userName.equals(controlMessage.getMeta("updatedTrends"))) {
                    if (!Constants.subscriptionStatus.containsKey(controlMessage.getMeta("channel"))) {
                        Log.i(CONTROLTAG,"Putting new trends in the database "+ controlMessage.getMeta("channel"));
                        Constants.subscriptionStatus.put(controlMessage.getMeta("channel"), false);
                    }
                }
            }
        }
    }
    class ChatGreeter extends Greeter {
        public String userName;
        public String trend;
        SubscriberStub lsubStub;

        public ChatGreeter(String userName, String trend) {
            this.userName = userName;
            this.trend = trend;
        }

        @Override
        public void welcome(Publisher pub, SubscriberStub subStub) {
            Message greeting = Message.toSubscriber(subStub.getUUID());
            greeting.putMeta("participant", userName);
            greeting.putMeta("trend", trend);
            greeting.putMeta("subscriber", subStub.getUUID());
            pub.send(greeting);
        }

        @Override
        public void farewell(Publisher pub, SubscriberStub subStub) {

            lsubStub = subStub;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (participants.containsKey(lsubStub.getUUID())) {
                        Log.i(TAG, participants.get(lsubStub.getUUID()) + " left the chat");
                        chatTextView.setText(chatTextView.getText().toString() +
                                participants.get(lsubStub.getUUID()) + " left the chat" +
                                "\n");
                    } else {
                        Log.w(TAG, "An unknown user left the chat: " + lsubStub.getUUID());
                        chatTextView.setText(chatTextView.getText().toString() +
                                "An unknown user left the chat: " + lsubStub.getUUID() +
                                "\n");
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        //TODO: do a loop over subscriptions and remove all publishers
        //chatNode.removePublisher(chatPub);
        //chatNode.removeSubscriber(chatSub);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        ArrayList<String> tempSet = new ArrayList<>(Constants.trendsDropDownList); // to avoid java.util.ConcurrentModificationException
        for(String newTrend : tempSet) {
            if (!Constants.subscriptionMap.keySet().contains(newTrend)) {
                subscribeToTrend(newTrend);
            }
        }
        ArrayList<String> tempList = new ArrayList<>(Constants.subscriptionMap.keySet());
        for (String trend : tempList) {
            if (!Constants.trendsDropDownList.contains(trend)) {
                unsubscribeFromTrend(trend);
            }
        }
        for (String s : Constants.trendsDropDownList) {
            Log.i(TAG, "drop down list item: " + s);
        }
        /**
         * In case the user added a new Trend; we let everyone know of it
         */
        for (String newTrend: Constants.newTrends) {
            subscribeToTrend(newTrend);
            broadcastTrend(newTrend);
        }
        Constants.newTrends = new ArrayList<>(); //Empty the list

        subSelection.setAdapter(trendsDropDownAdapter);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.subscription_lists) {
            startActivity(new Intent(this, SubscriptionListActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public void subscribeToTrend(String newTrend) {
        Subscriber chatSub = new Subscriber(newTrend);
        chatSub.setReceiver(new ChatReceiver());
        Publisher chatPub = new Publisher(newTrend);
        chatPub.setGreeter(new ChatGreeter(Constants.userName, newTrend));
        chatNode.addPublisher(chatPub);
        chatNode.addSubscriber(chatSub);
        subscription = new IMSubscription(chatPub, chatSub);
        Constants.subscriptionStatus.put(newTrend,true);
        Constants.subscriptionMap.put(newTrend, subscription);
        Log.i("Subscriber", "Successfully subscribed to \"" + newTrend + "\"");

    }

    public void unsubscribeFromTrend (String trend) {
        subscription = Constants.subscriptionMap.get(trend);
        if (subscription == null) {
            Log.w(TAG, "unsubscribing from not subsctribed trend:" + trend );
        } else {
            chatNode.removePublisher(subscription.getPublisher());
            chatNode.removeSubscriber(subscription.getSubscriber());
            Constants.subscriptionMap.remove(trend);
            Constants.subscriptionStatus.put(trend,false);
            Log.i("Publisher", "Successfully unsubscribed from \"" + trend + "\"");
        }
    }

    public void broadcastTrend ( String trend) {
        Message msg = new Message();
        msg.putMeta("newTrend",trend);
        controlPub.send(msg);
        Log.i(TAG,"The new Trend: "+ trend + " was sent to everyone on the control channel");
    }
}
