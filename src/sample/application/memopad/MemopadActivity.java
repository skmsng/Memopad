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
        this.setContentView(R.layout.main);	//main.xml
        
        //前回の画面(onStopで保存した画面)を表示
        EditText et = (EditText) this.findViewById(R.id.editText1);						//今のView（main.xmlのeditText1）の取得
        SharedPreferences pref = this.getSharedPreferences("MemoPrefs", MODE_PRIVATE);	//SharedPreferencesオブジェクトを生成
        et.setText(pref.getString("memo", ""));											//"memo"で保存したString型の値を取得して、EditTextにセット
        et.setSelection(pref.getInt("cursor", 0));										//"cursor"で保存したint型の値を取得して、カーソルの位置を移動
    }
    
    /**
     * SharedPreferences・・・getメソッドでデータを取得する
     * pref.getString('key','keyに対する値がない場合の値')
     * 
     * SharedPreferences.Editor・・・putメソッドでデータを保存する
     * editor.putString('key','value')・・・valueに格納する値
     * editor.commit()・・・格納したデータの保存
     * 
     * getSharedPreferences("任意の名前", モード)
     */
    @Override	//ホームボタン、違うアプリを起動したとき
    public void onStop(){
    	super.onStop();
    	EditText et = (EditText) this.findViewById(R.id.editText1);						//今のView（editText1）の取得
    	SharedPreferences pref = this.getSharedPreferences("MemoPrefs", MODE_PRIVATE);	//SharedPreferencesオブジェクトを生成
    	SharedPreferences.Editor editor = pref.edit();									//Editorオブジェクトを生成
    	editor.putString("memo", et.getText().toString());								//テキストボックスの文字列を取得し、"memo"という名前で格納
    	editor.putInt("cursor", Selection.getSelectionStart(et.getText()));				//テキストボックスのカーソル位置を取得し、"cursor"という名前で格納
    	editor.commit();																//editorに格納されたデータを保存
    }
    
    
    /**
	 * メニューボタンを押したとき
	 * MenuInflater・・・XMLからメニューを作る
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = this.getMenuInflater();		//オブジェクトの生成
		mi.inflate(R.menu.menu, menu);					//menu.xmlの内容でメニュー項目を作る
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * メニューから選択されたとき(イベントハンドラ)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		//引数で押されたメニュー項目番号を受け取る
		EditText et = (EditText)findViewById(R.id.editText1);	//今のView（editText1）の取得
		switch(item.getItemId()){								
		case R.id.menu_save:									//保存
			saveMemo();
			break;
		case R.id.menu_open:									//開く
			Intent i = new Intent(this, MemoList.class);		//MemoListクラスの画面にインテントを渡す　明示的インテント（自分のアプリ内のアクティビティを起動）
			startActivityForResult(i,0);						//MemoListの起動、第2引数がonActivityResult()の第1引数へ渡される
			break;
		case R.id.menu_new:										//新規作成
			et.setText("");										//今の画面（editText1）に""をセット（開く）
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
    
    /**
     * メニューから「保存」を押したとき
     * 
     * trim()・・・空白を取り除いた文字列
     * indexOf(検索文字)・・・検索文字の位置（検索文字が無いときは-1）
     * substring(num1,num2)・・・num1番目〜num2番目までの文字列
     * Math.min(num1,num2)・・・2つの値のうち小さい方の値
     * 
     * getDateTimeInstance()・・・デフォルトの日付/時刻フォーマット
     * format()・・・Date型を日付/時刻文字列に変換
     * 
     * getWritableDatabase()・・・DBオブジェクトを生成（SQLiteOpenHelperのメソッド）
     * ContentValues・・・列名と値を格納する（テーブルの中身）
     * db.insertOrThrow("memoDB", null, values)・・・第1引数（テーブル名）と第3引数（データ）を渡してinsert
     */
	public void saveMemo(){
		EditText et = (EditText)this.findViewById(R.id.editText1);				//今のView（editText1）の取得
		String title;
		String memo = et.getText().toString();									//テキストボックスの文字列を取得
		
		if(memo.trim().length()>0){												//空白以外の文字がある場合
			if(memo.indexOf("\n") == -1){										//改行がない場合
				title = memo.substring(0, Math.min(memo.length(), 20));			//最初の20文字をタイトル
			}else{																//改行がある場合
				title = memo.substring(0, Math.min(memo.indexOf("\n"),20));		//最初の改行までをタイトル
			}
			String ts = DateFormat.getDateTimeInstance().format(new Date());	//保存日時を文字列で格納
			
			MemoDBHelper memos = new MemoDBHelper(this);		//DBオブジェクトの生成準備
			SQLiteDatabase db = memos.getWritableDatabase();	//DBオブジェクトの生成
			ContentValues values = new ContentValues();			//テーブルのオブジェクトの生成
			values.put("title", title + "\n" + ts);				//列名=title　データ=タイトル+保存日時
			values.put("memo", memo);							//列名=memo データ=メモ内容
			db.insertOrThrow("memoDB", null, values);			//DBに登録（insert）
			memos.close();										//オブジェクトを閉じる
		}
	}

	
	
	
	/**
	 * メニューから「開く」を押し、MemoListアクティビティの処理が終わった後
	 * 
	 * 選択されたメモを開く
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			EditText et = (EditText)findViewById(R.id.editText1);
			
			switch(requestCode){							//拡張機能
			case 0:
				et.setText(data.getStringExtra("text"));
				break;
			}
		}
	}

	
	
	
}