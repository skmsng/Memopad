package sample.application.memopad;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.SimpleCursorAdapter;
import android.content.Intent;

public class MemoList extends ListActivity {
	public static final String[] cols = {"title", "memo", android.provider.BaseColumns._ID };	//SQLの列名
	public MemoDBHelper memos;
	
	/**
	 * メニューの「開く」を押したとき
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.memolist);	//memolist.xml
		
		
		this.showMemos(this.getMemos());	//select文でメモリストを取得し、表示メソッドに渡す
		
		ListView lv = (ListView)this.findViewById(android.R.id.list);	//今のView（memolist.xmlのlist）の取得
		this.registerForContextMenu(lv);								//ListViewオブジェクトにコンテキストメニューを登録？？？
	}


	/**
	 * select文
	 * 
	 * db.query("memoDB", MemoList.cols, null, null, null, null, null)・・・ 第1引数（テーブル名）、第2引数（列名）、第3以降（条件？）
	 * startManagingCursor()・・・ 検索結果(cursor)を自動的にリソース解放してくれるAndroidの便利なメソッド
	 */
	public Cursor getMemos() {
		this.memos = new MemoDBHelper(this);				//DBオブジェクトの生成準備
		SQLiteDatabase db = memos.getReadableDatabase();	//DBオブジェクトの生成
		Cursor cursor = db.query("memoDB", MemoList.cols, null, null, null, null, null);	//DB検索（select）
		this.startManagingCursor(cursor);					//とりあえず書いておく
		return cursor;
	}
	
	/**
	 * メモリストの表示
	 * 
	 * from・・・リストに表示するデータベースのフィールド名
	 * to・・・表示するビューのリソースID
	 * android.R.layout.simple_list_item_1・・・標準レイアウト
	 */
	public void showMemos(Cursor cursor) {
		if(cursor != null){							//検索結果がある場合
			String[] from = {"title"};				
			int[] to = {android.R.id.text1};
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(
					this, android.R.layout.simple_list_item_1,
					cursor, from, to);
			setListAdapter(adapter);
		}
		memos.close();								//select文のオブジェクトを閉じる
	}
	
	
	
	/**
	 * リスト(上記select文の結果)から１つのメモを選択したとき
	 * DBからメモ内容を取り出し、メインアクティビティ（MemopadActivity）にデータを渡して、このアクティビティ（MemoList）を終了する。
	 * 
	 * select文（id条件検索）
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);				//
		this.memos = new MemoDBHelper(this);					//DBオブジェクトの生成準備
		SQLiteDatabase db = this.memos.getWritableDatabase();	//DBオブジェクトの生成
		Cursor cursor = db.query("memoDB", MemoList.cols, "_ID="+String.valueOf(id), null, null, null, null);	//DB検索（select）
		this.startManagingCursor(cursor);						//とりあえず書いておく
		Integer idx = cursor.getColumnIndex("memo");
		cursor.moveToFirst();
		Intent i = new Intent();	//暗黙的インテント
		
		i.putExtra("text", cursor.getString(idx));
		this.setResult(RESULT_OK, i);
		memos.close();									//select文のオブジェクトを閉じる
		this.finish();	//現在のアクティビティ（MemoList）終了 --> MemopadActivity.onActivityResult()
	}



}
