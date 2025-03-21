package com.goody.dalda.ui.home

import android.util.Log
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goody.dalda.data.AlcoholData
import com.goody.dalda.data.RecommendAlcohol
import com.goody.dalda.data.repository.LoginRepository
import com.goody.dalda.data.repository.alcohol.AlcoholRepository
import com.goody.dalda.ui.home.data.UserProfile
import com.goody.dalda.util.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val alcoholRepository: AlcoholRepository,
    private val profileRepository: LoginRepository,
) : ViewModel() {
    private val _bookmarkAlcoholDataList = MutableStateFlow(emptyList<AlcoholData>())
    val bookmarkList: StateFlow<List<AlcoholData>> = _bookmarkAlcoholDataList

    private val _recommendAlcoholList = MutableStateFlow(emptyList<RecommendAlcohol>())
    val recommendAlcoholList: StateFlow<List<RecommendAlcohol>> = _recommendAlcoholList

    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.CommonState)
    val homeUiState: StateFlow<HomeUiState> = _homeUiState

    private val _authState = MutableStateFlow(determineInitialAuthState())
    val authState: StateFlow<AuthState> = _authState

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    private val _selectedItemIndex = MutableStateFlow(0)
    val selectedItemIndex: StateFlow<Int> = _selectedItemIndex

    private val _drawerState = MutableStateFlow(DrawerState(DrawerValue.Closed))
    val drawerState: StateFlow<DrawerState> = _drawerState

    init {
        fetchProfile()
    }

    /**
     * 다이얼로그의 표시 여부를 관리하는 상태
     */
    private val _isDialogVisible = MutableStateFlow(false)
    val isDialogVisible: StateFlow<Boolean> = _isDialogVisible

    fun setSelectedItemIndex(itemIndex: Int) {
        _selectedItemIndex.value = itemIndex
    }

    fun fetchProfile() {
        if (PreferenceManager.getAccessToken().isEmpty()) return
        viewModelScope.launch {
            try {
                val profile = profileRepository.getProfile()
                _userProfile.value = UserProfile(
                    profile.nickname,
                    profile.email,
                    profile.profileImg
                )
            } catch (e: IOException) {
                handleError(e, "네트워크 오류가 발생했습니다.")
            } catch (e: HttpException) {
                handleError(e, "서버 오류가 발생했습니다.")
            } catch (e: Exception) {
                handleError(e, "알 수 없는 오류가 발생했습니다.")
            }
        }
    }

    fun fetchBookmarkAlcoholList() {
        viewModelScope.launch {
            try {
                _bookmarkAlcoholDataList.value = alcoholRepository
                    .getBookmarkAlcoholList()
                    .asReversed()
            } catch (e: Exception) {
                handleError(e, "북마크 목록을 불러오는데 실패했습니다")
            }
        }
    }

    /**
     * 다이얼로그의 표시 상태를 설정합니다.
     * @param isVisible true일 경우 다이얼로그를 표시하고, false일 경우 숨깁니다.
     */
    fun setDialogVisible(isVisible: Boolean) {
        _isDialogVisible.value = isVisible
    }

    private fun handleError(e: Exception, errorMessage: String) {
        Log.e(TAG, "$errorMessage: $e")
        _homeUiState.value = HomeUiState.ErrorState(errorMessage)
    }

    private fun determineInitialAuthState(): AuthState {
        return if (PreferenceManager.getAccessToken().isEmpty()) {
            AuthState.SignOut
        } else {
            AuthState.SignIn
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
