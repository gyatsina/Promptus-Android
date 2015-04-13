package sdk.labwerk.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sdk.labwerk.core.model.api.Beacon;


public class BeaconsAdapter extends BaseAdapter implements Filterable {

    private List<Beacon> beaconList;
    private Context context;
    private Filter beaconFilter;
    private List<Beacon> origPlanetList;

    public BeaconsAdapter(Context ctx) {
        this.context = ctx;
        this.origPlanetList = new ArrayList<>();
        beaconList = new ArrayList<>();
    }


    public void addAll(List<Beacon> beacons) {
        origPlanetList.addAll(beacons);
        notifyDataSetChanged();
    }

    public int getCount() {
        return beaconList.size();
    }

    public Beacon getItem(int position) {
        return beaconList.get(position);
    }

    public long getItemId(int position) {
        return beaconList.get(position).hashCode();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        PlanetHolder holder = new PlanetHolder();


        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.img_row_layout, null);


            holder.beaconNameView = (TextView) v.findViewById(R.id.name);

            v.setTag(holder);
        } else
            holder = (PlanetHolder) v.getTag();

        Beacon p = beaconList.get(position);
        holder.beaconNameView.setText(p.getName());

        return v;
    }


    private static class PlanetHolder {
        public TextView beaconNameView;
    }


    @Override
    public Filter getFilter() {
        if (beaconFilter == null)
            beaconFilter = new BeaconsFilter();

        return beaconFilter;
    }


    private class BeaconsFilter extends Filter {


        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.values = new ArrayList<>();
                results.count = 0;
            } else {

                List<Beacon> beaconArrayList = new ArrayList<>();

                for (Beacon beacon : origPlanetList) {
                    if (beacon.getName().toUpperCase().contains(constraint.toString().toUpperCase())) {
                        beaconArrayList.add(beacon);
                    }
                }

                results.values = beaconArrayList;
                results.count = beaconArrayList.size();

            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            beaconList = (List<Beacon>) results.values;
            notifyDataSetChanged();
        }

    }
}
