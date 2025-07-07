package com.example.drwebtestapp.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.drwebtestapp.model.AppDetails
import com.example.drwebtestapp.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsScreen(
    packageName: String,
    viewModel: AppViewModel,
    onBackClick: () -> Unit
) {
    val appDetails by viewModel.appDetails.collectAsState()
    val isLoading by viewModel.isLoadingDetails.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(packageName) {
        viewModel.loadAppDetails(packageName)
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = appDetails?.appInfo?.name ?: "App Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                appDetails?.let { details ->
                    IconButton(
                        onClick = { 
                            val success = viewModel.launchApp(details.appInfo.packageName)
                            if (!success) {
                                // Show snackbar or toast
                            }
                        }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Launch App")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading app details...",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            appDetails == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Failed to load app details",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadAppDetails(packageName) }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            else -> {
                AppDetailsContent(
                    appDetails = appDetails!!,
                    onLaunchApp = { 
                        val success = viewModel.launchApp(packageName)
                        if (!success) {
                            // Show error message
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AppDetailsContent(
    appDetails: AppDetails,
    onLaunchApp: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showAllActivities by remember { mutableStateOf(false) }
    var showAllPermissions by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // App Header
        AppHeader(
            appDetails = appDetails,
            onLaunchApp = onLaunchApp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Basic Information
        InfoSection(
            title = "Basic Information",
            items = listOf(
                "Package Name" to appDetails.appInfo.packageName,
                "Version" to appDetails.appInfo.versionName,
                "Version Code" to appDetails.appInfo.versionCode.toString(),
                "System App" to if (appDetails.appInfo.isSystemApp) "Yes" else "No"
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // File Information
        InfoSection(
            title = "File Information",
            items = listOf(
                "APK Path" to appDetails.appInfo.apkPath,
                "File Size" to formatFileSize(appDetails.apkSize),
                "SHA-256 Checksum" to appDetails.sha256Checksum
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Install Information
        InfoSection(
            title = "Install Information",
            items = listOf(
                "Install Date" to formatDate(appDetails.appInfo.installTime),
                "Update Date" to formatDate(appDetails.appInfo.updateTime)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Activities
        if (appDetails.activities.isNotEmpty()) {
            val activitiesItems = if (appDetails.activities.size > 10 && !showAllActivities) {
                appDetails.activities.take(9).map { activity ->
                    "•" to (activity.split('.').lastOrNull() ?: activity)
                } + listOf("..." to "Show all ${appDetails.activities.size} activities")
            } else {
                appDetails.activities.map { activity ->
                    "•" to (activity.split('.').lastOrNull() ?: activity)
                }
            }
            
            ExpandableInfoSection(
                title = "Activities (${appDetails.activities.size})",
                items = activitiesItems,
                isExpanded = showAllActivities,
                onToggle = { showAllActivities = !showAllActivities }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Permissions
        if (appDetails.permissions.isNotEmpty()) {
            val permissionsItems = if (appDetails.permissions.size > 10 && !showAllPermissions) {
                appDetails.permissions.take(9).map { permission ->
                    "•" to (permission.split('.').lastOrNull() ?: permission)
                } + listOf("..." to "Show all ${appDetails.permissions.size} permissions")
            } else {
                appDetails.permissions.map { permission ->
                    "•" to (permission.split('.').lastOrNull() ?: permission)
                }
            }
            
            ExpandableInfoSection(
                title = "Permissions (${appDetails.permissions.size})",
                items = permissionsItems,
                isExpanded = showAllPermissions,
                onToggle = { showAllPermissions = !showAllPermissions }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AppHeader(
    appDetails: AppDetails,
    onLaunchApp: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon
            appDetails.appInfo.icon?.let { icon ->
                Image(
                    bitmap = icon.toBitmap(128, 128).asImageBitmap(),
                    contentDescription = "App icon",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
            } ?: run {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appDetails.appInfo.name.firstOrNull()?.toString() ?: "?",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App Name
            Text(
                text = appDetails.appInfo.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Version
            Text(
                text = "Version ${appDetails.appInfo.versionName}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Launch Button
            Button(
                onClick = onLaunchApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Launch",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Launch App",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    items: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            items.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                InfoItem(label = label, value = value)
            }
        }
    }
}

@Composable
fun ExpandableInfoSection(
    title: String,
    items: List<Pair<String, String>>,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            items.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (label == "...") {
                    ExpandableInfoItem(
                        label = label,
                        value = value,
                        onClick = onToggle
                    )
                } else {
                    InfoItem(label = label, value = value)
                }
            }
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String
) {
    if (label == "•") {
        // Special handling for list items
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "•",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp, top = 2.dp)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        // Regular info item
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ExpandableInfoItem(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp, top = 2.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1.0 -> "%.2f GB".format(gb)
        mb >= 1.0 -> "%.2f MB".format(mb)
        kb >= 1.0 -> "%.2f KB".format(kb)
        else -> "$bytes bytes"
    }
}

fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}
