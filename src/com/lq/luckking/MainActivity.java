package com.lq.luckking;  
  
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lq.luckking.utils.OnOffHelper;
  
public class MainActivity extends Activity {
	/**
	 * 是否开启抢红包的按钮
	 */
    private Button assistBtn;
    
    /**
     * 抢完红包是否自动返回的开关按钮
     */
    private Button autoBackBtn;
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);  
  
        assistBtn = (Button) findViewById(R.id.btn_assist);
        autoBackBtn = (Button) findViewById(R.id.btn_auto_back);
        
        assistBtn.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                try {  
                    //打开系统设置中辅助功能  
                    Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);  
                    startActivity(intent);  
                    Toast.makeText(MainActivity.this, "开启服务即可抢红包", Toast.LENGTH_LONG).show();
                    
                    String title = assistBtn.getText().toString();
                    if (getResources().getString(R.string.btn_assist_start).equalsIgnoreCase(title)) {
                    	assistBtn.setText(R.string.btn_assist_close);
                    	autoBackBtn.setEnabled(true);
                    }
                    else {
                    	assistBtn.setText(R.string.btn_assist_start);
                    	autoBackBtn.setEnabled(false);
                    }
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            }  
        });  
        
        autoBackBtn.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
	            String title = autoBackBtn.getText().toString();
	            if (getResources().getString(R.string.btn_auto_back_start).equalsIgnoreCase(title)) {
	            	OnOffHelper.autoBackFlag = true;
	            	autoBackBtn.setText(R.string.btn_auto_back_close);
	            }
	            else {
	            	OnOffHelper.autoBackFlag = false;
	            	autoBackBtn.setText(R.string.btn_auto_back_start);
	            }
            }  
        });
    }  
  
    @Override  
    public boolean onCreateOptionsMenu(Menu menu) {  
        // Inflate the menu; this adds items to the action bar if it is present.  
        return true;  
    }  
  
    @Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        // Handle action bar item clicks here. The action bar will  
        // automatically handle clicks on the Home/Up button, so long  
        // as you specify a parent activity in AndroidManifest.xml.  
        int id = item.getItemId();  
  
        if (id == R.id.action_settings) {  
            return true;  
        }  
  
        return super.onOptionsItemSelected(item);  
    }  
}