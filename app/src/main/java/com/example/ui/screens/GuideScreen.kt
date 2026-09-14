package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.VideoViewModel

@Composable
fun GuideScreen(
    viewModel: VideoViewModel,
    modifier: Modifier = Modifier
) {
    val isTamil by viewModel.isTamil.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isTamil) "வீடியோ டவுன்லோட் செய்வது எப்படி?" else "How to Download Videos",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isTamil) "1-கிளிக் எளிய வழிகாட்டி" else "1-Click Simple Quickstart Guide",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Method 1: In-App Paste
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isTamil) "முறை 1: ஆப் வழியாக 1-கிளிக் டவுன்லோட்" else "Method 1: Direct In-App 1-Click",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    GuideStepItem(
                        icon = Icons.Default.ContentCopy,
                        stepNumber = "படி 1",
                        title = if (isTamil) "லிங்கை காப்பி செய்யவும்" else "Copy the Video Link",
                        description = if (isTamil) "YouTube, Instagram Reel அல்லது Facebook வீடியோவில் உள்ள 'Share' பட்டனை அழுத்தி 'Copy Link' செய்யவும்."
                        else "In YouTube, Instagram Reel, or Facebook video, tap the 'Share' icon and choose 'Copy Link'."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    GuideStepItem(
                        icon = Icons.Default.PhoneAndroid,
                        stepNumber = "படி 2",
                        title = if (isTamil) "இந்த ஆப்பை திறக்கவும்" else "Open this App",
                        description = if (isTamil) "ஆப்பை திறந்ததும், 'Paste' பட்டனை அழுத்தினால் லிங்க் தானாகவே உள்ளிடப்படும்."
                        else "Open this app and tap the 'Paste' button to automatically insert the copied video link."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    GuideStepItem(
                        icon = Icons.Default.Download,
                        stepNumber = "படி 3",
                        title = if (isTamil) "'1-Click Download' அழுத்தவும்" else "Tap '1-Click Download'",
                        description = if (isTamil) "1080p Full HD அல்லது MP3 தரத்தை தேர்வு செய்து டவுன்லோட் செய்யுங்கள். வீடியோ உங்கள் கேலரியில் நேரடியாக சேமிக்கப்படும்!"
                        else "Select 1080p HD or MP3 audio, and tap Download. The video will be saved directly into your device Gallery!"
                    )
                }
            }
        }

        // Method 2: Android Share Sheet (Zero-copy instant download!)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("2", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isTamil) "முறை 2: பகிர்தல் (Share Sheet) மூலமாக" else "Method 2: One-Tap via Android Share",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    GuideStepItem(
                        icon = Icons.Default.Share,
                        stepNumber = "★",
                        title = if (isTamil) "நேரடி பகிர்வு மூலம் டவுன்லோட்" else "Share directly to Video Downloader",
                        description = if (isTamil) "எந்த சோஷியல் மீடியா ஆப்பிலும் 'Share' கொடுத்து பட்டியலில் உள்ள 'Video Downloader' ஆப்பை தேர்வு செய்யவும். தானாகவே ஆப் திறந்து 1-கிளிக்கில் டவுன்லோட் ஆகும்!"
                        else "When watching any video on YouTube, Instagram, or Facebook, tap 'Share via' and select 'Video Downloader' from the list. The app opens and starts the download immediately!"
                    )
                }
            }
        }

        // Supported Platforms Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTamil) "ஆதரிக்கப்படும் தளங்கள்" else "Supported Platforms & Features",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FeatureBullet(
                        text = if (isTamil) "YouTube: Shorts, வீடியோக்கள் & MP3 ஆடியோ" else "YouTube: Shorts, Videos & MP3 Audio extraction"
                    )
                    FeatureBullet(
                        text = if (isTamil) "Instagram: Reels, Stories & வீடியோ பதிவுகள்" else "Instagram: Reels, Stories, Post Videos"
                    )
                    FeatureBullet(
                        text = if (isTamil) "Facebook: Watch, வீடியோ ரீல்கள் & நேரடி கிளிப்புகள்" else "Facebook: Watch Videos, Reels & Clips"
                    )
                    FeatureBullet(
                        text = if (isTamil) "TikTok & Twitter/X: HD தர வீடியோக்கள் வாட்டர்மார்க் இன்றி" else "TikTok & Twitter/X: High Definition videos"
                    )
                    FeatureBullet(
                        text = if (isTamil) "இணையதள நேரடி MP4/WebM வீடியோ இணைப்புகள்" else "Direct Web MP4 / WebM video streaming links"
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun GuideStepItem(
    icon: ImageVector,
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FeatureBullet(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
