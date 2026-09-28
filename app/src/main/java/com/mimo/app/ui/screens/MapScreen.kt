package com.mimo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.mimo.app.data.model.SaveItem
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

/** Dark flat map, not Apple Maps or a real globe. No location permission is requested. */
@Composable
fun MapScreen(savedItems: List<SaveItem> = emptyList(), modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var map by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }
    val mapView = remember(context) {
        MapLibre.getInstance(context)
        MapView(context).apply {
            onCreate(null)
            getMapAsync { loaded ->
                loaded.setStyle("https://tiles.openfreemap.org/styles/dark")
                loaded.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(24.0, 30.0)).zoom(1.45).build()
                map = loaded
            }
        }
    }
    DisposableEffect(mapView, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        when (lifecycleOwner.lifecycle.currentState) {
            Lifecycle.State.RESUMED -> { mapView.onStart(); mapView.onResume() }
            Lifecycle.State.STARTED -> mapView.onStart()
            else -> Unit
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }
    Box(modifier.fillMaxSize().background(Color(0xFF081A2E))) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp, start = 20.dp, end = 20.dp)) {
            Text("Explore places", color = Color.White, fontSize = 24.sp)
            Spacer(Modifier.size(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                    .background(Color(0xF0141B27)).padding(horizontal = 18.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFB8C2D0))
                Spacer(Modifier.width(10.dp))
                Text("Places saved to Mimo", color = Color(0xFFB8C2D0), fontSize = 14.sp)
            }
        }
        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { map?.let { it.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.zoomIn()) } },
                modifier = Modifier.clip(CircleShape).background(Color(0xED121924))) {
                Icon(Icons.Default.Add, contentDescription = "Zoom in", tint = Color.White)
            }
            IconButton(onClick = { map?.let { it.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.zoomOut()) } },
                modifier = Modifier.clip(CircleShape).background(Color(0xED121924))) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom out", tint = Color.White)
            }
        }
        // Always visible, even while the vector style is loading or if tiles fail.
        Text(
            "© OpenStreetMap contributors · OpenFreeMap",
            color = Color.White,
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 112.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xC9111928))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
