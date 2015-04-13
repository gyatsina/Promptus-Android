package sdk.labwerk.sample;

import android.widget.SearchView;

public class SimpleTextListener implements SearchView.OnQueryTextListener{
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
