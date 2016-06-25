package com.qljl.tmm_business;

import com.qljl.tmm_business.util.MyLock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
/**
 * 输入觅卷
 * @author lw
 *
 */
public class InputCodeActivity extends Activity implements OnClickListener{
	private RadioButton scanRB;
	private ImageView exitImg;
	private EditText input_code;
	private Button commitBtn;
	private MainActivity mainActivity = null;
	private Handler handler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_code);
		initView();
	}

	private void initView() {
		scanRB = (RadioButton) findViewById(R.id.scanRB);
		scanRB.setOnClickListener(this);
		exitImg = (ImageView) findViewById(R.id.exitImg);
		exitImg.setOnClickListener(this);
		input_code = (EditText) findViewById(R.id.input_code);
		commitBtn = (Button) findViewById(R.id.commitBtn);
		commitBtn.setOnClickListener(this);
		
		mainActivity = MainActivity.getMainActivity();
		handler = mainActivity.getHandler();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.scanRB:
			Intent intent = new Intent();
			intent.setClass(InputCodeActivity.this, CaptureActivity.class);
			startActivity(intent);
			finish();
			break;
		case R.id.exitImg:
			if (MainActivity.bool) {
				MyLock.getSignal();
			}
			finish();
			break;
		case R.id.commitBtn:
			String inputStr = input_code.getText()+"";
			if(inputStr != "" || inputStr != null){
//				 Message msg = new Message();
//				 msg.what = 1;
//				 msg.obj = inputStr;
//				 handler.sendMessage(msg);
				 CaptureActivity.scanResult = inputStr;
				 if (MainActivity.bool) {
						MyLock.getSignal();
					}
				 finish();
			}
			break;
		default:
			break;
		}
	}

}
