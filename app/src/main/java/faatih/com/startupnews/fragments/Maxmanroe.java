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

public class Maxmanroe extends Fragment implements MyAdapter.ListItemClickListener {

    private final static String BASE_URL = "https://www.maxmanroe.com/category/startup/";
    private final static String MAIN_CLASS = "clear";

    private int myPages = 1;
    private ArrayList<News> list_news;

    private MyAdapter adapter;

    private SwipyRefreshLayout mSwipyRefreshLayout;

    public Maxmanroe() {
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

                ArrayList<String> data_judul = new ArrayList<>();
                ArrayList<String> data_url = new ArrayList<>();

                Elements links = new_doc.getElementsByClass(MAIN_CLASS);

                //new
                for (Element link : links) {
                    Elements myLink = link.select("h2 > a");
                    String judul = myLink.text();
                    if(!(judul.equals(""))){
                        if(data_judul.size() > 0){
                            if(!(judul.substring(0, 20).equals((data_judul.get(data_judul.size()-1)).substring(0, 20))))
                                data_judul.add(judul);
                            else
                                data_judul.set(data_judul.size()-1, judul);
                        }else{
                            data_judul.add(judul);
                        }
                    }
                    String url = myLink.attr("href");
                    if(!(url.equals(""))){
                        if(data_url.size() > 0){
                            if(!(url.equals(data_url.get(data_url.size()-1))))
                                data_url.add(url);
                        }else{
                            data_url.add(url);
                        }
                    }
                }

                for(int i = 0; i < data_judul.size(); i++){
                    list_news.add(new News(data_judul.get(i), data_url.get(i)));
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
