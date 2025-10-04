package me.ezar.anemon.network.websocket.models

import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.Serializable

@Serializable
data class Point(val latitude: Double, val longitude: Double) {
    fun toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }

    companion object Factory {
        fun fromLatLng(latLng: LatLng): Point {
            return Point(latLng.latitude, latLng.longitude)
        }
    }
}


@Serializable
data class LineSegment(val start: Point, val end: Point) {
    companion object Factory {
        fun fromPolyline(polyline: List<LatLng>): List<LineSegment> {
            return polyline.zipWithNext { start, end ->
                LineSegment(Point.fromLatLng(start), Point.fromLatLng(end))
            }
        }
    }
}