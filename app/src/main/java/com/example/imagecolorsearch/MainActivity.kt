package com.example.imagecolorsearch

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.example.imagecolorsearch.databinding.ActivityMainBinding
import com.example.imagecolorsearch.ui.main.MainFragment
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        if (savedInstanceState == null) {
            requestPermissionsIfNeeded()
        }
    }

    private fun requestPermissionsIfNeeded() {
        val requestedPermissions =
            packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions.toMutableList()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            requestedPermissions.remove(android.Manifest.permission.READ_MEDIA_IMAGES)
        }

        val missingPermissions = requestedPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }


        if (missingPermissions.isEmpty()) {
            showFragment()
            return
        }
        requestPermissions(missingPermissions.toTypedArray(), PERMISSION_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            permissions.forEachIndexed { index, permission ->
                Log.d(MainActivity::class.simpleName, "${permission} granted: ${grantResults[index] == PackageManager.PERMISSION_GRANTED}")
            }
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                Snackbar.make(
                    binding.container,
                    "Permissions required to display photos.",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                showFragment()
            }
        }
    }

    private fun showFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment.newInstance()).commitNow()
    }

    companion object {
        private const val PERMISSION_REQUEST = 13455
    }

}