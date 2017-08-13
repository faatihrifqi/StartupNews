package faatih.com.startupnews.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import faatih.com.startupnews.News;
import faatih.com.startupnews.R;
import faatih.com.startupnews.adapter.MyAdapter;

public class Teknopedia extends Fragment implements MyAdapter.ListItemClickListener {

    private final static String BASE_URL = "http://www.teknopedia.asia/";
    private final static String MAIN_CLASS = "meta-image";

    private int myPages = 1;
    private ArrayList<News> list_news;

    private MyAdapter adapter;

    private SwipyRefreshLayout mSwipyRefreshLayout;

    public Teknopedia() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.recycler_list, container, false);

        list_news = new ArrayList<>();

        myPages = 1;

        mSwipyRefreshLayout = (SwipyRefreshLayout) view.findViewById(R.id.swipyrefreshlayout);

        mSwipyRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.orange, R.color.green, R.color.blue);

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
        rv.setLayoutManager(llm);

        adapter = new MyAdapter(list_news, this);
        rv.setAdapter(adapter);

        mSwipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                myPages+=1;
                new DownloadNewsTask().execute();
            }
        });

        //run first when onCreate called
        mSwipyRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                new DownloadNewsTask().execute();
            }
        });

        return view;
    }

    private Document openConnection(String newUrl){
        Connection.Response response;
        try {
            response = Jsoup.connect(newUrl).userAgent("Mozilla").timeout(5000).execute();
            if(response.statusCode() == 200)
                return response.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class DownloadNewsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

            mSwipyRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(Void... params) {

            Document new_doc = openConnection(BASE_URL + "page/" + myPages);
            if(new_doc != null){
                Elements links = new_doc.getElementsByClass(MAIN_CLASS);
                for (Element link : links) {
                    Elements myLink = link.getElementsByTag("a");
                    String title = myLink.attr("title");
                    String url = myLink.attr("href");

                    list_news.add(new News(title, url));
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mSwipyRefreshLayout.setRefreshing(false);
            mSwipyRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTTOM);

            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(News news) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(news.getUrl())));
    }
}
