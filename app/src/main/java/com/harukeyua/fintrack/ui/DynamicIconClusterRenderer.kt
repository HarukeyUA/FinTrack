/*
 * Copyright  2021 Nazar Rusnak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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