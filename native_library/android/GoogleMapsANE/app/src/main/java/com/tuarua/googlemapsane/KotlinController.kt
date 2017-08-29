/*
 *  Copyright 2017 Tua Rua Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.tuarua.googlemapsane

import android.content.Intent
import android.graphics.Rect
import android.view.ViewGroup
import com.adobe.fre.FREContext
import com.adobe.fre.FREObject
import com.google.android.gms.maps.model.*

import com.tuarua.frekotlin.*
import com.tuarua.frekotlin.geom.FreRectangleKotlin
import java.util.ArrayList

typealias FREArgv = ArrayList<FREObject>
@Suppress("unused", "UNUSED_PARAMETER", "UNCHECKED_CAST")
class KotlinController : FreKotlinController {
    private var scaleFactor: Double = 1.0
    private var context: FREContext? = null
    private lateinit var airView: ViewGroup
    private val TRACE = "TRACE"
    private var isAdded: Boolean = false
    private var settings: Settings = Settings()
    private var asListeners: ArrayList<String> = ArrayList()
    private var listenersAddedToMapC: Boolean = false

    private var mapController: MapController? = null

    fun isSupported(ctx: FREContext, argv: FREArgv): FREObject? {
        return FreObjectKotlin(true).rawValue.guard { return null }
    }

    fun init(ctx: FREContext, argv: FREArgv): FREObject? {
        airView = context?.activity?.findViewById(android.R.id.content) as ViewGroup
        airView = airView.getChildAt(0) as ViewGroup
        return FreObjectKotlin(true).rawValue.guard { return null }
    }

    fun initMap(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 4 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        try {
            val zl = FreObjectKotlin(argv[2]).value // zoomLevel:Number
            val sf = FreObjectKotlin(argv[4]).value // scaleFactor:Number
            val zoomLevel = (zl as? Int)?.toFloat() ?: (zl as Double).toFloat()
            scaleFactor = (sf as? Int)?.toDouble() ?: sf as Double
            val centerAt = FreCoordinateKotlin(argv[1]).value // centerAt:Coordinate
            val viewPort = FreRectangleKotlin(argv[0]).value // viewPort:Rectangle
            val settingsFre = FreObjectKotlin(argv[3]) // settings: Settings
            settings.compassButton = settingsFre.getProperty("compassButton")?.value as Boolean
            settings.indoorPicker = settingsFre.getProperty("indoorPicker")?.value as Boolean
            settings.myLocationButton = settingsFre.getProperty("myLocationButton")?.value as Boolean
            settings.rotateGestures = settingsFre.getProperty("rotateGestures")?.value as Boolean
            settings.scrollGestures = settingsFre.getProperty("scrollGestures")?.value as Boolean
            settings.tiltGestures = settingsFre.getProperty("tiltGestures")?.value as Boolean
            settings.zoomGestures = settingsFre.getProperty("zoomGestures")?.value as Boolean
            mapController = MapController(ctx, airView, centerAt, zoomLevel, scaleViewPort(viewPort), settings)
        } catch (e: FreException) {
            return e.getError(Thread.currentThread().stackTrace) //return the error as an actionscript error
        } catch (e: Exception) {
            return FreException(e).getError(Thread.currentThread().stackTrace)
        }
        return null
    }

    fun addEventListener(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 0 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        val type: String = FreObjectKotlin(argv[0]).value as? String ?: return null
        if (mapController == null) {
            asListeners.add(type)
        } else {
            if (!listenersAddedToMapC) {
                for (asListener in asListeners) {
                    asListeners.add(asListener)
                }
            }
            listenersAddedToMapC = true
        }
        mapController?.addEventListener(type)
        return null
    }

    fun removeEventListener(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 0 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        val type: String = FreObjectKotlin(argv[0]).value as? String ?: return null
        if (mapController == null) {
            asListeners.remove(type)
        } else {
            if (!listenersAddedToMapC) {
                for (asListener in asListeners) {
                    asListeners.remove(asListener)
                }
            }
        }
        mapController?.removeEventListener(type)
        return null
    }

    fun addCircle(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 0 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        try {
            val circleOptions: CircleOptions = FreCircleOptionsKotlin(argv[0]).value
            mapController?.addCircle(circleOptions)
        } catch (e: FreException) {
            return e.getError(Thread.currentThread().stackTrace)
        } catch (e: Exception) {
            return FreException(e).getError(Thread.currentThread().stackTrace)
        }
        return null
    }

    fun addMarker(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 0 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        try {
            val markerOptionsFre = FreMarkerOptionsKotlin(argv[0]) // marker:Marker
            val markerOptions = markerOptionsFre.value ?: return null
            val addedMarker: Marker? = mapController?.addMarker(markerOptions)
            return FreObjectKotlin(addedMarker?.id).rawValue.guard { return null }
        } catch (e: FreException) {
            return e.getError(Thread.currentThread().stackTrace)
        } catch (e: Exception) {
            return FreException(e).getError(Thread.currentThread().stackTrace)
        }
    }

    fun updateMarker(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 1 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)

        try {
            val uuid: String = FreObjectKotlin(argv[0]).value as String // id:String
            val markerOptionsFre = FreMarkerOptionsKotlin(argv[1]) // marker:Marker
            val markerOptions = markerOptionsFre.value ?: return null
            mapController?.updateMarker(uuid, markerOptions)
        } catch (e: FreException) {
            return e.getError(Thread.currentThread().stackTrace)
        } catch (e: Exception) {
            return FreException(e).getError(Thread.currentThread().stackTrace)
        }

        return null
    }

    fun removeMarker(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 0 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        try {
            val uuid: String = FreObjectKotlin(argv[0]).value as String
            mapController?.removeMarker(uuid)
        } catch (e: FreException) {
            return e.getError(Thread.currentThread().stackTrace)
        } catch (e: Exception) {
            return FreException(e).getError(Thread.currentThread().stackTrace)
        }
        return null
    }

    fun showInfoWindow(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 0 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        try {
            val uuid: String = FreObjectKotlin(argv[0]).value as String // uuid:String
            mapController?.showInfoWindow(uuid)
        } catch (e: FreException) {
            return e.getError(Thread.currentThread().stackTrace)
        } catch (e: Exception) {
            return FreException(e).getError(Thread.currentThread().stackTrace)
        }
        return null
    }

    fun hideInfoWindow(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 0 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        try {
            val uuid: String = FreObjectKotlin(argv[0]).value as String  // uuid:String
            mapController?.hideInfoWindow(uuid)
        } catch (e: FreException) {
            return e.getError(Thread.currentThread().stackTrace)
        } catch (e: Exception) {
            return FreException(e).getError(Thread.currentThread().stackTrace)
        }
        return null
    }

    fun clear(ctx: FREContext, argv: FREArgv): FREObject? {
        mapController?.clear()
        return null
    }

    private fun scaleViewPort(rect: Rect): Rect {
        return Rect((rect.left * scaleFactor).toInt(), (rect.top * scaleFactor).toInt(),
                (rect.width() * scaleFactor).toInt(), ((rect.height() + rect.top) * scaleFactor).toInt())
    }

    fun setViewPort(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 0 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        val viewPortFre = FreRectangleKotlin(argv[0]).value // viewPort:Rectangle
        mapController?.viewPort = scaleViewPort(viewPortFre)
        return null
    }

    fun setVisible(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 0 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        val visible = FreObjectKotlin(argv[0]).value as Boolean
        if (!isAdded) {
            mapController?.add()
            isAdded = true
        }
        mapController?.visible = visible
        return null
    }

    fun moveCamera(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 4 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        val cameraPositionBuilder: CameraPosition.Builder = CameraPosition.builder()
        try {
            val targetFre = FreCoordinateKotlin(argv[0])
            val zoomFre = FreObjectKotlin(argv[1])
            val tiltFre = FreObjectKotlin(argv[2])
            val bearingFre = FreObjectKotlin(argv[3])
            val animates = FreObjectKotlin(argv[4]).value as Boolean

            if (targetFre.getType() != FreObjectTypeKotlin.NULL) {
                cameraPositionBuilder.target(targetFre.value)
            }
            if (zoomFre.getType() != FreObjectTypeKotlin.NULL) {
                val zoom: Float = (zoomFre.value as? Int)?.toFloat() ?: (zoomFre.value as Double).toFloat()
                cameraPositionBuilder.zoom(zoom)
            }
            if (tiltFre.getType() != FreObjectTypeKotlin.NULL) {
                val tilt: Float = (tiltFre.value as? Int)?.toFloat() ?: (tiltFre.value as Double).toFloat()
                cameraPositionBuilder.tilt(tilt)
            }
            if (bearingFre.getType() != FreObjectTypeKotlin.NULL) {
                val bearing: Float = (bearingFre.value as? Int)?.toFloat() ?: (bearingFre.value as Double).toFloat()
                cameraPositionBuilder.bearing(bearing)
            }
            mapController?.moveCamera(cameraPositionBuilder.build(), animates)
        } catch (e: FreException) {
            return e.getError(Thread.currentThread().stackTrace)
        } catch (e: Exception) {
            return FreException(e).getError(Thread.currentThread().stackTrace)
        }
        return null
    }

    fun setBounds(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 2 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        val southWest = FreCoordinateKotlin(argv[0]).value
        val northEast = FreCoordinateKotlin(argv[1]).value
        val animates = FreObjectKotlin(argv[2]).value as Boolean
        mapController?.setBounds(LatLngBounds(southWest, northEast), animates)
        return null
    }

    fun zoomIn(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 0 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        val animates = FreObjectKotlin(argv[0]).value as Boolean
        mapController?.zoomIn(animates)
        return null
    }

    fun zoomOut(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 0 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        val animates = FreObjectKotlin(argv[0]).value as Boolean
        mapController?.zoomOut(animates)
        return null
    }

    fun zoomTo(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 1 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        val toFre = FreObjectKotlin(argv[0]).value
        val zoomLevel = (toFre as? Int)?.toFloat() ?: (toFre as Double).toFloat()
        val animates = FreObjectKotlin(argv[1]).value as Boolean
        mapController?.zoomTo(zoomLevel, animates)
        return null
    }

    fun setAnimationDuration(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 0 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        mapController?.animationDuration = FreObjectKotlin(argv[0]).value as Int
        return null
    }

    fun setStyle(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 0 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        try {
            val json = FreObjectKotlin(argv[0]).value as String
            mapController?.style = json
        } catch (e: Exception) {
            return FreException(e).getError(Thread.currentThread().stackTrace)
        }
        return null
    }

    fun setMapType(ctx: FREContext, argv: FREArgv): FREObject? {
        argv.takeIf { argv.size > 0 } ?: return ArgCountException().getError(Thread.currentThread().stackTrace)
        val type: Int = FreObjectKotlin(argv[0]).value as Int
        mapController?.mapType = type
        return null
    }

    fun requestLocation(ctx: FREContext, argv: FREArgv): FREObject? {
        try {
            val permissionIntent = Intent(ctx.activity.applicationContext, PermissionActivity::class.java)
            ctx.activity.startActivity(permissionIntent)
        } catch (e: Exception) {
            return FreException(e).getError(Thread.currentThread().stackTrace)
        }
        return null
    }

    override fun onStarted() {
        super.onStarted()
    }

    override fun onRestarted() {
        super.onRestarted()
    }

    override fun onResumed() {
        super.onResumed()
    }

    override fun onPaused() {
        super.onPaused()
    }

    override fun onStopped() {
        super.onStopped()
    }

    override fun onDestroyed() {
        super.onDestroyed()
    }

    override fun setFREContext(context: FREContext) {
        this.context = context
    }

    private fun trace(vararg value: Any?) {
        context?.trace(TAG, value)
    }

    private fun sendEvent(name: String, value: String) {
        context?.sendEvent(name, value)
    }

    companion object {
        private var TAG = KotlinController::class.java.canonicalName
    }

}