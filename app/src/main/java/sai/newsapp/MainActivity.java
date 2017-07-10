package sai.newsapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar toolbar;

    Map<Integer, String> articleUrls = new HashMap<Integer, String>();
    Map<Integer , String> articleTitles = new HashMap<Integer, String>();
    ArrayList<Integer> articleIds = new ArrayList<Integer>();

    SQLiteDatabase sqLiteDatabase;

    ArrayList<String> titles = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    ArrayList<String> urls = new ArrayList<String>();
    ArrayList<String> content = new ArrayList<String>();

    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        mToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);

        drawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_bro);
        navigationView.setNavigationItemSelectedListener(this);

        ListView listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,titles);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),ArticleActivity.class);
                intent.putExtra("articleURL",urls.get(i));
                intent.putExtra("content",content.get(i));
                startActivity(intent);
            }
        });

        sqLiteDatabase = this.openOrCreateDatabase("Articles",MODE_PRIVATE,null);

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY,articleId INTEGER,url VARCHAR,title VARCHAR,content VARCHAR)");

        //updatelistview();

        spinner = (ProgressBar)findViewById(R.id.progressBar);
        spinner.getIndeterminateDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY); //Need to update color value with primary accent
        spinner.setVisibility(View.VISIBLE);

        DownloadTask downloadTask = new DownloadTask();
        try {
            downloadTask.execute(" https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatelistview(){

        Toast.makeText(getApplicationContext(),"New Content Downloaded",Toast.LENGTH_SHORT).show();

        try {

            Cursor c = sqLiteDatabase.rawQuery("SELECT * FROM articles", null);

            int contentIndex = c.getColumnIndex("content");
            int URLIndex = c.getColumnIndex("url");
            int titleIndex = c.getColumnIndex("title");

            titles.clear();
            urls.clear();

            c.moveToFirst();

            do{
                titles.add(c.getString(titleIndex));
                urls.add(c.getString(URLIndex));
                content.add(c.getString(contentIndex));
            }while (c.moveToNext());

            c.close();

            arrayAdapter.notifyDataSetChanged();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public class DownloadTask extends AsyncTask< String , Void , String >{

        @Override
        protected String doInBackground(String... urls) {

            String result ="";
            URL url;
            HttpURLConnection httpURLConnection=null;

            try{

                url=new URL(urls[0]);

                httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream in = httpURLConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while(data!=-1){
                    char current = (char)data;

                    result+=current;

                    data=reader.read();

                }

                JSONArray jsonArray = new JSONArray(result);

                sqLiteDatabase.execSQL("DELETE FROM articles");

                for(int i=0;i<20;i++){

                    String articleId = jsonArray.getString(i);

                    url =new URL("https://hacker-news.firebaseio.com/v0/item/" +jsonArray.getString(i)+".json?print=pretty");

                    httpURLConnection = (HttpURLConnection) url.openConnection();

                    in=httpURLConnection.getInputStream();

                    reader = new InputStreamReader(in);

                    data = reader.read();

                    String articleInfo=" ";

                    while(data!=-1){
                        char cur = (char)data;
                        articleInfo+=cur;
                        data=reader.read();
                    }

                    JSONObject jsonObject = new JSONObject(articleInfo);

                    String articleTitle = jsonObject.getString("title");

                    String articleUrl = jsonObject.optString("url");

                    String articleContent = "";

                    articleIds.add(Integer.valueOf(articleId));
                    articleTitles.put(Integer.valueOf(articleId),articleTitle);
                    articleUrls.put(Integer.valueOf(articleId),articleUrl);

                    String sql ="INSERT INTO articles (articleId,url,title,content) VALUES (?,?,?,?)";

                    SQLiteStatement statement = sqLiteDatabase.compileStatement(sql);

                    statement.bindString(1,articleId);

                    statement.bindString(2,articleUrl);

                    statement.bindString(3,articleTitle);

                    statement.bindString(4,articleContent);

                    statement.execute();

                }

            }catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            spinner.setVisibility(View.GONE);
            updatelistview();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.nav_settings: {
                //Calling the default settings activity,changes need to be made
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                break;
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}


