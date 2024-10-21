package com.goody.dalda.ui.home.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.goody.dalda.ui.component.AutoResizedText
import com.goody.dalda.ui.component.OrangeColorButton

@Composable
fun LoginBanner(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        AutoResizedText(
            modifier = Modifier.padding(bottom = 16.dp),
            text = text,
            style = MaterialTheme.typography.titleLarge,
        )

        OrangeColorButton(
            text = "로그인하기 >",
            onClick = onClick
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun LoginBannerPreview() {
    LoginBanner(
        text = "달다에 로그인하고\n나만의 술도감을 만들어보세요"
    )
}
