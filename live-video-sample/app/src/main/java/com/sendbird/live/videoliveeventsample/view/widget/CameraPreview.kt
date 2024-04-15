
package com.sendbird.live.videoliveeventsample.view.widget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ExifInterface
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper.getMainLooper
import android.util.AttributeSet
import android.util.Size
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.util.concurrent.HandlerExecutor
import com.sendbird.live.videoliveeventsample.databinding.ViewCameraPreviewBinding
import com.sendbird.webrtc.VideoDevice
import java.util.Collections

class CameraPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: ViewCameraPreviewBinding = ViewCameraPreviewBinding.inflate(LayoutInflater.from(getContext()), this, true)

    companion object {
        private val ORIENTATIONS = SparseIntArray()
        init {
            ORIENTATIONS.append(ExifInterface.ORIENTATION_NORMAL, 0)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_90, 90)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_180, 180)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_270, 270)
        }
    }

    private var isinitialized: Boolean = false
    private var cameraManager: CameraManager? = null
    private val mainHandler: Handler = Handler(getMainLooper())

    private var childHandler: Handler? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    internal val isAvailable
        get() = binding.svCameraPreview.isAvailable

    internal var currentVideoDevice: VideoDevice? = null

    internal fun initLayout(cameraManager: CameraManager, currentVideoDevice: VideoDevice, surfaceTextureListener: TextureView.SurfaceTextureListener) {
        this.cameraManager = cameraManager
        this.currentVideoDevice = currentVideoDevice
        binding.svCameraPreview.surfaceTextureListener = surfaceTextureListener
    }

    var previewSize: Size? = null
    private var map: StreamConfigurationMap? = null

    private fun initCamera(cameraManager: CameraManager, width: Int = 0, height: Int = 0) {
        this.cameraManager = cameraManager
        val characteristics: CameraCharacteristics = currentVideoDevice?.cameraCharacteristics ?: return
        map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val largest: Size = Collections.max(map!!.getOutputSizes(ImageFormat.JPEG).toMutableList(), CompareSizesByArea())
        val displaySize = Point()
        val maxPreviewWidth = displaySize.x
        val maxPreviewHeight = displaySize.y
        previewSize = chooseOptimalSize(map!!.getOutputSizes(SurfaceTexture::class.java), width, height, maxPreviewWidth, maxPreviewHeight, largest)
        val previewSize = previewSize ?: return
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.svCameraPreview.setAspectRatio(previewSize.width, previewSize.height)
        } else {
            binding.svCameraPreview.setAspectRatio(previewSize.height, previewSize.width)
        }
        isinitialized = true
    }

    private fun startPreview() {
        val cameraDevice = cameraDevice
        if (cameraDevice == null || !binding.svCameraPreview.isAvailable || previewSize == null) {
            // TODO: show error
            return
        }
        val surface = Surface(binding.svCameraPreview.surfaceTexture)
        try {
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                this.addTarget(surface)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val sessionConfiguration = SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        listOf(OutputConfiguration(surface)),
                        HandlerExecutor(mainHandler.looper),
                        previewStateCallback
                    )
                    cameraDevice.createCaptureSession(sessionConfiguration)
                } else {
                    cameraDevice.createCaptureSession(
                        listOf(
                            surface
                        ),
                        previewStateCallback,
                        childHandler
                    )
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    internal fun switchCameraByCameraId(currentCameraID: String) {
        val cameraManager = cameraManager ?: return
        if (!isinitialized) initCamera(cameraManager, width, height)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: fit error
            return
        }
        releaseCamera()
        cameraManager.openCamera(currentCameraID, stateCallback, mainHandler)
    }

    internal fun releaseCamera() {
        cameraDevice?.close()
        cameraDevice = null
    }

    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            startPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
        }
    }

    private val previewStateCallback: CameraCaptureSession.StateCallback =
        object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                if (null == cameraDevice) return
                this@CameraPreview.cameraCaptureSession = cameraCaptureSession
                updatePreview()
            }

            override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
            }
        }

    private fun updatePreview() {
        if (cameraDevice == null) {
            // TODO: show error
            return
        }
        previewRequestBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        val handlerThread = HandlerThread("cameraPreview")
        handlerThread.start()
        childHandler = Handler(handlerThread.looper)
        try {
            val previewRequest = previewRequestBuilder?.build()
            if (previewRequest != null) {
                this@CameraPreview.cameraCaptureSession?.setRepeatingRequest(previewRequest, null, childHandler)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun chooseOptimalSize(choices: Array<Size>, textureViewWidth: Int, textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size): Size {
        val bigEnough: MutableList<Size> = ArrayList()
        val notBigEnough: MutableList<Size> = ArrayList()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight && option.height == option.width * h / w) {
                if (option.width >= textureViewWidth &&
                    option.height >= textureViewHeight
                ) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }
        return if (bigEnough.size > 0) {
            Collections.min(bigEnough, CompareSizesByArea())
        } else if (notBigEnough.size > 0) {
            Collections.max(notBigEnough, CompareSizesByArea())
        } else {
            choices[0]
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearCameraSession()
    }

    fun clearCameraSession() {
        cameraManager = null
        previewRequestBuilder = null
        cameraCaptureSession?.close()
        cameraCaptureSession = null
        releaseCamera()
    }

    internal class CompareSizesByArea : Comparator<Size> {
        override fun compare(lhs: Size, rhs: Size): Int {
            return java.lang.Long.signum(
                lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height
            )
        }
    }
}
