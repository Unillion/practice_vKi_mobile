package ci.nsu.mobile.main.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ci.nsu.mobile.main.data.models.GroupDto
import ci.nsu.mobile.main.data.models.PersonDto
import ci.nsu.mobile.main.data.models.RegisterRequest
import ci.nsu.mobile.main.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

data class SimpleDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    fun format(pattern: String = "yyyy-MM-dd"): String {
        val calendar = Calendar.getInstance().apply {
            set(year, month, day)
        }
        return SimpleDateFormat(pattern, Locale.getDefault()).format(calendar.time)
    }

    companion object {
        fun now(): SimpleDate {
            val calendar = Calendar.getInstance()
            return SimpleDate(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH),
                day = calendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun nowMinusYears(years: Int): SimpleDate {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -years)
            return SimpleDate(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH),
                day = calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingGroups = true)
            val result = authRepository.getGroups()

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    groups = (result.getOrNull() ?: emptyList()),
                    isLoadingGroups = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    groupsError = result.exceptionOrNull()?.message,
                    isLoadingGroups = false
                )
            }
        }
    }

    fun updateField(field: String, value: String) {
        _uiState.value = when (field) {
            "firstName" -> _uiState.value.copy(firstName = value)
            "lastName" -> _uiState.value.copy(lastName = value)
            "middleName" -> _uiState.value.copy(middleName = value)
            "login" -> _uiState.value.copy(login = value)
            "password" -> _uiState.value.copy(password = value)
            "email" -> _uiState.value.copy(email = value)
            "phoneNumber" -> _uiState.value.copy(phoneNumber = value)
            else -> _uiState.value
        }
    }

    fun updateGender(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateSelectedGroup(groupId: Int) {
        _uiState.value = _uiState.value.copy(selectedGroupId = groupId)
    }

    fun updateBirthDate(date: SimpleDate) {
        _uiState.value = _uiState.value.copy(birthDate = date)
    }

    fun register() {
        viewModelScope.launch {
            if (!validateForm()) return@launch

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val person = PersonDto(
                firstName = _uiState.value.firstName,
                lastName = _uiState.value.lastName,
                middleName = _uiState.value.middleName,
                birthDate = _uiState.value.birthDate.format(),
                gender = _uiState.value.gender,
                groupId = _uiState.value.selectedGroupId
            )

            val request = RegisterRequest(
                login = _uiState.value.login,
                password = _uiState.value.password,
                email = _uiState.value.email,
                phoneNumber = _uiState.value.phoneNumber,
                person = person
            )

            val result = authRepository.register(request)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    private fun validateForm(): Boolean {
        val errors = mutableListOf<String>()

        if (_uiState.value.firstName.isBlank()) errors.add("Введите имя")
        if (_uiState.value.lastName.isBlank()) errors.add("Введите фамилию")
        if (_uiState.value.login.length < 3) errors.add("Логин должен содержать минимум 3 символа")
        if (_uiState.value.password.length < 6) errors.add("Пароль должен содержать минимум 6 символов")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) {
            errors.add("Введите корректный email")
        }
        if (_uiState.value.phoneNumber.isBlank()) errors.add("Введите телефон")
        if (_uiState.value.gender.isBlank()) errors.add("Выберите пол")
        if (_uiState.value.selectedGroupId == 0) errors.add("Выберите группу")

        _uiState.value = _uiState.value.copy(
            validationErrors = errors,
            error = if (errors.isNotEmpty()) "Пожалуйста, исправьте ошибки" else null
        )

        return errors.isEmpty()
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}

data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String = "",
    val birthDate: SimpleDate = SimpleDate.nowMinusYears(18),
    val gender: String = "",
    val selectedGroupId: Int = 0,
    val groups: List<GroupDto> = emptyList(),
    val login: String = "",
    val password: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val isLoadingGroups: Boolean = false,
    val error: String? = null,
    val groupsError: String? = null,
    val isSuccess: Boolean = false,
    val validationErrors: List<String> = emptyList()
)