package com.example.crowdsenseddt.ui

import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.example.crowdsenseddt.data.SignalPoint

@Composable
fun SignalHeatmapOSM(
    modifier: Modifier = Modifier,
    signalPoints: List<SignalPoint>,
    parameter: SignalParameter
) {

    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
            }
        },
        update = { mapView ->

            mapView.overlays.clear()

            signalPoints.forEach { point ->
                val marker = Marker(mapView)
                marker.position = GeoPoint(point.latitude, point.longitude)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                marker.title = "RSRP: ${point.rsrp}"
                mapView.overlays.add(marker)
            }

            if (signalPoints.isNotEmpty()) {
                val last = signalPoints.last()
                mapView.controller.setCenter(
                    GeoPoint(last.latitude, last.longitude)
                )
            }

            mapView.invalidate()
        }
    )

}

private fun getColorForRsrp(rsrp: Double?): Int {
    if (rsrp != null) {
        return when {
            rsrp >= -80 -> Color.GREEN
            rsrp >= -90 -> Color.YELLOW
            rsrp >= -100 -> Color.rgb(255, 165, 0) // Orange
            else -> Color.RED
        }
    }
    return Color.GRAY
}
