package de.tudarmstadt.tk.umundoim.activity;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;
import java.util.HashMap;
import android.util.Log;

import de.tudarmstadt.tk.umundoim.R;
import de.tudarmstadt.tk.umundoim.constant.Constants;

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

    public static String TAG = "ChatMainActivity";

    public ScrollView chatScrollView;
    public String trend = "coreChat";
    public Discovery disc;
    public Node chatNode;
    public Subscriber chatSub;
    public Publisher chatPub;

    public String userName;
    public HashMap<String, String> participants = new HashMap<>();

    TextView chatTextView;
    TextView messageEditText;
    Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            MulticastLock mcLock = wifi.createMulticastLock("mylock");
            mcLock.acquire();
            // mcLock.release();
        } else {
            Log.v("android-umundo", "Cannot get WifiManager");
        }

        userName = Constants.USER_NAME;
//		System.loadLibrary("umundoNativeJava");
        System.loadLibrary("umundoNativeJava_d");
        disc = new Discovery(DiscoveryType.MDNS);
        chatNode = new Node();
        chatSub = new Subscriber(trend);
        chatSub.setReceiver(new ChatReceiver());
        chatPub = new Publisher(trend);
        disc.add(chatNode);

        chatPub.setGreeter(new ChatGreeter(userName, trend));
        chatNode.addPublisher(chatPub);
        chatNode.addSubscriber(chatSub);

        setContentView(R.layout.activity_chat_main);
        chatTextView = (TextView) findViewById(R.id.textViewChat);
        messageEditText = (EditText) findViewById(R.id.textViewMessage);
        sendButton = (Button) findViewById(R.id.buttonSend);
        chatScrollView = (ScrollView) findViewById(R.id.scrollViewChat);

        chatTextView.setText(chatTextView.getText().toString() +
                Constants.USER_NAME + "!" +
                "\n");

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
                                    Constants.USER_NAME + ": " + messageEditText.getText() +
                                    "\n");
                            Message msg = new Message();
                            msg.putMeta("userName", userName);
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

    class ChatReceiver extends Receiver {

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
    protected void onStop() {
        chatNode.removePublisher(chatPub);
        chatNode.removeSubscriber(chatSub);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}