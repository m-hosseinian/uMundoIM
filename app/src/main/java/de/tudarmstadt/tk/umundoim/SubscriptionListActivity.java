package de.tudarmstadt.tk.umundoim;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class SubscriptionListActivity extends ActionBarActivity {

    public HashMap<String,Boolean> trends;
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
                Intent intent = new Intent();
                intent.putExtra("trends",trends);
                setResult(100,intent);
                finish();
            }
        });
        //Intent intent = getIntent();
        trends = (HashMap<String,Boolean>) getIntent().getSerializableExtra("trends");
        subscriptions.add((Switch) findViewById(R.id.TK1));
        subscriptions.add((Switch) findViewById(R.id.TK3));
        subscriptions.add((Switch) findViewById(R.id.PILOTY));
        subscriptions.add((Switch) findViewById(R.id.CRYPTO));
        subscriptions.add((Switch) findViewById(R.id.MENSA));
        subscriptions.add((Switch) findViewById(R.id.SEDC));
        subscriptions.add((Switch) findViewById(R.id.KN2));
        setSubscriptions();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_subscription_list, menu);
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
    private void setSubscriptions() {

        Iterator iter = subscriptions.iterator();
        while ( iter.hasNext()) {
            Switch subSwitch  = (Switch) iter.next();
            subSwitch.setChecked(trends.get( subSwitch.getText().toString()));
            subSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Switch temp = (Switch) v;
                    String text = temp.getText().toString();
                    trends.put(text, temp.isChecked());
                }
            });
        }
    }
}
