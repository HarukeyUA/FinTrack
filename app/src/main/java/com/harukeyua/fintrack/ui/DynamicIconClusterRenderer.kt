package com.harukeyua.fintrack.ui

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class DynamicIconClusterRenderer(
    context: Context,
    map: GoogleMap?,
    clusterManager: ClusterManager<TransactionClusterItem>?
) : DefaultClusterRenderer<TransactionClusterItem>(
    context,
    map,
    clusterManager
) {

    override fun onBeforeClusterItemRendered(
        item: TransactionClusterItem,
        markerOptions: MarkerOptions
    ) {
        markerOptions.icon(
            BitmapDescriptorFactory.defaultMarker(
                if (item.isGain)
                    BitmapDescriptorFactory.HUE_GREEN
                else
                    BitmapDescriptorFactory.HUE_ROSE
            )
        )
        super.onBeforeClusterItemRendered(item, markerOptions)
    }
}