package de.tudarmstadt.tk.umundoim.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import java.util.ArrayList;

import de.tudarmstadt.tk.umundoim.R;
import de.tudarmstadt.tk.umundoim.constant.Constants;


public class SubscriptionListActivity extends ActionBarActivity {

    /**
     * Done ->
     *  1) make everything here dynamic;
     *  2) when the user browses to this page we show all the synced subscriptionMap
     *  3) add the feature in which the user can "add a new trend"
     *  4) when ever a user adds a new Trend broadcast it to everyone
     *  4.5) Design a protocol for sending messages on a "Control" channel (new Trend, Trend List, etc
     *  5) When a new user joins ask everyone their current active subscriptionMap
     */
    ArrayList<Switch> subscriptions = new ArrayList<Switch>();
    Button submitButton, addButton;
    EditText newTrendText;
    LinearLayout subscriptionLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_list);
        submitButton = (Button) findViewById(R.id.submitSubscriptions);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /**
         * Get a list of all current subscriptionMap and create switches for them
         */
        subscriptionLinearLayout = (LinearLayout) findViewById(R.id.subscription_ll);

        newTrendText =  ( EditText) findViewById(R.id.new_trend_text);
        addButton = (Button) findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTrend(newTrendText.getText().toString());
            }
        });
        syncSubscription();
    }

    private void addNewTrend(String newText) {
        if (newText.isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Please type in a new Trend to be added to the domain", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            Constants.newTrends.add(newText);
            Constants.trendsDropDownList.add(newText);
            Toast.makeText(getApplicationContext(), "Trend successfully added to the domain", Toast.LENGTH_SHORT).show();
        }
    }

    private void syncSubscription() {

        for (String trend : Constants.subscriptionStatus.keySet()) {
            Switch subSwitch = new Switch(this);
            subSwitch.setText(trend);
            subSwitch.setChecked(Constants.subscriptionStatus.get(trend));
            subSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton view,
                                             boolean isChecked) {
                    Switch temp = (Switch) view;
                    String trend = temp.getText().toString();
                    if (isChecked) {
                        Constants.trendsDropDownList.add(trend);
                        Constants.subscriptionStatus.put(trend,true);
                    } else {
                        Constants.trendsDropDownList.remove(trend);
                        Constants.subscriptionStatus.put(trend,false);
                    }
                }
            });
            subscriptions.add(subSwitch);
            subscriptionLinearLayout.addView(subSwitch);
        }
    }
}
