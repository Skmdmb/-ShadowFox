package com.example.healthbuddy025;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthbuddy025.databinding.ActivityStatsBinding;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

    private ActivityStatsBinding binding;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dataManager = new DataManager(this);
        // initial chart draw
        showChart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh chart each time Stats screen is opened
        showChart();
    }

    private void showChart() {
        List<Integer> last7 = dataManager.getLast7(); // oldest ... newest
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < last7.size(); i++) {
            entries.add(new Entry(i, last7.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Steps");
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        binding.lineChart.setData(lineData);

        Description desc = new Description();
        desc.setText("");
        binding.lineChart.setDescription(desc);
        binding.lineChart.getLegend().setEnabled(false);

        // X axis labels as weekdays (Mon, Tue, ...)
        String[] labels = getLast7WeekdayLabels();
        XAxis xAxis = binding.lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setDrawGridLines(false);

        binding.lineChart.invalidate();
    }

    private String[] getLast7WeekdayLabels() {
        // last7 is oldest..newest; label each entry with its weekday
        String[] result = new String[7];
        Calendar cal = Calendar.getInstance();
        // position '6' corresponds to today; we need 6 days back -> index 0 is 6 days ago
        for (int i = 6; i >= 0; i--) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.DAY_OF_MONTH, - (6 - i));
            String[] shortWeekdays = new DateFormatSymbols().getShortWeekdays(); // index 1..7
            int dow = c.get(Calendar.DAY_OF_WEEK); // 1..7
            // shortWeekdays[dow] returns e.g. "Sun","Mon"
            result[i] = shortWeekdays[dow];
        }
        return result;
    }
}
