package com.goody.dalda.ui.search.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goody.dalda.R
import com.goody.dalda.data.AlcoholInfo
import com.goody.dalda.data.AlcoholType

@Composable
fun SearchAlcoholTab(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    categoryCount: Map<AlcoholType, Int>,
    category: List<AlcoholType>,
    onSelectedIndex: (Int) -> Unit = {}
) {
    TabRow(
        selectedTabIndex = selectedIndex,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                color = Color.Black
            )
        }
    ) {
        category.forEachIndexed { index, type ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onSelectedIndex(index) },
                modifier = Modifier.padding(16.dp),
                unselectedContentColor = Color.Gray
            ) {
                Row {
                    Text(
                        modifier = Modifier.fillMaxHeight(),
                        text = type.alcoholName,
                        fontSize = 18.sp
                    )
                    Text(
                        modifier = Modifier.align(Alignment.Bottom),
                        text = " ${categoryCount[type]}",
                        color = colorResource(id = R.color.buttonBackground),
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SearchAlcoholTabPrev() {
    val alcoholInfoList = listOf(
        AlcoholInfo(
            id = 1,
            imgUrl = "https://duckduckgo.com/?q=fames",
            name = "SOJU_1",
            type = AlcoholType.SOJU,
            abv = "2.3%"
        ),
        AlcoholInfo(
            id = 2,
            imgUrl = "https://duckduckgo.com/?q=fames",
            name = "WHISKEY_1",
            type = AlcoholType.WISKY,
            abv = "2.3%"
        ),
        AlcoholInfo(
            id = 3,
            imgUrl = "https://duckduckgo.com/?q=fames",
            name = "WHISKEY_2",
            type = AlcoholType.WISKY,
            abv = "2.3%"
        ),
        AlcoholInfo(
            id = 4,
            imgUrl = "https://duckduckgo.com/?q=fames",
            name = "BEER_1",
            type = AlcoholType.BEER,
            abv = "2.3%"
        ),
        AlcoholInfo(
            id = 4,
            imgUrl = "https://duckduckgo.com/?q=fames",
            name = "BEER_2",
            type = AlcoholType.BEER,
            abv = "2.3%"
        ),
        AlcoholInfo(
            id = 4,
            imgUrl = "https://duckduckgo.com/?q=fames",
            name = "BEER_3",
            type = AlcoholType.BEER,
            abv = "2.3%"
        ),
    )

    val selectedIndex = remember { mutableStateOf(0) }
    val category = alcoholInfoList.map { it.type }.distinct()
    val categoryCount = alcoholInfoList.groupBy { it.type }
        .mapValues { it.value.size }

    SearchAlcoholTab(
        selectedIndex = selectedIndex.value,
        category = category,
        categoryCount = categoryCount
    )
}
