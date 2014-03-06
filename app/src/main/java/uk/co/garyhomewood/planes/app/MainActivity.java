package uk.co.garyhomewood.planes.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.kevinsawicki.http.HttpRequest;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import uk.co.garyhomewood.planes.app.model.Plane;

public class MainActivity extends ActionBarActivity {

    String apiKey;
    PlanesPagerAdapter planesPagerAdapter;
    ViewPager viewPager;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("RELOAD")) {
                planesPagerAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set the actionbar bg to get rid of the standard blue divider
        ActionBar ab = getSupportActionBar();
        ab.setIcon(R.drawable.ic_planes);
        ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3b000000")));

        try {
            AssetManager assetManager = this.getAssets();
            InputStream inputStream = assetManager.open("config.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            apiKey = properties.getProperty("apikey");
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewPager = (ViewPager) findViewById(R.id.pager);
        new GetPlanes().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(receiver, new IntentFilter("RELOAD"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(receiver);
    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        unregisterReceiver(receiver);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    private class GetPlanes extends AsyncTask<Void, Void, List<Plane>> {

        private final String URL = "http://www.kimonolabs.com/api/4nnih7lm?apikey=" + apiKey;

        @Override
        protected List<Plane> doInBackground(Void... voids) {
            List<Plane> planes = new ArrayList<Plane>();
            String response = HttpRequest.get(URL).body();

            try {
                JSONObject json = new JSONObject(response);
                JSONArray planesJson = json.getJSONObject("results").getJSONArray("planes");

                for (int i = 0; i < planesJson.length(); i++) {
                    JSONObject planeJson = planesJson.getJSONObject(i);

                    String imageSrc = planeJson.getJSONObject("image").get("src").toString();
                    String manufacturer = planeJson.getJSONObject("manufacturer").get("text").toString();
                    String model = planeJson.getJSONObject("model").get("text").toString();
                    String airline = planeJson.getJSONObject("airline").get("text").toString();
                    String registration = planeJson.getJSONObject("registration").get("text").toString();

                    planes.add(new Plane(manufacturer, model, airline, registration, imageSrc));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return planes;
        }

        @Override
        protected void onPostExecute(List<Plane> planes) {
            planesPagerAdapter = new PlanesPagerAdapter(getSupportFragmentManager(), planes);
            viewPager.setAdapter(planesPagerAdapter);
            planesPagerAdapter.notifyDataSetChanged();
        }
    }

    public class PlanesPagerAdapter extends FragmentStatePagerAdapter {

        private List<Plane> planes;

        public PlanesPagerAdapter(FragmentManager fm, List<Plane> planes) {
            super(fm);
            this.planes = planes;
        }

        @Override
        public Fragment getItem(int position) {
            Plane plane = planes.get(position);
            return PlaneFragment.newInstance(plane);
        }

        @Override
        public int getCount() {
            return planes.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    public static class PlaneFragment extends Fragment {
        private Plane plane;
        private ActionBar ab;
        private RelativeLayout detailsPanel = null;

        public static PlaneFragment newInstance(Plane plane) {
            PlaneFragment fragment = new PlaneFragment();
            Bundle args = new Bundle();
            args.putSerializable("plane", plane);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaneFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            plane = (Plane) getArguments().getSerializable("plane");

            ab = ((ActionBarActivity) getActivity()).getSupportActionBar();
            detailsPanel = (RelativeLayout) rootView.findViewById(R.id.details);
            final ProgressBar loader = (ProgressBar) rootView.findViewById(R.id.loader);

            TextView manufacturer = (TextView) rootView.findViewById(R.id.manufacturer);
            TextView model = (TextView) rootView.findViewById(R.id.model);
            TextView airline = (TextView) rootView.findViewById(R.id.airline);
            TextView registration = (TextView) rootView.findViewById(R.id.registration);

            manufacturer.setText(plane.getManufacturer());
            model.setText(plane.getModel());
            airline.setText(plane.getAirline());
            registration.setText(plane.getRegistration());

            ImageView image = (ImageView) rootView.findViewById(R.id.image);
            Picasso.with(this.getActivity())
                    .load(plane.getSmallImageUrl())
                    .resize(640, 410)
                    .centerCrop()
                    .into(image, new Callback() {
                        @Override
                        public void onSuccess() {
                            loader.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            loader.setVisibility(View.GONE);
                        }
                    });

            final Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
            final Animation slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);

            if (ab.isShowing()) {
                detailsPanel.setVisibility(View.VISIBLE);
            } else {
                detailsPanel.setVisibility(View.INVISIBLE);
            }

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ab.isShowing()) {
                        ab.hide();
                    } else {
                        ab.show();
                        detailsPanel.setVisibility(View.VISIBLE);
                    }
                    getActivity().sendBroadcast(new Intent().setAction("RELOAD"));
                }
            });

            return rootView;
        }
    }
}