package cc.skylock.skylock;

/**
 * Created by AlexVijayRaj on 6/17/2015.
 */
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;

/**
 * Created by AlexVijayRaj on 6/16/2015.
 */
public class SQLView extends Activity {
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sqlview);
        TextView tv = (TextView) findViewById(R.id.tvSQLinfo);
        DatabaseClass info = new DatabaseClass(this);
        try {
            info.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String data = info.getData();
        int temp = 0, temp1 = 0, temp2 = 0;
        try {
            temp = info.getCount();
            temp1 = info.getStartRowID();
            temp2 = info.getEndRowID();
        }catch(Exception e){}
        info.close();
        tv.setText(data);
        Toast.makeText(getApplicationContext(), "Rows = " + temp + " Start = " +temp1 + " End = " +temp2 ,
                Toast.LENGTH_LONG).show();
    }
}

