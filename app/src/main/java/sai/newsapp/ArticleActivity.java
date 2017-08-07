package sai.newsapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ArticleActivity extends AppCompatActivity {

    String url;
    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        WebView webView = (WebView) findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        Intent i = getIntent();

        url = i.getStringExtra("articleURL");
        title = i.getStringExtra("title");

        //String content=i.getStringExtra("content");

        webView.loadUrl(url);

        //webView.loadData(content,"text/html","UTF-8");

    }

    public void shareIt(View view){
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT,"Share Link");
        share.putExtra(Intent.EXTRA_TEXT,title + ":" + url);
        startActivity(Intent.createChooser(share,"Share Article using"));
    }

}
