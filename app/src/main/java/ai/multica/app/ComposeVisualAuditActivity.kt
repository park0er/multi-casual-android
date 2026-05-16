package ai.multica.app

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ai.multica.app.ui.components.MulticaButtonTone
import ai.multica.app.ui.components.MulticaListRow
import ai.multica.app.ui.components.MulticaPillButton
import ai.multica.app.ui.components.MulticaShell
import ai.multica.app.ui.components.MulticaTab
import ai.multica.app.ui.components.StatusPill
import ai.multica.app.ui.theme.MulticaColors
import ai.multica.app.ui.theme.MulticaTheme

class ComposeVisualAuditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MulticaTheme {
                VisualAuditSandbox()
            }
        }
    }
}

private data class InboxPreview(
    val type: String,
    val status: String,
    val title: String,
    val body: String,
    val unread: Boolean,
)

private data class IssuePreview(
    val identifier: String,
    val title: String,
    val status: String,
    val priority: String,
    val assignee: String?,
)

@Composable
fun VisualAuditSandbox() {
    val selected = remember { mutableStateOf(MulticaTab.Inbox) }
    MulticaShell(
        selectedTab = selected.value,
        workspaceName = "park0er",
        languageLabel = "中文",
        onLanguageClick = {},
        onWorkspaceClick = { selected.value = MulticaTab.Settings },
        onSearchClick = {},
        onChatClick = { selected.value = MulticaTab.Inbox },
        onTabClick = { selected.value = it },
    ) {
        when (selected.value) {
            MulticaTab.Inbox -> InboxPreviewScreen()
            MulticaTab.Issues -> IssuesPreviewScreen(personal = false)
            MulticaTab.MyIssues -> IssuesPreviewScreen(personal = true)
            MulticaTab.Projects -> PlaceholderScreen("Projects", "Project list and detail will use the same list-row system.")
            MulticaTab.Settings -> PlaceholderScreen("Settings", "Settings will become grouped rows instead of a button stack.")
        }
    }
}

@Composable
private fun InboxPreviewScreen() {
    val rows = listOf(
        InboxPreview(
            type = "New Comment",
            status = "Todo",
            title = "核心功能：Issue 列表 & 详情页",
            body = "已确认 Android 视觉修正回写：Inbox / Issues 的 pill 控件、白底细描边卡片、底部选中态和正文截断都比之前更干净。",
            unread = true,
        ),
        InboxPreview(
            type = "Task Failed",
            status = "Todo",
            title = "核心功能：Issue 列表 & 详情页",
            body = "",
            unread = false,
        ),
        InboxPreview(
            type = "New Comment",
            status = "Done",
            title = "Android Markdown table QA",
            body = "表格、代码块、引用和普通段落都应该使用同一套 Markdown 视觉系统。",
            unread = false,
        ),
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            PageHeader(
                title = "Inbox",
                trailing = {
                    MulticaPillButton("Chat", onClick = {}, tone = MulticaButtonTone.Secondary)
                },
            )
        }
        items(rows) { row ->
            MulticaListRow(
                eyebrow = "${row.type} · ${row.status}",
                title = row.title,
                subtitle = row.body.ifBlank { null },
                unread = row.unread,
                onClick = {},
            )
        }
    }
}

@Composable
private fun IssuesPreviewScreen(personal: Boolean) {
    val rows = listOf(
        IssuePreview("PAR-62", "了解一下金事通 🍎", "Backlog", "Low", "ZhaoXishengGmail"),
        IssuePreview("PAR-68", "填写 W-8BEN 税务表格（税率 30%→10%）", "Backlog", "Low", null),
        IssuePreview("PAR-67", "绑定 Stripe 账户（Hong Kong / Individual）", "Backlog", "Low", null),
        IssuePreview("PAR-73", "核心功能：Issue 列表 & 详情页", "Todo", "High", "codex"),
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            PageHeader(
                title = if (personal) "My Issues" else "Issues",
                trailing = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MulticaPillButton("↓", onClick = {}, tone = MulticaButtonTone.Secondary)
                        MulticaPillButton("Board", onClick = {}, tone = MulticaButtonTone.Ghost)
                        MulticaPillButton("+", onClick = {}, tone = MulticaButtonTone.Primary)
                    }
                },
            )
        }
        item {
            Text(
                text = "Backlog (${rows.count { it.status == "Backlog" }})",
                style = MaterialTheme.typography.titleMedium,
                color = MulticaColors.Muted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        items(rows) { issue ->
            IssuePreviewRow(issue)
        }
    }
}

@Composable
private fun IssuePreviewRow(issue: IssuePreview) {
    MulticaListRow(
        eyebrow = issue.identifier,
        title = issue.title,
        subtitle = buildString {
            append(issue.status)
            append(" · ")
            append(issue.priority)
            if (!issue.assignee.isNullOrBlank()) {
                append(" · ")
                append(issue.assignee)
            }
        },
        unread = false,
        onClick = {},
    )
}

@Composable
private fun PlaceholderScreen(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PageHeader(title = title)
        StatusPill("Compose migration sandbox")
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MulticaColors.Muted,
        )
    }
}

@Composable
private fun PageHeader(
    title: String,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MulticaColors.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Spacer(modifier = Modifier.height(1.dp))
            trailing()
        }
    }
}
