package sample.application.memopad;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.text.Selection;
import android.widget.EditText;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MemopadActivity extends Activity {
    /** Called when the activity is first created. */
	
    @Override	//アプリを起動したとき
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);	//main.xmlの指示書
        
        //前回の画面を残す
        EditText et = (EditText) this.findViewById(R.id.editText1);	//今の画面（インスタンス）の取得
        SharedPreferences pref = this.getSharedPreferences("MemoPrefs", MODE_PRIVATE);	//前回のを取得？
        et.setText(pref.getString("memo", ""));	//前回のをセット
        et.setSelection(pref.getInt("cursor", 0));
    }
    
    @Override	//ホームボタン、違うアプリを起動したとき
    public void onStop(){
    	super.onStop();
    	EditText et = (EditText) this.findViewById(R.id.editText1);	//今の画面（インスタンス）の取得
    	SharedPreferences pref = this.getSharedPreferences("MemoPrefs", MODE_PRIVATE);
    	SharedPreferences.Editor editor = pref.edit();
    	editor.putString("memo", et.getText().toString());	//memoという名前で保存
    	editor.putInt("cursor", Selection.getSelectionStart(et.getText()));
    	editor.commit();
    }
    
	public void saveMemo(){
		EditText et = (EditText)this.findViewById(R.id.editText1);
		String title;
		String memo = et.getText().toString();
		
		if(memo.trim().length()>0){
			if(memo.indexOf("\n") == -1){
				title = memo.substring(0, Math.min(memo.length(), 20));
			}else{
				title = memo.substring(0, Math.min(memo.indexOf("\n"),20));
			}
			String ts = DateFormat.getDateTimeInstance().format(new Date());
			MemoDBHelper memos = new MemoDBHelper(this);
			SQLiteDatabase db = memos.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("title", title + "\n" + ts);
			values.put("memo", memo);
			db.insertOrThrow("memoDB", null, values);
			memos.close();
		}
	}

	@Override	//
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			EditText et = (EditText)findViewById(R.id.editText1);
			
			switch(requestCode){
			case 0:
				et.setText(data.getStringExtra("text"));
				break;
			}
		}
	}

	@Override	//メニューボタンを押したとき
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = this.getMenuInflater();
		mi.inflate(R.menu.menu, menu);	//menu.xml
		return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override	//イベントハンドラ（メニューから選択されたとき）
	public boolean onOptionsItemSelected(MenuItem item) {		//引数でメニュー番号を受け取る
		EditText et = (EditText)findViewById(R.id.editText1);	//今の画面（インスタンスの取得）の取得
		switch(item.getItemId()){								//
		case R.id.menu_save:
			saveMemo();
			break;
		case R.id.menu_open:
			Intent i = new Intent(this, MemoList.class);		//MemoListクラスの画面にインテントを渡す　明示的インテント（自分のアプリ内のアクティビティを起動）
			startActivityForResult(i,0);
			break;
		case R.id.menu_new:
			et.setText("");										//今の画面（インスタンス）に""をセット-->バグ？
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}