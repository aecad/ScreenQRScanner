package com.example.screenqrscanner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screenqrscanner.ui.theme.ScreenQRScannerTheme
import com.google.mlkit.vision.barcode.common.Barcode

class ScanResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val result = intent.getStringExtra("scan_result") ?: ""
        val type = intent.getIntExtra("barcode_type", Barcode.TYPE_TEXT)
        
        setContent {
            ScreenQRScannerTheme {
                ScanResultScreen(
                    result = result,
                    type = type,
                    onDismiss = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    result: String,
    type: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("扫码结果") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 结果类型
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getTypeIcon(type),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = getTypeLabel(type),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            // 结果内容
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "内容:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 操作按钮
            when (type) {
                Barcode.TYPE_URL -> {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result))
                            context.startActivity(intent)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("打开链接")
                    }
                }
                Barcode.TYPE_WIFI -> {
                    Button(
                        onClick = {
                            copyToClipboard(context, result)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.NetworkWifi, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("复制WiFi信息")
                    }
                }
                Barcode.TYPE_PHONE -> {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$result"))
                            context.startActivity(intent)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("拨打电话")
                    }
                }
                Barcode.TYPE_EMAIL -> {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$result"))
                            context.startActivity(intent)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("发送邮件")
                    }
                }
            }
            
            // 复制按钮
            OutlinedButton(
                onClick = {
                    copyToClipboard(context, result)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.FileCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("复制内容")
            }
            
            // 分享按钮
            OutlinedButton(
                onClick = {
                    shareText(context, result)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("分享")
            }
        }
    }
}

private fun getTypeIcon(type: Int): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        Barcode.TYPE_URL -> Icons.Default.InsertLink
        Barcode.TYPE_WIFI -> Icons.Default.NetworkWifi
        Barcode.TYPE_PHONE -> Icons.Default.Phone
        Barcode.TYPE_EMAIL -> Icons.Default.Email
        Barcode.TYPE_CONTACT_INFO -> Icons.Default.Person
        Barcode.TYPE_GEO -> Icons.Default.LocationOn
        else -> Icons.Default.QrCode2
    }
}

private fun getTypeLabel(type: Int): String {
    return when (type) {
        Barcode.TYPE_URL -> "网址链接"
        Barcode.TYPE_WIFI -> "WiFi网络"
        Barcode.TYPE_PHONE -> "电话号码"
        Barcode.TYPE_EMAIL -> "电子邮件"
        Barcode.TYPE_CONTACT_INFO -> "联系人"
        Barcode.TYPE_GEO -> "地理位置"
        Barcode.TYPE_CALENDAR_EVENT -> "日历事件"
        Barcode.TYPE_DRIVER_LICENSE -> "驾驶证"
        else -> "文本内容"
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("扫码结果", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "分享扫码结果"))
}
