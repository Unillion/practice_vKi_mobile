package ci.nsu.mobile.main.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ci.nsu.mobile.main.ui.viewmodel.RegisterViewModel
import ci.nsu.mobile.main.ui.viewmodel.SimpleDate

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit, viewModel: RegisterViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Состояние для диалога выбора даты
    var showDatePicker by remember { mutableStateOf(false) }

    // Форматирование даты для отображения
    val formattedDate = remember(uiState.birthDate) {
        String.format(
            "%02d.%02d.%04d",
            uiState.birthDate.day,
            uiState.birthDate.month + 1,  // month в SimpleDate начинается с 0
            uiState.birthDate.year
        )
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.clearSuccess()
            onRegisterSuccess()
        }
    }

    // DatePicker диалог
    if (showDatePicker) {
        android.app.DatePickerDialog(
            context, { _, year, month, dayOfMonth ->
                viewModel.updateBirthDate(SimpleDate(year, month, dayOfMonth))
                showDatePicker = false
            }, uiState.birthDate.year, uiState.birthDate.month, uiState.birthDate.day
        ).apply {
            show()
            setOnDismissListener { showDatePicker = false }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Регистрация",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Personal Info
        OutlinedTextField(
            value = uiState.lastName,
            onValueChange = { viewModel.updateField("lastName", it) },
            label = { Text("Фамилия") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.validationErrors.any { it.contains("фамилию") })

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.firstName,
            onValueChange = { viewModel.updateField("firstName", it) },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.validationErrors.any { it.contains("имя") })

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.middleName,
            onValueChange = { viewModel.updateField("middleName", it) },
            label = { Text("Отчество") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Date picker поле
        OutlinedTextField(
            value = formattedDate,
            onValueChange = {},
            label = { Text("Дата рождения") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Выбрать дату")
                }
            })

        Spacer(modifier = Modifier.height(8.dp))

        // Gender - Radio Buttons
        Text("Пол", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row {
                RadioButton(
                    selected = uiState.gender == "MALE",
                    onClick = { viewModel.updateGender("MALE") })
                Text("Мужской", modifier = Modifier.padding(start = 4.dp))
            }
            Row {
                RadioButton(
                    selected = uiState.gender == "FEMALE",
                    onClick = { viewModel.updateGender("FEMALE") })
                Text("Женский", modifier = Modifier.padding(start = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Group dropdown
        var expanded by remember { mutableStateOf(false) }
        val selectedGroup = uiState.groups.find { it.groupId == uiState.selectedGroupId }

        ExposedDropdownMenuBox(
            expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selectedGroup?.groupName ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Группа") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = uiState.validationErrors.any { it.contains("группу") })

            ExposedDropdownMenu(
                expanded = expanded, onDismissRequest = { expanded = false }) {
                if (uiState.isLoadingGroups) {
                    DropdownMenuItem(text = { Text("Загрузка...") }, onClick = { })
                } else if (uiState.groupsError != null) {
                    DropdownMenuItem(text = { Text(uiState.groupsError!!) }, onClick = { })
                } else {
                    uiState.groups.forEach { group ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = group.groupName ?: "Группа ${group.groupId}"
                                )
                            },
                            onClick = {
                                viewModel.updateSelectedGroup(group.groupId)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account Info
        OutlinedTextField(
            value = uiState.login,
            onValueChange = { viewModel.updateField("login", it) },
            label = { Text("Логин") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.validationErrors.any { it.contains("Логин") })

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.updateField("password", it) },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.validationErrors.any { it.contains("Пароль") })

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.updateField("email", it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.validationErrors.any { it.contains("email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.phoneNumber,
            onValueChange = { viewModel.updateField("phoneNumber", it) },
            label = { Text("Телефон") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.register() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && !uiState.isLoadingGroups
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Зарегистрироваться")
            }
        }

        if (uiState.validationErrors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    uiState.validationErrors.forEach { error ->
                        Text(
                            text = "• $error",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (uiState.error != null && uiState.validationErrors.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = uiState.error!!,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}