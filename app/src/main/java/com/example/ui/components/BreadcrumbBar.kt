package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProjectFileEntity
import com.example.ui.theme.LanternCyan
import com.example.ui.theme.LanternNeonGreen
import com.example.ui.theme.LanternObsidianBorder
import com.example.ui.theme.LanternObsidianCard
import com.example.ui.theme.LanternTextMuted
import com.example.ui.theme.LanternTextPrimary
import com.example.ui.theme.LanternTextSecondary

@Composable
fun BreadcrumbBar(
  activeFile: ProjectFileEntity?,
  allProjectFiles: List<ProjectFileEntity>,
  onSelectFile: (ProjectFileEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  if (activeFile == null) return

  val pathSegments = remember(activeFile.path) {
    activeFile.path.split("/").filter { it.isNotBlank() }
  }

  var expandedSegmentIndex by remember { mutableStateOf<Int?>(null) }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(LanternObsidianCard)
      .border(1.dp, LanternObsidianBorder)
      .padding(horizontal = 10.dp, vertical = 4.dp)
      .testTag("breadcrumb_navigation_bar"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Default.Folder,
      contentDescription = "Root",
      tint = LanternCyan,
      modifier = Modifier.size(14.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = "root",
      fontSize = 11.sp,
      fontFamily = FontFamily.Monospace,
      color = LanternTextMuted,
      fontWeight = FontWeight.Bold
    )

    if (pathSegments.isNotEmpty()) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = LanternTextMuted,
        modifier = Modifier.size(14.dp)
      )
    }

    LazyRow(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      itemsIndexed(pathSegments) { index, segment ->
        val isLast = index == pathSegments.size - 1
        val currentPrefix = pathSegments.take(index + 1).joinToString("/")

        Box {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .clickable {
                if (!isLast) {
                  expandedSegmentIndex = if (expandedSegmentIndex == index) null else index
                }
              }
              .padding(horizontal = 4.dp, vertical = 2.dp)
          ) {
            if (isLast) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = null,
                tint = LanternNeonGreen,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = segment,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = LanternNeonGreen,
                fontWeight = FontWeight.Bold
              )
            } else {
              Text(
                text = segment,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = LanternTextSecondary
              )
            }
          }

          // Dropdown for parent directory segments showing siblings & child files
          if (!isLast && expandedSegmentIndex == index) {
            val siblingFiles = remember(currentPrefix, allProjectFiles) {
              allProjectFiles.filter { file ->
                file.path.startsWith(currentPrefix) || file.path.contains("$segment/")
              }
            }

            DropdownMenu(
              expanded = true,
              onDismissRequest = { expandedSegmentIndex = null },
              modifier = Modifier.background(LanternObsidianCard).border(1.dp, LanternCyan)
            ) {
              Text(
                text = "Files in $segment/",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = LanternCyan,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
              siblingFiles.take(12).forEach { sibling ->
                DropdownMenuItem(
                  text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                        contentDescription = null,
                        tint = if (sibling.id == activeFile.id) LanternNeonGreen else LanternTextMuted,
                        modifier = Modifier.size(14.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = sibling.name,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (sibling.id == activeFile.id) LanternNeonGreen else LanternTextPrimary
                      )
                    }
                  },
                  onClick = {
                    expandedSegmentIndex = null
                    onSelectFile(sibling)
                  }
                )
              }
            }
          }
        }

        if (!isLast) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = LanternTextMuted,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}
