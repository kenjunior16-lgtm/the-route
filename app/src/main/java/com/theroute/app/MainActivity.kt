package com.theroute.app

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.theroute.app.service.Notify
import com.theroute.app.ui.*

class MainActivity : ComponentActivity() {
    private val vm: RouteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Notify.ensureChannel(this)
        // Portrait by default; rotation only if the user turned it on.
        requestedOrientation = if (vm.repo.allowRotation) ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        vm.resumeTrackingIfLive()
        setContent {
            MaterialTheme {
                Surface {
                    val screen by vm.screen.collectAsState()
                    when (screen) {
                        Screen.HOME -> HomeScreen(vm)
                        Screen.PARADES -> ParadesScreen(vm)
                        Screen.VENDORS -> VendorsScreen(vm)
                        Screen.PROFILE -> ProfileScreen(vm)
                        Screen.MAP -> MapScreen(vm)
                        Screen.LEAD -> LeadScreen(vm)
                        Screen.ADMIN -> AdminScreen(vm)
                        Screen.REGISTER -> RegisterScreen(vm)
                        Screen.VENDOR_FORM -> VendorFormScreen(vm)
                        Screen.LIMITS -> LimitsScreen(vm)
                    }
                }
            }
        }
    }
}
