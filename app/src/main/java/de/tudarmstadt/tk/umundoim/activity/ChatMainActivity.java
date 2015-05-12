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

    private ScrollView chatScrollView;
    private String trend;
    private Discovery disc;
    private Node chatNode;
    private Subscriber chatSub;
    private Publisher chatPub;
    private IMSubscription subscription;

    private HashMap<String, String> participants = new HashMap<>();


    private ArrayAdapter<String> trendsDropDownAdapter;
    private TextView chatTextView;
    private TextView messageEditText;
    private Button sendButton;
    private Spinner subSelection;

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
                            chatPub.send(msg);
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
            greeting.putMeta("subscriber", chatSub.getUUID());
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
        chatNode.removePublisher(chatPub);
        chatNode.removeSubscriber(chatSub);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        ArrayList<String> tempSet = new ArrayList<>(Constants.trendsDropDownList); // to avoid java.util.ConcurrentModificationException
        for(String newTrend : tempSet) {
            if (!Constants.trends.keySet().contains(newTrend)) {
                subscribeToTrend(newTrend);
            }
        }
        ArrayList<String> tempList = new ArrayList<>(Constants.trends.keySet());
        for (String trend : tempList) {
            if (!Constants.trendsDropDownList.contains(trend)) {
                unsubscribeFromTrend(trend);
            }
        }
        for (String s : Constants.trendsDropDownList) {
            Log.i(TAG, "drop down list item: " + s);
        }
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
        chatSub = new Subscriber(newTrend);
        chatSub.setReceiver(new ChatReceiver());
        chatPub = new Publisher(newTrend);
        chatPub.setGreeter(new ChatGreeter(Constants.userName, newTrend));
        chatNode.addPublisher(chatPub);
        chatNode.addSubscriber(chatSub);
        subscription = new IMSubscription(chatPub, chatSub);
        Constants.trends.put(newTrend, subscription);
        Log.i("Subscriber", "Successfully subscribed to \"" + newTrend + "\"");

    }

    public void unsubscribeFromTrend (String trend) {
        subscription = Constants.trends.get(trend);
        if (subscription == null) {
            Log.w(TAG, "unsubscribing from not subsctribed trend:" + trend );
        } else {
            chatNode.removePublisher(subscription.getPublisher());
            chatNode.removeSubscriber(subscription.getSubscriber());
            Constants.trends.remove(trend);
            Log.i("Publisher", "Successfully unsubscribed from \"" + trend + "\"");
        }
    }
}
