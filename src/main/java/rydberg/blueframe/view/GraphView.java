package rydberg.blueframe.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * Created by rydberg on 7/5/2016.
 */
public class GraphView  extends com.jjoe64.graphview.GraphView {

    private static final int MAX_DATA_POINTS = 150;
    private static final boolean SCROLL_TO_END = true;
    private LineGraphSeries<DataPoint> seriesA;

    public GraphView(Context context) {
        super(context);
        initView();
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        seriesA = new LineGraphSeries<>();


        seriesA.setColor(Color.BLACK);

        seriesA.setTitle("Sensor A");

        getViewport().setXAxisBoundsManual(false);

       // getViewport().setXAxisBoundsManual(true);
       // getViewport().setMinX(0);
       // getViewport().setMaxX(100);

        getViewport().setYAxisBoundsManual(true);
        getViewport().setMinY(4250);
        getViewport().setMaxY(5500);

        getViewport().setScalable(true);
        getViewport().setScrollable(true);

        getLegendRenderer().setVisible(true);
        getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        addSeries(seriesA);
    }

    public void appendData(DataPoint dataPointA) {
        seriesA.appendData(dataPointA, SCROLL_TO_END, MAX_DATA_POINTS);
    }

}
