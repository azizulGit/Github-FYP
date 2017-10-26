package azizul.adruino;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import android.view.MenuItem;
import android.view.Menu;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.os.AsyncTask;
import com.temboo.Library.Google.Spreadsheets.RetrieveLastColumnValue;
import com.temboo.Library.Google.Spreadsheets.RetrieveLastColumnValue.RetrieveLastColumnValueInputSet;
import com.temboo.Library.Google.Spreadsheets.RetrieveLastColumnValue.RetrieveLastColumnValueResultSet;


import com.temboo.core.TembooSession;
import android.os.Handler;




public class MainActivity extends Activity implements OnItemClickListener {
    private Button startBtn;
    private TextView textView;
    private Handler mHandler;
    private WebView mWebView;


    private static MainActivity inst;
    ArrayList<String> smsMessagesList = new ArrayList<String>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tvTemp);
        mHandler = new Handler();
        mHandler.post(mUpdate);


        mWebView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mWebView.loadUrl("https://docs.google.com/spreadsheets/d/1ovHmYAsUU7lwCmB2vD30DkZIkiCMID4hwM8lFBePIiI/pubchart?oid=2046295042&format=interactive");


        smsListView = (ListView) findViewById(R.id.SMSList);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);

        refreshSmsInbox();




        startBtn = (Button) findViewById(R.id.makeCall);
        startBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                makeCall();
            }
        });


    }



    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        do {
            String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
                    "\n" + smsInboxCursor.getString(indexBody) + "\n";
            arrayAdapter.add(str);
        } while (smsInboxCursor.moveToNext());
    }

    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String[] smsMessages = smsMessagesList.get(pos).split("\n");
            String address = smsMessages[0];
            String smsMessage = "";
            for (int i = 1; i < smsMessages.length; ++i) {
                smsMessage += smsMessages[i];
            }

            String smsMessageStr = address + "\n";
            smsMessageStr += smsMessage;
            Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Runnable mUpdate = new Runnable() {
        public void run() {

            new SpreadsheetTask(textView).execute();
            mHandler.postDelayed(this, 1000);

        }
    };


    class SpreadsheetTask extends AsyncTask<Void, Void, String> {

        TextView textView;

        public SpreadsheetTask(TextView textView){
            this.textView = textView;
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {


                // Instantiate the Choreo, using a previously instantiated TembooSession object, eg:
                TembooSession session = new TembooSession("azizul", "tempArduino", "Hp3pNjNEM8gY6LmGLUt9k7yCknerk1RX");

                RetrieveLastColumnValue retrieveLastColumnValueChoreo = new RetrieveLastColumnValue(session);

                // Get an InputSet object for the choreo
                RetrieveLastColumnValueInputSet retrieveLastColumnValueInputs = retrieveLastColumnValueChoreo.newInputSet();

                // Set inputs
                retrieveLastColumnValueInputs.set_ClientSecret("cSs-ApLFoAFqjmXtSp22Szea");
                retrieveLastColumnValueInputs.set_WorksheetName("Sheet1");
                retrieveLastColumnValueInputs.set_RefreshToken("1/rT5ImPuqHdE2ckWQYSU9RwXsfl61Pw1ByaS27B-CniYMEudVrK5jSpoR30zcRFq6");
                retrieveLastColumnValueInputs.set_ColumnName("Temperature");
                retrieveLastColumnValueInputs.set_ClientID("665457325837-38tb22kd99l3l7nieorbkefrg4l7k7e0.apps.googleusercontent.com");
                retrieveLastColumnValueInputs.set_SpreadsheetName("Arduino");



                // Execute Choreo
                RetrieveLastColumnValueResultSet retrieveLastColumnValueResults = retrieveLastColumnValueChoreo.execute(retrieveLastColumnValueInputs);

                    return retrieveLastColumnValueResults.get_CellValue();



            } catch(Exception e) {
                // if an exception occurred, log it
                Log.e(this.getClass().toString(), e.getMessage());
            }
            return null;
        }

        protected void onPostExecute(String result) {
            try {
                // Will update UI here
                textView.setText(result);

            } catch(Exception e) {
                // if an exception occurred, show an error message
                Log.e(this.getClass().toString(), e.getMessage());
            }
        }
    }



    protected void makeCall() {
        Log.i("Make call", "");

        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse("tel:014-5719976"));

        try {
            startActivity(phoneIntent);
            finish();
            Log.i("Finished making a call.", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "Call failed, please try again later.", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
/*Handle action bar item clicks here.The action bar will
        automatically handle clicks on the Home/Up button, so long
        as you specify a parent activity in AndroidManifest.xml.*/

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
