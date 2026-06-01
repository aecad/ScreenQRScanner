package com.example.screenqrscanner.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.example.screenqrscanner.ScreenCaptureActivity

@RequiresApi(Build.VERSION_CODES.N)
class QrScannerTileService : TileService() {

    override fun onClick() {
        super.onClick()
        
        // 解锁屏幕（如果需要）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(this, ScreenCaptureActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivityAndCollapse(intent)
        } else {
            val intent = Intent(this, ScreenCaptureActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_ACTIVE
            label = "屏幕扫码"
            updateTile()
        }
    }
}
