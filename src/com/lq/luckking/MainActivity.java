package com.lq.luckking;  
  
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.lq.luckking.utils.OnOffHelper;
  
public class MainActivity extends Activity implements OnClickListener{
	/**
	 * 是否开启抢QQ红包的按钮
	 */
    private Button qqAssistBtn;
    
    /**
	 * 是否开启抢微信红包的按钮
	 */
    private Button wechatAssistBtn;

    /**
	 * 是否开启咻红包的按钮
	 */
    private Button xiuAssistBtn;
    
    /**
     * 抢完红包是否自动返回的开关按钮
     */
    private Button autoBackBtn;
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);  
  
        qqAssistBtn = null == qqAssistBtn ? (Button) findViewById(R.id.btn_qq_assist) : qqAssistBtn;
        wechatAssistBtn = null == wechatAssistBtn ? (Button) findViewById(R.id.btn_wechat_assist) : wechatAssistBtn;
        xiuAssistBtn = null == xiuAssistBtn ? (Button) findViewById(R.id.btn_xiu_assist) : xiuAssistBtn; 
        autoBackBtn = null == autoBackBtn ? (Button) findViewById(R.id.btn_auto_back) : autoBackBtn;
        
        qqAssistBtn.setOnClickListener(this);
        wechatAssistBtn.setOnClickListener(this);
        xiuAssistBtn.setOnClickListener(this);
        autoBackBtn.setOnClickListener(this);
    }

    /**
     * 初始化各个按钮的状态
     * Description: <br> 
     *  
     * @author lei.qiang<br>
     * @taskId <br> <br>
     */
	private void initButtonState() {
		if (OnOffHelper.qqAsHasOpened) {
        	qqAssistBtn.setText(R.string.btn_qq_assist_close);
        }
        else {
        	qqAssistBtn.setText(R.string.btn_qq_assist_start);
        }
        
        if (OnOffHelper.wechatAsHasOpened) {
        	wechatAssistBtn.setText(R.string.btn_wechat_assist_close);
        }
        else {
        	wechatAssistBtn.setText(R.string.btn_wechat_assist_start);
        }
        
        if (OnOffHelper.xiuAsHasOpened) {
        	xiuAssistBtn.setText(R.string.btn_alipay_xiu_assist_close);
        }
        else {
        	xiuAssistBtn.setText(R.string.btn_alipay_xiu_assist_start);
        }
        
        if (OnOffHelper.autoBackFlag) {
        	autoBackBtn.setText(R.string.btn_auto_back_close);
        }
        else {
        	autoBackBtn.setText(R.string.btn_auto_back_start);
        }
	}  
  
    
    @Override
    protected void onResume() {
    	super.onResume();
    	initButtonState();
    }
    
    @Override  
    public boolean onCreateOptionsMenu(Menu menu) {  
        // Inflate the menu; this adds items to the action bar if it is present.  
        return true;  
    }  
  
    @Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        int id = item.getItemId();  
  
        if (id == R.id.action_settings) {  
            return true;  
        }  
  
        return super.onOptionsItemSelected(item);  
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_auto_back:
				String title = autoBackBtn.getText().toString();
	            if (getResources().getString(R.string.btn_auto_back_start).equalsIgnoreCase(title)) {
	            	OnOffHelper.autoBackFlag = true;
	            	autoBackBtn.setText(R.string.btn_auto_back_close);
	            }
	            else {
	            	OnOffHelper.autoBackFlag = false;
	            	autoBackBtn.setText(R.string.btn_auto_back_start);
	            }
				break;
			case R.id.btn_qq_assist:
			case R.id.btn_wechat_assist:
			case R.id.btn_xiu_assist:
				//打开系统设置中辅助功能  
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);  
                startActivity(intent);  
                Toast.makeText(MainActivity.this, "开启服务即可抢红包", Toast.LENGTH_LONG).show();
				break;
			default:
				break;
		}

	}  
}