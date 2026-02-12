package com.example.crowdsenseddt.ui

import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.crowdsenseddt.model.SignalSample
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun SignalLineChart(
    modifier: Modifier = Modifier,
    samples: List<SignalSample>,
    label: String
) {

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {

                val entries = samples.map {
                    Entry(it.timestamp.toFloat(), it.value)
                }

                val dataSet = LineDataSet(entries, label).apply {
                    color = Color.BLUE
                    valueTextColor = Color.BLACK
                    setDrawCircles(true)
                    setDrawValues(false)
                    lineWidth = 2f
                }

                data = LineData(dataSet)

                description.isEnabled = false
                axisRight.isEnabled = false
                xAxis.granularity = 1f

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }
    )
}