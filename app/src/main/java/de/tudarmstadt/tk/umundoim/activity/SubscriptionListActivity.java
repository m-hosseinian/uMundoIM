package de.tudarmstadt.tk.umundoim.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import java.util.ArrayList;

import de.tudarmstadt.tk.umundoim.R;
import de.tudarmstadt.tk.umundoim.constant.Constants;


public class SubscriptionListActivity extends ActionBarActivity {

    ArrayList<Switch> subscriptions = new ArrayList<Switch>();
    Button submitButton;
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

        subscriptions.add((Switch) findViewById(R.id.TK1));
        subscriptions.add((Switch) findViewById(R.id.TK3));
        subscriptions.add((Switch) findViewById(R.id.PILOTY));
        subscriptions.add((Switch) findViewById(R.id.CRYPTO));
        subscriptions.add((Switch) findViewById(R.id.MENSA));
        subscriptions.add((Switch) findViewById(R.id.SEDC));
        subscriptions.add((Switch) findViewById(R.id.KN2));
        setSubscriptions();
    }

    private void setSubscriptions() {

        for (Switch subSwitch :  subscriptions) {
            subSwitch.setChecked(Constants.trends.containsKey(subSwitch.getText().toString()));
            subSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton view,
                                             boolean isChecked) {
                    Switch temp = (Switch) view;
                    String trend = temp.getText().toString();
                    if (isChecked) {
                        Constants.trendsDropDownList.add(trend);
                    } else {
                        Constants.trendsDropDownList.remove(trend);
                    }
                }
            });
        }
    }
}
