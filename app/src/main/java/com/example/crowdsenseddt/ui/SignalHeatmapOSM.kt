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
import com.example.crowdsenseddt.model.SignalPoint

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

                controller.setZoom(13.0)
                controller.setCenter(GeoPoint(3.8480, 11.5021))

                signalPoints.forEach { point ->
                    val marker = Marker(this)
                    marker.position = GeoPoint(point.latitude, point.longitude)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    marker.icon?.setTint(getColorForRsrp(point.rsrp))
                    marker.title = "RSRP: ${point.rsrp} dBm"
                    overlays.add(marker)
                }

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
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
